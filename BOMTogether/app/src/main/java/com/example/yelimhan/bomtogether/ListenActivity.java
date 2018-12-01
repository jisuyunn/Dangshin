
package com.example.yelimhan.bomtogether;

import android.annotation.TargetApi;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.media.AudioManager;
import android.media.MediaPlayer;
import android.net.Uri;
import android.os.Build;
import android.speech.tts.TextToSpeech;
import android.speech.tts.UtteranceProgressListener;
import android.support.annotation.NonNull;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.MotionEvent;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.SeekBar;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;

import java.io.IOException;
import java.util.HashMap;
import java.util.Locale;

// 질문에 대한 답변 확인 (Blind)
public class ListenActivity extends AppCompatActivity {

    private DatabaseReference mDatabase;
    private QuestionInfo questionInfo;
    private AnswerInfo answerInfo;
    private ImageView imageView;
    private TextView questionText;
    private TextView answerText;
    private StorageReference storageReference = FirebaseStorage.getInstance("gs://bomtogether-5f74b.appspot.com").getReference();
    private TextToSpeech tts;
    private DatabaseReference userReference;
    private MediaPlayer mediaPlayer = new MediaPlayer();
    private boolean isPlaying = false;
    private boolean isPrepared = false;
    private String text;
    private float curpos;
    private String utteranceId;
    private String userId;
    private SeekBar seekBar;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Intent intent = getIntent();
        questionInfo = (QuestionInfo)intent.getSerializableExtra("question");

