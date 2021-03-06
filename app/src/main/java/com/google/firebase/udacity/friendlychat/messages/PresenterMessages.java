package com.google.firebase.udacity.friendlychat.messages;

import android.content.Context;

import com.google.firebase.database.ChildEventListener;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.udacity.friendlychat.common.base.PresenterBase;
import com.google.firebase.udacity.friendlychat.common.models.FriendlyMessage;

/**
 * Created by Marianne.Wazif on 12-Sep-17.
 */

public class PresenterMessages extends PresenterBase {
    private ViewMessages viewMessages;
    private Context context;
    private FirebaseDatabase firebaseDatabase;
    private DatabaseReference databaseReference;

    public PresenterMessages(Context context, ViewMessages viewMessages , DatabaseReference databaseReference) {
        this.viewMessages = viewMessages;
        this.context = context;
        this.databaseReference = databaseReference;
    }

    public void sendMessage(FriendlyMessage friendlyMessage) {
       this.databaseReference.push().setValue(friendlyMessage);
    }
}
