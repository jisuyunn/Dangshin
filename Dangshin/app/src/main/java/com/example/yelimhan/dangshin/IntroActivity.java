package com.example.yelimhan.dangshin;

import android.Manifest;
import android.app.ProgressDialog;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.content.pm.ResolveInfo;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.support.annotation.NonNull;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v4.content.FileProvider;
import android.support.v7.app.AppCompatActivity;
import android.text.format.DateFormat;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.Toast;

import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.OnProgressListener;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;

import java.io.File;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;


public class IntroActivity extends AppCompatActivity {

    private static final String TAG = "IntroActivity";
    String[] REQUIRED_PERMISSIONS  = {Manifest.permission.CAMERA,
            Manifest.permission.WRITE_EXTERNAL_STORAGE};

    private Button btChoose;
    private Button btUpload;
    private Button btCamera;
    private Button btCamera2;
    private ImageView ivPreview;

    private Uri filePath;
    private Uri outputFileUri;

    private String imgPath = "";
    String dir;
    Uri photoURI;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_intro);

        permissionCheck();


        btChoose = (Button) findViewById(R.id.bt_choose);
        btUpload = (Button) findViewById(R.id.bt_upload);
        btCamera = (Button) findViewById(R.id.bt_camera);
        btCamera2 = (Button) findViewById(R.id.bt_customcamera);

        ivPreview = (ImageView) findViewById(R.id.iv_preview);

        //버튼 클릭 이벤트
        btChoose.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                //이미지를 선택
                Intent intent = new Intent();
                intent.setType("image/*");
                intent.setAction(Intent.ACTION_GET_CONTENT);
                startActivityForResult(Intent.createChooser(intent, "이미지를 선택하세요."), 0);
            }
        });

        btUpload.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                //업로드
                uploadFile(filePath);
            }
        });

        btCamera.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                goCamera();

            }
        });
        btCamera2.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(IntroActivity.this, CustomCameraActivity.class);
                startActivityForResult(intent, 2222);
            }
        });
    }

    public void permissionCheck(){
        //권한이 부여되어 있는지 확인
        int cameraPermissionCheck = ContextCompat.checkSelfPermission(this, Manifest.permission.CAMERA);
        int writePermissionCheck = ContextCompat.checkSelfPermission(this, Manifest.permission.WRITE_EXTERNAL_STORAGE);

        if(cameraPermissionCheck == PackageManager.PERMISSION_GRANTED
                && writePermissionCheck == PackageManager.PERMISSION_GRANTED){
            Toast.makeText(getApplicationContext(), "권한 있음", Toast.LENGTH_SHORT).show();
        }else{
            Toast.makeText(getApplicationContext(), "권한 없음", Toast.LENGTH_SHORT).show();
            if (ActivityCompat.shouldShowRequestPermissionRationale(this, REQUIRED_PERMISSIONS[0])
                    || ActivityCompat.shouldShowRequestPermissionRationale(this, REQUIRED_PERMISSIONS[1])) {

                Toast.makeText(getApplicationContext(), "카메라와 저장공간 권한이 필요합니다", Toast.LENGTH_SHORT).show();
                ActivityCompat.requestPermissions( IntroActivity.this, REQUIRED_PERMISSIONS,1000);
            }else{
                ActivityCompat.requestPermissions(this, REQUIRED_PERMISSIONS, 1000);

                //권한설정 dialog에서 거부를 누르면
                //ActivityCompat.shouldShowRequestPermissionRationale 메소드의 반환값이 true가 된다.
                //단, 사용자가 "다시 묻지 않음"을 체크한 경우
                //거부하더라도 false를 반환하여, 직접 사용자가 권한을 부여하지 않는 이상, 권한을 요청할 수 없게 된다.
                if(ActivityCompat.shouldShowRequestPermissionRationale(this, Manifest.permission.CAMERA)){
                    //이곳에 권한이 왜 필요한지 설명하는 Toast나 dialog를 띄워준 후, 다시 권한을 요청한다.
                    Toast.makeText(getApplicationContext(), "카메라와 저장공간 권한이 필요합니다", Toast.LENGTH_SHORT).show();
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
                        || ActivityCompat.shouldShowRequestPermissionRationale(this, REQUIRED_PERMISSIONS[1])) {

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
        //request코드가 0이고 OK를 선택했고 data에 뭔가가 들어 있다면
        if(requestCode == 0 && resultCode == RESULT_OK){
            filePath = data.getData();
            Log.d(TAG, "uri:" + String.valueOf(filePath));
            try {
                //Uri 파일을 Bitmap으로 만들어서 ImageView에 집어 넣는다.
                Bitmap bitmap = MediaStore.Images.Media.getBitmap(getContentResolver(), filePath);
                ivPreview.setImageBitmap(bitmap);
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        if (requestCode == 1 && resultCode == RESULT_OK) {
            filePath = photoURI;
        }
        if (requestCode == 2222 && resultCode == RESULT_OK){
            String path = data.getStringExtra("PATH");
            Log.d("testt", path);
            filePath = Uri.fromFile(new File(path));
        }
    }

    private Uri getFileUri() {
        // 저장되는 사진 경로
        String dir_path = "/sdcard/Android/data/com.example.yelimhan.dangshin/";
        File dir = new File(dir_path);
        if(!dir.exists()){
            dir.mkdirs();
        }

        // 저장되는 사진 이름
        File file = new File( dir, DateFormat.format("yyyy-MM-dd_hhmmss", new Date()).toString()+ ".jpg" );
        imgPath = file.getAbsolutePath();
        return FileProvider.getUriForFile( this, getApplicationContext().getPackageName() + ".fileprovider", file );
    }

    private void goCamera() {
        Intent intent = new Intent( MediaStore.ACTION_IMAGE_CAPTURE );
        List<ResolveInfo> resolvedIntentActivities = IntroActivity.this.getPackageManager().queryIntentActivities(intent, PackageManager.MATCH_DEFAULT_ONLY);

        photoURI = getFileUri();
        for (ResolveInfo resolvedIntentInfo : resolvedIntentActivities) {

            String packageName = resolvedIntentInfo.activityInfo.packageName;

            IntroActivity.this.grantUriPermission(packageName,      // 패키지이름
                    photoURI, Intent.FLAG_GRANT_WRITE_URI_PERMISSION | Intent.FLAG_GRANT_READ_URI_PERMISSION);
        }
        intent.putExtra( MediaStore.EXTRA_OUTPUT, photoURI );
        startActivityForResult( intent, 1 );


    }

    //upload the file
    private void uploadFile(Uri fileUri) {
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
}
