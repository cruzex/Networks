package com.example.android.wifidirect;


import android.content.Intent;
import android.os.Bundle;
import android.app.Activity;
import android.view.View;

/**
 * Created by Simarjit on 21-Mar-17.
 */

public class HomeActivity extends Activity {

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.home);

    }

    public void left_tab(View view) {
        Intent intent = new Intent(HomeActivity.this, WiFiDirectActivity.class);
        startActivity(intent);
    }

    public void right_tab(View view) {
        Intent intent = new Intent(HomeActivity.this, PlaylistActivity.class);
        startActivity(intent);
    }
}
