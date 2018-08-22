package com.thecoffeecoders.chatex;


import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.text.Editable;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.GridLayout;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.ChildEventListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.firebase.database.ServerValue;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;
import com.mobapphome.mahencryptorlib.MAHEncryptor;
import com.thecoffeecoders.chatex.GetTimeAgo;
import com.thecoffeecoders.chatex.MessageAdapter;
import com.thecoffeecoders.chatex.Messages;
import com.thecoffeecoders.chatex.R;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.net.URI;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import id.zelory.compressor.Compressor;
import io.github.kexanie.library.MathView;

public class ChatActivity extends AppCompatActivity {
    private boolean isMathButtonsVisible;
    private String mChatUser;
    private Toolbar mChatToolbar;

    private DatabaseReference mRootRef;

    private TextView mTitleView;
    private TextView mLastSeenView;
    //private CircleImageView mProfileImage;
    private FirebaseAuth mAuth;
    private String mCurrentUserId;

    private GridLayout mHelpButtonLayout;
    private ImageButton mChatAddBtn;
    private ImageButton mChatSendBtn;
    private ImageButton mMathToggleBtn;
    private EditText mChatMessageView;

    private MathView previewPane;

    private RecyclerView mMessagesList;
    //private SwipeRefreshLayout mRefreshLayout;

    private final List<Messages> messagesList = new ArrayList<>();
    private LinearLayoutManager mLinearLayout;
    private MessageAdapter mAdapter;

    private static final int TOTAL_ITEMS_TO_LOAD = 10;
    private int mCurrentPage = 1;

    private static final int GALLERY_PICK = 1;


    //LATEX EQUATION TEXTS
    static final String integralLatexText = "\\int_{ }^{ } ";
    static final String squareRootLatexText = "\\sqrt[ ]{ }";
    static final String superScriptLatexText = "^";
    static final String subScriptLatexText = "_";
    static final String parenthesesLatexText = "\\left(  \\right)";
    static final String curlyBracketsLatexText = "{ }";
    static final String squareBracketsLatexText = "\\left[  \\right]";

    static final String absoluteLatexText = "\\left |  \\right |";
    static final String complementBarLatexText = "\\bar{ } ";
    static final String fractionLatexText = "\\frac{ }{ } ";
    static final String greaterThanOrEqualToLatexText = "\\geq ";
    static final String infinityLatexText = "\\infty ";
    static final String intersectionLatexText = "\\bigcap ";
    static final String unionLatexText = "\\bigcup ";
    static final String lessThanOrEqualToLatexText = "\\leq ";
    static final String limitLatexText = "\\lim_{{x} \\rightarrow {a}} ";
    static final String notEqualLatexText = "\\neq ";
    static final String partialDerivativesLatexText = "\\frac{\\partial^{ } }{\\partial { }^{ }} ";
    static final String plusOrMinusLatexText = "\\pm ";
    static final String doubleRightArrowLatexText = "\\Rightarrow ";
    static final String singleRightArrowLatexText = "\\rightarrow ";
    static final String summationLatexText = "\\sum_{x = a}^{b} ";
    static final String downTriangleLatexText = "\\bigtriangledown ";
    static final String upTriangleLatexText = "\\bigtriangleup ";
    static final String vectorLatexText = "\\vec{ } ";
    static final String backSlashLatexText = " \\";


    //LATEX TYPING BUTTONS
    private Button latexIntegralButtton;
    private Button latexSquareRootButton;
    private Button latexSuperScriptButton;
    private Button latexSubScriptButton;
    private Button latexParenthesesButton;
    private Button latexCurlyBracketsButton;
    private Button latexSquareBracketsButton;

    private Button latexAbsoluteButton;
    private Button latexComplementButton;
    private Button latexFractionButton;
    private Button latexGreaterThanOrEqualToButton;
    private Button latexInfinityButton;
    private Button latexIntersectionButton;
    private Button latexUnionButton;
    private Button latexLessThanOrEqualToButton;
    private Button latexLimitButton;
    private Button latexNotEqualToButton;
    private Button latexPartialDerivativeButton;
    private Button latexPlusOrMinusButton;
    private Button latexDoubleRightArrowButton;
    private Button latexSingleRightArrowButton;
    private Button latexSummationButton;
    private Button latexDownTriangleButton;
    private Button latexUpTriangleButton;
    private Button latexVectorButton;
    private Button latexBackSlashButton;
    private Button latexClearButton;

