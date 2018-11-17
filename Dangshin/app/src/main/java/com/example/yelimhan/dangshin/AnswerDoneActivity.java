package com.example.yelimhan.dangshin;

import android.annotation.TargetApi;
import android.content.Intent;
import android.os.Build;
import android.os.Bundle;
import android.speech.tts.TextToSpeech;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;

import java.util.HashMap;
import java.util.Locale;

public class AnswerDoneActivity extends AppCompatActivity {

    private DatabaseReference mDatabase;
    private QuestionInfo questionInfo;
    private ImageView imageView;
    private TextView questionText;
    private TextView answerText;
    private StorageReference storageReference = FirebaseStorage.getInstance("gs://dangshin-fa136.appspot.com").getReference();
    private TextToSpeech tts;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_answer_done);
        Intent intent = getIntent();
        questionInfo = (QuestionInfo)intent.getSerializableExtra("question");
        imageView = findViewById(R.id.answerimage2);
        GlideApp.with(this)
                .load(storageReference.child(questionInfo.q_pic))
                .override(500,300)
                .into(imageView);
        questionText = findViewById(R.id.questiontext2);
        // STT
        questionText.setText("음성녹음 STT한 질문글");
        answerText = findViewById(R.id.answertext2);
        tts = new TextToSpeech(getApplicationContext(), new TextToSpeech.OnInitListener() {
            @Override
            public void onInit(int i) {
                if(i != TextToSpeech.ERROR) {
                    tts.setLanguage(Locale.KOREA);
                }
            }
        });

        mDatabase = FirebaseDatabase.getInstance().getReference("AnswerInfo");
        mDatabase.orderByChild("q_id").equalTo(questionInfo.q_id).addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                for(DataSnapshot snapshot : dataSnapshot.getChildren()) {
                    String text = snapshot.child("answer").getValue().toString();
                    answerText.setText(text);

                    // tts 한번만 읽어줌
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                        ttsGreater21(text);
                    } else {
                        ttsUnder20(text);
                    }
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });
    }

    @SuppressWarnings("deprecation")
    private void ttsUnder20(String text) {
        HashMap<String, String> map = new HashMap<>();
        map.put(TextToSpeech.Engine.KEY_PARAM_UTTERANCE_ID, "MessageId");
        tts.speak(text, TextToSpeech.QUEUE_FLUSH, map);
    }

    @TargetApi(Build.VERSION_CODES.LOLLIPOP)
    private void ttsGreater21(String text) {
        String utteranceId=this.hashCode() + "";
        tts.speak(text, TextToSpeech.QUEUE_FLUSH, null, utteranceId);
    }
}
