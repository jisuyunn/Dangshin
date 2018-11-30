package com.example.yelimhan.dangshin;

import android.graphics.Color;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.View;

import com.baoyz.widget.PullRefreshLayout;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.Collections;


// 내 답변 목록 (Volunteer)
public class AnswerListActivity extends AppCompatActivity {

    private DatabaseReference mDatabase;
    private ArrayList<QuestionInfo> imagePath = new ArrayList<QuestionInfo>();
    private QuestionListImageAdapter gridImageAdapter;
    private RecyclerView recyclerView;
    private ArrayList<QuestionInfo> path = new ArrayList<>();
    private UserInfo userInfo;
    private final int MaxData = 8;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_answer_list);
        findViewById(R.id.text3).setVisibility(View.GONE);
        recyclerView = findViewById(R.id.recyclerView);
        recyclerView.addItemDecoration(new GridSpacingItemDecoration(2,15,false));

        // 현재 접속중인 사용자의 정보를 받아옴. 없으면 null
        final FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
        String userId = user.getEmail();
        // firebase database 참조 객체
        DatabaseReference table = FirebaseDatabase.getInstance().getReference("UserInfo");
        if(user != null) {
            Query query = table.orderByChild("u_googleId").equalTo(userId);
            query.addListenerForSingleValueEvent(new ValueEventListener() {       // 역할 알아오기
                @Override
                public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                    for (DataSnapshot snapshot : dataSnapshot.getChildren()) {
                        userInfo = snapshot.getValue(UserInfo.class);
                        // 시각장애인이라면?? 답변 리스트 필요한가..
                        // 봉사자라면
                        if(userInfo.u_position.equals("Volunteer")) {
                            getFirstFirebaseData();
                            // 당겨서 새로고침
                            final PullRefreshLayout layout = (PullRefreshLayout) findViewById(R.id.swipeRefreshLayout);
                            layout.setColor(Color.LTGRAY);
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
                        }
                    }
                }

                @Override
                public void onCancelled(@NonNull DatabaseError databaseError) {
                }
            });
        }
    }

    public void getFirstFirebaseData() {
            mDatabase = FirebaseDatabase.getInstance().getReference("AnswerInfo");
            final ArrayList<AnswerInfo> answerInfo = new ArrayList<>();
            mDatabase.orderByChild("a_writer").equalTo(userInfo.u_googleId).addListenerForSingleValueEvent(new ValueEventListener() {
                @Override
                public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                    for(DataSnapshot snapshot : dataSnapshot.getChildren()) {
                        answerInfo.add(snapshot.getValue(AnswerInfo.class));
                    }
                    path.clear();
                    imagePath.clear();
                    for(int i=0; i<answerInfo.size(); i++) {
                        FirebaseDatabase.getInstance().getReference("QuestionInfo").orderByKey().equalTo(answerInfo.get(i).q_id)
                                .addListenerForSingleValueEvent(new ValueEventListener() {
                                    @Override
                                    public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                                        for (DataSnapshot snapshot : dataSnapshot.getChildren()) {
                                            imagePath.add(snapshot.getValue(QuestionInfo.class));
                                            if(imagePath.size() == answerInfo.size()) {
                                                printimage();
                                            }
                                        }
                                    }

                                    @Override
                                    public void onCancelled(@NonNull DatabaseError databaseError) {

                                    }
                                });
                    }
                }

                @Override
                public void onCancelled(@NonNull DatabaseError databaseError) {

                }
            });
    }

    public void printimage() {
        Collections.reverse(imagePath);
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
        gridImageAdapter = new QuestionListImageAdapter(recyclerView,this,path,true);
        recyclerView.setAdapter(gridImageAdapter);
        final GridLayoutManager manager = new GridLayoutManager(this, 2);
        recyclerView.setLayoutManager(manager);
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
