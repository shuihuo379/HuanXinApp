package com.itheima.huanxin;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.TextView;

/**
 * 添加好友界面(第一步)
 * @author zhangming
 * @date 2016/06/04
 */
public class AddFriendsOneActivity extends BaseActivity{
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_addfriends_one);
		
		TextView tv_search=(TextView) this.findViewById(R.id.tv_search);
        tv_search.setOnClickListener(new OnClickListener(){
            @Override
            public void onClick(View v) {
            	startActivity(new Intent(AddFriendsOneActivity.this,AddFriendsTwoActivity.class));                
            }
        });
	}
	
	public void back(View view){
        finish();
    }
}
