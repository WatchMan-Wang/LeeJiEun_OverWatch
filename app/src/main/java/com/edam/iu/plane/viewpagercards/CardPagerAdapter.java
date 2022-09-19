package com.edam.iu.plane.viewpagercards;


import android.text.TextPaint;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.cardview.widget.CardView;
import androidx.viewpager.widget.PagerAdapter;

import com.bumptech.glide.Glide;
import com.edam.iu.plane.GameActivity;
import com.edam.iu.plane.R;
import com.makeramen.roundedimageview.RoundedImageView;

import java.util.ArrayList;
import java.util.List;

public class CardPagerAdapter extends PagerAdapter implements CardAdapter {

    private final GameActivity gameActivity;
    private List<CardView> mViews;
    private List<CardItem> mData;
    private float mBaseElevation;


    public CardPagerAdapter(GameActivity gameActivity) {
        this.mData = new ArrayList<>();
        this.mViews = new ArrayList<>();
        this.gameActivity = gameActivity;
    }

    public void addCardItem(CardItem item) {
        mViews.add(null);
        mData.add(item);
    }

    public float getBaseElevation() {
        return mBaseElevation;
    }

    @Override
    public CardView getCardViewAt(int position) {
        return mViews.get(position);
    }

    @Override
    public int getCount() {
        return mData.size();
    }

    @Override
    public boolean isViewFromObject(View view, Object object) {
        return view == object;
    }

    @Override
    public Object instantiateItem(ViewGroup container, int position) {
        View view = LayoutInflater.from(container.getContext())
                .inflate(R.layout.adapter, container, false);
        container.addView(view);
        bind(mData.get(position), view);
        CardView cardView = (CardView) view.findViewById(R.id.cardView);

        if (mBaseElevation == 0) {
            mBaseElevation = cardView.getCardElevation();
        }

        cardView.setMaxCardElevation(mBaseElevation * MAX_ELEVATION_FACTOR);
        mViews.set(position, cardView);
        return view;
    }

    @Override
    public void destroyItem(ViewGroup container, int position, Object object) {
        container.removeView((View) object);
        mViews.set(position, null);
    }

    private void bind(CardItem item, View view) {
        ImageView avatarImg = (RoundedImageView) view.findViewById(R.id.ranking_avatar);
        TextView nickname = (TextView) view.findViewById(R.id.ranking_nickname);
        TextView rankingTextView = (TextView) view.findViewById(R.id.ranking);
        TextView leeJiEunFans = (TextView) view.findViewById(R.id.leejieun_fans);
        if(item.getIsLeeJiEunFans()){
            leeJiEunFans.setVisibility(View.VISIBLE);
            TextPaint paint = leeJiEunFans.getPaint();
            paint.setFakeBoldText(true);
        }
        Glide.with(gameActivity.getApplicationContext()).load(item.getAvatarUrl()).into(avatarImg);
        nickname.setText(item.getNickName());
        rankingTextView.setText(item.getRanking());

    }

}
