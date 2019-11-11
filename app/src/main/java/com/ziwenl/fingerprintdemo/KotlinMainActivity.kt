package com.ziwenl.fingerprintdemo

import android.Manifest
import android.content.Intent
import android.os.Build
import android.os.Bundle
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.ziwenl.fingerprintdemo.utils.fingerprint.FingerprintRecognitionDialog
import com.ziwenl.fingerprintdemo.utils.fingerprint.FingerprintRecognitionHelper
import com.ziwenl.fingerprintdemo.utils.permission.PermissionCallback
import com.ziwenl.fingerprintdemo.utils.permission.RequestPermissionHelper
import kotlinx.android.synthetic.main.activity_main.*
import pub.devrel.easypermissions.EasyPermissions

class KotlinMainActivity : AppCompatActivity() {
    private lateinit var mRequestPermissionHelper: RequestPermissionHelper
    private var mFingerprintRecognitionDialog: FingerprintRecognitionDialog? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        mRequestPermissionHelper = RequestPermissionHelper()


        btn_fingerprint_recognition.setOnClickListener {
            //指纹识别
            //1.判断系统版本是否高于等于6.0
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                //2.确认拥有指纹识别权限--(虽然指纹权限为NormalPermissions,但国内部分厂商将此权限定为了危险权限,所以使用前先确定拥有该权限)
                mRequestPermissionHelper.requestPermissions(
                    this@KotlinMainActivity,
                    1,
                    "获取指纹识别权限",
                    object : PermissionCallback {
                        override fun requestPermissionSuccess(requestCode: Int) {
                            //3.判断指纹识别功能是否可用
                            val fingerprintAuthAvailableStatus =
                                FingerprintRecognitionHelper.getFingerprintAuthAvailable(this@KotlinMainActivity)
                            when (fingerprintAuthAvailableStatus) {
                                FingerprintRecognitionHelper.AvailableStatus.NOT_HAVE -> {
                                    //TODO 不存在指纹识别模块
                                }
                                FingerprintRecognitionHelper.AvailableStatus.NOT_LOCK -> {
                                    //TODO 未启用指纹识别
                                }
                                FingerprintRecognitionHelper.AvailableStatus.NOT_ENTRY -> {
                                    //TODO 未录入指纹

                                }
                                FingerprintRecognitionHelper.AvailableStatus.AVAILABLE -> {
                                    //指纹识别可用
                                    if (mFingerprintRecognitionDialog == null) {
                                        mFingerprintRecognitionDialog =
                                            FingerprintRecognitionDialog()
                                        mFingerprintRecognitionDialog?.setCallback(object :
                                            FingerprintRecognitionDialog.Callback {
                                            override fun onSuccess() {
                                                //TODO 指纹识别成功
                                                Toast.makeText(this@KotlinMainActivity,"指纹识别成功",Toast.LENGTH_LONG).show()
                                            }

                                            override fun onFailure() {
                                                //TODO 指纹识别失败，且30秒无法再进行指纹识别
                                                Toast.makeText(this@KotlinMainActivity,"指纹识别失败",Toast.LENGTH_LONG).show()
                                            }
                                        })
                                    }
                                    mFingerprintRecognitionDialog?.show(
                                        supportFragmentManager,
                                        KotlinMainActivity::class.java.simpleName
                                    )
                                }
                            }

                        }

                        override fun requestPermissionFailed(requestCode: Int) {
                            //TODO 未授予指纹识别权限
                        }
                    },
                    Manifest.permission.USE_FINGERPRINT
                )
            } else {
                //TODO 系统版本低于6.0
            }

        }
    }


    //------------权限请求相关 start------------
    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        mRequestPermissionHelper.requestPermissionsAgain(requestCode)
    }

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<String>,
        grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        EasyPermissions.onRequestPermissionsResult(
            requestCode,
            permissions,
            grantResults,
            mRequestPermissionHelper.getPermissionCallbacks()
        )
    }
    //------------权限请求相关 end------------
}
