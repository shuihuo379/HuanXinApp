package com.itheima.util;

import android.content.Context;
import android.widget.Toast;

public class T{

	public static void show(Context context, String content){
		Toast.makeText(context, content, Toast.LENGTH_SHORT).show();
	}
	
	public static void show(Context context, int resid){
		Toast.makeText(context, resid, Toast.LENGTH_SHORT).show();
	}
}
