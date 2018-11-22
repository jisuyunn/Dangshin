package com.example.yelimhan.dangshin;

import android.content.Context;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.app.Fragment;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.StaggeredGridLayoutManager;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.GridView;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.orangegangsters.github.swipyrefreshlayout.library.SwipyRefreshLayout;
import com.orangegangsters.github.swipyrefreshlayout.library.SwipyRefreshLayoutDirection;

import java.util.ArrayList;
import java.util.Collections;

public class QuestionDoneListFragment extends Fragment {


    private Context context;
    private DatabaseReference mDatabase;
    private ArrayList<QuestionInfo> imagePath = new ArrayList<QuestionInfo>();
    private QuestionListImageAdapter gridImageAdapter;
    private View view;
    private RecyclerView recyclerView;
    private ArrayList<QuestionInfo> path = new ArrayList<>();
    private final int MaxData = 8;

    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        view = inflater.inflate(R.layout.fragment_question_list, container, false);
        context = container.getContext();
        recyclerView = view.findViewById(R.id.recyclerView);
        recyclerView.addItemDecoration(new GridSpacingItemDecoration(2,15,false));
        view.findViewById(R.id.text3).setVisibility(View.GONE);
        // 파이어베이스에서 데이터 받아오기
        getFirstFirebaseData();
        // 당겨서 새로고침
        final SwipyRefreshLayout swipeRefreshLayout = view.findViewById(R.id.swipe_layout);
        swipeRefreshLayout.setDirection(SwipyRefreshLayoutDirection.BOTH);
        swipeRefreshLayout.setOnRefreshListener(new SwipyRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh(SwipyRefreshLayoutDirection direction) {
                if(direction == SwipyRefreshLayoutDirection.TOP) {
                    getFirstFirebaseData();
                    swipeRefreshLayout.setRefreshing(false);
                } else if(direction == SwipyRefreshLayoutDirection.BOTTOM) {
                    getFirebaseData();
                    swipeRefreshLayout.setRefreshing(false);
                }
            }
        });

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


    public void getFirstFirebaseData() {
        mDatabase = FirebaseDatabase.getInstance().getReference("QuestionInfo");
        mDatabase.orderByChild("checkAnswer").equalTo(true).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                path.clear();
                imagePath.clear();
                for(DataSnapshot snapshot : dataSnapshot.getChildren()) {
                    QuestionInfo questionInfo = snapshot.getValue(QuestionInfo.class);
                    imagePath.add(questionInfo);
                }
                //Collections.reverse(imagePath);
                // 그리드 뷰 사용
                int size = imagePath.size();
                if(size < MaxData) {
                    for (int i = 0; i < size; i++) {
                        path.add(imagePath.get(0));
                        imagePath.remove(0);
                    }
                } else {
                    for (int i = 0; i < MaxData; i++) {
                        path.add(imagePath.get(0));
                        imagePath.remove(0);
                    }
                }
                gridImageAdapter = new QuestionListImageAdapter(recyclerView,context,path,true);
                GridLayoutManager manager = new GridLayoutManager(context,2);
                recyclerView.setAdapter(gridImageAdapter);
                recyclerView.setLayoutManager(manager);
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

    public void getFirebaseData() {
        path.clear();
        int size = imagePath.size();
        if(size < MaxData) {
            for (int i = 0; i < size; i++) {
                path.add(imagePath.get(0));
                imagePath.remove(0);
            }
        } else {
            for (int i = 0; i < MaxData; i++) {
                path.add(imagePath.get(0));
                imagePath.remove(0);
            }
        }
        gridImageAdapter.add(path);
    }

}
