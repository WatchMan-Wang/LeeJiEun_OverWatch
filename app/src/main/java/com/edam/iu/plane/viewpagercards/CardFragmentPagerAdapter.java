package com.edam.iu.plane.viewpagercards;

import android.view.View;

import androidx.cardview.widget.CardView;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentPagerAdapter;

import java.util.ArrayList;
import java.util.List;



public class CardFragmentPagerAdapter extends FragmentPagerAdapter implements CardAdapter {
    private List<CardFragment> mFragments;
    private float mBaseElevation;

    public CardFragmentPagerAdapter(androidx.fragment.app.FragmentManager supportFragmentManager, float baseElevation) {
        super(supportFragmentManager, (int) baseElevation);
        mFragments = new ArrayList<>();
        mBaseElevation = baseElevation;

        for(int i = 0; i< 5; i++){
            addCardFragment(new CardFragment());
        }
    }

    @Override
    public Fragment getItem(int i) {
        return null;
    }

    @Override
    public float getBaseElevation() {
        return mBaseElevation;
    }

    @Override
    public CardView getCardViewAt(int position) {
        return mFragments.get(position).getCardView();
    }

    @Override
    public int getCount() {
        return mFragments.size();
    }

    @Override
    public Object instantiateItem(View container, int position) {
        Object fragment = super.instantiateItem(container, position);
        mFragments.set(position, (CardFragment) fragment);
        return fragment;
    }

    public void addCardFragment(CardFragment fragment) {
        mFragments.add(fragment);
    }
}
