package com.itheima.huanxin;

import android.app.ProgressDialog;
import android.content.Intent;
import android.os.Bundle;

import com.easemob.EMCallBack;
import com.itheima.app.MyApplication;

/**
 * 设置界面
 * @author zhangming
 * @date 2016/06/06
 */
public class SettingActivity extends BaseActivity{
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
	}
	
	/**
	 * 注销功能
	 */
	private void logout(){
		final ProgressDialog pd = new ProgressDialog(SettingActivity.this);
        pd.setMessage("正在退出登陆..");
        pd.setCanceledOnTouchOutside(false);
        pd.show();
        MyApplication.getInstance().logout(new EMCallBack() {
            @Override
            public void onSuccess() {
                SettingActivity.this.runOnUiThread(new Runnable() {
                    public void run() {
                        pd.dismiss();
                        // 重新显示登陆页面
                        finish();
                        startActivity(new Intent(SettingActivity.this, LoginActivity.class));
                    }
                });
            }
            
            @Override
            public void onProgress(int progress, String status) {
                
            }
            
            @Override
            public void onError(int code, String message) {
                
            }
        });
	}
}
