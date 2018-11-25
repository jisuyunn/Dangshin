package com.example.yelimhan.dangshin;

import android.Manifest;
import android.annotation.SuppressLint;
import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.content.pm.ResolveInfo;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.media.MediaRecorder;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.provider.MediaStore;
import android.speech.RecognitionListener;
import android.speech.RecognizerIntent;
import android.speech.SpeechRecognizer;
import android.speech.tts.TextToSpeech;
import android.speech.tts.UtteranceProgressListener;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v4.content.FileProvider;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.text.Layout;
import android.text.format.DateFormat;
import android.util.Log;
import android.view.MotionEvent;
import android.view.GestureDetector;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.auth.api.Auth;
import com.google.android.gms.auth.api.signin.GoogleSignInOptions;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.common.api.ResultCallback;
import com.google.android.gms.common.api.Status;
import com.google.android.gms.tasks.OnFailureListener;
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
import com.google.firebase.storage.OnProgressListener;
import com.google.firebase.storage.StorageMetadata;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;

import org.json.JSONObject;

import java.io.File;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;

import static com.example.yelimhan.dangshin.R.*;
import static com.example.yelimhan.dangshin.R.drawable.*;
import static com.example.yelimhan.dangshin.R.id.question_layout;


// 질문하기 (Blind)
public class QuestionActivity extends AppCompatActivity implements GoogleApiClient.OnConnectionFailedListener {

    private FirebaseAuth mAuth;
    private GoogleApiClient mGoogleApiClient;
    private DatabaseReference mDatabase;
    public Button bt;
    public TextView textView;
    public ImageView imageView;
    public LinearLayout question_layout;
    private static final int SWIPE_MIN_DISTANCE = 120;
    GestureDetector detector;
    String userId = "";
    String userq_key = "";
    String volIndexId = "";
    String newQuestion = "";

    //사진
    Uri photoURI;
    private Uri filePath;
    private String imgPath = "";
    String[] REQUIRED_PERMISSIONS  = {Manifest.permission.CAMERA,
            Manifest.permission.WRITE_EXTERNAL_STORAGE, Manifest.permission.RECORD_AUDIO};
    private int stage;
    private String storagePath = "";
    //tts, stt
    TextToSpeech tts;
    SpeechRecognizer mRecognizer;
    //음성녹음
    MediaRecorder mRecorder = null;
    boolean isRecording = false;
    String mPath = "";
    Uri uri = null;
    private String storageVPath = "";
    public String userIndexId = "";

    public static final int RECORD_AUDIO = 0;
    public static final int CUSTOM_CAMERA = 1000;
    private boolean urgent_flag = false;

    @SuppressLint("ClickableViewAccessibility")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(layout.activity_question);

        Toast.makeText(this, "  Blind - 질문하기 (QuestionActivity",Toast.LENGTH_SHORT).show();
        permissionCheck();

        Intent it = getIntent();
        userIndexId = it.getStringExtra("USERINDEX");
        Log.d("testt", "QA userindexid : "+userIndexId);
        mAuth = FirebaseAuth.getInstance();
        GoogleSignInOptions gso = new GoogleSignInOptions.Builder
                (GoogleSignInOptions.DEFAULT_SIGN_IN)
                .requestIdToken(getString(string.default_web_client_id))
                .requestEmail()
                .build();
        mGoogleApiClient = new GoogleApiClient.Builder(this)
                .enableAutoManage(this, this )
                .addApi(Auth.GOOGLE_SIGN_IN_API, gso)
                .build();
        bt = (Button) findViewById(id.logout);
        textView = findViewById(id.textView);
        imageView = findViewById(id.imageView);
        question_layout = findViewById(id.question_layout);
        stage = 0;

        tts = new TextToSpeech(QuestionActivity.this, new TextToSpeech.OnInitListener() {
            @Override
            public void onInit(int status) {
                if(status != android.speech.tts.TextToSpeech.ERROR) {
                    tts.setLanguage(Locale.KOREAN);
                }

                String speach = "등록된 질문이 없네요. 위로 화면을 밀면 바로 질문할 수 있어요.";
                tts.speak(speach, TextToSpeech.QUEUE_FLUSH, null);

                if(status == TextToSpeech.SUCCESS) {
                    tts.setOnUtteranceProgressListener(new UtteranceProgressListener() {
                        @Override
                        public void onStart(String utteranceId) {

                        }

                        @Override
                        public void onDone(String Id) {

                        }

                        @Override
                        public void onError(String utteranceId) {

                        }
                    });
                }
            }
        });


