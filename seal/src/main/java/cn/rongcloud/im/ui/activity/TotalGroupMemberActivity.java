package cn.rongcloud.im.ui.activity;

import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.BaseAdapter;
import android.widget.ListView;
import android.widget.TextView;

import com.nostra13.universalimageloader.core.ImageLoader;

import java.util.List;

import cn.rongcloud.im.App;
import cn.rongcloud.im.R;
import cn.rongcloud.im.server.response.GetGroupMemberResponse;
import cn.rongcloud.im.server.utils.RongGenerate;
import cn.rongcloud.im.server.widget.SelectableRoundedImageView;
import io.rong.imlib.model.Conversation;
import io.rong.imlib.model.UserInfo;

/**
 * Created by AMing on 16/7/1.
 * Company RongCloud
 */
public class TotalGroupMemberActivity extends BaseActivity {

    private List<GetGroupMemberResponse.ResultEntity> mGroupMember;

    private ListView mTotalListView;


    private TotalGroupMember adapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_toatl_member);
        getSupportActionBar().setTitle(R.string.total_member);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        initViews();
        mGroupMember = (List<GetGroupMemberResponse.ResultEntity>) getIntent().getSerializableExtra("TotalMember");
        if (mGroupMember != null && mGroupMember.size() > 0) {
            getSupportActionBar().setTitle(getString(R.string.total_member) + "(" + mGroupMember.size() + ")");
            adapter = new TotalGroupMember(mGroupMember, mContext);
            mTotalListView.setAdapter(adapter);
            mTotalListView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
                @Override
                public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                    GetGroupMemberResponse.ResultEntity bean = mGroupMember.get(position);
                    UserInfo userInfo = new UserInfo(bean.getUser().getId(), bean.getUser().getNickname(), Uri.parse(TextUtils.isEmpty(bean.getUser().getPortraitUri()) ? RongGenerate.generateDefaultAvatar(bean.getUser().getNickname(), bean.getUser().getId()) : bean.getUser().getPortraitUri()));
                    Intent intent = new Intent(mContext, PersonalProfileActivity.class);
                    intent.putExtra("userinfo", userInfo);
                    intent.putExtra("conversationType", Conversation.ConversationType.GROUP.getValue());
//                    intent.putExtra("groupinfo", mGroup);
                    startActivity(intent);
                }
            });
        }

    }

    private void initViews() {
        mTotalListView = (ListView) findViewById(R.id.total_listview);
    }


    class TotalGroupMember extends BaseAdapter {

        private List<GetGroupMemberResponse.ResultEntity> list;

        private Context context;

        private ViewHolder holder;


        public TotalGroupMember(List<GetGroupMemberResponse.ResultEntity> list, Context mContext) {
            this.list = list;
            this.context = mContext;
        }

        @Override
        public int getCount() {
            return list.size();
        }

        @Override
        public Object getItem(int position) {
            return list.get(position);
        }

        @Override
        public long getItemId(int position) {
            return position;
        }

        @Override
        public View getView(int position, View convertView, ViewGroup parent) {
            if (convertView == null) {
                holder = new ViewHolder();
                convertView = LayoutInflater.from(context).inflate(R.layout.share_item, null);
                holder.mImageView = (SelectableRoundedImageView) convertView.findViewById(R.id.share_icon);
                holder.title = (TextView) convertView.findViewById(R.id.share_name);
                convertView.setTag(holder);
            } else {
                holder = (ViewHolder) convertView.getTag();
            }
            GetGroupMemberResponse.ResultEntity bean = list.get(position);
            ImageLoader.getInstance().displayImage(TextUtils.isEmpty(bean.getUser().getPortraitUri()) ? RongGenerate.generateDefaultAvatar(bean.getUser().getNickname(), bean.getUser().getId()) : bean.getUser().getPortraitUri(), holder.mImageView, App.getOptions());
            holder.title.setText(bean.getUser().getNickname());
            return convertView;
        }
    }


    final static class ViewHolder {
        /**
         * 头像
         */
        SelectableRoundedImageView mImageView;

        TextView title;
    }
}
