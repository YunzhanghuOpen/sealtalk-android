package com.yunzhanghu.redpacketui.provider;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.graphics.drawable.Drawable;
import android.support.v4.app.Fragment;
import android.support.v4.content.ContextCompat;
import android.util.Log;

import com.yunzhanghu.redpacketsdk.bean.RedPacketInfo;
import com.yunzhanghu.redpacketsdk.constant.RPConstant;
import com.yunzhanghu.redpacketui.R;
import com.yunzhanghu.redpacketui.RedPacketUtil;
import com.yunzhanghu.redpacketui.message.RongRedPacketMessage;
import com.yunzhanghu.redpacketui.ui.activity.RPRedPacketActivity;

import io.rong.imkit.RongExtension;
import io.rong.imkit.RongIM;
import io.rong.imkit.plugin.IPluginModule;
import io.rong.imlib.RongIMClient;
import io.rong.imlib.model.Conversation;

/**
 * 自定义扩展栏红包提供者
 *
 * @author desert
 * @date 2016-05-17
 */
public class RongSingleRedPacketProvider implements IPluginModule {

    private static final String TAG = RongSingleRedPacketProvider.class.getSimpleName();

    private String mGreeting;//祝福语

    private String mSponsor;//厂商名字(XX红包)

    private Conversation.ConversationType mConversationType;

    private String mTargetId;

    private Context mContext;

    /**
     * 设置展示的图标
     *
     * @param context
     * @return
     */
    @Override
    public Drawable obtainDrawable(Context context) {
        return ContextCompat.getDrawable(context, R.drawable.yzh_chat_money_provider);
    }

    /**
     * 设置图标下的title
     *
     * @param context
     * @return
     */
    @Override
    public String obtainTitle(Context context) {
        return context.getString(R.string.red_packet);
    }

    @Override
    public void onClick(Fragment fragment, RongExtension rongExtension) {
        mContext = rongExtension.getContext();
        mConversationType = rongExtension.getConversationType();
        mTargetId = rongExtension.getTargetId();
        final Intent intent = new Intent(mContext, RPRedPacketActivity.class);
        final RedPacketInfo redPacketInfo = new RedPacketInfo();
        redPacketInfo.fromAvatarUrl = RedPacketUtil.getInstance().getUserAvatar();//发送者头像
        redPacketInfo.fromNickName = RedPacketUtil.getInstance().getUserName();//发送者名字
        redPacketInfo.toUserId = mTargetId; //接受者id
        redPacketInfo.chatType = RPConstant.CHATTYPE_SINGLE;//单聊
        //跳转到发红包界面
        intent.putExtra(RPConstant.EXTRA_RED_PACKET_INFO, redPacketInfo);
        intent.putExtra(RPConstant.EXTRA_TOKEN_DATA, RedPacketUtil.getInstance().getTokenData());
        rongExtension.startActivityForPluginResult(intent, RedPacketUtil.REQUEST_CODE_SEND_MONEY,this);
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        //接受返回的红包信息,并发送红包消息
        if (resultCode == Activity.RESULT_OK && data != null && requestCode == RedPacketUtil.REQUEST_CODE_SEND_MONEY) {
            mGreeting = data.getStringExtra(RPConstant.EXTRA_RED_PACKET_GREETING);//祝福语
            mSponsor = mContext.getString(R.string.sponsor_red_packet);//XX红包
            String moneyID = data.getStringExtra(RPConstant.EXTRA_RED_PACKET_ID);//红包ID
            String userId = RedPacketUtil.getInstance().getUserID();//发送者ID
            String userName = RedPacketUtil.getInstance().getUserName();//发送者名字
            RongRedPacketMessage message = RongRedPacketMessage.obtain(userId, userName,
                    mGreeting, moneyID, "1", mSponsor, "", "");
            Log.e(TAG, "--发送红包返回参数--" + "-moneyID-" + moneyID + "-greeting-" + mGreeting);
            //发送红包消息到聊天界面
            sendMessage(message);
        }
    }

    private void sendMessage(RongRedPacketMessage message) {
        if (RongIM.getInstance() != null && RongIM.getInstance().getRongIMClient() != null) {

            String pushContent = "[" + mSponsor + "]" + mGreeting;
            RongIM.getInstance().getRongIMClient().sendMessage(mConversationType,
                    mTargetId, message, pushContent, "", new RongIMClient.SendMessageCallback() {
                        @Override
                        public void onError(Integer integer, RongIMClient.ErrorCode errorCode) {
                            Log.e(TAG, "--onError--" + errorCode);
                        }

                        @Override
                        public void onSuccess(Integer integer) {
                            Log.e(TAG, "--onSuccess--" + integer);
                        }
                    }, null);
        }
    }

}
