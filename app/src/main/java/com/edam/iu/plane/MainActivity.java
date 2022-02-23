package com.edam.iu.plane;

import androidx.appcompat.app.AppCompatActivity;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.view.View;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.webkit.WebView;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.edam.iu.plane.tools.StatusBarUtil;
import com.tapsdk.antiaddiction.config.AntiAddictionFunctionConfig;
import com.tapsdk.antiaddiction.constants.Constants;
import com.tapsdk.antiaddictionui.AntiAddictionUICallback;
import com.tapsdk.antiaddictionui.AntiAddictionUIKit;
import com.tapsdk.bootstrap.Callback;
import com.tapsdk.bootstrap.TapBootstrap;
import com.tapsdk.bootstrap.account.TDSUser;
import com.tapsdk.bootstrap.exceptions.TapError;
import com.taptap.sdk.TapLoginHelper;
import com.tds.common.entities.TapConfig;
import com.tds.common.entities.TapDBConfig;
import com.tds.common.models.TapRegionType;

import java.util.Map;

import cn.leancloud.LCLogger;
import cn.leancloud.LeanCloud;
import cn.leancloud.json.JSON;

public class MainActivity extends AppCompatActivity {
    private static final String TAG = "LeeJiEun";
    private TextView textView;
    private int count = 3;
    private Animation animation;
    private Button btnTapLogin;
    private Button btnInGame;
    private LinearLayout linearLayout;
    private String userID = "";
    TDSUser currentUser = null;
    String userIdentifier = "";
    private SharedPreferences sp;
    private SharedPreferences.Editor editor;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        sp = getPreferences(Context.MODE_PRIVATE);
        editor = sp.edit();

        initStatusBar();

        initView();

