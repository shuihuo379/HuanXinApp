package com.itheima.huanxin;

import android.os.Bundle;
import android.support.v4.app.FragmentActivity;
import android.view.View;

import com.easemob.chat.EMChatManager;

public class BaseActivity extends FragmentActivity{
	@Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    protected void onResume() {
        super.onResume();
        // onresume时，取消notification显示
        EMChatManager.getInstance().activityResumed();
    }

    @Override
    protected void onStart() {
        super.onStart();
    }
    
    /**
     * 返回
     * @param view
     */
    public void back(View view) {
        finish();
    }
}
