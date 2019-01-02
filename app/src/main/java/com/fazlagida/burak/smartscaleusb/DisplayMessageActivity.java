package com.fazlagida.burak.smartscaleusb;

import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.widget.TextView;

public class DisplayMessageActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_display_message);

        // Get the intent that started this activity and extract the message
        Intent intent = getIntent();
        String message = intent.getStringExtra( MainActivity.EXTRA_MESSAGE );

        // Capture the layout's TextView and set the string as its text
        TextView textView = findViewById( R.id.text_intent );
        textView.setText( message );
    }
}
