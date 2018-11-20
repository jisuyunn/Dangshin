package com.example.yelimhan.dangshin;

import android.content.DialogInterface;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.design.widget.TabLayout;
import android.support.v4.view.ViewPager;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;

import com.google.android.gms.auth.api.Auth;
import com.google.android.gms.auth.api.signin.GoogleSignInOptions;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.common.api.ResultCallback;
import com.google.android.gms.common.api.Status;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;

public class QuestionListActivity extends AppCompatActivity implements GoogleApiClient.OnConnectionFailedListener{

    private FirebaseAuth mAuth;
    private GoogleApiClient mGoogleApiClient;
    public QuestionListAdapter adapter;
    public Button bt;
    public String userID;
    public boolean firstTime = true;
    public ViewPager viewPager;
    public QuestionListFragment qlFragment;
    public QuestionDoneListFragment qdlFragment;
    private final long FINISH_INTERVAL_TIME = 2000;
    private long backPressedTime = 0;
    public String userIndexId;
    boolean flag = true;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_question_list);

        Log.d("testt", " Q L A onCreate");

        bt = (Button) findViewById(R.id.logout);

        mAuth = FirebaseAuth.getInstance();
        GoogleSignInOptions gso = new GoogleSignInOptions.Builder
                (GoogleSignInOptions.DEFAULT_SIGN_IN)
                .requestIdToken(getString(R.string.default_web_client_id))
                .requestEmail()
                .build();
        mGoogleApiClient = new GoogleApiClient.Builder(this)
                .enableAutoManage(this, this )
                .addApi(Auth.GOOGLE_SIGN_IN_API, gso)
                .build();

        final FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
        if(user != null){
            userID = user.getEmail();
        }
        else
            Toast.makeText(this, "로그인 정보가 없습니다!", Toast.LENGTH_SHORT).show();

        getUserIndexId();
        // 로그아웃 버튼 클릭 이벤트 > dialog 예/아니오
        Button logout_btn_google = (Button) findViewById(R.id.logout);
        logout_btn_google.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(final View view) {
                Log.v("알림", "구글 LOGOUT");
                AlertDialog.Builder alt_bld = new AlertDialog.Builder(view.getContext());
                alt_bld.setMessage("로그아웃 하시겠습니까?").setCancelable(false)
                        .setPositiveButton("네",
                                new DialogInterface.OnClickListener() {
                                    public void onClick(DialogInterface dialog, int id) {
                                        // 네 클릭
                                        // 로그아웃 함수 call
                                        signOut();
                                    }
                                }).setNegativeButton("아니오",
                        new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialog, int id) {
                                // 아니오 클릭. dialog 닫기.
                                dialog.cancel();
                            }
                        });
                AlertDialog alert = alt_bld.create();
                alert.setTitle("로그아웃");

                // 대화창 배경 색 설정
                alert.getWindow().setBackgroundDrawable(new ColorDrawable(Color.argb(255,62,79,92)));
                alert.show();
            }
        });

        //Toast.makeText(this, "봉사자 - 질문 선택",Toast.LENGTH_SHORT).show();
        ViewPager viewPager = findViewById(R.id.pager);
        setupViewPager(viewPager);
        TabLayout tabLayout = findViewById(R.id.tabs);
        tabLayout.setupWithViewPager(viewPager);
        // 홈 버튼 눌렀을 때
        findViewById(R.id.button_home).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

            }
        });
        // refresh버튼 눌렀을 때
        findViewById(R.id.button_refresh).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                view.setAlpha(.5f);
                qlFragment.getFirebaseData();
                qdlFragment.getFirebaseData();
                view.setAlpha(1f);
            }
        });
    }


    // 유저의 인덱스 아이디(일련번호)를 알아내는 함수 (액티비티 시작할때 실행됨)
    public void getUserIndexId(){
        DatabaseReference updateReference;
        updateReference = FirebaseDatabase.getInstance().getReference("UserInfo");
        Query query = updateReference.orderByChild("u_googleId").equalTo(userID);

        if(flag){
            query.addListenerForSingleValueEvent(new ValueEventListener() {
                @Override
                public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                    for(DataSnapshot child : dataSnapshot.getChildren()){
                        userIndexId = child.getKey().toString();
                        Log.d("testt", " Q L A ondatachanged  "+ userIndexId + "   "+String.valueOf(flag));
                    }
                }

                @Override
                public void onCancelled(@NonNull DatabaseError databaseError) {

                }
            });
        }

    }

    private void setupViewPager(ViewPager viewPager) {

        Log.d("testt", "   setupviewpager");

        adapter = new QuestionListAdapter(getSupportFragmentManager());
        qlFragment = new QuestionListFragment();
        qdlFragment = new QuestionDoneListFragment();
        adapter.addFrag(qlFragment, "질문");
        adapter.addFrag(qdlFragment, "완료질문");
        viewPager.setAdapter(adapter);

    }

    // 로그아웃
    public void signOut() {
        mGoogleApiClient.connect();
        mGoogleApiClient.registerConnectionCallbacks(new GoogleApiClient.ConnectionCallbacks() {
            @Override
            public void onConnected(@Nullable Bundle bundle) {
                mAuth.signOut();
                if (mGoogleApiClient.isConnected()) {
                    Auth.GoogleSignInApi.signOut(mGoogleApiClient).setResultCallback(new ResultCallback<Status>() {
                        @Override
                        public void onResult(@NonNull Status status) {
                            if (status.isSuccess()) {
                                Log.v("알림", "로그아웃 성공");
                                setResult(1);
                            } else {
                                setResult(0);
                            }
                            finish();
                        }
                    });
                }
            }
            @Override
            public void onConnectionSuspended(int i) {
                Log.v("알림", "Google API Client Connection Suspended");
                setResult(-1);
                finish();
            }
        });
    }

    @Override
    public void onConnectionFailed(@NonNull ConnectionResult connectionResult) {
        Log.v("알림", "onConnectionFailed");
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        Log.d("testt", " Q L A onDestroy");

        DatabaseReference updateReference;
        updateReference = FirebaseDatabase.getInstance().getReference("UserInfo");

        updateReference.child(userIndexId).child("u_online").setValue(false);
        Toast.makeText(this, "종료",Toast.LENGTH_SHORT).show();


    }

    @Override
    protected void onPause() {
        super.onPause();
        Log.d("testt", " Q L A onPause");

    }

    @Override
    protected void onResume() {
        super.onResume();
        Log.d("testt", " Q L A onResume");

    }


    @Override
    public void onStart() {
        super.onStart();
        Log.d("testt", " Q L A onStart");

        if(!firstTime) {
            qlFragment.getFirebaseData();
            qdlFragment.getFirebaseData();
        }
    }

    @Override
    public void onStop() {
        super.onStop();

        firstTime = false;
        flag = false;

        Log.d("testt", " Q L A onStop   "+String.valueOf(flag));

    }


    @Override
    public void onBackPressed() {

        super.onBackPressed();
//        long tempTime = System.currentTimeMillis();
//        long intervalTime = tempTime - backPressedTime;
//
//        if (0 <= intervalTime && FINISH_INTERVAL_TIME >= intervalTime)
//        {
//            super.onBackPressed();
//        }
//        else
//        {
//            backPressedTime = tempTime;
//            Toast.makeText(getApplicationContext(), "한번 더 뒤로가기 누르면 꺼집니다", Toast.LENGTH_SHORT).show();
//        }
    }
}
