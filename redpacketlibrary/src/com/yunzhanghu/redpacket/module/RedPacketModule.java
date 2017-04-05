package com.yunzhanghu.redpacket.module;

import com.yunzhanghu.redpacket.callback.GetGroupInfoCallback;
import com.yunzhanghu.redpacket.message.EmptyMessage;
import com.yunzhanghu.redpacket.message.NotificationMessage;
import com.yunzhanghu.redpacket.message.RedPacketMessage;
import com.yunzhanghu.redpacket.provider.GroupRedPacketProvider;
import com.yunzhanghu.redpacket.provider.NotificationMsgProvider;
import com.yunzhanghu.redpacket.provider.RedPacketMsgProvider;
import com.yunzhanghu.redpacket.provider.SingleRedPacketProvider;

import java.util.List;

import io.rong.imkit.DefaultExtensionModule;
import io.rong.imkit.RongExtension;
import io.rong.imkit.RongIM;
import io.rong.imkit.emoticon.IEmoticonTab;
import io.rong.imkit.plugin.IPluginModule;
import io.rong.imlib.model.Conversation;

/**
 * Created by desert on 17/3/10.
 */

public class RedPacketModule extends DefaultExtensionModule {
    private GetGroupInfoCallback mGetGroupInfoCallback;

    public RedPacketModule(GetGroupInfoCallback getGroupInfoCallback) {
        this.mGetGroupInfoCallback = getGroupInfoCallback;
    }

    @Override
    public void onInit(String appKey) {
        super.onInit(appKey);
        RongIM.registerMessageType(EmptyMessage.class);
        RongIM.registerMessageType(NotificationMessage.class);
        RongIM.registerMessageType(RedPacketMessage.class);
        RongIM.registerMessageTemplate(new NotificationMsgProvider());
        RongIM.registerMessageTemplate(new RedPacketMsgProvider());
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
        if (conversationType == Conversation.ConversationType.PRIVATE) {
            pluginModules.add(new SingleRedPacketProvider());
        } else if (conversationType == Conversation.ConversationType.GROUP || conversationType == Conversation.ConversationType.DISCUSSION) {
            pluginModules.add(new GroupRedPacketProvider(mGetGroupInfoCallback));
        }
        return pluginModules;
    }

    @Override
    public List<IEmoticonTab> getEmoticonTabs() {
        return super.getEmoticonTabs();
    }
}
