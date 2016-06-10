package com.itheima.huanxin;

import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.List;

import android.annotation.SuppressLint;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.support.v4.view.ViewPager;
import android.text.Editable;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.view.MotionEvent;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.View.OnFocusChangeListener;
import android.view.View.OnTouchListener;
import android.view.WindowManager;
import android.view.inputmethod.InputMethodManager;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.easemob.chat.EMChatManager;
import com.easemob.chat.EMConversation;
import com.easemob.chat.EMMessage;
import com.easemob.chat.TextMessageBody;
import com.itheima.huanxin.adapter.ExpressionAdapter;
import com.itheima.huanxin.adapter.ExpressionPagerAdapter;
import com.itheima.huanxin.adapter.MessageAdapter;
import com.itheima.huanxin.other.LocalUserInfo;
import com.itheima.huanxin.view.ExpandGridView;
import com.itheima.huanxin.view.PasteEditText;
import com.itheima.util.SmileUtils;

/**
 * 聊天模块
 * @author zhangming
 * @date 2016/06/10
 */
public class ChatActivity extends BaseActivity implements OnClickListener{
	private PasteEditText mEditTextContent;
	private RelativeLayout edittext_layout;
	private Button btnMore;
	private View buttonSend;
	private InputMethodManager manager;
	
	private EMConversation conversation;
	private String toChatUsername; // 给谁发送消息
    private String myUserNick = "";
    private String myUserAvatar = "";
	private String toUserNick="";
    private String toUserAvatar="";
    private List<String> reslist;
    
    private ListView listView;
    private MessageAdapter mAdapter;
    private View buttonSetModeKeyboard;
    private ViewPager expressionViewpager;
    private View buttonSetModeVoice; 
    private View buttonPressToSpeak; //选择模式按钮(语音or文本)
    private NewMessageBroadcastReceiver newMsgReceiver;
    
    private ImageView iv_emoticons_normal;
    private ImageView iv_emoticons_checked;
    private LinearLayout emojiIconContainer;
    private LinearLayout btnContainer;
    private View more;
    
    private ImageView iv_setting; 
    private ImageView iv_setting_group; //设置按钮
    
