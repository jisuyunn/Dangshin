package com.example.yelimhan.dangshin;

import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.iid.FirebaseInstanceId;

public class SignInActivity extends AppCompatActivity{

    Button btnBlind;
    Button btnVol;
    String userID="";
    FirebaseDatabase db;
    DatabaseReference table;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_sign_in);


        Button btnVol = (Button)findViewById(R.id.btnVolunteer);
        Button btnBlind = (Button)findViewById(R.id.btnBlind);

        final FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
        userID = user.getEmail();


        db = FirebaseDatabase.getInstance();
        table = db.getReference("UserInfo");

        btnVol.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Toast.makeText(SignInActivity.this,"blind click",Toast.LENGTH_SHORT).show();
                saveUserToDatabase("Volunteer");
            }
        });

        btnBlind.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Toast.makeText(SignInActivity.this,"vol click",Toast.LENGTH_SHORT).show();
                saveUserToDatabase("Blind");
            }
        });

    }




    private Boolean saveUserToDatabase(String pos){

        // 파이어베이스에 데이터 넣는 부분(랜덤 키로 push)
        UserInfo uif = new UserInfo(userID,pos,true, 0, FirebaseInstanceId.getInstance().getToken(), "");
        DatabaseReference newUser = table.push();
        newUser.setValue(uif);
        Intent intent = new Intent(SignInActivity.this,MainActivity.class);
        startActivity(intent);
        finish();
        return true;
    }

}
