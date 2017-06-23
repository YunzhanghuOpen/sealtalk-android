package com.yunzhanghu.redpacket.provider;

import android.content.Context;
import android.content.Intent;
import android.graphics.drawable.Drawable;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentActivity;
import android.support.v4.content.ContextCompat;
import android.widget.Toast;

import com.yunzhanghu.redpacket.R;
import com.yunzhanghu.redpacket.RedPacketUtil;
import com.yunzhanghu.redpacket.callback.GetGroupInfoCallback;
import com.yunzhanghu.redpacket.callback.ToRedPacketActivity;
import com.yunzhanghu.redpacket.message.RedPacketMessage;
import com.yunzhanghu.redpacketsdk.RPSendPacketCallback;
import com.yunzhanghu.redpacketsdk.RedPacket;
import com.yunzhanghu.redpacketsdk.bean.RedPacketInfo;
import com.yunzhanghu.redpacketsdk.constant.RPConstant;
import com.yunzhanghu.redpacketui.utils.RPRedPacketUtil;

import io.rong.imkit.RongExtension;
import io.rong.imkit.RongIM;
import io.rong.imkit.plugin.IPluginModule;
import io.rong.imlib.model.Conversation;

/**
 * 自定义群/讨论组红包提供者
 *
 * @author desert
 * @date 2016-05-23
 */
public class GroupRedPacketProvider implements ToRedPacketActivity, IPluginModule {

    private static final String TAG = GroupRedPacketProvider.class.getSimpleName();

    private String mGreeting;//祝福语

    private String mSponsor;//厂商名字(XX红包)

    private GetGroupInfoCallback callback;

    private RedPacketInfo redPacketInfo;


    private Context mContext;

    private Conversation.ConversationType mConversationType;

    private String mTargetId;


    public GroupRedPacketProvider(GetGroupInfoCallback callback) {
        this.callback = callback;
    }

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
     * click 事件，在这里做跳转
     *
     * @param fragment
     * @param rongExtension
     */
    @Override
    public void onClick(Fragment fragment, RongExtension rongExtension) {
        mContext = rongExtension.getContext();
        mConversationType = rongExtension.getConversationType();
        mTargetId = rongExtension.getTargetId();
        redPacketInfo = new RedPacketInfo();
        redPacketInfo.groupId = mTargetId;//群ID
        redPacketInfo.chatType = RPConstant.RP_ITEM_TYPE_GROUP;//群聊、讨论组类型
        if (mConversationType == Conversation.ConversationType.GROUP) {
            RedPacketUtil.getInstance().setChatType(RedPacketUtil.CHAT_GROUP);
        } else {
            RedPacketUtil.getInstance().setChatType(RedPacketUtil.CHAT_DISCUSSION);
        }

        if (callback != null) {
            callback.getGroupPersonNumber(redPacketInfo.groupId, this);
        } else {
            Toast.makeText(mContext, "回调函数不能为空", Toast.LENGTH_SHORT).show();
        }

    }

    @Override
    public void onActivityResult(int i, int i1, Intent intent) {

    }

    /**
     * 跳转到发送红包界面
     *
     * @param number
     */
    @Override
    public void toRedPacketActivity(int number) {
        redPacketInfo.groupMemberCount = number;
        RPRedPacketUtil.getInstance().startRedPacket((FragmentActivity) mContext, RPConstant.RP_ITEM_TYPE_GROUP, redPacketInfo, new RPSendPacketCallback() {
            @Override
            public void onSendPacketSuccess(RedPacketInfo redPacketInfo) {
                mGreeting = redPacketInfo.redPacketGreeting;//祝福语
                mSponsor = mContext.getString(R.string.sponsor_red_packet);//XX红包
                RedPacketInfo currentUserSync = RedPacket.getInstance().getRPInitRedPacketCallback().initCurrentUserSync();
                String userId = currentUserSync.currentUserId;//发送者ID
                String userName = currentUserSync.currentNickname;//发送者名字
                String redPacketType = redPacketInfo.redPacketType;//群红包类型
                String specialReceiveId = redPacketInfo.receiverId;//专属红包接受者ID
                RedPacketMessage message = RedPacketMessage.obtain(userId, userName,
                        redPacketInfo.redPacketGreeting, redPacketInfo.redPacketId, "1", mSponsor, redPacketType, specialReceiveId);
                //发送红包消息到聊天界面
                sendMessage(message);
            }

            @Override
            public void onGenerateRedPacketId(String s) {

            }
        });
    }

    private void sendMessage(RedPacketMessage message) {
        if (RongIM.getInstance() != null && RongIM.getInstance().getRongIMClient() != null) {
            String mPushContent = "[" + mSponsor + "]" + mGreeting;
            RongIM.getInstance().getRongIMClient().sendMessage(mConversationType,
                    mTargetId, message, mPushContent, "", null, null);
        }
    }

}
