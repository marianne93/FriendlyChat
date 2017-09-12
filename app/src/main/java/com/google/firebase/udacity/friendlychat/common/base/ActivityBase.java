package com.google.firebase.udacity.friendlychat.common.base;

import android.graphics.PorterDuff;
import android.graphics.drawable.Drawable;
import android.os.Build;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;

/**
 * Created by Marianne.Wazif on 05-Apr-17.
 */

public abstract class ActivityBase extends AppCompatActivity {

    protected abstract void initializeViews();

    protected abstract void setListeners();

    protected abstract void loadFragment();
}
