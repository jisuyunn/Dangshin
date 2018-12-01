package com.example.yelimhan.bomtogether;


import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.os.Bundle;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.AlphaAnimation;
import android.view.animation.Animation;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.TextView;

import com.bumptech.glide.load.MultiTransformation;
import com.bumptech.glide.load.resource.bitmap.CenterCrop;
import com.bumptech.glide.load.resource.bitmap.RoundedCorners;
import com.bumptech.glide.request.RequestOptions;
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

    public QuestionListImageAdapter(RecyclerView recyclerView, Context c, ArrayList<QuestionInfo> uris, boolean flag) {
        mContext = c;
        questionInfos = new ArrayList<QuestionInfo>();
        questionInfos.addAll(uris);
        doneFlag = flag;
    }

    @Override
    public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.view_question_adapter, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(QuestionListImageAdapter.ViewHolder holder, int position) {
        final QuestionInfo questionInfo = questionInfos.get(position);
        // 시간계산
        long nowTime = System.currentTimeMillis();
        SimpleDateFormat dateFormat = new SimpleDateFormat("yyyyMMddHHmmss", Locale.KOREA);
        Date date1;

        // 답변이 없으면
        if (!doneFlag && !questionInfos.get(position).checkAnswer) {
            // 긴급이면
            if (questionInfos.get(position).checkUrgent) {
                holder.urgent.setVisibility(View.VISIBLE);
            }
            GlideApp.with(mContext)
                    .load(storageReference.child(questionInfo.q_pic))
                    .placeholder(R.drawable.ic_image_black_24dp)
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
            try {
                date1 = dateFormat.parse(questionInfo.q_dataTime);
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
        }

        // 답변이 있으면
        if (doneFlag && questionInfos.get(position).checkAnswer) {
            GlideApp.with(mContext)
                    .load(storageReference.child(questionInfo.q_pic))
                    .placeholder(R.drawable.ic_image_black_24dp)
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
    public void onViewAttachedToWindow(QuestionListImageAdapter.ViewHolder holder) {
        if(holder != null) {
            if (holder instanceof ViewHolder) {
                ViewHolder messageHolder = (ViewHolder) holder;
                Animation anim = new AlphaAnimation(0.0f, 1.0f);
                anim.setDuration(400); //You can manage the time of the blink with this parameter
                anim.setStartOffset(50);
                anim.setRepeatMode(Animation.REVERSE);
                anim.setRepeatCount(Animation.INFINITE);
                holder.urgent.startAnimation(anim);
            }
        }
    }

    @Override
    public int getItemCount() {
        return questionInfos.size();
    }

    public void add(ArrayList<QuestionInfo> uris) {
        int startpos = questionInfos.size();
        questionInfos.addAll(uris);
        notifyItemRangeChanged(startpos,uris.size());
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

