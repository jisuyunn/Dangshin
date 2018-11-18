package com.example.yelimhan.dangshin;

import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;

public class MainActivity extends AppCompatActivity {


    String userId = "";

    @Override
    protected void onResume() {
        super.onResume();
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        // 현재 접속중인 사용자의 정보를 받아옴. 없으면 null
        final FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();

        // firebase database 참조 객체
        DatabaseReference table = FirebaseDatabase.getInstance().getReference("UserInfo");


        // 현재 접속중인 사용자 있음 -> 다음 동작으로
        if (user != null) {
            userId = user.getEmail();

            Query query = table.orderByChild("u_googleId").equalTo(userId);
            query.addValueEventListener(new ValueEventListener() {
                @Override
                public void onDataChange(@NonNull DataSnapshot dataSnapshot) {

                    for (DataSnapshot snapshot: dataSnapshot.getChildren()) {
                        UserInfo ui = snapshot.getValue(UserInfo.class);

                        if(ui.u_position.equals("Volunteer")){      // 접속한 사용자가 봉사자일 경우
                            Intent it = new Intent(MainActivity.this,QuestionListActivity.class);
                            it.putExtra("USERID",userId);
                            startActivity(it);
                        }
                        else if(ui.u_position.equals("Blind")){     // 접속한 사용자가 시각장애인일 경우


                            if(ui.u_haveQuestion == 0){             // 시각장애인의 질문이 없는 경우 -> QuestionActivity로
                                Intent it = new Intent(MainActivity.this,QuestionActivity.class);
                                it.putExtra("USERID",userId);
                                startActivity(it);
                            }
                            else if (ui.u_haveQuestion == 1){       // 시각장애인의 질문이 있는데 답변 없는 경우 -> ReQuesion?
                                Intent it = new Intent(MainActivity.this,QuestionListActivity.class);
                                it.putExtra("USERID",userId);
                                startActivity(it);
                            }
                            else if(ui.u_haveQuestion == 2)         // 시각장애인의 질문이 있고 답변이 달린 경우 -> ListenActivity
                            {
                                Intent it = new Intent(MainActivity.this,ListenActivity.class);
                                it.putExtra("USERID",userId);
                                startActivity(it);
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
            intent.putExtra("TEST",11);
            startActivity(intent);
           // MainActivity.this.finish();
        }
    }

    @Override
    protected void onPause() {
        super.onPause();
        finish();
    }
}
