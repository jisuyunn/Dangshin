package com.example.yelimhan.bomtogether;

import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.widget.ImageView;

import com.github.chrisbanes.photoview.PhotoView;
import com.github.chrisbanes.photoview.PhotoViewAttacher;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;

public class ImageManager extends AppCompatActivity {

    private QuestionInfo questionInfo;
    private StorageReference storageReference = FirebaseStorage.getInstance("gs://bomtogether-5f74b.appspot.com").getReference();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.view_image);

        Intent intent = getIntent();
        questionInfo = (QuestionInfo) intent.getSerializableExtra("question");
        PhotoView photoView = (PhotoView) findViewById(R.id.photo_view);
        PhotoViewAttacher photoViewAttacher = new PhotoViewAttacher(photoView);
        photoViewAttacher.setScaleType(PhotoView.ScaleType.FIT_XY);
        GlideApp.with(this)
                .load(storageReference.child(questionInfo.q_pic))
                .into(photoView);

    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
    }
}
