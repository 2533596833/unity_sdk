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
        Log.d(LOG_TAG,str);
        Intent intent = new Intent(mContext,WebViewActivity.class);
        this.startActivity(intent);
    }
}