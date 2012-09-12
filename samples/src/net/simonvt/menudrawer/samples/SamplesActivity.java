package net.simonvt.menudrawer.samples;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;

public class SamplesActivity extends Activity {

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.main);

        findViewById(R.id.windowSample).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent i = new Intent(SamplesActivity.this, WindowSample.class);
                startActivity(i);
            }
        });

        findViewById(R.id.overlayWindowSample).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent i = new Intent(SamplesActivity.this, ActionBarOverlaySample.class);
                startActivity(i);
            }
        });

        findViewById(R.id.contentSample).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent i = new Intent(SamplesActivity.this, ContentSample.class);
                startActivity(i);
            }
        });

        findViewById(R.id.listActivitySample).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent i = new Intent(SamplesActivity.this, ListActivitySample.class);
                startActivity(i);
            }
        });
    }
}
