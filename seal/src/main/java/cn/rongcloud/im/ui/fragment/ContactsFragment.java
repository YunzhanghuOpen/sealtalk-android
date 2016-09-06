package cn.rongcloud.im.ui.fragment;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.text.Editable;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.nostra13.universalimageloader.core.ImageLoader;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import cn.rongcloud.im.App;
import cn.rongcloud.im.R;
import cn.rongcloud.im.SealAppContext;
import cn.rongcloud.im.SealConst;
import cn.rongcloud.im.db.DBManager;
import cn.rongcloud.im.server.broadcast.BroadcastManager;
import cn.rongcloud.im.server.pinyin.CharacterParser;
import cn.rongcloud.im.server.pinyin.Friend;
import cn.rongcloud.im.server.pinyin.PinyinComparator;
import cn.rongcloud.im.server.pinyin.SideBar;
import cn.rongcloud.im.server.utils.CommonUtils;
import cn.rongcloud.im.server.utils.NToast;
import cn.rongcloud.im.server.utils.RongGenerate;
import cn.rongcloud.im.server.widget.SelectableRoundedImageView;
import cn.rongcloud.im.ui.activity.GroupListActivity;
import cn.rongcloud.im.ui.activity.NewFriendListActivity;
import cn.rongcloud.im.ui.activity.PublicServiceActivity;
import cn.rongcloud.im.ui.activity.SingleContactActivity;
import cn.rongcloud.im.ui.adapter.FriendAdapter;
import io.rong.imkit.RongIM;

/**
 * tab 3 通讯录的 Fragment
 * Created by Bob on 2015/1/25.
 */
public class ContactsFragment extends Fragment implements View.OnClickListener {


    public static ContactsFragment instance = null;

    public static ContactsFragment getInstance() {
        if (instance == null) {
            instance = new ContactsFragment();
        }
        return instance;
    }

    private static final int DELETEFRIEND = 40;

    private EditText mSearch;

    private ListView mListView;

    private List<Friend> dataLsit = new ArrayList<>();

    private List<Friend> sourceDataList = new ArrayList<>();

    private List<Friend> tempList = new ArrayList<>();

    /**
     * 好友列表的 adapter
     */
    private FriendAdapter adapter;
    /**
     * 右侧好友指示 Bar
     */
    private SideBar mSidBar;
    /**
     * 中部展示的字母提示
     */
    public TextView dialog;

    /**
     * 汉字转换成拼音的类
     */
    private CharacterParser characterParser;
    /**
     * 根据拼音来排列ListView里面的数据类
     */
    private PinyinComparator pinyinComparator;

    private LayoutInflater infalter;

    private SelectableRoundedImageView img;

    private TextView name;

    private TextView mNoFriends;
    private TextView unread;

    private String id;
    private String cacheName;

