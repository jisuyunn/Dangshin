package com.example.yelimhan.bomtogether;


import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.GridLayoutManager;
import android.util.Log;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ListView;
import android.widget.Toast;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;

public class FavoriteActivity extends AppCompatActivity {

    EditText editText;
    ImageButton addBtn;

    String searchText = "";

    String userIndexId = "";
    String blindIndexId = "";

    String userId ="";
    private DatabaseReference mDatabase;

    ArrayList<FavoriteListItem> items;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_favorite);
        final DatabaseReference table = FirebaseDatabase.getInstance().getReference("UserInfo");

        editText = findViewById(R.id.edittext);
        addBtn = findViewById(R.id.btn);
        final FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
        userId = user.getEmail();

        ////////intent에서 받아와야함
        userIndexId = "-LSh0D6mj-ZxOmNbWyrF";

        ListView listView;
        FavoriteListAdapter adapter;
        items = new ArrayList<>();

        loadItemsFromDB();


        adapter = new FavoriteListAdapter(this, R.layout.favoriterow, items );
        listView = (ListView) findViewById(R.id.listview);
        listView.setAdapter(adapter);


        addBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                searchText = editText.getText().toString();

                Query query = table.orderByChild("u_googleId").equalTo(searchText);
                query.addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot dataSnapshot){
                        for (DataSnapshot snapshot: dataSnapshot.getChildren()) {
                            if(dataSnapshot.exists()){
                                // 추가 버튼 누르면 (있는지 검사해서) firebase에 저장
                                // Query query = table.orderByChild("u_position").equalTo(searchText);


                                // 봉사자의 즐겨찾기에 시각장애인 추가
                                DatabaseReference newUser = FirebaseDatabase.getInstance().getReference("UserInfo").child(userIndexId).child("u_favorite").push();
                                newUser.setValue(searchText);

                                // 시각장애인의 즐겨찾기에 봉사자 추가
                                blindIndexId = snapshot.getKey().toString();
                                Toast.makeText(getApplicationContext(),blindIndexId,Toast.LENGTH_SHORT).show();
                                Log.d("testt", blindIndexId);
                                DatabaseReference blindRef = FirebaseDatabase.getInstance().getReference("UserInfo").child(blindIndexId).child("u_favorite").push();
                                blindRef.setValue(userId);

                                editText.setText("");
                            }
                            else {
                                Toast.makeText(getApplicationContext(),"잘못된 입력입니다", Toast.LENGTH_SHORT).show();
                            }
                        }

                    }


                    @Override
                    public void onCancelled(@NonNull DatabaseError databaseError) {

                    }

                });
            }
        });


    }

    public void loadItemsFromDB() {

        int i;
        if (items == null) {
            items = new ArrayList<FavoriteListItem>();
        }


        mDatabase = FirebaseDatabase.getInstance().getReference("UserInfo");
        Query query = mDatabase.orderByChild("u_googleId").equalTo(userId);


        query.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                for (DataSnapshot snapshot : dataSnapshot.getChildren()) {

                    String fl = snapshot.getKey().toString();
                    DatabaseReference db = FirebaseDatabase.getInstance().getReference("UserInfo").child(userId).child(fl);
                    Query q1 = db.orderByValue();
                    q1.addListenerForSingleValueEvent(new ValueEventListener() {
                        @Override
                        public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                            for (DataSnapshot snapshot : dataSnapshot.getChildren()) {
                                FavoriteListItem item = null;
                                item.setText(snapshot.getValue().toString());
                                Log.d("test", snapshot.getValue().toString());
                                items.add(item);
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


}