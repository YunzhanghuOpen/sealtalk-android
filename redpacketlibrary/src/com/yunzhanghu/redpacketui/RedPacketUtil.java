package com.yunzhanghu.redpacketui;

import android.content.Context;
import android.content.Intent;
import android.text.TextUtils;

import com.android.volley.DefaultRetryPolicy;
import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.Volley;
import com.yunzhanghu.redpacketsdk.RPValueCallback;
import com.yunzhanghu.redpacketsdk.bean.RedPacketInfo;
import com.yunzhanghu.redpacketsdk.bean.TokenData;
import com.yunzhanghu.redpacketsdk.constant.RPConstant;
import com.yunzhanghu.redpacketui.callback.GetUserInfoCallback;
import com.yunzhanghu.redpacketui.message.RongEmptyMessage;
import com.yunzhanghu.redpacketui.message.RongNotificationMessage;
import com.yunzhanghu.redpacketui.message.RongRedPacketMessage;
import com.yunzhanghu.redpacketui.provider.RongNotificationMessageProvider;
import com.yunzhanghu.redpacketui.provider.RongRedPacketMessageProvider;
import com.yunzhanghu.redpacketui.ui.activity.RPChangeActivity;

import org.json.JSONObject;

import io.rong.imkit.RongIM;
import io.rong.imlib.model.Message;

/**
 * Created by desert on 16/5/29.
 */
public class RedPacketUtil implements Response.Listener<JSONObject>, Response.ErrorListener {

    public static final int REQUEST_CODE_SEND_MONEY = 15;

    public static final String CHAT_GROUP = "chat_group";

    public static final String CHAT_DISCUSSION = "chat_discussion";

    private String userName;

    private String userAvatar;

    private String userID;

    private String chatType;

    private TokenData mTokenData;

    private GetUserInfoCallback mGetUserInfoCallback;

    private RPValueCallback<TokenData> mRPValueCallback;

    private static RedPacketUtil mRedPacketUtil;

    private RedPacketUtil() {

    }

    public static RedPacketUtil getInstance() {
        if (mRedPacketUtil == null) {
            synchronized (RedPacketUtil.class) {
                if (mRedPacketUtil == null) {
                    mRedPacketUtil = new RedPacketUtil();
                }

            }
        }
        return mRedPacketUtil;
    }

    public TokenData getTokenData() {
        if (mTokenData == null) {
            mTokenData = new TokenData();
            mTokenData.appUserId = userID;
        }
        return mTokenData;
    }

    /**
     * 初始化用户信息
     *
     * @param userID     用户ID
     * @param userName   用户名字
     * @param userAvatar 用户头像
     */

    public void initUserInfo(String userID, String userName, String userAvatar) {
        this.userID = userID;
        this.userName = userName;
        this.userAvatar = userAvatar;
        if (TextUtils.isEmpty(userID)) {
            this.userID = "default";
        }
        if (TextUtils.isEmpty(userName)) {
            this.userName = "default";
        }
        if (TextUtils.isEmpty(userAvatar)) {
            this.userAvatar = "default";
        }
    }

    /**
     * 注册消息类型以及消息展示模板
     */
    public void registerMsgTypeAndTemplate(Context mContext) {
        RongIM.registerMessageType(RongRedPacketMessage.class);
        RongIM.registerMessageType(RongNotificationMessage.class);
        RongIM.registerMessageType(RongEmptyMessage.class);
        RongIM.registerMessageTemplate(new RongRedPacketMessageProvider(mContext));
        RongIM.registerMessageTemplate(new RongNotificationMessageProvider(mContext));
    }

    /**
     * 插入消息体
     *
     * @param message 消息类型
     */
    public void insertMessage(Message message) {
        RongEmptyMessage content = (RongEmptyMessage) message.getContent();
        userID = RongIM.getInstance().getCurrentUserId();
        RongNotificationMessage rongNotificationMessage = RongNotificationMessage.obtain(content.getSendUserID(), content.getSendUserName(), content.getReceiveUserID(), content.getReceiveUserName(), content.getIsOpenMoney());
        if (content.getSendUserID().equals(userID)) {//如果当前用户是发送红包者,插入一条"XX领取了你的红包"
            RongIM.getInstance().getRongIMClient().insertMessage(message.getConversationType(), message.getTargetId(), content.getReceiveUserID(), rongNotificationMessage, null);
        }
    }

    /**
     * 跳转到零钱页
     *
     * @param mContext
     */
    public void toChangeActivity(Context mContext) {
        Intent intent = new Intent(mContext, RPChangeActivity.class);
        RedPacketInfo redPacketInfo = new RedPacketInfo();
        redPacketInfo.fromNickName = userName;
        redPacketInfo.fromAvatarUrl = userAvatar;
        intent.putExtra(RPConstant.EXTRA_RED_PACKET_INFO, redPacketInfo);
        intent.putExtra(RPConstant.EXTRA_TOKEN_DATA, getTokenData());
        mContext.startActivity(intent);
    }

    public GetUserInfoCallback getGetUserInfoCallback() {
        return mGetUserInfoCallback;
    }

    public void setGetUserInfoCallback(GetUserInfoCallback mGetUserInfoCallback) {
        this.mGetUserInfoCallback = mGetUserInfoCallback;
    }

    public String getUserName() {
        return userName;
    }

    public void setUserName(String userName) {
        this.userName = userName;
    }

    public String getUserAvatar() {
        return userAvatar;
    }

    public void setUserAvatar(String userAvatar) {
        this.userAvatar = userAvatar;
    }

    public String getUserID() {
        return userID;
    }

    public void setUserID(String userID) {
        this.userID = userID;
    }

    public String getChatType() {
        return chatType;
    }

    public void setChatType(String chatType) {
        this.chatType = chatType;
    }

    public void requestSign(Context mContext, String url, final RPValueCallback<TokenData> rpValueCallback) {
        mRPValueCallback = rpValueCallback;
        RequestQueue mRequestQueue = Volley.newRequestQueue(mContext);
        JsonObjectRequest jsonObjectRequest = new JsonObjectRequest(Request.Method.GET, url, this, this);
        jsonObjectRequest.setRetryPolicy(new DefaultRetryPolicy(3000, 2, 2));
        mRequestQueue.add(jsonObjectRequest);
    }

    @Override
    public void onErrorResponse(VolleyError volleyError) {
        mRPValueCallback.onError(volleyError.getMessage(), volleyError.toString());
    }

    @Override
    public void onResponse(JSONObject jsonObject) {
        if (jsonObject != null && jsonObject.length() > 0) {
            String partner = jsonObject.optString("partner");
            String userId = jsonObject.optString("user_id");
            String timestamp = jsonObject.optString("timestamp");
            String sign = jsonObject.optString("sign");
            //保存红包Token
            if (mTokenData == null) {
                mTokenData = new TokenData();
            }
            mTokenData.authPartner = partner;
            mTokenData.appUserId = userId;
            mTokenData.timestamp = timestamp;
            mTokenData.authSign = sign;
            mRPValueCallback.onSuccess(mTokenData);
        } else {
            mRPValueCallback.onError("", "sign data is  null");
        }
    }
}
