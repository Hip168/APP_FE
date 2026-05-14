package com.example.btck.activities;

import android.content.Intent;
import android.os.Bundle;
import android.view.MenuItem;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.Fragment;
import com.example.btck.R;
import com.example.btck.databinding.ActivityMainBinding;
import com.example.btck.fragments.HomeFragment;
import com.example.btck.fragments.GroupsFragment;
import com.example.btck.fragments.NotificationsFragment;
import com.example.btck.fragments.ProfileFragment;
import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.android.material.floatingactionbutton.FloatingActionButton;

public class MainActivity extends AppCompatActivity {

    private ActivityMainBinding binding;
    private Fragment currentFragment;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityMainBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        setupBottomNavigation();
        setupFab();

        // Show home by default
        if (savedInstanceState == null) {
            loadFragment(new HomeFragment());
        }
    }

    private void setupBottomNavigation() {
        binding.bottomNav.setOnItemSelectedListener(item -> {
            int id = item.getItemId();
            if (id == R.id.nav_home) {
                loadFragment(new HomeFragment());
                return true;
            } else if (id == R.id.nav_groups) {
                loadFragment(new GroupsFragment());
                return true;
            } else if (id == R.id.nav_notifications) {
                loadFragment(new NotificationsFragment());
                return true;
            } else if (id == R.id.nav_profile) {
                loadFragment(new ProfileFragment());
                return true;
            }
            return false;
        });
    }

    private void setupFab() {
        binding.fabAddExpense.setOnClickListener(v -> {
            // Open add expense - require event selection first
            showAddExpenseMenu();
        });
    }

    private void showAddExpenseMenu() {
        // Go to groups screen to select an event
        binding.bottomNav.setSelectedItemId(R.id.nav_groups);
    }

    private void loadFragment(Fragment fragment) {
        currentFragment = fragment;
        getSupportFragmentManager()
                .beginTransaction()
                .setCustomAnimations(android.R.anim.fade_in, android.R.anim.fade_out)
                .replace(R.id.fragmentContainer, fragment)
                .commit();
    }

    public void navigateToGroupDetail(String eventId, String eventName) {
        Intent intent = new Intent(this, GroupDetailActivity.class);
        intent.putExtra("event_id", eventId);
        intent.putExtra("event_name", eventName);
        startActivity(intent);
        overridePendingTransition(android.R.anim.slide_in_left, android.R.anim.slide_out_right);
    }
}
