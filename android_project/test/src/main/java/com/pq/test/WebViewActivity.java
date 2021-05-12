package com.pq.test;

import com.unity3d.player.UnityPlayer;

import android.Manifest;
import android.app.Activity;
import android.content.ContentResolver;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.Matrix;
import android.media.MediaScannerConnection;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.provider.MediaStore;
import android.security.keystore.StrongBoxUnavailableException;
import android.util.Log;
import android.content.ContentValues;
import android.widget.Toast;
import android.os.Environment;
import android.graphics.BitmapFactory;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

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
    private Uri mImgUri;
    private Uri mCutUri;
    private boolean isCrop = false;//是否裁剪
    private boolean isScaleBitmap = false;//是否压缩图片
    private int cropImgWidth = 300;//裁剪图片宽度
    private int cropImgHeight = 300;//裁剪图片高度
    private String LOG_TAG = "LOG_My";
    @Override
    protected void onCreate(Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);
        String type = this.getIntent().getStringExtra("type");
        UnityPersistentDataPath = this.getIntent().getStringExtra("UnityPersistentDataPath");
        isCrop = this.getIntent().getBooleanExtra("isCrop",false);
        isScaleBitmap = this.getIntent().getBooleanExtra("isScaleBitmap",false);
        cropImgWidth = this.getIntent().hasExtra("cropImgWidth") ? this.getIntent().getIntExtra("cropImgWidth",300) : 300;
        cropImgHeight = this.getIntent().hasExtra("cropImgHeight") ? this.getIntent().getIntExtra("cropImgHeight",300) : 300;

        UnityUsePicturePath = UnityPersistentDataPath + "/UNITY_GALLERY_PICTUER.png";
        showLog("WebViewActivity======"+type+"  path===="+UnityPersistentDataPath+"==UnityUsePicturePath==="+UnityUsePicturePath+" isCrop="+isCrop+" isScaleBitmap"+isScaleBitmap+" cropImgWidth="+cropImgWidth+" cropImgHeight"+cropImgHeight);
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
                this.requestPermissions(new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE,Manifest.permission.READ_EXTERNAL_STORAGE}, STORAGE_OK);//请求 相册储存权限
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
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED ) {
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
        File imgFile = createFile("temp_"+getNowTime()+".png",false);
//        File imgFile = new File(Environment.getExternalStorageDirectory()+ "/DCIM/Camera/temp.png");
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
            mImgUri = getImageContentUri(imgFile);
//            mImgUri = getImageContentUri(new File(UnityUsePicturePath));
//            intent.putExtra(MediaStore.EXTRA_OUTPUT, getImageContentUri(new File(UnityUsePicturePath)));
        } else {
            mImgUri = Uri.fromFile(imgFile);
//            mImgUri = Uri.fromFile(new File(UnityUsePicturePath));
//            intent.putExtra(MediaStore.EXTRA_OUTPUT, Uri.fromFile(new File(UnityUsePicturePath)));
        }
        showLog("打开相机=====mImgUri="+(mImgUri != null ? mImgUri.getPath():"null")+"===imgFile==="+(imgFile != null? imgFile.getPath() : "null"));
        intent.putExtra(MediaStore.EXTRA_OUTPUT, mImgUri);
        intent.addFlags(Intent.FLAG_GRANT_WRITE_URI_PERMISSION);
        intent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);
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
            Uri originalUri = data.getData();
            showLog("相册3======"+isCrop+"==originalUri===="+(originalUri != null ? originalUri.getPath() : ""));
            if(isCrop){
                cropPhoto(originalUri,true);
            }else{
                ContentResolver resolver = getContentResolver();
                Bitmap bm=null;
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
            }

        }else if(requestCode == PHOTOHRAPH){//请求码==相机拍照2
            String path = UnityUsePicturePath;
            showLog("相机拍照2======"+isCrop+"==mImgUri===="+(mImgUri != null ? mImgUri.getPath() : ""));
            if(isCrop){
                cropPhoto(mImgUri,true);
            }else{
                try {
                    trySaveBitmap(mImgUri);
                } catch (FileNotFoundException e) {
                    e.printStackTrace();
                    finish();
                }
            }
        }else if(requestCode == PHOTOCROP) {//请求码==相机拍照结束1
            try {
                trySaveBitmap(mCutUri);
                if(mCutUri != null)  backCropPhotoPath(mCutUri.getPath());
            } catch (FileNotFoundException e) {
                e.printStackTrace();
                finish();
            }
        }else{
            backPhotoPath("");
            this.finish();
        }
    }
    private void cropPhoto(Uri uri, boolean fromCapture)
    {
        Intent intent = new Intent("com.android.camera.action.CROP"); //打开系统自带的裁剪图片的intent
        intent.setDataAndType(uri, "image/*");
        intent.putExtra("crop", "true");

        // 注意一定要添加该项权限，否则会提示无法裁剪
        intent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);
        intent.addFlags(Intent.FLAG_GRANT_WRITE_URI_PERMISSION);

        intent.putExtra("scale", true);

        // 设置裁剪区域的宽高比例
        intent.putExtra("aspectX", 1);
        intent.putExtra("aspectY", 1);

        // 设置裁剪区域的宽度和高度
        intent.putExtra("outputX", cropImgWidth);
        intent.putExtra("outputY", cropImgHeight);

        // 取消人脸识别
        intent.putExtra("noFaceDetection", true);
        // 图片输出格式
//        intent.putExtra("outputFormat", Bitmap.CompressFormat.JPEG.toString());

        // 若为false则表示不返回数据
        intent.putExtra("return-data", false);

