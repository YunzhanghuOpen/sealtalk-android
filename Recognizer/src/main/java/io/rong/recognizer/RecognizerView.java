package io.rong.recognizer;

import android.content.Context;
import android.graphics.drawable.AnimationDrawable;
import android.os.Bundle;
import android.os.Environment;
import android.text.TextUtils;
import android.util.AttributeSet;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.iflytek.cloud.ErrorCode;
import com.iflytek.cloud.InitListener;
import com.iflytek.cloud.RecognizerListener;
import com.iflytek.cloud.RecognizerResult;
import com.iflytek.cloud.SpeechConstant;
import com.iflytek.cloud.SpeechError;
import com.iflytek.cloud.SpeechRecognizer;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.json.JSONTokener;

import java.util.Locale;
import java.util.Random;



/**
 * Created by zwfang on 16/11/8.
 */
public class RecognizerView extends RelativeLayout implements RecognizerListener {

    private final static String TAG = "RecognizerView";

    private ImageView imgMic;
    private RelativeLayout rlBottom;


    private Random random;
    private IRecognizedResult resultCallBack;
    private SpeechRecognizer mIat = null;
    AnimationDrawable animStart;
    AnimationDrawable animEnd;

    public RecognizerView(Context context) {
        super(context);
        initViews();
    }

    public RecognizerView(Context context, AttributeSet attrs) {
        super(context, attrs);
        initViews();
    }

