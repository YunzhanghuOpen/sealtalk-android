package io.rong.recognizer;

import android.Manifest;
import android.content.Context;
import android.content.Intent;
import android.graphics.drawable.Drawable;
import android.support.v4.app.Fragment;
import android.text.Editable;

import com.iflytek.cloud.SpeechConstant;
import com.iflytek.cloud.SpeechUtility;

import io.rong.imkit.RongExtension;
import io.rong.imkit.plugin.IPluginModule;
import io.rong.imkit.utilities.PermissionCheckUtil;

/**
 * Created by zwfang on 16/11/8.
 */

public class RecognizePlugin implements IPluginModule {
    private RecognizerView recognizerView;

    /**
     * 初始化语音识别实例
     *
     * @param context 上下文
     */
    public void init(Context context) {
        SpeechUtility.createUtility(context.getApplicationContext(), SpeechConstant.APPID + "=581f2927"); //初始化
    }

    @Override
    public Drawable obtainDrawable(Context context) {
        return context.getResources().getDrawable(R.drawable.rc_recognizer_voice_selector);
    }

    @Override
    public String obtainTitle(Context context) {
        return context.getString(R.string.rc_plugin_recognize);
    }

    @Override
    public void onClick(Fragment currentFragment, final RongExtension extension) {
        String[] permissions = {Manifest.permission.RECORD_AUDIO};
        if (!PermissionCheckUtil.requestPermissions(currentFragment, permissions)) {
            return;
        }
        if (SpeechUtility.getUtility() == null) {
            SpeechUtility.createUtility(extension.getContext().getApplicationContext(), SpeechConstant.APPID + "=581f2927"); //初始化
        }
        recognizerView = new RecognizerView(extension.getContext());
        recognizerView.setResultCallBack(new IRecognizedResult() {
            @Override
            public void onResult(String data) {
                Editable editable = extension.getInputEditText().getText();
                String str = editable.toString() + data;
                extension.getInputEditText().setText(str);
                extension.getInputEditText().setSelection(str.length());
            }

            @Override
            public void onClearClick() {
                extension.getInputEditText().setText("");
            }
        });
        extension.addPluginPager(recognizerView);
    }


    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {

    }

    /**
     * 释放语音识别实例。
     */
    public void destroy() {
        if (recognizerView != null) {
            recognizerView.stopRecord();
            recognizerView = null;
        }
        if (SpeechUtility.getUtility() != null) {
            SpeechUtility.getUtility().destroy();
        }
    }
}
