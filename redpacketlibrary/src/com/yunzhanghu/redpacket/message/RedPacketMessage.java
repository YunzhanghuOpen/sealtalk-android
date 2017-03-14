package com.yunzhanghu.redpacket.message;

import android.os.Parcel;
import android.util.Log;

import com.yunzhanghu.redpacketsdk.constant.RPConstant;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.UnsupportedEncodingException;

import io.rong.common.ParcelUtils;
import io.rong.imlib.MessageTag;
import io.rong.imlib.model.MessageContent;
import io.rong.imlib.model.UserInfo;

/**
 * 自定义红包消息类
 *
 * @author desert
 * @date 2016-05-19
 * <p/>
 * MessageTag 中 flag 中参数的含义：
 * 1.NONE，空值，不表示任何意义.在会话列表不会显示出来。
 * 2.ISPERSISTED，消息需要被存储到消息历史记录。
 * 3.ISCOUNTED，消息需要被记入未读消息数。
 * <p/>
 * value：消息对象名称。
 * 请不要以 "RC:" 开头， "RC:" 为官方保留前缀。
 */

@MessageTag(value = "YZH:RedPacketMsg", flag = MessageTag.ISPERSISTED | MessageTag.ISCOUNTED)
public class RedPacketMessage extends MessageContent {

    private String sendUserID;//红包发送者ID

    private String sendUserName;//红包发送者名字

    private String message;//红包祝福语

    private String moneyID;//红包ID

    private String isMoneyMsg;//是否属红包消息(和ios保持统一需要的字段)

    private String sponsorName;//什么红包(例如融云红包)

    private String redPacketType;//群红包类型专属红包/平均红包/随机红包

    private String specialReceivedID;//专属红包接受者ID

    public RedPacketMessage() {

    }

    public static RedPacketMessage obtain(String sendUserId, String sendUserName, String message,
                                          String moneyID, String isMoneyMsg, String sponsorName, String redPacketType, String specialReceivedID) {
        RedPacketMessage redPacketMessage = new RedPacketMessage();
        redPacketMessage.sendUserID = sendUserId;
        redPacketMessage.sendUserName = sendUserName;
        redPacketMessage.message = message;
        redPacketMessage.moneyID = moneyID;
        redPacketMessage.isMoneyMsg = isMoneyMsg;
        redPacketMessage.sponsorName = sponsorName;
        redPacketMessage.redPacketType = redPacketType;
        redPacketMessage.specialReceivedID = specialReceivedID;
        return redPacketMessage;
    }

    // 给消息赋值。
    public RedPacketMessage(byte[] data) {

        try {
            String jsonStr = new String(data, "UTF-8");
            JSONObject jsonObj = new JSONObject(jsonStr);
            setSendUserID(jsonObj.getString(RPConstant.EXTRA_RED_PACKET_SENDER_ID));
            setSendUserName(jsonObj.getString(RPConstant.EXTRA_RED_PACKET_SENDER_NAME));
            setMessage(jsonObj.getString(RPConstant.EXTRA_RED_PACKET_GREETING));
            setMoneyID(jsonObj.getString(RPConstant.EXTRA_RED_PACKET_ID));
            setIsMoneyMsg(jsonObj.getString(RPConstant.MESSAGE_ATTR_IS_RED_PACKET_MESSAGE));
            setSponsorName(jsonObj.getString(RPConstant.EXTRA_SPONSOR_NAME));
            setRedPacketType(jsonObj.getString(RPConstant.MESSAGE_ATTR_RED_PACKET_TYPE));
            setSpecialReceivedID(jsonObj.getString(RPConstant.MESSAGE_ATTR_SPECIAL_RECEIVER_ID));
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
    public RedPacketMessage(Parcel in) {
        setSendUserID(ParcelUtils.readFromParcel(in));
        setSendUserName(ParcelUtils.readFromParcel(in));
        setMessage(ParcelUtils.readFromParcel(in));
        setMoneyID(ParcelUtils.readFromParcel(in));
        setIsMoneyMsg(ParcelUtils.readFromParcel(in));
        setSponsorName(ParcelUtils.readFromParcel(in));
        setRedPacketType(ParcelUtils.readFromParcel(in));
        setSpecialReceivedID(ParcelUtils.readFromParcel(in));
        setUserInfo(ParcelUtils.readFromParcel(in, UserInfo.class));
    }

    /**
     * 读取接口，目的是要从Parcel中构造一个实现了Parcelable的类的实例处理。
     */
    public static final Creator<RedPacketMessage> CREATOR = new Creator<RedPacketMessage>() {

        @Override
        public RedPacketMessage createFromParcel(Parcel source) {
            return new RedPacketMessage(source);
        }

        @Override
        public RedPacketMessage[] newArray(int size) {
            return new RedPacketMessage[size];
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
        ParcelUtils.writeToParcel(dest, message);
        ParcelUtils.writeToParcel(dest, moneyID);
        ParcelUtils.writeToParcel(dest, isMoneyMsg);
        ParcelUtils.writeToParcel(dest, sponsorName);
        ParcelUtils.writeToParcel(dest, redPacketType);
        ParcelUtils.writeToParcel(dest, specialReceivedID);
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
            jsonObj.put(RPConstant.EXTRA_RED_PACKET_GREETING, message);
            jsonObj.put(RPConstant.EXTRA_RED_PACKET_ID, moneyID);
            jsonObj.put(RPConstant.MESSAGE_ATTR_IS_RED_PACKET_MESSAGE, isMoneyMsg);
            jsonObj.put(RPConstant.EXTRA_SPONSOR_NAME, sponsorName);
            jsonObj.put(RPConstant.MESSAGE_ATTR_RED_PACKET_TYPE, redPacketType);
            jsonObj.put(RPConstant.MESSAGE_ATTR_SPECIAL_RECEIVER_ID, specialReceivedID);

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

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public String getMoneyID() {
        return moneyID;
    }

    public void setMoneyID(String moneyID) {
        this.moneyID = moneyID;
    }

    public String getIsMoneyMsg() {
        return isMoneyMsg;
    }

    public void setIsMoneyMsg(String isMoneyMsg) {
        this.isMoneyMsg = isMoneyMsg;
    }

    public String getSponsorName() {
        return sponsorName;
    }

    public void setSponsorName(String sponsorName) {
        this.sponsorName = sponsorName;
    }

    public String getRedPacketType() {
        return redPacketType;
    }

    public void setRedPacketType(String redPacketType) {
        this.redPacketType = redPacketType;
    }

    public String getSpecialReceivedID() {
        return specialReceivedID;
    }

    public void setSpecialReceivedID(String specialReceivedID) {
        this.specialReceivedID = specialReceivedID;
    }
}
