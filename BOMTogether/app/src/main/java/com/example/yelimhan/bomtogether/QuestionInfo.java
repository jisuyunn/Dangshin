package com.example.yelimhan.bomtogether;

import java.io.Serializable;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

@SuppressWarnings("serial")
public class QuestionInfo implements Serializable {
    public String q_dataTime;
    public String q_id;
    public String q_pic;
    public String q_writer;
    public String q_voice;
    public String q_stt;
    public boolean checkAnswer;
    public boolean checkUrgent;

    QuestionInfo() {

    }
    public QuestionInfo(String id, String pic, String voice, String writer, String stt, boolean urgent) {
        q_id = id;
        q_pic = pic;
        q_voice = voice;
        q_writer = writer;
        q_stt = stt;
        checkUrgent = urgent;
        checkAnswer = false;
        long nowTime = System.currentTimeMillis();
        Date nowDate = new Date(nowTime);
        SimpleDateFormat dateFormat = new SimpleDateFormat("yyyyMMddHHmmss", Locale.KOREA);
        q_dataTime = dateFormat.format(nowDate);
    }

    public void setQ_dataTime(String q_dataTime) {
        this.q_dataTime = q_dataTime;
    }

    public void setQ_id(String q_id) {
        this.q_id = q_id;
    }

    public void setQ_pic(String q_pic) {
        this.q_pic = q_pic;
    }

    public void setQ_writer(String q_writer) {
        this.q_writer = q_writer;
    }

    public void setQ_voice(String q_voice) {
        this.q_voice = q_voice;
    }

    public void setCheckAnswer(boolean checkAnswer) {
        this.checkAnswer = checkAnswer;
    }

    public void setCheckUrgent(boolean checkUrgent) {
        this.checkUrgent = checkUrgent;
    }
}
