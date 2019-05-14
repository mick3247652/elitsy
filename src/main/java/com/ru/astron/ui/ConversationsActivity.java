/*
 * Copyright (c) 2018, Daniel Gultsch All rights reserved.
 *
 * Redistribution and use in source and binary forms, with or without modification,
 * are permitted provided that the following conditions are met:
 *
 * 1. Redistributions of source code must retain the above copyright notice, this
 * list of conditions and the following disclaimer.
 *
 * 2. Redistributions in binary form must reproduce the above copyright notice,
 * this list of conditions and the following disclaimer in the documentation and/or
 * other materials provided with the distribution.
 *
 * 3. Neither the name of the copyright holder nor the names of its contributors
 * may be used to endorse or promote products derived from this software without
 * specific prior written permission.
 *
 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS" AND
 * ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE IMPLIED
 * WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE
 * DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT HOLDER OR CONTRIBUTORS BE LIABLE FOR
 * ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES
 * (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES;
 * LOSS OF USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND ON
 * ANY THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT
 * (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF THIS
 * SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
 */

package com.ru.astron.ui;


import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.Dialog;
import android.app.Fragment;
import android.app.FragmentManager;
import android.app.FragmentTransaction;
import android.app.PendingIntent;
import android.content.ActivityNotFoundException;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.databinding.DataBindingUtil;
import android.net.Uri;
import android.os.Bundle;
import android.provider.Settings;
import android.support.annotation.IdRes;
import android.support.annotation.NonNull;
import android.support.design.widget.NavigationView;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBar;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.app.AlertDialog;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.util.Pair;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import com.ru.astron.ConfigRequests;
import com.ru.astron.utils.*;
import org.openintents.openpgp.util.OpenPgpApi;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.concurrent.atomic.AtomicBoolean;

import com.ru.astron.Config;
import com.ru.astron.R;
import com.ru.astron.crypto.OmemoSetting;
import com.ru.astron.databinding.ActivityConversationsBinding;
import com.ru.astron.entities.Account;
import com.ru.astron.entities.Bookmark;
import com.ru.astron.entities.Contact;
import com.ru.astron.entities.Conversation;
import com.ru.astron.services.XmppConnectionService;
import com.ru.astron.ui.interfaces.OnBackendConnected;
import com.ru.astron.ui.interfaces.OnConversationArchived;
import com.ru.astron.ui.interfaces.OnConversationRead;
import com.ru.astron.ui.interfaces.OnConversationSelected;
import com.ru.astron.ui.interfaces.OnConversationsListItemUpdated;
import com.ru.astron.ui.util.ActivityResult;
import com.ru.astron.ui.util.AvatarWorkerTask;
import com.ru.astron.ui.util.ConversationMenuConfigurator;
import com.ru.astron.ui.util.MenuDoubleTabUtil;
import com.ru.astron.ui.util.PendingItem;
import com.ru.astron.xmpp.OnUpdateBlocklist;

import rocks.xmpp.addr.Jid;

import static com.ru.astron.ui.ConversationFragment.REQUEST_DECRYPT_PGP;
import static com.ru.astron.ui.StartConversationActivity.getSelectedAccount;

public class ConversationsActivity extends XmppActivity implements AddChannelListener, OnConversationSelected, OnConversationArchived, OnConversationsListItemUpdated, OnConversationRead, XmppConnectionService.OnAccountUpdate, XmppConnectionService.OnConversationUpdate, XmppConnectionService.OnRosterUpdate, OnUpdateBlocklist, XmppConnectionService.OnShowErrorToast, XmppConnectionService.OnAffiliationChanged, CreatePrivateGroupChatDialog.CreateConferenceDialogListener, JoinConferenceDialog.JoinConferenceDialogListener, CreatePublicChannelDialog.CreatePublicChannelDialogListener {

    public static final String ACTION_VIEW_CONVERSATION = "eu.siacs.conversations.action.VIEW";
    public static final String EXTRA_CONVERSATION = "conversationUuid";
    public static final String EXTRA_DOWNLOAD_UUID = "eu.siacs.conversations.download_uuid";
    public static final String EXTRA_AS_QUOTE = "as_quote";
    public static final String EXTRA_NICK = "nick";
    public static final String EXTRA_IS_PRIVATE_MESSAGE = "pm";
    public static final String EXTRA_DO_NOT_APPEND = "do_not_append";

    private static List<String> VIEW_AND_SHARE_ACTIONS = Arrays.asList(
            ACTION_VIEW_CONVERSATION,
            Intent.ACTION_SEND,
            Intent.ACTION_SEND_MULTIPLE
    );

    public static final int REQUEST_OPEN_MESSAGE = 0x9876;
    public static final int REQUEST_PLAY_PAUSE = 0x5432;


    //secondary fragment (when holding the conversation, must be initialized before refreshing the overview fragment
    private static final @IdRes
    int[] FRAGMENT_ID_NOTIFICATION_ORDER = {R.id.secondary_fragment, R.id.main_fragment};
    private final PendingItem<Intent> pendingViewIntent = new PendingItem<>();
    private final PendingItem<ActivityResult> postponedActivityResult = new PendingItem<>();
    private ActivityConversationsBinding binding;
    private boolean mActivityPaused = true;
    private AtomicBoolean mRedirectInProcess = new AtomicBoolean(false);
    private List<String> mActivatedAccounts = new ArrayList<>();

