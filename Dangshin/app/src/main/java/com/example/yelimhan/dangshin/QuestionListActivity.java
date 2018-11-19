package com.example.yelimhan.dangshin;

import android.media.Image;
import android.support.design.widget.TabLayout;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentTransaction;
import android.support.v4.view.ViewPager;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.ImageView;
import android.widget.Toast;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

public class QuestionListActivity extends AppCompatActivity {

    public String userId = "";
    public QuestionListAdapter adapter;
    public boolean firstTime = true;
    public ViewPager viewPager;
    public QuestionListFragment qlFragment;
    public QuestionDoneListFragment qdlFragment;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_question_list);
        Toast.makeText(this, "봉사자 - 질문 선택",Toast.LENGTH_LONG).show();
        viewPager = findViewById(R.id.pager);
        setupViewPager(viewPager);
        TabLayout tabLayout = findViewById(R.id.tabs);
        tabLayout.setupWithViewPager(viewPager);
        // 홈 버튼 눌렀을 때
        findViewById(R.id.button_home).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

            }
        });
        // refresh버튼 눌렀을 때
        findViewById(R.id.button_refresh).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                view.setAlpha(.5f);
                qlFragment.getFirebaseData();
                qdlFragment.getFirebaseData();
                view.setAlpha(1f);
            }
        });
    }

    private void setupViewPager(ViewPager viewPager) {
        adapter = new QuestionListAdapter(getSupportFragmentManager());
        qlFragment = new QuestionListFragment();
        qdlFragment = new QuestionDoneListFragment();
        adapter.addFrag(qlFragment, "질문");
        adapter.addFrag(qdlFragment, "완료질문");
        viewPager.setAdapter(adapter);
    }

    @Override
    public void onStart() {
        super.onStart();
        if(!firstTime) {
            qlFragment.getFirebaseData();
            qdlFragment.getFirebaseData();
        }
    }

    @Override
    public void onStop() {
        super.onStop();
        firstTime = false;
    }

}
