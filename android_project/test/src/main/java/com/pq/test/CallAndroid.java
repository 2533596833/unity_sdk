package com.pq.test;

import android.widget.Toast;
import com.unity3d.player.*;
public class CallAndroid {

    public static void GetPhoto(String str){
        UnityPlayer.UnitySendMessage("Main Camera","GetPhoto",str);
    }

}
