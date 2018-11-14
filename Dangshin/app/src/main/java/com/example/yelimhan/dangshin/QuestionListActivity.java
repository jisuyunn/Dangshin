package com.example.yelimhan.dangshin;

import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.widget.Toast;

public class QuestionListActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_question_list);

        Toast.makeText(this, "봉사자 - 질문 선택",Toast.LENGTH_LONG).show();
    }
}
