package com.edam.iu.plane.viewpagercards;


public class CardItem {

    private String mAvatarUrl;
    private String mNickName;
    private String mRanking;
    private boolean isLeeJiEun;

    public CardItem(String avatar, String nickname, String ranking, boolean isFans) {
        mAvatarUrl = avatar;
        mNickName = nickname;
        mRanking = ranking;
        isLeeJiEun = isFans;
    }

    public String getAvatarUrl() {
        return mAvatarUrl;
    }

    public String getNickName() {
        return mNickName;
    }

    public String getRanking() { return mRanking; }

    public boolean getIsLeeJiEunFans() { return isLeeJiEun; }
}
