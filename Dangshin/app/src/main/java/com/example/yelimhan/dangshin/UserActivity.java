package com.example.yelimhan.dangshin;

import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.auth.api.Auth;
import com.google.android.gms.auth.api.signin.GoogleSignIn;
import com.google.android.gms.auth.api.signin.GoogleSignInAccount;
import com.google.android.gms.auth.api.signin.GoogleSignInClient;
import com.google.android.gms.auth.api.signin.GoogleSignInOptions;
import com.google.android.gms.auth.api.signin.GoogleSignInResult;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.SignInButton;
import com.google.android.gms.common.api.ApiException;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthCredential;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.auth.GoogleAuthProvider;
import com.google.firebase.database.DatabaseReference;


public class UserActivity extends AppCompatActivity {

    // 구글로그인 result 상수
    private static final int RC_SIGN_IN = 900;

    // 구글api클라이언트
    private GoogleSignInClient mGoogleSignInClient;
    private GoogleApiClient mGoogleApiClient;

    // 파이어베이스 인증 객체
    private FirebaseAuth mAuth;

    // 유저 데이터베이스 참조 객체
    private DatabaseReference UserDatabase;

    private String userId;  // 유저의 ID




    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_user);

        TextView tv  = findViewById(R.id.tv1);

        // 파이어베이스 인증 객체 선언
        mAuth = FirebaseAuth.getInstance();

        // Configure Google Sign In
        GoogleSignInOptions gso = new GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
                .requestIdToken(getString(R.string.default_web_client_id))
                .requestEmail()
                .build();
        mGoogleApiClient = new GoogleApiClient.Builder(this)
                .enableAutoManage(this /* FragmentActivity */,
                        new GoogleApiClient.OnConnectionFailedListener() {
                            @Override
                            public void onConnectionFailed(@NonNull ConnectionResult connectionResult) {
                            }
                        } /* OnConnectionFailedListener */)
                .addApi(Auth.GOOGLE_SIGN_IN_API, gso)
                .build();


        mGoogleSignInClient = GoogleSignIn.getClient(this, gso);

        // Set the dimensions of the sign-in button.
        SignInButton signInButton = findViewById(R.id.sign_in_button);
        signInButton.setSize(SignInButton.SIZE_STANDARD);
        signInButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                signIn();
            }
        });

    }
    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        // 구글로그인 버튼 응답
        if (requestCode == RC_SIGN_IN) {
            Task<GoogleSignInAccount> task = GoogleSignIn.getSignedInAccountFromIntent(data);
            try {
                // Google Sign In was successful, authenticate with Firebase
                GoogleSignInAccount account = task.getResult(ApiException.class);
                firebaseAuthWithGoogle(account);
                GoogleSignInResult result = Auth.GoogleSignInApi.getSignInResultFromIntent(data);
                if (result != null) {
                    if (result.isSuccess()) {

                        // 로그인 성공 했을때
                        GoogleSignInAccount acct = result.getSignInAccount();

                        String personName = acct.getDisplayName();
                        String personEmail = acct.getEmail();
                        String personId = acct.getId();
                        String tokenKey = acct.getServerAuthCode();
                        mGoogleApiClient.disconnect();

                        Log.e("GoogleLogin", "personNa1me=" + personName);
                        Log.e("GoogleLogin", "personEmail=" + personEmail);
                        Log.e("GoogleLogin", "personId=" + personId);
                        Log.e("GoogleLogin", "tokenKey=" + tokenKey);
                        Toast.makeText(UserActivity.this,"구글로그인성공",Toast.LENGTH_LONG).show();

                    } else {
                        // 로그인 실패 했을때
                        Log.e("GoogleLogin", "login fail cause=" + result.getStatus().getStatusMessage());
                    }
                }
            } catch (ApiException e) {
                // 구글 로그인 실패
                Log.e("GoogleLogin", "fail cause=" + e.toString());
            }
        }
    }
    // 사용자가 정상적으로 로그인한 후에 GoogleSignInAccount 개체에서 ID 토큰을 가져와서
    // Firebase 사용자 인증 정보로 교환하고 Firebase 사용자 인증 정보를 사용해 Firebase에 인증합니다.
    private void firebaseAuthWithGoogle(GoogleSignInAccount acct) {
        AuthCredential credential = GoogleAuthProvider.getCredential(acct.getIdToken(), null);
        mAuth.signInWithCredential(credential)
                .addOnCompleteListener(this, new OnCompleteListener<AuthResult>() {
                    @Override
                    public void onComplete(@NonNull Task<AuthResult> task) {
                        if (task.isSuccessful()) {
                            Toast.makeText(UserActivity.this, "로그인 성공", Toast.LENGTH_LONG).show();
                            // 로그인 성공
                            FirebaseUser user = mAuth.getCurrentUser();

                            Intent intent = new Intent(UserActivity.this,SignInActivity.class);
                            intent.putExtra("ID",user.getEmail());
                            startActivity(intent);
                            UserActivity.this.finish();

                        } else {
                            // 로그인 실패
                            // updateUI(null);
                            Log.e("GoogleLogin", "firebase login fail cause=");
                        }
                    }
                });
    }

    @Override
    protected void onStart() {
        super.onStart();
        // 활동을 초기화할 때 사용자가 현재 로그인되어 있는지 확인합니다.
        //FirebaseUser currentUser = mAuth.getCurrentUser();

    }
    private void signIn() {
        Intent signInIntent = mGoogleSignInClient.getSignInIntent();
        startActivityForResult(signInIntent, RC_SIGN_IN);
    }

}
