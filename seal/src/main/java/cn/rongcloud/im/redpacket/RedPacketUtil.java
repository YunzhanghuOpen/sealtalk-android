package cn.rongcloud.im.redpacket;

import android.content.Context;

import com.android.volley.DefaultRetryPolicy;
import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.Volley;
import com.yunzhanghu.redpacketsdk.RPValueCallback;
import com.yunzhanghu.redpacketsdk.bean.TokenData;

import org.json.JSONObject;

import io.rong.imkit.RongIM;
import io.rong.imlib.model.Message;

/**
 * Created by desert on 16/5/29.
 */
public class RedPacketUtil implements Response.Listener<JSONObject>, Response.ErrorListener {

    public static final String CHAT_GROUP = "chat_group";

    public static final String CHAT_DISCUSSION = "chat_discussion";

    private String chatType;

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

    /**
     * 插入消息体
     *
     * @param message 消息类型
     */
    public void insertMessage(Message message) {
        EmptyMessage content = (EmptyMessage) message.getContent();
        String userID = RongIM.getInstance().getCurrentUserId();
        NotificationMessage notificationMessage = NotificationMessage.obtain(content.getSendUserID(), content.getSendUserName(), content.getReceiveUserID(), content.getReceiveUserName(), content.getIsOpenMoney());
        if (content.getSendUserID().equals(userID)) {//如果当前用户是发送红包者,插入一条"XX领取了你的红包"
            RongIM.getInstance().getRongIMClient().insertMessage(message.getConversationType(), message.getTargetId(), content.getReceiveUserID(), notificationMessage, null);
        }
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
            //@param partner      商户代码 (联系云账户后端获取)
            //@param userId       商户用户id
            //@param timestamp    签名使用的时间戳
            //@param sign         签名
            String partner = jsonObject.optString("partner");
            String userId = jsonObject.optString("user_id");
            String timestamp = jsonObject.optString("timestamp");
            String sign = jsonObject.optString("sign");
            //保存红包Token
            TokenData mTokenData = new TokenData();
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
