package cn.rongcloud.im;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.support.v4.app.FragmentActivity;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;

import com.yunzhanghu.redpacketsdk.bean.RPUserBean;
import com.yunzhanghu.redpacketui.RedPacketCache;
import com.yunzhanghu.redpacketui.RedPacketUtil;
import com.yunzhanghu.redpacketui.callback.GetGroupInfoCallback;
import com.yunzhanghu.redpacketui.callback.GetUserInfoCallback;
import com.yunzhanghu.redpacketui.callback.GroupMemberCallback;
import com.yunzhanghu.redpacketui.callback.NotifyGroupMemberCallback;
import com.yunzhanghu.redpacketui.callback.SetUserInfoCallback;
import com.yunzhanghu.redpacketui.callback.ToRedPacketActivity;
import com.yunzhanghu.redpacketui.message.RongEmptyMessage;
import com.yunzhanghu.redpacketui.provider.RongGroupRedPacketProvider;
import com.yunzhanghu.redpacketui.provider.RongRedPacketProvider;
import com.yunzhanghu.redpacketui.utils.RPGroupMemberUtil;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Stack;

import cn.rongcloud.im.db.DBManager;
import cn.rongcloud.im.db.Friend;
import cn.rongcloud.im.message.provider.RealTimeLocationInputProvider;
import cn.rongcloud.im.server.SealAction;
import cn.rongcloud.im.server.broadcast.BroadcastManager;
import cn.rongcloud.im.server.network.async.AsyncTaskManager;
import cn.rongcloud.im.server.network.async.OnDataListener;
import cn.rongcloud.im.server.network.http.HttpException;
import cn.rongcloud.im.server.response.ContactNotificationMessageData;
import cn.rongcloud.im.server.response.GetGroupMemberResponse;
import cn.rongcloud.im.server.response.GetUserInfoByIdResponse;
import cn.rongcloud.im.server.response.GetUserInfosResponse;
import cn.rongcloud.im.server.utils.NLog;
import cn.rongcloud.im.server.utils.NToast;
import cn.rongcloud.im.server.utils.RongGenerate;
import cn.rongcloud.im.server.utils.json.JsonMananger;
import cn.rongcloud.im.server.widget.LoadDialog;
import cn.rongcloud.im.ui.activity.AMAPLocationActivity;
import cn.rongcloud.im.ui.activity.NewFriendListActivity;
import cn.rongcloud.im.ui.activity.PersonalProfileActivity;
import cn.rongcloud.im.ui.activity.RealTimeLocationActivity;
import cn.rongcloud.im.utils.SharedPreferencesContext;
import io.rong.imkit.RongContext;
import io.rong.imkit.RongIM;
import io.rong.imkit.model.GroupUserInfo;
import io.rong.imkit.model.UIConversation;
import io.rong.imkit.widget.AlterDialogFragment;
import io.rong.imkit.widget.provider.FileInputProvider;
import io.rong.imkit.widget.provider.ImageInputProvider;
import io.rong.imkit.widget.provider.InputProvider;
import io.rong.imkit.widget.provider.LocationInputProvider;
import io.rong.imlib.RongIMClient;
import io.rong.imlib.location.RealTimeLocationConstant;
import io.rong.imlib.location.message.RealTimeLocationStartMessage;
import io.rong.imlib.model.Conversation;
import io.rong.imlib.model.Discussion;
import io.rong.imlib.model.Group;
import io.rong.imlib.model.Message;
import io.rong.imlib.model.MessageContent;
import io.rong.imlib.model.UserInfo;
import io.rong.message.ContactNotificationMessage;
import io.rong.message.GroupNotificationMessage;
import io.rong.message.ImageMessage;
import io.rong.message.LocationMessage;

/**
 * 融云相关监听 事件集合类
 * Created by AMing on 16/1/7.
 * Company RongCloud
 */