    private Account mAccount = null;

    private static boolean isViewOrShareIntent(Intent i) {
        Log.d(Config.LOGTAG, "action: " + (i == null ? null : i.getAction()));
        return i != null && VIEW_AND_SHARE_ACTIONS.contains(i.getAction()) && i.hasExtra(EXTRA_CONVERSATION);
    }

    private static Intent createLauncherIntent(Context context) {
        final Intent intent = new Intent(context, ConversationsActivity.class);
        intent.setAction(Intent.ACTION_MAIN);
        intent.addCategory(Intent.CATEGORY_LAUNCHER);
        return intent;
    }

    @Override
    protected void refreshUiReal() {
        for (@IdRes int id : FRAGMENT_ID_NOTIFICATION_ORDER) {
            refreshFragment(id);
        }
    }

    @Override
    void onBackendConnected() {
        if (performRedirectIfNecessary(true)) {
            return;
        }
        xmppConnectionService.getNotificationService().setIsInForeground(true);
        Intent intent = pendingViewIntent.pop();
        if (intent != null) {
            if (processViewIntent(intent)) {
                if (binding.secondaryFragment != null) {
                    notifyFragmentOfBackendConnected(R.id.main_fragment);
                }
                invalidateActionBarTitle();
                return;
            }
        }
        for (@IdRes int id : FRAGMENT_ID_NOTIFICATION_ORDER) {
            notifyFragmentOfBackendConnected(id);
        }
        if (mPostponedActivityResult2 != null) {
            onActivityResult2(mPostponedActivityResult2.first, RESULT_OK, mPostponedActivityResult2.second);
            this.mPostponedActivityResult2 = null;
        }
        ActivityResult activityResult = postponedActivityResult.pop();
        if (activityResult != null) {
            handleActivityResult(activityResult);

        }

        invalidateActionBarTitle();
        if (binding.secondaryFragment != null && ConversationFragment.getConversation(this) == null) {
            Conversation conversation = ConversationsOverviewFragment.getSuggestion(this);
            if (conversation != null) {
                openConversation(conversation, null);
            }
        }
        showDialogsIfMainIsOverview();
        displayAccountInformation();
        fillActivateAccounts();
    }

    private boolean performRedirectIfNecessary(boolean noAnimation) {
        return performRedirectIfNecessary(null, noAnimation);
    }

    private boolean performRedirectIfNecessary(final Conversation ignore, final boolean noAnimation) {
        if (xmppConnectionService == null) {
            return false;
        }
        boolean isConversationsListEmpty = xmppConnectionService.isConversationsListEmpty(ignore);
        if (isConversationsListEmpty && mRedirectInProcess.compareAndSet(false, true)) {
            final Intent intent = SignupUtils.getRedirectionIntent(this);
            if (noAnimation) {
                intent.addFlags(Intent.FLAG_ACTIVITY_NO_ANIMATION);
            }
            runOnUiThread(() -> {
                startActivity(intent);
                if (noAnimation) {
                    overridePendingTransition(0, 0);
                }
            });
        }
        return mRedirectInProcess.get();
    }

    private void showDialogsIfMainIsOverview() {
        if (xmppConnectionService == null) {
            return;
        }
        final Fragment fragment = getFragmentManager().findFragmentById(R.id.main_fragment);
        if (fragment instanceof ConversationsOverviewFragment) {
            if (ExceptionHelper.checkForCrash(this)) {
                return;
            }
            openBatteryOptimizationDialogIfNeeded();
        }
    }

    private String getBatteryOptimizationPreferenceKey() {
        @SuppressLint("HardwareIds") String device = Settings.Secure.getString(getContentResolver(), Settings.Secure.ANDROID_ID);
        return "show_battery_optimization" + (device == null ? "" : device);
    }

    private void setNeverAskForBatteryOptimizationsAgain() {
        getPreferences().edit().putBoolean(getBatteryOptimizationPreferenceKey(), false).apply();
    }

    private void openBatteryOptimizationDialogIfNeeded() {
        if (hasAccountWithoutPush()
                && isOptimizingBattery()
                && android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.M
                && getPreferences().getBoolean(getBatteryOptimizationPreferenceKey(), true)) {
            AlertDialog.Builder builder = new AlertDialog.Builder(this);
            builder.setTitle(R.string.battery_optimizations_enabled);
            builder.setMessage(R.string.battery_optimizations_enabled_dialog);
            builder.setPositiveButton(R.string.next, (dialog, which) -> {
                Intent intent = new Intent(Settings.ACTION_REQUEST_IGNORE_BATTERY_OPTIMIZATIONS);
                Uri uri = Uri.parse("package:" + getPackageName());
                intent.setData(uri);
                try {
                    startActivityForResult(intent, REQUEST_BATTERY_OP);
                } catch (ActivityNotFoundException e) {
                    Toast.makeText(this, R.string.device_does_not_support_battery_op, Toast.LENGTH_SHORT).show();
                }
            });
            builder.setOnDismissListener(dialog -> setNeverAskForBatteryOptimizationsAgain());
            final AlertDialog dialog = builder.create();
            dialog.setCanceledOnTouchOutside(false);
            dialog.show();
        }
    }

