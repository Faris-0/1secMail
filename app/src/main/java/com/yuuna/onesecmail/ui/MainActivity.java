package com.yuuna.onesecmail.ui;

import static android.Manifest.permission.POST_NOTIFICATIONS;

import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.view.MenuItem;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.yuuna.onesecmail.R;
import com.yuuna.onesecmail.util.NotificationService;

public class MainActivity extends AppCompatActivity implements BottomNavigationView.OnNavigationItemSelectedListener {

    private BottomNavigationView bottomNavigationView;

    private Boolean doubleBackToExit = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        if (Build.VERSION.SDK_INT >= 33 && ContextCompat.checkSelfPermission(this, POST_NOTIFICATIONS) == PackageManager.PERMISSION_DENIED) {
            ActivityCompat.requestPermissions(this, new String[]{POST_NOTIFICATIONS}, 1);
        }

        startService(new Intent(this, NotificationService.class));

        bottomNavigationView = findViewById(R.id.mNavigation);
        bottomNavigationView.setOnNavigationItemSelectedListener(this);
        if (getIntent().getBooleanExtra("isOPEN", false)) bottomNavigationView.setSelectedItemId(R.id.inbox);
        else bottomNavigationView.setSelectedItemId(R.id.home);
    }

    @Override
    public boolean onNavigationItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.home:
                getSupportFragmentManager().beginTransaction().replace(R.id.mContent, new home()).commit();
                return true;
            case R.id.inbox:
                getSupportFragmentManager().beginTransaction().replace(R.id.mContent, new inbox()).commit();
                return true;
        }
        return false;
    }

    @Override
    public void onBackPressed() {
        if (doubleBackToExit) finishAndRemoveTask();

        doubleBackToExit = true;
        Toast.makeText(this, "Tekan sekali lagi untuk keluar dari aplikasi", Toast.LENGTH_SHORT).show();

        new Handler(Looper.getMainLooper()).postDelayed(() -> doubleBackToExit = false, 2000);
    }
}