public class SealAppContext implements RongIM.ConversationListBehaviorListener, RongIMClient.OnReceiveMessageListener, RongIM.UserInfoProvider, RongIM.GroupInfoProvider, RongIM.GroupUserInfoProvider, RongIMClient.ConnectionStatusListener, RongIM.LocationProvider, RongIM.ConversationBehaviorListener, OnDataListener {

    public static final int REQUEST_SYNCGROUP = 101;
    public static final int REQUEST_GROUP_MEMBER = 102;
    public static final int REQUEST_USERINFO = 103;
    public static final int REQUEST_SYNCDISCUSSION = 104;
    public static final int REQUEST_DISCUSSION_MEMBER = 105;
    public static final String UPDATE_FRIEND = "update_friend";
    public static final String UPDATE_RED_DOT = "update_red_dot";
    private Context mContext;

    private static SealAppContext mRongCloudInstance;

    private RongIM.LocationProvider.LocationCallback mLastLocationCallback;

    private Stack<Map<String, Activity>> mActivityStack;

    private List<String> mIds;

    private SealAction action;

    private String mGroupId;

    private String mUserId;

    private RedPacketCache mRedPacketCache;

    private GroupMemberCallback mGroupMemberCallback;

    private SetUserInfoCallback mSetUserInfoCallback;

    public SealAppContext(Context mContext) {
        this.mContext = mContext;
        initListener();
        mActivityStack = new Stack<>();
        action = new SealAction(mContext);
        mRedPacketCache = RedPacketCache.get(mContext);
    }

    /**
     * 初始化 RongCloud.
     *
     * @param context 上下文。
     */
    public static void init(Context context) {

        if (mRongCloudInstance == null) {

            synchronized (SealAppContext.class) {

                if (mRongCloudInstance == null) {
                    mRongCloudInstance = new SealAppContext(context);
                }
            }
        }

    }

    public boolean pushActivity(Conversation.ConversationType conversationType, String targetId, Activity activity) {
        if (conversationType == null || targetId == null || activity == null)
            return false;

        String key = conversationType.getName() + targetId;
        Map<String, Activity> map = new HashMap<>();
        map.put(key, activity);
        mActivityStack.push(map);
        return true;
    }

    public boolean popActivity(Conversation.ConversationType conversationType, String targetId) {
        if (conversationType == null || targetId == null)
            return false;

        String key = conversationType.getName() + targetId;
        Map<String, Activity> map = mActivityStack.peek();
        if (map.containsKey(key)) {
            mActivityStack.pop();
            return true;
        }
        return false;
    }

    public boolean containsInQue(Conversation.ConversationType conversationType, String targetId) {
        if (conversationType == null || targetId == null)
            return false;

        String key = conversationType.getName() + targetId;
        Map<String, Activity> map = mActivityStack.peek();
        return map.containsKey(key);
    }

    /**
     * 获取RongCloud 实例。
     *
     * @return RongCloud。
     */
    public static SealAppContext getInstance() {
        return mRongCloudInstance;
    }

    /**
     * init 后就能设置的监听
     */
    private void initListener() {
        RongIM.setConversationBehaviorListener(this);//设置会话界面操作的监听器。
        RongIM.setConversationListBehaviorListener(this);
        RongIM.setUserInfoProvider(this, true);
        RongIM.setGroupInfoProvider(this, true);
        RongIM.setLocationProvider(this);//设置地理位置提供者,不用位置的同学可以注掉此行代码
        setInputProvider();
        setUserInfoEngineListener();
        setReadReceiptConversationType();
//        RongIM.setGroupUserInfoProvider(this, true);
    }

    private void setReadReceiptConversationType() {
        Conversation.ConversationType[] types = new Conversation.ConversationType[]{
                Conversation.ConversationType.PRIVATE,
                Conversation.ConversationType.GROUP,
                Conversation.ConversationType.DISCUSSION
        };
        RongIM.getInstance().setReadReceiptConversationTypeList(types);
    }