    private boolean hasAccountWithoutPush() {
        for (Account account : xmppConnectionService.getAccounts()) {
            if (account.getStatus() == Account.State.ONLINE && !xmppConnectionService.getPushManagementService().available(account)) {
                return true;
            }
        }
        return false;
    }

    private void notifyFragmentOfBackendConnected(@IdRes int id) {
        final Fragment fragment = getFragmentManager().findFragmentById(id);
        if (fragment instanceof OnBackendConnected) {
            ((OnBackendConnected) fragment).onBackendConnected();
        }
    }

    private void refreshFragment(@IdRes int id) {
        final Fragment fragment = getFragmentManager().findFragmentById(id);
        if (fragment instanceof XmppFragment) {
            ((XmppFragment) fragment).refresh();
        }
    }

    private boolean processViewIntent(Intent intent) {
        String uuid = intent.getStringExtra(EXTRA_CONVERSATION);
        Conversation conversation = uuid != null ? xmppConnectionService.findConversationByUuid(uuid) : null;
        if (conversation == null) {
            Log.d(Config.LOGTAG, "unable to view conversation with uuid:" + uuid);
            return false;
        }
        openConversation(conversation, intent.getExtras());
        return true;
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String permissions[], @NonNull int[] grantResults) {
        UriHandlerActivity.onRequestPermissionResult(this, requestCode, grantResults);
        if (grantResults.length > 0) {
            if (grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                switch (requestCode) {
                    case REQUEST_OPEN_MESSAGE:
                        refreshUiReal();
                        ConversationFragment.openPendingMessage(this);
                        break;
                    case REQUEST_PLAY_PAUSE:
                        ConversationFragment.startStopPending(this);
                        break;
                }
            }
        }
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, final Intent data) {
        onActivityResult2(requestCode, resultCode, data);
        ActivityResult activityResult = ActivityResult.of(requestCode, resultCode, data);
        if (xmppConnectionService != null) {
            handleActivityResult(activityResult);
        } else {
            this.postponedActivityResult.push(activityResult);
        }
    }

    private void handleActivityResult(ActivityResult activityResult) {
        if (activityResult.resultCode == Activity.RESULT_OK) {
            handlePositiveActivityResult(activityResult.requestCode, activityResult.data);
        } else {
            handleNegativeActivityResult(activityResult.requestCode);
        }
    }

    private void handleNegativeActivityResult(int requestCode) {
        Conversation conversation = ConversationFragment.getConversationReliable(this);
        switch (requestCode) {
            case REQUEST_DECRYPT_PGP:
                if (conversation == null) {
                    break;
                }
                conversation.getAccount().getPgpDecryptionService().giveUpCurrentDecryption();
                break;
            case REQUEST_BATTERY_OP:
                setNeverAskForBatteryOptimizationsAgain();
                break;
        }
    }

    private void handlePositiveActivityResult(int requestCode, final Intent data) {
        Conversation conversation = ConversationFragment.getConversationReliable(this);
        if (conversation == null) {
            Log.d(Config.LOGTAG, "conversation not found");
            return;
        }
        switch (requestCode) {
            case REQUEST_DECRYPT_PGP:
                conversation.getAccount().getPgpDecryptionService().continueDecryption(data);
                break;
            case REQUEST_CHOOSE_PGP_ID:
                long id = data.getLongExtra(OpenPgpApi.EXTRA_SIGN_KEY_ID, 0);
                if (id != 0) {
                    conversation.getAccount().setPgpSignId(id);
                    announcePgp(conversation.getAccount(), null, null, onOpenPGPKeyPublished);
                } else {
                    choosePgpSignId(conversation.getAccount());
                }
                break;
            case REQUEST_ANNOUNCE_PGP:
                announcePgp(conversation.getAccount(), conversation, data, onOpenPGPKeyPublished);
                break;
        }
    }

    private ActionBarDrawerToggle toggle = null;
    private DrawerLayout drawerLayout = null;

    @Override
    protected void onCreate(final Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        ConversationMenuConfigurator.reloadFeatures(this);
        OmemoSetting.load(this);
        this.binding = DataBindingUtil.setContentView(this, R.layout.activity_conversations);
        setSupportActionBar((Toolbar) binding.toolbar);
        configureActionBar(getSupportActionBar());
        this.getFragmentManager().addOnBackStackChangedListener(this::invalidateActionBarTitle);
        this.getFragmentManager().addOnBackStackChangedListener(this::showDialogsIfMainIsOverview);
        this.initializeFragments();
        boolean pressBack = this.invalidateActionBarTitle();
        final Intent intent;
        if (savedInstanceState == null) {
            intent = getIntent();
        } else {
            intent = savedInstanceState.getParcelable("intent");
        }
        if (isViewOrShareIntent(intent)) {
            pendingViewIntent.push(intent);
            setIntent(createLauncherIntent(this));
        }

        drawerLayout = (DrawerLayout) findViewById(R.id.drawer_layout);

        Toolbar toolbar = (Toolbar) binding.toolbar;

        if (!pressBack) {
            toggle = new ActionBarDrawerToggle(
                    this, binding.drawerLayout, (Toolbar) binding.toolbar, R.string.navigation_drawer_open, R.string.navigation_drawer_close
            );
            binding.drawerLayout.addDrawerListener(toggle);
            toggle.setDrawerIndicatorEnabled(true);
            toggle.syncState();
            toggle.setToolbarNavigationClickListener(new View.OnClickListener() {

                @Override
                public void onClick(View v) {
                    FragmentManager fm = getFragmentManager();
                    if (fm.getBackStackEntryCount() > 0) {
                        try {
                            fm.popBackStack();
                        } catch (IllegalStateException e) {
                            Log.w(Config.LOGTAG, "Unable to pop back stack after pressing home button");
                        }
                        return;
                    }
                    finish();
                }
            });

            displayAccountInformation();
        }

    }


