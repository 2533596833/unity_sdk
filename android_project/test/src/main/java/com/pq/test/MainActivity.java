package com.pq.test;

//import android.support.v7.app.AppCompatActivity;
import android.content.ComponentName;
import android.content.pm.PackageManager;
import android.os.Bundle;
import com.unity3d.player.UnityPlayer;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.widget.Toast;

import java.io.File;
import java.util.HashMap;

public class MainActivity extends UnityPlayerActivity {
    private static String LOG_TAG = "LOG_My";
    Context mContext = null;
    private HashMap<String, ComponentName> componentNameDic;
    private PackageManager packageManager;
    private ComponentName defaultComponent;
    private ComponentName curComponent;

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
    public void getPhoto(String type,String path,boolean isCrop,boolean isScaleBitmap,int cropImgWidth,int cropImgHeight){
        Log.d(LOG_TAG,"getPhoto====type==="+type+" path==="+path+" isCrop="+isCrop+" isScaleBitmap"+isScaleBitmap+" cropImgWidth="+cropImgWidth+" cropImgHeight"+cropImgHeight);
        Intent intent = new Intent(mContext,WebViewActivity.class);
        intent.putExtra("type", type);//传给跳转的Activity 参数一是键 参数二是值
        intent.putExtra("UnityPersistentDataPath", path);
        intent.putExtra("isCrop", isCrop);
        intent.putExtra("isScaleBitmap", isScaleBitmap);
        intent.putExtra("cropImgWidth", cropImgWidth);
        intent.putExtra("cropImgHeight", cropImgHeight);
        this.startActivity(intent);
    }
    public boolean deleteFile(String path){
        File file = new File(path);
        if(file != null && file.exists() && file.isFile()){
            if(file.delete()){
                Log.d(LOG_TAG,"删除文件成功======"+path);
                return true;
            }
        }
        Log.d(LOG_TAG,"删除文件失败======"+path+"===file===="+file+"==res===="+(file != null ? file.exists()+"-"+file.isFile() : ""));
        return false;
    }

    //---------------android切换app图标----------------------------
    //unity调用初始化icon信息
    public String InitCurUseIconInfo(String icons){
        if(packageManager == null){
            componentNameDic = new HashMap<>();
            packageManager = getApplicationContext().getPackageManager();
            defaultComponent = new ComponentName(getBaseContext(),"com.pq.test.MainActivity" );  //拿到默认的组件
        }
        if(icons == null){
            Log.d(LOG_TAG,"InitCurUseIconInfo,icon config is null.");
            return "";
        }
        Log.d(LOG_TAG,"InitCurUseIconInfo,icon config:"+icons);
        String[] iconStrs = icons.split(",");
        if (iconStrs!=null && iconStrs.length > 0){
            for (int i=0;i<iconStrs.length;i++)
            {
                String tempStr = iconStrs[i];
                if(ComponentIsEnabled(tempStr)){
                    curComponent =GetComponentByName(tempStr);
                    Log.d(LOG_TAG,"cur using icon:"+tempStr);
                    return tempStr;
                }
            }
        }
        curComponent = defaultComponent;
        return "";
    }

    //unity调用换icon
    public boolean changeIcon(String iconName,boolean killApp){
        if(ComponentIsEnabled(iconName)){
            Log.d(LOG_TAG,"changeIcon,but icon is using:"+iconName);
            return false;
        }
        disableComponent(curComponent);
        curComponent = GetComponentByName(iconName);
        if(curComponent == null){
            Log.d(LOG_TAG,"changeIcon curComponent,iconName:"+iconName);
            curComponent = defaultComponent;
            enableComponent(curComponent,false);
            return false;
        }
        else{
            enableComponent(curComponent,killApp);
            return true;
        }
    }

    private ComponentName GetComponentByName(String componentName){
        ComponentName temp = null;
        if(componentName == null || componentName.equals("")|| componentName.equals("MainActivity")|| componentName.equals("default")){
            temp = defaultComponent;
        }
        else{
            if(componentNameDic.containsKey(componentName)){
                temp = componentNameDic.get(componentName);
            }
            else{
                temp = new ComponentName(getBaseContext(),"com.pq.test."+componentName );
                componentNameDic.put(componentName,temp);
            }
        }
        return temp;
    }
    //检测某个icon是否在用
    private boolean ComponentIsEnabled(String componentName){
        ComponentName temp = GetComponentByName(componentName);
        int state = packageManager.getComponentEnabledSetting(temp);
        if (state == PackageManager.COMPONENT_ENABLED_STATE_ENABLED) {
            //已经启用
            return true;
        }
        return false;
    }

    //启用组件
    private void enableComponent(ComponentName componentName,boolean killApp){
        int state = packageManager.getComponentEnabledSetting(componentName);
        if (state == PackageManager.COMPONENT_ENABLED_STATE_ENABLED) {
            //已经启用
            return;
        }
        if (killApp){
            Log.d(LOG_TAG,"killApp and packageManager.enable Setting:"+componentName.toString());
            packageManager.setComponentEnabledSetting(componentName,
                    PackageManager.COMPONENT_ENABLED_STATE_ENABLED,0);
        }
        else{
            Log.d(LOG_TAG,"not kill app and packageManager.enable Setting:"+componentName.toString());
            packageManager.setComponentEnabledSetting(componentName,
                    PackageManager.COMPONENT_ENABLED_STATE_ENABLED,
                    PackageManager.DONT_KILL_APP);
        }
    }

    //禁用组件
    private void disableComponent(ComponentName componentName){
        int state = packageManager.getComponentEnabledSetting(componentName);
        if (state == PackageManager.COMPONENT_ENABLED_STATE_DISABLED) {
            //已经禁用
            return;
        }
        Log.d(LOG_TAG,"packageManager.disable Setting:"+componentName.toString());
        packageManager.setComponentEnabledSetting(componentName,
                PackageManager.COMPONENT_ENABLED_STATE_DISABLED,
                PackageManager.DONT_KILL_APP);
    }


}