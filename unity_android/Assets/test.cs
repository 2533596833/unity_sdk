using System.Collections;
using System.Collections.Generic;
using UnityEngine;
using UnityEngine.UI;
using System;
using System.IO;
using UnityEngine.Android;
public class test : MonoBehaviour
{
    AndroidJavaObject ajc;

    AndroidJavaClass gallerySdk;
    AndroidJavaObject unityActivity;
    public Text txt;
    public RawImage img;
    public Toggle cropTogle;//是否裁剪
    public Toggle scaleBitmapTogle;//是否裁剪
    public InputField wInput;
    public InputField hInput;
    bool isCrop = false;
    bool isScaleBitmap = false;
    int cropW = 300;
    int cropH = 300;
    public string deletePath;
    string ttt = "";
    void Start()
    {
        try {
            ajc = new AndroidJavaObject("com.pq.test.CallAndroid");
        }
        catch (Exception e) {
            showLog("ajdc==error=======", e);
            throw;
        }
        showLog("ajdc========", ajc);

        //gallerySdk = new AndroidJavaClass("com.pq.android.CameraManager");
        //AndroidJavaClass player = new AndroidJavaClass("com.unity3d.player.UnityPlsayer");
        //unityActivity = player.GetStatic<AndroidJavaObject>("currentActivity");
    }

    private void OnGUI() {
        try {
            if (GUILayout.Button("相册", GUILayout.Width(200), GUILayout.Height(100))) {
                using (AndroidJavaClass jc = new AndroidJavaClass("com.unity3d.player.UnityPlayer")) {
                    using (AndroidJavaObject jo = jc.GetStatic<AndroidJavaObject>("currentActivity")) {
                        jo.Call("getPhoto", "photo", Application.persistentDataPath, isCrop, isScaleBitmap,cropW,cropH);
                    }
                }
            }
            if (GUILayout.Button("相机", GUILayout.Width(200), GUILayout.Height(100))) {
                using (AndroidJavaClass jc = new AndroidJavaClass("com.unity3d.player.UnityPlayer")) {
                    using (AndroidJavaObject jo = jc.GetStatic<AndroidJavaObject>("currentActivity")) {
                        jo.Call("getPhoto", "camera",Application.persistentDataPath, isCrop, isScaleBitmap, cropW, cropH);
                    }
                }
            }
            if (GUILayout.Button("检查相册权限", GUILayout.Width(200), GUILayout.Height(100))) {
                showLog(Permission.ExternalStorageRead +"==="+ Permission.HasUserAuthorizedPermission(Permission.ExternalStorageRead)+"\n"+
                    Permission.ExternalStorageWrite + "===" + Permission.HasUserAuthorizedPermission(Permission.ExternalStorageWrite));
            }
            if (GUILayout.Button("请求相册权限", GUILayout.Width(200), GUILayout.Height(100))) {
                if (!Permission.HasUserAuthorizedPermission(Permission.ExternalStorageRead)) {
                    Permission.RequestUserPermission(Permission.ExternalStorageRead);
                }
                if (!Permission.HasUserAuthorizedPermission(Permission.ExternalStorageWrite)) {
                    Permission.RequestUserPermission(Permission.ExternalStorageWrite);
                }
                showLog(Permission.ExternalStorageRead + "==2=" + Permission.HasUserAuthorizedPermission(Permission.ExternalStorageRead) + "\n" +
                    Permission.ExternalStorageWrite + "==2=" + Permission.HasUserAuthorizedPermission(Permission.ExternalStorageWrite));
            }
            if (GUILayout.Button("检查相机权限", GUILayout.Width(200), GUILayout.Height(100))) {
                showLog(Permission.Camera + "===" + Permission.HasUserAuthorizedPermission(Permission.Camera));
            }
            if (GUILayout.Button("请求相册权限", GUILayout.Width(200), GUILayout.Height(100))) {
                if (!Permission.HasUserAuthorizedPermission(Permission.Camera)) {
                    Permission.RequestUserPermission(Permission.Camera);
                }
                showLog(Permission.Camera + "===" + Permission.HasUserAuthorizedPermission(Permission.Camera));
            }
            if (GUILayout.Button("Toaset", GUILayout.Width(200), GUILayout.Height(100))) {
                using (AndroidJavaClass jc = new AndroidJavaClass("com.unity3d.player.UnityPlayer")) {
                    using (AndroidJavaObject jo = jc.GetStatic<AndroidJavaObject>("currentActivity")) {
                        jo.Call("showToast","tttt===="+ Application.persistentDataPath);
                    }
                }
            }
            if (cropTogle != null) isCrop = cropTogle.isOn;
            if (scaleBitmapTogle != null) isScaleBitmap = scaleBitmapTogle.isOn;
            if (wInput != null) {
                if(int.TryParse(wInput.text,out int r)) {
                    cropW = r;
                }
            }
            if (hInput != null) {
                if (int.TryParse(hInput.text, out int r)) {
                    cropH = r;
                }
            }
        }
        catch (Exception e) {
            showLog("OnGUI===error=====", e);
            throw;
        }
        

    }
    public void callAndroid(string funName,params object[] paras) {
        using (AndroidJavaClass jc = new AndroidJavaClass("com.unity3d.player.UnityPlayer")) {
            using (AndroidJavaObject jo = jc.GetStatic<AndroidJavaObject>("currentActivity")) {
                jo.Call(funName, paras);
            }
        }
    }

