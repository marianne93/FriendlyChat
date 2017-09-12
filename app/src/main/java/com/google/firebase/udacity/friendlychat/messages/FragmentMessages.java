package com.google.firebase.udacity.friendlychat.messages;

import android.content.Context;
import android.os.Bundle;
import android.support.v4.app.Fragment;
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

import com.google.firebase.udacity.friendlychat.MessageAdapter;
import com.google.firebase.udacity.friendlychat.R;
import com.google.firebase.udacity.friendlychat.common.base.FragmentBase;
import com.google.firebase.udacity.friendlychat.common.models.FriendlyMessage;

import java.util.ArrayList;
import java.util.List;

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
        presenterMessages = new PresenterMessages(context, this);
        initializeViews(rootView);
        initRecyclerView();
        setListeners();
        return rootView;
    }

    private void initRecyclerView() {
        rvMessages.setLayoutManager(new LinearLayoutManager(context, LinearLayoutManager.VERTICAL, false));
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
    }

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
}
