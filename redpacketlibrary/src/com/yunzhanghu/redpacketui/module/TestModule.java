package com.yunzhanghu.redpacketui.module;

import com.yunzhanghu.redpacketui.message.RongEmptyMessage;
import com.yunzhanghu.redpacketui.message.RongNotificationMessage;
import com.yunzhanghu.redpacketui.message.RongRedPacketMessage;
import com.yunzhanghu.redpacketui.provider.RongGroupRedPacketProvider;
import com.yunzhanghu.redpacketui.provider.RongNotificationMessageProvider;
import com.yunzhanghu.redpacketui.provider.RongRedPacketMessageProvider;
import com.yunzhanghu.redpacketui.provider.RongSingleRedPacketProvider;

import java.util.List;

import io.rong.imkit.DefaultExtensionModule;
import io.rong.imkit.RongContext;
import io.rong.imkit.RongExtension;
import io.rong.imkit.RongIM;
import io.rong.imkit.emoticon.IEmoticonTab;
import io.rong.imkit.plugin.IPluginModule;
import io.rong.imlib.model.Conversation;

/**
 * Created by desert on 17/3/10.
 */

public class TestModule extends DefaultExtensionModule{
    @Override
    public void onInit(String appKey) {
        super.onInit(appKey);
        RongIM.registerMessageType(RongEmptyMessage.class);
        RongIM.registerMessageType(RongNotificationMessage.class);
        RongIM.registerMessageType(RongRedPacketMessage.class);
        RongIM.registerMessageTemplate(new RongNotificationMessageProvider(RongContext.getInstance()));
        RongIM.registerMessageTemplate(new RongRedPacketMessageProvider(RongContext.getInstance()));
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
            pluginModules.add(new RongSingleRedPacketProvider());
        } else if (conversationType == Conversation.ConversationType.GROUP || conversationType == Conversation.ConversationType.DISCUSSION) {
            pluginModules.add(new RongGroupRedPacketProvider());
        }
        return pluginModules;
    }

    @Override
    public List<IEmoticonTab> getEmoticonTabs() {
        return super.getEmoticonTabs();
    }
}
