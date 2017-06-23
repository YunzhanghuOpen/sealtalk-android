package com.yunzhanghu.redpacket.provider;

import android.content.Context;
import android.content.Intent;
import android.graphics.drawable.Drawable;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentActivity;
import android.support.v4.content.ContextCompat;
import android.text.TextUtils;

import com.yunzhanghu.redpacket.R;
import com.yunzhanghu.redpacket.message.RedPacketMessage;
import com.yunzhanghu.redpacketsdk.RPSendPacketCallback;
import com.yunzhanghu.redpacketsdk.RedPacket;
import com.yunzhanghu.redpacketsdk.bean.RedPacketInfo;
import com.yunzhanghu.redpacketsdk.constant.RPConstant;
import com.yunzhanghu.redpacketui.utils.RPRedPacketUtil;

import io.rong.imkit.RongContext;
import io.rong.imkit.RongExtension;
import io.rong.imkit.RongIM;
import io.rong.imkit.plugin.IPluginModule;
import io.rong.imlib.model.Conversation;
import io.rong.imlib.model.UserInfo;

/**
 * 自定义扩展栏红包提供者
 *
 * @author desert
 * @date 2016-05-17
 */
public class SingleRedPacketProvider implements IPluginModule {

    private static final String TAG = SingleRedPacketProvider.class.getSimpleName();

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
        mContext = context;
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
        mContext = context;
        return context.getString(R.string.red_packet);
    }

    /**
     * * click 事件，在这里做跳转
     *
     * @param fragment
     * @param rongExtension
     */
    @Override
    public void onClick(Fragment fragment, RongExtension rongExtension) {
        mContext = rongExtension.getContext();
        mConversationType = rongExtension.getConversationType();
        mTargetId = rongExtension.getTargetId();
        final RedPacketInfo redPacketInfo = new RedPacketInfo();
        String toUserId = mTargetId;
        UserInfo userInfo = RongContext.getInstance().getUserInfoFromCache(toUserId);
        redPacketInfo.receiverId = toUserId; //接受者id
        if (userInfo != null) {
            redPacketInfo.receiverNickname = !TextUtils.isEmpty(userInfo.getName()) ? userInfo.getName() : toUserId;
            redPacketInfo.receiverAvatarUrl= !TextUtils.isEmpty(userInfo.getPortraitUri().toString()) ? userInfo.getPortraitUri().toString() : "none";
        }
        redPacketInfo.chatType = RPConstant.CHAT_TYPE_SINGLE;//单聊
        RPRedPacketUtil.getInstance().startRedPacket((FragmentActivity) mContext, RPConstant.RP_ITEM_TYPE_SINGLE, redPacketInfo, new RPSendPacketCallback() {
            @Override
            public void onSendPacketSuccess(RedPacketInfo redPacketInfo) {
                mGreeting = redPacketInfo.redPacketGreeting;//祝福语
                mSponsor = mContext.getString(R.string.sponsor_red_packet);//XX红包
                RedPacketInfo currentUserSync = RedPacket.getInstance().getRPInitRedPacketCallback().initCurrentUserSync();
                String userId = currentUserSync.currentUserId;//发送者ID
                String userName = currentUserSync.currentNickname;//发送者名字
                RedPacketMessage message = RedPacketMessage.obtain(userId, userName,
                        mGreeting, redPacketInfo.redPacketId, "1", mSponsor, redPacketInfo.redPacketType, redPacketInfo.receiverId);
                //发送红包消息到聊天界面
                sendMessage(message);
            }

            @Override
            public void onGenerateRedPacketId(String s) {

            }
        });
    }

    @Override
    public void onActivityResult(int i, int i1, Intent intent) {

    }

    private void sendMessage(RedPacketMessage message) {
        if (RongIM.getInstance() != null && RongIM.getInstance().getRongIMClient() != null) {

            String pushContent = "[" + mSponsor + "]" + mGreeting;
            RongIM.getInstance().getRongIMClient().sendMessage(mConversationType,
                    mTargetId, message, pushContent, "", null, null);
        }
    }

}
