package com.example.yelimhan.dangshin;

import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

public class MainActivity extends AppCompatActivity {


    String name = "";
    UserInfo ui = null;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();

        FirebaseDatabase database = FirebaseDatabase.getInstance();
        DatabaseReference table = database.getReference("UserInfo");


        // 현재 접속중인 사용자 있음 -> 다음 동작으로
        if (user != null) {
            name = user.getEmail();

            table.addListenerForSingleValueEvent(new ValueEventListener() {
                @Override
                public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                    ui = dataSnapshot.getValue(UserInfo.class);

                    if (ui != null){
                        // 포지션 별 엑티비티 이동
                        if(ui.u_position == "Blind"){
                            Intent intent = new Intent(MainActivity.this, QuestionActivity.class);
                            intent.putExtra("TEST",name);
                            startActivity(intent);
                        }
                        else if(ui.u_position == "Volunteer"){
                            Intent intent = new Intent(MainActivity.this, QuestionListActivity.class);
                            intent.putExtra("TEST",name);
                            startActivity(intent);
                        }
                    }
                }

                @Override
                public void onCancelled(@NonNull DatabaseError databaseError) {

                }
            });
//            Query query = table.child("UserInfo").orderByChild("u_googleId");
//            query.addListenerForSingleValueEvent(new ValueEventListener() {
//                @Override
//                public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
//                    for (DataSnapshot snapshot: dataSnapshot.getChildren()) {
//
//                    }
//                }
//
//                @Override
//                public void onCancelled(@NonNull DatabaseError databaseError) {
//
//                }
//            });


            // 현재 접속중인 사용자 없음 -> 로그인
        } else {
            Intent intent = new Intent(MainActivity.this, UserActivity.class);
            intent.putExtra("TEST",11);
            startActivity(intent);
        }
    }
}
