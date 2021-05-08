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
import android.content.ContentValues;
import com.unity3d.player.UnityPlayer;
import java.io.IOException;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import android.widget.Toast;
//import android.view.KeyEvent;

public class WebViewActivity extends Activity
{
    public static final int NONE = 0;

    public static final int PHOTORESOULT = 3;//相册
    public static final int PHOTOHRAPH = 2;// 拍照
    public static final int PHOTOCROP = 1;// 拍照结束返回unity
    public static final String IMAGE_UNSPECIFIED = "image/*"; //image  or video 划重点，这里指定视频或图片
    public static final int CAMERA_OK = 1;//请求相机权限
    public static final int STORAGE_OK = 2;//存储权限 获取成功
    private static String UnityPersistentDataPath;//unity中的沙盒文件路径
    //沙盒文件下生成默认图片路径
    private static String UnityUsePicturePath;

    private String LOG_TAG = "LOG_ZDQ";
    @Override
    protected void onCreate(Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);
        String type = this.getIntent().getStringExtra("type");
        UnityPersistentDataPath = this.getIntent().getStringExtra("UnityPersistentDataPath");
        UnityUsePicturePath = UnityPersistentDataPath + "/UNITY_GALLERY_PICTUER.png";
        showLog("WebViewActivity======"+type+"  path===="+UnityPersistentDataPath);
        if(type.equals("photo")){
            checkPhoto();
        }else if(type.equals("camera")){
            checkCamera();
        }else{
            finish();
        }

    }
    //判断相机是否对应的权限
    public void checkCamera(){
        showLog("WebViewActivity=====checkCamera=");
        if(Build.VERSION.SDK_INT > 22){
            //判断是否 有授权相机权限
            if(this.checkSelfPermission(Manifest.permission.CAMERA) != PackageManager.PERMISSION_GRANTED){
                this.requestPermissions(new String[]{Manifest.permission.CAMERA}, CAMERA_OK);//请求 权限
            }else{
                openCamera();
            }
        }else{
            openCamera();
        }
    }
    //判断相册是否对应的权限
    public void checkPhoto(){
        showLog("WebViewActivity=====checkPhoto=");
        if(Build.VERSION.SDK_INT > 22){
            //判断是否 有授权相册权限
            if(this.checkSelfPermission(Manifest.permission.WRITE_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED){
                this.requestPermissions(new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE}, STORAGE_OK);//请求 相册储存权限
            }else{
                openPhoto();
            }
        }else{
            openPhoto();
        }
    }

    //获取权限回调
    @Override
    public void onRequestPermissionsResult(int requestCode, String[] permissions,int[] grantResults) {
        if(requestCode == STORAGE_OK){//相册权限
            if(grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED){
                openPhoto();
            }else{
                showLog("onRequestPermissionsResult==="+Manifest.permission.WRITE_EXTERNAL_STORAGE + " === No authorization");
                finish();
            }
        }else if(requestCode == CAMERA_OK){
            //获得相机权限
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED && grantResults[1] == PackageManager.PERMISSION_GRANTED) {
                openCamera();
            } else {
                showLog("onRequestPermissionsResult==="+Manifest.permission.CAMERA + " === No authorization");
                finish();
            }

        }
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
    }

    //打开相机
    public void openCamera(){
        Intent intent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
//        intent.putExtra(MediaStore.EXTRA_OUTPUT, getImageUri(new File(UnityUsePicturePath)));
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
            File outFile = new File(UnityUsePicturePath);
            intent.putExtra(MediaStore.EXTRA_OUTPUT, getImageContentUri(outFile));
        } else {
            intent.putExtra(MediaStore.EXTRA_OUTPUT, Uri.fromFile(new File(UnityUsePicturePath)));
        }
        this.startActivityForResult(intent, PHOTOHRAPH);
    }
    //打开相册
    public void openPhoto(){
        Intent intent = new Intent(Intent.ACTION_PICK, null);
        intent.setDataAndType(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, IMAGE_UNSPECIFIED);
        this.startActivityForResult(intent, PHOTORESOULT);
    }

    //选择照片的回到
    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        showLog( "requestCode=======" + requestCode+"  resultCode====="+resultCode);
        if (resultCode == NONE)
        {
            backPhotoPath("");
            this.finish();
            return;
        }

        if(requestCode == PHOTORESOULT){//请求码 == 相册3
            if (data == null)
            {
                backPhotoPath("");
                this.finish();
                return;
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

            backPhotoPath(_path);
            this.finish();
        }else if(requestCode == PHOTOHRAPH){//请求码==相机拍照2
            String path = UnityUsePicturePath;
//            backPhotoPath(path);
//            finish();
//            File picture = new File(path);
//            StartPhotoZoom(Uri.fromFile(picture));

            Bundle extras = data.getExtras();
            showLog("data==="+data+"  extras==="+extras);
            if (extras != null) {
                Bitmap photo = extras.getParcelable("data");
                try {
                    SaveBitmap(photo);
                } catch (IOException e) {
                    finish();
                    e.printStackTrace();
                }
            }

        }else if(requestCode == PHOTOCROP) {//请求码==相机拍照结束1
            Bundle extras = data.getExtras();
            if (extras != null) {
                Bitmap photo = extras.getParcelable("data");
                try {
                    SaveBitmap(photo);
                } catch (IOException e) {
                    finish();
                    e.printStackTrace();
                }
            }
//            String path = UnityUsePicturePath;
//            backPhotoPath(path);
            finish();
        }else{
            backPhotoPath("");
            this.finish();
        }
    }
    //对图片修改
    private void StartPhotoZoom(Uri uri) {
        Intent intent = new Intent("com.android.camera.action.CROP");
        intent.setDataAndType(uri, IMAGE_UNSPECIFIED);
        intent.putExtra("crop", "true");
        // aspectX aspectY 是宽高的比例
        intent.putExtra("aspectX", 1);
        intent.putExtra("aspectY", 1);
        // outputX outputY 是裁剪图片宽高
        intent.putExtra("outputX", 300);
        intent.putExtra("outputY", 300);
        intent.putExtra("return-data", true);

        startActivityForResult(intent, PHOTOCROP);
    }


    //存储图片并且通知unity更新
    public void SaveBitmap(Bitmap bitmap) throws IOException {
        FileOutputStream fOut = null;
        //            String FILE_NAME = System.currentTimeMillis() + ".jpg";
        // 一直设置一张图片
        String path = UnityUsePicturePath;
        try {
            //判断路径，如果没有则创建
            File destDir = new File(UnityPersistentDataPath);
            if (!destDir.exists()) {
                destDir.mkdir();
            }

            fOut = new FileOutputStream(path);
        } catch (FileNotFoundException e) {
            e.printStackTrace();
            finish();
        }
        //将bitmap对象写入本地路径中
        bitmap.compress(Bitmap.CompressFormat.PNG, 100, fOut);
        try {
            fOut.flush();
        } catch (IOException e) {
            e.printStackTrace();
            finish();
        }
        try {
            fOut.close();
        } catch (IOException e) {
            e.printStackTrace();
            finish();
        }
        //调用unity中方法 GetImagePath（path）
        backPhotoPath(path);
        finish();
    }



    public Uri getImageContentUri(File imageFile) {
        String filePath = imageFile.getAbsolutePath();
        Cursor cursor = getContentResolver().query(
                MediaStore.Images.Media.EXTERNAL_CONTENT_URI,
                new String[]{MediaStore.Images.Media._ID},
                MediaStore.Images.Media.DATA + "=? ",
                new String[]{filePath}, null);

        if (cursor != null && cursor.moveToFirst()) {
            int id = cursor.getInt(cursor
                    .getColumnIndex(MediaStore.MediaColumns._ID));
            Uri baseUri = Uri.parse("content://media/external/images/media");
            return Uri.withAppendedPath(baseUri, "" + id);
        } else {
            if (imageFile.exists()) {
                ContentValues values = new ContentValues();
                values.put(MediaStore.Images.Media.DATA, filePath);
                return getContentResolver().insert(
                        MediaStore.Images.Media.EXTERNAL_CONTENT_URI, values);
            } else {
                return null;
            }
        }
    }
    public Uri getImageUri(File imageFile) {
        Uri imageUri = null;

        String path = imageFile.getPath();
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.N) {
            imageUri = Uri.fromFile(imageFile);
        } else {
            //兼容android7.0 使用共享文件的形式
            ContentValues contentValues = new ContentValues(1);
            contentValues.put(MediaStore.Images.Media.DATA, path);
            imageUri = this.getApplication().getContentResolver().insert(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, contentValues);
        }
        return imageUri;
    }

    //返回图片路径
    public void backPhotoPath(String str){
        CallAndroid.GetPhoto(str);
    }
    public void showLog(String str){
        Log.d(LOG_TAG, str);
        Toast.makeText(this, str, Toast.LENGTH_SHORT).show();
    }

}
