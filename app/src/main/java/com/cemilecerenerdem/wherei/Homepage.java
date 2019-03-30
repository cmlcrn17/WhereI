package com.cemilecerenerdem.wherei;

import android.app.Activity;
import android.os.Bundle;
import android.support.annotation.Nullable;

public class Homepage extends Activity {
    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.homepage);
        String username = getIntent().getStringExtra("username");
    }
}
