package com.itheima.huanxin;

import java.io.File;

import android.content.Intent;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.content.pm.PackageManager.NameNotFoundException;
import android.os.Bundle;
import android.view.View;

import com.easemob.chat.EMChatManager;
import com.easemob.chat.EMGroupManager;
import com.itheima.app.DemoHXSDKHelper;
import com.itheima.util.SDCardUtil;

/**
 * 闪屏页
 * @author zhangming
 * @date 2016/06/01
 */
public class SplashActivity extends BaseActivity{
	private static final int sleepTime = 2000;
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		final View view = View.inflate(this,R.layout.activity_splash,null);
		setContentView(view);
		initFile();
	}
	
	@Override
	protected void onStart() {
		super.onStart();
		new Thread(new Runnable() {
			@Override
			public void run() {
				if(DemoHXSDKHelper.getInstance().isLogined()){
					// ** 免登陆情况 加载所有本地群和会话
					//不是必须的，不加sdk也会自动异步去加载(不会重复加载)；
					//加上的话保证进了主页面会话和群组都已经load完毕
					long start = System.currentTimeMillis();
					EMGroupManager.getInstance().loadAllGroups();
					EMChatManager.getInstance().loadAllConversations();
					long costTime = System.currentTimeMillis() - start;
					//等待sleeptime时长
					if (sleepTime - costTime > 0) {
						try {
							Thread.sleep(sleepTime - costTime);
						} catch (InterruptedException e) {
							e.printStackTrace();
						}
					}
					//进入主页面
					startActivity(new Intent(SplashActivity.this, MainActivity.class));
					finish();
				}else {
					try {
						Thread.sleep(sleepTime);
					} catch (InterruptedException e) {
						e.printStackTrace();
					}
					startActivity(new Intent(SplashActivity.this, LoginActivity.class));
					finish();
				}
			}
		}).start();
	}
	

	private void initFile() {
		 File MyAppDir = null;
		 if(SDCardUtil.ExistSDCard()){
			 String sdRootDir = SDCardUtil.getNormalSDPath();
			 MyAppDir= new File(sdRootDir,"HuanXinApp");
	         if (!MyAppDir.exists()) {
	        	 MyAppDir.mkdirs();
	         }
		 }else{
			 String memRootDir = SDCardUtil.getPhoneCardPath();
			 MyAppDir = new File(memRootDir,"HuanXinApp");
			 if (!MyAppDir.exists()) {
	        	 MyAppDir.mkdirs();
	         }
		 }
	}
	
	/**
	 * 获取当前应用程序的版本号
	 */
	private String getVersion() {
		PackageManager pm = getPackageManager();
		try {
			PackageInfo packinfo = pm.getPackageInfo(getPackageName(), 0);
			String version = packinfo.versionName;
			return version;
		} catch (NameNotFoundException e) {
			e.printStackTrace();
			return "版本号错误";
		}
	}
}