    //反射的表情工具类
    private static final String flectExpressionClass = "com.itheima.util.SmileUtils";
    
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_chat);
		
        // 读取本地自己的头像和昵称
        myUserNick = LocalUserInfo.getInstance(ChatActivity.this).getUserInfo("nick");
        myUserAvatar = LocalUserInfo.getInstance(ChatActivity.this).getUserInfo("avatar");
        
		initView();
		setUpView();
	}

	private void initView() {
		mEditTextContent = (PasteEditText) findViewById(R.id.et_sendmessage);
		edittext_layout = (RelativeLayout) findViewById(R.id.edittext_layout);
		btnMore = (Button) findViewById(R.id.btn_more);
		buttonSend = findViewById(R.id.btn_send);
		listView = (ListView) findViewById(R.id.list);
		expressionViewpager = (ViewPager) findViewById(R.id.vPager);
		
		buttonSetModeKeyboard = findViewById(R.id.btn_set_mode_keyboard);
		emojiIconContainer = (LinearLayout) findViewById(R.id.ll_face_container);
	    btnContainer = (LinearLayout) findViewById(R.id.ll_btn_container);
	    iv_emoticons_normal = (ImageView) findViewById(R.id.iv_emoticons_normal);
	    iv_emoticons_checked = (ImageView) findViewById(R.id.iv_emoticons_checked);
	    more = findViewById(R.id.more);
	    
	    buttonSetModeVoice = findViewById(R.id.btn_set_mode_voice);
	    buttonPressToSpeak = findViewById(R.id.btn_press_to_speak);
	    
	    // 表情list
        reslist = getExpressionRes(35);
        // 初始化表情viewpager
        List<View> views = new ArrayList<View>();
        View gv1 = getGridChildView(1);
        View gv2 = getGridChildView(2);
        views.add(gv1);
        views.add(gv2);
        expressionViewpager.setAdapter(new ExpressionPagerAdapter(views));
        edittext_layout.requestFocus();
	    
	    iv_emoticons_normal.setVisibility(View.VISIBLE);
        iv_emoticons_checked.setVisibility(View.INVISIBLE);
        edittext_layout.setBackgroundResource(R.drawable.input_bar_bg_normal);
		
		// 监听文字框焦点改变
		mEditTextContent.setOnFocusChangeListener(new OnFocusChangeListener() {
			@Override
			public void onFocusChange(View v, boolean hasFocus) {
				if (hasFocus) {
                    edittext_layout.setBackgroundResource(R.drawable.input_bar_bg_active);
                } else {
                    edittext_layout.setBackgroundResource(R.drawable.input_bar_bg_normal);
                }
			}
		});
		
		// 监听文字框内容改变
		mEditTextContent.addTextChangedListener(new TextWatcher() {
			@Override
			public void onTextChanged(CharSequence s, int start, int before, int count) {
				 if (!TextUtils.isEmpty(s)) {
	                 btnMore.setVisibility(View.GONE);
	                 buttonSend.setVisibility(View.VISIBLE);
	             }else {
	                 btnMore.setVisibility(View.VISIBLE);
	                 buttonSend.setVisibility(View.GONE);
	             }
			}
			
			@Override
			public void beforeTextChanged(CharSequence s, int start, int count,int after) {
				
			}
			
			@Override
			public void afterTextChanged(Editable s) {
				
			}
		});
		
		// 监听文字框点击事件
	    mEditTextContent.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                edittext_layout.setBackgroundResource(R.drawable.input_bar_bg_active);
                more.setVisibility(View.GONE);
                iv_emoticons_normal.setVisibility(View.VISIBLE);
                iv_emoticons_checked.setVisibility(View.INVISIBLE);
                emojiIconContainer.setVisibility(View.GONE);
                btnContainer.setVisibility(View.GONE);
            }
        });
		
		listView.setOnTouchListener(new OnTouchListener() {
            @SuppressLint("ClickableViewAccessibility")
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                hideKeyboard(); //隐藏软键盘
                more.setVisibility(View.GONE);
                iv_emoticons_normal.setVisibility(View.VISIBLE);
                iv_emoticons_checked.setVisibility(View.INVISIBLE);
                emojiIconContainer.setVisibility(View.GONE);
                btnContainer.setVisibility(View.GONE);
                return false;
            }
	    });
		 
		buttonPressToSpeak.setOnTouchListener(new PressToSpeakListener());
	}
	
	/**
     * 按住说话listener
     */
	private class PressToSpeakListener implements View.OnTouchListener {
		@Override
		public boolean onTouch(View v, MotionEvent event) {
			//TODO do something...
			return false;
		}
	}
	
	private void setUpView(){
         iv_emoticons_normal.setOnClickListener(this);
         iv_emoticons_checked.setOnClickListener(this);
         manager = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
         getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_ALWAYS_HIDDEN);
         
		 //默认单聊
		 toChatUsername = getIntent().getStringExtra("userId");
         String toChatUserNick = getIntent().getStringExtra("userNick");
         ((TextView) findViewById(R.id.name)).setText(toChatUserNick);
         toUserNick=getIntent().getStringExtra("userNick");
         toUserAvatar=getIntent().getStringExtra("userAvatar");
         
         // 把此会话的未读数置为0
         conversation = EMChatManager.getInstance().getConversation(toChatUsername);
         conversation.resetUnreadMsgCount(); 
         
         //显示消息
         mAdapter = new MessageAdapter(this, toChatUsername);
         listView.setAdapter(mAdapter);
         
         // 注册接收消息广播
         newMsgReceiver = new NewMessageBroadcastReceiver();
         IntentFilter intentFilter = new IntentFilter(EMChatManager
                 .getInstance().getNewMessageBroadcastAction());
         // 设置广播的优先级别大于Mainacitivity,这样如果消息来的时候正好在chat页面，直接显示消息，而不是提示消息未读
         intentFilter.setPriority(5);
         registerReceiver(newMsgReceiver, intentFilter);

         // 注册一个ack回执消息的BroadcastReceiver
         IntentFilter ackMessageIntentFilter = new IntentFilter(EMChatManager
                 .getInstance().getAckMessageBroadcastAction());
         ackMessageIntentFilter.setPriority(5);
         registerReceiver(ackMessageReceiver, ackMessageIntentFilter);

         // 注册一个消息送达的BroadcastReceiver
         IntentFilter deliveryAckMessageIntentFilter = new IntentFilter(
                 EMChatManager.getInstance().getDeliveryAckMessageBroadcastAction());
         deliveryAckMessageIntentFilter.setPriority(5);
         registerReceiver(deliveryAckMessageReceiver,deliveryAckMessageIntentFilter);
         
         iv_setting = (ImageView) this.findViewById(R.id.iv_setting);  //单聊设置
 	     iv_setting_group = (ImageView) this.findViewById(R.id.iv_setting_group);  //群聊设置
 	     //默认单聊设置
         iv_setting.setVisibility(View.VISIBLE);
         iv_setting.setOnClickListener(new OnClickListener() {
             @Override
             public void onClick(View v) {
                 startActivity(new Intent(ChatActivity.this,ChatSingleSettingActivity.class).putExtra("userId",toChatUsername));
             }
         });
	}
	
	/**
     * 获取表情的gridview的子view
     * @param page 第几页
     * @return
     */
    private View getGridChildView(int page) {
        View view = View.inflate(this, R.layout.expression_gridview, null);
        ExpandGridView gv = (ExpandGridView) view.findViewById(R.id.gridview);
        List<String> list = new ArrayList<String>();
        if (page == 1) {
            List<String> list1 = reslist.subList(0, 20);
            list.addAll(list1);
        } else if (page == 2) {
            list.addAll(reslist.subList(20, reslist.size()));
        }
        list.add("delete_expression");
        final ExpressionAdapter expressionAdapter = new ExpressionAdapter(this,1, list);
        gv.setAdapter(expressionAdapter);
        gv.setOnItemClickListener(new OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                String filename = expressionAdapter.getItem(position);
                try {
                    // 文字输入框可见时，才可输入表情
                    // 按住说话可见，不让输入表情
                    if (buttonSetModeKeyboard.getVisibility() != View.VISIBLE) {
                        if (filename != "delete_expression") { // 不是删除键，显示表情
                            // 这里用的反射，所以混淆的时候不要混淆SmileUtils这个类
                            @SuppressWarnings("rawtypes")
                            Class clz = Class.forName(flectExpressionClass);
                            Field field = clz.getField(filename);
                            mEditTextContent.append(SmileUtils.getSmiledText(
                                    ChatActivity.this, (String) field.get(null)));
                        } else { // 删除文字或者表情
                            if (!TextUtils.isEmpty(mEditTextContent.getText())) {
                                int selectionStart = mEditTextContent.getSelectionStart();// 获取光标的位置
                                if (selectionStart > 0) {
                                    String body = mEditTextContent.getText().toString();
                                    String tempStr = body.substring(0,selectionStart);
                                    int i = tempStr.lastIndexOf("[");// 获取最后一个表情的位置
                                    if (i != -1) {
                                        CharSequence cs = tempStr.substring(i,selectionStart);
                                        if (SmileUtils.containsKey(cs.toString())){
                                            mEditTextContent.getEditableText().delete(i, selectionStart);
                                        }else{
                                            mEditTextContent.getEditableText().delete(selectionStart - 1,selectionStart);
                                        }
                                    } else {
                                        mEditTextContent.getEditableText().delete(selectionStart - 1,selectionStart);
                                    }
                                }
                            }
                        }
                    }
                } catch (Exception e) {
                	e.printStackTrace();
                }
            }
        });
        return view;
    }

	public List<String> getExpressionRes(int getSum) {
        List<String> reslist = new ArrayList<String>();
        for (int x = 1; x <= getSum; x++) {
            String filename = "ee_" + x;
            reslist.add(filename);
        }
        return reslist;
    }

	@Override
	public void onClick(View v) {
		int id = v.getId();
		if(id == R.id.btn_send){
			String s = mEditTextContent.getText().toString();
			sendText(s);
		}else if (id == R.id.iv_emoticons_normal) { // 点击显示表情框
            more.setVisibility(View.VISIBLE);
            iv_emoticons_normal.setVisibility(View.INVISIBLE);
            iv_emoticons_checked.setVisibility(View.VISIBLE);
            btnContainer.setVisibility(View.GONE);
            emojiIconContainer.setVisibility(View.VISIBLE);
            hideKeyboard();
        } else if (id == R.id.iv_emoticons_checked) { // 点击隐藏表情框
            iv_emoticons_normal.setVisibility(View.VISIBLE);
            iv_emoticons_checked.setVisibility(View.INVISIBLE);
            btnContainer.setVisibility(View.VISIBLE);
            emojiIconContainer.setVisibility(View.GONE);
            more.setVisibility(View.GONE);
        }
	}
	
	/**
     * 显示语音图标按钮
     * @param view
     */
    public void setModeVoice(View view) {
        hideKeyboard();
        edittext_layout.setVisibility(View.GONE);
        more.setVisibility(View.GONE);
        view.setVisibility(View.GONE);
        buttonSetModeKeyboard.setVisibility(View.VISIBLE);
        buttonSend.setVisibility(View.GONE);
        btnMore.setVisibility(View.VISIBLE);
        buttonPressToSpeak.setVisibility(View.VISIBLE);
        iv_emoticons_normal.setVisibility(View.VISIBLE);
        iv_emoticons_checked.setVisibility(View.INVISIBLE);
        btnContainer.setVisibility(View.VISIBLE);
        emojiIconContainer.setVisibility(View.GONE);
    }

	
	/**
     * 显示键盘图标
     * @param view
     */
    public void setModeKeyboard(View view) {
        edittext_layout.setVisibility(View.VISIBLE);
        more.setVisibility(View.GONE);
        view.setVisibility(View.GONE);
        buttonSetModeVoice.setVisibility(View.VISIBLE);
        mEditTextContent.requestFocus();
        buttonPressToSpeak.setVisibility(View.GONE);
        if (TextUtils.isEmpty(mEditTextContent.getText())) {
            btnMore.setVisibility(View.VISIBLE);
            buttonSend.setVisibility(View.GONE);
        } else {
            btnMore.setVisibility(View.GONE);
            buttonSend.setVisibility(View.VISIBLE);
        }
    }
	
	/**
	 * 发送文本消息
	 * @param content
	 */
	private void sendText(String content) {
		EMMessage message = EMMessage.createSendMessage(EMMessage.Type.TXT);
		
		//默认是单聊
		TextMessageBody txtBody = new TextMessageBody(content);
		message.addBody(txtBody); // 设置消息body
		// 设置要发给谁,用户username或者群聊groupid
		message.setReceipt(toChatUsername);
		message.setAttribute("useravatar", myUserAvatar);
		message.setAttribute("usernick", myUserNick);
		conversation.addMessage(message); // 把messgage加到conversation中
		
		// 通知adapter有消息变动,adapter会根据加入的这条message显示消息和调用sdk的发送方法
		mAdapter.refresh();
		listView.setSelection(listView.getCount()-1);
		mEditTextContent.setText("");
		setResult(RESULT_OK);
	}

	/**
     * 消息广播接收者
     */
    private class NewMessageBroadcastReceiver extends BroadcastReceiver {
        @Override
        public void onReceive(Context context, Intent intent) {
            abortBroadcast();   //把广播给终结掉
            String username = intent.getStringExtra("from");
            String msgid = intent.getStringExtra("msgid");
            // 收到这个广播的时候，message已经在db和内存里了，可以通过id获取mesage对象
            EMMessage message = EMChatManager.getInstance().getMessage(msgid);
            if (!username.equals(toChatUsername)) {
                // 消息不是发给当前会话，return
                // notifyNewMessage(message);
                return;
            }
            mAdapter.refresh();   // 通知adapter有新消息，更新ui
            listView.setSelection(listView.getCount() - 1);
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
                // 把message设为已读
                EMMessage msg = conversation.getMessage(msgid);
                if (msg != null) {
                    msg.isAcked = true;
                }
            }
            mAdapter.notifyDataSetChanged();
        }
    };
    

    /**
     * 消息送达BroadcastReceiver
     */
    private BroadcastReceiver deliveryAckMessageReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            abortBroadcast();
            String msgid = intent.getStringExtra("msgid");
            String from = intent.getStringExtra("from");
            EMConversation conversation = EMChatManager.getInstance().getConversation(from);
            if (conversation != null) {
                // 把message设为已读
                EMMessage msg = conversation.getMessage(msgid);
                if (msg != null) {
                    msg.isDelivered = true;
                }
            }
            mAdapter.notifyDataSetChanged();
        }
    };
	
	
	/**
	 * 隐藏软键盘
	 */
	private void hideKeyboard(){
		InputMethodManager manager = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
		if (getWindow().getAttributes().softInputMode != WindowManager.LayoutParams.SOFT_INPUT_STATE_HIDDEN) {
            if (getCurrentFocus() != null){
                manager.hideSoftInputFromWindow(getCurrentFocus().getWindowToken(), InputMethodManager.HIDE_NOT_ALWAYS);
            }
		}
	}
	
	@Override
	protected void onDestroy() {
		super.onDestroy();
		if(newMsgReceiver!=null){
			unregisterReceiver(newMsgReceiver);
		}
		if(ackMessageReceiver!=null){
			unregisterReceiver(ackMessageReceiver);
		}
		if(deliveryAckMessageReceiver!=null){
			unregisterReceiver(deliveryAckMessageReceiver);
		}
	}
}