    private View headView;
//    private String deleteId;
//    private AsyncTaskManager atm = AsyncTaskManager.getInstance(getActivity());


    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.activity_address, container, false);
        initView(view);
        initData();
        if (dataLsit != null && dataLsit.size() > 0) {
            sourceDataList = filledData(dataLsit); //过滤数据为有字母的字段  现在有字母 别的数据没有
        } else {
            mNoFriends.setVisibility(View.VISIBLE);
        }

        //还原除了带字母字段的其他数据
        for (int i = 0; i < dataLsit.size(); i++) {
            sourceDataList.get(i).setName(dataLsit.get(i).getName());
            sourceDataList.get(i).setUserId(dataLsit.get(i).getUserId());
            sourceDataList.get(i).setPortraitUri(dataLsit.get(i).getPortraitUri());
            sourceDataList.get(i).setDisplayName(dataLsit.get(i).getDisplayName());
        }

        // 根据a-z进行排序源数据
        Collections.sort(sourceDataList, pinyinComparator);

        infalter = LayoutInflater.from(getActivity());
        headView = infalter.inflate(R.layout.item_contact_list_header,
                                    null);
        unread = (TextView) headView.findViewById(R.id.tv_unread);
        RelativeLayout re_newfriends = (RelativeLayout) headView.findViewById(R.id.re_newfriends);
        RelativeLayout re_group = (RelativeLayout) headView.findViewById(R.id.re_chatroom);
        RelativeLayout re_public = (RelativeLayout) headView.findViewById(R.id.publicservice);
        RelativeLayout seal_me = (RelativeLayout) headView.findViewById(R.id.contact_me_item);
        img = (SelectableRoundedImageView) headView.findViewById(R.id.contact_me_img);
        name = (TextView) headView.findViewById(R.id.contact_me_name);
        SharedPreferences sp = getActivity().getSharedPreferences("config", Context.MODE_PRIVATE);
        id = sp.getString("loginid", "");
        cacheName = sp.getString("loginnickname", "");
        final String header = sp.getString("loginPortrait", "");
        name.setText(cacheName);
        ImageLoader.getInstance().displayImage(TextUtils.isEmpty(header) ? RongGenerate.generateDefaultAvatar(cacheName, id) : header, img, App.getOptions());

        seal_me.setOnClickListener(this);
        re_group.setOnClickListener(this);
        re_newfriends.setOnClickListener(this);
        re_public.setOnClickListener(this);
        adapter = new FriendAdapter(getActivity(), sourceDataList);
        mListView.addHeaderView(headView);
        mListView.setAdapter(adapter);
        mListView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                if (!CommonUtils.isNetworkConnected(getActivity())) {
                    NToast.shortToast(getActivity(), "请检查网络");
                    return;
                }
                if (isFilter) {
                    Friend bean = tempList.get(position);
                    startFriendDetailsPage(bean);
                } else {
                    Friend bean = sourceDataList.get(position - 1);
                    startFriendDetailsPage(bean);
                }
            }
        });
        mListView.setOnItemLongClickListener(new AdapterView.OnItemLongClickListener() {
            @Override
            public boolean onItemLongClick(AdapterView<?> parent, View view, final int position, long id) {
                Friend bean = sourceDataList.get(position - 1);
                startFriendDetailsPage(bean);
                return true;
            }
        });
        mSearch.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                //当输入框里面的值为空，更新为原来的列表，否则为过滤数据列表
                filterData(s.toString());
            }

            @Override
            public void afterTextChanged(Editable s) {
                if (s.length() != 0) {
                    mListView.removeHeaderView(headView);
                } else {
                    mListView.addHeaderView(headView);
                    isFilter = false;
                }
            }
        });
        refreshUIListener();
        return view;
    }

    private void startFriendDetailsPage(Friend friend) {
        Intent intent = new Intent(getActivity(), SingleContactActivity.class);
        intent.putExtra("FriendDetails", friend);
        startActivity(intent);
    }


    private void initData() {
        List<cn.rongcloud.im.db.Friend> list = DBManager.getInstance(getActivity()).getDaoSession().getFriendDao().loadAll();
        if (list != null && list.size() > 0) {
            for (cn.rongcloud.im.db.Friend friend : list) {
                dataLsit.add(new Friend(friend.getUserId(), friend.getName(), friend.getPortraitUri(), friend.getDisplayName(), null, null));
            }

        }
    }


    private void initView(View view) {
        //实例化汉字转拼音类
        characterParser = CharacterParser.getInstance();
        pinyinComparator = PinyinComparator.getInstance();
        mSearch = (EditText) view.findViewById(R.id.search);
        mListView = (ListView) view.findViewById(R.id.listview);
        mNoFriends = (TextView) view.findViewById(R.id.show_no_friend);
        mSidBar = (SideBar) view.findViewById(R.id.sidrbar);
        dialog = (TextView) view.findViewById(R.id.group_dialog);
        mSidBar.setTextView(dialog);
        //设置右侧触摸监听
        mSidBar.setOnTouchingLetterChangedListener(new SideBar.OnTouchingLetterChangedListener() {

            @Override
            public void onTouchingLetterChanged(String s) {
                //该字母首次出现的位置
                int position = adapter.getPositionForSection(s.charAt(0));
                if (position != -1) {
                    mListView.setSelection(position);
                }

            }
        });
    }


    @Override
    public void setUserVisibleHint(boolean isVisibleToUser) {
        super.setUserVisibleHint(isVisibleToUser);
        if (dialog != null) {
            dialog.setVisibility(View.INVISIBLE);
        }
    }

    public TextView getDialog() {
        return dialog;
    }

    public void setDialog(TextView dialog) {
        this.dialog = dialog;
    }

    boolean isFilter;

    /**
     * 根据输入框中的值来过滤数据并更新ListView
     *
     * @param filterStr
     */
    private void filterData(String filterStr) {
        List<Friend> filterDateList = new ArrayList<>();

        if (TextUtils.isEmpty(filterStr)) {
            filterDateList = sourceDataList;
        } else {
            filterDateList.clear();
            for (Friend friendModel : sourceDataList) {
                String name = friendModel.getName();
                if (name.indexOf(filterStr.toString()) != -1 || characterParser.getSelling(name).startsWith(filterStr.toString())) {
                    filterDateList.add(friendModel);
                }
            }
        }

        // 根据a-z进行排序
        Collections.sort(filterDateList, pinyinComparator);
        isFilter = true;
        tempList = filterDateList;
        adapter.updateListView(filterDateList);
    }


    /**
     * 为ListView填充数据
     *
     * @param
     * @return
     */
    private List<Friend> filledData(List<Friend> lsit) {
        List<Friend> mFriendList = new ArrayList<>();

        for (int i = 0; i < lsit.size(); i++) {
            Friend friendModel = new Friend();
            friendModel.setName(lsit.get(i).getName());
            //汉字转换成拼音
            String pinyin = characterParser.getSelling(lsit.get(i).getName());
            String sortString = pinyin.substring(0, 1).toUpperCase();

            // 正则表达式，判断首字母是否是英文字母
            if (sortString.matches("[A-Z]")) {
                friendModel.setLetters(sortString.toUpperCase());
            } else {
                friendModel.setLetters("#");
            }

            mFriendList.add(friendModel);
        }
        return mFriendList;

    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.re_newfriends:
                unread.setVisibility(View.GONE);
                Intent intent = new Intent(getActivity(), NewFriendListActivity.class);
                startActivityForResult(intent, 20);
                break;
            case R.id.re_chatroom:
                startActivity(new Intent(getActivity(), GroupListActivity.class));
                break;
            case R.id.publicservice:
                Intent intentPublic = new Intent(getActivity(), PublicServiceActivity.class);
                startActivity(intentPublic);
                break;
            case R.id.contact_me_item:
                RongIM.getInstance().startPrivateChat(getActivity(), id, cacheName);
                break;
        }
    }


    private void refreshUIListener() {
        BroadcastManager.getInstance(getActivity()).addAction(SealAppContext.UPDATE_FRIEND, new BroadcastReceiver() {
            @Override
            public void onReceive(Context context, Intent intent) {
                String command = intent.getAction();
                if (!TextUtils.isEmpty(command)) {
                    updateUI();
                }
            }
        });

        BroadcastManager.getInstance(getActivity()).addAction(SealAppContext.UPDATE_RED_DOT, new BroadcastReceiver() {
            @Override
            public void onReceive(Context context, Intent intent) {
                String command = intent.getAction();
                if (!TextUtils.isEmpty(command)) {
                    unread.setVisibility(View.INVISIBLE);
                }
            }
        });
        BroadcastManager.getInstance(getActivity()).addAction(SealConst.CHANGEINFO, new BroadcastReceiver() {
            @Override
            public void onReceive(Context context, Intent intent) {
                SharedPreferences sp = getActivity().getSharedPreferences("config", Context.MODE_PRIVATE);
                id = sp.getString("loginid", "");
                cacheName = sp.getString("loginnickname", "");
                String header = sp.getString("loginPortrait", "");
                name.setText(cacheName);
                ImageLoader.getInstance().displayImage(TextUtils.isEmpty(header) ? RongGenerate.generateDefaultAvatar(cacheName, id) : header, img, App.getOptions());
            }
        });

    }


    @Override
    public void onDestroy() {
        super.onDestroy();
        BroadcastManager.getInstance(getActivity()).destroy(SealAppContext.UPDATE_FRIEND);
        BroadcastManager.getInstance(getActivity()).destroy(SealAppContext.UPDATE_RED_DOT);
        BroadcastManager.getInstance(getActivity()).destroy(SealConst.CHANGEINFO);
    }

    private void updateUI() {
        List<cn.rongcloud.im.db.Friend> list = DBManager.getInstance(getActivity()).getDaoSession().getFriendDao().loadAll();
        if (list != null) {
            if (dataLsit != null) {
                dataLsit.clear();
            }
            if (sourceDataList != null) {
                sourceDataList.clear();
            }
            for (cn.rongcloud.im.db.Friend friend : list) {
                dataLsit.add(new Friend(friend.getUserId(), friend.getName(), friend.getPortraitUri(), friend.getDisplayName()));
            }

        }
        if (dataLsit != null) {
            sourceDataList = filledData(dataLsit); //过滤数据为有字母的字段  现在有字母 别的数据没有
        } else {
            mNoFriends.setVisibility(View.VISIBLE);
        }

        //还原除了带字母字段的其他数据
        for (int i = 0; i < dataLsit.size(); i++) {
            sourceDataList.get(i).setName(dataLsit.get(i).getName());
            sourceDataList.get(i).setUserId(dataLsit.get(i).getUserId());
            sourceDataList.get(i).setPortraitUri(dataLsit.get(i).getPortraitUri());
            sourceDataList.get(i).setDisplayName(dataLsit.get(i).getDisplayName());
        }

        // 根据a-z进行排序源数据
        Collections.sort(sourceDataList, pinyinComparator);
        adapter.updateListView(sourceDataList);
    }

}
