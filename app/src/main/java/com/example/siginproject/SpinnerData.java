package com.example.siginproject;

public class SpinnerData {

    private String courseNo = "";
    private int planId;
    private String text = "";

    public SpinnerData() {
        courseNo = "";
        text = "";
    }

    public SpinnerData(String courseNo, int planId, String _text) {
        this.courseNo = courseNo;
        this.planId = planId;
        this.text = _text;
    }

    @Override
    public String toString() {

        return text;
    }

    public String getCourseNo() {
        return courseNo;
    }

    public int getPlanId() {
        return planId;
    }

    public String getText() {
        return text;
    }
}
