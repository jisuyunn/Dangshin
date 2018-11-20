package com.example.yelimhan.dangshin;

import android.content.Context;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.GridView;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.Collections;

public class QuestionListFragment extends Fragment {


    private Context context;
    private DatabaseReference mDatabase;
    private ArrayList<QuestionInfo> imagePath = new ArrayList<QuestionInfo>();
    private GridView gridView;
    private QuestionListImageAdapter gridImageAdapter;
    private View view;

    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        Log.d("testt  ", "QL fragment oncreate");
        view = inflater.inflate(R.layout.fragment_question_list, container, false);
        context = container.getContext();
        gridView = view.findViewById(R.id.gridView);

        // 파이어베이스에서 데이터 받아오기
        getFirebaseData();

        return view;
    }

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
    }

    @Override
    public void onDetach() {
        super.onDetach();

    }

    @Override
    public void onResume() {
        super.onResume();
    }

    @Override
    public void onPause() {
        super.onPause();
    }

    @Override
    public void onStop() {
        super.onStop();
    }


    public void getFirebaseData() {
        mDatabase = FirebaseDatabase.getInstance().getReference("QuestionInfo");
        mDatabase.limitToLast(10).orderByChild("q_dataTime").addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                imagePath.clear();
                for(DataSnapshot snapshot : dataSnapshot.getChildren()) {
                    QuestionInfo questionInfo = snapshot.getValue(QuestionInfo.class);
                    if(!questionInfo.checkAnswer)
                        imagePath.add(questionInfo);
                }
                Collections.reverse(imagePath);
                // 그리드 뷰 사용
                gridImageAdapter = new QuestionListImageAdapter(context, imagePath, false);
                gridView.setAdapter(gridImageAdapter);
                // 스크롤 맨 아래 닿았을 때
               /* gridView.setOnScrollListener(new AbsListView.OnScrollListener() {
                    boolean checkLast;
                    @Override
                    public void onScrollStateChanged(AbsListView view, int scrollState) {
                        if(scrollState == AbsListView.OnScrollListener.SCROLL_STATE_IDLE && checkLast) {
                        }
                    }
                    @Override
                    public void onScroll(AbsListView view, int firstVisibleItem, int visibleItemCount, int totalItemCount) {
                        checkLast = ((totalItemCount>0) && (firstVisibleItem+visibleItemCount >= totalItemCount-1));
                    }
                });*/
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });

    }

}
