package com.example.yelimhan.dangshin;


import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.constraint.ConstraintLayout;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.DisplayMetrics;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.Locale;

public class QuestionListImageAdapter extends RecyclerView.Adapter<QuestionListImageAdapter.ViewHolder>{

    private Context mContext;
    private ArrayList<QuestionInfo> questionInfos;
    private StorageReference storageReference = FirebaseStorage.getInstance("gs://dangshin-fa136.appspot.com").getReference();
    private Boolean doneFlag;


    private int visibleThreshold = 2;
    private int lastVisibleItem, totalItemCount;
    private boolean isLoading;
    private OnLoadMoreListener onLoadMoreListener;

    public interface OnLoadMoreListener {
        void onLoadMore();
    }

    public QuestionListImageAdapter(Context c, ArrayList<QuestionInfo> uris, boolean flag) {
        mContext = c;
        questionInfos = new ArrayList<QuestionInfo>();
        questionInfos.addAll(uris);
        doneFlag = flag;

        // 프로그레스 로딩
        /*if (recyclerView.getLayoutManager() instanceof LinearLayoutManager) {
            final LinearLayoutManager linearLayoutManager = (LinearLayoutManager) recyclerView
                    .getLayoutManager();
            recyclerView.addOnScrollListener(new RecyclerView.OnScrollListener() {
                @Override
                public void onScrolled(@NonNull RecyclerView recyclerView, int dx, int dy) {
                    super.onScrolled(recyclerView, dx, dy);
                    totalItemCount = linearLayoutManager.getItemCount();
                    lastVisibleItem = linearLayoutManager.findLastVisibleItemPosition();
                    if (!isLoading && totalItemCount <= (lastVisibleItem + visibleThreshold)) {
                        if (onLoadMoreListener != null) {
                            onLoadMoreListener.onLoadMore();
                        }
                        isLoading = true;
                    }
                }
            });
        }*/
    }

    @Override
    public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.view_question_adapter, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(QuestionListImageAdapter.ViewHolder holder, int position) {
        final QuestionInfo questionInfo = questionInfos.get(position);
        DisplayMetrics dm = mContext.getResources().getDisplayMetrics();
        int Width = dm.widthPixels/2;
         // 시간계산
        long nowTime = System.currentTimeMillis();
        Date nowdate = new Date(nowTime);
        SimpleDateFormat dateFormat = new SimpleDateFormat("yyyyMMddHHmmss", Locale.KOREA);
        try {
            Date date1 = dateFormat.parse(questionInfo.q_dataTime);
            long diffTime = nowTime - date1.getTime();
            long diffsec = diffTime / 1000 % 60;
            long diffmin = diffTime / (60 * 1000) % 60;
            long diffhour = diffTime / (60 * 60 * 1000) % 24;
            long diffDays = diffTime / (24 * 60 * 60 * 1000);
            if (diffDays == 0) {
                if (diffhour == 0) {
                    if (diffmin == 0) {
                        if (diffsec <= 0) {
                            holder.text.setText("방금 전");
                        }
                        holder.text.setText(diffsec + "초 전");
                    } else {
                        holder.text.setText(diffmin + "분 전");
                    }
                } else {
                    holder.text.setText(diffhour + "시간 전");
                }
            } else {
                holder.text.setText(diffDays + "일 전");
            }
        } catch (ParseException e) {
            e.printStackTrace();
        }
        // 긴급이면
        if (questionInfos.get(position).checkUrgent) {
            holder.urgent.setVisibility(View.VISIBLE);
        }

        // 답변이 없으면
        if (!doneFlag && !questionInfos.get(position).checkAnswer) {
            GlideApp.with(mContext)
                    .load(storageReference.child(questionInfo.q_pic))
                    .override(Width)
                    .into(holder.image);
            holder.image.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    // 음성파일 STT는 나중에..
                    Intent intent = new Intent(mContext, AnswerActivity.class);
                    Bundle bundle = new Bundle();
                    bundle.putSerializable("question", questionInfo);
                    intent.putExtras(bundle);
                    mContext.startActivity(intent);
                }
            });
        }
        // 답변이 있으면

        if (doneFlag && questionInfos.get(position).checkAnswer) {
            GlideApp.with(mContext)
                    .load(storageReference.child(questionInfo.q_pic))
                    .override(Width)
                    .into(holder.image);
            holder.image.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    Intent intent = new Intent(mContext, ListenActivity.class);
                    Bundle bundle = new Bundle();
                    bundle.putSerializable("question", questionInfo);
                    intent.putExtras(bundle);
                    mContext.startActivity(intent);
                }
            });
        }
    }

    @Override
    public int getItemCount() {
        return questionInfos.size();
    }

    public void setLoading() {
        isLoading = false;
    }

    public void add(ArrayList<QuestionInfo> uris) {
        questionInfos.addAll(uris);
        notifyDataSetChanged();
    }

    class ViewHolder extends RecyclerView.ViewHolder{
        ImageView urgent;
        ImageView image;
        TextView text;

        ViewHolder(View itemView) {
            super(itemView);
            image = itemView.findViewById(R.id.image);
            urgent = itemView.findViewById(R.id.emergency);
            text = itemView.findViewById(R.id.showtime);
        }
    }

}

