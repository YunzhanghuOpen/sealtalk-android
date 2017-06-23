package com.yunzhanghu.redpacket.provider;

import android.content.Context;
import android.text.Spannable;
import android.text.SpannableString;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.yunzhanghu.redpacket.R;
import com.yunzhanghu.redpacket.message.NotificationMessage;
import com.yunzhanghu.redpacketsdk.RedPacket;
import com.yunzhanghu.redpacketsdk.bean.RedPacketInfo;

import io.rong.imkit.RongContext;
import io.rong.imkit.model.ProviderTag;
import io.rong.imkit.model.UIMessage;
import io.rong.imkit.widget.provider.IContainerItemProvider;


/**
 * 自定义红包回执消息展示模板
 *
 * @author desert
 * @date 2016-05-22
 */
// 会话界面自定义UI注解
@ProviderTag(messageContent = NotificationMessage.class, showWarning = false, showPortrait = false, showProgress = false, showSummaryWithName = false, centerInHorizontal = true)
public class NotificationMsgProvider extends IContainerItemProvider.MessageProvider<NotificationMessage> {

    private Context mContext;

    public NotificationMsgProvider() {
        super();
        this.mContext = RongContext.getInstance();
    }

    /**
     * 初始化View
     */
    @Override
    public View newView(Context context, ViewGroup group) {
        View view = LayoutInflater.from(context).inflate(R.layout.yzh_notify_money_message, null);
        ViewHolder holder = new ViewHolder();
        holder.message = (TextView) view.findViewById(R.id.yzh_tv_money_msg);
        view.setTag(holder);
        return view;
    }

    @Override
    public void bindView(View v, int i, NotificationMessage content, UIMessage message) {
        ViewHolder holder = (ViewHolder) v.getTag();
        //群红包,自己领取了自己红包,显示"你领取了自己的红包"
        //单聊红包,自己不能领取自己的红包
        holder.message.setText(getMessage(content));
    }

    @Override
    public Spannable getContentSummary(NotificationMessage data) {
        if (data != null)
            return new SpannableString(getMessage(data));
        return null;
    }

    public String getMessage(NotificationMessage content) {
        String msgContent;
        if (TextUtils.isEmpty(content.getSendUserID()) || TextUtils.isEmpty(content.getReceiveUserID())) {
            return "";
        }
        if (content.getSendUserID().equals(content.getReceiveUserID())) {//自己领取了自己的红包
            msgContent = mContext.getString(R.string.yzh_receive_self_red_packet);
        } else {
            RedPacketInfo currentUserInfo = RedPacket.getInstance().getRPInitRedPacketCallback().initCurrentUserSync();
            String currentUserID = currentUserInfo.currentUserId;//发送者ID
            if (content.getReceiveUserID().equals(currentUserID)) {//接受红包者
                //你领取了XX红包
                msgContent = String.format(mContext.getString(R.string.yzh_you_receive_red_packet), content.getSendUserName());
            } else {//红包发送者
                //XX领取了你的红包
                msgContent = String.format(mContext.getString(R.string.yzh_other_receive_you_red_packet), content.getReceiveUserName());
            }
        }
        return msgContent;
    }

    @Override
    public void onItemClick(View view, int i, NotificationMessage notificationMessage, UIMessage uiMessage) {

    }

    @Override
    public void onItemLongClick(View view, int i, NotificationMessage notificationMessage, UIMessage uiMessage) {

    }

    class ViewHolder {
        TextView message;
    }

}
