package com.yunzhanghu.redpacketui.provider;

import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.support.v4.app.FragmentActivity;
import android.text.Spannable;
import android.text.SpannableString;
import android.text.TextUtils;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import android.widget.Toast;

import com.yunzhanghu.redpacketsdk.bean.RedPacketInfo;
import com.yunzhanghu.redpacketsdk.constant.RPConstant;
import com.yunzhanghu.redpacketui.R;
import com.yunzhanghu.redpacketui.RedPacketUtil;
import com.yunzhanghu.redpacketui.callback.SetUserInfoCallback;
import com.yunzhanghu.redpacketui.message.RongEmptyMessage;
import com.yunzhanghu.redpacketui.message.RongNotificationMessage;
import com.yunzhanghu.redpacketui.message.RongRedPacketMessage;
import com.yunzhanghu.redpacketui.utils.RPOpenPacketUtil;

import io.rong.imkit.RongIM;
import io.rong.imkit.model.ProviderTag;
import io.rong.imkit.model.UIMessage;
import io.rong.imkit.widget.ArraysDialogFragment;
import io.rong.imkit.widget.provider.IContainerItemProvider;
import io.rong.imlib.RongIMClient;
import io.rong.imlib.model.Conversation;


/**
 * 自定义红包消息展示模板
 *
 * @author desert
 * @date 2016-05-23
 */
// 会话界面自定义UI注解
@ProviderTag(messageContent = RongRedPacketMessage.class, showPortrait = true, showProgress = false, centerInHorizontal = false)
public class RongRedPacketMessageProvider extends IContainerItemProvider.MessageProvider<RongRedPacketMessage> implements SetUserInfoCallback {

    private static final String TAG = "RedPacketLibrary";

    private Context mContext;

    private RedPacketInfo redPacketInfo;

    private ProgressDialog progressDialog;

    private RongRedPacketMessage mContent;

    private UIMessage mMessage;


    public RongRedPacketMessageProvider(Context mContext) {
        super();
        this.mContext = mContext;
    }

    /**
     * RedPacketInfo初始化View
     */
    @Override
    public View newView(Context context, ViewGroup group) {
        View view = LayoutInflater.from(context).inflate(R.layout.yzh_customize_message_red_packet, null);
        ViewHolder holder = new ViewHolder();
        holder.greeting = (TextView) view.findViewById(R.id.tv_money_greeting);
        holder.sponsor = (TextView) view.findViewById(R.id.tv_sponsor_name);
        holder.special = (TextView) view.findViewById(R.id.tv_packet_type);
        holder.view = view.findViewById(R.id.bubble);
        view.setTag(holder);
        this.mContext = context;
        return view;
    }

    @Override
    public void bindView(View v, int position, RongRedPacketMessage content, UIMessage message) {
        ViewHolder holder = (ViewHolder) v.getTag();

        // 更改气泡样式
        if (message.getMessageDirection() == UIMessage.MessageDirection.SEND) {
            // 消息方向，自己发送的
            holder.view.setBackgroundResource(R.drawable.yzh_money_chat_to_bg);
        } else {
            // 消息方向，别人发送的
            holder.view.setBackgroundResource(R.drawable.yzh_money_chat_from_bg);
        }
        holder.greeting.setText(content.getMessage()); // 设置问候语
        holder.sponsor.setText(content.getSponsorName()); // 设置赞助商
        if (!TextUtils.isEmpty(content.getRedPacketType())//专属红包
                && content.getRedPacketType().equals(RPConstant.GROUP_RED_PACKET_TYPE_EXCLUSIVE)) {
            holder.special.setVisibility(View.VISIBLE);
            holder.special.setText(mContext.getString(R.string.special_red_packet));
        } else {
            holder.special.setVisibility(View.GONE);
        }
    }

    /**
     * 消息为该会话的最后一条消息时，会话列表要显示的内容
     *
     * @param data
     * @return
     */
    @Override
    public Spannable getContentSummary(RongRedPacketMessage data) {
        if (data != null && !TextUtils.isEmpty(data.getMessage()) && !TextUtils.isEmpty(data.getSponsorName())) {
            return new SpannableString("[" + data.getSponsorName() + "]" + data.getMessage());
        }
        return null;
    }

    @Override
    public void onItemClick(View view, int position, final RongRedPacketMessage content, final UIMessage message) {
        mContent = content;
        mMessage = message;
        progressDialog = new ProgressDialog(mContext);
        //进度条风格开发者可以根据需求改变
        progressDialog.setProgressStyle(ProgressDialog.STYLE_SPINNER);
        progressDialog.setCanceledOnTouchOutside(false);
        //以下是打开红包所需要的参数
        redPacketInfo = new RedPacketInfo();
        redPacketInfo.redPacketId = content.getMoneyID();//获取红包id
        redPacketInfo.toAvatarUrl = RedPacketUtil.getInstance().getUserAvatar();//获取打开红包者的名字
        redPacketInfo.toNickName = RedPacketUtil.getInstance().getUserName();//获取打开红包者的头像
        //判断发送方还是接收方
        if (message.getMessageDirection() == UIMessage.MessageDirection.SEND) {
            redPacketInfo.moneyMsgDirect = RPConstant.MESSAGE_DIRECT_SEND;//发送者
        } else {
            redPacketInfo.moneyMsgDirect = RPConstant.MESSAGE_DIRECT_RECEIVE;//接受方
        }
        //获取聊天类型
        if (message.getConversationType() == Conversation.ConversationType.PRIVATE) {//单聊
            redPacketInfo.chatType = RPConstant.CHATTYPE_SINGLE;

        } else {//群聊
            redPacketInfo.chatType = RPConstant.CHATTYPE_GROUP;
        }
        String redPacketType = content.getRedPacketType();
        //专属红包需要根据用户id获取用户信息
        if (!TextUtils.isEmpty(redPacketType) && redPacketType.
                equals(RPConstant.GROUP_RED_PACKET_TYPE_EXCLUSIVE)) {
            String specialReceiveId = content.getSpecialReceivedID();
            progressDialog.show();
            RedPacketUtil.getInstance().getGetUserInfoCallback().getUserInfo(specialReceiveId, this);
        } else {
            openRedPacket(false);
        }
    }

