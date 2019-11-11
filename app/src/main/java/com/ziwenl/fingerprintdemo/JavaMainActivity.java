package com.ziwenl.fingerprintdemo;

import android.Manifest;
import android.content.Intent;
import android.os.Build;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import com.ziwenl.fingerprintdemo.utils.fingerprint.FingerprintRecognitionDialog;
import com.ziwenl.fingerprintdemo.utils.fingerprint.FingerprintRecognitionHelper;
import com.ziwenl.fingerprintdemo.utils.permission.PermissionCallback;
import com.ziwenl.fingerprintdemo.utils.permission.RequestPermissionHelper;

import pub.devrel.easypermissions.EasyPermissions;

/**
 * Author : Ziwen Lan
 * Date : 2019/11/8
 * Time : 9:27
 * Introduction :
 */
public class JavaMainActivity extends AppCompatActivity {
    private RequestPermissionHelper mRequestPermissionHelper;
    private FingerprintRecognitionDialog mFingerprintRecognitionDialog;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        Button btnFingerprintRecognition = findViewById(R.id.btn_fingerprint_recognition);
        mRequestPermissionHelper = new RequestPermissionHelper();

        btnFingerprintRecognition.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //指纹识别
                //1.判断系统版本是否高于等于6.0
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                    //2.确认拥有指纹识别权限--(虽然指纹权限为NormalPermissions,但国内部分厂商将此权限定为了危险权限,所以使用前先确定拥有该权限)
                    mRequestPermissionHelper.requestPermissions(JavaMainActivity.this, 2, "获取指纹识别权限", new PermissionCallback() {
                        @Override
                        public void requestPermissionSuccess(int requestCode) {
                            //3.判断指纹识别功能是否可用
                            FingerprintRecognitionHelper.AvailableStatus availableStatus = FingerprintRecognitionHelper.getFingerprintAuthAvailable(JavaMainActivity.this);
                            switch (availableStatus) {
                                case NOT_HAVE:
                                    //TODO 不存在指纹识别模块
                                    break;
                                case NOT_LOCK:
                                    //TODO 未启用指纹识别功能
                                    break;
                                case NOT_ENTRY:
                                    //TODO 未录入指纹
                                    break;
                                case AVAILABLE:
                                    //TODO 指纹识别可用
                                    break;
                            }
                        }

                        @Override
                        public void requestPermissionFailed(int requestCode) {
                            //TODO 未授予指纹识别权限
                        }
                    }, Manifest.permission.USE_FINGERPRINT);
                } else {
                    //TODO 系统版本低于6.0
                }
            }
        });
    }

    //------------权限请求相关 start------------
    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (mRequestPermissionHelper != null) {
            //设置页面授予权限回调
            mRequestPermissionHelper.requestPermissionsAgain(requestCode);
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (mRequestPermissionHelper != null) {
            EasyPermissions.onRequestPermissionsResult(requestCode, permissions, grantResults, mRequestPermissionHelper.getPermissionCallbacks());
        }
    }
    //------------权限请求相关 end------------

    private void a() {
        //指纹识别
        //1.判断系统版本是否高于等于6.0
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            //2.确认拥有指纹识别权限--(虽然指纹权限为NormalPermissions,但国内部分厂商将此权限定为了危险权限,所以使用前先确定拥有该权限)
            mRequestPermissionHelper.requestPermissions(JavaMainActivity.this, 2, "获取指纹识别权限", new PermissionCallback() {
                @Override
                public void requestPermissionSuccess(int requestCode) {
                    //3.判断指纹识别功能是否可用
                    FingerprintRecognitionHelper.AvailableStatus availableStatus = FingerprintRecognitionHelper.getFingerprintAuthAvailable(JavaMainActivity.this);
                    switch (availableStatus) {
                        case NOT_HAVE:
                            //TODO 不存在指纹识别模块
                            break;
                        case NOT_LOCK:
                            //TODO 未启用指纹识别功能
                            break;
                        case NOT_ENTRY:
                            //TODO 未录入指纹
                            break;
                        case AVAILABLE:
                            //TODO 指纹识别可用
                            break;
                    }
                }

                @Override
                public void requestPermissionFailed(int requestCode) {
                    //TODO 未授予指纹识别权限
                }
            }, Manifest.permission.USE_FINGERPRINT);
        }
    }
}
