package com.yunzhanghu.redpacketui.module;

import com.yunzhanghu.redpacketui.message.RongEmptyMessage;
import com.yunzhanghu.redpacketui.message.RongNotificationMessage;
import com.yunzhanghu.redpacketui.message.RongRedPacketMessage;
import com.yunzhanghu.redpacketui.provider.RongGroupRedPacketProvider;
import com.yunzhanghu.redpacketui.provider.RongNotificationMessageProvider;
import com.yunzhanghu.redpacketui.provider.RongRedPacketMessageProvider;
import com.yunzhanghu.redpacketui.provider.RongSingleRedPacketProvider;

import java.util.ArrayList;
import java.util.List;

import io.rong.eventbus.EventBus;
import io.rong.imkit.IExtensionModule;
import io.rong.imkit.RongContext;
import io.rong.imkit.RongExtension;
import io.rong.imkit.RongIM;
import io.rong.imkit.emoticon.IEmoticonTab;
import io.rong.imkit.plugin.IPluginModule;
import io.rong.imlib.model.Conversation;
import io.rong.imlib.model.Message;

/**
 * Created by desert on 17/3/10.
 */

public class RedPacketModule implements IExtensionModule {

    public void onInit(String var1) {
        RongIM.registerMessageType(RongEmptyMessage.class);
        RongIM.registerMessageType(RongNotificationMessage.class);
        RongIM.registerMessageType(RongRedPacketMessage.class);
        RongIM.registerMessageTemplate(new RongNotificationMessageProvider(RongContext.getInstance()));
        RongIM.registerMessageTemplate(new RongRedPacketMessageProvider(RongContext.getInstance()));
        EventBus.getDefault().register(this);
    }

    public void onConnect(String var1) {
    }

    public void onAttachedToExtension(RongExtension var1) {

    }

    public void onDetachedFromExtension() {
    }

    public void onReceivedMessage(Message var1) {
    }

    @Override
    public List<IPluginModule> getPluginModules(Conversation.ConversationType conversationType) {
        ArrayList<IPluginModule> modules = new ArrayList<>();
        if (conversationType == Conversation.ConversationType.PRIVATE) {
            modules.add(new RongSingleRedPacketProvider());
        } else if (conversationType == Conversation.ConversationType.GROUP || conversationType == Conversation.ConversationType.DISCUSSION) {
            modules.add(new RongGroupRedPacketProvider());
        }
        return modules;
    }

    public List<IEmoticonTab> getEmoticonTabs() {
        return null;
    }


    public void onDisconnect() {
    }
}