    private void setInputProvider() {

        RongIM.setOnReceiveMessageListener(this);
        RongIM.setConnectionStatusListener(this);

        InputProvider.ExtendProvider[] singleProvider = {
                new ImageInputProvider(RongContext.getInstance()),
                new RealTimeLocationInputProvider(RongContext.getInstance()), //带位置共享的地理位
                new FileInputProvider(RongContext.getInstance()),//文件消息
                new RongRedPacketProvider(RongContext.getInstance())//单聊红包
        };

        InputProvider.ExtendProvider[] muiltiProvider = {
                new ImageInputProvider(RongContext.getInstance()),
                new LocationInputProvider(RongContext.getInstance()),//地理位置
                new FileInputProvider(RongContext.getInstance())//文件消息
        };
        InputProvider.ExtendProvider[] groupProvider = {
                new ImageInputProvider(RongContext.getInstance()),
                new LocationInputProvider(RongContext.getInstance()),//地理位置
                createGroupProvider()//群红包
        };

        RongIM.resetInputExtensionProvider(Conversation.ConversationType.PRIVATE, singleProvider);
        RongIM.resetInputExtensionProvider(Conversation.ConversationType.DISCUSSION, groupProvider);
        RongIM.resetInputExtensionProvider(Conversation.ConversationType.CUSTOMER_SERVICE, muiltiProvider);
        RongIM.resetInputExtensionProvider(Conversation.ConversationType.GROUP, groupProvider);
        //根据群(讨论组)id获取群(讨论组)成员信息,然后 groupMemberCallback.setGroupMember(list);
        RPGroupMemberUtil.getInstance().setGroupMemberListener(new NotifyGroupMemberCallback() {
            @Override
            public void getGroupMember(String groupId, GroupMemberCallback groupMemberCallback) {
                //(只是针对融云demo做的缓存逻辑,App开发者及供参考)
                if (RedPacketUtil.getInstance().getChatType().equals(RedPacketUtil.CHAT_GROUP)) {
                    ArrayList<GetGroupMemberResponse.ResultEntity> list = (ArrayList<GetGroupMemberResponse.ResultEntity>) mRedPacketCache.getAsObject(groupId);
                    if (list != null) {
                        NLog.e("group_member", "-cache-");
                        groupMemberCallback.setGroupMember(sortingData(list));
                    } else {
                        NLog.e("group_member", "-no-cache-");
                        mGroupId = groupId;
                        mGroupMemberCallback = groupMemberCallback;
                        AsyncTaskManager.getInstance(mContext).request(REQUEST_GROUP_MEMBER, SealAppContext.this);
                    }
                } else if (RedPacketUtil.getInstance().getChatType().equals(RedPacketUtil.CHAT_DISCUSSION)) {//讨论组
                    ArrayList<GetUserInfosResponse.ResultEntity> list = (ArrayList<GetUserInfosResponse.ResultEntity>) mRedPacketCache.getAsObject(groupId);
                    if (list != null) {
                        NLog.e("discussion_member", "-cache-");
                        groupMemberCallback.setGroupMember(sortingDiscussionData(list));
                    } else {
                        NLog.e("discussion_member", "-no-cache-");
                        mGroupId = groupId;
                        mGroupMemberCallback = groupMemberCallback;
                        AsyncTaskManager.getInstance(mContext).request(REQUEST_DISCUSSION_MEMBER, SealAppContext.this);
                    }

                }

            }
        });
        //App开发者需要根据用户ID获取用户信息,然后mCallback.setUserInfo
        RedPacketUtil.getInstance().setGetUserInfoCallback(new GetUserInfoCallback() {
            @Override
            public void getUserInfo(String userId, final SetUserInfoCallback mCallback) {
                //(只是针对融云demo做的缓存逻辑,App开发者仅供参考)
                UserInfo userInfo = RongContext.getInstance().getUserInfoFromCache(userId);
                if (userInfo != null) {
                    NLog.e("userInfo", "-user-cache-" + userInfo.getName());
                    mCallback.setUserInfo(userInfo.getName(), userInfo.getPortraitUri().toString());
                } else {
                    NLog.e("userInfo", "-user-no-cache-");
                    mUserId = userId;
                    mSetUserInfoCallback = mCallback;
                    AsyncTaskManager.getInstance(mContext).request(REQUEST_USERINFO, SealAppContext.this);
                }

            }
        });

    }

