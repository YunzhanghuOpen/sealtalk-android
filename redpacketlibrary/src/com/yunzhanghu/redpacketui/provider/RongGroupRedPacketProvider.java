package com.yunzhanghu.redpacketui.provider;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.graphics.drawable.Drawable;
import android.support.v4.app.Fragment;
import android.support.v4.content.ContextCompat;
import android.util.Log;
import android.widget.Toast;

import com.yunzhanghu.redpacketsdk.bean.RedPacketInfo;
import com.yunzhanghu.redpacketsdk.constant.RPConstant;
import com.yunzhanghu.redpacketui.R;
import com.yunzhanghu.redpacketui.RedPacketUtil;
import com.yunzhanghu.redpacketui.callback.GetGroupInfoCallback;
import com.yunzhanghu.redpacketui.callback.ToRedPacketActivity;
import com.yunzhanghu.redpacketui.message.RongRedPacketMessage;
import com.yunzhanghu.redpacketui.ui.activity.RPRedPacketActivity;

import io.rong.imkit.RongExtension;
import io.rong.imkit.RongIM;
import io.rong.imkit.plugin.IPluginModule;
import io.rong.imlib.RongIMClient;
import io.rong.imlib.model.Conversation;
import io.rong.imlib.model.Conversation.ConversationType;

/**
 * 自定义群/讨论组红包提供者
 *
 * @author desert
 * @date 2016-05-23
 */
public class RongGroupRedPacketProvider implements ToRedPacketActivity, IPluginModule {

    private static final String TAG = RongGroupRedPacketProvider.class.getSimpleName();

    private String mGreeting;//祝福语

    private String mSponsor;//厂商名字(XX红包)

    private GetGroupInfoCallback callback;

    private RedPacketInfo redPacketInfo;

    private ConversationType mConversationType;

    private String mTargetId;

    private Context mContext;

    private RongExtension mRongExtension;

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
        mRongExtension = rongExtension;
        redPacketInfo = new RedPacketInfo();
        redPacketInfo.fromAvatarUrl = RedPacketUtil.getInstance().getUserAvatar(); //发送者头像url
        redPacketInfo.fromNickName = RedPacketUtil.getInstance().getUserName();//发送者昵称 设置了昵称就传昵称 否则传id
        redPacketInfo.toGroupId = rongExtension.getTargetId();//群ID
        redPacketInfo.chatType = RPConstant.CHATTYPE_GROUP;//群聊、讨论组类型
        if (rongExtension.getConversationType() == Conversation.ConversationType.GROUP) {
            RedPacketUtil.getInstance().setChatType(RedPacketUtil.CHAT_GROUP);
        } else {
            RedPacketUtil.getInstance().setChatType(RedPacketUtil.CHAT_DISCUSSION);
        }

        if (callback != null) {
            callback.getGroupPersonNumber(redPacketInfo.toGroupId, this);
        } else {
            Toast.makeText(rongExtension.getContext(), "回调函数不能为空", Toast.LENGTH_SHORT).show();
        }
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {

        if (resultCode != Activity.RESULT_OK)
            return;
        //接受返回的红包信息,并发送红包消息
        if (data != null && requestCode == RedPacketUtil.REQUEST_CODE_SEND_MONEY) {
            mSponsor = mContext.getString(R.string.sponsor_red_packet);//XX红包
            mGreeting = data.getStringExtra(RPConstant.EXTRA_RED_PACKET_GREETING);//祝福语
            String moneyID = data.getStringExtra(RPConstant.EXTRA_RED_PACKET_ID);//红包ID
            String userId = RedPacketUtil.getInstance().getUserID();//发送者ID
            String userName = RedPacketUtil.getInstance().getUserName();//发送者名字
            String redPacketType = data.getStringExtra(RPConstant.EXTRA_RED_PACKET_TYPE);//群红包类型
            String specialReceiveId = data.getStringExtra(RPConstant.EXTRA_RED_PACKET_RECEIVER_ID);//专属红包接受者ID

            RongRedPacketMessage message = RongRedPacketMessage.obtain(userId, userName,
                    mGreeting, moneyID, "1", mSponsor, redPacketType, specialReceiveId);
            //发送红包消息到聊天界面
            sendMessage(message);
        }

    }

    /**
     * 跳转到发送红包界面
     *
     * @param number
     */
    @Override
    public void toRedPacketActivity(int number) {
        Intent intent = new Intent(mContext, RPRedPacketActivity.class);
        redPacketInfo.groupMemberCount = number;
        intent.putExtra(RPConstant.EXTRA_RED_PACKET_INFO, redPacketInfo);
        intent.putExtra(RPConstant.EXTRA_TOKEN_DATA, RedPacketUtil.getInstance().getTokenData());
        mRongExtension.startActivityForPluginResult(intent, RedPacketUtil.REQUEST_CODE_SEND_MONEY, this);
    }

    private void sendMessage(RongRedPacketMessage message) {
        if (RongIM.getInstance() != null && RongIM.getInstance().getRongIMClient() != null) {
            String mPushContent = "[" + mSponsor + "]" + mGreeting;
            RongIM.getInstance().getRongIMClient().sendMessage(mConversationType,
                    mTargetId, message, mPushContent, "", new RongIMClient.SendMessageCallback() {
                        @Override
                        public void onError(Integer integer, RongIMClient.ErrorCode errorCode) {
                            Log.e(TAG, "-----onError--" + errorCode);
                        }

                        @Override
                        public void onSuccess(Integer integer) {
                            Log.e(TAG, "-----onSuccess--" + integer);
                        }
                    }, null);
        }
    }

}
