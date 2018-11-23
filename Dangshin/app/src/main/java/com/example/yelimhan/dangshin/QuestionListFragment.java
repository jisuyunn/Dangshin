package com.example.yelimhan.dangshin;

import android.content.Context;
import android.graphics.Color;
import android.graphics.Path;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.app.Fragment;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.StaggeredGridLayoutManager;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.GridView;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.baoyz.widget.PullRefreshLayout;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.orangegangsters.github.swipyrefreshlayout.library.SwipyRefreshLayout;
import com.orangegangsters.github.swipyrefreshlayout.library.SwipyRefreshLayoutDirection;

import java.util.ArrayList;
import java.util.Collections;

import static com.firebase.ui.auth.AuthUI.TAG;

public class QuestionListFragment extends Fragment {


    private Context context;
    private DatabaseReference mDatabase;
    private ArrayList<QuestionInfo> imagePath = new ArrayList<QuestionInfo>();
    private QuestionListImageAdapter gridImageAdapter;
    private View view;
    private RecyclerView recyclerView;
    private ArrayList<QuestionInfo> path = new ArrayList<>();
    private final int MaxData = 6;

    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        Log.d("testt  ", "QL fragment oncreate");
        view = inflater.inflate(R.layout.fragment_question_list, container, false);
        context = container.getContext();
        recyclerView = view.findViewById(R.id.recyclerView);
        recyclerView.addItemDecoration(new GridSpacingItemDecoration(2,15,false));


        // 파이어베이스에서 데이터 받아오기
        getFirstFirebaseData();
        // 당겨서 새로고침
        final PullRefreshLayout layout = (PullRefreshLayout) view.findViewById(R.id.swipeRefreshLayout);
        layout.setColor(Color.MAGENTA);
        layout.setOnRefreshListener(new PullRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {
                getFirstFirebaseData();
                layout.setRefreshing(false);
            }
        });
        recyclerView.setOnScrollListener(new RecyclerView.OnScrollListener() {
            @Override
            public void onScrollStateChanged(@NonNull RecyclerView recyclerView, int newState) {
                super.onScrollStateChanged(recyclerView, newState);
                if(!recyclerView.canScrollVertically(1)) {
                    getFirebaseData();
                }
            }
        });
        /*final SwipyRefreshLayout swipeRefreshLayout = view.findViewById(R.id.swipe_layout);
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
        });*/

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
        mDatabase.orderByChild("checkAnswer").equalTo(false).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                path.clear();
                imagePath.clear();
                for(DataSnapshot snapshot : dataSnapshot.getChildren()) {
                    QuestionInfo questionInfo = snapshot.getValue(QuestionInfo.class);
                    imagePath.add(questionInfo);
                }
                // 그리드 뷰 사용
                int size = imagePath.size();
                Collections.reverse(imagePath);
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
                gridImageAdapter = new QuestionListImageAdapter(recyclerView,context,path,false);
                recyclerView.setAdapter(gridImageAdapter);
                //final StaggeredGridLayoutManager manager = new StaggeredGridLayoutManager(2, StaggeredGridLayoutManager.VERTICAL);
                final GridLayoutManager manager = new GridLayoutManager(context,2);
                recyclerView.setLayoutManager(manager);



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
        if(path.size()>0) {
            gridImageAdapter.add(path);
        }
    }



}
