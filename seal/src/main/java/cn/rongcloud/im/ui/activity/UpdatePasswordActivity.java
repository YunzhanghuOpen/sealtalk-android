package cn.rongcloud.im.ui.activity;

import android.content.Context;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;

import cn.rongcloud.im.R;
import cn.rongcloud.im.server.network.http.HttpException;
import cn.rongcloud.im.server.response.ChangePasswordResponse;
import cn.rongcloud.im.server.utils.NToast;
import cn.rongcloud.im.server.widget.LoadDialog;

/**
 * Created by AMing on 16/6/23.
 * Company RongCloud
 */
public class UpdatePasswordActivity extends BaseActivity implements View.OnClickListener {

    private static final int UPDATEPASSWORD = 15;

    private EditText oldPasswordEdit, newPasswrodEdit, newPassword2Edit;

    private Button confirm;

    private String mOldPassword, mNewPassword;


    private SharedPreferences sp;

    private SharedPreferences.Editor editor;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_update_pswd);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setHomeAsUpIndicator(R.drawable.de_actionbar_back);
        getSupportActionBar().setTitle(R.string.change_password);
        sp = getSharedPreferences("config", MODE_PRIVATE);
        editor = sp.edit();

        initViews();
    }

    private void initViews() {
        oldPasswordEdit = (EditText) findViewById(R.id.old_password);
        newPasswrodEdit = (EditText) findViewById(R.id.new_password);
        newPassword2Edit = (EditText) findViewById(R.id.new_password2);
        confirm = (Button) findViewById(R.id.update_pswd_confirm);
        confirm.setOnClickListener(this);
    }

    @Override
    public void onClick(View v) {
        String old = oldPasswordEdit.getText().toString().trim();
        String new1 = newPasswrodEdit.getText().toString().trim();
        String new2 = newPassword2Edit.getText().toString().trim();
        String cachePassword = getSharedPreferences("config", Context.MODE_PRIVATE).getString("loginpassword", "");
        if (TextUtils.isEmpty(old)) {
            NToast.shortToast(mContext, R.string.original_password);
            return;
        }
        if (TextUtils.isEmpty(new1)) {
            NToast.shortToast(mContext, R.string.new_password_not_null);
            return;
        }
        if (TextUtils.isEmpty(new2)) {
            NToast.shortToast(
                mContext, R.string.confirm_password_not_null);
            return;
        }
        if (!cachePassword.equals(old)) {
            NToast.shortToast(mContext, R.string.original_password_mistake);
            return;
        }
        if (!new1.equals(new2)) {
            NToast.shortToast(mContext, R.string.passwords_do_not_match);
            return;
        }

        if (new1.equals(old)) {
            NToast.shortToast(mContext, R.string.new_and_old_password);
            return;
        }

        mOldPassword = old;
        mNewPassword = new1;
        LoadDialog.show(mContext);
        request(UPDATEPASSWORD, true);

    }


    @Override
    public Object doInBackground(int requsetCode, String id) throws HttpException {
        return action.changePassword(mOldPassword, mNewPassword);
    }

    @Override
    public void onSuccess(int requestCode, Object result) {
        ChangePasswordResponse cpRes = (ChangePasswordResponse) result;
        if (cpRes.getCode() == 200) {
            NToast.shortToast(mContext, getString(R.string.update_success));
            LoadDialog.dismiss(mContext);
            finish();
        } else if (cpRes.getCode() == 1000) {
            NToast.shortToast(mContext, "初始密码有误:" + cpRes.getCode());
            LoadDialog.dismiss(mContext);
        } else {
            NToast.shortToast(mContext, "修改密码失败:" + cpRes.getCode());
            LoadDialog.dismiss(mContext);
        }
    }

    @Override
    public void onFailure(int requestCode, int state, Object result) {
        LoadDialog.dismiss(mContext);
        NToast.shortToast(mContext, "修改密码请求失败");
    }
}
