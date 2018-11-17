package com.example.yelimhan.dangshin;

import android.media.Image;
import android.support.design.widget.TabLayout;
import android.support.v4.view.ViewPager;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.ImageView;
import android.widget.Toast;

public class QuestionListActivity extends AppCompatActivity {

    public QuestionListAdapter adapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_question_list);

        Toast.makeText(this, "봉사자 - 질문 선택",Toast.LENGTH_LONG).show();
        ViewPager viewPager = findViewById(R.id.pager);
        setupViewPager(viewPager);
        TabLayout tabLayout = findViewById(R.id.tabs);
        tabLayout.setupWithViewPager(viewPager);
    }

    private void setupViewPager(ViewPager viewPager) {
        adapter = new QuestionListAdapter(getSupportFragmentManager());
        QuestionListFragment QLFragment = new QuestionListFragment();
        Bundle bundle = new Bundle(1);
        bundle.putBoolean("Done",false);
        QLFragment.setArguments(bundle);
        adapter.addFrag(QLFragment, "질문");
        QuestionListFragment DQLFragment = new QuestionListFragment();
        Bundle bundle2 = new Bundle(1);
        bundle2.putBoolean("Done",true);
        DQLFragment.setArguments(bundle2);
        adapter.addFrag(DQLFragment, "완료질문");
        viewPager.setAdapter(adapter);
    }
}
