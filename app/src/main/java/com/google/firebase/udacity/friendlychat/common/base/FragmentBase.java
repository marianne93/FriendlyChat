package com.google.firebase.udacity.friendlychat.common.base;

import android.support.v4.app.Fragment;
import android.view.View;


public abstract class FragmentBase extends Fragment {

    protected abstract void initializeViews(View v);

    protected abstract void setListeners();
}