    //App开发者需要根据群(讨论组)ID获取群(讨论组)成员人数,
    //然后mCallback.toRedPacketActivity(number),打开发送红包界面
    private RongGroupRedPacketProvider createGroupProvider() {
        //(只是针对融云demo做的缓存逻辑,App开发者及供参考)
        RongGroupRedPacketProvider groupRedPacketProvider = new RongGroupRedPacketProvider(
                RongContext.getInstance(), new GetGroupInfoCallback() {
            @Override
            public void getGroupPersonNumber(String groupID, final ToRedPacketActivity mCallback) {

                if (RedPacketUtil.getInstance().getChatType().equals(RedPacketUtil.CHAT_GROUP)) {
                    mGroupId = groupID;
                    //同步群信息
                    AsyncTaskManager.getInstance(mContext).request(REQUEST_SYNCGROUP, SealAppContext.this);
                    int number = SharedPreferencesContext.getInstance().getSharedPreferences().getInt(groupID, 0);
                    mCallback.toRedPacketActivity(number);
                } else {//讨论组
                    mGroupId = groupID;
                    RongIM.getInstance().getDiscussion(groupID, new RongIMClient.ResultCallback<Discussion>() {
                        @Override
                        public void onSuccess(Discussion discussion) {
                            mIds = discussion.getMemberIdList();
                            //同步讨论组信息
                            AsyncTaskManager.getInstance(mContext).request(REQUEST_SYNCDISCUSSION, SealAppContext.this);
                            mCallback.toRedPacketActivity(discussion.getMemberIdList().size());
                        }

                        @Override
                        public void onError(RongIMClient.ErrorCode errorCode) {

                        }
                    });
                }
            }
        });
        return groupRedPacketProvider;
    }

    private List<RPUserBean> sortingDiscussionData(List<GetUserInfosResponse.ResultEntity> mList) {
        String userID = RongIM.getInstance().getCurrentUserId();
        List<RPUserBean> data = new ArrayList<RPUserBean>();
        for (int i = 0; i < mList.size(); i++) {
            if (userID.equals(mList.get(i).getId())) {
                continue;
            }
            RPUserBean mRPUserBean = new RPUserBean();
            mRPUserBean.userId = mList.get(i).getId();
            mRPUserBean.userNickname = mList.get(i).getNickname();
            mRPUserBean.userAvatar = mList.get(i).getPortraitUri();
            data.add(mRPUserBean);
        }
        return data;
    }

    private List<RPUserBean> sortingData(List<GetGroupMemberResponse.ResultEntity> mList) {
        String userID = RongIM.getInstance().getCurrentUserId();
        List<RPUserBean> data = new ArrayList<RPUserBean>();
        for (int i = 0; i < mList.size(); i++) {
            if (userID.equals(mList.get(i).getUser().getId())) {
                continue;
            }
            RPUserBean mRPUserBean = new RPUserBean();
            mRPUserBean.userId = mList.get(i).getUser().getId();
            mRPUserBean.userNickname = mList.get(i).getUser().getNickname();
            mRPUserBean.userAvatar = mList.get(i).getUser().getPortraitUri();
            data.add(mRPUserBean);
        }
        return data;
    }


    @Override
    public Object doInBackground(int requestCode, String parameter) throws HttpException {
        switch (requestCode) {
            case REQUEST_SYNCGROUP:
                return action.getGroupMember(mGroupId);
            case REQUEST_GROUP_MEMBER:
                return action.getGroupMember(mGroupId);
            case REQUEST_USERINFO:
                return action.getUserInfoById(mUserId);
            case REQUEST_SYNCDISCUSSION:
                return action.getUserInfos(mIds);
            case REQUEST_DISCUSSION_MEMBER:
                return action.getUserInfos(mIds);
        }

        return null;
    }

