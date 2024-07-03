//Main Activity
package com.example.userinterface;

import android.content.Context;
import android.content.DialogInterface;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.MenuItem;

import androidx.annotation.NonNull;
import androidx.appcompat.app.ActionBarDrawerToggle;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.drawerlayout.widget.DrawerLayout;
import androidx.fragment.app.Fragment;

import com.google.android.material.navigation.NavigationView;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

public class MainActivity extends AppCompatActivity {


    private DrawerLayout drawerLayout;
    private ActionBarDrawerToggle actionBarDrawerToggle;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);


        drawerLayout = findViewById(R.id.drawerLayout);

        String drawerOpen = getString(R.string.navigation_drawer_open);
        String drawerClose = getString(R.string.navigation_drawer_close);

        actionBarDrawerToggle = new ActionBarDrawerToggle(this, drawerLayout, R.string.navigation_drawer_open, R.string.navigation_drawer_close);

        drawerLayout.addDrawerListener(actionBarDrawerToggle);
        actionBarDrawerToggle.syncState();

        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setHomeButtonEnabled(true);

        // Check if the user is already signed in
        FirebaseUser currentUser = FirebaseAuth.getInstance().getCurrentUser();
        if (currentUser != null) {
            // User is signed in, navigate to WelcomeFragment
            loadWelcomeFragment(currentUser.getDisplayName());
        } else {
            // User is not signed in, load RegistrationFragment
            loadRegistrationFragment();
        }

        NavigationView navigationView = findViewById(R.id.navigationView);
        navigationView.setNavigationItemSelectedListener(new NavigationView.OnNavigationItemSelectedListener() {
            @Override
            public boolean onNavigationItemSelected(@NonNull MenuItem item) {
                int id = item.getItemId();
                Fragment selectedFragment = null;

                if (id == R.id.nav_home) {
                    selectedFragment = new WelcomeFragment();
                } else if (id == R.id.nav_registration) {
                    selectedFragment = new RegistrationFragment();
                } else if (id == R.id.nav_contacts) {
                    selectedFragment = new ContactsFragment();
                } else if (id == R.id.nav_rating) {
                    // Handle About Us! item
                }else if (id == R.id.sign_out) {
                    // Ask for confirmation before signing out
                    showSignOutConfirmationDialog();
                }

                if (selectedFragment != null) {
                    getSupportFragmentManager().beginTransaction()
                            .replace(R.id.frameLayoutContent, selectedFragment)
                            .commit();
                    drawerLayout.closeDrawers();
                }

                return true;
            }
        });
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (actionBarDrawerToggle.onOptionsItemSelected(item)) {
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    private void loadRegistrationFragment() {
        getSupportFragmentManager().beginTransaction()
                .replace(R.id.frameLayoutContent, new RegistrationFragment())
                .commit();
    }

    private void loadSignInFragment() {
        getSupportFragmentManager().beginTransaction()
                .replace(R.id.frameLayoutContent, new SignInFragment())
                .commit();
    }

    private void loadWelcomeFragment(String currentUser) {
        getSupportFragmentManager().beginTransaction()
                .replace(R.id.frameLayoutContent, new WelcomeFragment())
                .commit();
    }

    private void showSignOutConfirmationDialog() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Sign Out");
        builder.setMessage("Are you sure you want to sign out?");
        builder.setPositiveButton("Yes", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                // Sign out the user
                FirebaseAuth.getInstance().signOut();
                // Navigate to the sign-in screen or any other appropriate screen
                // For example:
                loadSignInFragment();
            }
        });
        builder.setNegativeButton("No", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                // Dismiss the dialog
                dialog.dismiss();
            }
        });
        builder.show();
    }


}
