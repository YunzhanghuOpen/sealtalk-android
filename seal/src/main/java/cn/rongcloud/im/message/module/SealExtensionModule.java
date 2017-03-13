package cn.rongcloud.im.message.module;

import android.content.Intent;
import android.net.Uri;
import android.text.TextUtils;
import android.view.View;

import java.util.List;

import cn.rongcloud.contactcard.ContactCardPlugin;
import cn.rongcloud.contactcard.IContactCardClickCallback;
import cn.rongcloud.contactcard.IContactCardInfoProvider;
import cn.rongcloud.contactcard.message.ContactMessage;
import cn.rongcloud.im.SealUserInfoManager;
import cn.rongcloud.im.db.Friend;
import cn.rongcloud.im.server.pinyin.CharacterParser;
import cn.rongcloud.im.server.utils.RongGenerate;
import cn.rongcloud.im.ui.activity.UserDetailActivity;
import io.rong.imkit.DefaultExtensionModule;
import io.rong.imkit.RongExtension;
import io.rong.imkit.emoticon.IEmoticonTab;
import io.rong.imkit.model.UIMessage;
import io.rong.imkit.plugin.IPluginModule;
import io.rong.imlib.model.Conversation;
import io.rong.imlib.model.UserInfo;


public class SealExtensionModule extends DefaultExtensionModule {
    @Override
    public void onInit(String appKey) {
        super.onInit(appKey);
        ContactCardPlugin.init();
        ContactCardPlugin.getInstance().setContactCardInfoProvider(new IContactCardInfoProvider() {
            @Override
            public void getContactCardInfoProvider(final IContactCardInfoCallback contactInfoCallback) {
                SealUserInfoManager.getInstance().getFriends(new SealUserInfoManager.ResultCallback<List<Friend>>() {
                    @Override
                    public void onSuccess(List<Friend> friendList) {
                        contactInfoCallback.getContactCardInfoCallback(friendList);
                    }

                    @Override
                    public void onError(String errString) {
                        contactInfoCallback.getContactCardInfoCallback(null);
                    }
                });
            }
        });

        ContactCardPlugin.getInstance().setContactCardClickCallback(new IContactCardClickCallback() {
            @Override
            public void onContactCardMessageClick(View view, int position, ContactMessage content, UIMessage message) {
                Intent intent = new Intent(view.getContext(), UserDetailActivity.class);
                Friend friend = SealUserInfoManager.getInstance().getFriendByID(content.getId());
                if (friend == null) {
                    UserInfo userInfo = new UserInfo(content.getId(), content.getName(), Uri.parse(TextUtils.isEmpty(content.getImgUrl()) ? RongGenerate.generateDefaultAvatar(content.getName(), content.getId()) : content.getImgUrl()));
                    friend = CharacterParser.getInstance().generateFriendFromUserInfo(userInfo);
                }
                intent.putExtra("friend", friend);
                view.getContext().startActivity(intent);
            }
        });
    }

    @Override
    public void onAttachedToExtension(RongExtension extension) {
        super.onAttachedToExtension(extension);


    }

    @Override
    public void onDetachedFromExtension() {
        super.onDetachedFromExtension();
    }

    @Override
    public List<IPluginModule> getPluginModules(Conversation.ConversationType conversationType) {
        List<IPluginModule> pluginModules = super.getPluginModules(conversationType);
        if (conversationType.equals(Conversation.ConversationType.PRIVATE)
                || conversationType.equals(Conversation.ConversationType.GROUP)) {
            pluginModules.add(ContactCardPlugin.getInstance());
        }
        return pluginModules;
    }

    @Override
    public List<IEmoticonTab> getEmoticonTabs() {
        return super.getEmoticonTabs();
    }
}