        mRecorder = new MediaRecorder();

        // 로그아웃 버튼 클릭 이벤트 > dialog 예/아니오
        Button logout_btn_google = (Button) findViewById(id.logout);
        logout_btn_google.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(final View view) {
                Log.v("알림", "구글 LOGOUT");
                AlertDialog.Builder alt_bld = new AlertDialog.Builder(view.getContext());
                alt_bld.setMessage("로그아웃 하시겠습니까?").setCancelable(false)
                        .setPositiveButton("네",
                                new DialogInterface.OnClickListener() {
                                    public void onClick(DialogInterface dialog, int id) {
                                        // 네 클릭
                                        // 로그아웃 함수 call
                                        signOut();
                                    }
                                }).setNegativeButton("아니오",
                        new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialog, int id) {
                                // 아니오 클릭. dialog 닫기.
                                dialog.cancel();
                            }
                        });
                AlertDialog alert = alt_bld.create();
                alert.setTitle("로그아웃");

                // 대화창 배경 색 설정
                alert.getWindow().setBackgroundDrawable(new ColorDrawable(Color.argb(255,62,79,92)));
                alert.show();
            }
        });

        // 현재 접속중인 사용자의 정보를 받아옴. 없으면 null
        final FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
        final DatabaseReference table = FirebaseDatabase.getInstance().getReference("UserInfo");
        userId = user.getEmail();

        detector = new GestureDetector(this, new GestureAdapter());
    }


    // 로그아웃
    public void signOut() {
        mGoogleApiClient.connect();
        mGoogleApiClient.registerConnectionCallbacks(new GoogleApiClient.ConnectionCallbacks() {
            @Override
            public void onConnected(@Nullable Bundle bundle) {
                mAuth.signOut();
                if (mGoogleApiClient.isConnected()) {
                    Auth.GoogleSignInApi.signOut(mGoogleApiClient).setResultCallback(new ResultCallback<Status>() {
                        @Override
                        public void onResult(@NonNull Status status) {
                            if (status.isSuccess()) {
                                Log.v("알림", "로그아웃 성공");
                                setResult(1);
                            } else {
                                setResult(0);
                            }
                            finish();
                        }
                    });
                }
            }
            @Override
            public void onConnectionSuspended(int i) {
                Log.v("알림", "Google API Client Connection Suspended");
                setResult(-1);
                finish();
            }
        });
    }

    @Override
    public void onConnectionFailed(@NonNull ConnectionResult connectionResult) {
        Log.v("알림", "onConnectionFailed");
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
            if(stage == 1){
                mRecorder.stop();
                //mRecognizer.stopListening();
                isRecording = false;
                uploadVoiceFile();

                stage = 2;
                question_layout.setBackgroundResource(R.drawable.gradient5);
                textView.setText("질문이 긴급하신가요?\n 그렇다면 화면을 아래에서로\n위로 올려주세요.\n\n긴급이 아니라면 화면을\n위에서 아래로 내려주세요.");
                String speech = "질문이 긴급하신가요?\n 그렇다면 화면을 아래에서 위로 올려주세요\n\n긴급이 아니라면 화면을 위에서 아래로 내려주세요";
                tts.speak(speech, TextToSpeech.QUEUE_FLUSH, null);
            }
        }

        @SuppressLint("WrongConstant")
        @Override
        public boolean onFling(MotionEvent e1, MotionEvent e2, float velocityX, float velocityY) {
            try {
                double distanceX = Math.abs(e1.getX() - e2.getY());
                double distanceY = Math.abs(e1.getY() - e2.getY());
                //if (distanceX > SWIPE_MAX_OFF_PATH || distanceY > SWIPE_MAX_OFF_PATH) {
//                    Toast.makeText(getApplicationContext(),
//                            Double.toString(distanceX) + "," + Double.toString(distanceY),
//                            Toast.LENGTH_SHORT).show();
                //    return true;
                //}

                // right to left swipe
//                if (e1.getX() - e2.getX() > SWIPE_MIN_DISTANCE && Math.abs(velocityX) > SWIPE_THRESHOLD_VELOCITY) {
//                    Toast.makeText(getApplicationContext(), "Left swipe", Toast.LENGTH_SHORT).show();
//                } // left to right swipe
//                else if (e2.getX() - e1.getX() > SWIPE_MIN_DISTANCE && Math.abs(velocityX) > SWIPE_THRESHOLD_VELOCITY) {
//                    Toast.makeText(getApplicationContext(), "Right Swipe", Toast.LENGTH_SHORT).show();
//                } //down to up swipe
                if (e1.getY() - e2.getY() > SWIPE_MIN_DISTANCE && stage == 0) {
                    stage = 1;
                    Toast.makeText(getApplicationContext(), "Swipe UP", Toast.LENGTH_SHORT).show();
                    question_layout.setBackgroundResource(R.drawable.gradient2);
                    String totalSpeak = "먼저 사진을 찍을게요\n\n알고 싶은 물체나 내용을 평평한 곳에 놓아주세요.";
                    textView.setText("먼저 사진을 찍을게요\n알고 싶은 물체나 내용을\n평평한 곳에 놓아주세요.");
                    imageView.setVisibility(View.INVISIBLE);
                    tts.setPitch(1.0f);
                    tts.setSpeechRate(1.0f);
                    tts.speak(totalSpeak, TextToSpeech.QUEUE_FLUSH, null);
                    final Handler delayHandler = new Handler();
                    delayHandler.postDelayed(new Runnable() {
                        @Override
                        public void run() {
                            //goCamera();
                            Intent intent = new Intent(QuestionActivity.this, CustomCameraActivity.class);
                            startActivityForResult(intent, 2222);
                        }
                    }, 9000);

                } // up to down swipe
                else if (e2.getY() - e1.getY() > SWIPE_MIN_DISTANCE && stage == 2) {
                    question_layout.setBackgroundColor(Color.rgb(225, 191, 224));
                    textView.setText("질문 등록이 완료되었습니다.\n답변이 오면 알려드릴게요!");
                    String speech = "질문 등록이 완료되었습니다.\n\n답변이 오면 알려드릴게요!";
                    tts.speak(speech, TextToSpeech.QUEUE_FLUSH, null);
                    stage = 3;
                    urgent_flag = false;

                    // 데이터베이스에 질문 추가
                    mDatabase = FirebaseDatabase.getInstance().getReference("QuestionInfo");
                    newQuestion = mDatabase.push().getKey();
                    QuestionInfo questionInfo = new QuestionInfo(newQuestion, storagePath, storageVPath, userId, "stt", urgent_flag);
                    mDatabase.child(newQuestion).setValue(questionInfo);
                    DatabaseReference userR = FirebaseDatabase.getInstance().getReference("UserInfo");
                    userR.child(userIndexId).child("q_key").setValue(newQuestion);
                    userR.child(userIndexId).child("u_haveQuestion").setValue(1);
                    Log.d("지금", userIndexId);

                }
                else if(e1.getY() - e2.getY() > SWIPE_MIN_DISTANCE && stage == 2){
                    Toast.makeText(getApplicationContext(), "Swipe Down", Toast.LENGTH_SHORT).show();
                    question_layout.setBackgroundColor(Color.rgb(225, 191, 224));
                    textView.setText("긴급 질문 등록이\n완료되었습니다.\n\n답변이 오면 알려드릴게요!");
                    String speech = "긴급 질문 등록이 완료되었습니다.\n\n답변이 오면 알려드릴게요!";
                    tts.speak(speech, TextToSpeech.QUEUE_FLUSH, null);
                    stage = 3;
                    urgent_flag = true;

                    // 데이터베이스에 질문 추가
                    mDatabase = FirebaseDatabase.getInstance().getReference("QuestionInfo");
                    String newQuestion = mDatabase.push().getKey();
                    QuestionInfo questionInfo = new QuestionInfo(newQuestion, storagePath, storageVPath, userId, "stt", urgent_flag);
                    mDatabase.child(newQuestion).setValue(questionInfo);
                    DatabaseReference userR = FirebaseDatabase.getInstance().getReference("UserInfo");
                    userR.child(userIndexId).child("q_key").setValue(newQuestion);
                    userR.child(userIndexId).child("u_haveQuestion").setValue(1);
                    Log.d("testt", "urgent : "+String.valueOf(urgent_flag));
                    sentUrgent();




                }
            } catch (Exception ex) {
                Log.d("swipe", ex.toString());
            }

            return true;
        }

    }

    public void sentUrgent() {
        // 1 긴급 질문 저장
        // 2 온라인인 봉사자 검색해서 한명 뽑아냄
        // 3 푸시메세지 보냄 (해당봉사자 info에 질문id 추가)
        final String FCM_MESSAGE_URL = "https://fcm.googleapis.com/fcm/send";
        final String SERVER_KEY = "AAAAQjYIgGo:APA91bFDx0BUk2pa77EAAmeAIak73owBnfZbZitHV3G3e7_4_wCSpkv2yqCga0fk03jJ8eTvyxmNv0Oqze0FuzwFDYm9vE_zSrBiLt-tw5RTDX6W6I79pGnU91rTdddbcwaU5qOgIEA-";

        // 2 온라인인 봉사자 검색해서 한명 뽑아냄
        final DatabaseReference table = FirebaseDatabase.getInstance().getReference("UserInfo");
        Query query = table.orderByChild("u_online").equalTo(true);
        query.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                for (DataSnapshot snapshot : dataSnapshot.getChildren()) {
                    UserInfo userInfo = snapshot.getValue(UserInfo.class);          // 온라인 봉사자 랜덤으로 가져오려면 여기 바꿈
                    Log.d("testt", "selected online volunteer : "+userInfo.u_googleId);
                    volIndexId = snapshot.getKey().toString();

                    final String usertoken = userInfo.u_token;
                    new Thread(new Runnable() {
                        @Override
                        public void run() {
                            try {
                                // FMC 메시지 생성 start
                                JSONObject root = new JSONObject();
                                JSONObject notification = new JSONObject();
                                notification.put("body", "눌러서 긴급한 질문에 답해주세요!");
                                notification.put("title", getString(R.string.app_name));
                                root.put("data", notification);
                                root.put("to", usertoken);

                                //root.put("click_action", "URGENT_PUSH_ACTIVITY");
                                // FMC 메시지 생성 end

                                Log.d("testt", "vid: "+volIndexId +" , qid: "+ newQuestion);
                                URL Url = new URL(FCM_MESSAGE_URL);
                                HttpURLConnection conn = (HttpURLConnection) Url.openConnection();
                                conn.setRequestMethod("POST");
                                conn.setDoOutput(true);
                                conn.setDoInput(true);
                                conn.addRequestProperty("Authorization", "key=" + SERVER_KEY);
                                conn.setRequestProperty("Accept", "application/json");
                                conn.setRequestProperty("Content-type", "application/json");
                                OutputStream os = conn.getOutputStream();
                                os.write(root.toString().getBytes("utf-8"));
                                os.flush();
                                conn.getResponseCode();
                                table.child(volIndexId).child("urgent_qid").setValue(newQuestion);
                                QuestionActivity.this.finish();
                            } catch (Exception e) {
                                e.printStackTrace();
                            }
                        }
                    }).start();
                    break;
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
            }
        });


    }

    public void permissionCheck(){
        //권한이 부여되어 있는지 확인
        int cameraPermissionCheck = ContextCompat.checkSelfPermission(this, Manifest.permission.CAMERA);
        int writePermissionCheck = ContextCompat.checkSelfPermission(this, Manifest.permission.WRITE_EXTERNAL_STORAGE);
        int recordPermissionCheck = ContextCompat.checkSelfPermission(this, Manifest.permission.RECORD_AUDIO);

        if(cameraPermissionCheck == PackageManager.PERMISSION_GRANTED
                && writePermissionCheck == PackageManager.PERMISSION_GRANTED
                && recordPermissionCheck == PackageManager.PERMISSION_GRANTED){
            Toast.makeText(getApplicationContext(), "권한 있음", Toast.LENGTH_SHORT).show();
        }else{
            Toast.makeText(getApplicationContext(), "권한 없음", Toast.LENGTH_SHORT).show();
            if (ActivityCompat.shouldShowRequestPermissionRationale(this, REQUIRED_PERMISSIONS[0])
                    || ActivityCompat.shouldShowRequestPermissionRationale(this, REQUIRED_PERMISSIONS[1])
                     ||  ActivityCompat.shouldShowRequestPermissionRationale(this, REQUIRED_PERMISSIONS[2])) {

                Toast.makeText(getApplicationContext(), "카메라, 음성녹음, 저장공간 권한이 필요합니다", Toast.LENGTH_SHORT).show();
                ActivityCompat.requestPermissions( QuestionActivity.this, REQUIRED_PERMISSIONS,1000);
            }else{
                ActivityCompat.requestPermissions(this, REQUIRED_PERMISSIONS, 1000);

                //권한설정 dialog에서 거부를 누르면
                //ActivityCompat.shouldShowRequestPermissionRationale 메소드의 반환값이 true가 된다.
                //단, 사용자가 "다시 묻지 않음"을 체크한 경우
                //거부하더라도 false를 반환하여, 직접 사용자가 권한을 부여하지 않는 이상, 권한을 요청할 수 없게 된다.
                if(ActivityCompat.shouldShowRequestPermissionRationale(this, Manifest.permission.CAMERA)){
                    //이곳에 권한이 왜 필요한지 설명하는 Toast나 dialog를 띄워준 후, 다시 권한을 요청한다.
                    Toast.makeText(getApplicationContext(), "카메라, 음성녹음, 저장공간 권한이 필요합니다", Toast.LENGTH_SHORT).show();
                }
            }
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == 1000 && grantResults.length == REQUIRED_PERMISSIONS.length) {
            boolean check_result = true;

            for (int result : grantResults) {
                if (result != PackageManager.PERMISSION_GRANTED) {
                    check_result = false;
                    break;
                }
            }

            if ( check_result ) {

            }
            else {

                if (ActivityCompat.shouldShowRequestPermissionRationale(this, REQUIRED_PERMISSIONS[0])
                        || ActivityCompat.shouldShowRequestPermissionRationale(this, REQUIRED_PERMISSIONS[1])
                        || ActivityCompat.shouldShowRequestPermissionRationale(this, REQUIRED_PERMISSIONS[2]) ) {

                    Toast.makeText(this, "퍼미션이 거부되었습니다.",Toast.LENGTH_SHORT).show();
                    finish();

                }else {

                    Toast.makeText(this, "설정(앱 정보)에서 퍼미션을 허용해야 합니다. ",Toast.LENGTH_SHORT).show();
                    finish();
                }
            }

        }
    }

    //결과 처리
    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if(requestCode == RECORD_AUDIO && resultCode == RESULT_OK){
            ArrayList<String> speechList = data.getStringArrayListExtra(RecognizerIntent.EXTRA_RESULTS);
            String result = speechList.get(0);
            Toast.makeText(this, result, Toast.LENGTH_SHORT).show();
//            Uri audiouri = data.getData();
//            ContentResolver contentResolver = getContentResolver();
//            InputStream inputStream = null;
//            OutputStream outputStream = null;
//
//            try{
//                inputStream = contentResolver.openInputStream(audiouri);
//
//                File targetFile = new File(Environment.getExternalStorageDirectory().getAbsolutePath()+"/dangshin");
//                if(!targetFile.exists()){
//                    targetFile.mkdirs();
//                }
//                String mfileName = new SimpleDateFormat("yyyyMMdd_HHmmss").format(new Date()) + ".mp4";
//                mPath = targetFile + "/" + mfileName;
//                outputStream = new FileOutputStream(targetFile + "/" + mfileName);
//                int read = 0;
//                byte[] bytes = new byte[1024];
//
//                while((read = inputStream.read(bytes)) != -1)
//                    outputStream.write(bytes, 0, read);
//            }catch(IOException e){
//                e.printStackTrace();
//            }finally{
//                if(inputStream != null){
//                    try{
//                        inputStream.close();
//                    }catch(IOException e){
//                        e.printStackTrace();
//                    }
//                }
//
//                if(outputStream != null){
//                    try{
//                        outputStream.close();
//                    }catch(IOException e){
//                        e.printStackTrace();
//                    }
//                }
//            }
        }

        // CustomCamera에서 돌아옴
        if (requestCode == 2222 && resultCode == RESULT_OK){
            String path = data.getStringExtra("PATH");
            Log.d("testt", path);
            filePath = Uri.fromFile(new File(path));
            uploadPhotoFile(filePath);
            question_layout.setBackgroundResource(R.drawable.gradient4);
            String totalSpeak = "더 정확한 답변을 받기 위해 음성을 녹음할게요.\n\n알고싶은 내용을 질문해주세요.\n\n음성 녹음을 끝내고싶으면 화면을 길게 눌러주세요.\n3 2 1";
            textView.setText("더 정확한 답변을 받기 위해\n음성을 녹음할게요.\n알고싶은 내용을 질문해주세요.\n\n음성 녹음을 끝내고싶으면\n화면을 길게 눌러주세요.\n3 2 1");
            tts.speak(totalSpeak, TextToSpeech.QUEUE_FLUSH, null);
            final Handler delayHandler = new Handler();
            delayHandler.postDelayed(new Runnable() {
                @Override
                public void run() {
//                    if(isRecording == false){
//                        if (ActivityCompat.checkSelfPermission(QuestionActivity.this, Manifest.permission.RECORD_AUDIO) != PackageManager.PERMISSION_GRANTED) {
//
//                            ActivityCompat.requestPermissions(QuestionActivity.this, new String[]{Manifest.permission.RECORD_AUDIO},
//                                    RECORD_AUDIO);
//                        }
//                    }
                    initAudioRecorder();
//                    Intent intent = new Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH);
//                    intent.putExtra(RecognizerIntent.EXTRA_CALLING_PACKAGE, getPackageName());
//                    intent.putExtra(RecognizerIntent.EXTRA_LANGUAGE, "ko-KR");
//                    mRecognizer = SpeechRecognizer.createSpeechRecognizer(QuestionActivity.this);
//                    mRecognizer.setRecognitionListener(listener);
//                    mRecognizer.startListening(intent);
                    mRecorder.start();
                    //startActivityForResult(intent, RECORD_AUDIO);
                    isRecording = true;
                }
            }, 12000);
        }
    }

    //upload the file
    private void uploadPhotoFile(Uri fileUri) {
        //업로드할 파일이 있으면 수행
        if (fileUri != null) {
            //업로드 진행 Dialog 보이기
            final ProgressDialog progressDialog = new ProgressDialog(this);
            progressDialog.setTitle("업로드중...");
            progressDialog.show();

            //storage
            FirebaseStorage storage = FirebaseStorage.getInstance();

            //Unique한 파일명을 만들자.
            SimpleDateFormat formatter = new SimpleDateFormat("yyyyMMHH_mmss");
            Date now = new Date();
            String filename = formatter.format(now) + ".png";
            storagePath = "images/" + filename;
            //storage 주소와 폴더 파일명을 지정해 준다.
            StorageReference storageRef = storage.getReferenceFromUrl("gs://dangshin-fa136.appspot.com").child("images/" + filename);
            //올라가거라...
            storageRef.putFile(fileUri)
                    //성공시
                    .addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
                        @Override
                        public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {
                            progressDialog.dismiss(); //업로드 진행 Dialog 상자 닫기
                            Toast.makeText(getApplicationContext(), "업로드 완료!", Toast.LENGTH_SHORT).show();
                        }
                    })
                    //실패시
                    .addOnFailureListener(new OnFailureListener() {
                        @Override
                        public void onFailure(@NonNull Exception e) {
                            progressDialog.dismiss();
                            Toast.makeText(getApplicationContext(), "업로드 실패!", Toast.LENGTH_SHORT).show();
                        }
                    })
                    //진행중
                    .addOnProgressListener(new OnProgressListener<UploadTask.TaskSnapshot>() {
                        @Override
                        public void onProgress(UploadTask.TaskSnapshot taskSnapshot) {
                            @SuppressWarnings("VisibleForTests") //이걸 넣어 줘야 아랫줄에 에러가 사라진다. 넌 누구냐?
                                    double progress = (100 * taskSnapshot.getBytesTransferred()) /  taskSnapshot.getTotalByteCount();
                            //dialog에 진행률을 퍼센트로 출력해 준다
                            progressDialog.setMessage("Uploaded " + ((int) progress) + "% ...");
                        }
                    });
        } else {
            Toast.makeText(getApplicationContext(), "파일을 먼저 선택하세요.", Toast.LENGTH_SHORT).show();
        }
    }

    void initAudioRecorder(){

        mRecorder.setAudioSource(MediaRecorder.AudioSource.MIC);
        mRecorder.setOutputFormat(MediaRecorder.OutputFormat.MPEG_4);
        mRecorder.setAudioEncoder(MediaRecorder.AudioEncoder.AAC);

        mPath = Environment.getExternalStorageDirectory().getAbsolutePath() + "/record.mp4";

        Log.d("file path : ", mPath);
        mRecorder.setOutputFile(mPath);
        try {
            mRecorder.prepare();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void uploadVoiceFile() {
        if(mPath != null){
            final ProgressDialog progressDialog = new ProgressDialog(this);
            progressDialog.setTitle("업로드 중...");
            progressDialog.show();

            //storage
            FirebaseStorage storage = FirebaseStorage.getInstance();

            //Unique한 파일명을 만들자.
            SimpleDateFormat formatter = new SimpleDateFormat("yyyyMMHH_mmss");
            Date now = new Date();
            String filename = formatter.format(now) + ".mp4";
            storageVPath = "voice/" + filename;
            //storage 주소와 폴더 파일명을 지정해 준다.
            StorageMetadata metadata = new StorageMetadata.Builder()
                    .setContentType("audio/mp4")
                    .build();

            Uri uri = Uri.fromFile(new File(mPath));
            StorageReference storageRef = storage.getReferenceFromUrl("gs://dangshin-fa136.appspot.com").child("voice/" + filename);
            //올라가거라...
            storageRef.putFile(uri, metadata)
                    //성공시
                    .addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
                        @Override
                        public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {
                            progressDialog.dismiss(); //업로드 진행 Dialog 상자 닫기
                            Toast.makeText(getApplicationContext(), "업로드 완료!", Toast.LENGTH_SHORT).show();
                        }
                    })
                    //실패시
                    .addOnFailureListener(new OnFailureListener() {
                        @Override
                        public void onFailure(@NonNull Exception e) {
                            progressDialog.dismiss();
                            Toast.makeText(getApplicationContext(), "업로드 실패!", Toast.LENGTH_SHORT).show();
                            Log.d("error", e.toString());
                        }
                    })
                    //진행중
                    .addOnProgressListener(new OnProgressListener<UploadTask.TaskSnapshot>() {
                        @Override
                        public void onProgress(UploadTask.TaskSnapshot taskSnapshot) {
                            @SuppressWarnings("VisibleForTests") //이걸 넣어 줘야 아랫줄에 에러가 사라진다. 넌 누구냐?
                                    double progress = (100 * taskSnapshot.getBytesTransferred()) /  taskSnapshot.getTotalByteCount();
                            //dialog에 진행률을 퍼센트로 출력해 준다
                            progressDialog.setMessage("Uploaded " + ((int) progress) + "% ...");
                        }
                    });
        } else {
            Toast.makeText(getApplicationContext(), "파일을 먼저 선택하세요.", Toast.LENGTH_SHORT).show();
        }
    }

    private RecognitionListener listener = new RecognitionListener() {
        @Override
        public void onReadyForSpeech(Bundle params) {

        }

        @Override
        public void onBeginningOfSpeech() {

        }

        @Override
        public void onRmsChanged(float rmsdB) {

        }

        @Override
        public void onBufferReceived(byte[] buffer) {

        }

        @Override
        public void onEndOfSpeech() {

        }

        @Override
        public void onError(int error) {
            Toast.makeText(QuestionActivity.this, String.valueOf(error), Toast.LENGTH_SHORT).show();
            }

        @Override
        public void onResults(Bundle results) {
            String key = "";
            key = SpeechRecognizer.RESULTS_RECOGNITION;
            ArrayList<String> mResult = results.getStringArrayList(key);

            String[] rs = new String[mResult.size()];
            mResult.toArray(rs);

            Toast.makeText(QuestionActivity.this, rs[0], Toast.LENGTH_SHORT).show();
        }

        @Override
        public void onPartialResults(Bundle partialResults) {

        }

        @Override
        public void onEvent(int eventType, Bundle params) {

        }
    };

}
