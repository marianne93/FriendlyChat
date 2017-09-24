package com.google.firebase.udacity.friendlychat.messages;

import android.content.Context;
import android.content.Intent;
import android.support.annotation.NonNull;
import android.support.v4.app.FragmentTransaction;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.widget.Toast;

import com.firebase.ui.auth.AuthUI;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.udacity.friendlychat.R;
import com.google.firebase.udacity.friendlychat.common.base.ActivityBase;

import java.util.Arrays;

public class ActivityMessages extends ActivityBase implements FragmentMessages.OnFragmentMessagesInteractionListener {
    private static final int RC_SIGN_IN = 1000;
    private FirebaseAuth firebaseAuth;
    private String username;
    private static final String ANONYMOUS = "anonymous";

    public static void startActivity(Context context) {
        Intent i = new Intent(context, ActivityMessages.class);
        context.startActivity(i);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_messages);
        username = ANONYMOUS;
        firebaseAuth = FirebaseAuth.getInstance();
//        if (savedInstanceState == null) {
//            loadFragment();
//        }
        setListeners();

    }

    @Override
    protected void initializeViews() {

    }

    @Override
    protected void setListeners() {
        firebaseAuth.addAuthStateListener(authStateListener);
    }

    @Override
    protected void loadFragment() {
        FragmentTransaction fragmentTransaction = getSupportFragmentManager().beginTransaction();
        fragmentTransaction.add(R.id.frmContainer, FragmentMessages.newInstance(username)).commitAllowingStateLoss();

    }

    @Override
    public void onAuthenticationListener() {

    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.main_menu, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.sign_out_menu:
                AuthUI.getInstance().signOut(this);
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == RC_SIGN_IN) {
            if (resultCode == RESULT_OK) {
                Toast.makeText(this, "Signed in!", Toast.LENGTH_SHORT).show();
                loadFragment();
            } else if (resultCode == RESULT_CANCELED) {
                // Sign in was canceled by the user, finish the activity
                Toast.makeText(this, "Sign in canceled", Toast.LENGTH_SHORT).show();
                finish();
            }
        }
    }

    private FirebaseAuth.AuthStateListener authStateListener = new FirebaseAuth.AuthStateListener() {
        @Override
        public void onAuthStateChanged(@NonNull FirebaseAuth firebaseAuth) {
            FirebaseUser user = firebaseAuth.getCurrentUser();
            if (user != null) {
                username = user.getDisplayName();
                loadFragment();
            } else {
                startActivityForResult(AuthUI.getInstance()
                                .createSignInIntentBuilder()
                                .setIsSmartLockEnabled(false)
                                .setProviders(
                                        Arrays.asList(new AuthUI.IdpConfig.Builder(AuthUI.EMAIL_PROVIDER).build(),
                                                new AuthUI.IdpConfig.Builder(AuthUI.GOOGLE_PROVIDER).build())).build(),
                        RC_SIGN_IN);
            }
        }
    };
}
