package com.google.firebase.udacity.friendlychat.messages;

import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.text.Editable;
import android.text.InputFilter;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ProgressBar;
import android.widget.Toast;

import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.database.ChildEventListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.remoteconfig.FirebaseRemoteConfig;
import com.google.firebase.remoteconfig.FirebaseRemoteConfigSettings;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;
import com.google.firebase.udacity.friendlychat.BuildConfig;
import com.google.firebase.udacity.friendlychat.MessageAdapter;
import com.google.firebase.udacity.friendlychat.R;
import com.google.firebase.udacity.friendlychat.common.base.FragmentBase;
import com.google.firebase.udacity.friendlychat.common.models.FriendlyMessage;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

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
    private FirebaseDatabase firebaseDatabase;
    private DatabaseReference databaseReference;
    private LinearLayoutManager linearLayoutManager;
    private final int RC_SIGN_IN = 1000;
    private final String FRIENDLY_MSG_LENGTH_KEY = "friendly_msg_length";
    private OnFragmentMessagesInteractionListener mListener;
    private static final String ARG_USERNAME = "username";
    private final int RC_PHOTO_PICKER = 2000;
    private FirebaseStorage firebaseStorage;
    private StorageReference storageReference;
    private FirebaseRemoteConfig firebaseRemoteConfig;

    public FragmentMessages() {
        // Required empty public constructor
    }

    public static FragmentMessages newInstance(String username) {
        FragmentMessages fragment = new FragmentMessages();
        Bundle args = new Bundle();
        args.putString(ARG_USERNAME, username);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null)
            username = getArguments().getString(ARG_USERNAME);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View rootView = inflater.inflate(R.layout.fragment_messages, container, false);
        friendlyMessages = new ArrayList<>();
        context = getActivity();
        databaseReference = FirebaseDatabase.getInstance().getReference().child("messages");
        presenterMessages = new PresenterMessages(context, this, databaseReference);
        firebaseStorage = FirebaseStorage.getInstance();
        firebaseRemoteConfig = FirebaseRemoteConfig.getInstance();
        storageReference = firebaseStorage.getReference().child("chat_photos");
        initializeViews(rootView);
        initRecyclerView();
        setListeners();
        initRemoteConfigSettings();
        return rootView;
    }

    private void initRemoteConfigSettings() {
        // Create Remote Config Setting to enable developer mode.
        // Fetching configs from the server is normally limited to 5 requests per hour.
        // Enabling developer mode allows many more requests to be made per hour, so developers
        // can test different config values during development.
        FirebaseRemoteConfigSettings configSettings = new FirebaseRemoteConfigSettings.Builder()
                .setDeveloperModeEnabled(BuildConfig.DEBUG)
                .build();
        firebaseRemoteConfig.setConfigSettings(configSettings);
        // Define default config values. Defaults are used when fetched config values are not
        // available. Eg: if an error occurred fetching values from the server.
        Map<String, Object> defaultConfigMap = new HashMap<>();
        defaultConfigMap.put(FRIENDLY_MSG_LENGTH_KEY, DEFAULT_MSG_LENGTH_LIMIT);
        firebaseRemoteConfig.setDefaults(defaultConfigMap);
        fetchConfig();
    }

    // Fetch the config to determine the allowed length of messages.
    private void fetchConfig() {
        long cacheExpiration = 3600; // 1 hour in seconds
        // If developer mode is enabled reduce cacheExpiration to 0 so that each fetch goes to the
        // server. This should not be used in release builds.
        if (firebaseRemoteConfig.getInfo().getConfigSettings().isDeveloperModeEnabled())
            cacheExpiration = 0;
        firebaseRemoteConfig.fetch(cacheExpiration).addOnSuccessListener(firebaseRemoteConfigOnSuccessListener)
                .addOnFailureListener(firebaseRemoteConfigOnFailureListener);
    }

    private OnSuccessListener<Void> firebaseRemoteConfigOnSuccessListener = new OnSuccessListener<Void>() {
        @Override
        public void onSuccess(Void aVoid) {
            // Make the fetched config available
            // via FirebaseRemoteConfig get<type> calls, e.g., getLong, getString.
            firebaseRemoteConfig.activateFetched();
            // Update the EditText length limit with
            // the newly retrieved values from Remote Config.
            applyRetrievedLengthLimit();
        }
    };
    private OnFailureListener firebaseRemoteConfigOnFailureListener = new OnFailureListener() {
        @Override
        public void onFailure(@NonNull Exception e) {
            // An error occurred when fetching the config.
            applyRetrievedLengthLimit();

        }
    };

    private void applyRetrievedLengthLimit() {
        Long friendly_msg_length = firebaseRemoteConfig.getLong(FRIENDLY_MSG_LENGTH_KEY);
        edtMessage.setFilters(new InputFilter[]{new InputFilter.LengthFilter(friendly_msg_length.intValue())});
    }

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
            openPhotoPicker();
        }
    };

    private void openPhotoPicker() {
        Intent intent = new Intent(Intent.ACTION_GET_CONTENT);
        intent.setType("image/jpeg");
        intent.putExtra(Intent.EXTRA_LOCAL_ONLY, true);
        startActivityForResult(Intent.createChooser(intent, "Complete action using"), RC_PHOTO_PICKER);
    }

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
        if (requestCode == RC_PHOTO_PICKER) {
            if (resultCode == RESULT_OK) {
                Uri selectedImageUri = data.getData();
                // Get a reference to store file at Chat_photos/<Filename>
                StorageReference photoRef = storageReference.child(selectedImageUri.getLastPathSegment());
                photoRef.putFile(selectedImageUri).addOnSuccessListener((ActivityMessages) context, uploadedImageOnSuccessListener);
            } else if (resultCode == RESULT_CANCELED) {
                // Sign in was canceled by the user, finish the activity
                Toast.makeText(getActivity(), "Sign in canceled", Toast.LENGTH_SHORT).show();
                getActivity().finish();
            }
        }
    }

    private OnSuccessListener<UploadTask.TaskSnapshot> uploadedImageOnSuccessListener = new OnSuccessListener<UploadTask.TaskSnapshot>() {
        @Override
        public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {
            // When the image has successfully uploaded, we get its download URL
            @SuppressWarnings("VisibleForTests") Uri downloadURL = taskSnapshot.getDownloadUrl();
            FriendlyMessage friendlyMessage = new FriendlyMessage(null, username, downloadURL.toString());
            presenterMessages.sendMessage(friendlyMessage);
        }
    };

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        if (context instanceof OnFragmentMessagesInteractionListener) {
            mListener = (OnFragmentMessagesInteractionListener) context;
        } else {
            throw new RuntimeException(context.toString()
                    + " must implement OnFragmentMessagesInteractionListener");
        }
    }

    @Override
    public void onDetach() {
        super.onDetach();
        mListener = null;
    }

    public interface OnFragmentMessagesInteractionListener {
        void onAuthenticationListener();
    }
}
