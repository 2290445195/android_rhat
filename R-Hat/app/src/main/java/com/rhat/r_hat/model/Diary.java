package com.rhat.r_hat.model;

/**
 * Created by PartyJat on 2016/10/22.
 */
//日记类
public class Diary {
    private int id;
    private String title;
    private String diary;
    private String date;

    public int getId() {
        return id;
    }
    public void setId(int id) {
        this.id = id;
    }

    public String getTitle() {
        return title;
    }
    public void setTitle(String title) {
        this.title = title;
    }

    public String getDiary() {
        return diary;
    }
    public void setDiary(String diary) {
        this.diary = diary;
    }

    public String getDate() {
        return date;
    }
    public void setDate(String date) {
        this.date = date;
    }

}
