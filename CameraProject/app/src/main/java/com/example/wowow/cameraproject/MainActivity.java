package com.example.wowow.cameraproject;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.content.pm.ResolveInfo;
import android.net.Uri;
import android.provider.MediaStore;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v4.content.FileProvider;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.text.format.DateFormat;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.Toast;

import java.io.File;
import java.util.Date;
import java.util.List;

public class MainActivity extends AppCompatActivity {
    private ImageView img;
    private String imgPath = "";
    static final int MY_PERMISSIONS_REQUEST_CAMERA  = 1234;

    static final int MY_PERMISSIONS_REQUEST_WRITE  = 2345;
    private Button btCamera;

    Uri photoURI;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        btCamera = (Button) findViewById(R.id.bt_camera);

        permissionCheck();
        btCamera.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                showCamera();

            }
        });
    }

    private Uri getFileUri() {
        String dir_path = "/sdcard/Android/data/com.example.wowow.cameraproject/";
        File dir = new File(dir_path);
        if(!dir.exists()){
            dir.mkdirs();
        }

        File file = new File( dir, DateFormat.format("yyyy-MM-dd_hhmmss", new Date()).toString()+ ".jpg" );
        imgPath = file.getAbsolutePath();
        return FileProvider.getUriForFile( this, getApplicationContext().getPackageName() + ".fileprovider", file );
    }

    private void showCamera() {
        Intent itt = new Intent( MediaStore.ACTION_IMAGE_CAPTURE );
        List<ResolveInfo> resolvedIntentActivities = MainActivity.this.getPackageManager().queryIntentActivities(itt, PackageManager.MATCH_DEFAULT_ONLY);

        photoURI = getFileUri();
        for (ResolveInfo resolvedIntentInfo : resolvedIntentActivities) {

            String packageName = resolvedIntentInfo.activityInfo.packageName;

            MainActivity.this.grantUriPermission("com.example.wowow.cameraproject",
                    photoURI, Intent.FLAG_GRANT_WRITE_URI_PERMISSION | Intent.FLAG_GRANT_READ_URI_PERMISSION);

        }

        itt.putExtra( MediaStore.EXTRA_OUTPUT, photoURI );
        startActivityForResult( itt, 1 );
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if( resultCode == RESULT_OK){
            switch ( requestCode ) {
                case 1:
                    //Bitmap bitmap = BitmapFactory.decodeFile( imgPath );
                    //img.setImageBitmap( bitmap );
                    Toast.makeText(MainActivity.this,photoURI.toString(),Toast.LENGTH_SHORT).show();
                    break;
            }
        }
    }

    public void permissionCheck(){
        //권한이 부여되어 있는지 확인
        int permissionCheck= ContextCompat.checkSelfPermission(this, Manifest.permission.CAMERA);

        if(permissionCheck == PackageManager.PERMISSION_GRANTED){
            Toast.makeText(getApplicationContext(), "권한 있음", Toast.LENGTH_SHORT).show();
        }else{
            Toast.makeText(getApplicationContext(), "권한 없음", Toast.LENGTH_SHORT).show();

            //권한설정 dialog에서 거부를 누르면
            //ActivityCompat.shouldShowRequestPermissionRationale 메소드의 반환값이 true가 된다.
            //단, 사용자가 "Don't ask again"을 체크한 경우
            //거부하더라도 false를 반환하여, 직접 사용자가 권한을 부여하지 않는 이상, 권한을 요청할 수 없게 된다.
            if(ActivityCompat.shouldShowRequestPermissionRationale(this, Manifest.permission.CAMERA)){
                //이곳에 권한이 왜 필요한지 설명하는 Toast나 dialog를 띄워준 후, 다시 권한을 요청한다.
                Toast.makeText(getApplicationContext(), "카메라 권한이 필요합니다", Toast.LENGTH_SHORT).show();

            }else{
                ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.CAMERA}, MY_PERMISSIONS_REQUEST_CAMERA);
            }
        }
    }
    public void permissionCheck2(){
        //권한이 부여되어 있는지 확인
        int permissionCheck= ContextCompat.checkSelfPermission(this, Manifest.permission.WRITE_EXTERNAL_STORAGE);

        if(permissionCheck == PackageManager.PERMISSION_GRANTED){
            Toast.makeText(getApplicationContext(), "권한 있음", Toast.LENGTH_SHORT).show();
        }else{
            Toast.makeText(getApplicationContext(), "권한 없음", Toast.LENGTH_SHORT).show();

            //권한설정 dialog에서 거부를 누르면
            //ActivityCompat.shouldShowRequestPermissionRationale 메소드의 반환값이 true가 된다.
            //단, 사용자가 "Don't ask again"을 체크한 경우
            //거부하더라도 false를 반환하여, 직접 사용자가 권한을 부여하지 않는 이상, 권한을 요청할 수 없게 된다.
            if(ActivityCompat.shouldShowRequestPermissionRationale(this, Manifest.permission.WRITE_EXTERNAL_STORAGE)){
                //이곳에 권한이 왜 필요한지 설명하는 Toast나 dialog를 띄워준 후, 다시 권한을 요청한다.
                Toast.makeText(getApplicationContext(), "카메라 권한이 필요합니다", Toast.LENGTH_SHORT).show();

            }else{
                ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE}, MY_PERMISSIONS_REQUEST_WRITE);
            }
        }
    }
    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        switch (requestCode) {
            case MY_PERMISSIONS_REQUEST_CAMERA:

                if (grantResults.length > 0
                        && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    // 권한 허가
                    // 해당 권한을 사용해서 작업을 진행할 수 있습니다
                } else {
                    // 권한 거부
                    // 사용자가 해당권한을 거부했을때 해주어야 할 동작을 수행합니다
                }
                return;

            case MY_PERMISSIONS_REQUEST_WRITE:

                if (grantResults.length > 0
                        && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    // 권한 허가
                    // 해당 권한을 사용해서 작업을 진행할 수 있습니다
                } else {
                    // 권한 거부
                    // 사용자가 해당권한을 거부했을때 해주어야 할 동작을 수행합니다
                }
                return;
        }
    }

}