    @Override
    public void onSuccess(int requestCode, Object result) {
        if (result != null) {
            switch (requestCode) {
                case REQUEST_SYNCGROUP:
                    GetGroupMemberResponse groupMemberResponse = (GetGroupMemberResponse) result;
                    if (groupMemberResponse.getCode() == 200) {
                        List<GetGroupMemberResponse.ResultEntity> list = groupMemberResponse.getResult();
                        if (list != null && list.size() > 0) {
                            //缓存群成员个数
                            SharedPreferencesContext.getInstance().getSharedPreferences().edit().putInt(mGroupId, list.size());
                            //缓存群成员信息
                            mRedPacketCache.put(mGroupId, (ArrayList<GetGroupMemberResponse.ResultEntity>) list);
                        }
                    }
                    break;
                case REQUEST_GROUP_MEMBER:
                    GetGroupMemberResponse mGroupMemberResponse = (GetGroupMemberResponse) result;
                    if (mGroupMemberResponse.getCode() == 200) {
                        List<GetGroupMemberResponse.ResultEntity> list = mGroupMemberResponse.getResult();
                        if (list != null && list.size() > 0) {
                            //缓存群成员个数
                            SharedPreferencesContext.getInstance().getSharedPreferences().edit().putInt(mGroupId, list.size());
                            //缓存群成员信息
                            mRedPacketCache.put(mGroupId, (ArrayList<GetGroupMemberResponse.ResultEntity>) list);
                            if (mGroupMemberCallback != null) {
                                mGroupMemberCallback.setGroupMember(sortingData(list));
                            }
                        } else {
                            if (mGroupMemberCallback != null) {
                                mGroupMemberCallback.setGroupMember(null);
                            }
                        }
                    }
                    break;
                case REQUEST_USERINFO:
                    GetUserInfoByIdResponse userResponse = (GetUserInfoByIdResponse) result;
                    if (userResponse.getCode() == 200) {
                        GetUserInfoByIdResponse.ResultEntity resultEntity = userResponse.getResult();
                        if (resultEntity != null) {
                            UserInfo userInfo = new UserInfo(resultEntity.getId(), resultEntity.getNickname(), Uri.parse(resultEntity.getPortraitUri()));
                            RongIM.getInstance().refreshUserInfoCache(userInfo);
                            if (mSetUserInfoCallback != null) {
                                mSetUserInfoCallback.setUserInfo(userInfo.getName(), userInfo.getPortraitUri().toString());
                            }
                        } else {
                            if (mSetUserInfoCallback != null) {
                                mSetUserInfoCallback.UserInfoError("data is null");
                            }
                        }

                    }
                    break;
                case REQUEST_SYNCDISCUSSION:
                    GetUserInfosResponse response = (GetUserInfosResponse) result;
                    if (response.getCode() == 200) {
                        List<GetUserInfosResponse.ResultEntity> infos = response.getResult();
                        if (infos != null && infos.size() > 0) {
                            //缓存讨论组成员信息
                            mRedPacketCache.put(mGroupId, (ArrayList<GetUserInfosResponse.ResultEntity>) infos);
                        }
                    }
                    break;
                case REQUEST_DISCUSSION_MEMBER:
                    GetUserInfosResponse mInfoResponse = (GetUserInfosResponse) result;
                    if (mInfoResponse.getCode() == 200) {
                        List<GetUserInfosResponse.ResultEntity> infos = mInfoResponse.getResult();
                        if (infos != null && infos.size() > 0) {
                            //缓存讨论组成员信息
                            mRedPacketCache.put(mGroupId, (ArrayList<GetUserInfosResponse.ResultEntity>) infos);
                            if (mGroupMemberCallback != null) {
                                mGroupMemberCallback.setGroupMember(sortingDiscussionData(infos));
                            }
                        } else {
                            if (mGroupMemberCallback != null) {
                                mGroupMemberCallback.setGroupMember(null);
                            }
                        }
                    }
                    break;
            }

        }

    }

