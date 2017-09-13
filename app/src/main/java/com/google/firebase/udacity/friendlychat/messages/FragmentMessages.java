package com.google.firebase.udacity.friendlychat.messages;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.app.Fragment;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.text.Editable;
import android.text.InputFilter;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ProgressBar;
import android.widget.Toast;

import com.firebase.ui.auth.AuthUI;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.ChildEventListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Transaction;
import com.google.firebase.udacity.friendlychat.MessageAdapter;
import com.google.firebase.udacity.friendlychat.R;
import com.google.firebase.udacity.friendlychat.common.base.FragmentBase;
import com.google.firebase.udacity.friendlychat.common.models.FriendlyMessage;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import static android.app.Activity.RESULT_CANCELED;
import static android.app.Activity.RESULT_OK;

public class FragmentMessages extends FragmentBase implements ViewMessages {
    private RecyclerView rvMessages;
    private ImageButton btnPhotoPicker;
    private EditText edtMessage;
    private Button btnSend;
    private ProgressBar progressBar;
    private List<FriendlyMessage> friendlyMessages;
    private MessageAdapter messageAdapter;
    private Context context;
    private static final int DEFAULT_MSG_LENGTH_LIMIT = 1000;
    private PresenterMessages presenterMessages;
    private String username;
    private static final String ANONYMOUS = "anonymous";
    private FirebaseDatabase firebaseDatabase;
    private DatabaseReference databaseReference;
    private LinearLayoutManager linearLayoutManager;
    private FirebaseAuth firebaseAuth;
    private static final int RC_SIGN_IN = 1000;

    public FragmentMessages() {
        // Required empty public constructor
    }

    public static FragmentMessages newInstance() {
        return new FragmentMessages();
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View rootView = inflater.inflate(R.layout.fragment_messages, container, false);
        friendlyMessages = new ArrayList<>();
        username = ANONYMOUS;
        context = getActivity();
        databaseReference = FirebaseDatabase.getInstance().getReference().child("messages");
        presenterMessages = new PresenterMessages(context, this, databaseReference);
        firebaseAuth = FirebaseAuth.getInstance();
        initializeViews(rootView);
        initRecyclerView();
        setListeners();
        setHasOptionsMenu(true);
        return rootView;
    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        inflater.inflate(R.menu.main_menu, menu);
        super.onCreateOptionsMenu(menu, inflater);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.sign_out_menu: {
                AuthUI.getInstance().signOut(getActivity());
                return true;
            }
            default:
                return super.onOptionsItemSelected(item);
        }
    }

    private FirebaseAuth.AuthStateListener authStateListener = new FirebaseAuth.AuthStateListener() {
        @Override
        public void onAuthStateChanged(@NonNull FirebaseAuth firebaseAuth) {
            FirebaseUser user = firebaseAuth.getCurrentUser();
            if (user != null) {
                username = user.getDisplayName();
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

    @Override
    public void onResume() {
        super.onResume();
    }


    private void initRecyclerView() {
        linearLayoutManager = new LinearLayoutManager(context, LinearLayoutManager.VERTICAL, false);
        linearLayoutManager.setStackFromEnd(true);
        rvMessages.setLayoutManager(linearLayoutManager);
        messageAdapter = new MessageAdapter(context, friendlyMessages);
        rvMessages.setAdapter(messageAdapter);
    }

    @Override
    protected void initializeViews(View v) {
        rvMessages = (RecyclerView) v.findViewById(R.id.rvMessages);
        btnPhotoPicker = (ImageButton) v.findViewById(R.id.btnPhotoPicker);
        edtMessage = (EditText) v.findViewById(R.id.edtMessage);
        edtMessage.setFilters(new InputFilter[]{new InputFilter.LengthFilter(DEFAULT_MSG_LENGTH_LIMIT)});
        btnSend = (Button) v.findViewById(R.id.btnSend);
        progressBar = (ProgressBar) v.findViewById(R.id.progressBar);
    }

    @Override
    protected void setListeners() {
        btnPhotoPicker.setOnClickListener(btnPhotoPickerOnClickListener);
        edtMessage.addTextChangedListener(edtMessageTextWatcher);
        btnSend.setOnClickListener(btnSendOnClickListener);
        messageAdapter.registerAdapterDataObserver(adapterDataObserver);
        firebaseAuth.addAuthStateListener(authStateListener);
        databaseReference.addChildEventListener(childEventListener);
    }

    private RecyclerView.AdapterDataObserver adapterDataObserver = new RecyclerView.AdapterDataObserver() {
        @Override
        public void onItemRangeInserted(int positionStart, int itemCount) {
            super.onItemRangeInserted(positionStart, itemCount);
            int friendlyMessageCount = messageAdapter.getItemCount();
            int lastVisiblePosition =
                    linearLayoutManager.findLastCompletelyVisibleItemPosition();
            // If the recycler view is initially being loaded or the
            // user is at the bottom of the list, scroll to the bottom
            // of the list to show the newly added message.
            if (lastVisiblePosition == -1 ||
                    (positionStart >= (friendlyMessageCount - 1) &&
                            lastVisiblePosition == (positionStart - 1))) {
                rvMessages.scrollToPosition(positionStart);
            }
        }
    };
    private ChildEventListener childEventListener = new ChildEventListener() {
        @Override
        public void onChildAdded(DataSnapshot dataSnapshot, String s) {
            friendlyMessages.add(dataSnapshot.getValue(FriendlyMessage.class));
            messageAdapter.notifyItemInserted(friendlyMessages.size() - 1);
        }

        @Override
        public void onChildChanged(DataSnapshot dataSnapshot, String s) {

        }

        @Override
        public void onChildRemoved(DataSnapshot dataSnapshot) {

        }

        @Override
        public void onChildMoved(DataSnapshot dataSnapshot, String s) {

        }

        @Override
        public void onCancelled(DatabaseError databaseError) {

        }
    };
    private View.OnClickListener btnSendOnClickListener = new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            FriendlyMessage friendlyMessage = new FriendlyMessage(edtMessage.getText().toString(), username, null);
            presenterMessages.sendMessage(friendlyMessage);
            edtMessage.setText("");
        }
    };
    private TextWatcher edtMessageTextWatcher = new TextWatcher() {
        @Override
        public void beforeTextChanged(CharSequence s, int start, int count, int after) {

        }

        @Override
        public void onTextChanged(CharSequence charSequence, int start, int before, int count) {
            if (charSequence.toString().trim().length() > 0) {
                btnSend.setEnabled(true);
            } else {
                btnSend.setEnabled(false);
            }
        }

        @Override
        public void afterTextChanged(Editable s) {

        }
    };
    private View.OnClickListener btnPhotoPickerOnClickListener = new View.OnClickListener() {
        @Override
        public void onClick(View v) {

        }
    };


    @Override
    public void showProgress(boolean show) {
        if (show)
            progressBar.setVisibility(View.VISIBLE);
        else
            progressBar.setVisibility(View.GONE);

    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == RC_SIGN_IN) {
            if (resultCode == RESULT_OK) {
                Toast.makeText(getActivity(), "Signed in!", Toast.LENGTH_SHORT).show();
                getActivity().finish();
                startActivity(getActivity().getIntent());
            } else if (resultCode == RESULT_CANCELED) {
                // Sign in was canceled by the user, finish the activity
                Toast.makeText(getActivity(), "Sign in canceled", Toast.LENGTH_SHORT).show();
                getActivity().finish();
            }
        }
    }
}
