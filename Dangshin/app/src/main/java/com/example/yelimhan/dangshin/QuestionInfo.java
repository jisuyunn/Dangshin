package com.example.yelimhan.dangshin;

import java.io.Serializable;
import java.text.ParseException;
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
    public boolean checkAnswer;
    public boolean checkUrgent;

    QuestionInfo() {

    }
    public QuestionInfo(String id, String pic, String voice, String writer, boolean urgent) {
        q_id = id;
        q_pic = pic;
        q_voice = voice;
        q_writer = writer;
        checkUrgent = urgent;
        checkAnswer = false;
        long nowTime = System.currentTimeMillis();
        Date nowDate = new Date(nowTime);
        SimpleDateFormat dateFormat = new SimpleDateFormat("yyyyMMddHHmmss", Locale.KOREA);
        q_dataTime = dateFormat.format(nowDate);
    }
}
