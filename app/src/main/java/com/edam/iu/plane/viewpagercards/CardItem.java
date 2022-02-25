package com.edam.iu.plane.viewpagercards;


public class CardItem {

    private int mTextResource;
    private int mTitleResource;
    private String mRanking;

    public CardItem(int title, int text, String ranking) {
        mTitleResource = title;
        mTextResource = text;
        mRanking = ranking;
    }

    public int getText() {
        return mTextResource;
    }

    public int getTitle() {
        return mTitleResource;
    }

    public String getRanking(){return mRanking;}
}