//        // 指定裁剪完成以后的图片所保存的位置,pic info显示有延时
//        if (fromCapture) {
//            // 如果是使用拍照，那么原先的uri和最终目标的uri一致,注意这里的uri必须是Uri.fromFile生成的
//            mCutUri = Uri.fromFile(imgFile);
//        } else { // 从相册中选择，那么裁剪的图片保存在take_photo中
//            String time = new SimpleDateFormat("yyyyMMddHHmmss", Locale.CHINA).format(new Date());
//            String fileName = "photo_" + time;
//            File mCutFile = new File(Environment.getExternalStorageDirectory() + "/DCIM/Camera/", fileName + ".jpg");
//            if (!mCutFile.getParentFile().exists()) {
//                mCutFile.getParentFile().mkdirs();
//            }
//            mCutUri = Uri.fromFile(mCutFile);
//        }
        String time = getNowTime();
        mCutUri = Uri.fromFile(createFile("tempCrop_"+time+".png",true));
        intent.putExtra(MediaStore.EXTRA_OUTPUT, mCutUri);
        // 以广播方式刷新系统相册，以便能够在相册中找到刚刚所拍摄和裁剪的照片
        Intent intentBc = new Intent(Intent.ACTION_MEDIA_SCANNER_SCAN_FILE);
        intentBc.setData(uri);
        this.sendBroadcast(intentBc);
        startActivityForResult(intent, PHOTOCROP);
    }


    public void trySaveBitmap(Uri imgUri) throws FileNotFoundException {
        Bitmap bitmap = imgUri != null ? BitmapFactory.decodeStream(getContentResolver().openInputStream(imgUri)) : null;
        showLog("trySaveBitmap======="+(imgUri != null ? imgUri.getPath() : "")+"==bitmap=="+bitmap);
        if (bitmap != null) {
            try {
                SaveBitmap(bitmap);
            } catch (IOException e) {
                finish();
                e.printStackTrace();
            }
        }else{
            finish();
        }
    }
    //存储图片并且通知unity更新
    public void SaveBitmap(Bitmap bitmap) throws IOException {
        FileOutputStream fOut = null;
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
        if(isScaleBitmap){
            scaleBitmap(bitmap).compress(Bitmap.CompressFormat.PNG, 100, fOut);
        }else{
            bitmap.compress(Bitmap.CompressFormat.PNG, 100, fOut);
        }
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
    //压缩bitmap
    public Bitmap scaleBitmap(Bitmap bitmap){
//        BitmapFactory.Options options = new BitmapFactory.Options();
//        options.inSampleSize = 4;
//        ByteArrayOutputStream os = new ByteArrayOutputStream();
//        bitmap.compress(Bitmap.CompressFormat.PNG, 100, os);
//        byte[] bts = os.toByteArray();
//        Bitmap bbb = BitmapFactory.decodeByteArray(bts,0,bts.length,options);
        //缩放法压缩（martix）
        Matrix matrix = new Matrix();
        matrix.setScale(0.5f,0.5f);
        Bitmap bm = Bitmap.createBitmap(bitmap,0,0,bitmap.getWidth(),bitmap.getHeight(),matrix,true);
        return bm;
    }


    public File createFile(String name,boolean create ){
        File file = new File(Environment.getExternalStorageDirectory()+ "/DCIM/Camera/"+name);
        if(create){
            try
            {
                if(file.exists())
                {
                    file.delete();
                }
//            file.getParentFile().mkdirs();
                file.createNewFile();
//            FileOutputStream fout = new FileOutputStream(file);
//            fout.write(0);
//            fout.flush();
//            fout.close();
                showLog("createFile===succ="+file.getPath());
            }catch(IOException e)
            {
                e.printStackTrace();
            }
        }
        return file;
    }

    public boolean deleteFile(String path){
        File file = new File(path);
        if(file != null && file.exists() && file.isFile()){
            if(file.delete()){
                showLog("删除文件成功======"+path);
                return true;
            }
        }
        showLog("删除文件失败======"+path+"===file===="+file+"==res===="+(file != null ? file.exists()+"-"+file.isFile() : ""));
        return false;
    }

    public Uri getImageContentUri(File imageFile) {
        return getImageUri(imageFile);
//        String filePath = imageFile.getAbsolutePath();
//        Cursor cursor = getContentResolver().query(
//                MediaStore.Images.Media.EXTERNAL_CONTENT_URI,
//                new String[]{MediaStore.Images.Media._ID},
//                MediaStore.Images.Media.DATA + "=? ",
//                new String[]{filePath}, null);
//
//        if (cursor != null && cursor.moveToFirst()) {
//            int id = cursor.getInt(cursor
//                    .getColumnIndex(MediaStore.MediaColumns._ID));
//            Uri baseUri = Uri.parse("content://media/external/images/media");
//            return Uri.withAppendedPath(baseUri, "" + id);
//        } else {
//            if (imageFile.exists()) {
//                ContentValues values = new ContentValues();
//                values.put(MediaStore.Images.Media.DATA, filePath);
//                return getContentResolver().insert(
//                        MediaStore.Images.Media.EXTERNAL_CONTENT_URI, values);
//            } else {
//                return null;
//            }
//        }
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
    //获取当前时间文本
    public String getNowTime(){
        return new SimpleDateFormat("yyyyMMddHHmmss", Locale.CHINA).format(new Date());
    }
    //返回图片路径
    public void backPhotoPath(String str){
        CallAndroid.GetPhoto(str);
    }
    //返回裁剪生成的图片路径
    public void backCropPhotoPath(String str){
        CallAndroid.DeletePhoto(str);
    }
    public void showLog(String str){
        Log.d(LOG_TAG, str);
        Toast.makeText(this, str, Toast.LENGTH_SHORT).show();
    }

}
