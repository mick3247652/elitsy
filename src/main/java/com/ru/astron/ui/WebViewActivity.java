package com.ru.astron.ui;

import android.annotation.TargetApi;
import android.app.Activity;
import android.content.Intent;
import android.databinding.DataBindingUtil;
import android.os.Build;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.Toolbar;
import android.view.MenuItem;
import android.webkit.WebResourceRequest;
import android.webkit.WebView;
import android.webkit.WebViewClient;

import com.ru.astron.R;
import com.ru.astron.databinding.ActivityWebViewBinding;
import com.ru.astron.ui.util.MenuDoubleTabUtil;
import com.ru.astron.utils.ParseURL;

import static com.ru.astron.ui.ActionBarActivity.configureActionBar;

public class WebViewActivity extends AppCompatActivity {

    private ActivityWebViewBinding binding;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        this.binding = DataBindingUtil.setContentView(this, R.layout.activity_web_view);
        Toolbar toolbar = (Toolbar) binding.toolbar;
        setSupportActionBar(toolbar);
        configureActionBar(getSupportActionBar());

        Intent i = getIntent();
        String sUrl = i.getStringExtra("url");

        if(sUrl == null) setTitle(getResources().getString(R.string.title_action_donate));


        WebViewClient webViewClient = new WebViewClient() {
            @SuppressWarnings("deprecation") @Override
            public boolean shouldOverrideUrlLoading(WebView view, String url) {
                return loadURL(view, url);
            }

            @TargetApi(Build.VERSION_CODES.N) @Override
            public boolean shouldOverrideUrlLoading(WebView view, WebResourceRequest request) {
                return loadURL(view, request.getUrl().toString());
            }

            private boolean loadURL(WebView view, String url){
                if(ParseURL.INSTANCE.testDomain(url,"astron")) {
                    String channel = ParseURL.INSTANCE.getChannel(url);
                    onAddChannel(channel);
                    return true;
                } else {
                    view.loadUrl(url);
                }
                return true;
            }
        };

        binding.webView.setWebViewClient(webViewClient);
        binding.webView.getSettings().setJavaScriptEnabled(true);
        if(sUrl == null) binding.webView.loadUrl("https://yasobe.ru/na/astron2");
        else binding.webView.loadUrl(sUrl);
    }

    private void onAddChannel(String channel){
        Intent intent = new Intent();
        intent.putExtra("channel", channel);
        setResult(Activity.RESULT_OK, intent);
        finish() ;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (MenuDoubleTabUtil.shouldIgnoreTap()) {
            return false;
        }
        switch (item.getItemId()) {
            case android.R.id.home:
                finish();
                return true;
        }
        return super.onOptionsItemSelected(item);
    }

}