        initTapSDK();


    }

    private void initView() {
        textView = (TextView) findViewById(R.id.textView);
        btnTapLogin = (Button) findViewById(R.id.btn_tap_login);
        btnInGame = (Button) findViewById(R.id.btn_ingame);
        linearLayout = (LinearLayout) findViewById(R.id.ll_text);

        btnTapLogin.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                taptapLogin();
            }
        });

        btnInGame.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                // 判定是否认证过，因为不检查，TapTap 未实名认证，调转认证时杀死游戏进程，重新启动游戏判定已经登陆过，则直接进入游戏。
                // 这是逻辑上的 Bug。所以，这里需要处理下。
                // 方案一：sp 缓存用户唯一标识，这里不进入游戏，而是进行实名认证。认证通过后可以游戏则进入游戏
                // TODO
                userIdentifier = sp.getString("USERIDENTIFIER", "");
                tapAntiAddiction(userIdentifier);
            }
        });

        animation = AnimationUtils.loadAnimation(this, R.anim.countdown_animation);
        handler.sendEmptyMessageDelayed(0, 1000);
    }

    private void enterGame() {
        // 登陆成功后跳转
        Intent intent = new Intent(getApplicationContext(), GameActivity.class);
        startActivity(intent);
        finish();
    }

    private void initTapSDK() {
        LeanCloud.setLogLevel(LCLogger.Level.DEBUG);
        WebView.setWebContentsDebuggingEnabled(true);
        // 内建账户方式登陆 SDK 初始化
        // TapDB 初始化
        TapDBConfig tapDBConfig = new TapDBConfig();
        tapDBConfig.setEnable(true);
        tapDBConfig.setChannel("gameChannel");
        tapDBConfig.setGameVersion("gameVersion");
        // TapSDK 初始化
        TapConfig tapConfig = new TapConfig.Builder()
                .withAppContext(getApplicationContext())
                .withRegionType(TapRegionType.CN) // TapRegionType.CN: 国内  TapRegionType.IO: 国外
                // 自己账号 - 知恩
                .withClientId("FwFdCIr6u71WQDQwQN")
                .withClientToken("8zkWbrNMBXYtdg6GTyGy3FLRcIi1C5PuKjxwWAUe")
                .withServerUrl("https://fwfdcir6.cloud.tds1.tapapis.cn")
                .withTapDBConfig(tapDBConfig)
                .build();
        TapBootstrap.init(MainActivity.this, tapConfig);



        // Android SDK 的各接口第一个参数是当前 Activity，以下不再说明
        String gameIdentifier = "FwFdCIr6u71WQDQwQN";
        AntiAddictionFunctionConfig config = new AntiAddictionFunctionConfig.Builder()
                .enablePaymentLimit(true) // 是否启用消费限制功能
                .enableOnLineTimeLimit(true) // 是否启用时长限制功能
                .build();
        AntiAddictionUIKit.init(MainActivity.this, gameIdentifier, config,
                new AntiAddictionUICallback() {
                    @Override
                    public void onCallback(int code, Map<String, Object> extras) {
                        // 根据 code 不同提示玩家不同信息，详见下面的说明
                        if (null != extras) {
                            Log.d(TAG, extras.toString());
                            Log.d(TAG, String.valueOf(code));
                        }
                        switch (code) {
                            case Constants.ANTI_ADDICTION_CALLBACK_CODE.LOGIN_SUCCESS:
//                                Log.d(TAG, extras.toString());
                                Log.d(TAG, "防沉迷登陆成功");
                                Toast.makeText(MainActivity.this, "防沉迷认证成功，进入游戏", Toast.LENGTH_SHORT).show();
                                enterGame();
                                break;
                            case Constants.ANTI_ADDICTION_CALLBACK_CODE.LOGOUT:
//                                Log.d(TAG, extras.toString());
                                Log.d(TAG, "防沉迷的登出");
                                Toast.makeText(MainActivity.this, "防沉迷的登出", Toast.LENGTH_SHORT).show();
                                break;
                            case Constants.ANTI_ADDICTION_CALLBACK_CODE.NIGHT_STRICT:
                                Log.d(TAG, "防沉迷未成年玩家无法进行游戏");
                                Toast.makeText(MainActivity.this, "防沉迷未成年玩家无法进行游戏", Toast.LENGTH_SHORT).show();
                                break;
                            case Constants.ANTI_ADDICTION_CALLBACK_CODE.REAL_NAME_STOP:
                                Log.d(TAG, "防沉迷实名认证过程中点击了关闭实名窗");
                                Toast.makeText(MainActivity.this, "防沉迷实名认证过程中点击了关闭实名窗", Toast.LENGTH_SHORT).show();
//                                System.exit(0);
                                break;
                            case Constants.ANTI_ADDICTION_CALLBACK_CODE.SWITCH_ACCOUNT:
                                Log.d(TAG, "防沉迷实名认证过程中点击了切换账号按钮");
                                Toast.makeText(MainActivity.this, "防沉迷实名认证过程中点击了切换账号按钮", Toast.LENGTH_SHORT).show();
                                break;
                        }
                    }
                }
        );
    }

    private void tapAntiAddiction(String userIdentifier) {
        if (userIdentifier == "") {
            Toast.makeText(MainActivity.this, "用户唯一标识为空，检查 Tap 授权", Toast.LENGTH_SHORT).show();
            return;
        }
        AntiAddictionUIKit.startup(MainActivity.this, true, userIdentifier);
    }

    private void taptapLogin() {
        TDSUser.loginWithTapTap(MainActivity.this, new Callback<TDSUser>() {
            @Override
            public void onSuccess(TDSUser resultUser) {
                // 开发者可以调用 resultUser 的方法获取更多属性。
                userID = resultUser.getObjectId();

                String userName = resultUser.getUsername();
                String avatar = (String) resultUser.get("avatar");
                Log.d(TAG, "userID: " + userID);
                Log.d(TAG, "userName: " + userName);
                Log.d(TAG, "avatar: " + avatar);
                Map<String, Object> authData = (Map<String, Object>) resultUser.get("authData");
                Map<String, Object> taptapAuthData = (Map<String, Object>) authData.get("taptap");
                Log.d(TAG, "authData:" + JSON.toJSONString(authData));
                Map<String, Object> authDataResult = (Map<String, Object>) ((Map<String, Object>) resultUser.get("authData")).get("taptap");
                Log.d(TAG, "unionid:" + taptapAuthData.get("unionid").toString());
                Log.d(TAG, "openid:" + taptapAuthData.get("openid").toString());
                userIdentifier = taptapAuthData.get("openid").toString();
                editor.putString("USERIDENTIFIER", userIdentifier)
                        .commit();
                Toast.makeText(MainActivity.this, "succeed to login with Taptap.", Toast.LENGTH_SHORT).show();

                tapAntiAddiction(userIdentifier);
            }

            @Override
            public void onFail(TapError error) {
                Toast.makeText(MainActivity.this, error.getMessage(), Toast.LENGTH_SHORT).show();
                Log.d(TAG, error.detailMessage);
                Log.d(TAG, error.getMessage());
                Log.d(TAG, error.toJSON());
            }
        }, TapLoginHelper.SCOPE_PUBLIC_PROFILE);
    }

    private int getCount() {
        count--;
        if (count == 0) {
            Log.d(TAG, "******* handler *******");
            handler.removeMessages(0);
            linearLayout.setVisibility(View.GONE);
            currentUser = TDSUser.getCurrentUser();
            if (currentUser == null) {
                btnInGame.setVisibility(View.GONE);
                btnTapLogin.setVisibility(View.VISIBLE);
            } else {
                btnInGame.setVisibility(View.VISIBLE);
                btnTapLogin.setVisibility(View.GONE);
            }
        }
        return count;
    }

    @SuppressLint("HandlerLeak")
    private Handler handler = new Handler() {
        public void handleMessage(android.os.Message msg) {
            if (msg.what == 0) {
                textView.setText(getCount() + "");
                handler.sendEmptyMessageDelayed(0, 1000);
                animation.reset();
                textView.startAnimation(animation);
            }
        }

        ;
    };

    private void initStatusBar() {
        //沉浸式代码配置
        //当FitsSystemWindows设置 true 时，会在屏幕最上方预留出状态栏高度的 padding
        StatusBarUtil.setRootViewFitsSystemWindows(this, false);
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

    @Override
    protected void onDestroy() {
        super.onDestroy();
        handler.removeMessages(0);
    }

}