    private ImageButton moveCursorLeftButton;
    private ImageButton moveCursorRightButton;
    private LinearLayout previewPaneLinearLayout;


    // Storage Firebase
    private StorageReference mImageStorage;

    public boolean pressedUp = false;

    //New Solution
    private int itemPos = 0;

    private String mLastKey = "";
    private String mPrevKey = "";


    @Override
    protected void onCreate(Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_chat);

        isMathButtonsVisible = false;

        mChatToolbar = (Toolbar) findViewById(R.id.chat_app_bar);
        setSupportActionBar(mChatToolbar);

        ActionBar actionBar = getSupportActionBar();
        //
        actionBar.setTitle("");
        //
        actionBar.setDisplayHomeAsUpEnabled(true);
        actionBar.setDisplayShowCustomEnabled(true);

        mRootRef = FirebaseDatabase.getInstance().getReference();
        mAuth = FirebaseAuth.getInstance();
        mCurrentUserId = mAuth.getCurrentUser().getUid();

        mChatUser = getIntent().getStringExtra("user_id");
        String userName = getIntent().getStringExtra("user_name");

        LayoutInflater inflater = (LayoutInflater) this.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        View action_bar_view = inflater.inflate(R.layout.chat_custom_bar, null);

        actionBar.setCustomView(action_bar_view);

        // ---- Custom Action bar Items ----

        mTitleView = (TextView) findViewById(R.id.custom_bar_title);
        mLastSeenView = (TextView) findViewById(R.id.custom_bar_seen);
        //mProfileImage = (CircleImageView) findViewById(R.id.custom_bar_image);

        mHelpButtonLayout = (GridLayout) findViewById(R.id.help_buttons_grid);
        mChatAddBtn = (ImageButton) findViewById(R.id.chat_add_btn);
        mChatSendBtn = (ImageButton) findViewById(R.id.chat_send_btn);
        mMathToggleBtn = (ImageButton) findViewById(R.id.chat_math_btn);
        mChatMessageView = (EditText) findViewById(R.id.chat_message_view);
        previewPane = (MathView) findViewById(R.id.math_preview);

        //
        mRootRef.child("Users").child(mCurrentUserId).child("online").setValue("true");
        //



