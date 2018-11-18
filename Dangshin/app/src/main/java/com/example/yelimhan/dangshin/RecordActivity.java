package com.example.yelimhan.dangshin;


import android.Manifest;
import android.app.ProgressDialog;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.content.pm.ResolveInfo;
import android.media.MediaPlayer;
import android.media.MediaRecorder;
import android.net.Uri;
import android.os.Environment;
import android.support.annotation.NonNull;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.FileProvider;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;

import com.google.android.gms.auth.api.signin.internal.Storage;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.OnProgressListener;
import com.google.firebase.storage.StorageMetadata;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;

import java.io.File;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;


public class RecordActivity extends AppCompatActivity {

    MediaPlayer mPlayer = null;
    MediaRecorder mRecorder = null;
    boolean isPlaying = false;
    Button mBtPlay = null;
    boolean isRecording = false;
    Button mBtRecord = null;
    Button mBtUpload = null;
    String mPath = "";
    Uri uri = null;
    public static final int RECORD_AUDIO = 0;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_record);

        mRecorder = new MediaRecorder();
        mBtRecord = (Button)findViewById(R.id.bt_record);
        mBtPlay = (Button)findViewById(R.id.bt_play);
        mBtUpload = (Button)findViewById(R.id.bt_upload);
        mBtRecord.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(isRecording == false){
                    initAudioRecorder();
                    mRecorder.start();

                    isRecording = true;
                    mBtRecord.setText("Stop Recording");
                }else{
                    mRecorder.stop();

                    isRecording = false;
                    mBtRecord.setText("Start Recording");
                }
            }
        });

        mBtPlay.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(isPlaying == false){
                    try{
                        mPlayer.setDataSource(mPath);
                        mPlayer.prepare();
                    }catch(Exception e){
                        e.printStackTrace();
                    }
                    mPlayer.start();

                    isPlaying = true;
                    mBtPlay.setText("Stop Playing");
                }else{
                    mPlayer.stop();

                    isPlaying = false;
                    mBtPlay.setText("Start Playing");
                }
            }
        });

        mBtUpload.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                uploadFile();
            }
        });

        mPlayer = new MediaPlayer();
        mPlayer.setOnCompletionListener(new MediaPlayer.OnCompletionListener() {
            @Override
            public void onCompletion(MediaPlayer mp) {
                isPlaying = false;
                mBtPlay.setText("Start Playing");
            }
        });
    }

    private void uploadFile() {
        if(mPath != null){
            final ProgressDialog progressDialog = new ProgressDialog(this);
            progressDialog.setTitle("업로드 중...");
            progressDialog.show();

            //storage
            FirebaseStorage storage = FirebaseStorage.getInstance();

            //Unique한 파일명을 만들자.
            SimpleDateFormat formatter = new SimpleDateFormat("yyyyMMHH_mmss");
            Date now = new Date();
            String filename = formatter.format(now) + ".aac";
            //storage 주소와 폴더 파일명을 지정해 준다.
            StorageMetadata metadata = new StorageMetadata.Builder()
                    .setContentType("audio/mpeg")
                    .build();

            StorageReference storageRef = storage.getReferenceFromUrl("gs://dangshin-fa136.appspot.com").child("voice/" + filename);
            //올라가거라...
            storageRef.putFile(Uri.parse(mPath), metadata)
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

    void initAudioRecorder(){
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.RECORD_AUDIO) != PackageManager.PERMISSION_GRANTED) {

            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.RECORD_AUDIO},
                    RECORD_AUDIO);
        }

        mRecorder.setAudioSource(MediaRecorder.AudioSource.MIC);
        mRecorder.setOutputFormat(MediaRecorder.OutputFormat.AAC_ADTS);
        mRecorder.setAudioEncoder(MediaRecorder.AudioEncoder.AAC);

        // mPath = "/sdcard/Android/data/com.example.yelimhan.dangshin/"+"test.aac";

        mPath = Environment.getExternalStorageDirectory().getAbsolutePath() + "/record.aac";
        Log.d("file name : ", mPath);

        // File file = new File(mPath);

//        Uri uri = FileProvider.getUriForFile(this, getApplicationContext().getPackageName()+".fileprovider", file);
//        RecordActivity.this.grantUriPermission("com.example.yelimhan.dangshin", uri,
//                Intent.FLAG_GRANT_WRITE_URI_PERMISSION | Intent.FLAG_GRANT_READ_URI_PERMISSION);

        mRecorder.setOutputFile(mPath);
        try{
            mRecorder.prepare();
        } catch(Exception e){
            e.printStackTrace();
        }


    }

}
