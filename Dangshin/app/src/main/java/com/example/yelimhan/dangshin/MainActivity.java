package com.example.yelimhan.dangshin;

import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

public class MainActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();


        if (user != null) {
            // User is signed in
            Intent intent = new Intent(MainActivity.this, QuestionActivity.class);
            String name = user.getEmail();
            intent.putExtra("TEST",name);
            startActivity(intent);

        } else {
            // No user is signed in
            Intent intent = new Intent(MainActivity.this, UserActivity.class);
            intent.putExtra("TEST",11);
            startActivity(intent);
        }
    }
}
