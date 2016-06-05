package com.itheima.huanxin;

import java.util.HashMap;
import java.util.Map;

import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.alibaba.fastjson.JSONException;
import com.alibaba.fastjson.JSONObject;
import com.easemob.util.HanziToPinyin;
import com.itheima.app.Constant;
import com.itheima.app.MyApplication;
import com.itheima.huanxin.db.UserDao;
import com.itheima.huanxin.domain.User;
import com.itheima.huanxin.other.LoadDataFromServer;
import com.itheima.huanxin.other.LoadDataFromServer.DataCallBack;
import com.itheima.huanxin.other.LocalUserInfo;

/**
 * 详细资料
 * @author zhangming
 * @date 2016/06/05
 */
public class UserInfoActivity extends BaseActivity{
	private boolean is_friend = false;
	private String hxid;
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_userinfo);
		
		Button btn_sendmsg = (Button) this.findViewById(R.id.btn_sendmsg);
        ImageView iv_sex = (ImageView) this.findViewById(R.id.iv_sex);
        TextView tv_name = (TextView) this.findViewById(R.id.tv_name);
        final String nick = this.getIntent().getStringExtra("nick");
        final String avatar = this.getIntent().getStringExtra("avatar");
        String sex = this.getIntent().getStringExtra("sex");
        hxid = this.getIntent().getStringExtra("hxid");
        
        if (nick != null && avatar != null && sex != null && hxid != null) {
            tv_name.setText(nick);
            if (sex.equals("1")) {
                iv_sex.setImageResource(R.drawable.ic_sex_male);
            } else if (sex.equals("2")) {
                iv_sex.setImageResource(R.drawable.ic_sex_female);
            } else {
                iv_sex.setVisibility(View.GONE);
            }
            if (MyApplication.getInstance().getContactList().containsKey(hxid)) {
                is_friend = true;
                btn_sendmsg.setText("发消息");
            }
        }

        btn_sendmsg.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                if(hxid.equals(LocalUserInfo.getInstance(getApplicationContext()).getUserInfo("hxid"))){
                    Toast.makeText(getApplicationContext(), "不能和自己聊天。。", Toast.LENGTH_SHORT).show();
                    return ;
                }
                if (is_friend) {
                    Intent intent = new Intent();
                    intent.putExtra("userId", hxid);
                    intent.putExtra("userAvatar", avatar);
                    intent.putExtra("userNick", nick);
                    intent.setClass(UserInfoActivity.this, ChatActivity.class);
                    startActivity(intent);
                } else {
                    Intent intent = new Intent();
                    intent.putExtra("hxid", hxid);
                    // intent.putExtra("avatar", avatar);
                    // intent.putExtra("nick", nick);
                    intent.setClass(UserInfoActivity.this,AddFriendsFinalActivity.class);
                    startActivity(intent);
                }
            }
        });
        
        Button btn_new= (Button) this.findViewById(R.id.btn_new);
        btn_new.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                if(hxid.equals(LocalUserInfo.getInstance(getApplicationContext()).getUserInfo("hxid"))){
                     Toast.makeText(getApplicationContext(), "不能和自己聊天。。", Toast.LENGTH_SHORT).show();
                     return ;
                }
                Intent intent = new Intent();
                intent.putExtra("userId", hxid);
                intent.putExtra("userNick", nick);
                intent.putExtra("userAvatar", avatar);
                intent.setClass(UserInfoActivity.this, ChatActivity.class);
                startActivity(intent);
            }
        });
       refresh();
	}
	
	private void refresh(){
        Map<String, String> map = new HashMap<String, String>();
        map.put("uid", hxid);
        LoadDataFromServer task = new LoadDataFromServer(UserInfoActivity.this, Constant.URL_Search_User, map);
        task.getData(new DataCallBack() {
            @Override
            public void onDataCallBack(JSONObject data) {
                try {
                    int code = data.getInteger("code");
                    if (code == 1) {
                        JSONObject json = data.getJSONObject("user");
                        String hxid = json.getString("hxid");
                        String fxid = json.getString("fxid");
                        String nick = json.getString("nick");
                        String avatar = json.getString("avatar");
                        String sex = json.getString("sex");
                        String region = json.getString("region");
                        String sign = json.getString("sign");
                        String tel = json.getString("tel");

                        User user = new User();
                        user.setFxid(fxid);
                        user.setUsername(hxid);
                        user.setBeizhu("");
                        user.setNick(nick);
                        user.setRegion(region);
                        user.setSex(sex);
                        user.setTel(tel);
                        user.setSign(sign);
                        user.setAvatar(avatar);
                        setUserHearder(hxid, user);
                       
                        UserDao dao = new UserDao(UserInfoActivity.this);
                        dao.saveContact(user);
                        MyApplication.getInstance().getContactList().put(hxid, user);
                    } 
                } catch (JSONException e) {
                    e.printStackTrace();
                }
            }
        });
    }
	
	protected void setUserHearder(String username, User user) {
       String headerName = null;
       if (!TextUtils.isEmpty(user.getNick())) {
           headerName = user.getNick();
       } else {
           headerName = user.getUsername();
       }
       headerName = headerName.trim();
       if (username.equals(Constant.NEW_FRIENDS_USERNAME)) {
           user.setHeader("");
       } else if (Character.isDigit(headerName.charAt(0))) {
           user.setHeader("#");
       } else {
           user.setHeader(HanziToPinyin.getInstance().get(headerName.substring(0, 1)).get(0).target.substring(0,1).toUpperCase());
           char header = user.getHeader().toLowerCase().charAt(0);
           if (header < 'a' || header > 'z') {
               user.setHeader("#");
           }
       }
	}
}
