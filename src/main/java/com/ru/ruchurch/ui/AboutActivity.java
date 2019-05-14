package com.ru.ruchurch.ui;

import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.text.Html;
import android.text.Spanned;
import android.text.method.LinkMovementMethod;
import android.widget.TextView;

import com.ru.ruchurch.R;
import com.ru.ruchurch.utils.ThemeHelper;

import static com.ru.ruchurch.ui.XmppActivity.configureActionBar;

public class AboutActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setTheme(ThemeHelper.find(this));

        setContentView(R.layout.activity_about);
        setSupportActionBar(findViewById(R.id.toolbar));
        configureActionBar(getSupportActionBar());
        setTitle(getString(R.string.title_activity_about_x, getString(R.string.app_name
        )));

        TextView termsOfUse = (TextView)findViewById(R.id.text_about);
        Spanned policy = Html.fromHtml(getString(R.string.pref_about_message));
        termsOfUse.setText(policy);
        termsOfUse.setMovementMethod(LinkMovementMethod.getInstance());

    }
}
