package com.ziwenl.fingerprintdemo.utils.fingerprint;

import android.app.Activity;
import android.content.Context;
import android.hardware.fingerprint.FingerprintManager;
import android.os.Build;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.annotation.RequiresApi;
import androidx.fragment.app.DialogFragment;

import com.ziwenl.fingerprintdemo.R;


/**
 * Author : Ziwen Lan
 * Date : 2019/11/7
 * Time : 9:14
 * Introduction : 指纹识别弹窗
 */
@RequiresApi(Build.VERSION_CODES.M)
public class FingerprintRecognitionDialog extends DialogFragment {
    private static final String KEY_NAME = "default_key_name";
    private static final long ERROR_TIMEOUT_MILLIS = 1600;
    private static final long SUCCESS_DELAY_MILLIS = 1300;

    private Activity mActivity;
    private ImageView mIvFingerprintIcon;
    private TextView mTvFingerprintStatus;
    private FingerprintRecognitionHelper mFingerprintRecognitionHelper;
    private FingerprintManager.CryptoObject mCryptoObject;
    private Callback mCallback;

    @Override
    public void onAttach(@NonNull Context context) {
        super.onAttach(context);
        mActivity = getActivity();
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setRetainInstance(true);
        setStyle(DialogFragment.STYLE_NORMAL, R.style.DialogFragment);
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.dialog_fingerprint_recognition, container, false);
        mIvFingerprintIcon = view.findViewById(R.id.iv_fingerprint_icon);
        mTvFingerprintStatus = view.findViewById(R.id.tv_fingerprint_status);

        mFingerprintRecognitionHelper = new FingerprintRecognitionHelper(mActivity, new FingerprintRecognitionHelper.Callback() {
            @Override
            public void onSuccess() {
                //指纹识别成功
                showSuccess();
                mIvFingerprintIcon.postDelayed(mSuccessRunnable, SUCCESS_DELAY_MILLIS);
            }

            @Override
            public void onWarning(CharSequence helpString) {
                //指纹采集中或采集失败
                if (TextUtils.isEmpty(helpString)) {
                    showTips();
                } else {
                    showError(helpString, true);
                }
            }

            @Override
            public void onError(CharSequence errString) {
                //指纹识别失败,30秒内无法再调用指纹识别功能
                showError(errString, false);
                mIvFingerprintIcon.postDelayed(mFailureRunnable, ERROR_TIMEOUT_MILLIS);
            }
        });
        mCryptoObject = mFingerprintRecognitionHelper.createCryptoObject(KEY_NAME);
        return view;
    }

    @Override
    public void onResume() {
        super.onResume();
        mFingerprintRecognitionHelper.startListening(mCryptoObject);
    }

    @Override
    public void onPause() {
        super.onPause();
        mFingerprintRecognitionHelper.stopListening();
    }


    private void showSuccess() {
        mTvFingerprintStatus.removeCallbacks(mResetUIRunnable);
        mIvFingerprintIcon.setImageResource(R.drawable.ic_fingerprint_success);
        mTvFingerprintStatus.setTextColor(mTvFingerprintStatus.getResources().getColor(R.color.success_color));
        mTvFingerprintStatus.setText(mTvFingerprintStatus.getResources().getString(R.string.fingerprint_success));

    }

    private void showError(CharSequence error, boolean isReset) {
        mTvFingerprintStatus.removeCallbacks(mResetUIRunnable);
        mIvFingerprintIcon.setImageResource(R.drawable.ic_fingerprint_error);
        mTvFingerprintStatus.setText(error);
        mTvFingerprintStatus.setTextColor(mTvFingerprintStatus.getResources().getColor(R.color.warning_color));
        if (isReset) {
            mTvFingerprintStatus.postDelayed(mResetUIRunnable, ERROR_TIMEOUT_MILLIS);
        }
    }

    private void showTips() {
        mIvFingerprintIcon.setImageResource(R.mipmap.ic_fp_40px);
        mTvFingerprintStatus.setTextColor(mTvFingerprintStatus.getResources().getColor(R.color.hint_color));
        mTvFingerprintStatus.setText(mTvFingerprintStatus.getResources().getString(R.string.fingerprint_recognizing));
        mTvFingerprintStatus.removeCallbacks(mResetUIRunnable);
        mTvFingerprintStatus.postDelayed(mResetUIRunnable, ERROR_TIMEOUT_MILLIS);
    }


    private Runnable mResetUIRunnable = new Runnable() {
        @Override
        public void run() {
            mIvFingerprintIcon.setImageResource(R.mipmap.ic_fp_40px);
            mTvFingerprintStatus.setTextColor(mTvFingerprintStatus.getResources().getColor(R.color.hint_color));
            mTvFingerprintStatus.setText(mTvFingerprintStatus.getResources().getString(R.string.fingerprint_hint));
        }
    };

    private Runnable mSuccessRunnable = new Runnable() {
        @Override
        public void run() {
            if (!FingerprintRecognitionDialog.this.isResumed()){
               return;
            }
            dismiss();
            mCallback.onSuccess();

        }
    };

        private Runnable mFailureRunnable = new Runnable() {
        @Override
        public void run() {
            if (!FingerprintRecognitionDialog.this.isResumed()){
                return;
            }
            dismiss();
            mCallback.onFailure();
        }
    };

    public void setCallback(Callback callback) {
        mCallback = callback;
    }

    public interface Callback {
        void onSuccess();

        void onFailure();
    }
}