    @Override
    public void onFailure(int requestCode, int state, Object result) {
        if (state == AsyncTaskManager.HTTP_NULL_CODE || state == AsyncTaskManager.HTTP_ERROR_CODE) {
            LoadDialog.dismiss(mContext);
            NToast.shortToast(mContext, R.string.network_not_available);
            return;
        }
        switch (requestCode) {
            case REQUEST_GROUP_MEMBER:
                if (mGroupMemberCallback != null) {
                    mGroupMemberCallback.setGroupMember(null);
                }
                break;
            case REQUEST_DISCUSSION_MEMBER:
                if (mGroupMemberCallback != null) {
                    mGroupMemberCallback.setGroupMember(null);
                }
                break;
            case REQUEST_USERINFO:
                if (mSetUserInfoCallback != null) {
                    mSetUserInfoCallback.UserInfoError("data is null");
                }
                break;
        }

    }


    /**
     * 需要 rongcloud connect 成功后设置的 listener
     */
    public void setUserInfoEngineListener() {
        UserInfoEngine.getInstance(mContext).setListener(new UserInfoEngine.UserInfoListener() {
            @Override
            public void onResult(UserInfo info) {
                if (info != null && RongIM.getInstance() != null) {
                    if (TextUtils.isEmpty(String.valueOf(info.getPortraitUri()))) {
                        info.setPortraitUri(Uri.parse(RongGenerate.generateDefaultAvatar(info.getName(), info.getUserId())));
                    }
                    NLog.e("UserInfoEngine", info.getName() + info.getPortraitUri());
                    RongIM.getInstance().refreshUserInfoCache(info);
                }
            }
        });
        GroupInfoEngine.getInstance(mContext).setmListener(new GroupInfoEngine.GroupInfoListeners() {
            @Override
            public void onResult(Group info) {
                if (info != null && RongIM.getInstance() != null) {
                    NLog.e("GroupInfoEngine:" + info.getId() + "----" + info.getName() + "----" + info.getPortraitUri());
                    if (TextUtils.isEmpty(String.valueOf(info.getPortraitUri()))) {
                        info.setPortraitUri(Uri.parse(RongGenerate.generateDefaultAvatar(info.getName(), info.getId())));
                    }
                    RongIM.getInstance().refreshGroupInfoCache(info);

                }
            }
        });
    }

    @Override
    public boolean onConversationPortraitClick(Context context, Conversation.ConversationType conversationType, String s) {
        return false;
    }

    @Override
    public boolean onConversationPortraitLongClick(Context context, Conversation.ConversationType conversationType, String s) {
        return false;
    }

    @Override
    public boolean onConversationLongClick(Context context, View view, UIConversation uiConversation) {
        return false;
    }

    @Override
    public boolean onConversationClick(Context context, View view, UIConversation uiConversation) {
        MessageContent messageContent = uiConversation.getMessageContent();
        if (messageContent instanceof ContactNotificationMessage) {
            ContactNotificationMessage contactNotificationMessage = (ContactNotificationMessage) messageContent;
            if (contactNotificationMessage.getOperation().equals("AcceptResponse")) {
                // 被加方同意请求后
                if (contactNotificationMessage.getExtra() != null) {
                    ContactNotificationMessageData bean = null;
                    try {
                        bean = JsonMananger.jsonToBean(contactNotificationMessage.getExtra(), ContactNotificationMessageData.class);
                    } catch (HttpException e) {
                        e.printStackTrace();
                    }
                    RongIM.getInstance().startPrivateChat(context, uiConversation.getConversationSenderId(), bean.getSourceUserNickname());

                }
            } else {
                context.startActivity(new Intent(context, NewFriendListActivity.class));
            }
            return true;
        }
        return false;
    }

