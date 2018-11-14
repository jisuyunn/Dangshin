package com.example.yelimhan.dangshin;


// 사용자 정보 클래스
public class UserInfo {
    public String u_googleId;       // 사용자의 구글 아이디
    public String u_position;       // 사용자의 역할 (volunteer, blind, administrator)
    public boolean u_online;            // 사용자 - Volunteer의 접속 여부 (접속중 - 1 / 접속중 아님 - 0)

    public UserInfo(String i, String p, boolean o){
        this.u_googleId = i;
        this.u_position = p;
        this.u_online = o;
    }
}
