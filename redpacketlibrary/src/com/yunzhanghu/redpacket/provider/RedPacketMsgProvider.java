package com.yunzhanghu.redpacket.provider;

import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.support.v4.app.FragmentActivity;
import android.text.Spannable;
import android.text.SpannableString;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import android.widget.Toast;

import com.yunzhanghu.redpacket.R;
import com.yunzhanghu.redpacket.message.EmptyMessage;
import com.yunzhanghu.redpacket.message.NotificationMessage;
import com.yunzhanghu.redpacket.message.RedPacketMessage;
import com.yunzhanghu.redpacketsdk.RedPacket;
import com.yunzhanghu.redpacketsdk.bean.RedPacketInfo;
import com.yunzhanghu.redpacketsdk.constant.RPConstant;
import com.yunzhanghu.redpacketui.utils.RPRedPacketUtil;

import io.rong.imkit.RongContext;
import io.rong.imkit.RongIM;
import io.rong.imkit.model.ProviderTag;
import io.rong.imkit.model.UIMessage;
import io.rong.imkit.widget.ArraysDialogFragment;
import io.rong.imkit.widget.provider.IContainerItemProvider;
import io.rong.imlib.model.Conversation;
import io.rong.imlib.model.Message;


/**
 * 自定义红包消息展示模板
 *
 * @author desert
 * @date 2016-05-23
 */
// 会话界面自定义UI注解
@ProviderTag(messageContent = RedPacketMessage.class, showProgress = false)
public class RedPacketMsgProvider extends IContainerItemProvider.MessageProvider<RedPacketMessage> {

    private Context mContext;

    private RedPacketInfo redPacketInfo;

    private ProgressDialog progressDialog;

    private RedPacketMessage mContent;

    private UIMessage mMessage;


    public RedPacketMsgProvider() {
        super();
        this.mContext = RongContext.getInstance();
    }

    /**
     * RedPacketInfo初始化View
     */
    @Override
    public View newView(Context context, ViewGroup group) {
        View view = LayoutInflater.from(context).inflate(R.layout.yzh_red_packet_message, null);
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
    public void bindView(View v, int position, RedPacketMessage content, UIMessage message) {
        ViewHolder holder = (ViewHolder) v.getTag();

        // 更改气泡样式
        if (message.getMessageDirection() == Message.MessageDirection.SEND) {
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
    public Spannable getContentSummary(RedPacketMessage data) {
        if (data != null && !TextUtils.isEmpty(data.getMessage()) && !TextUtils.isEmpty(data.getSponsorName())) {
            return new SpannableString("[" + data.getSponsorName() + "]" + data.getMessage());
        }
        return null;
    }

    @Override
    public void onItemClick(View view, int position, final RedPacketMessage content, final UIMessage message) {
        mContent = content;
        mMessage = message;
        progressDialog = new ProgressDialog(mContext);
        //进度条风格开发者可以根据需求改变
        progressDialog.setProgressStyle(ProgressDialog.STYLE_SPINNER);
        progressDialog.setCanceledOnTouchOutside(false);
        //以下是打开红包所需要的参数
        redPacketInfo = new RedPacketInfo();
        redPacketInfo.redPacketId = content.getMoneyID();//获取红包id
        //判断发送方还是接收方
        if (message.getMessageDirection() == Message.MessageDirection.SEND) {
            redPacketInfo.messageDirect = RPConstant.MESSAGE_DIRECT_SEND;//发送者
        } else {
            redPacketInfo.messageDirect = RPConstant.MESSAGE_DIRECT_RECEIVE;//接受方
        }
        //获取聊天类型
        if (message.getConversationType() == Conversation.ConversationType.PRIVATE) {//单聊
            redPacketInfo.chatType = RPConstant.CHATTYPE_SINGLE;

        } else {//群聊
            redPacketInfo.chatType = RPConstant.CHATTYPE_GROUP;
        }
        openRedPacket();
    }

    @Override
    public void onItemLongClick(View view, int position, RedPacketMessage content, final UIMessage message) {

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

    public void sendAckMsg(RedPacketMessage content, UIMessage message) {
        RedPacketInfo currentUserSync = RedPacket.getInstance().getRPInitRedPacketCallback().initCurrentUserSync();
        String receiveID = currentUserSync.fromUserId;
        String receiveName = currentUserSync.fromNickName;
        NotificationMessage notificationMessage = NotificationMessage.obtain(content.getSendUserID(),
                content.getSendUserName(), receiveID, receiveName, "1");//回执消息
        final EmptyMessage emptyMessage = EmptyMessage.obtain(content.getSendUserID(),
                content.getSendUserName(), receiveID, receiveName, "1");//空消息
        //单聊回执消息,直接发送回执消息即可
        if (message.getConversationType() == Conversation.ConversationType.PRIVATE) {//单聊
            RongIM.getInstance().getRongIMClient().sendMessage(message.getConversationType(),
                    content.getSendUserID(), notificationMessage, null, null, null, null);
        } else {//群聊讨论组回执消息
            if (content.getSendUserID().equals(receiveID)) {//自己领取了自己的红包
                RongIM.getInstance().getRongIMClient().insertMessage(message.getConversationType(),
                        message.getTargetId(), receiveID, notificationMessage, null);
            } else {
                //1、接受者先向本地插入一条“你领取了XX的红包”，然后发送一条空消息（不在聊天界面展示），
                // 发送红包者收到消息之后，向本地插入一条“XX领取了你的红包”，
                // 2、如果接受者和发送者是一个人就直接向本地插入一条“你领取了自己的红包”
                RongIM.getInstance().getRongIMClient().sendMessage(message.getConversationType(),
                        message.getTargetId(), emptyMessage, null, null, null, null);
                RongIM.getInstance().getRongIMClient().insertMessage(message.getConversationType(),
                        message.getTargetId(), receiveID, notificationMessage, null);
            }
        }

    }

    public void openRedPacket() {
        //打开红包
        RPRedPacketUtil.getInstance().openRedPacket(redPacketInfo, (FragmentActivity) mContext, new RPRedPacketUtil.RPOpenPacketCallback() {
            @Override
            public void onSuccess(String senderId, String senderNickname, String myAmount) {
                //打开红包消息成功,然后发送回执消息例如"你领取了XX的红包"
                sendAckMsg(mContent, mMessage);
            }

            @Override
            public void showLoading() {
                progressDialog.show();
            }

            @Override
            public void hideLoading() {
                progressDialog.hide();
            }

            @Override
            public void onError(String errorCode, String errorMsg) {
                Toast.makeText(mContext, errorMsg, Toast.LENGTH_SHORT).show();

            }
        });
    }

    class ViewHolder {
        TextView greeting, sponsor, special;
        View view;
    }

}