    private void displayAccountInformation() {
        if (xmppConnectionService == null) return;
        if (mAccount == null) {
            mAccount = AccountUtils.getFirst(xmppConnectionService);
        }

        NavigationView nv = (NavigationView) findViewById(R.id.nav_view);
        ImageView iv = (ImageView) nv.findViewById(R.id.photo);
        AvatarWorkerTask.loadAvatar(mAccount, iv, R.dimen.nav_avatar_size);

        TextView phone = (TextView) nv.findViewById(R.id.phone);
        TextView account_name = (TextView) nv.findViewById(R.id.account_name);

        String name = mAccount.getDisplayName();
        if (phone != null) phone.setText(ParsePhoneNumber.parse(mAccount.getJid().getLocal()));
        if (name.isEmpty()) {
            account_name.setText(ParsePhoneNumber.parse(mAccount.getJid().getLocal()));
            phone.setVisibility(View.GONE);
        } else {
            account_name.setText(name);
            phone.setVisibility(View.VISIBLE);
        }

        nv.setNavigationItemSelectedListener(menuItem -> {
            switch (menuItem.getItemId()) {
                case R.id.action_settings:
                    startActivity(new Intent(ConversationsActivity.this, SettingsActivity.class));
                    break;
                case R.id.action_account:
                    AccountUtils.launchManageAccount(ConversationsActivity.this);
                    break;
                case R.id.action_search:
                    startActivity(new Intent(ConversationsActivity.this, SearchActivity.class));
                    break;
                case R.id.join_public_channel:
                    showJoinConferenceDialog(null);
                    break;
                case R.id.create_public_channel:
                    showPublicChannelDialog();
                    break;
                case R.id.create_private_group_chat:
                    showCreatePrivateGroupChatDialog();
                    break;
                case R.id.create_contact:
                    showCreateContactDialog();
                    break;
                case R.id.create_contact_from_address_book:
                    showCreateContactFromAddressBook(null);
                    break;
                case R.id.news:
                    startActivityForResult(new Intent(this, NewsActivity.class), ConfigRequests.REQUEST_ADD_CHANNEL);
                    break;
                case R.id.channels:
                    Intent intent = new Intent(this, NewsActivity.class);
                    intent.putExtra("url","http://channel.astron.world/feed/");
                    intent.putExtra("title",getResources().getString(R.string.channels_title));
                    startActivityForResult(intent, ConfigRequests.REQUEST_ADD_CHANNEL);
                    break;
            }


            binding.drawerLayout.closeDrawers();
            return true;
        });
    }

    @SuppressLint("InflateParams")
    protected void showJoinConferenceDialog(final String prefilledJid) {
        android.support.v4.app.FragmentTransaction ft = getSupportFragmentManager().beginTransaction();
        android.support.v4.app.Fragment prev = getSupportFragmentManager().findFragmentByTag(FRAGMENT_TAG_DIALOG);
        if (prev != null) {
            ft.remove(prev);
        }
        ft.addToBackStack(null);
        JoinConferenceDialog joinConferenceFragment = JoinConferenceDialog.newInstance(prefilledJid, mActivatedAccounts);
        joinConferenceFragment.show(ft, FRAGMENT_TAG_DIALOG);
    }

    private void showCreatePrivateGroupChatDialog() {
        android.support.v4.app.FragmentTransaction ft = getSupportFragmentManager().beginTransaction();
        android.support.v4.app.Fragment prev = getSupportFragmentManager().findFragmentByTag(FRAGMENT_TAG_DIALOG);
        if (prev != null) {
            ft.remove(prev);
        }
        ft.addToBackStack(null);
        CreatePrivateGroupChatDialog createConferenceFragment = CreatePrivateGroupChatDialog.newInstance(mActivatedAccounts);
        createConferenceFragment.show(ft, FRAGMENT_TAG_DIALOG);
    }

    private void showPublicChannelDialog() {
        android.support.v4.app.FragmentTransaction ft = getSupportFragmentManager().beginTransaction();
        android.support.v4.app.Fragment prev = getSupportFragmentManager().findFragmentByTag(FRAGMENT_TAG_DIALOG);
        if (prev != null) {
            ft.remove(prev);
        }
        ft.addToBackStack(null);
        CreatePublicChannelDialog dialog = CreatePublicChannelDialog.newInstance(mActivatedAccounts);
        dialog.show(ft, FRAGMENT_TAG_DIALOG);
    }

