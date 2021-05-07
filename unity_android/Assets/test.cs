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
            if (GUILayout.Button("相册", GUILayout.Width(200), GUILayout.Height(200))) {
                using (AndroidJavaClass jc = new AndroidJavaClass("com.unity3d.player.UnityPlayer")) {
                    using (AndroidJavaObject jo = jc.GetStatic<AndroidJavaObject>("currentActivity")) {
                        jo.Call("TakePhoto", Application.persistentDataPath);
                    }
                }
            }
            if (GUILayout.Button("检查相册权限", GUILayout.Width(200), GUILayout.Height(200))) {
                showLog(Permission.ExternalStorageRead +"==="+ Permission.HasUserAuthorizedPermission(Permission.ExternalStorageRead)+"\n"+
                    Permission.ExternalStorageWrite + "===" + Permission.HasUserAuthorizedPermission(Permission.ExternalStorageWrite));
            }
            if (GUILayout.Button("请求相册权限", GUILayout.Width(200), GUILayout.Height(200))) {
                if (!Permission.HasUserAuthorizedPermission(Permission.ExternalStorageRead)) {
                    Permission.RequestUserPermission(Permission.ExternalStorageRead);
                }
                if (!Permission.HasUserAuthorizedPermission(Permission.ExternalStorageWrite)) {
                    Permission.RequestUserPermission(Permission.ExternalStorageWrite);
                }
                showLog(Permission.ExternalStorageRead + "==2=" + Permission.HasUserAuthorizedPermission(Permission.ExternalStorageRead) + "\n" +
                    Permission.ExternalStorageWrite + "==2=" + Permission.HasUserAuthorizedPermission(Permission.ExternalStorageWrite));
            }
            if (GUILayout.Button("Toaset", GUILayout.Width(200), GUILayout.Height(200))) {
                using (AndroidJavaClass jc = new AndroidJavaClass("com.unity3d.player.UnityPlayer")) {
                    using (AndroidJavaObject jo = jc.GetStatic<AndroidJavaObject>("currentActivity")) {
                        jo.Call("showToast","tttt===="+ Application.persistentDataPath);
                    }
                }
            }
            GUILayout.Label(ttt, GUILayout.Width(200), GUILayout.Height(200));
        }
        catch (Exception e) {
            showLog("OnGUI===error=====", e);
            throw;
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
