package com.example.yelimhan.dangshin;

import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.Button;

import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

public class SignInActivity extends AppCompatActivity implements View.OnClickListener{

    Button btnBlind;
    Button btnVol;

    String pos = "";

    DatabaseReference mDatabase;
    DatabaseReference rDatabase;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_sign_in);

        btnBlind = (Button)findViewById(R.id.btnBlind);
        btnVol = (Button)findViewById(R.id.btnVolunteer);


        Intent intent = getIntent();
        String u_id = intent.getStringExtra("ID");

        mDatabase = FirebaseDatabase.getInstance().getReference("UserInfo");
        rDatabase = mDatabase.child("");
    }


    @Override
    public void onClick(View v) {

        switch (v.getId()){

            case R.id.btnBlind:
                saveUserToDatabase("Blind");
                break;

            case R.id.btnVolunteer:
                saveUserToDatabase("Volunteer");
                break;
        }
    }

    private Boolean saveUserToDatabase(String pos){


        UserInfo uif = new UserInfo("id",pos,1);
        mDatabase.child("UserInfo").child("00002").setValue(uif);
        return true;
    }

}
