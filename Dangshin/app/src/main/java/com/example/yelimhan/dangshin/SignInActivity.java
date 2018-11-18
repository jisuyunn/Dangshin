package com.example.yelimhan.dangshin;

import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;

import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

public class SignInActivity extends AppCompatActivity{

    Button btnBlind;
    Button btnVol;

    String u_id="";

    DatabaseReference mDatabase;
    DatabaseReference rDatabase;
    FirebaseDatabase db;
    DatabaseReference table;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_sign_in);

        btnBlind = (Button)findViewById(R.id.btnBlind);
        btnVol = (Button)findViewById(R.id.btnVolunteer);




        Intent intent = getIntent();
        u_id = intent.getStringExtra("ID");

        db = FirebaseDatabase.getInstance();
        table = db.getReference("UserInfo");


        //mDatabase = FirebaseDatabase.getInstance().getReference("UserInfo");
        //rDatabase = mDatabase.child("");

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
        UserInfo uif = new UserInfo(u_id,pos,true, 0);
        DatabaseReference newUser = table.push();
        newUser.setValue(uif);
        Intent intent = new Intent(SignInActivity.this,MainActivity.class);
        startActivity(intent);
        finish();
        return true;
    }

}
