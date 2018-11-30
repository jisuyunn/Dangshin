package com.example.yelimhan.dangshin;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

public class ReportInfo {   // 신고하기
    public String r_dateTime;
    public String r_writer;
    public String a_id;
    public String r_id;

    ReportInfo() {

    }
    public ReportInfo(String id,String writer, String a) {
        r_id = id;
        r_writer = writer;
        a_id = a;
        long nowTime = System.currentTimeMillis();
        Date nowDate = new Date(nowTime);
        SimpleDateFormat dateFormat = new SimpleDateFormat("yyyyMMddHHmmss", Locale.KOREA);
        r_dateTime = dateFormat.format(nowDate);
    }
}
