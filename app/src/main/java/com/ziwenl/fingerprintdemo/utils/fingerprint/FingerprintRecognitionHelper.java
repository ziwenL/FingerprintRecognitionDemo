package com.ziwenl.fingerprintdemo.utils.fingerprint;

import android.app.KeyguardManager;
import android.content.Context;
import android.hardware.fingerprint.FingerprintManager;
import android.os.Build;
import android.os.CancellationSignal;
import android.security.keystore.KeyGenParameterSpec;
import android.security.keystore.KeyProperties;
import androidx.annotation.RequiresApi;
import com.ziwenl.fingerprintdemo.R;
import java.security.KeyStore;
import javax.crypto.Cipher;
import javax.crypto.KeyGenerator;
import javax.crypto.SecretKey;

/**
 * Author : Ziwen Lan
 * Date : 2019/11/6
 * Time : 16:36
 * Introduction : 指纹识别帮助类
 * <p>
 * Android6.0以上才支持
 * 要在AndroidManifest.xml中声明权限
 * <uses-permission android:name="android.permission.USE_FINGERPRINT" />
 * 虽然该权限Google声明级别为NormalPermissions,但由于国内厂家定制原因,存在将该权限定为动态权限的定制系统,所以保守起见,使用前还是要确定拥有该权限
 */
public class FingerprintRecognitionHelper {
    private Context mContext;
    private final FingerprintManager mFingerprintManager;
    private CancellationSignal mCancellationSignal;
    private boolean mSelfCancelled;
    private final Callback mCallback;

    /**
     * 指纹识别回调
     */
    @RequiresApi(Build.VERSION_CODES.M)
    private FingerprintManager.AuthenticationCallback mAuthenticationCallback = new FingerprintManager.AuthenticationCallback() {
        @Override
        public void onAuthenticationError(int errorCode, CharSequence errString) {
            //多次指纹验证错误,并且短时间内不可再验
            if (!mSelfCancelled) {
                mCallback.onError(errString);
            }
        }

        @Override
        public void onAuthenticationHelp(int helpCode, CharSequence helpString) {
            //指纹采集失败,可继续验证,可能原因：手指移动过快或指纹识别器上有油污
            mCallback.onWarning(helpString);
        }

        @Override
        public void onAuthenticationSucceeded(FingerprintManager.AuthenticationResult result) {
            //指纹验证成功
            mCallback.onSuccess();
        }

        @Override
        public void onAuthenticationFailed() {
            //指纹验证失败,可重新验证,原因：采集到的指纹跟系统录入的不一致
            mCallback.onWarning(mContext.getString(R.string.fingerprint_not_recognized));
        }
    };

    @RequiresApi(Build.VERSION_CODES.M)
    public FingerprintRecognitionHelper(Context context, Callback callback) {
        mContext = context;
        mFingerprintManager = context.getSystemService(FingerprintManager.class);
        mCallback = callback;
    }

    /**
     * 开始识别
     */
    @RequiresApi(Build.VERSION_CODES.M)
    public void startListening(FingerprintManager.CryptoObject cryptoObject) {
        if (getFingerprintAuthAvailable(mContext) != AvailableStatus.AVAILABLE) {
            mCallback.onWarning("指纹识别不可用");
            return;
        }
        mCancellationSignal = new CancellationSignal();
        mSelfCancelled = false;
        mFingerprintManager.authenticate(cryptoObject, mCancellationSignal, 0, mAuthenticationCallback, null);
    }

    /**
     * 停止识别
     */
    public void stopListening() {
        if (mCancellationSignal != null) {
            mSelfCancelled = true;
            mCancellationSignal.cancel();
            mCancellationSignal = null;
        }
    }

    /**
     * 创建CryptoObject
     *
     * @param keyName KeyStore键值
     */
    @RequiresApi(Build.VERSION_CODES.M)
    public FingerprintManager.CryptoObject createCryptoObject(String keyName) {
        return new FingerprintManager.CryptoObject(createCipher(keyName));
    }

    /**
     * 创建Cipher对象
     * Android指纹认证API要求的标准用法
     */
    @RequiresApi(Build.VERSION_CODES.M)
    private Cipher createCipher(String keyName) {
        try {
            //1.生成对称加密的Key
            KeyStore keyStore = KeyStore.getInstance("AndroidKeyStore");
            keyStore.load(null);
            if (keyStore.getKey(keyName, null) == null) {
                KeyGenerator keyGenerator = KeyGenerator.getInstance(KeyProperties.KEY_ALGORITHM_AES, "AndroidKeyStore");
                KeyGenParameterSpec.Builder builder = new KeyGenParameterSpec.Builder(keyName,
                        KeyProperties.PURPOSE_ENCRYPT |
                                KeyProperties.PURPOSE_DECRYPT)
                        .setBlockModes(KeyProperties.BLOCK_MODE_CBC)
                        .setUserAuthenticationRequired(true)
                        .setEncryptionPaddings(KeyProperties.ENCRYPTION_PADDING_PKCS7);
                keyGenerator.init(builder.build());
                keyGenerator.generateKey();
            }
            //2.生成Cipher对象
            SecretKey key = (SecretKey) keyStore.getKey(keyName, null);
            Cipher cipher = Cipher.getInstance(KeyProperties.KEY_ALGORITHM_AES + "/"
                    + KeyProperties.BLOCK_MODE_CBC + "/"
                    + KeyProperties.ENCRYPTION_PADDING_PKCS7);
            cipher.init(Cipher.ENCRYPT_MODE, key);
            return cipher;
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    public interface Callback {
        /**
         * 认证成功
         */
        void onSuccess();

        /**
         * 识别失败，可以继续识别
         */
        void onWarning(CharSequence helpString);

        /**
         * 识别错误(结束)，短时间内无法继续识别
         */
        void onError(CharSequence errString);
    }


    /**
     * 指纹识别功能是否可用枚举
     */
    public enum AvailableStatus {
        /**
         * 没有指纹识别模块
         */
        NOT_HAVE,
        /**
         * 未启用锁屏密码-->未启用指纹识别
         */
        NOT_LOCK,
        /**
         * 未录入指纹
         */
        NOT_ENTRY,
        /**
         * 可用
         */
        AVAILABLE
    }

    /**
     * 判断指纹识别是否可用
     */
    @RequiresApi(api = Build.VERSION_CODES.M)
    public static AvailableStatus getFingerprintAuthAvailable(Context context) {
        FingerprintManager fingerprintManager = context.getSystemService(FingerprintManager.class);
        if (!fingerprintManager.isHardwareDetected()) {
            //不存在指纹识别模块
            return AvailableStatus.NOT_HAVE;
        }
        KeyguardManager keyguardManager = context.getSystemService(KeyguardManager.class);
        if (!keyguardManager.isKeyguardSecure()) {
            //不存在锁屏密码-->未启用指纹识别
            return AvailableStatus.NOT_LOCK;
        }
        if (!fingerprintManager.hasEnrolledFingerprints()) {
            //未录入指纹
            return AvailableStatus.NOT_ENTRY;
        }
        //指纹识别可用
        return AvailableStatus.AVAILABLE;
    }
}
