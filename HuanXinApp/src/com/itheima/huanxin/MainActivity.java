package com.itheima.huanxin;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import android.annotation.SuppressLint;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentTransaction;
import android.text.TextUtils;
import android.util.Log;
import android.view.KeyEvent;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONException;
import com.alibaba.fastjson.JSONObject;
import com.easemob.chat.EMChat;
import com.easemob.chat.EMChatManager;
import com.easemob.chat.EMContactListener;
import com.easemob.chat.EMContactManager;
import com.easemob.chat.EMConversation;
import com.easemob.chat.EMMessage;
import com.easemob.chat.EMNotifier;
import com.easemob.exceptions.EaseMobException;
import com.easemob.util.HanziToPinyin;
import com.itheima.app.Constant;
import com.itheima.app.MyApplication;
import com.itheima.huanxin.db.InviteMessageDao;
import com.itheima.huanxin.db.UserDao;
import com.itheima.huanxin.domain.InviteMessage;
import com.itheima.huanxin.domain.InviteMessage.InviteMessageStatus;
import com.itheima.huanxin.domain.User;
import com.itheima.huanxin.fragment.FragmentCoversation;
import com.itheima.huanxin.fragment.FragmentFind;
import com.itheima.huanxin.fragment.FragmentFriends;
import com.itheima.huanxin.fragment.FragmentProfile;
import com.itheima.huanxin.other.LoadDataFromServer;
import com.itheima.huanxin.other.LoadDataFromServer.DataCallBack;
import com.itheima.huanxin.view.AddPopWindow;

/**
 * APP主界面
 * @author zhangming
 */
public class MainActivity extends BaseActivity {
    private Fragment[] fragments;
    private FragmentCoversation homefragment;
    private FragmentFriends contactlistfragment;
    private FragmentFind findfragment;
    private FragmentProfile profilefragment;
    
    private ImageView[] imagebuttons;
    private TextView[] textviews;
    private int index;
    private int currentTabIndex;  // 当前fragment的index
    
    private ImageView iv_add;
    private ImageView iv_search;
    
    private InviteMessageDao inviteMessgeDao;
    private UserDao userDao;
    
    private NewMessageBroadcastReceiver msgReceiver;
    