    @Override
    public boolean onReceived(Message message, int i) {
        MessageContent messageContent = message.getContent();
        if (messageContent instanceof ContactNotificationMessage) {
            ContactNotificationMessage contactNotificationMessage = (ContactNotificationMessage) messageContent;
            if (contactNotificationMessage.getOperation().equals("Request")) {
                //对方发来好友邀请
                BroadcastManager.getInstance(mContext).sendBroadcast(SealAppContext.UPDATE_RED_DOT);
            } else if (contactNotificationMessage.getOperation().equals("AcceptResponse")) {
                //对方同意我的好友请求
                ContactNotificationMessageData c = null;
                try {
                    c = JsonMananger.jsonToBean(contactNotificationMessage.getExtra(), ContactNotificationMessageData.class);
                } catch (HttpException e) {
                    e.printStackTrace();
                }
                if (c != null) {
                    DBManager.getInstance(mContext).getDaoSession().getFriendDao().insertOrReplace(new Friend(contactNotificationMessage.getSourceUserId(), c.getSourceUserNickname(), null, null, null, null));
                }
                BroadcastManager.getInstance(mContext).sendBroadcast(UPDATE_FRIEND);
                BroadcastManager.getInstance(mContext).sendBroadcast(SealAppContext.UPDATE_RED_DOT);
            }
//                // 发广播通知更新好友列表
//            BroadcastManager.getInstance(mContext).sendBroadcast(UPDATE_RED_DOT);
//            }
        } else if (messageContent instanceof GroupNotificationMessage) {
            GroupNotificationMessage groupNotificationMessage = (GroupNotificationMessage) messageContent;
            NLog.e("" + groupNotificationMessage.getMessage());
            if (groupNotificationMessage.getOperation().equals("Kicked")) {
            } else if (groupNotificationMessage.getOperation().equals("Add")) {
            } else if (groupNotificationMessage.getOperation().equals("Quit")) {
            } else if (groupNotificationMessage.getOperation().equals("Rename")) {
            }

        } else if (messageContent instanceof ImageMessage) {
            ImageMessage imageMessage = (ImageMessage) messageContent;
            Log.e("imageMessage", imageMessage.getRemoteUri().toString());
        } else if (messageContent instanceof RongEmptyMessage) {
            //接收到空消息（不展示UI的消息）向本地插入一条“XX领取了你的红包”
            RedPacketUtil.getInstance().insertMessage(message);
        }
        return false;
    }

    @Override
    public UserInfo getUserInfo(String s) {
        return UserInfoEngine.getInstance(mContext).startEngine(s);
    }

    @Override
    public Group getGroupInfo(String s) {
        return GroupInfoEngine.getInstance(mContext).startEngine(s);
    }

    @Override
    public GroupUserInfo getGroupUserInfo(String groupId, String userId) {
//        return GroupUserInfoEngine.getInstance(mContext).startEngine(groupId, userId);
        return null;
    }

    @Override
    public void onChanged(ConnectionStatus connectionStatus) {
        if (connectionStatus.getMessage().equals(ConnectionStatus.KICKED_OFFLINE_BY_OTHER_CLIENT)) {

        }
    }

