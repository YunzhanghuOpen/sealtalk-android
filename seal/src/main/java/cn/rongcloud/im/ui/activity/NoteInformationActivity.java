package cn.rongcloud.im.ui.activity;

import android.content.Intent;
import android.graphics.Color;
import android.net.Uri;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.view.View;
import android.widget.EditText;
import android.widget.TextView;

import cn.rongcloud.im.R;
import cn.rongcloud.im.SealAppContext;
import cn.rongcloud.im.db.DBManager;
import cn.rongcloud.im.server.broadcast.BroadcastManager;
import cn.rongcloud.im.server.network.http.HttpException;
import cn.rongcloud.im.server.pinyin.Friend;
import cn.rongcloud.im.server.response.SetFriendDisplayNameResponse;
import cn.rongcloud.im.server.widget.LoadDialog;
import io.rong.imkit.RongIM;
import io.rong.imlib.model.UserInfo;

/**
 * Created by AMing on 16/8/10.
 * Company RongCloud
 */
public class NoteInformationActivity extends BaseActivity {

    private static final int SETDISPLAYNAME = 12;
    private Friend mFriend;

    private EditText mNoteEdit;

    private TextView mNoteSave;

    private String displayName;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        getSupportActionBar().hide();
        setContentView(R.layout.activity_noteinfo);
        mNoteEdit = (EditText) findViewById(R.id.notetext);
        mNoteSave = (TextView) findViewById(R.id.notesave);
        mFriend = (Friend) getIntent().getSerializableExtra("friend");
        if (mFriend != null) {
            mNoteSave.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    LoadDialog.show(mContext);
                    request(SETDISPLAYNAME);
                }
            });
            mNoteSave.setClickable(false);
            mNoteEdit.setText(mFriend.getDisplayName());
            mNoteEdit.setSelection(mNoteEdit.getText().length());
            mNoteEdit.addTextChangedListener(new TextWatcher() {
                @Override
                public void beforeTextChanged(CharSequence s, int start, int count, int after) {

                }

                @Override
                public void onTextChanged(CharSequence s, int start, int before, int count) {
                    if (TextUtils.isEmpty(s.toString())) {
                        mNoteSave.setClickable(false);
                        mNoteSave.setTextColor(Color.parseColor("#9fcdfd"));
                    } else if (s.toString().equals(mFriend.getDisplayName())) {
                        mNoteSave.setClickable(false);
                        mNoteSave.setTextColor(Color.parseColor("#9fcdfd"));
                    } else {
                        mNoteSave.setClickable(true);
                        mNoteSave.setTextColor(getResources().getColor(R.color.white));
                    }
                }

                @Override
                public void afterTextChanged(Editable s) {

                }
            });


        }
    }

    @Override
    public Object doInBackground(int requsetCode, String id) throws HttpException {
        if (requsetCode == SETDISPLAYNAME) {
            return action.setFriendDisplayName(mFriend.getUserId(), mNoteEdit.getText().toString().trim());
        }
        return super.doInBackground(requsetCode, id);
    }

    @Override
    public void onSuccess(int requestCode, Object result) {
        if (result != null) {
            if (requestCode == SETDISPLAYNAME) {
                SetFriendDisplayNameResponse response = (SetFriendDisplayNameResponse) result;
                if (response.getCode() == 200) {
                    //TODO 1 更新通讯录 UI  2 个人详情 UI 3 更新数据库 4 更新服务端数据 5 更新融云缓存
                    DBManager.getInstance(mContext).getDaoSession().getFriendDao().insertOrReplace(new cn.rongcloud.im.db.Friend(mFriend.getUserId(), mFriend.getName(), mFriend.getPortraitUri(), mNoteEdit.getText().toString().trim(), mFriend.getStatus(), mFriend.getTimestamp()));
                    RongIM.getInstance().refreshUserInfoCache(new UserInfo(mFriend.getUserId(), mNoteEdit.getText().toString().trim(), Uri.parse(mFriend.getPortraitUri())));
                    BroadcastManager.getInstance(mContext).sendBroadcast(SealAppContext.UPDATE_FRIEND);
                    Intent intent = new Intent(mContext, SingleContactActivity.class);
                    intent.putExtra("displayName", mNoteEdit.getText().toString().trim());
                    setResult(155, intent);
                    LoadDialog.dismiss(mContext);
                    finish();
                }
            }
        }
    }

    public void finishPage(View view) {
        this.finish();
    }
}
