package com.example.yelimhan.dangshin;

import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;

public class AnswerActivity extends AppCompatActivity {

    private DatabaseReference mDatabase;
    private QuestionInfo questionInfo;
    private ImageView imageView;
    private TextView questionText;
    private EditText editText;
    private StorageReference storageReference = FirebaseStorage.getInstance("gs://dangshin-fa136.appspot.com").getReference();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_answer);
        Intent intent = getIntent();
        questionInfo = (QuestionInfo)intent.getSerializableExtra("question");
        imageView = findViewById(R.id.answerimage);
        GlideApp.with(this)
                .load(storageReference.child(questionInfo.q_pic))
                .override(500,300)
                .into(imageView);
        questionText = findViewById(R.id.questiontext);
        // STT
        questionText.setText("음성녹음 STT한 질문글");
        editText = findViewById(R.id.answertext);

        // 확인버튼 누르면 답변 디비에 저장
        findViewById(R.id.checkbutton).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if(!editText.equals("")) {
                    mDatabase = FirebaseDatabase.getInstance().getReference("AnswerInfo");
                    String newAnswer = mDatabase.push().getKey();
                    AnswerInfo answerInfo = new AnswerInfo(newAnswer,questionInfo.q_id,"a",editText.getText().toString());
                    mDatabase.child(newAnswer).setValue(answerInfo);
                    mDatabase = FirebaseDatabase.getInstance().getReference("QuestionInfo");
                    mDatabase.child(questionInfo.q_id).child("checkAnswer").setValue(true);
                    startActivity(new Intent(AnswerActivity.this,QuestionListActivity.class));
                }
            }
        });

    }
}
