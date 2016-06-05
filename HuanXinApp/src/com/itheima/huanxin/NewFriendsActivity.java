package com.itheima.huanxin;

import java.util.List;

import com.itheima.app.Constant;
import com.itheima.app.MyApplication;
import com.itheima.huanxin.adapter.NewFriendsAdapter;
import com.itheima.huanxin.db.InviteMessageDao;
import com.itheima.huanxin.domain.InviteMessage;
import com.itheima.huanxin.domain.User;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.ListView;
import android.widget.TextView;

/**
 * 新的朋友界面
 * @author zhangming
 * @date 2016/06/05
 */
public class NewFriendsActivity extends BaseActivity{
	private ListView listView;
	private NewFriendsAdapter newFriendAdapter;
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_newfriendsmsg);
		
		listView = (ListView) findViewById(R.id.listview);
        TextView et_search = (TextView) findViewById(R.id.et_search);
        TextView tv_add = (TextView) findViewById(R.id.tv_add);
        et_search.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivity(new Intent(NewFriendsActivity.this,AddFriendsTwoActivity.class));
            }
        });
        tv_add.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivity(new Intent(NewFriendsActivity.this,AddFriendsOneActivity.class));
            }
        });
        
        InviteMessageDao dao = new InviteMessageDao(this);
        List<InviteMessage> msgs = dao.getMessagesList();
        newFriendAdapter = new NewFriendsAdapter(this, msgs);
        listView.setAdapter(newFriendAdapter);
        
        User userTemp = MyApplication.getInstance().getContactList().get(Constant.NEW_FRIENDS_USERNAME);
        if (userTemp != null && userTemp.getUnreadMsgCount() != 0) {
            userTemp.setUnreadMsgCount(0);
        }
        MyApplication.getInstance().getContactList().get(Constant.NEW_FRIENDS_USERNAME).setUnreadMsgCount(0);
	}
}