        // 현재 접속중인 사용자의 정보를 받아옴. 없으면 null
        final FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
        userId = user.getEmail();
        // firebase database 참조 객체
        DatabaseReference table = FirebaseDatabase.getInstance().getReference("UserInfo");
        if(user != null) {
            Query query = table.orderByChild("u_googleId").equalTo(userId);
            query.addListenerForSingleValueEvent(new ValueEventListener() {       // 역할 알아오기
                @Override
                public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                    for (DataSnapshot snapshot : dataSnapshot.getChildren()) {
                        UserInfo ui = snapshot.getValue(UserInfo.class);
                        userReference = snapshot.getRef();
                        if(ui.u_position.equals("Volunteer")) {
                            setContentView(R.layout.activity_listen);
                            imageView = findViewById(R.id.answerimage2);
                            setMediaPlayer();
                        } else if(ui.u_position.equals("Blind")){
                            setContentView(R.layout.activity_blind_listen);
                            userReference.child("q_key").setValue("");
                            userReference.child("u_haveQuestion").setValue(0);
                        }
                        answerText = findViewById(R.id.answertext2);
                        showAnswer(ui.u_position);
                    }
                }
                @Override
                public void onCancelled(@NonNull DatabaseError databaseError) {
                }
            });
        }
    }

    // 재생 관련 함수
    public void setMediaPlayer() {
        final Button playbutton = findViewById(R.id.playbutton);
        seekBar = findViewById(R.id.seekbar);

        // 음성 파일 재생
        storageReference.child(questionInfo.q_voice).getDownloadUrl().addOnSuccessListener(new OnSuccessListener<Uri>() {
            @Override
            public void onSuccess(Uri uri) {
                try {
                    mediaPlayer.setDataSource(getApplicationContext(), uri);
                    mediaPlayer.prepare();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        });
        mediaPlayer.setOnPreparedListener(new MediaPlayer.OnPreparedListener() {
            @Override
            public void onPrepared(MediaPlayer mp) {
                seekBar.setMax(mediaPlayer.getDuration());
                seekBar.setProgress(0);
                isPrepared = true;
                findViewById(R.id.recordlinear).setVisibility(View.VISIBLE);
            }
        });
        mediaPlayer.setOnCompletionListener(new MediaPlayer.OnCompletionListener() {
            @Override
            public void onCompletion(MediaPlayer mp) {
                if(isPrepared) {
                    isPlaying = false;
                    mediaPlayer.seekTo(0);
                    seekBar.setProgress(0);
                    playbutton.setSelected(false);
                }
            }
        });
        seekBar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {
                if(isPrepared && isPlaying) {
                    isPlaying = false;
                    mediaPlayer.pause();
                    playbutton.setSelected(false);
                }
            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {
                if(isPrepared) {
                    mediaPlayer.seekTo(seekBar.getProgress());
                    if (seekBar.getProgress() == seekBar.getMax()) {
                        isPlaying = false;
                        playbutton.setSelected(false);
                    }
                }
            }
        });
        playbutton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(isPrepared) {
                    if (isPlaying) {
                        isPlaying = false;
                        mediaPlayer.pause();
                        v.setSelected(false);
                    } else {
                        isPlaying = true;
                        new PlayRecord().start();
                        mediaPlayer.start();
                        v.setSelected(true);
                    }
                }
            }
        });
    }

    private void doreport() {       // 신고처리 함수
        final Button reportbutton = findViewById(R.id.reportbutton);
        reportbutton.setVisibility(View.VISIBLE);
        reportbutton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                AlertDialog.Builder alt_bld = new AlertDialog.Builder(ListenActivity.this);
                alt_bld.setMessage("신고 하시겠습니까?").setCancelable(false)
                        .setPositiveButton("네",
                                new DialogInterface.OnClickListener() {
                                    public void onClick(DialogInterface dialog, int id) {
                                        // 네 클릭
                                        mDatabase = FirebaseDatabase.getInstance().getReference("ReportInfo");
                                        mDatabase.orderByChild("a_id").equalTo(answerInfo.a_id).addListenerForSingleValueEvent(new ValueEventListener() {
                                            @Override
                                            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                                                boolean isReport = false;
                                                for(DataSnapshot snapshot : dataSnapshot.getChildren()) {
                                                    if(snapshot.child("r_writer").getValue().equals(userId)) {
                                                        isReport = true;
                                                    }
                                                }
                                                if(!isReport){
                                                    String newReport = mDatabase.push().getKey();
                                                    ReportInfo reportInfo = new ReportInfo(newReport, userId, answerInfo.a_id);
                                                    mDatabase.child(newReport).setValue(reportInfo);
                                                    Toast.makeText(getApplicationContext(), "신고되었습니다!", Toast.LENGTH_SHORT).show();
                                                } else {
                                                    Toast.makeText(getApplicationContext(), "이미 신고한 답변입니다.", Toast.LENGTH_SHORT).show();
                                                }
                                            }

                                            @Override
                                            public void onCancelled(@NonNull DatabaseError databaseError) {

                                            }
                                        });
                                    }
                                }).setNegativeButton("아니오",
                        new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialog, int id) {
                                // 아니오 클릭. dialog 닫기.
                                dialog.cancel();
                            }
                        });
                AlertDialog alert = alt_bld.create();
                alert.setTitle("신고");

                // 대화창 배경 색 설정
                //alert.getWindow().setBackgroundDrawable(new ColorDrawable(Color.argb(255, 62, 79, 92)));
                alert.show();
            }
        });
    }


    private void showAnswer(final String u_position) {
        if (u_position.equals("Volunteer")) {      // 봉사자라면
            GlideApp.with(this)
                    .load(storageReference.child(questionInfo.q_pic))
                    .override(500, 300)
                    .into(imageView);
            imageView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    Intent intent = new Intent(ListenActivity.this, ImageManager.class);
                    intent.putExtra("question", questionInfo);
                    startActivity(intent);
                }
            });
            // STT 일단 뺌
            //questionText.setText("음성녹음 STT한 질문글");
        }
        mDatabase = FirebaseDatabase.getInstance().getReference("AnswerInfo");
        mDatabase.orderByChild("q_id").equalTo(questionInfo.q_id).addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                for (DataSnapshot snapshot : dataSnapshot.getChildren()) {
                    answerInfo = snapshot.getValue(AnswerInfo.class);
                    text = answerInfo.answer;
                    answerText.setText(text);
                    if (u_position.equals("Blind")) {    // 시각 장애인 이라면
                        texttospeechs();
                    } else {
                        if (!userId.equals(snapshot.child("a_writer").getValue().toString())) {
                            doreport();
                        }
                    }
                }
            }
            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
            }
        });
    }

    public void texttospeechs() {
        tts = new TextToSpeech(getApplicationContext(), new TextToSpeech.OnInitListener() {
            @Override
            public void onInit(int i) {
                if (i != TextToSpeech.ERROR) {
                    tts.setLanguage(Locale.KOREA);
                    // tts 한번만 읽어줌
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                        ttsGreater21(text);
                    } else {
                        ttsUnder20(text);
                    }
                }
                if(i == TextToSpeech.SUCCESS) {
                    tts.setOnUtteranceProgressListener(new UtteranceProgressListener() {
                        @Override
                        public void onStart(String utteranceId) {

                        }

                        @Override
                        public void onDone(String Id) {
                            if(Id.equals(utteranceId)) {
                                ttsGreater21("다시 들으시려면 화면을 위로 밀어주세요!");
                                utteranceId = "";
                            }
                        }

                        @Override
                        public void onError(String utteranceId) {

                        }
                    });
                }
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
        utteranceId=this.hashCode() + "";
        tts.speak(text, TextToSpeech.QUEUE_FLUSH, null, utteranceId);
    }

    @Override
    public void onStop() {
        if (tts != null) {
            tts.stop();
        }
        super.onStop();
    }

    @Override
    protected void onDestroy() {
        imageView.setOnClickListener(null);
        if(tts!=null) {
            tts.stop();
            tts.shutdown();
            tts = null;
        }
        super.onDestroy();
    }

    @Override
    public void onBackPressed() {
        imageView.setOnClickListener(null);
        isPrepared = false;
        mediaPlayer.stop();
        isPlaying = false;
        finish();
        super.onBackPressed();
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
                    texttospeechs();
                }
                else if(diff > 300){
                    Intent it = new Intent(this, MainActivity.class);
                    startActivity(it);
                    finish();
                }
                break;
            }
        }
        return super.onTouchEvent(event);
    }

    public class PlayRecord extends Thread {
        @Override
        public void run() {
            try {   // 시작할 때 버퍼링 임시 해결
                Thread.sleep(300);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
            while(isPlaying&&isPrepared) {
                seekBar.setProgress(mediaPlayer.getCurrentPosition());
            }

        }
    }
}