    @Override
    public void onItemLongClick(View view, int position, RongRedPacketMessage content, final UIMessage message) {

        String[] items;
        items = new String[]{view.getContext().getResources().getString(R.string.yzh_dialog_item_delete)};
        ArraysDialogFragment.newInstance("", items).setArraysDialogItemListener(
                new ArraysDialogFragment.OnArraysDialogItemListener() {
                    @Override
                    public void OnArraysDialogItemClick(DialogInterface dialog, int which) {
                        if (which == 0)
                            RongIM.getInstance().getRongIMClient().deleteMessages(new int[]{message.getMessageId()}, null);

                    }
                }).show(((FragmentActivity) view.getContext()).getSupportFragmentManager());
    }

    public void sendAckMsg(RongRedPacketMessage content, UIMessage message, String receiveName) {
        String receiveID = RedPacketUtil.getInstance().getUserID();
        RongNotificationMessage rongNotificationMessage = RongNotificationMessage.obtain(content.getSendUserID(),
                content.getSendUserName(), receiveID, receiveName, "1");//回执消息
        final RongEmptyMessage rongEmptyMessage = RongEmptyMessage.obtain(content.getSendUserID(),
                content.getSendUserName(), receiveID, receiveName, "1");//空消息
        //单聊回执消息,直接发送回执消息即可
        if (message.getConversationType() == Conversation.ConversationType.PRIVATE) {//单聊
            RongIM.getInstance().getRongIMClient().sendMessage(message.getConversationType(),
                    content.getSendUserID(), rongNotificationMessage, null, null, new RongIMClient.SendMessageCallback() {
                        @Override
                        public void onError(Integer integer, RongIMClient.ErrorCode errorCode) {
                            Log.e(TAG, "-单聊发送回执消息失败-");

                        }

                        @Override
                        public void onSuccess(Integer integer) {

                            Log.e(TAG, "-单聊发送回执消息成功-");
                        }
                    }, null);
        } else {//群聊讨论组回执消息
            if (content.getSendUserID().equals(receiveID)) {//自己领取了自己的红包
                RongIM.getInstance().getRongIMClient().insertMessage(message.getConversationType(),
                        message.getTargetId(), receiveID, rongNotificationMessage, null);
            } else {
                //1、接受者先向本地插入一条“你领取了XX的红包”，然后发送一条空消息（不在聊天界面展示），
                // 发送红包者收到消息之后，向本地插入一条“XX领取了你的红包”，
                // 2、如果接受者和发送者是一个人就直接向本地插入一条“你领取了自己的红包”
                RongIM.getInstance().getRongIMClient().sendMessage(message.getConversationType(),
                        message.getTargetId(), rongEmptyMessage, null, null, new RongIMClient.SendMessageCallback() {
                            @Override
                            public void onError(Integer integer, RongIMClient.ErrorCode errorCode) {
                                Log.e(TAG, "-发送空消息通知类失败-");

                            }

                            @Override
                            public void onSuccess(Integer integer) {

                                Log.e(TAG, "-发送空消息通知类成功-");
                            }
                        }, null);
                RongIM.getInstance().getRongIMClient().insertMessage(message.getConversationType(),
                        message.getTargetId(), receiveID, rongNotificationMessage, null);
            }
        }

    }

    public void openRedPacket(final boolean isSpecial) {
        //打开红包
        RPOpenPacketUtil.getInstance().openRedPacket(redPacketInfo,
                RedPacketUtil.getInstance().getTokenData(), (FragmentActivity) mContext,
                new RPOpenPacketUtil.RPOpenPacketCallBack() {
                    @Override
                    public void onSuccess(String senderId, String senderNickname, String myAmount) {
                        //打开红包消息成功,然后发送回执消息例如"你领取了XX的红包"
                        sendAckMsg(mContent, mMessage, RedPacketUtil.getInstance().getUserName());
                    }

                    @Override
                    public void showLoading() {
                        if (!isSpecial) {
                            progressDialog.show();
                        }

                    }

                    @Override
                    public void hideLoading() {
                        progressDialog.dismiss();
                    }

                    @Override
                    public void onError(String errorCode, String errorMsg) {
                        //错误处理
                        Toast.makeText(mContext, errorMsg, Toast.LENGTH_SHORT).show();
                    }
                });
    }

    @Override
    public void setUserInfo(String userName, String userAvatar) {
        redPacketInfo.toUserId = RedPacketUtil.getInstance().getUserID();
        redPacketInfo.specialNickname = userName;
        redPacketInfo.specialAvatarUrl = userAvatar;
        openRedPacket(true);
    }

    @Override
    public void UserInfoError(String msg) {
        Toast.makeText(mContext, msg, Toast.LENGTH_SHORT).show();
    }


    class ViewHolder {
        TextView greeting, sponsor, special;
        View view;
    }

}