    private void fillActivateAccounts() {
        this.mActivatedAccounts.clear();
        for (Account account : xmppConnectionService.getAccounts()) {
            if (account.getStatus() != Account.State.DISABLED) {
                if (Config.DOMAIN_LOCK != null) {
                    this.mActivatedAccounts.add(account.getJid().getLocal());
                } else {
                    this.mActivatedAccounts.add(account.getJid().asBareJid().toString());
                }
            }
        }
    }

    @SuppressLint("InflateParams")
    protected void showCreateContactDialog() {
        android.support.v4.app.FragmentTransaction ft = getSupportFragmentManager().beginTransaction();
        android.support.v4.app.Fragment prev = getSupportFragmentManager().findFragmentByTag(FRAGMENT_TAG_DIALOG);
        if (prev != null) {
            ft.remove(prev);
        }
        ft.addToBackStack(null);
        EnterJidDialog dialog = EnterJidDialog.newInstance(
                mActivatedAccounts,
                getString(R.string.add_contact),
                getString(R.string.add),
                null,
                null,
                true);

        dialog.setOnEnterJidDialogPositiveListener((accountJid, contactJid) -> {
            if (!xmppConnectionServiceBound) {
                return false;
            }

            final Account account = xmppConnectionService.findAccountByJid(accountJid);
            if (account == null) {
                return true;
            }

            final Contact contact = account.getRoster().getContact(contactJid);
            if (contact.isSelf()) {
                switchToConversation(contact);
                return true;
            } else if (contact.showInRoster()) {
                throw new EnterJidDialog.JidError(getString(R.string.contact_already_exists));
            } else {
                xmppConnectionService.createContact(contact, true);
                switchToConversationDoNotAppend(contact, null);
                return true;
            }
        });
        dialog.show(ft, FRAGMENT_TAG_DIALOG);
    }

    protected void switchToConversationDoNotAppend(Contact contact, String body) {
        Conversation conversation = xmppConnectionService.findOrCreateConversation(contact.getAccount(), contact.getJid(), false, true);
        switchToConversationDoNotAppendWOfinish(conversation, body);

    }

