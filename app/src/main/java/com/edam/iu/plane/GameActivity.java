package com.edam.iu.plane;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.widget.CompoundButton;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.viewpager.widget.ViewPager;
import com.edam.iu.plane.game.GameView;
import com.edam.iu.plane.tools.StatusBarUtil;
import com.edam.iu.plane.viewpagercards.CardFragmentPagerAdapter;
import com.edam.iu.plane.viewpagercards.CardItem;
import com.edam.iu.plane.viewpagercards.CardPagerAdapter;
import com.edam.iu.plane.viewpagercards.ShadowTransformer;
import com.tapsdk.antiaddictionui.AntiAddictionUIKit;
import com.tapsdk.bootstrap.account.TDSUser;
import com.tapsdk.moment.TapMoment;
import com.tds.achievement.AchievementCallback;
import com.tds.achievement.AchievementException;
import com.tds.achievement.TapAchievement;
import com.tds.achievement.TapAchievementBean;

import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import cn.leancloud.LCLeaderboard;
import cn.leancloud.LCLeaderboardResult;
import cn.leancloud.LCRanking;
import cn.leancloud.LCStatistic;
import cn.leancloud.LCStatisticResult;
import io.reactivex.Observer;
import io.reactivex.disposables.Disposable;


public class GameActivity extends AppCompatActivity implements View.OnClickListener, CompoundButton.OnCheckedChangeListener{

