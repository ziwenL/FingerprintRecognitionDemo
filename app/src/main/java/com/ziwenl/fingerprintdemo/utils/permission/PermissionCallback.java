package com.ziwenl.fingerprintdemo.utils.permission;

/**
 * Author : Ziwen Lan
 * Date : 2019/10/25
 * Time : 10:45
 * Introduction :
 */
public interface PermissionCallback {
    /**
     * 请求权限成功回调
     */
    void requestPermissionSuccess(int requestCode);

    /**
     * 请求权限失败回调
     */
    void requestPermissionFailed(int requestCode);
}
