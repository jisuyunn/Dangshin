package com.example.yelimhan.bomtogether;


// 사용자 정보 클래스
public class UserInfo {
    public String u_googleId;       // 사용자의 구글 아이디
    public String u_position;       // 사용자의 역할 (volunteer, blind, administrator)
    public boolean u_online;            // 사용자 - Volunteer의 접속 여부 (접속중 - 1 / 접속중 아님 - 0)
    public int u_haveQuestion;      // 질문이 없으면 0, 있는데 답변이 없으면 1, 질문있고 답변도 있으면 2
    public String u_token;
    public String q_key;            // 질문이 있는 경우 질문의 id
    public String urgent_qid;

    UserInfo(String i, String p, boolean o, int q, String t, String k, String u){
        this.u_googleId = i;
        this.u_position = p;
        this.u_online = o;
        this.u_haveQuestion = q;
        this.u_token = t;
        this.q_key = k;
        this.urgent_qid = u;
    }

    UserInfo() {

    }
}
