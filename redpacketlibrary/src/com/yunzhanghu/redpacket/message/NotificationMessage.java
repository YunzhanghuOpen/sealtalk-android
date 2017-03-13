package com.yunzhanghu.redpacket.message;

import android.os.Parcel;
import android.util.Log;

import com.yunzhanghu.redpacketsdk.constant.RPConstant;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.UnsupportedEncodingException;

import io.rong.common.ParcelUtils;
import io.rong.imlib.MessageTag;
import io.rong.imlib.model.UserInfo;

/**
 * 自定义红包回执消息类
 *
 * @author desert
 * @date 2016-05-18
 * <p/>
 * MessageTag 中 flag 中参数的含义：
 * 1.NONE，空值，不表示任何意义.在会话列表不会显示出来。
 * 2.ISPERSISTED，消息需要被存储到消息历史记录。
 * 3.ISCOUNTED，消息需要被记入未读消息数。
 * <p/>
 * value：消息对象名称。
 * 请不要以 "RC:" 开头， "RC:" 为官方保留前缀。
 */

@MessageTag(value = "YZH:RedPacketAckMsg", flag = MessageTag.ISPERSISTED)
public class NotificationMessage extends io.rong.message.NotificationMessage {

    private String sendUserID;//发送红包者ID

    private String sendUserName;//发送红包这名字

    private String receiveUserID;//接受红包者Id

    private String receiveUserName;//接受红包者名字

    private String isOpenMoney;//是否打开红包(和ios保持统一需要的字段)


    public NotificationMessage() {
    }

    public static NotificationMessage obtain(String sendUserID, String sendUserName, String receiveUserID, String receiveUserName, String isOpenMoney) {
        NotificationMessage notificationMessage = new NotificationMessage();
        notificationMessage.sendUserID = sendUserID;
        notificationMessage.sendUserName = sendUserName;
        notificationMessage.receiveUserID = receiveUserID;
        notificationMessage.receiveUserName = receiveUserName;
        notificationMessage.isOpenMoney = isOpenMoney;
        return notificationMessage;
    }

    // 给消息赋值。
    public NotificationMessage(byte[] data) {

        try {
            String jsonStr = new String(data, "UTF-8");
            JSONObject jsonObj = new JSONObject(jsonStr);
            setSendUserID(jsonObj.getString(RPConstant.EXTRA_RED_PACKET_SENDER_ID));
            setSendUserName(jsonObj.getString(RPConstant.EXTRA_RED_PACKET_SENDER_NAME));
            setReceiveUserID(jsonObj.getString(RPConstant.EXTRA_RED_PACKET_RECEIVER_ID));
            setReceiveUserName(jsonObj.getString(RPConstant.EXTRA_RED_PACKET_RECEIVER_NAME));
            setIsOpenMoney(jsonObj.getString(RPConstant.MESSAGE_ATTR_IS_RED_PACKET_ACK_MESSAGE));
            if (jsonObj.has("user")) {
                setUserInfo(parseJsonToUserInfo(jsonObj.getJSONObject("user")));
            }
        } catch (JSONException e) {
            Log.e("JSONException", e.getMessage());
        } catch (UnsupportedEncodingException e1) {

        }
    }

    /**
     * 构造函数。
     *
     * @param in 初始化传入的 Parcel。
     */
    public NotificationMessage(Parcel in) {
        setSendUserID(ParcelUtils.readFromParcel(in));
        setSendUserName(ParcelUtils.readFromParcel(in));
        setReceiveUserID(ParcelUtils.readFromParcel(in));
        setReceiveUserName(ParcelUtils.readFromParcel(in));
        setIsOpenMoney(ParcelUtils.readFromParcel(in));

        setUserInfo(ParcelUtils.readFromParcel(in, UserInfo.class));
    }

    /**
     * 读取接口，目的是要从Parcel中构造一个实现了Parcelable的类的实例处理。
     */
    public static final Creator<NotificationMessage> CREATOR = new Creator<NotificationMessage>() {

        @Override
        public NotificationMessage createFromParcel(Parcel source) {
            return new NotificationMessage(source);
        }

        @Override
        public NotificationMessage[] newArray(int size) {
            return new NotificationMessage[size];
        }
    };

    /**
     * 描述了包含在 Parcelable 对象排列信息中的特殊对象的类型。
     *
     * @return 一个标志位，表明Parcelable对象特殊对象类型集合的排列。
     */
    @Override
    public int describeContents() {
        // TODO Auto-generated method stub
        return 0;
    }

    /**
     * 将类的数据写入外部提供的 Parcel 中。
     *
     * @param dest  对象被写入的 Parcel。
     * @param flags 对象如何被写入的附加标志。
     */
    @Override
    public void writeToParcel(Parcel dest, int flags) {
        // 这里可继续增加你消息的属性
        ParcelUtils.writeToParcel(dest, sendUserID);
        ParcelUtils.writeToParcel(dest, sendUserName);
        ParcelUtils.writeToParcel(dest, receiveUserID);
        ParcelUtils.writeToParcel(dest, receiveUserName);
        ParcelUtils.writeToParcel(dest, isOpenMoney);
        ParcelUtils.writeToParcel(dest, getUserInfo());

    }

    /**
     * 将消息属性封装成 json 串，再将 json 串转成 byte 数组，该方法会在发消息时调用
     */
    @Override
    public byte[] encode() {
        JSONObject jsonObj = new JSONObject();
        try {

            jsonObj.put(RPConstant.EXTRA_RED_PACKET_SENDER_ID, sendUserID);
            jsonObj.put(RPConstant.EXTRA_RED_PACKET_SENDER_NAME, sendUserName);
            jsonObj.put(RPConstant.EXTRA_RED_PACKET_RECEIVER_ID, receiveUserID);
            jsonObj.put(RPConstant.EXTRA_RED_PACKET_RECEIVER_NAME, receiveUserName);
            jsonObj.put(RPConstant.MESSAGE_ATTR_IS_RED_PACKET_ACK_MESSAGE, isOpenMoney);

            if (getJSONUserInfo() != null)
                jsonObj.putOpt("user", getJSONUserInfo());

        } catch (JSONException e) {
            Log.e("JSONException", e.getMessage());
        }

        try {
            return jsonObj.toString().getBytes("UTF-8");
        } catch (UnsupportedEncodingException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
        return null;
    }

    public String getSendUserID() {
        return sendUserID;
    }

    public void setSendUserID(String sendUserID) {
        this.sendUserID = sendUserID;
    }

    public String getSendUserName() {
        return sendUserName;
    }

    public void setSendUserName(String sendUserName) {
        this.sendUserName = sendUserName;
    }


    public String getReceiveUserID() {
        return receiveUserID;
    }

    public void setReceiveUserID(String receiveUserID) {
        this.receiveUserID = receiveUserID;
    }

    public String getReceiveUserName() {
        return receiveUserName;
    }

    public void setReceiveUserName(String receiveUserName) {
        this.receiveUserName = receiveUserName;
    }

    public String getIsOpenMoney() {
        return isOpenMoney;
    }

    public void setIsOpenMoney(String isOpenMoney) {
        this.isOpenMoney = isOpenMoney;
    }
}