    @Override
    public void onStartLocation(Context context, LocationCallback locationCallback) {
        /**
         * demo 代码  开发者需替换成自己的代码。
         */
        SealAppContext.getInstance().setLastLocationCallback(locationCallback);
        Intent intent = new Intent(context, AMAPLocationActivity.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        context.startActivity(intent);

    }

    @Override
    public boolean onUserPortraitClick(Context context, Conversation.ConversationType conversationType, UserInfo userInfo) {
        if (userInfo != null) {
            Intent intent = new Intent(context, PersonalProfileActivity.class);
            intent.putExtra("conversationType", conversationType.getValue());
            intent.putExtra("userinfo", userInfo);
            context.startActivity(intent);
        }
        return true;
    }

    @Override
    public boolean onUserPortraitLongClick(Context context, Conversation.ConversationType conversationType, UserInfo userInfo) {
        return false;
    }

    @Override
    public boolean onMessageClick(final Context context, final View view, final Message message) {

        //real-time location message begin
        if (message.getContent() instanceof RealTimeLocationStartMessage) {
            RealTimeLocationConstant.RealTimeLocationStatus status = RongIMClient.getInstance().getRealTimeLocationCurrentState(message.getConversationType(), message.getTargetId());

//            if (status == RealTimeLocationConstant.RealTimeLocationStatus.RC_REAL_TIME_LOCATION_STATUS_IDLE) {
//                startRealTimeLocation(context, message.getConversationType(), message.getTargetId());
//            } else
            if (status == RealTimeLocationConstant.RealTimeLocationStatus.RC_REAL_TIME_LOCATION_STATUS_INCOMING) {


                final AlterDialogFragment alterDialogFragment = AlterDialogFragment.newInstance("", "加入位置共享", "取消", "加入");
                alterDialogFragment.setOnAlterDialogBtnListener(new AlterDialogFragment.AlterDialogBtnListener() {

                    @Override
                    public void onDialogPositiveClick(AlterDialogFragment dialog) {
                        RealTimeLocationConstant.RealTimeLocationStatus status = RongIMClient.getInstance().getRealTimeLocationCurrentState(message.getConversationType(), message.getTargetId());

                        if (status == null || status == RealTimeLocationConstant.RealTimeLocationStatus.RC_REAL_TIME_LOCATION_STATUS_IDLE) {
                            startRealTimeLocation(context, message.getConversationType(), message.getTargetId());
                        } else {
                            joinRealTimeLocation(context, message.getConversationType(), message.getTargetId());
                        }

                    }

                    @Override
                    public void onDialogNegativeClick(AlterDialogFragment dialog) {
                        alterDialogFragment.dismiss();
                    }
                });

                alterDialogFragment.show(((FragmentActivity) context).getSupportFragmentManager());
            } else {

                if (status != null && (status == RealTimeLocationConstant.RealTimeLocationStatus.RC_REAL_TIME_LOCATION_STATUS_OUTGOING || status == RealTimeLocationConstant.RealTimeLocationStatus.RC_REAL_TIME_LOCATION_STATUS_CONNECTED)) {

                    Intent intent = new Intent(((FragmentActivity) context), RealTimeLocationActivity.class);
                    intent.putExtra("conversationType", message.getConversationType().getValue());
                    intent.putExtra("targetId", message.getTargetId());
                    context.startActivity(intent);
                }
            }
            return true;
        }

        //real-time location message end
        /**
         * demo 代码  开发者需替换成自己的代码。
         */
        if (message.getContent() instanceof LocationMessage) {
            Intent intent = new Intent(context, AMAPLocationActivity.class);
            intent.putExtra("location", message.getContent());
            intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            context.startActivity(intent);
        } else if (message.getContent() instanceof ImageMessage) {
//            Intent intent = new Intent(context, PhotoActivity.class);
//            intent.putExtra("message", message);
//            context.startActivity(intent);
        }

        return false;
    }


    private void startRealTimeLocation(Context context, Conversation.ConversationType conversationType, String targetId) {
        RongIMClient.getInstance().startRealTimeLocation(conversationType, targetId);

        Intent intent = new Intent(((FragmentActivity) context), RealTimeLocationActivity.class);
        intent.putExtra("conversationType", conversationType.getValue());
        intent.putExtra("targetId", targetId);
        context.startActivity(intent);
    }

    private void joinRealTimeLocation(Context context, Conversation.ConversationType conversationType, String targetId) {
        RongIMClient.getInstance().joinRealTimeLocation(conversationType, targetId);

        Intent intent = new Intent(((FragmentActivity) context), RealTimeLocationActivity.class);
        intent.putExtra("conversationType", conversationType.getValue());
        intent.putExtra("targetId", targetId);
        context.startActivity(intent);
    }

    @Override
    public boolean onMessageLinkClick(Context context, String s) {
        return false;
    }

    @Override
    public boolean onMessageLongClick(Context context, View view, Message message) {
        return false;
    }


    public RongIM.LocationProvider.LocationCallback getLastLocationCallback() {
        return mLastLocationCallback;
    }

    public void setLastLocationCallback(RongIM.LocationProvider.LocationCallback lastLocationCallback) {
        this.mLastLocationCallback = lastLocationCallback;
    }
}
