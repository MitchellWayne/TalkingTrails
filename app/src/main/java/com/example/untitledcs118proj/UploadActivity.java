package com.example.untitledcs118proj;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;

public class UploadActivity extends MapActivity {
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_profile);
    }

    public void returnMap(View view) {
        finish();
    }
}