    private TextView unreadLabel;  // 未读消息textview
    private TextView unreadAddressLable;  // 未读通讯录textview
    
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		if (savedInstanceState != null && savedInstanceState.getBoolean(Constant.ACCOUNT_REMOVED,false)) {
            // 防止被移除后，没点确定按钮然后按了home键，长期在后台又进app导致的crash
            // 三个fragment里加的判断同理
            MyApplication.getInstance().logout(null);
            finish();
            startActivity(new Intent(this, LoginActivity.class));
            return;
        } else if (savedInstanceState != null
                && savedInstanceState.getBoolean("isConflict", false)) {
            // 防止被T后，没点确定按钮然后按了home键，长期在后台又进app导致的crash
            // 三个fragment里加的判断同理
            finish();
            startActivity(new Intent(this, LoginActivity.class));
            return;
        }
		setContentView(R.layout.activity_main);
		initView();
	}
	
   @Override
    protected void onResume() {
        super.onResume();
        updateUnreadLabel();
        updateUnreadAddressLable();
        EMChatManager.getInstance().activityResumed();
    }
   
    @Override
    protected void onDestroy() {
    	super.onDestroy();
    	if(msgReceiver!=null){
    		unregisterReceiver(msgReceiver);
    		msgReceiver = null;
    	}
    	if(ackMessageReceiver!=null){
    		unregisterReceiver(ackMessageReceiver);
    		ackMessageReceiver = null;
    	}
    	if(cmdMessageReceiver!=null){
    		unregisterReceiver(cmdMessageReceiver);
    		cmdMessageReceiver = null;
    	}
    }
	
	private void initView(){
		unreadLabel = (TextView) findViewById(R.id.unread_msg_number);
	    unreadAddressLable = (TextView) findViewById(R.id.unread_address_number);
		
		homefragment = new FragmentCoversation();
	    contactlistfragment = new FragmentFriends();
	    findfragment = new FragmentFind();
	    profilefragment = new FragmentProfile();
	    fragments = new Fragment[] {homefragment, contactlistfragment,findfragment, profilefragment};
	
        imagebuttons = new ImageView[4];
        imagebuttons[0] = (ImageView) findViewById(R.id.ib_weixin);
        imagebuttons[1] = (ImageView) findViewById(R.id.ib_contact_list);
        imagebuttons[2] = (ImageView) findViewById(R.id.ib_find);
        imagebuttons[3] = (ImageView) findViewById(R.id.ib_profile);

        imagebuttons[0].setSelected(true);
        textviews = new TextView[4];
        textviews[0] = (TextView) findViewById(R.id.tv_weixin);
        textviews[1] = (TextView) findViewById(R.id.tv_contact_list);
        textviews[2] = (TextView) findViewById(R.id.tv_find);
        textviews[3] = (TextView) findViewById(R.id.tv_profile);
        textviews[0].setTextColor(0xFF45C01A);
        // 添加显示第一个fragment
        getSupportFragmentManager().beginTransaction()
                .add(R.id.fragment_container, homefragment)
                .add(R.id.fragment_container, contactlistfragment)
                .add(R.id.fragment_container, profilefragment)
                .add(R.id.fragment_container, findfragment)
                .hide(contactlistfragment).hide(profilefragment)
                .hide(findfragment).show(homefragment).commit();
        
        
        iv_add = (ImageView) this.findViewById(R.id.iv_add);
        iv_search = (ImageView) this.findViewById(R.id.iv_search);
        iv_add.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                AddPopWindow addPopWindow = new AddPopWindow(MainActivity.this);
                addPopWindow.showPopupWindow(iv_add);
            }
        });
        iv_search.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {

            }
        });
        
        inviteMessgeDao = new InviteMessageDao(this);
        userDao = new UserDao(this);
        
        // 注册一个接收消息的BroadcastReceiver
        msgReceiver = new NewMessageBroadcastReceiver();
        IntentFilter intentFilter = new IntentFilter(EMChatManager.getInstance().getNewMessageBroadcastAction());
        intentFilter.setPriority(3);
        registerReceiver(msgReceiver, intentFilter);
        
        // 注册一个ack回执消息的BroadcastReceiver
        IntentFilter ackMessageIntentFilter = new IntentFilter(EMChatManager.getInstance().getAckMessageBroadcastAction());
        ackMessageIntentFilter.setPriority(3);
        registerReceiver(ackMessageReceiver, ackMessageIntentFilter);
        
        // 注册一个透传消息的BroadcastReceiver
        IntentFilter cmdMessageIntentFilter = new IntentFilter(EMChatManager.getInstance().getCmdMessageBroadcastAction());
        cmdMessageIntentFilter.setPriority(3);
        registerReceiver(cmdMessageReceiver, cmdMessageIntentFilter);
        
        // setContactListener监听联系人的变化等
        EMContactManager.getInstance().setContactListener(new MyContactListener());
        // 通知sdk，UI 已经初始化完毕，注册了相应的receiver和listener, 可以接受broadcast了
        EMChat.getInstance().setAppInited();
	}
	
	/**
     * 新消息广播接收者
     */
    private class NewMessageBroadcastReceiver extends BroadcastReceiver {
        @Override
        public void onReceive(Context context, Intent intent) {
            // 主页面收到消息后，主要为了提示未读，实际消息内容需要到chat页面查看
            String from = intent.getStringExtra("from");
            // 消息id
            String msgId = intent.getStringExtra("msgid");
            EMMessage message = EMChatManager.getInstance().getMessage(msgId);

            // 注销广播接收者，否则在ChatActivity中会收到这个广播
            abortBroadcast();
            //notifyNewMessage(message);
            // 刷新bottom bar消息未读数
            updateUnreadLabel();
            if (currentTabIndex == 0) {
                // 当前页面如果为聊天历史页面，刷新此页面
                if (homefragment != null) {
                    //homefragment.refresh();
                }
            }

        }
    }

    
    /**
     * 消息回执BroadcastReceiver
     */
    private BroadcastReceiver ackMessageReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            abortBroadcast();
            String msgid = intent.getStringExtra("msgid");
            String from = intent.getStringExtra("from");
            EMConversation conversation = EMChatManager.getInstance().getConversation(from);
            if (conversation != null) {
                EMMessage msg = conversation.getMessage(msgid);   // 把message设为已读
                if (msg != null) {
                    msg.isAcked = true;
                }
            }
        }
    };

    /**
     * 透传消息BroadcastReceiver
     */
    private BroadcastReceiver cmdMessageReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            abortBroadcast();
        }
    };
	
	
	/**
     * 好友变化listener
     */
    private class MyContactListener implements EMContactListener {
        @Override
        public void onContactAdded(List<String> usernameList) {
        	Log.i("test","联系人添加...");
            refreshFriendsList();
            // 刷新ui
            if (currentTabIndex == 1){
                contactlistfragment.refresh();
            }
        }

        @Override
        public void onContactDeleted(final List<String> usernameList) {
            // 被删除
            Map<String, User> localUsers =MyApplication.getInstance()
                    .getContactList();
            for (String username : usernameList) {
                localUsers.remove(username);
                userDao.deleteContact(username);
                inviteMessgeDao.deleteMessage(username);
            }
            runOnUiThread(new Runnable() {
                public void run() {
                    updateUnreadLabel();
                    // 刷新ui
                    if (currentTabIndex == 1){
                        contactlistfragment.refresh();
                    }else if (currentTabIndex == 0){
                        //homefragment.refresh();
                    }
                }
            });
        }

        @Override
        public void onContactInvited(String username, String reason) {
        	Log.i("test","收到邀请...");
            // 接到邀请的消息，如果不处理(同意或拒绝)，掉线后，服务器会自动再发过来，所以客户端不需要重复提醒
            List<InviteMessage> msgs = inviteMessgeDao.getMessagesList();
            for (InviteMessage inviteMessage : msgs) {
                if (inviteMessage.getGroupId() == null
                        && inviteMessage.getFrom().equals(username)) {
                    inviteMessgeDao.deleteMessage(username);
                }
            }
            // 自己封装的javabean
            InviteMessage msg = new InviteMessage();
            msg.setFrom(username);
            msg.setTime(System.currentTimeMillis());
            msg.setReason(reason);
            Log.d("test", username + "请求加你为好友,reason: " + reason);
            // 设置相应status
            msg.setStatus(InviteMessageStatus.BEINVITEED);
            notifyNewInviteMessage(msg);
        }

        @Override
        public void onContactAgreed(final String username) {
            List<InviteMessage> msgs = inviteMessgeDao.getMessagesList();
            for (InviteMessage inviteMessage : msgs) {
                if (inviteMessage.getFrom().equals(username)) {
                    return;
                }
            }
            runOnUiThread(new Runnable() {
                public void run() {
                    addFriendToList(username);
                }
            });
        }

        @Override
        public void onContactRefused(String username) {
            // 参考同意，被邀请实现此功能,demo未实现
            Log.i("test", username + "拒绝了你的好友请求");
        }
    }

    public void refreshFriendsList() {
        List<String> usernames = new ArrayList<String>();
        try {
            usernames = EMContactManager.getInstance().getContactUserNames();
        } catch (EaseMobException e1) {
            e1.printStackTrace();
        }
        if (usernames != null && usernames.size() > 0) {
            String totaluser = usernames.get(0);
            for (int i = 1; i < usernames.size(); i++) {
                final String split = "66split88";
                totaluser += split + usernames.get(i);
            }
            totaluser = totaluser.replace(Constant.NEW_FRIENDS_USERNAME, "");
            totaluser = totaluser.replace(Constant.GROUP_USERNAME, "");

            Map<String, String> map = new HashMap<String, String>();
            map.put("uids", totaluser);
            LoadDataFromServer task = new LoadDataFromServer(MainActivity.this,Constant.URL_Friends, map);
            task.getData(new DataCallBack() {
                @Override
                public void onDataCallBack(JSONObject data) {
                    try {
                        int code = data.getInteger("code");
                        if (code == 1) {
                            JSONArray jsonArray = data.getJSONArray("friends");
                            Log.i("test","jsonArray===>"+jsonArray.toJSONString());
                            saveFriends(jsonArray);
                        }
                    } catch (JSONException e) {
                        Log.e("MainActivity", "update friendsLiST ERROR");
                        e.printStackTrace();
                    }
                }
            });
        }
    }
    
    private void saveFriends(JSONArray jsonArray) {
        Map<String, User> map = new HashMap<String, User>();

        if (jsonArray != null) {
            for (int i = 0; i < jsonArray.size(); i++) {
                JSONObject json = (JSONObject) jsonArray.getJSONObject(i);
                try {
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
                    setUserHeader(hxid, user);
                    map.put(hxid, user);
                } catch (JSONException e) {
                    e.printStackTrace();
                }
            }
        }
        // 添加user"申请与通知"
        User newFriends = new User();
        newFriends.setUsername(Constant.NEW_FRIENDS_USERNAME);
        String strChat = getResources().getString(R.string.Application_and_notify);
        newFriends.setNick(strChat);
        newFriends.setBeizhu("");
        newFriends.setFxid("");
        newFriends.setHeader("");
        newFriends.setRegion("");
        newFriends.setSex("");
        newFriends.setTel("");
        newFriends.setSign("");
        newFriends.setAvatar("");
        map.put(Constant.NEW_FRIENDS_USERNAME, newFriends);
        // 添加"群聊"
        User groupUser = new User();
        String strGroup = getResources().getString(R.string.group_chat);
        groupUser.setUsername(Constant.GROUP_USERNAME);
        groupUser.setNick(strGroup);
        groupUser.setHeader("");
        groupUser.setNick(strChat);
        groupUser.setBeizhu("");
        groupUser.setFxid("");
        groupUser.setHeader("");
        groupUser.setRegion("");
        groupUser.setSex("");
        groupUser.setTel("");
        groupUser.setSign("");
        groupUser.setAvatar("");
        map.put(Constant.GROUP_USERNAME, groupUser);

        // 存入内存
        MyApplication.getInstance().setContactList(map);
        // 存入db
        UserDao dao = new UserDao(MainActivity.this);
        List<User> users = new ArrayList<User>(map.values());
        dao.saveContactList(users);
    }
    
    private void addFriendToList(final String hxid) {
        Map<String, String> map_uf = new HashMap<String, String>();
        map_uf.put("hxid", hxid);
        LoadDataFromServer task = new LoadDataFromServer(null,
                Constant.URL_Get_UserInfo, map_uf);
        task.getData(new DataCallBack() {
            @Override
            public void onDataCallBack(JSONObject data) {
                try {

                    int code = data.getInteger("code");
                    if (code == 1) {

                        JSONObject json = data.getJSONObject("user");
                        if (json != null && json.size() != 0) {

                        }
                        String nick = json.getString("nick");
                        String avatar = json.getString("avatar");

                        String hxid = json.getString("hxid");
                        String fxid = json.getString("fxid");
                        String region = json.getString("region");
                        String sex = json.getString("sex");
                        String sign = json.getString("sign");
                        String tel = json.getString("tel");
                        User user = new User();

                        user.setUsername(hxid);
                        user.setNick(nick);
                        user.setAvatar(avatar);
                        user.setFxid(fxid);
                        user.setRegion(region);
                        user.setSex(sex);
                        user.setSign(sign);
                        user.setTel(tel);
                        setUserHeader(hxid, user);
                        Map<String, User> userlist = MyApplication.getInstance().getContactList();
                        Map<String, User> map_temp = new HashMap<String, User>();
                        map_temp.put(hxid, user);
                        userlist.putAll(map_temp);
                        // 存入内存
                        MyApplication.getInstance().setContactList(userlist);
                        // 存入db
                        UserDao dao = new UserDao(MainActivity.this);

                        dao.saveContact(user);

                        // 自己封装的javabean
                        InviteMessage msg = new InviteMessage();
                        msg.setFrom(hxid);
                        msg.setTime(System.currentTimeMillis());

                        String reason_temp = nick + "66split88" + avatar
                                + "66split88"
                                + String.valueOf(System.currentTimeMillis())
                                + "66split88" + "已经同意请求";
                        msg.setReason(reason_temp);

                        msg.setStatus(InviteMessageStatus.BEAGREED);
                        User userTemp = MyApplication.getInstance()
                                .getContactList()
                                .get(Constant.NEW_FRIENDS_USERNAME);
                        if (userTemp != null
                                && userTemp.getUnreadMsgCount() == 0) {
                            userTemp.setUnreadMsgCount(userTemp
                                    .getUnreadMsgCount() + 1);
                        }
                        notifyNewInviteMessage(msg);
                    }
                } catch (JSONException e) {
                    e.printStackTrace();
                }
            }
        });
    }
    
    /* =========== 微信tab标签上的红色标志数字的设置  ============== */
    /**
     * 刷新未读消息数
     */
    public void updateUnreadLabel() {
        int count = getUnreadMsgCountTotal();
        if (count > 0) {
            unreadLabel.setText(String.valueOf(count));
            unreadLabel.setVisibility(View.VISIBLE);
        } else {
            unreadLabel.setVisibility(View.INVISIBLE);
        }
    }

    /**
     * 获取未读消息数
     * @return
     */
    public int getUnreadMsgCountTotal() {
        int unreadMsgCountTotal = 0;
        unreadMsgCountTotal = EMChatManager.getInstance().getUnreadMsgsCount();
        return unreadMsgCountTotal;
    }
    
    
    /* =========== 通讯录tab标签上的红色标志数字的设置  ============== */
    /**
     * 刷新申请与通知消息数
     */
    public void updateUnreadAddressLable() {
        runOnUiThread(new Runnable() {
            public void run() {
                int count = getUnreadAddressCountTotal();
                if (count > 0) {
                    unreadAddressLable.setText(String.valueOf(count));
                    unreadAddressLable.setVisibility(View.VISIBLE);
                } else {
                    unreadAddressLable.setVisibility(View.INVISIBLE);
                }
            }
        });
    }
    
    /**
     * 获取未读申请与通知消息数
     * @return
     */
    public int getUnreadAddressCountTotal() {
        int unreadAddressCountTotal = 0;
        if (MyApplication.getInstance().getContactList().get(Constant.NEW_FRIENDS_USERNAME) != null){
            unreadAddressCountTotal = MyApplication.getInstance()
                    .getContactList().get(Constant.NEW_FRIENDS_USERNAME).getUnreadMsgCount();
        }
        return unreadAddressCountTotal;
    }

    
    
    /**
     * 保存提示新消息
     * @param msg
     */
    private void notifyNewInviteMessage(InviteMessage msg) {
        saveInviteMsg(msg);
        // 提示有新消息
        EMNotifier.getInstance(getApplicationContext()).notifyOnNewMsg();

        // 刷新bottom bar消息未读数
        updateUnreadAddressLable();
        // 刷新好友页面ui
        if (currentTabIndex == 1){
            contactlistfragment.refresh();
        }
    }
    
    /**
     * 保存邀请等msg
     * @param msg
     */
    private void saveInviteMsg(InviteMessage msg) {
        // 保存msg
        inviteMessgeDao.saveMessage(msg);
        // 未读数加1
        User user = MyApplication.getInstance().getContactList().get(Constant.NEW_FRIENDS_USERNAME);
        if (user.getUnreadMsgCount() == 0){
            user.setUnreadMsgCount(user.getUnreadMsgCount() + 1);
        }
    }
    
    /**
     * 设置hearder属性，方便通讯中对联系人按header分类显示，以及通过右侧ABCD...字母栏快速定位联系人
     * @param username
     * @param user
     */
    @SuppressLint("DefaultLocale")
    protected void setUserHeader(String username, User user) {
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
            user.setHeader(HanziToPinyin.getInstance()
                    .get(headerName.substring(0, 1)).get(0).target.substring(0,
                    1).toUpperCase());
            char header = user.getHeader().toLowerCase().charAt(0);
            if (header < 'a' || header > 'z') {
                user.setHeader("#");
            }
        }
    }
	
	public void onTabClicked(View view) {
        switch (view.getId()) {
        case R.id.re_weixin:
            index = 0;
            break;
        case R.id.re_contact_list:
            index = 1;
            break;
        case R.id.re_find:
            index = 2;
            break;
        case R.id.re_profile:
            index = 3;
            break;
        }

        if (currentTabIndex != index) {
            FragmentTransaction trx = getSupportFragmentManager()
                    .beginTransaction();
            trx.hide(fragments[currentTabIndex]);
            if (!fragments[index].isAdded()) {
                trx.add(R.id.fragment_container, fragments[index]);
            }
            trx.show(fragments[index]).commit();
        }
        imagebuttons[currentTabIndex].setSelected(false);
        // 把当前tab设为选中状态
        imagebuttons[index].setSelected(true);
        textviews[currentTabIndex].setTextColor(0xFF999999);
        textviews[index].setTextColor(0xFF45C01A);
        currentTabIndex = index;
	}
	
	private long exitTime = 0;
	@Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        if (keyCode == KeyEvent.KEYCODE_BACK
                && event.getAction() == KeyEvent.ACTION_DOWN) {
            if ((System.currentTimeMillis() - exitTime) > 2000) {
                Toast.makeText(getApplicationContext(), "再按一次退出程序",
                        Toast.LENGTH_SHORT).show();
                exitTime = System.currentTimeMillis();
            } else {
                moveTaskToBack(false);
                finish();

            }
            return true;
        }
        return super.onKeyDown(keyCode, event);
    }
}