    private void initViews() {
        setClickable(true);
        setBackgroundColor(getResources().getColor(R.color.rc_normal));
        RelativeLayout recognizerContainer = (RelativeLayout) LayoutInflater.from(getContext()).inflate(R.layout.rc_view_recognizer, null);
        imgMic = (ImageView) recognizerContainer.findViewById(R.id.img_mic);
        imgMic.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View view) {
                startRecognize();
            }
        });
        TextView tvClear = (TextView) recognizerContainer.findViewById(R.id.btn_clear);
        tvClear.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                if (null != resultCallBack) {
                    resultCallBack.onClearClick();
                }
            }
        });
        rlBottom = (RelativeLayout) recognizerContainer.findViewById(R.id.rl_bottom);
        addView(recognizerContainer);
        random = new Random();
    }

    /**
     * 初始化监听器。
     */
    private static InitListener mInitListener = new InitListener() {

        @Override
        public void onInit(int code) {
            Log.i(TAG, "onInit " + code);
        }
    };

    private void startRecognize() {
        if (null == mIat) {
            mIat = SpeechRecognizer.createRecognizer(getContext(), mInitListener);
        }
        if (mIat.isListening()) {
            return;
        }
        setParam();
        int ret = mIat.startListening(this);
        if (ret != ErrorCode.SUCCESS) {
            Log.d(TAG, "startRecognize ret error " + ret);
        }
    }

    /**
     * 参数设置,设置听写参数，详见《科大讯飞MSC API手册(Android)》SpeechConstant类
     *
     * @param
     * @return
     */
    public void setParam() {
        // 清空参数
        mIat.setParameter(SpeechConstant.PARAMS, null);

        // 设置听写引擎
        mIat.setParameter(SpeechConstant.ENGINE_TYPE, SpeechConstant.TYPE_CLOUD);
        // 设置返回结果格式
        mIat.setParameter(SpeechConstant.RESULT_TYPE, "json");


        mIat.setParameter(SpeechConstant.DOMAIN, "iat");
        if ("zh".equals(Locale.getDefault().getLanguage().toLowerCase())) {
            mIat.setParameter(SpeechConstant.LANGUAGE, "zh_cn");
            mIat.setParameter(SpeechConstant.ACCENT, "mandarin ");
        } else {
            mIat.setParameter(SpeechConstant.LANGUAGE, "en_us");
        }
        // 设置语音前端点:静音超时时间，即用户多长时间不说话则当做超时处理
        mIat.setParameter(SpeechConstant.VAD_BOS, "4000");
        // 设置语音后端点:后端点静音检测时间，即用户停止说话多长时间内即认为不再输入， 自动停止录音
        mIat.setParameter(SpeechConstant.VAD_EOS, "1000");
        // 设置标点符号,设置为"0"返回结果无标点,设置为"1"返回结果有标点
        mIat.setParameter(SpeechConstant.ASR_PTT, "1");

        // 设置音频保存路径，保存音频格式支持pcm、wav，设置路径为sd卡请注意WRITE_EXTERNAL_STORAGE权限
        // 注：AUDIO_FORMAT参数语记需要更新版本才能生效
        mIat.setParameter(SpeechConstant.AUDIO_FORMAT, "wav");
        mIat.setParameter(SpeechConstant.ASR_AUDIO_PATH, Environment.getExternalStorageDirectory() + "/msc/iat.wav");
    }

    private void setRandomImageResource() {
        int num = random.nextInt(3) + 1;
        switch (num) {
            case 1:
                imgMic.setImageResource(R.drawable.volume_1);
                break;
            case 2:
                imgMic.setImageResource(R.drawable.volume_2);
                break;
            default:
                imgMic.setImageResource(R.drawable.volume_3);
                break;
        }
    }

    public void changeVolume(int volume) {

        if (null != imgMic) {
            switch (volume / 2) {
                case 0:
                    setRandomImageResource();
                    break;
                case 1:
                    imgMic.setImageResource(R.drawable.volume_2);
                    break;
                case 2:
                    imgMic.setImageResource(R.drawable.volume_3);
                    break;
                case 3:
                    imgMic.setImageResource(R.drawable.volume_4);
                    break;
                case 4:
                    imgMic.setImageResource(R.drawable.volume_5);
                    break;
                case 5:
                    imgMic.setImageResource(R.drawable.volume_6);
                    break;
                case 6:
                    imgMic.setImageResource(R.drawable.volume_7);
                    break;
                case 7:
                    imgMic.setImageResource(R.drawable.volume_8);
                    break;
                case 8:
                    imgMic.setImageResource(R.drawable.volume_9);
                    break;
                case 9:
                    imgMic.setImageResource(R.drawable.volume_10);
                    break;
                case 10:
                    imgMic.setImageResource(R.drawable.volume_11);
                    break;
                default:
                    imgMic.setImageResource(R.drawable.volume_12);
                    break;
            }
        }
    }


    public void endOfSpeech() {
        if (null == imgMic) return;
        imgMic.setImageResource(R.drawable.rc_anim_speech_end);
        animEnd = (AnimationDrawable) imgMic.getDrawable();
        if (!animEnd.isRunning()) {
            animEnd.start();
        }
    }


    public void beginOfSpeech() {
        if (null == imgMic) return;
        imgMic.setImageResource(R.drawable.rc_anim_speech_start);
        animStart = (AnimationDrawable) imgMic.getDrawable();
        if (!animStart.isRunning()) {
            animStart.start();
        }
    }

    @Override
    protected void onDetachedFromWindow() {
        super.onDetachedFromWindow();
        if (null != mIat) {
            mIat.cancel();
            mIat.destroy();
            mIat = null;
        }
        if (null != resultCallBack) {
            resultCallBack = null;
        }
        if (animEnd != null) {
            animEnd.stop();
            animEnd = null;
        }
        if (animStart != null) {
            animStart.stop();
            animEnd = null;
        }
    }

    private String parseIatResult(String json) {
        {
            StringBuffer ret = new StringBuffer();
            JSONTokener jsonTokener = new JSONTokener(json);
            try {
                JSONObject jsonObject = new JSONObject(jsonTokener);
                JSONArray jsonArray = jsonObject.getJSONArray("ws");
                for (int i = 0; i < jsonArray.length(); ++i) {
                    // 转写结果词，默认使用第一个结果
                    JSONArray items = jsonArray.getJSONObject(i).getJSONArray("cw");
                    JSONObject obj = items.getJSONObject(0);
//				如果需要多候选结果，解析数组其他字段0
//				for(int j = 0; j < items.length(); j++)
//				{
//					JSONObject obj = items.getJSONObject(j);
//					ret.append(obj.getString("w"));
//				}
                    ret.append(obj.getString("w"));
                }
            } catch (JSONException e) {
                e.printStackTrace();
            }
            return ret.toString();
        }

    }

    private void printResult(RecognizerResult result) {
        String json = result.getResultString();
        String text = parseIatResult(json);
        try {
            JSONObject obj = new JSONObject(json);
            boolean isLast = obj.getBoolean("ls");
            if (isLast) {
                endOfSpeech();
            }
        } catch (JSONException e) {
            e.printStackTrace();
        }
        if (!TextUtils.isEmpty(text)) {
            rlBottom.setVisibility(View.VISIBLE);
        }
        if (resultCallBack != null) {
            resultCallBack.onResult(text);
        }
    }

    @Override
    public void onResult(RecognizerResult recognizerResult, boolean b) {
        printResult(recognizerResult);

    }

    @Override
    public void onError(SpeechError speechError) {
        if (speechError.getErrorCode() == ErrorCode.ERROR_NO_NETWORK) {
            Toast.makeText(getContext(), getContext().getString(R.string.check_network), Toast.LENGTH_SHORT).show();
        }
    }

    @Override
    public void onEvent(int eventType, int i1, int i2, Bundle bundle) {
        Log.d(TAG, "RecognizerView onEvent eventType: " + eventType);
    }

    @Override
    public void onVolumeChanged(int volume, byte[] bytes) {
        changeVolume(volume);
    }

    @Override
    public void onBeginOfSpeech() {
        Log.d(TAG, "RecognizerView onBeginOfSpeech");
        beginOfSpeech();
    }

    @Override
    public void onEndOfSpeech() {
        Log.d(TAG, "RecognizerView onEndOfSpeech");
//        endOfSpeech();
    }

    public void setResultCallBack(IRecognizedResult resultCallBack) {
        this.resultCallBack = resultCallBack;
    }

    public void startRecord() {
        startRecognize();
    }

    public void stopRecord() {
        if (null != mIat) {
            mIat.cancel();
            mIat.destroy();
            mIat = null;
        }
        if (null != animEnd) {
            animEnd.stop();
            animEnd = null;
        }
        ViewGroup parent = (ViewGroup) getParent();
        if (null != parent) {
            parent.removeView(this);
        }
        mInitListener = null;
    }
}
