package com.yunzhanghu.redpacket.provider;

import android.content.Context;
import android.graphics.drawable.Drawable;
import android.os.Handler;
import android.os.HandlerThread;
import android.support.v4.app.FragmentActivity;
import android.support.v4.content.ContextCompat;
import android.text.TextUtils;
import android.view.View;

import com.yunzhanghu.redpacket.R;
import com.yunzhanghu.redpacket.message.RedPacketMessage;
import com.yunzhanghu.redpacketsdk.RPSendPacketCallback;
import com.yunzhanghu.redpacketsdk.RedPacket;
import com.yunzhanghu.redpacketsdk.bean.RedPacketInfo;
import com.yunzhanghu.redpacketsdk.constant.RPConstant;
import com.yunzhanghu.redpacketui.utils.RPRedPacketUtil;

import io.rong.imkit.RongContext;
import io.rong.imkit.RongIM;
import io.rong.imkit.widget.provider.InputProvider;
import io.rong.imlib.model.UserInfo;

/**
 * 自定义扩展栏红包提供者
 *
 * @author desert
 * @date 2016-05-17
 */
public class SingleRedPacketProvider extends InputProvider.ExtendProvider {

    private String mGreeting;//祝福语

    private String mSponsor;//厂商名字(XX红包)

    private HandlerThread mWorkThread;

    private Handler mUploadHandler;

    private Context mContext;

    public SingleRedPacketProvider(RongContext context) {
        super(context);
        mWorkThread = new HandlerThread("YZHRedPacket");
        mWorkThread.start();
        mUploadHandler = new Handler(mWorkThread.getLooper());
    }

    /**
     * 设置展示的图标
     *
     * @param context
     * @return
     */
    @Override
    public Drawable obtainPluginDrawable(Context context) {
        mContext=context;
        return ContextCompat.getDrawable(context, R.drawable.yzh_chat_money_provider);
    }

    /**
     * 设置图标下的title
     *
     * @param context
     * @return
     */
    @Override
    public CharSequence obtainPluginTitle(Context context) {
        mContext=context;
        return context.getString(R.string.red_packet);
    }

    /**
     * click 事件，在这里做跳转
     *
     * @param view
     */
    @Override
    public void onPluginClick(View view) {
        final RedPacketInfo redPacketInfo = new RedPacketInfo();
        String toUserId = getCurrentConversation().getTargetId();
        UserInfo userInfo = RongContext.getInstance().getUserInfoFromCache(toUserId);
        redPacketInfo.toUserId = toUserId; //接受者id
        if (userInfo != null) {
            redPacketInfo.toNickName = !TextUtils.isEmpty(userInfo.getName()) ? userInfo.getName() : toUserId;
            redPacketInfo.toNickName = !TextUtils.isEmpty(userInfo.getPortraitUri().toString()) ? userInfo.getPortraitUri().toString() : toUserId;
        }
        redPacketInfo.chatType = RPConstant.CHATTYPE_SINGLE;//单聊
        RPRedPacketUtil.getInstance().startRedPacket((FragmentActivity) mContext, RPConstant.RP_ITEM_TYPE_SINGLE, redPacketInfo, new RPSendPacketCallback() {
            @Override
            public void onSendPacketSuccess(RedPacketInfo redPacketInfo) {
                mGreeting = redPacketInfo.redPacketGreeting;//祝福语
                mSponsor = getContext().getString(R.string.sponsor_red_packet);//XX红包
                RedPacketInfo currentUserSync = RedPacket.getInstance().getRPInitRedPacketCallback().initCurrentUserSync();
                String userId = currentUserSync.fromUserId;//发送者ID
                String userName = currentUserSync.fromNickName;//发送者名字
                RedPacketMessage message = RedPacketMessage.obtain(userId, userName,
                        mGreeting, redPacketInfo.redPacketId, "1", mSponsor, redPacketInfo.redPacketType, redPacketInfo.toUserId);
                //发送红包消息到聊天界面
                mUploadHandler.post(new MyRunnable(message));
            }

            @Override
            public void onGenerateRedPacketId(String s) {

            }
        });
    }

    class MyRunnable implements Runnable {

        RedPacketMessage mMessage;

        public MyRunnable(RedPacketMessage message) {
            mMessage = message;
        }

        @Override
        public void run() {
            if (RongIM.getInstance() != null && RongIM.getInstance().getRongIMClient() != null) {

                String mPushContent = "[" + mSponsor + "]" + mGreeting;
                RongIM.getInstance().getRongIMClient().sendMessage(getCurrentConversation().getConversationType(),
                        getCurrentConversation().getTargetId(), mMessage, mPushContent, "", null, null);
            }

        }
    }

}
