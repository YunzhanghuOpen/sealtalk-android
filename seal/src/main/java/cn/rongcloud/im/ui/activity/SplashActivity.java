package cn.rongcloud.im.ui.activity;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.text.TextUtils;

import cn.rongcloud.im.R;
import cn.rongcloud.im.server.utils.CommonUtils;
import cn.rongcloud.im.server.utils.NToast;
import io.rong.imkit.RongIM;
import io.rong.imlib.RongIMClient;

/**
 * Created by AMing on 16/8/5.
 * Company RongCloud
 */
public class SplashActivity extends BaseActivity {

    private SharedPreferences sp;

    private String cacheToken;

    private Context context;

    private android.os.Handler handler = new android.os.Handler();


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_splash);
        getSupportActionBar().hide();
        context = this;
        sp = getSharedPreferences("config", MODE_PRIVATE);

        if (!CommonUtils.isNetworkConnected(context)) {
            NToast.shortToast(context, getString(R.string.network_not_available));
            goToLogin();
            return;
        }

        cacheToken = sp.getString("loginToken", "");
        if (!TextUtils.isEmpty(cacheToken)) {
            if (RongIM.getInstance().getCurrentConnectionStatus() == RongIMClient.ConnectionStatusListener.ConnectionStatus.CONNECTED) {
                goToMain();
            } else {
                RongIM.connect(cacheToken, new RongIMClient.ConnectCallback() {
                    @Override
                    public void onTokenIncorrect() {
                        handler.post(new Runnable() {
                            @Override
                            public void run() {
                                goToLogin();
                            }
                        });
                    }

                    @Override
                    public void onSuccess(String s) {
                        handler.post(new Runnable() {
                            @Override
                            public void run() {
                                goToMain();
                            }
                        });
                    }

                    @Override
                    public void onError(final RongIMClient.ErrorCode e) {
                        handler.post(new Runnable() {
                            @Override
                            public void run() {
                                NToast.shortToast(context, "connect error value:" + e.getValue());
                                goToLogin();
                            }
                        });
                    }
                });
            }
        } else {
            goToLogin();
        }
    }


    private void goToMain() {
        startActivity(new Intent(context, MainActivity.class));
        finish();
    }

    private void goToLogin() {
        startActivity(new Intent(context, LoginActivity.class));
        finish();
    }
}