    public void GetPhoto(string path) {
        FileStream fileStream = new FileStream(path, FileMode.Open, FileAccess.Read);
        fileStream.Seek(0, SeekOrigin.Begin);
        byte[] bye = new byte[fileStream.Length];
        fileStream.Read(bye, 0, bye.Length);
        fileStream.Close();
        Texture2D texture2D = new Texture2D((int)img.rectTransform.rect.width, (int)img.rectTransform.rect.height);
        texture2D.LoadImage(bye);
        showLog("GetPhoto===path====", path, " texture===", texture2D);
        img.texture = texture2D;
    }
    public void DeletePhoto(string path) {
        deletePath = path;
        try {
            FileInfo ff = new FileInfo(path);
            if (ff != null && ff.Exists) {
                ff.Delete();
                showLog("DeletePhoto===fileinfo===succ==path==", path);
            }
        }
        catch (Exception e) {
            showLog("DeletePhoto====fileinfo===error=====", e);
            
        }
        //using (AndroidJavaClass jc = new AndroidJavaClass("com.unity3d.player.UnityPlayer")) {
        //    using (AndroidJavaObject jo = jc.GetStatic<AndroidJavaObject>("currentActivity")) {
        //        if (jo.Call<bool>("deleteFile", deletePath)) {
        //            showLog("删除图片====succ=" + deletePath);
        //        } else {
        //            showLog("删除图片====error=" + deletePath);
        //        }
        //    }
        //}
    }

    public void showLog(params object[] paras) {
        string res = "";
        for (int i = 0; i < paras.Length; i++) {
            res += paras[i].ToString() + (i>= paras.Length-1 ? "" : " , ");
        }
        Debug.Log(res);
        ttt = res;
        if(txt != null) {
            txt.text = res;
        }
    }


    public T msgToAndroid<T>(string funcName,params object[] paras) {
        T res = ajc.Call<T>(funcName, paras);
        return res;
    }
    public void msgToAndroid(string funcName, params object[] paras) {
        ajc.Call(funcName, paras);
    }

    public void parseAndroidMsg(string json) {
        var jd = JsonUtility.FromJson<Dictionary<string,object>>(json);
        if(jd != null) {
            var funcName = jd.ContainsKey("func_name") ? jd["func_name"].ToString() : null;
            if (!string.IsNullOrEmpty(funcName)) {
                SendMessage(funcName,jd);
            }
        }
    }
}
