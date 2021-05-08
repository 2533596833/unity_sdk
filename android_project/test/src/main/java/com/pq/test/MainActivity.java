package com.pq.test;

//import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import com.unity3d.player.UnityPlayer;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.widget.Toast;

public class MainActivity extends UnityPlayerActivity {
    private static String LOG_TAG = "LOG_My";
    Context mContext = null;
    @Override
    protected void onCreate(Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);
        mContext = this;
    }

    public void showToast(String str){
        Toast.makeText(this, str, Toast.LENGTH_SHORT).show();
        Log.d(LOG_TAG,str);
    }

    //Unity中会调用这个方法，用于打开本地相册
    public void TakePhoto(String str)
    {
        Log.d(LOG_TAG,"TakePhoto======"+str);
        Intent intent = new Intent(mContext,WebViewActivity.class);
        intent.putExtra("type", "photo");//传给跳转的Activity 参数一是键 参数二是值
        intent.putExtra("UnityPersistentDataPath", str);
        this.startActivity(intent);
    }
    // Unity中会调用这个方法，用于获取照片 相册或者相机
    public void getPhoto(String type,String path){
        Log.d(LOG_TAG,"getPhoto====type==="+type+" path==="+path);
        Intent intent = new Intent(mContext,WebViewActivity.class);
        intent.putExtra("type", type);//传给跳转的Activity 参数一是键 参数二是值
        intent.putExtra("UnityPersistentDataPath", path);
        this.startActivity(intent);
    }
}