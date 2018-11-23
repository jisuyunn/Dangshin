package com.example.yelimhan.dangshin;

import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.view.MotionEvent;
import android.widget.TextView;
import android.widget.Toast;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

public class QuestionAgainActivity extends AppCompatActivity {

    float curpos;
    public QuestionInfo questionInfo;
    final FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
    public String userId = user.getEmail();
    DatabaseReference reference;
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_question);
        Toast.makeText(this, "  Blind - 재질문하기 (QuestionAgainActivity",Toast.LENGTH_SHORT).show();
        Intent intent = getIntent();
        questionInfo = (QuestionInfo)intent.getSerializableExtra("question");
        TextView text = findViewById(R.id.textView);
        text.setText("답변이 없네요\n 위로 화면을 밀면 \n새로운 질문을 하실 수 있습니다!");
    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        switch (event.getAction()) {
            case MotionEvent.ACTION_DOWN:
                curpos = event.getY();
                break;
            case MotionEvent.ACTION_UP: {
                float diff = event.getY() - curpos;
                if(diff < -300) {
                    setQuestionInfo();
                }
                break;
            }
        }
        return super.onTouchEvent(event);
    }

    public void setQuestionInfo() {

        FirebaseDatabase.getInstance().getReference("UserInfo")
                .orderByChild("u_googleId").equalTo(userId).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                for(DataSnapshot snapshot : dataSnapshot.getChildren()) {
                    reference = snapshot.getRef();
                    reference.child("u_haveQuestion").setValue(0);
                    //FirebaseDatabase.getInstance().getReference("QuestionInfo").child(questionInfo.q_id).removeValue();
                    finish();
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });
    }
}
