package com.example.yelimhan.bomtogether;

import android.annotation.SuppressLint;
import android.annotation.TargetApi;
import android.content.Intent;
import android.media.MediaPlayer;
import android.net.Uri;
import android.os.Build;
import android.os.Handler;
import android.speech.tts.TextToSpeech;
import android.speech.tts.UtteranceProgressListener;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.GestureDetector;
import android.view.MotionEvent;
import android.view.View;
import android.widget.Button;
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
public class ListenDoneActivity extends AppCompatActivity {

    private DatabaseReference mDatabase;
    private QuestionInfo questionInfo;
    private StorageReference storageReference = FirebaseStorage.getInstance("gs://bomtogether-5f74b.appspot.com").getReference();
    private TextToSpeech tts;
    private DatabaseReference userReference;
    private MediaPlayer mediaPlayer = new MediaPlayer();
    private boolean isPlaying = false;
    private boolean isPrepared = false;
    private String text;
    private String utteranceId;
    private String userId;
    private SeekBar seekBar;
    private static final int SWIPE_MIN_DISTANCE = 120;
    GestureDetector detector;
    public String userIndexId = "";
    public TextView emailView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_listen_done);
        Intent intent = getIntent();
        questionInfo = (QuestionInfo)intent.getSerializableExtra("question");
        userIndexId = intent.getStringExtra("USERINDEX");

        emailView = findViewById(R.id.email_id);
        emailView.setText("ID : " + userId);

        // 현재 접속중인 사용자의 정보를 받아옴. 없으면 null
        final FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
        userId = user.getEmail();
        // firebase database 참조 객체
        DatabaseReference table = FirebaseDatabase.getInstance().getReference("UserInfo");
        detector = new GestureDetector(this, new ListenDoneActivity.GestureAdapter());
        if(user != null) {
            Query query = table.orderByChild("u_googleId").equalTo(userId);
            query.addListenerForSingleValueEvent(new ValueEventListener() {       // 역할 알아오기
                @Override
                public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                    for (DataSnapshot snapshot : dataSnapshot.getChildren()) {
                        UserInfo ui = snapshot.getValue(UserInfo.class);
                        userReference = snapshot.getRef();
                        if(ui.u_position.equals("Volunteer")) {
                            setMediaPlayer();
                        } else if(ui.u_position.equals("Blind")){
                            //userReference.child("q_key").setValue("");
                            //userReference.child("u_haveQuestion").setValue(2); /////////////////////////////////////////////////////////////////
                        }
                        //showAnswer(ui.u_position);
                    }
                }
                @Override
                public void onCancelled(@NonNull DatabaseError databaseError) {
                }
            });
        }

        setMediaPlayer();
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
                //findViewById(R.id.recordlinear).setVisibility(View.VISIBLE);
                mp.start();
                int duration = mp.getDuration();
                final Handler delayHandler = new Handler();
                delayHandler.postDelayed(new Runnable() {
                    @Override
                    public void run() {
                        tts = new TextToSpeech(ListenDoneActivity.this, new TextToSpeech.OnInitListener() {
                            @Override
                            public void onInit(int status) {
                                if(status != android.speech.tts.TextToSpeech.ERROR) {
                                    tts.setLanguage(Locale.KOREAN);
                                }

                                String speach = "질문에 답이 달렸어요!";
                                tts.speak(speach, TextToSpeech.QUEUE_FLUSH, null);
                                showAnswer("Blind");
                                texttospeechs();

                                if(status == TextToSpeech.SUCCESS) {
                                    tts.setOnUtteranceProgressListener(new UtteranceProgressListener() {
                                        @Override
                                        public void onStart(String utteranceId) {

                                        }

                                        @Override
                                        public void onDone(String Id) {
                                            ttsGreater21("다시 들으시려면 화면을 위로 밀어주세요! 새로 질문하시려면 화면을 아래로 밀어주세요.");
                                        }

                                        @Override
                                        public void onError(String utteranceId) {

                                        }
                                    });
                                }
                            }
                        });
                    }
                }, duration);
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

    private void doreport() {
        final Button reportbutton = findViewById(R.id.reportbutton);
        reportbutton.setVisibility(View.VISIBLE);
        reportbutton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Toast.makeText(getApplicationContext(), "신고하기", Toast.LENGTH_LONG).show();
            }
        });
    }

    private void showAnswer(final String u_position) {
        if (u_position.equals("Volunteer")) {      // 봉사자라면
//            GlideApp.with(this)
//                    .load(storageReference.child(questionInfo.q_pic))
//                    .override(500, 300)
//                    .into(imageView);
            // STT
            //questionText.setText("음성녹음 STT한 질문글");
        } else if(u_position.equals("Blind")) {        // 시각 장애인 이라면
            //findViewById(R.id.recordlinear).setVisibility(View.GONE);
        }
        mDatabase = FirebaseDatabase.getInstance().getReference("AnswerInfo");
        mDatabase.orderByChild("q_id").equalTo(questionInfo.q_id).addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                for (DataSnapshot snapshot : dataSnapshot.getChildren()) {
                    text = snapshot.child("answer").getValue().toString();
                    //answerText.setText(text);
                    if (u_position.equals("Blind")) {    // 시각 장애인 이라면
                        texttospeechs();
                    }
                    if (!userId.equals(snapshot.child("a_writer").getValue().toString())) {
                        doreport();
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
                                ttsGreater21("다시 들으시려면 화면을 위로 밀어주세요! 새로 질문하시려면 화면을 아래로 밀어주세요.");
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
        if(tts!=null) {
            tts.stop();
            tts.shutdown();
            tts = null;
        }
        super.onDestroy();
    }

    @Override
    public void onBackPressed() {
        finish();
        isPrepared = false;
        mediaPlayer.stop();
        isPlaying = false;
        super.onBackPressed();
    }

//    @Override
//    public boolean onTouchEvent(MotionEvent event) {
//        switch (event.getAction()) {
//            case MotionEvent.ACTION_DOWN:
//                curpos = event.getY();
//                Intent intent = new Intent(ListenDoneActivity.this, QuestionActivity.class);
//                startActivity(intent);
//                finish();
//                break;
//            case MotionEvent.ACTION_UP: {
//                float diff = event.getY() - curpos;
//                if(diff < -300) {
//                    texttospeechs();
//                }
//                break;
//            }
//        }
//        return super.onTouchEvent(event);
//    }

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

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        detector.onTouchEvent(event);
        return true;
    }

    class GestureAdapter implements GestureDetector.OnGestureListener{

        @Override
        public boolean onDown(MotionEvent e) {
            return false;
        }

        @Override
        public void onShowPress(MotionEvent e) {

        }

        @Override
        public boolean onSingleTapUp(MotionEvent e) {
            return false;
        }

        @Override
        public boolean onScroll(MotionEvent e1, MotionEvent e2, float distanceX, float distanceY) {
            return false;
        }

        @Override
        public void onLongPress(MotionEvent e) {

        }

        @SuppressLint("WrongConstant")
        @Override
        public boolean onFling(MotionEvent e1, MotionEvent e2, float velocityX, float velocityY) {
            try {
                double distanceX = Math.abs(e1.getX() - e2.getY());
                double distanceY = Math.abs(e1.getY() - e2.getY());

                if (e1.getY() - e2.getY() > SWIPE_MIN_DISTANCE) {
                    texttospeechs();
                } // up to down swipe
                else if (e2.getY() - e1.getY() > SWIPE_MIN_DISTANCE) {
                    Intent intent = new Intent(ListenDoneActivity.this, QuestionAgainActivity.class);
                    intent.putExtra("USERID",userId);
                    intent.putExtra("USERINDEX", userIndexId);
                    startActivity(intent);
                    finish();
                }
            } catch (Exception ex) {
                Log.d("swipe", ex.toString());
            }

            return true;
        }

    }

}
