package com.example.yelimhan.bomtogether;

import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.design.widget.NavigationView;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;

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

public class MainActivity extends AppCompatActivity implements GoogleApiClient.OnConnectionFailedListener{

    private FirebaseAuth mAuth;
    private GoogleApiClient mGoogleApiClient;
    String userId = "";
    public boolean flag = true;
    String userIndexId = "";
    String qid = "";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        // 현재 접속중인 사용자의 정보를 받아옴. 없으면 null
        final FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();

        // firebase database 참조 객체
        final DatabaseReference table = FirebaseDatabase.getInstance().getReference("UserInfo");
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
                alert.getWindow().setBackgroundDrawable(new ColorDrawable(Color.argb(255,213,167,212)));
                alert.show();
            }
        });


        // 현재 접속중인 사용자 있음 -> 다음 동작으로
        if (user != null) {
            userId = user.getEmail();

            Query query = table.orderByChild("u_googleId").equalTo(userId);
            query.addListenerForSingleValueEvent(new ValueEventListener() {
                @Override
                public void onDataChange(@NonNull DataSnapshot dataSnapshot) {

                    for (DataSnapshot snapshot: dataSnapshot.getChildren()) {
                        userIndexId = snapshot.getKey().toString();
                        UserInfo ui = snapshot.getValue(UserInfo.class);

                        if(ui.u_position.equals("Volunteer")){      // 접속한 사용자가 봉사자일 경우
                            DatabaseReference updateReference;
                            updateReference = FirebaseDatabase.getInstance().getReference("UserInfo");
                            final Query query = updateReference.orderByChild("u_googleId").equalTo(userId);
                            query.addListenerForSingleValueEvent(new ValueEventListener() {
                                @Override
                                public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                                    for(DataSnapshot child : dataSnapshot.getChildren()){
                                        child.getRef().child("u_online").setValue(true);
                                        qid = child.getRef().child("urgent_qid").toString();
                                        if(!qid.equals("")){
                                            DatabaseReference uqidReference;
                                            uqidReference = FirebaseDatabase.getInstance().getReference("QuestionInfo");
                                            Query query1 = uqidReference.orderByChild("q_id").equalTo(qid);
                                            query1.addListenerForSingleValueEvent(new ValueEventListener() {
                                                @Override
                                                public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                                                    for (DataSnapshot snapshot : dataSnapshot.getChildren()) {
                                                        QuestionInfo qi  = snapshot.getValue(QuestionInfo.class);
                                                        Intent intent = new Intent(MainActivity.this, AnswerActivity.class);
                                                        Bundle bundle = new Bundle();
                                                        bundle.putSerializable("question", qi);
                                                        intent.putExtras(bundle);
                                                        MainActivity.this.startActivity(intent);
                                                    }
                                                }

                                                @Override
                                                public void onCancelled(@NonNull DatabaseError databaseError) {

                                                }
                                            });

                                        }
                                    }
                                }                @Override
                                public void onCancelled(@NonNull DatabaseError databaseError) {
                                }
                            });

                            Intent it = new Intent(MainActivity.this,QuestionListActivity.class);
                            startActivity(it);
                            finish();
                        }
                        else if(ui.u_position.equals("Blind")){     // 접속한 사용자가 시각장애인일 경우

                            if(ui.u_haveQuestion == 0){             // 시각장애인의 질문이 없는 경우 -> QuestionActivity로
                                Intent it = new Intent(MainActivity.this,QuestionActivity.class);
                                it.putExtra("USERID",userId);
                                it.putExtra("USERINDEX", userIndexId);
                                startActivity(it);
                                finish();
                            }
                            else if (ui.u_haveQuestion == 1)        // 시각장애인의 질문이 있는데 답변 없는 경우 -> ReQuesion?
                            {
                                FirebaseDatabase.getInstance().getReference("QuestionInfo")
                                        .orderByKey().equalTo(ui.q_key)
                                        .addListenerForSingleValueEvent(new ValueEventListener() {
                                            @Override
                                            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                                                for (DataSnapshot snapshot : dataSnapshot.getChildren()) {
                                                    QuestionInfo questionInfo = snapshot.getValue(QuestionInfo.class);
                                                    Intent it = new Intent(MainActivity.this,QuestionAgainActivity.class);
                                                    //Intent it = new Intent(MainActivity.this, QuestionActivity.class);
                                                    //Bundle bundle = new Bundle();
                                                    //bundle.putSerializable("question",questionInfo);
                                                    //it.putExtras(bundle);
                                                    it.putExtra("VOICE", questionInfo.q_voice);
                                                    it.putExtra("USERID",userId);
                                                    it.putExtra("USERINDEX", userIndexId);
                                                    startActivity(it);
                                                    finish();
                                                }
                                            }
                                            @Override
                                            public void onCancelled(@NonNull DatabaseError databaseError) {

                                            }
                                        });
                            }
                            else if(ui.u_haveQuestion == 2)         // 시각장애인의 질문이 있고 답변이 달린 경우 -> ListenActivity
                            {
                                FirebaseDatabase.getInstance().getReference("QuestionInfo")
                                        .orderByKey().equalTo(ui.q_key)
                                        .addListenerForSingleValueEvent(new ValueEventListener() {
                                            @Override
                                            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                                                for (DataSnapshot snapshot : dataSnapshot.getChildren()) {
                                                    QuestionInfo questionInfo = snapshot.getValue(QuestionInfo.class);
                                                    Intent it = new Intent(MainActivity.this,ListenDoneActivity.class);
                                                    Bundle bundle = new Bundle();
                                                    bundle.putSerializable("question",questionInfo);
                                                    it.putExtra("USERID", userId);
                                                    it.putExtra("USERINDEX", userIndexId);
                                                    Log.d("testt", "Main have 2 indexid : "+userIndexId);
                                                    it.putExtras(bundle);
                                                    startActivity(it);
                                                    finish();
                                                }
                                            }
                                            @Override
                                            public void onCancelled(@NonNull DatabaseError databaseError) {

                                            }
                                        });
                            }
                        }
                    }
                }


                @Override
                public void onCancelled(@NonNull DatabaseError databaseError) {
                    Log.w("loadPost:onCancelled", databaseError.toException());

                }
            });


            // 현재 접속중인 사용자 없음 -> 로그인(가입)
        } else {
            Intent intent = new Intent(MainActivity.this, UserActivity.class);
            startActivity(intent);
            finish();
        }
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

    }
}