    private static final String TAG = "LeeJiEun";
    private GameView gameView;
    private LinearLayout tapAchievement;
    private LinearLayout tapMoment;
    private LinearLayout tapCloudLeaderboard;
    private LinearLayout tapMenu4;
    private LinearLayout tapLogout;
    private SharedPreferences sp;
    private TDSUser currentUser;
    private ViewPager mViewPager;
    private CardPagerAdapter mCardAdapter;
    private ShadowTransformer mCardShadowTransformer;
    private CardFragmentPagerAdapter mFragmentCardAdapter;
    private ShadowTransformer mFragmentCardShadowTransformer;
    private ImageButton imageButton;
    private LinearLayout lvViewPager;
    private boolean mShowingFragments = false;
    List<LCRanking> rankings;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_game);

        initStatusBar();
        sp = getPreferences(Context.MODE_PRIVATE);
        gameView = (GameView)findViewById(R.id.gameView);
        View contentView = LayoutInflater.from(GameActivity.this).inflate(R.layout.pop_menu,null, false);
        tapMoment = (LinearLayout) contentView.findViewById(R.id.tapsdk_moment);
        tapMoment.setOnClickListener(this);
        tapAchievement = (LinearLayout) contentView.findViewById(R.id.tapsdk_achievement);
        tapAchievement.setOnClickListener(this);
        tapLogout = (LinearLayout) contentView.findViewById(R.id.tap_logout);
        tapLogout.setOnClickListener(this);
        tapCloudLeaderboard = (LinearLayout) contentView.findViewById(R.id.tapsdk_leaderboard);
        tapCloudLeaderboard.setOnClickListener(this);
        int[] bitmapIds = {
                R.drawable.plane,
                R.drawable.explosion,
                R.drawable.yellow_bullet,
                R.drawable.blue_bullet,
                R.drawable.small,
                R.drawable.middle,
                R.drawable.big,
                R.drawable.bomb_award,
                R.drawable.bullet_award,
                R.drawable.pause1,
                R.drawable.pause2,
                R.drawable.bomb,
                R.drawable.user_center
        };

        mViewPager = (ViewPager) findViewById(R.id.leadboard_vpager);
        imageButton = (ImageButton) findViewById(R.id.imagebutton);
        imageButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                lvViewPager.setVisibility(View.GONE);
                initStatusBar();
            }
        });
        lvViewPager = (LinearLayout) findViewById(R.id.lv_view_pager);
        lvViewPager.setVisibility(View.GONE);

        currentUser = TDSUser.currentUser();
        TapMoment.init(GameActivity.this, "FwFdCIr6u71WQDQwQN");
        // 注册动态回调监听
        TapMoment.setCallback(new TapMoment.TapMomentCallback() {
            @Override
            public void onCallback(int code, String msg) {
                Log.d(TAG, "内嵌动态：code: " + String.valueOf(code));
                Log.d(TAG, "内嵌动态： " + msg);
                if (code == TapMoment.CALLBACK_CODE_GET_NOTICE_SUCCESS) {
                    // 获取用户新消息成功
                    Toast.makeText(GameActivity.this, "获取新通知数据为： " + msg, Toast.LENGTH_SHORT).show();
                }
                if(code == TapMoment.CALLBACK_CODE_LOGIN_SUCCESS){
                    // 动态内登陆成功
                    Toast.makeText(GameActivity.this, "动态内登陆成功： " + msg, Toast.LENGTH_SHORT).show();
                }
                if(code == TapMoment.CALLBACK_CODE_PUBLISH_SUCCESS){
                    // 动态发布成功
                    Toast.makeText(GameActivity.this, "动态发布成功： " + msg, Toast.LENGTH_SHORT).show();
                }
            }
        });
        // 初始化成就数据
        TapAchievement.initData();
        TapAchievement.registerCallback(new AchievementCallback() {
            @Override
            public void onAchievementSDKInitSuccess() {
                // 数据加载成功
                Log.d(TAG, "数据加载成功！");
                Toast.makeText(GameActivity.this, "数据加载成功！", Toast.LENGTH_SHORT).show();
            }

            @Override
            public void onAchievementSDKInitFail(AchievementException exception) {
                // 数据加载失败，请重试
                Log.d(TAG, "数据加载失败： " + exception.toString());
                Toast.makeText(GameActivity.this, "数据加载失败： " + exception.toString(), Toast.LENGTH_SHORT).show();
            }

            @Override
            public void onAchievementStatusUpdate(TapAchievementBean item, AchievementException exception) {
                if (exception != null) {
                    // 成就更新失败
                    Log.d(TAG, "成就更新失败: " + exception.toString());
                    Toast.makeText(GameActivity.this, "成就更新失败: " + exception.toString(), Toast.LENGTH_SHORT).show();
                    return;
                }
                if (item != null) {
                    // item 更新成功
                    Log.d(TAG, "成就更新成功");
                    Toast.makeText(GameActivity.this, "成就更新成功", Toast.LENGTH_SHORT).show();
                }
            }
        });

        gameView.start(bitmapIds, GameActivity.this, contentView);
    }

    @Override
    protected void onResume() {
        super.onResume();
    }

    @Override
    protected void onPause() {
        super.onPause();
        if(gameView != null){
            gameView.pause();
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if(gameView != null){
            gameView.destroy();
        }
        gameView = null;
    }


    // 再按一次退出程序(如果两次按退出按钮的时间间隔小于2秒就执行退出操作)
    private long exitTime = 0;

    @Override
    public void onBackPressed() {
        if((System.currentTimeMillis() - exitTime) > 2000){
            Toast.makeText(this, "再按一次退出程序", Toast.LENGTH_SHORT).show();
            exitTime = System.currentTimeMillis();
        } else {
            super.onBackPressed();
        }
    }

    @Override
    public void onClick(View view) {
        switch (view.getId()){
            case R.id.tapsdk_moment:
                // 场景化入口
                tapDirectlyOpen();
                gameView.dismissPopupWindow();
                Toast.makeText(GameActivity.this, "内嵌动态", Toast.LENGTH_SHORT).show();
                break;
            case R.id.tapsdk_achievement:
                // 展示成就系统
                tapShowAchiene();
                gameView.dismissPopupWindow();
                Toast.makeText(GameActivity.this, "成就系统", Toast.LENGTH_SHORT).show();
                break;
            case R.id.tapsdk_leaderboard:
                // 分数排行榜
                tapShowCloudLeaderboard();
                getCloudLeaderboard();
                lvViewPager.setVisibility(View.VISIBLE);
                lvViewPager.setClickable(false);
                lvViewPager.setOnTouchListener(new View.OnTouchListener() {
                    @Override
                    public boolean onTouch(View view, MotionEvent motionEvent) {
                        return true;
                    }
                });
                StatusBarUtil.setStatusBarColor(this, 0x55000000);
                gameView.dismissPopupWindow();
                break;
            case R.id.tap_logout:
                // 退出登陆
                logout();
                gameView.dismissPopupWindow();
                break;
        }
    }

    public void submitCloudLeaderboard(String score){
        Map<String, Double> statistic  = new HashMap<>();
        statistic.put("score_leejieun", Double.parseDouble(score));
        LCLeaderboard.updateStatistic(currentUser, statistic).subscribe(new Observer<LCStatisticResult>() {

            @Override
            public void onSubscribe(@NonNull Disposable d) {

            }

            @Override
            public void onNext(@NonNull LCStatisticResult lcStatisticResult) {
                Toast.makeText(GameActivity.this, "分数上传成功", Toast.LENGTH_SHORT).show();
            }

            @Override
            public void onError(@NonNull Throwable e) {

            }

            @Override
            public void onComplete() {

            }
        });
    }

    public void getCloudLeaderboard(){
        LCLeaderboard leaderboard = LCLeaderboard.createWithoutData("score_leejieun");
        List<String> selectKeys = new ArrayList<>();
        selectKeys.add("nickname");
        selectKeys.add("avatar");
        leaderboard.getResults(0, 10, selectKeys, null).subscribe(new Observer<LCLeaderboardResult>() {
            @Override
            public void onSubscribe(@NotNull Disposable disposable) {}

            @Override
            public void onNext(@NotNull LCLeaderboardResult leaderboardResult) {
                mCardAdapter = new CardPagerAdapter(GameActivity.this);
                rankings = leaderboardResult.getResults();
                // process rankings
                int i = 0;
                for (LCRanking r:rankings) {
                    Log.d(TAG+"RANK", String.valueOf(r.getRank()));
                    Log.d(TAG+"RANK", String.valueOf(r.getUser().getObjectId()));
                    // 下面两行代码行不通
                    Log.d(TAG+"RANK", String.valueOf(r.getUser().get("nickname")));
                    Log.d(TAG+"RANK", String.valueOf(r.getUser().get("avatar")));
                    i ++;
                    if(i == 1){
                        mCardAdapter.addCardItem(new CardItem(r.getUser().get("avatar").toString(), r.getUser().get("nickname").toString(), String.valueOf(i), true));
                    }else {
                        mCardAdapter.addCardItem(new CardItem(r.getUser().get("avatar").toString(), r.getUser().get("nickname").toString(), String.valueOf(i), false));
                    }

                }
            }

            @Override
            public void onError(@NotNull Throwable throwable) {
                // handle error
            }

            @Override
            public void onComplete() {
                mFragmentCardAdapter = new CardFragmentPagerAdapter(getSupportFragmentManager(),
                        dpToPixels(2, GameActivity.this));

                mCardShadowTransformer = new ShadowTransformer(mViewPager, mCardAdapter);
                mFragmentCardShadowTransformer = new ShadowTransformer(mViewPager, mFragmentCardAdapter);

                mViewPager.setAdapter(mCardAdapter);
                mViewPager.setPageTransformer(false, mCardShadowTransformer);
                mViewPager.setOffscreenPageLimit(3);
                mCardAdapter = null;
            }
        });
    }

    public void getCurrentUserLeaderboard(){
        // 查询排行榜成员成绩
        LCLeaderboard.getUserStatistics(currentUser).subscribe(new Observer<LCStatisticResult>() {
            @Override
            public void onSubscribe(@NotNull Disposable disposable) {}

            @Override
            public void onNext(@NotNull LCStatisticResult lcStatisticResult) {
                List<LCStatistic> statistics = lcStatisticResult.getResults();
                for (LCStatistic statistic : statistics) {
                    Log.d(TAG, statistic.getName());
                    Log.d(TAG, String.valueOf(statistic.getValue()));
                }
            }

            @Override
            public void onError(@NotNull Throwable throwable) {
                // handle error
                Toast.makeText(GameActivity.this, "查询失败： " + throwable.getMessage(), Toast.LENGTH_SHORT).show();
            }

            @Override
            public void onComplete() {}
        });
    }

    private void tapShowCloudLeaderboard() {


    }

    private void tapShowAchiene() {
        TapAchievement.showAchievementPage();
    }

    private void tapDirectlyOpen() {
//        Map<String, String> extras = new HashMap<>();
//        // 注意：这里的 key 是固定的，"scene_id"； 第二个参数：开发者后台开启场景化入口并配置相关项后可以得到
//        extras.put("scene_id", "LeeJiEun");
//        // 注意：第二个参数固定为 "tap://moment/scene/"
//        TapMoment.directlyOpen(TapMoment.ORIENTATION_DEFAULT, "tap://moment/scene/", extras);
        TapMoment.open(TapMoment.ORIENTATION_DEFAULT);
    }

    private void logout (){
        TDSUser.logOut();
        AntiAddictionUIKit.logout();
    }

    public static float dpToPixels(int dp, GameActivity context) {
        return dp * (context.getResources().getDisplayMetrics().density);
    }

    @Override
    public void onCheckedChanged(CompoundButton compoundButton, boolean b) {
        mCardShadowTransformer.enableScaling(b);
        mFragmentCardShadowTransformer.enableScaling(b);
    }

    private void initStatusBar() {
        //沉浸式代码配置
        //当FitsSystemWindows设置 true 时，会在屏幕最上方预留出状态栏高度的 padding
        StatusBarUtil.setRootViewFitsSystemWindows(this, true);
        //设置状态栏透明
        StatusBarUtil.setTranslucentStatus(this);
        //一般的手机的状态栏文字和图标都是白色的, 可如果你的应用也是纯白色的, 或导致状态栏文字看不清
        //所以如果你是这种情况,请使用以下代码, 设置状态使用深色文字图标风格, 否则你可以选择性注释掉这个if内容
        if (!StatusBarUtil.setStatusBarDarkTheme(this, true)) {
            //如果不支持设置深色风格 为了兼容总不能让状态栏白白的看不清, 于是设置一个状态栏颜色为半透明,
            //这样半透明+白=灰, 状态栏的文字能看得清
            StatusBarUtil.setStatusBarColor(this, 0x55000000);
        }

    }

}