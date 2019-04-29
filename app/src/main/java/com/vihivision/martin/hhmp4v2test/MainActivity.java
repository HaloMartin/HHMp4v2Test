package com.vihivision.martin.hhmp4v2test;

import android.Manifest;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Build;
import android.support.annotation.NonNull;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

public class MainActivity extends AppCompatActivity {
    private final static String TAG = "MartinDev";//Only for debug
//    private final static String TAG = "MainActivity";

    /**
     * 因为涉及到危险权限，WRITE_EXTERNAL_STORAGE 和 READ_EXTERNAL_STORAGE，
     * 在API 23以后，即时在AndroidManifest.xml文件中有声明了，也需要在提示用户确认一次
     * 这里我没有进行权限请求的代码，实际测试时，如果要使用，必须请求权限，或者手动开启存储权限，
     * 否则MP4文件初始化会失败
     *
     * 请求权限方法：
     *
     * */
    private final static String[] PERMISSIONS = {Manifest.permission.WRITE_EXTERNAL_STORAGE
            , Manifest.permission.READ_EXTERNAL_STORAGE};

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        // Example of a call to a native method
        TextView tv = findViewById(R.id.sample_text);
        tv.setText("123，木头人！");

        //
        Button recordBtn = findViewById(R.id.record_button);
        recordBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(MainActivity.this, TestMp4Activity.class);
                startActivity(intent);
            }
        });

        //版本判断，当手机系统大于API 23时，才有必要去判断权限是否需要获取
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            //检查权限是否已经获取，WRITE_EXTERNAL_STORAGE 和 READ_EXTERNAL_STORAGE 只需要检查一个
            int i = ContextCompat.checkSelfPermission(this, PERMISSIONS[0]);
            if (i != PackageManager.PERMISSION_GRANTED) {//未授权，弹窗提示请求用户
                showDialogForPermissionRequest();
            }
        }
    }

    //提示用户权限请求
    private void showDialogForPermissionRequest() {
        new AlertDialog.Builder(this)
                .setTitle("存储权限不可用")
                .setMessage("需要权限来保存MP4文件；\n否则，将无法正常使用")
                .setPositiveButton("立即开启", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                        //开始请求权限
                        ActivityCompat.requestPermissions(MainActivity.this, PERMISSIONS, 321);
                    }
                })
                .setNegativeButton("取消", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                        //
                        Toast.makeText(MainActivity.this, "将无法保存MP4文件", Toast.LENGTH_SHORT).show();
                    }
                })
                .setCancelable(false).show();
    }

    /**
     * 用户权限申请的回调
     * */
    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == 321) {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                if (grantResults[0] != PackageManager.PERMISSION_GRANTED) {//用户拒绝了权限
                    Toast.makeText(this, "权限获取失败，请前往手机系统设置进行权限设置", Toast.LENGTH_SHORT).show();
//                    boolean b = shouldShowRequestPermissionRationale(PERMISSIONS[0]);//判断用户是否点击了不再提醒
                }else {
                    Toast.makeText(this, "权限获取成功", Toast.LENGTH_SHORT).show();
                }
            }
        }
    }

}
