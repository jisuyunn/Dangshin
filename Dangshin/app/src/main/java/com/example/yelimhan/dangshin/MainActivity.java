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
                                        Log.d("testt", "urgent qid : "+qid);
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
                                                        Log.d("testt", "query qid success: "+qid);
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

}
