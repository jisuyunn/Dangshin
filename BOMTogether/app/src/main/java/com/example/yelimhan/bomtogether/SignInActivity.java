package com.example.yelimhan.bomtogether;

import android.annotation.TargetApi;
import android.content.Intent;
import android.os.Build;
import android.os.Bundle;
import android.speech.tts.TextToSpeech;
import android.speech.tts.UtteranceProgressListener;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.view.MotionEvent;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.iid.FirebaseInstanceId;

import java.util.HashMap;
import java.util.Locale;

public class SignInActivity extends AppCompatActivity{

    String userID="";
    FirebaseDatabase db;
    DatabaseReference table;
    private float curpos;
    private TextToSpeech tts;
    private String text;
    private String utteranceId;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_sign_in);


        Button btnVol = (Button)findViewById(R.id.btnVolunteer);

        final FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
        userID = user.getEmail();


        db = FirebaseDatabase.getInstance();
        table = db.getReference("UserInfo");

        btnVol.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //Toast.makeText(SignInActivity.this,"blind click",Toast.LENGTH_SHORT).show();
                saveUserToDatabase("Volunteer");
            }
        });
        text = "시각장애인이라면 위로 스와이프 해주세요.";
        texttospeechs();


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


    @NonNull
    private Boolean saveUserToDatabase(String pos){

        // 파이어베이스에 데이터 넣는 부분(랜덤 키로 push)
        UserInfo uif = new UserInfo(userID,pos,false, 0, FirebaseInstanceId.getInstance().getToken(), "", "");
        DatabaseReference newUser = table.push();
        Toast.makeText(this, userID, Toast.LENGTH_SHORT).show();
        newUser.setValue(uif);
        Intent intent = new Intent(SignInActivity.this,MainActivity.class);
        startActivity(intent);
        finish();
        return true;
    }
    @Override
    public boolean onTouchEvent(MotionEvent event) {
        switch (event.getAction()) {
            case MotionEvent.ACTION_DOWN:
                curpos = event.getY();
                break;
            case MotionEvent.ACTION_UP: {
                float diff = event.getY() - curpos;
                if(diff < -200) {
                    //texttospeechs();
                   // Toast.makeText(getApplicationContext(),"up",Toast.LENGTH_SHORT).show();
                   saveUserToDatabase("Blind");
                }
                else if(diff>200){
                    //Toast.makeText(getApplicationContext(),"down",Toast.LENGTH_SHORT).show();
                   // saveUserToDatabase("Volunteer");
                }
                break;
            }
        }
        return super.onTouchEvent(event);
    }
}
