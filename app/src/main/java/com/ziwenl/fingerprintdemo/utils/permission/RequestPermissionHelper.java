package com.ziwenl.fingerprintdemo.utils.permission;

import android.app.Activity;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;

import java.util.List;

import pub.devrel.easypermissions.AppSettingsDialog;
import pub.devrel.easypermissions.EasyPermissions;

/**
 * Author : Ziwen Lan
 * Date : 2019/10/25
 * Time : 11:32
 * Introduction : 权限请求帮助类
 */
public class RequestPermissionHelper {
    private Activity mActivity;
    private Fragment mFragment;
    //请求码
    private int mPermissionRequestCode;
    //请求描述
    private String mPermissionsRequestRationale;
    //请求结果回调
    private PermissionCallback mPermissionCallback;
    //请求的权限数组
    private String[] mRequestPremissions;
    //永久拒绝时弹窗
    private AppSettingsDialog mAppSettingsDialog;

    /**
     * EasyPermissions请求结果回调
     */
    private EasyPermissions.PermissionCallbacks mPermissionCallbacks = new EasyPermissions.PermissionCallbacks() {
        @Override
        public void onPermissionsGranted(int requestCode, @NonNull List<String> perms) {
            //获得授予的权限
            //请求的全部权限获取成功才表示获取成功
            if (perms.size() == mRequestPremissions.length) {
                mPermissionCallback.requestPermissionSuccess(requestCode);
            }
        }

        @Override
        public void onPermissionsDenied(int requestCode, @NonNull List<String> perms) {
            //被拒绝的权限
            if ((mActivity == null && EasyPermissions.somePermissionPermanentlyDenied(mFragment, perms))
                    || (mFragment == null && EasyPermissions.somePermissionPermanentlyDenied(mActivity, perms))) {
                //存在权限被永久拒绝 -- 此时要跳转系统软件设置页面，让用户手动给予权限
                if (mAppSettingsDialog == null) {
                    AppSettingsDialog.Builder builder;
                    if (mActivity == null) {
                        builder = new AppSettingsDialog.Builder(mFragment);
                    } else {
                        builder = new AppSettingsDialog.Builder(mActivity);
                    }
                    mAppSettingsDialog = builder
                            .setTitle("权限申请")
                            .setRationale(mPermissionsRequestRationale + "失败，请手动授权")
                            .setPositiveButton("确定")
                            .setNegativeButton("取消")
                            .setRequestCode(mPermissionRequestCode)
                            .build();

                    mAppSettingsDialog.show();
                } else {
                    //权限获取失败
                    mAppSettingsDialog = null;
                    mPermissionCallback.requestPermissionFailed(requestCode);
                }
            } else {
                //权限获取失败
                mPermissionCallback.requestPermissionFailed(requestCode);
            }
        }

        @Override
        public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {

        }
    };


    /**
     * 重复请求
     */
    public void requestPermissionsAgain(int requestCode) {
        if (requestCode == mPermissionRequestCode) {
            if (mActivity == null) {
                requestPermissions(mFragment, mPermissionRequestCode, mPermissionsRequestRationale, mPermissionCallback, mRequestPremissions);
            } else {
                requestPermissions(mActivity, mPermissionRequestCode, mPermissionsRequestRationale, mPermissionCallback, mRequestPremissions);
            }
        }
    }

    /**
     * 请求权限
     *
     * @param requestCode 权限请求码
     * @param rationale   权限请求描述（eg: 申请获取定位权限、图片相关权限）
     * @param perms       Manifest.permission.WRITE_EXTERNAL_STORAGE, Manifest.permission.READ_EXTERNAL_STORAGE, Manifest.permission.ACCESS_FINE_LOCATION
     */
    public void requestPermissions(Activity activity, int requestCode, @NonNull String rationale, PermissionCallback permissionCallback, @NonNull String... perms) {
        mActivity = activity;
        mPermissionRequestCode = requestCode;
        mPermissionsRequestRationale = rationale;
        mPermissionCallback = permissionCallback;
        mRequestPremissions = perms;
        if (EasyPermissions.hasPermissions(mActivity, perms)) {
            //存在权限
            mPermissionCallback.requestPermissionSuccess(requestCode);
        } else {
            //不存在权限，发起申请
            EasyPermissions.requestPermissions(mActivity, rationale, requestCode, perms);
        }
    }

    /**
     * 请求权限
     *
     * @param requestCode 权限请求码
     * @param rationale   权限请求描述（eg: 申请获取定位权限、图片相关权限）
     * @param perms       Manifest.permission.WRITE_EXTERNAL_STORAGE, Manifest.permission.READ_EXTERNAL_STORAGE, Manifest.permission.ACCESS_FINE_LOCATION
     */
    public void requestPermissions(Fragment fragment, int requestCode, @NonNull String rationale, PermissionCallback permissionCallback, @NonNull String... perms) {
        mFragment = fragment;
        mPermissionRequestCode = requestCode;
        mPermissionsRequestRationale = rationale;
        mPermissionCallback = permissionCallback;
        mRequestPremissions = perms;
        if (EasyPermissions.hasPermissions(mFragment.getContext(), perms)) {
            //存在权限
            mPermissionCallback.requestPermissionSuccess(requestCode);
        } else {
            //不存在权限，发起申请
            EasyPermissions.requestPermissions(mFragment, rationale, requestCode, perms);
        }
    }

    public EasyPermissions.PermissionCallbacks getPermissionCallbacks() {
        return mPermissionCallbacks;
    }
}
