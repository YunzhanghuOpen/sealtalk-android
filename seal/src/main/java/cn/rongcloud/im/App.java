package cn.rongcloud.im;

import android.app.Application;
import android.content.Context;
import android.support.multidex.MultiDex;

import com.nostra13.universalimageloader.cache.disc.naming.Md5FileNameGenerator;
import com.nostra13.universalimageloader.core.DisplayImageOptions;
import com.nostra13.universalimageloader.core.ImageLoader;
import com.nostra13.universalimageloader.core.ImageLoaderConfiguration;
import com.nostra13.universalimageloader.core.display.FadeInBitmapDisplayer;
import com.yunzhanghu.redpacketsdk.RedPacket;
import com.yunzhanghu.redpacketsdk.constant.RPConstant;
import com.yunzhanghu.redpacketui.RedPacketUtil;

import cn.rongcloud.im.message.provider.ContactNotificationMessageProvider;
import cn.rongcloud.im.message.provider.GroupNotificationMessageProvider;
import cn.rongcloud.im.message.provider.RealTimeLocationMessageProvider;
import cn.rongcloud.im.server.utils.NLog;
import cn.rongcloud.im.utils.SharedPreferencesContext;
import io.rong.imkit.RongIM;
import io.rong.imkit.widget.provider.FileMessageItemProvider;
import io.rong.imlib.ipc.RongExceptionHandler;
import io.rong.message.FileMessage;
import io.rong.message.GroupNotificationMessage;
import io.rong.push.RongPushClient;
import io.rong.push.common.RongException;



public class App extends Application {

    private static DisplayImageOptions options;

    @Override
    public void onCreate() {

        super.onCreate();


        RongPushClient.registerHWPush(this);
        RongPushClient.registerMiPush(this, "2882303761517473625", "5451747338625");
        try {
            RongPushClient.registerGCM(this);
        } catch (RongException e) {
            e.printStackTrace();
        }
        /**
         * 注意：
         *
         * IMKit SDK调用第一步 初始化
         *
         * context上下文
         *
         * 只有两个进程需要初始化，主进程和 push 进程
         */
        //RongIM.setServerInfo("nav.cn.ronghub.com", "img.cn.ronghub.com");
        RongIM.init(this);
        NLog.setDebug(true);//Seal Module Log 开关
        SealAppContext.init(this);
        SharedPreferencesContext.init(this);
        Thread.setDefaultUncaughtExceptionHandler(new RongExceptionHandler(this));

        try {
            //注册红包消息、回执消息类以及消息展示模板
            RedPacketUtil.getInstance().registerMsgTypeAndTemplate(this);
            RongIM.registerMessageType(GroupNotificationMessage.class);
            RongIM.registerMessageType(FileMessage.class);
            RongIM.registerMessageTemplate(new ContactNotificationMessageProvider());
            RongIM.registerMessageTemplate(new RealTimeLocationMessageProvider());
            RongIM.registerMessageTemplate(new GroupNotificationMessageProvider());
            RongIM.registerMessageTemplate(new FileMessageItemProvider());
        } catch (Exception e) {
            e.printStackTrace();
        }

        options = new DisplayImageOptions.Builder()
        .showImageForEmptyUri(R.drawable.de_default_portrait)
        .showImageOnFail(R.drawable.de_default_portrait)
        .showImageOnLoading(R.drawable.de_default_portrait)
        .displayer(new FadeInBitmapDisplayer(300))
        .cacheInMemory(true)
        .cacheOnDisk(true)
        .build();

        //初始化图片下载组件
        ImageLoaderConfiguration config = new ImageLoaderConfiguration.Builder(getApplicationContext())
        .threadPriority(Thread.NORM_PRIORITY - 2)
        .denyCacheImageMultipleSizesInMemory()
        .diskCacheSize(50 * 1024 * 1024)
        .diskCacheFileCount(200)
        .diskCacheFileNameGenerator(new Md5FileNameGenerator())
        .defaultDisplayImageOptions(options)
        .build();

        //Initialize ImageLoader with configuration.
        ImageLoader.getInstance().init(config);
        //初始化红包上下文
        RedPacket.getInstance().initContext(this, RPConstant.AUTH_METHOD_SIGN);
        RedPacket.getInstance().setDebugMode(true);
    }

    @Override
    protected void attachBaseContext(Context base) {
        super.attachBaseContext(base);
        MultiDex.install(this);
    }

    public static DisplayImageOptions getOptions() {
        return options;
    }

}
