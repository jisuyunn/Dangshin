package com.example.yelimhan.dangshin;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.Locale;

public class QuestionListImageAdapter extends BaseAdapter {

    private Context mContext;
    private ArrayList<QuestionInfo> questionInfos;
    private StorageReference storageReference = FirebaseStorage.getInstance("gs://dangshin-fa136.appspot.com").getReference();
    private Boolean doneFlag;


    public QuestionListImageAdapter(Context c, ArrayList<QuestionInfo> uris, boolean flag) {

        mContext = c;
        questionInfos = new ArrayList<QuestionInfo>();
        questionInfos.addAll(uris);
        doneFlag = flag;

    }

    @Override
    public int getCount() {
        return questionInfos.size();
    }

    @Override
    public Object getItem(int position) {
        return questionInfos.get(position).hashCode();
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    @Override
    public View getView(final int position, View convertView, ViewGroup parent) {
        final LayoutInflater inflater = (LayoutInflater) mContext.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        View gridView;

        if(convertView == null) {
            gridView = inflater.inflate(R.layout.view_question_adapter,null);
            ImageView imageView1 = gridView.findViewById(R.id.image);
            TextView textView = gridView.findViewById(R.id.showtime);
            // 시간계산
            long nowTime = System.currentTimeMillis();
            Date nowdate = new Date(nowTime);
            SimpleDateFormat dateFormat = new SimpleDateFormat("yyyyMMddHHmmss", Locale.KOREA);
            try {
                Date date1 = dateFormat.parse(questionInfos.get(position).q_dataTime);
                long diffTime = nowTime - date1.getTime();
                long diffsec = diffTime/1000 % 60;
                long diffmin = diffTime/(60*1000) % 60;
                long diffhour = diffTime/(60*60*1000) % 24;
                long diffDays = diffTime/(24*60*60*1000);
                if(diffDays == 0) {
                    if(diffhour == 0) {
                        if(diffmin == 0) {
                            if(diffsec <= 0) {
                                textView.setText("방금 전");
                            }
                            textView.setText(diffsec + "초 전");
                        } else {
                            textView.setText(diffmin + "분 전");
                        }
                    } else {
                        textView.setText(diffhour + "시간 전");
                    }
                } else {
                    textView.setText(diffDays + "일 전");
                }
            } catch (ParseException e) {
                e.printStackTrace();
            }
            // 긴급이면
            if(questionInfos.get(position).checkUrgent) {
                ImageView imageView2 = gridView.findViewById(R.id.emergency);
                imageView2.setVisibility(View.VISIBLE);
            }
            // 답변이 없으면
            if(!doneFlag && !questionInfos.get(position).checkAnswer) {
                GlideApp.with(mContext)
                        .load(storageReference.child(questionInfos.get(position).q_pic))
                        .centerCrop()
                        .override(300,300)
                        .into(imageView1);
                imageView1.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        // 음성파일 STT는 나중에..
                        Intent intent = new Intent(mContext,AnswerActivity.class);
                        Bundle bundle = new Bundle();
                        bundle.putSerializable("question",questionInfos.get(position));
                        intent.putExtras(bundle);
                        mContext.startActivity(intent);
                    }
                });
            }
            // 답변이 있으면
            if(doneFlag && questionInfos.get(position).checkAnswer) {
                GlideApp.with(mContext)
                        .load(storageReference.child(questionInfos.get(position).q_pic))
                        .centerCrop()
                        .override(300,300)
                        .into(imageView1);
                imageView1.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        Intent intent = new Intent(mContext,ListenActivity.class);
                        Bundle bundle = new Bundle();
                        bundle.putSerializable("question",questionInfos.get(position));
                        intent.putExtras(bundle);
                        mContext.startActivity(intent);
                    }
                });
            }


        } else {
            gridView = convertView;
        }

        return gridView;
    }

    public void add(ArrayList<QuestionInfo> uris) {
        questionInfos.addAll(uris);
        notifyDataSetChanged();
    }

}

