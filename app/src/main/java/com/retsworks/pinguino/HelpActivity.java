package com.retsworks.pinguino;

import android.os.Bundle;
import android.app.Activity;
import android.text.Html;
import android.widget.TextView;

public class HelpActivity extends Activity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_help);
        TextView helpText = (TextView)findViewById(R.id.helpText);
//        helpText.setText(getText(R.string.help_text));

        helpText.setText(Html.fromHtml(getString(R.string.help_text)));
    }
}
