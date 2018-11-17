package com.example.yelimhan.dangshin;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

// 답변 정보 클래스
public class AnswerInfo {
    public String a_id;
    public String a_dateTime;
    public String a_writer;
    public String answer;
    public String q_id;
    public boolean checkReport;

    public AnswerInfo(){

    }

    public AnswerInfo(String a, String q, String writer, String ans) {
        a_id = a;
        q_id = q;
        a_writer = writer;
        answer = ans;
        checkReport = false;
        long nowTime = System.currentTimeMillis();
        Date nowDate = new Date(nowTime);
        SimpleDateFormat dateFormat = new SimpleDateFormat("yyyyMMddHHmmss", Locale.KOREA);
        a_dateTime = dateFormat.format(nowDate);
    }
}
