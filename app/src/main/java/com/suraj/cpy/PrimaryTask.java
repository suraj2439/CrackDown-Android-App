package com.suraj.cpy;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.os.Bundle;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;

import com.google.android.material.bottomnavigation.BottomNavigationView;

public class PrimaryTask extends AppCompatActivity {
    String senderName = "";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        Log.d("hello", "restarting");
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_groups);

        getSupportFragmentManager().beginTransaction().replace(R.id.group_container, new login()).commit();

        Log.d("hello", "Finished primary task");
    }
}