package com.google.firebase.udacity.friendlychat.common.helpers;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.content.pm.Signature;
import android.util.Base64;
import android.util.Log;

import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

/**
 * Created by Marianne.Wazif on 11-Sep-17.
 */

public  class Utilities {
    public static void getHashKey(Context context) {
        try {
            @SuppressLint("PackageManagerGetSignatures") PackageInfo info = context.getPackageManager().getPackageInfo("com.google.firebase.udacity.friendlychat", PackageManager.GET_SIGNATURES);
            for (Signature signature : info.signatures) {
                MessageDigest md = MessageDigest.getInstance("SHA");
                md.update(signature.toByteArray());
                Log.d("KeyHash:", Base64.encodeToString(md.digest(), Base64.DEFAULT));
            }
        } catch (PackageManager.NameNotFoundException | NoSuchAlgorithmException e) {
            e.printStackTrace();
        }
    }
}