        //
        mRootRef.child("Friends").child(mCurrentUserId).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                if(!dataSnapshot.hasChild(mChatUser)){
                    mChatAddBtn.setVisibility(View.GONE);
                    mChatSendBtn.setVisibility(View.GONE);
                    mMathToggleBtn.setVisibility(View.GONE);
                    mChatMessageView.setVisibility(View.GONE);
                }
            }
            @Override
            public void onCancelled(DatabaseError databaseError) {}
        });
        //
        moveCursorLeftButton = (ImageButton) findViewById(R.id.move_cursor_left_button);
        moveCursorRightButton = (ImageButton) findViewById(R.id.move_cursor_right_button);
        previewPaneLinearLayout = (LinearLayout) findViewById(R.id.preview_pane_linear_layout);

        //LATEX EDITING BUTTONS INSTANTIATION
        latexIntegralButtton = (Button) findViewById(R.id.latex_integral);
        latexSquareRootButton = (Button) findViewById(R.id.latex_squareRoot);
        latexSuperScriptButton = (Button) findViewById(R.id.latex_power);
        latexSubScriptButton = (Button) findViewById(R.id.latex_underscore);
        latexParenthesesButton = (Button) findViewById(R.id.latex_parentheses);
        latexCurlyBracketsButton = (Button) findViewById(R.id.latex_curly_braces);
        latexSquareBracketsButton = (Button) findViewById(R.id.latex_square_brackets);

        latexAbsoluteButton = (Button) findViewById(R.id.latex_absolute);
        latexFractionButton = (Button) findViewById(R.id.latex_fraction);
        latexComplementButton = (Button) findViewById(R.id.latex_complement);
        latexGreaterThanOrEqualToButton = (Button) findViewById(R.id.latex_greaterThanOrEqualsTo);
        latexInfinityButton = (Button) findViewById(R.id.latex_infinity);
        latexIntersectionButton = (Button) findViewById(R.id.latex_intersection);
        latexUnionButton = (Button) findViewById(R.id.latex_union);
        latexLessThanOrEqualToButton = (Button) findViewById(R.id.latex_lessThanOrEqualsTo);
        latexLimitButton = (Button) findViewById(R.id.latex_limit);
        latexNotEqualToButton = (Button) findViewById(R.id.latex_not_equal);
        latexPartialDerivativeButton = (Button) findViewById(R.id.latex_partialDerivatives);
        latexPlusOrMinusButton = (Button) findViewById(R.id.latex_plusOrMinus);
        latexDoubleRightArrowButton = (Button) findViewById(R.id.latex_doubleArrow);
        latexSingleRightArrowButton = (Button) findViewById(R.id.latex_singleArrow);
        latexSummationButton = (Button) findViewById(R.id.latex_summation);
        latexDownTriangleButton = (Button) findViewById(R.id.latex_triangleDown);
        latexUpTriangleButton = (Button) findViewById(R.id.latex_triangleUp);
        latexVectorButton = (Button) findViewById(R.id.latex_vector);
        latexBackSlashButton = (Button) findViewById(R.id.latex_back_slash);
        latexClearButton = (Button) findViewById(R.id.latex_clear);


        /*moveCursorLeftButton.setVisibility(View.GONE);
        moveCursorRightButton.setVisibility(View.GONE);
        previewPane.setVisibility(View.GONE);*/
        previewPaneLinearLayout.setVisibility(View.GONE);
        mHelpButtonLayout.setVisibility(View.GONE);
        //mMathToggleBtn.setVisibility(View.GONE);



        mAdapter = new MessageAdapter(this, messagesList);

        mMessagesList = (RecyclerView) findViewById(R.id.messages_list);
        //mRefreshLayout = (SwipeRefreshLayout) findViewById(R.id.message_swipe_layout);
        mLinearLayout = new LinearLayoutManager(this);

        mMessagesList.setHasFixedSize(true);
        mMessagesList.setLayoutManager(mLinearLayout);

        mMessagesList.setAdapter(mAdapter);

        //------- IMAGE STORAGE ---------
        mImageStorage = FirebaseStorage.getInstance().getReference();

        mRootRef.child("Chat").child(mCurrentUserId).child(mChatUser).child("seen").setValue(true);




        mTitleView.setText(userName);

        mRootRef.child("Users").child(mChatUser).addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {

                String online = dataSnapshot.child("online").getValue().toString();
                String image = dataSnapshot.child("image").getValue().toString();

                if(online.equals("true")) {

                    mLastSeenView.setText("Online");

                } else {

                    GetTimeAgo getTimeAgo = new GetTimeAgo();

                    long lastTime = Long.parseLong(online);

                    String lastSeenTime = getTimeAgo.getTimeAgo(lastTime, getApplicationContext());

                    mLastSeenView.setText(lastSeenTime);

                }

            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });


        mRootRef.child("Chat").child(mCurrentUserId).addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {

                if(!dataSnapshot.hasChild(mChatUser)){

                    Map chatAddMap = new HashMap();
                    chatAddMap.put("seen", false);
                    chatAddMap.put("timestamp", ServerValue.TIMESTAMP);

                    Map chatUserMap = new HashMap();
                    chatUserMap.put("Chat/" + mCurrentUserId + "/" + mChatUser, chatAddMap);
                    chatUserMap.put("Chat/" + mChatUser + "/" + mCurrentUserId, chatAddMap);

                    mRootRef.updateChildren(chatUserMap, new DatabaseReference.CompletionListener() {
                        @Override
                        public void onComplete(DatabaseError databaseError, DatabaseReference databaseReference) {

                            if(databaseError != null){

                                Log.d("CHAT_LOG", databaseError.getMessage().toString());

                            }

                        }
                    });

                }

            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });



        mChatSendBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                sendMessage();

            }
        });

       /* mRefreshLayout.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {

                mCurrentPage++;

                itemPos = 0;

                loadMoreMessages();


            }
        });
        */



        mChatAddBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                Intent galleryIntent = new Intent();
                galleryIntent.setType("image/*");
                galleryIntent.setAction(Intent.ACTION_GET_CONTENT);

                startActivityForResult(Intent.createChooser(galleryIntent, "SELECT IMAGE"), GALLERY_PICK);

            }
        });



        mMathToggleBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //
                previewPane.setText(mChatMessageView.getText().toString());
                //
                if(isMathButtonsVisible){
                    mHelpButtonLayout.setVisibility(View.GONE);
                    mChatAddBtn.setVisibility(View.VISIBLE);
                    /*previewPane.setVisibility(View.GONE);
                    moveCursorLeftButton.setVisibility(View.GONE);
                    moveCursorRightButton.setVisibility(View.GONE);*/
                    previewPaneLinearLayout.setVisibility(View.GONE);
                    isMathButtonsVisible = false;
                } else{
                    if(mChatMessageView.getText().toString().equals("")){
                        mChatMessageView.setText("$$  $$");
                        mChatMessageView.setSelection(3);
                    }
                    mHelpButtonLayout.setVisibility(View.VISIBLE);
                    mChatAddBtn.setVisibility(View.GONE);
                   /* moveCursorLeftButton.setVisibility(View.VISIBLE);
                    moveCursorRightButton.setVisibility(View.VISIBLE);
                    previewPane.setVisibility(View.VISIBLE);*/
                   previewPaneLinearLayout.setVisibility(View.VISIBLE);
                    isMathButtonsVisible = true;
                }
            }
        });

        mMathToggleBtn.setOnLongClickListener(new View.OnLongClickListener() {
            @Override
            public boolean onLongClick(View v) {
                previewPane.setText(mChatMessageView.getText().toString());
                //
                if(isMathButtonsVisible){
                    mHelpButtonLayout.setVisibility(View.GONE);
                    mChatAddBtn.setVisibility(View.VISIBLE);
                    /*previewPane.setVisibility(View.GONE);
                    moveCursorLeftButton.setVisibility(View.GONE);
                    moveCursorRightButton.setVisibility(View.GONE);*/
                    previewPaneLinearLayout.setVisibility(View.GONE);
                    isMathButtonsVisible = false;
                } else{
                    if(mChatMessageView.getText().toString().equals("")){
                        mChatMessageView.setText("\\(   \\)");
                        mChatMessageView.setSelection(5);
                    }
                    mHelpButtonLayout.setVisibility(View.VISIBLE);
                    mChatAddBtn.setVisibility(View.GONE);
                    /*previewPane.setVisibility(View.VISIBLE);
                    moveCursorLeftButton.setVisibility(View.VISIBLE);
                    moveCursorRightButton.setVisibility(View.VISIBLE);*/
                    previewPaneLinearLayout.setVisibility(View.VISIBLE);
                    isMathButtonsVisible = true;
                }
                return true;
            }
        });

       /* previewPane.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                previewPane.setText(mChatMessageView.getText().toString());
            }
        });*/

        mChatMessageView.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                previewPane.setText(mChatMessageView.getText().toString());
            }

            @Override
            public void afterTextChanged(Editable s) {

            }
        });


        moveCursorLeftButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(mChatMessageView.getSelectionStart()>0) {
                    mChatMessageView.setSelection(mChatMessageView.getSelectionStart() - 1);
                }
            }
        });

        //
       /* moveCursorLeftButton.setOnTouchListener(new View.OnTouchListener() {

            @Override
            public boolean onTouch(View v, MotionEvent event) {
                switch (event.getAction()) {

                    case MotionEvent.ACTION_DOWN:


                        if(pressedUp == false){
                            pressedUp = true;
                            if(mChatMessageView.getSelectionStart()>0) {
                                mChatMessageView.setSelection(mChatMessageView.getSelectionStart() - 1);
                            }
                        }
                        break;

                    case MotionEvent.ACTION_UP:

                        pressedUp = false;

                }
                return true;
            }
        });*/
        //

       /* moveCursorLeftButton.setOnLongClickListener(new View.OnLongClickListener() {
            @Override
            public boolean onLongClick(View v) {
                if(mChatMessageView.getSelectionStart()>0) {
                    mChatMessageView.setSelection(mChatMessageView.getSelectionStart() - 1);
                }
                return true;
            }
        });*/

        moveCursorRightButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(mChatMessageView.getSelectionStart() < mChatMessageView.getText().toString().length()) {
                    mChatMessageView.setSelection(mChatMessageView.getSelectionStart() + 1);
                }
            }
        });
       /* moveCursorRightButton.setOnLongClickListener(new View.OnLongClickListener() {
            @Override
            public boolean onLongClick(View v) {
                if(mChatMessageView.getSelectionStart() < mChatMessageView.getText().toString().length()) {
                    mChatMessageView.setSelection(mChatMessageView.getSelectionStart() + 1);
                }
                return true;
            }
        });*/
        //ADDING LATEX TEXT VIA BUTTONS
        /*private Button latexIntegralButtton;
        private Button latexSquareRootButton;
        private Button latexSuperScriptButton;
        private Button latexSubScriptButton;
        private Button latexParenthesesButton;
        private Button latexCurlyBracketsButton;
        private Button latexSquareBracketsButton;*/

        latexIntegralButtton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                int i= mChatMessageView.getSelectionStart();
                mChatMessageView.getText().insert(i, integralLatexText);
                mChatMessageView.setSelection(i+13);
            }
        });

        latexSquareRootButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                int i= mChatMessageView.getSelectionStart();
                mChatMessageView.getText().insert(i, squareRootLatexText);
                mChatMessageView.setSelection(i+9);
            }
        });
        latexSuperScriptButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mChatMessageView.getText().insert(mChatMessageView.getSelectionStart(), superScriptLatexText);
            }
        });
        latexSubScriptButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mChatMessageView.getText().insert(mChatMessageView.getSelectionStart(), subScriptLatexText);
            }
        });
        latexParenthesesButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                int i= mChatMessageView.getSelectionStart();
                mChatMessageView.getText().insert(i, parenthesesLatexText);
                mChatMessageView.setSelection(i+7);
            }
        });

        latexCurlyBracketsButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                int i= mChatMessageView.getSelectionStart();
                mChatMessageView.getText().insert(i, curlyBracketsLatexText);
                mChatMessageView.setSelection(i+2);
            }
        });
        latexSquareBracketsButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                int i= mChatMessageView.getSelectionStart();
                mChatMessageView.getText().insert(i, squareBracketsLatexText);
                mChatMessageView.setSelection(i+7);
            }
        });


        latexAbsoluteButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                int i= mChatMessageView.getSelectionStart();
                mChatMessageView.getText().insert(i, absoluteLatexText);
                mChatMessageView.setSelection(i+8);
            }
        });
        latexComplementButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                int i= mChatMessageView.getSelectionStart();
                mChatMessageView.getText().insert(i, complementBarLatexText);
                mChatMessageView.setSelection(i+5);
            }
        });
        latexFractionButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                int i= mChatMessageView.getSelectionStart();
                mChatMessageView.getText().insert(i, fractionLatexText);
                mChatMessageView.setSelection(i+6);
            }
        });
        latexGreaterThanOrEqualToButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mChatMessageView.getText().insert(mChatMessageView.getSelectionStart(), greaterThanOrEqualToLatexText);
            }
        });
        latexInfinityButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mChatMessageView.getText().insert(mChatMessageView.getSelectionStart(), infinityLatexText);
            }
        });
        latexIntersectionButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mChatMessageView.getText().insert(mChatMessageView.getSelectionStart(), intersectionLatexText);
            }
        });
        latexUnionButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mChatMessageView.getText().insert(mChatMessageView.getSelectionStart(), unionLatexText);
            }
        });
        latexLessThanOrEqualToButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mChatMessageView.getText().insert(mChatMessageView.getSelectionStart(), lessThanOrEqualToLatexText);
            }
        });
        latexLimitButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                int i= mChatMessageView.getSelectionStart();
                mChatMessageView.getText().insert(i, limitLatexText);
                mChatMessageView.setSelection(i+27);
            }
        });
        latexNotEqualToButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mChatMessageView.getText().insert(mChatMessageView.getSelectionStart(), notEqualLatexText);
            }
        });
        latexPartialDerivativeButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                int i= mChatMessageView.getSelectionStart();
                mChatMessageView.getText().insert(i, partialDerivativesLatexText);
                mChatMessageView.setSelection(i+31);
            }
        });
        latexPlusOrMinusButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mChatMessageView.getText().insert(mChatMessageView.getSelectionStart(), plusOrMinusLatexText);
            }
        });
        latexDoubleRightArrowButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mChatMessageView.getText().insert(mChatMessageView.getSelectionStart(), doubleRightArrowLatexText);
            }
        });
        latexSingleRightArrowButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mChatMessageView.getText().insert(mChatMessageView.getSelectionStart(), singleRightArrowLatexText);
            }
        });
        latexSummationButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                int i= mChatMessageView.getSelectionStart();
                mChatMessageView.getText().insert(i, summationLatexText);
                mChatMessageView.setSelection(i+17);
            }
        });
        latexDownTriangleButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mChatMessageView.getText().insert(mChatMessageView.getSelectionStart(), downTriangleLatexText);
            }
        });
        latexUpTriangleButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mChatMessageView.getText().insert(mChatMessageView.getSelectionStart(), upTriangleLatexText);
            }
        });
        latexVectorButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                int i= mChatMessageView.getSelectionStart();
                mChatMessageView.getText().insert(i, vectorLatexText);
                mChatMessageView.setSelection(i+5);
            }
        });
        latexClearButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mChatMessageView.getText().clear();
                mChatMessageView.setText("\\(   \\)");
                mChatMessageView.setSelection(5);
            }
        });
        latexBackSlashButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mChatMessageView.getText().insert(mChatMessageView.getSelectionStart(), backSlashLatexText);
            }
        });

        loadMessages();
    }

    @Override
    protected void onStop() {
        super.onStop();

        mRootRef.child("Users").child(mCurrentUserId).child("online").setValue(ServerValue.TIMESTAMP);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if(requestCode == GALLERY_PICK && resultCode == RESULT_OK){

            Uri imageUri = data.getData();

            final String current_user_ref = "messages/" + mCurrentUserId + "/" + mChatUser;
            final String chat_user_ref = "messages/" + mChatUser + "/" + mCurrentUserId;

            DatabaseReference user_message_push = mRootRef.child("messages")
                    .child(mCurrentUserId).child(mChatUser).push();

            final String push_id = user_message_push.getKey();

            StorageReference filepath = mImageStorage.child("message_images").child( push_id + ".jpg");

            filepath.putFile(imageUri).addOnCompleteListener(new OnCompleteListener<UploadTask.TaskSnapshot>() {
                @Override
                public void onComplete(@NonNull Task<UploadTask.TaskSnapshot> task) {

                    if(task.isSuccessful()){

                        @SuppressWarnings("VisibleForTests")
                        String download_url = task.getResult().getDownloadUrl().toString();

                        Map messageMap = new HashMap();
                        messageMap.put("message", download_url);
                        messageMap.put("seen", false);
                        messageMap.put("type", "image");
                        messageMap.put("time", ServerValue.TIMESTAMP);
                        messageMap.put("from", mCurrentUserId);

                        Map messageUserMap = new HashMap();
                        messageUserMap.put(current_user_ref + "/" + push_id, messageMap);
                        messageUserMap.put(chat_user_ref + "/" + push_id, messageMap);

                        mChatMessageView.setText("");

                        mRootRef.updateChildren(messageUserMap, new DatabaseReference.CompletionListener() {
                            @Override
                            public void onComplete(DatabaseError databaseError, DatabaseReference databaseReference) {

                                if(databaseError != null){

                                    Log.d("CHAT_LOG", databaseError.getMessage().toString());

                                }

                            }
                        });
                        mAdapter.notifyDataSetChanged();


                    }

                }
            });

        }

    }

    /*private void loadMoreMessages() {

        DatabaseReference messageRef = mRootRef.child("messages").child(mCurrentUserId).child(mChatUser);

        Query messageQuery = messageRef.orderByKey().endAt(mLastKey).limitToLast(10);

        messageQuery.addChildEventListener(new ChildEventListener() {
            @Override
            public void onChildAdded(DataSnapshot dataSnapshot, String s) {


                Messages message = dataSnapshot.getValue(Messages.class);
                String messageKey = dataSnapshot.getKey();

                if(!mPrevKey.equals(messageKey)){

                    messagesList.add(itemPos++, message);

                } else {

                    mPrevKey = mLastKey;

                }


                if(itemPos == 1) {

                    mLastKey = messageKey;

                }


                Log.d("TOTALKEYS", "Last Key : " + mLastKey + " | Prev Key : " + mPrevKey + " | Message Key : " + messageKey);

                mAdapter.notifyDataSetChanged();

                mRefreshLayout.setRefreshing(false);

                mLinearLayout.scrollToPositionWithOffset(10, 0);

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
        });

    }*/

    private void loadMessages() {

        DatabaseReference messageRef = mRootRef.child("messages").child(mCurrentUserId).child(mChatUser);

        Query messageQuery = messageRef.orderByKey();//limitToLast(mCurrentPage * TOTAL_ITEMS_TO_LOAD);


        messageQuery.addChildEventListener(new ChildEventListener() {
            @Override
            public void onChildAdded(DataSnapshot dataSnapshot, String s) {

                Messages message = dataSnapshot.getValue(Messages.class);

                /*itemPos++;

                if(itemPos == 1){

                    String messageKey = dataSnapshot.getKey();

                    mLastKey = messageKey;
                    mPrevKey = messageKey;

                }*/

                messagesList.add(message);
                mAdapter.notifyDataSetChanged();

                mMessagesList.scrollToPosition(messagesList.size() - 1);

                //mRefreshLayout.setRefreshing(false);

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
        });

    }

    private void sendMessage() {

        //String message = mChatMessageView.getText().toString();
        String message = mChatMessageView.getText().toString();



        if(!TextUtils.isEmpty(message)){
            try {
                MAHEncryptor mahEncryptor = MAHEncryptor.newInstance("IzeKVH8DlBfBe2uHSf6fYJbVuFSmS31n");
                String encrypted = mahEncryptor.encode(message);
                message = encrypted;
            }catch (Exception e){
                e.printStackTrace();
            }


            String current_user_ref = "messages/" + mCurrentUserId + "/" + mChatUser;
            String chat_user_ref = "messages/" + mChatUser + "/" + mCurrentUserId;


            //

            DatabaseReference newNotificationref = mRootRef.child("notifications").child(mChatUser).push();
            String newNotificationId = newNotificationref.getKey();
            HashMap<String, String> notificationData = new HashMap<>();
            notificationData.put("from", mCurrentUserId);
            notificationData.put("type", "message");

            Map requestMap = new HashMap();
            requestMap.put("notifications/" + mChatUser + "/" + newNotificationId, notificationData);
            mRootRef.updateChildren(requestMap, new DatabaseReference.CompletionListener() {
                @Override
                public void onComplete(DatabaseError databaseError, DatabaseReference databaseReference) {
                    if(databaseError != null){
                        //Toast.makeText(ChatActivity.this, "There was some error in sending request", Toast.LENGTH_SHORT).show();
                    } else {
                    }
                }
            });

            //

            DatabaseReference user_message_push = mRootRef.child("messages")
                    .child(mCurrentUserId).child(mChatUser).push();

            String push_id = user_message_push.getKey();

            Map messageMap = new HashMap();
            messageMap.put("message", message);
            messageMap.put("seen", false);
            messageMap.put("type", "text");
            messageMap.put("time", ServerValue.TIMESTAMP);
            messageMap.put("from", mCurrentUserId);

            Map messageUserMap = new HashMap();
            messageUserMap.put(current_user_ref + "/" + push_id, messageMap);
            messageUserMap.put(chat_user_ref + "/" + push_id, messageMap);

            mChatMessageView.setText("");

            mRootRef.child("Chat").child(mCurrentUserId).child(mChatUser).child("seen").setValue(true);
            mRootRef.child("Chat").child(mCurrentUserId).child(mChatUser).child("timestamp").setValue(ServerValue.TIMESTAMP);

            mRootRef.child("Chat").child(mChatUser).child(mCurrentUserId).child("seen").setValue(false);
            mRootRef.child("Chat").child(mChatUser).child(mCurrentUserId).child("timestamp").setValue(ServerValue.TIMESTAMP);

            mRootRef.updateChildren(messageUserMap, new DatabaseReference.CompletionListener() {
                @Override
                public void onComplete(DatabaseError databaseError, DatabaseReference databaseReference) {

                    if(databaseError != null){

                        Log.d("CHAT_LOG", databaseError.getMessage().toString());

                    }

                }
            });

        }

    }
}