    @Override
    public void onBackPressed() {
        if (binding.drawerLayout.isDrawerOpen(GravityCompat.START)) {
            binding.drawerLayout.closeDrawer(GravityCompat.START);
        } else {
            super.onBackPressed();
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.activity_conversations, menu);
        AccountUtils.showHideMenuItems(menu);
        MenuItem qrCodeScanMenuItem = menu.findItem(R.id.action_scan_qr_code);
        if (qrCodeScanMenuItem != null) {
            if (isCameraFeatureAvailable()) {
                Fragment fragment = getFragmentManager().findFragmentById(R.id.main_fragment);
                boolean visible = getResources().getBoolean(R.bool.show_qr_code_scan)
                        && fragment != null
                        && fragment instanceof ConversationsOverviewFragment;
                qrCodeScanMenuItem.setVisible(visible);
            } else {
                qrCodeScanMenuItem.setVisible(false);
            }
        }
        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public void onConversationSelected(Conversation conversation) {
        clearPendingViewIntent();
        if (ConversationFragment.getConversation(this) == conversation) {
            Log.d(Config.LOGTAG, "ignore onConversationSelected() because conversation is already open");
            return;
        }
        openConversation(conversation, null);
    }

    public void clearPendingViewIntent() {
        if (pendingViewIntent.clear()) {
            Log.e(Config.LOGTAG, "cleared pending view intent");
        }
    }

    private void displayToast(final String msg) {
        runOnUiThread(() -> Toast.makeText(ConversationsActivity.this, msg, Toast.LENGTH_SHORT).show());
    }

    @Override
    public void onAffiliationChangedSuccessful(Jid jid) {

    }

    @Override
    public void onAffiliationChangeFailed(Jid jid, int resId) {
        displayToast(getString(resId, jid.asBareJid().toString()));
    }

    private void openConversation(Conversation conversation, Bundle extras) {
        openConversation(conversation, extras, false);
    }

    private void openConversation(Conversation conversation, Bundle extras, boolean hideInput) {
        ConversationFragment conversationFragment = (ConversationFragment) getFragmentManager().findFragmentById(R.id.secondary_fragment);

        final boolean mainNeedsRefresh;
        if (conversationFragment == null) {
            mainNeedsRefresh = false;
            Fragment mainFragment = getFragmentManager().findFragmentById(R.id.main_fragment);
            if (mainFragment instanceof ConversationFragment) {
                conversationFragment = (ConversationFragment) mainFragment;
                conversationFragment.setAddChannelListener(this);
            } else {
                conversationFragment = new ConversationFragment();
                conversationFragment.setAddChannelListener(this);
                FragmentTransaction fragmentTransaction = getFragmentManager().beginTransaction();
                fragmentTransaction.replace(R.id.main_fragment, conversationFragment);
                fragmentTransaction.addToBackStack(null);
                try {
                    fragmentTransaction.commit();
                } catch (IllegalStateException e) {
                    Log.w(Config.LOGTAG, "sate loss while opening conversation", e);
                    //allowing state loss is probably fine since view intents et all are already stored and a click can probably be 'ignored'
                    return;
                }
            }
        } else {
            mainNeedsRefresh = true;
        }

        conversationFragment.hideInput = hideInput;
        conversationFragment.reInit(conversation, extras == null ? new Bundle() : extras);
        conversationFragment.setAddChannelListener(this);
        if (mainNeedsRefresh) {
            refreshFragment(R.id.main_fragment);
        } else {
            invalidateActionBarTitle();
        }
    }

    public boolean onXmppUriClicked(Uri uri) {
        XmppUri xmppUri = new XmppUri(uri);
        if (xmppUri.isJidValid() && !xmppUri.hasFingerprints()) {
            final Conversation conversation = xmppConnectionService.findUniqueConversationByJid(xmppUri);
            if (conversation != null) {
                openConversation(conversation, null);
                return true;
            }
        }
        return false;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (MenuDoubleTabUtil.shouldIgnoreTap()) {
            return false;
        }
        switch (item.getItemId()) {
            //case R.id.action_donate:
            //Intent intent = new Intent(this, WebViewActivity.class);
            //startActivity(intent);
            //    return true;

            case android.R.id.home:
                FragmentManager fm = getFragmentManager();
                if (fm.getBackStackEntryCount() > 0) {
                    try {
                        fm.popBackStack();
                    } catch (IllegalStateException e) {
                        Log.w(Config.LOGTAG, "Unable to pop back stack after pressing home button");
                    }
                    return true;
                }
                break;
            case R.id.action_scan_qr_code:
                UriHandlerActivity.scan(this);
                return true;
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    public void onSaveInstanceState(Bundle savedInstanceState) {
        Intent pendingIntent = pendingViewIntent.peek();
        savedInstanceState.putParcelable("intent", pendingIntent != null ? pendingIntent : getIntent());
        super.onSaveInstanceState(savedInstanceState);
    }

    @Override
    protected void onStart() {
        final int theme = findTheme();
        if (this.mTheme != theme) {
            this.mSkipBackgroundBinding = true;
            recreate();
        } else {
            this.mSkipBackgroundBinding = false;
        }
        mRedirectInProcess.set(false);
        super.onStart();
    }

    @Override
    protected void onNewIntent(final Intent intent) {
        if (isViewOrShareIntent(intent)) {
            if (xmppConnectionService != null) {
                clearPendingViewIntent();
                processViewIntent(intent);
            } else {
                pendingViewIntent.push(intent);
            }
        }
        setIntent(createLauncherIntent(this));
    }

    @Override
    public void onPause() {
        this.mActivityPaused = true;
        super.onPause();
    }

    @Override
    public void onResume() {
        super.onResume();
        this.mActivityPaused = false;
    }

    private void initializeFragments() {
        FragmentTransaction transaction = getFragmentManager().beginTransaction();
        Fragment mainFragment = getFragmentManager().findFragmentById(R.id.main_fragment);
        Fragment secondaryFragment = getFragmentManager().findFragmentById(R.id.secondary_fragment);
        if (mainFragment != null) {
            if (binding.secondaryFragment != null) {
                if (mainFragment instanceof ConversationFragment) {
                    getFragmentManager().popBackStack();
                    transaction.remove(mainFragment);
                    transaction.commit();
                    getFragmentManager().executePendingTransactions();
                    transaction = getFragmentManager().beginTransaction();
                    transaction.replace(R.id.secondary_fragment, mainFragment);
                    transaction.replace(R.id.main_fragment, new ConversationsOverviewFragment());
                    transaction.commit();
                    return;
                }
            } else {
                if (secondaryFragment instanceof ConversationFragment) {
                    transaction.remove(secondaryFragment);
                    transaction.commit();
                    getFragmentManager().executePendingTransactions();
                    transaction = getFragmentManager().beginTransaction();
                    transaction.replace(R.id.main_fragment, secondaryFragment);
                    transaction.addToBackStack(null);
                    transaction.commit();
                    return;
                }
            }
        } else {
            transaction.replace(R.id.main_fragment, new ConversationsOverviewFragment());
        }
        if (binding.secondaryFragment != null && secondaryFragment == null) {
            transaction.replace(R.id.secondary_fragment, new ConversationFragment());
        }
        transaction.commit();
    }

    private boolean invalidateActionBarTitle() {
        final ActionBar actionBar = getSupportActionBar();
        if (actionBar != null) {
            Fragment mainFragment = getFragmentManager().findFragmentById(R.id.main_fragment);
            if (mainFragment instanceof ConversationFragment) {
                final Conversation conversation = ((ConversationFragment) mainFragment).getConversation();
                if (conversation != null) {

                    actionBar.setTitle(EmojiWrapper.transform(conversation.getName()));
                    actionBar.setDisplayHomeAsUpEnabled(false);
                    if (drawerLayout != null) drawerLayout.setDrawerLockMode(DrawerLayout.LOCK_MODE_LOCKED_CLOSED);
                    if (toggle != null) {
                        toggle.setDrawerIndicatorEnabled(false);
                        toggle.syncState();
                    }
                    actionBar.setDisplayHomeAsUpEnabled(true);

                    return true;
                }
            }
            actionBar.setTitle(R.string.app_name);
            actionBar.setDisplayHomeAsUpEnabled(false);

            if (drawerLayout != null) drawerLayout.setDrawerLockMode(DrawerLayout.LOCK_MODE_UNLOCKED);
            if (toggle != null) {
                toggle.setDrawerIndicatorEnabled(true);
                toggle.syncState();
            }
        }
        return false;
    }

    @Override
    public boolean onSupportNavigateUp() {
        return super.onSupportNavigateUp();
    }

    @Override
    public void onConversationArchived(Conversation conversation) {
        if (performRedirectIfNecessary(conversation, false)) {
            return;
        }
        Fragment mainFragment = getFragmentManager().findFragmentById(R.id.main_fragment);
        if (mainFragment instanceof ConversationFragment) {
            try {
                getFragmentManager().popBackStack();
            } catch (IllegalStateException e) {
                Log.w(Config.LOGTAG, "state loss while popping back state after archiving conversation", e);
                //this usually means activity is no longer active; meaning on the next open we will run through this again
            }
            return;
        }
        Fragment secondaryFragment = getFragmentManager().findFragmentById(R.id.secondary_fragment);
        if (secondaryFragment instanceof ConversationFragment) {
            if (((ConversationFragment) secondaryFragment).getConversation() == conversation) {
                Conversation suggestion = ConversationsOverviewFragment.getSuggestion(this, conversation);
                if (suggestion != null) {
                    openConversation(suggestion, null);
                }
            }
        }
    }

    @Override
    public void onConversationsListItemUpdated() {
        Fragment fragment = getFragmentManager().findFragmentById(R.id.main_fragment);
        if (fragment instanceof ConversationsOverviewFragment) {
            ((ConversationsOverviewFragment) fragment).refresh();
        }
    }

    @Override
    public void switchToConversation(Conversation conversation) {
        switchToConversation(conversation, false);
    }


    public void switchToConversation(Conversation conversation, boolean hideInput) {
        Log.d(Config.LOGTAG, "override");
        openConversation(conversation, null, hideInput);
    }

    protected void switchToConversation(Contact contact) {
        Conversation conversation = xmppConnectionService.findOrCreateConversation(contact.getAccount(), contact.getJid(), false, true);
        switchToConversation(conversation);
    }

    @Override
    public void onConversationRead(Conversation conversation, String upToUuid) {
        if (!mActivityPaused && pendingViewIntent.peek() == null) {
            xmppConnectionService.sendReadMarker(conversation, upToUuid);
        } else {
            Log.d(Config.LOGTAG, "ignoring read callback. mActivityPaused=" + Boolean.toString(mActivityPaused));
        }
    }

    @Override
    public void onAccountUpdate() {
        this.refreshUi();
    }

    @Override
    public void onConversationUpdate() {
        if (performRedirectIfNecessary(false)) {
            return;
        }
        this.refreshUi();
    }

    @Override
    public void onRosterUpdate() {
        this.refreshUi();
    }

    @Override
    public void OnUpdateBlocklist(OnUpdateBlocklist.Status status) {
        this.refreshUi();
    }

    @Override
    public void onShowErrorToast(int resId) {
        runOnUiThread(() -> Toast.makeText(this, resId, Toast.LENGTH_SHORT).show());
    }

    private final int REQUEST_CREATE_CONFERENCE = 0x39da;

    @Override
    public void onCreateDialogPositiveClick(Spinner spinner, String name) {
        if (!xmppConnectionServiceBound) {
            return;
        }
        final Account account = getSelectedAccount(this, spinner);
        if (account == null) {
            return;
        }
        Intent intent = new Intent(getApplicationContext(), ChooseContactActivity.class);
        intent.putExtra(ChooseContactActivity.EXTRA_SHOW_ENTER_JID, false);
        intent.putExtra(ChooseContactActivity.EXTRA_SELECT_MULTIPLE, true);
        intent.putExtra(ChooseContactActivity.EXTRA_GROUP_CHAT_NAME, name.trim());
        intent.putExtra(ChooseContactActivity.EXTRA_ACCOUNT, account.getJid().asBareJid().toString());
        intent.putExtra(ChooseContactActivity.EXTRA_TITLE_RES_ID, R.string.choose_participants);
        startActivityForResult(intent, REQUEST_CREATE_CONFERENCE);
    }

    @Override
    public void onCreatePublicChannel(Account account, String name, Jid address) {
        mToast = Toast.makeText(this, R.string.creating_channel, Toast.LENGTH_LONG);
        mToast.show();
        xmppConnectionService.createPublicChannel(account, name, address, new UiCallback<Conversation>() {
            @Override
            public void success(Conversation conversation) {
                runOnUiThread(() -> {
                    hideToast();
                    switchToConversation(conversation);
                });

            }

            @Override
            public void error(int errorCode, Conversation conversation) {
                runOnUiThread(() -> {
                    replaceToast(getString(errorCode));
                    switchToConversation(conversation);
                });
            }

            @Override
            public void userInputRequried(PendingIntent pi, Conversation object) {

            }
        });
    }

    @Override
    public void onJoinDialogPositiveClick(Dialog dialog, Spinner spinner, EditText jid, boolean isBookmarkChecked) {
        if (!xmppConnectionServiceBound) {
            return;
        }
        final Account account = getSelectedAccount(this, spinner);
        if (account == null) {
            return;
        }
        final Jid conferenceJid;
        try {
            conferenceJid = Jid.of(jid.getText().toString() + "@conference." + Config.DOMAIN_LOCK);
        } catch (final IllegalArgumentException e) {
            jid.setError(getString(R.string.invalid_jid));
            return;
        }

        if (isBookmarkChecked) {
            if (account.hasBookmarkFor(conferenceJid)) {
                jid.setError(getString(R.string.bookmark_already_exists));
            } else {
                final Bookmark bookmark = new Bookmark(account, conferenceJid.asBareJid());
                bookmark.setAutojoin(getBooleanPreference("autojoin", R.bool.autojoin));
                String nick = conferenceJid.getResource();
                if (nick != null && !nick.isEmpty()) {
                    bookmark.setNick(nick);
                }
                account.getBookmarks().add(bookmark);
                xmppConnectionService.pushBookmarks(account);
                final Conversation conversation = xmppConnectionService
                        .findOrCreateConversation(account, conferenceJid, true, true, true);
                bookmark.setConversation(conversation);
                dialog.dismiss();
                switchToConversation(conversation);
            }
        } else {
            final Conversation conversation = xmppConnectionService
                    .findOrCreateConversation(account, conferenceJid, true, true, true);
            dialog.dismiss();
            switchToConversation(conversation, true);
        }
    }

    private Pair<Integer, Intent> mPostponedActivityResult2;

    public void onActivityResult2(int requestCode, int resultCode, Intent intent) {
        if (resultCode == RESULT_OK) {
            if (xmppConnectionServiceBound) {
                this.mPostponedActivityResult2 = null;
                if (requestCode == REQUEST_CREATE_CONFERENCE) {
                    Account account = extractAccount(intent);
                    final String name = intent.getStringExtra(ChooseContactActivity.EXTRA_GROUP_CHAT_NAME);
                    final List<Jid> jids = ChooseContactActivity.extractJabberIds(intent);
                    if (account != null && jids.size() > 0) {
                        if (xmppConnectionService.createAdhocConference(account, name, jids, mAdhocConferenceCallback)) {
                            mToast = Toast.makeText(this, R.string.creating_conference, Toast.LENGTH_LONG);
                            mToast.show();
                        }
                    }
                } else if (requestCode == REQUEST_CODE_PHONE_SELECT) {
                    if (intent != null) {
                        String contactJid = intent.getStringExtra("phone") + "@astron.online";
                        if (mActivatedAccounts.size() >= 1) {
                            String accountJid = mActivatedAccounts.get(0);
                            Log.v("PHONE", accountJid);
                            addPhoneContact(contactJid, accountJid);
                        }
                    }
                } else if(requestCode == ConfigRequests.REQUEST_ADD_CHANNEL) {
                    addChannel(intent.getStringExtra("channel"));
                }
            } else {
                this.mPostponedActivityResult2 = new Pair<>(requestCode, intent);
            }
        }
        super.onActivityResult(requestCode, requestCode, intent);
    }

    private UiCallback<Conversation> mAdhocConferenceCallback = new UiCallback<Conversation>() {
        @Override
        public void success(final Conversation conversation) {
            runOnUiThread(() -> {
                hideToast();
                switchToConversation(conversation);
            });
        }

        @Override
        public void error(final int errorCode, Conversation object) {
            runOnUiThread(() -> replaceToast(getString(errorCode)));
        }

        @Override
        public void userInputRequried(PendingIntent pi, Conversation object) {

        }
    };
    private final int REQUEST_CODE_PHONE_SELECT = 0x3777;

    private void showCreateContactFromAddressBook(final String prefilledJid) {
        Intent i = new Intent(this, PhoneSelectActivity.class);
        startActivityForResult(i, REQUEST_CODE_PHONE_SELECT);
    }

    private boolean addPhoneContact(String cJid, String aJid) {
        Jid accountJid = Jid.of(aJid, Config.DOMAIN_LOCK, null);
        Jid contactJid = Jid.of(cJid);

        if (!xmppConnectionServiceBound) {
            return false;
        }

        final Account account = xmppConnectionService.findAccountByJid(accountJid);
        if (account == null) {
            return true;
        }

        final Contact contact = account.getRoster().getContact(contactJid);
        if (contact.isSelf()) {
            switchToConversation(contact);
            return true;
        } else {
            xmppConnectionService.createContact(contact, true);
            switchToConversationDoNotAppend(contact, null);
            return true;
        }
    }


    @Override
    public void addChannel(String channel) {
        //Toast.makeText(this, "Add channel " + channel, Toast.LENGTH_SHORT).show();
        if (!xmppConnectionServiceBound) {
            return;
        }
        final Account account = mAccount;
        if (account == null) {
            return;
        }
        final Jid conferenceJid;
        try {
            conferenceJid = Jid.of(channel + "@conference." + Config.DOMAIN_LOCK);
        } catch (final IllegalArgumentException e) {
            return;
        }


        final Conversation conversation = xmppConnectionService
                .findOrCreateConversation(account, conferenceJid, true, true, true);
        switchToConversation(conversation, true);
    }

}
