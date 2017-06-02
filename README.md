# 融云Demo红包SDK集成文档

## 集成概述
* 红包SDK分为两个版本，即钱包版红包SDK与支付宝版红包SDK。
* 使用钱包版红包SDK的用户，可以使用银行卡支付或支付宝支付等第三方支付来发红包；收到的红包金额会进入到钱包余额，并支持提现到绑定的银行卡。
* 使用支付宝版红包SDK的用户，发红包仅支持支付宝支付；收到的红包金额即时入账至绑定的支付宝账号。
* 请选择希望接入的版本并下载对应的SDK进行集成，钱包版红包SDK与支付宝版红包SDK集成方式相同。
* 需要注意的是如果已经集成了钱包版红包SDK，暂不支持切换到支付宝版红包SDK（两个版本不支持互通）。
* [融云Demo](https://github.com/YunzhanghuOpen/sealtalk-android/tree/redpacket-plugin)中使用redPacketlibrary模块集成了红包SDK相关红能。

## redPacketlibrary介绍

* redPacketlibrary是在融云Demo中集成红包功能的模块，开发者可参考该模块中的方法集成红包功能。建议开发者以模块的形式集成红包功能，便于项目的更新和维护。
* **注意此library仅支持非支付宝UI开源版本使用。**
## redPacketlibrary目录说明

* libs ：包含了集成红包功能所依赖的jar包。
* res ：包含了聊天页面中的资源文件（例如红包消息卡片，回执消息的UI等）。
* message包 ： 定义了红包消息以及红包回执消息。
* module包：自定义扩展栏的类。
* provider包：单聊、群聊（讨论组）红包提供者、红包消息和回执消息UI展示提供者。
* RedPacketUtil.java ：封装了和红包相关的工具类。
* RedPacketCache.java ：缓存类（App开发者可以根据需求进行删除）。
* **注意: redpacketlibrary依赖了IMKIT，可以查看redpacketlibrary的build.gradle。**
## 支付宝UI开源版本
* git clone git@github.com:YunzhanghuOpen/sealtalk-android.git
* cd sealtalk-android
* git checkout redpack-ali-open
* cd redpacketui-open 
* git submodule init
* git submodule update
* **开源版没有redpacketlibrary，红包使用相关的工具类移步到seal里面的redpacket包下面。**
## 红包SDK的更新
* 以支付宝版红包SDK为例，修改com.yunzhanghu.redpacket:redpacket-alipay:1.1.2中的1.1.2为已发布的更高版本(例如1.1.3)。

# 开始集成

## 添加对红包工程的依赖

* 在工程的build.gradle(Top-level build file)添加远程仓库地址
```java
allprojects {
    repositories {
        jcenter()
        mavenCentral()
        maven {
            url "https://raw.githubusercontent.com/YunzhanghuOpen/redpacket-maven-repo/master/release"
        }
    }
}
```
* SealTalkDemo的build.gradle中
```java
compile project(':redpacketlibrary')
```    
* 在redpacketlibrary的build.gradle中

* 钱包版配置如下

```java
dependencies {
    compile fileTree(include: ['*.jar'], dir: 'libs')
    compile 'com.android.support:support-v4:23.0.1'
    compile project(':IMKit')
    compile 'com.yunzhanghu.redpacket:redpacket-wallet:3.4.5'
}
```
* 支付宝版配置如下

```java
dependencies {
    compile fileTree(include: ['*.jar'], dir: 'libs')
    compile 'com.android.support:support-v4:23.0.1'
    compile project(':IMKit')
    compile 'com.yunzhanghu.redpacket:redpacket-alipay:2.0.0'
    compile 'com.android.support:recyclerview-v7:23.0.1'
}
```
## 初始化红包SDK

* 在Application的onCreate()方法中
```java
//初始化红包上下文
RedPacket.getInstance().initRedPacket(this, RPConstant.AUTH_METHOD_SIGN, new RPInitRedPacketCallback() {
                @Override
                public void initTokenData(RPValueCallback<TokenData> rpValueCallback) {
                    //TokenData中的四个参数需要开发者向自己的Server获取
                    final String url = "http://rpv2.yunzhanghu.com/api/sign?duid=" + RongIM.getInstance().getCurrentUserId() + "&dcode=1101%23testrongyun";
                    RedPacketUtil.getInstance().requestSign(App.this, url, rpValueCallback);

                }

                @Override
                public RedPacketInfo initCurrentUserSync() {
                   // 这里需要同步设置当前用户id、昵称和头像url
                   //钱包版
                   RedPacketInfo redPacketInfo = new RedPacketInfo();
                   redPacketInfo.fromUserId = "yunzhanghu";
                   redPacketInfo.fromAvatarUrl = "testURL";
                   redPacketInfo.fromNickName = "yunzhanghu001";
                   //支付宝版 Since 2.0.0
                   RedPacketInfo redPacketInfo = new RedPacketInfo();
                   redPacketInfo.currentUserId = "yunzhanghu";
                   redPacketInfo.currentAvatarUrl = "testURL";
                   redPacketInfo.currentNickname = "yunzhanghu001";
                   return redPacketInfo;
                }
            });
// 打开Log开关，正式发布需要关闭
RedPacket.getInstance().setDebugMode(true); 
```
* **initRedPacket(context, authMethod, callback) 参数说明**

| 参数名称       | 参数类型             | 参数说明  | 必填         |
| :---------- | :----------------------- | :----- | :---------- |
| context    | Context                 | 上下文   | 是          |
| authMethod | String                  | 授权类型  | 是**（见注1）** |
| callback   | RPInitRedPacketCallback | 初始化接口 | 是          |  

* **RPInitRedPacketCallback 接口说明**

| **initTokenData(RPValueCallback<TokenData> callback)**      |
| :---------------------------------------- |
| **该方法用于初始化TokenData，在进入红包相关页面、红包Token不存在或红包Token过期时调用。TokenData是请求红包Token所需要的数据模型，建议在该方法中异步向APP服务器获取相关参数，以保证数据的有效性；不建议从本地缓存中获取TokenData所需的参数，可能导致获取红包Token无效。** |
| **initCurrentUserSync()**                |
| **该方法用于初始化当前用户信息，在进入红包相关页面时调用，需同步获取。**   |

* **注1 ：**

**使用签名方式获取红包Token时，authMethod赋值必须为RPConstant.AUTH_METHOD_SIGN。**

* **注意：App Server提供的获取签名的接口必须先验证用户身份，并保证签名的用户和该登录用户一致，防止该接口被滥用。详见云账户[REST API开发文档](http://yunzhanghu-com.oss-cn-qdjbp-a.aliyuncs.com/%E4%BA%91%E8%B4%A6%E6%88%B7%E7%BA%A2%E5%8C%85%E6%8E%A5%E5%8F%A3%E6%96%87%E6%A1%A3-v3_1_0.pdf)** 

## 注册RedPacketModule

* 在SealAppContext注册RedPacketModule（开发者可以在Application中实现）

```java
List<IExtensionModule> moduleList = RongExtensionManager.getInstance().getExtensionModules();
      IExtensionModule defaultModule = null;
      if (moduleList != null) {
          for (IExtensionModule module : moduleList) {
              if (module instanceof DefaultExtensionModule) {
                  defaultModule = module;
                  break;
              }
          }
          if (defaultModule != null) {
              RongExtensionManager.getInstance().unregisterExtensionModule(defaultModule);
              //注册自定义module
              RongExtensionManager.getInstance().registerExtensionModule(createRedPacketModule());
          }
      }
```

* 创建RedPacketModule

```java
private RedPacketModule createRedPacketModule() {
     //App开发者需要根据群(讨论组)ID获取群(讨论组)成员人数,
     //然后mCallback.toRedPacketActivity(number),打开发送红包界面
     RedPacketModule redPacketModule = new RedPacketModule(new GetGroupInfoCallback() {
         @Override
         public void getGroupPersonNumber(String groupID, final ToRedPacketActivity mCallback) {
             //(只是针对融云demo做的缓存逻辑,App开发者仅供参考)
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
     return redPacketModule;
 }
```
## 回执消息
* SealAppContext中实现OnReceiveMessageListener（开发者可以在Application中实现）

```java
@Override
public boolean onReceived(Message message, int left) {
  ...
  MessageContent messageContent = message.getContent();
    if (messageContent instanceof RongEmptyMessage) {
       //接收到空消息（不展示UI的消息）向本地插入一条“XX领取了你的红包”
         RedPacketUtil.getInstance().insertMessage(message);
      }       
}
```
## 专属红包（可选）

* 在SealAppContext实现setRPGroupMemberListener（App开发者可以在Application中实现）

```java
//根据群(讨论组)id获取群(讨论组)成员信息,然后 rpValueCallback.onSuccess(list);
RedPacket.getInstance().setRPGroupMemberListener(new RPGroupMemberListener() {
       @Override
       public void getGroupMember(String groupId, RPValueCallback<List<RPUserBean>> rpValueCallback) {
           //(只是针对融云demo做的缓存逻辑,App开发者仅供参考)
           if (RedPacketUtil.getInstance().getChatType().equals(RedPacketUtil.CHAT_GROUP)) {
               ArrayList<GetGroupMemberResponse.ResultEntity> list = (ArrayList<GetGroupMemberResponse.ResultEntity>) mRedPacketCache.getAsObject(groupId);
               if (list != null) {
                  NLog.e("group_member", "-cache-");
                  rpValueCallback.onSuccess(sortingData(list));
              } else {
                   NLog.e("group_member", "-no-cache-");
                   mGroupId = groupId;
                   mGroupMemberCallback = rpValueCallback;
                   AsyncTaskManager.getInstance(mContext).request(REQUEST_GROUP_MEMBER, SealAppContext.this);
               }
           } else if (RedPacketUtil.getInstance().getChatType().equals(RedPacketUtil.CHAT_DISCUSSION)) {//讨论组
               ArrayList<GetUserInfosResponse.ResultEntity> list = (ArrayList<GetUserInfosResponse.ResultEntity>) mRedPacketCache.getAsObject(groupId);
               if (list != null) {
                   NLog.e("discussion_member", "-cache-");
                   rpValueCallback.onSuccess(sortingDiscussionData(list));
               } else {
                   NLog.e("discussion_member", "-no-cache-");
                   mGroupId = groupId;
                   mGroupMemberCallback = rpValueCallback;
                   AsyncTaskManager.getInstance(mContext).request(REQUEST_DISCUSSION_MEMBER, SealAppContext.this);
               }

           }
       }
   });   
```
## 进入零钱页(钱包版)

```java
RPRedPacketUtil.getInstance().startChangeActivity(getActivity());
```
## 进入红包记录页(支付宝版)

```java
RPRedPacketUtil.getInstance().startRecordActivity(context)
```
## 兼容Android7.0以上系统（钱包版）

* Android 7.0强制启用了被称作StrictMode的策略，带来的影响就是你的App对外无法暴露file://类型URI了。
* 如果你使用Intent携带这样的URI去打开外部App(比如：打开系统相机拍照)，那么会抛出FileUriExposedException异常。
* 由于钱包版SDK中有上传身份信息的功能，该功能调用了系统相机拍照，为了兼容Android 7.0以上系统，使用了FileProvider。
* 为保证红包SDK声明的FileProvider唯一，且不与其他应用中的FileProvider冲突，需要在App的build.gradle中增加resValue。

* 示例如下：
```java
defaultConfig {
  applicationId "your applicationId"
  minSdkVersion androidMinSdkVersion
  targetSdkVersion androidTargetSdkVersion
  resValue "string", "rp_provider_authorities_name","${applicationId}.FileProvider"
}
```
* 如果你的应用中也定义了FileProvider，会报合并清单文件错误，需要你在定义的FileProvider中添加tools:replace="android:authorities" 、 tools:replace="android:resource"

* 示例如下：

```java
<provider
   android:name="android.support.v4.content.FileProvider"
   tools:replace="android:authorities"
   android:authorities="包名.FileProvider"
   android:exported="false"
   android:grantUriPermissions="true">
   <meta-data
     android:name="android.support.FILE_PROVIDER_PATHS"
     tools:replace="android:resource"
     android:resource="@xml/rc_file_path" />
</provider>
```
## detachView接口

* RPRedPacketUtil.getInstance().detachView()

* 在拆红包方法所在页面销毁时调用，可防止内存泄漏。

* 调用示例(以ChatFragment为例)
```java
@Override
public void onDestroy() {
    super.onDestroy();
    RPRedPacketUtil.getInstance().detachView();
}
```
## 拆红包音效
* 在assets目录下添加open_packet_sound.mp3或者open_packet_sound.wav文件即可(文件大小不要超过1M)。

## 进入转账页面(钱包版)
```java
RedPacketUtil.startRedPacket(getActivity(), RPConstant.RP_ITEM_TYPE_TRANSFER, toChatUsername, new RPSendPacketCallback() {
     @Override
     public void onGenerateRedPacketId(String redPacketId) {

     }
     @Override
     public void onSendPacketSuccess(RedPacketInfo redPacketInfo) {
      //参考红包消息、自定义转账消息并发送
    }
});
```
## 进入转账详情方法(钱包版)

**RPRedPacketUtil.getInstance().openTransferPacket(context, redPacketInfo)**

* redPacketInfo传入参数

| 参数名称                |    参数类型    |   参数说明    |
| :------------------ | :--------: | :-------: |
| **redPacketAmount** | **String** | **转账金额**  |
| **transferTime**    | **String** | **转账时间**  |
| **messageDirect**   |  **int**   | **消息的方向** |

* 调用示例
```java
RPRedPacketUtil.getInstance().openTransferPacket(context, redPacketInfo)
```
**以上集成完毕**

# 关于收发红包的说明

## 发送红包
* SingleRedPacketProvider中进入单聊红包页面

```java
public void onClick(Fragment fragment, RongExtension rongExtension) {
     mContext = rongExtension.getContext();
     mConversationType = rongExtension.getConversationType();
     mTargetId = rongExtension.getTargetId();
     final RedPacketInfo redPacketInfo = new RedPacketInfo();
     String toUserId = mTargetId;
     UserInfo userInfo = RongContext.getInstance().getUserInfoFromCache(toUserId);
     redPacketInfo.toUserId = toUserId; //接收者ID
     if (userInfo != null) {
        redPacketInfo.toNickName = !TextUtils.isEmpty(userInfo.getName()) ? userInfo.getName() : toUserId;
        redPacketInfo.toAvatarUrl= !TextUtils.isEmpty(userInfo.getPortraitUri().toString()) ? 
                                     userInfo.getPortraitUri().toString() :"none";
     }
     redPacketInfo.chatType = RPConstant.CHATTYPE_SINGLE;//单聊
     RPRedPacketUtil.getInstance().startRedPacket((FragmentActivity) mContext, RPConstant.RP_ITEM_TYPE_SINGLE, redPacketInfo, new RPSendPacketCallback() {
          @Override
          public void onSendPacketSuccess(RedPacketInfo redPacketInfo) {
              mGreeting = redPacketInfo.redPacketGreeting;//祝福语
              mSponsor = mContext.getString(R.string.sponsor_red_packet);//XX红包
              RedPacketInfo currentUserSync = RedPacket.getInstance().getRPInitRedPacketCallback().initCurrentUserSync();
              //钱包版
              String userId = currentUserSync.fromUserId;//发送者ID
              String userName = currentUserSync.fromNickName;//发送者名字
              //支付宝版
              String userId = currentUserSync.currentUserId;//发送者ID
              String userName = currentUserSync.currentNickname;//发送者名字
              RedPacketMessage message = RedPacketMessage.obtain(userId, userName,
                      mGreeting, redPacketInfo.redPacketId, "1", mSponsor, redPacketInfo.redPacketType, redPacketInfo.toUserId);
              //发送红包消息到聊天页面
              sendMessage(message);
          }

          @Override
          public void onGenerateRedPacketId(String s) {

          }
      });
  }
```
* RongGroupRedPacketProvider中进入群红包（讨论组）页面

```java
public void onClick(Fragment fragment, RongExtension rongExtension) {
     mContext = rongExtension.getContext();
     mConversationType = rongExtension.getConversationType();
     mTargetId = rongExtension.getTargetId();
     redPacketInfo = new RedPacketInfo();
     redPacketInfo.toGroupId = mTargetId;//群ID
     redPacketInfo.chatType = RPConstant.CHATTYPE_GROUP;//群聊、讨论组类型
     if (mConversationType == Conversation.ConversationType.GROUP) {
         RedPacketUtil.getInstance().setChatType(RedPacketUtil.CHAT_GROUP);
     } else {
         RedPacketUtil.getInstance().setChatType(RedPacketUtil.CHAT_DISCUSSION);
     }
     if (callback != null) {
       callback.getGroupPersonNumber(redPacketInfo.toGroupId, this);
     } else {
         Toast.makeText(mContext, "回调函数不能为空", Toast.LENGTH_SHORT).show();
     }
 } 
 /**
  * 跳转到发送红包页面
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
           //钱包版
            String userId = currentUserSync.fromUserId;//发送者ID
            String userName = currentUserSync.fromNickName;//发送者名字
            //支付宝版
            String userId = currentUserSync.currentUserId;//发送者ID
            String userName = currentUserSync.currentNickname;//发送者名字
            String redPacketType = redPacketInfo.redPacketType;//群红包类型
            String specialReceiveId = redPacketInfo.toUserId;//专属红包接受者ID
            RedPacketMessage message = RedPacketMessage.obtain(userId, userName,
                    redPacketInfo.redPacketGreeting, redPacketInfo.redPacketId, "1", mSponsor, redPacketType, specialReceiveId);
            //发送红包消息到聊天页面
            sendMessage(message);
        }
        @Override
        public void onGenerateRedPacketId(String s) {
        }
    });
}   
```
## 拆红包
* 在RedPacketMsgProvider中拆红包

```java
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
      redPacketInfo.redPacketType=content.getRedPacketType();//获取红包类型
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
      //钱包版
      RPRedPacketUtil.getInstance().openRedPacket(redPacketInfo, (FragmentActivity) mContext, new RPRedPacketUtil.RPOpenPacketCallback() {
            @Override
        public void onSuccess(String senderId, String senderNickname, String myAmount) {
           //拆红包消息成功,然后发送回执消息例如"你领取了XX的红包"
            sendAckMsg(mContent, mMessage);
       }

        @Override
        public void showLoading() {
            progressDialog.show();
        }

        @Override
        public void hideLoading() {
            progressDialog.dismiss();
        }

        @Override
        public void onError(String errorCode, String errorMsg) {
            Toast.makeText(mContext, errorMsg, Toast.LENGTH_SHORT).show();
        }
    });
    //支付宝版
    //打开红包
    RPRedPacketUtil.getInstance().openRedPacket(redPacketInfo.redPacketId,redPacketInfo.redPacketType, (FragmentActivity) mContext, new  RPRedPacketUtil.RPOpenPacketCallback() {

         @Override
         public void onSuccess(RedPacketInfo redPacketInfo) {
              //打开红包消息成功,然后发送回执消息例如"你领取了XX的红包"
              sendAckMsg(mContent, mMessage);
         }

         @Override
         public void showLoading() {
             progressDialog.show();
         }

         @Override
         public void hideLoading() {
             progressDialog.dismiss();
         }

         @Override
         public void onError(String errorCode, String errorMsg) {
            Toast.makeText(mContext, errorMsg, Toast.LENGTH_SHORT).show();

         }
     });
}
```
## 回执消息
* 在RedPacketMsgProvider中拆红包成功之后处理红包回执消息

```java
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
```
* 删除红包消息

```java
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
 ```       
