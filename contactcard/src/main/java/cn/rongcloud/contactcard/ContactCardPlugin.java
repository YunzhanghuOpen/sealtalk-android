package cn.rongcloud.contactcard;

import android.content.Context;
import android.content.Intent;
import android.graphics.drawable.Drawable;
import android.support.v4.app.Fragment;
import android.support.v4.content.ContextCompat;

import cn.rongcloud.contactcard.activities.ContactListActivity;
import cn.rongcloud.contactcard.message.ContactMessage;
import cn.rongcloud.contactcard.message.ContactMessageItemProvider;
import io.rong.imkit.RongExtension;
import io.rong.imkit.RongIM;
import io.rong.imkit.plugin.IPluginModule;
import io.rong.imlib.model.Conversation;

/**
 * Created by Beyond on 2016/11/14.
 */

public class ContactCardPlugin implements IPluginModule {

    private static final int REQUEST_CONTACT = 55;
    private static volatile ContactCardPlugin contactPlugin = null;
    private IContactCardInfoProvider iContactCardInfoProvider;

    private IContactCardClickCallback iContactCardClickCallback;

    private ContactCardPlugin(){

    }

    public static void init() {
        RongIM.registerMessageType(ContactMessage.class); //注册名片消息
        RongIM.registerMessageTemplate(new ContactMessageItemProvider());

    }

    public static ContactCardPlugin getInstance(){
        if(contactPlugin == null){
            synchronized (ContactCardPlugin.class){
                if(contactPlugin == null){
                    contactPlugin = new ContactCardPlugin();
                }
            }
        }
        return contactPlugin;
    }

    public void setContactCardInfoProvider(IContactCardInfoProvider iContactCardInfoProvider) {
        this.iContactCardInfoProvider = iContactCardInfoProvider;
    }

    public IContactCardInfoProvider getContactCardInfoProvider() {
        return iContactCardInfoProvider;
    }

    public IContactCardClickCallback getContactCardClickCallback() {
        return iContactCardClickCallback;
    }

    public void setContactCardClickCallback(IContactCardClickCallback iContactCardClickCallback) {
        this.iContactCardClickCallback = iContactCardClickCallback;
    }

    @Override
    public Drawable obtainDrawable(Context context) {
        return ContextCompat.getDrawable(context, R.drawable.rc_contact_plugin_icon);
    }

    @Override
    public String obtainTitle(Context context) {
        return context.getString(R.string.rc_plugins_contact);
    }

    @Override
    public void onClick(Fragment currentFragment, RongExtension extension) {
        Conversation.ConversationType conversationType = extension.getConversationType();
        String targetId = extension.getTargetId();
        Intent intent = new Intent(currentFragment.getActivity(), ContactListActivity.class);
        intent.putExtra("conversationType", conversationType);
        intent.putExtra("targetId", targetId);
        extension.startActivityForPluginResult(intent, REQUEST_CONTACT, this);
        extension.collapseExtension();
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {

    }
}
