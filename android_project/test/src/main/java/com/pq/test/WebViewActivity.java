package com.pq.test;

import android.Manifest;
import android.app.Activity;
import android.content.ContentResolver;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.provider.MediaStore;
import android.util.Log;
import com.unity3d.player.UnityPlayer;
import java.io.IOException;
//import android.view.KeyEvent;

public class WebViewActivity extends Activity
{
    public static final int NONE = 0;
    public static final int PHOTORESOULT = 3;
    public static final String IMAGE_UNSPECIFIED = "image/*"; //image  or video 划重点，这里指定视频或图片
    public static final int STORAGE_OK = 2;//存储权限 获取成功
    private String LOG_TAG = "LOG_ZDQ";
    @Override
    protected void onCreate(Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);
        checkPhoto();
    }
    //判断是否对应的权限
    public void checkPhoto(){
        if(Build.VERSION.SDK_INT > 22){
            //判断是否 有授权相册权限
            if(this.checkSelfPermission(Manifest.permission.WRITE_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED){

                this.requestPermissions(new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE}, STORAGE_OK);//请求 相册储存权限
            }else{
                openPhoto();
            }
        }
    }

    //获取权限回调
    @Override
    public void onRequestPermissionsResult(int requestCode, String[] permissions,int[] grantResults) {
        if(requestCode == STORAGE_OK){//相册权限
            if(grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED){
                openPhoto();
            }else{
                Log.d(LOG_TAG, "拒绝授权相册权限");
                finish();
            }
        }
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
    }


    //打开相册
    public void openPhoto(){
        Intent intent = new Intent(Intent.ACTION_PICK, null);
        intent.setDataAndType(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, IMAGE_UNSPECIFIED);
        startActivityForResult(intent, PHOTORESOULT);
        Log.d(LOG_TAG, "打开相册！");
    }
    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        Log.d(LOG_TAG, "resultCode ：" + resultCode);
        if (resultCode == NONE)
        {
            if (data == null)
            {
                this.finish();
                CallAndroid.GetPhoto("");
            }else
            {
                if(this.isFinishing() == false)
                {
                    this.finish();
                }
                CallAndroid.GetPhoto("");
            }
            return;
        }

        if (data == null)
        {
            this.finish();
            CallAndroid.GetPhoto("");
        }

        ContentResolver resolver = getContentResolver();
        Bitmap bm=null;

        Uri originalUri = data.getData();
        try {
            bm = MediaStore.Images.Media.getBitmap(resolver, originalUri);
        } catch (IOException e) {
            e.printStackTrace();
        }


        String[] proj = {MediaStore.Images.Media.DATA};

        Cursor cursor= getContentResolver().query(originalUri,proj,null,null,null);
        int column_index = cursor.getColumnIndexOrThrow(MediaStore.Images.Media.DATA);
        cursor.moveToFirst();
        String _path = cursor.getString(column_index);

        CallAndroid.GetPhoto(_path);
        super.onActivityResult(requestCode, resultCode, data);
        this.finish();
    }
}
