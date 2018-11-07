package com.example.yelimhan.dangshin;

import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
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


// 질문하기 (Blind)
public class QuestionActivity extends AppCompatActivity implements GoogleApiClient.OnConnectionFailedListener {

    private FirebaseAuth mAuth;
    private GoogleApiClient mGoogleApiClient;
    public Button bt;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_question);

        // GoogleSignInOptions 생성
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
        bt = (Button) findViewById(R.id.logout);
        Intent intent = getIntent();
        String id = intent.getStringExtra("TEST");
        Toast.makeText(this,id+"님이 로그인하셨습니다!", Toast.LENGTH_LONG).show();

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


                // 대화창 클릭시 뒷 배경 어두워지는 것 막기

                //alert.getWindow().clearFlags(WindowManager.LayoutParams.FLAG_DIM_BEHIND);


                // 대화창 제목 설정

                alert.setTitle("로그아웃");


                // 대화창 아이콘 설정

               // alert.setIcon(R.drawable.check_dialog_64);


                // 대화창 배경 색 설정

                alert.getWindow().setBackgroundDrawable(new ColorDrawable(Color.argb(255,62,79,92)));

                alert.show();


            }

        });

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
}
