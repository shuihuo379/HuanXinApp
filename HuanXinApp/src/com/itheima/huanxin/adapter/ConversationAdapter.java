package com.itheima.huanxin.adapter;

import java.io.File;
import java.util.Date;
import java.util.List;

import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.TextView.BufferType;
import android.widget.Toast;

import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.easemob.chat.EMConversation;
import com.easemob.chat.EMGroup;
import com.easemob.chat.EMGroupManager;
import com.easemob.chat.EMMessage;
import com.easemob.chat.TextMessageBody;
import com.easemob.exceptions.EaseMobException;
import com.easemob.util.DateUtils;
import com.itheima.app.Constant;
import com.itheima.app.MyApplication;
import com.itheima.huanxin.ChatActivity;
import com.itheima.huanxin.R;
import com.itheima.huanxin.domain.User;
import com.itheima.huanxin.other.LoadUserAvatar;
import com.itheima.huanxin.other.LoadUserAvatar.ImageDownloadedCallBack;
import com.itheima.util.SDCardUtil;
import com.itheima.util.SmileUtils;

public class ConversationAdapter extends BaseAdapter{
	private List<EMConversation> mlist;
	private Context context;
	private LayoutInflater inflater;
	private LoadUserAvatar avatarLoader;
	
	public ConversationAdapter(Context context,List<EMConversation> mlist){
		this.context = context;
		this.inflater = LayoutInflater.from(context);
		this.mlist = mlist;
		
		String MyAppDir = null;
		if(SDCardUtil.ExistSDCard()){
			 String sdRootDir = SDCardUtil.getNormalSDPath();
			 MyAppDir= sdRootDir+ File.separator +"HuanXinApp";
		}else{
			 String memRootDir = SDCardUtil.getPhoneCardPath();
			 MyAppDir = memRootDir+ File.separator +"HuanXinApp";
		}
        this.avatarLoader = new LoadUserAvatar(context,MyAppDir);
	}
	
	@Override
	public int getCount() {
		return mlist.size();
	}

	@Override
	public EMConversation getItem(int position) {
		return mlist.get(position);
	}

	@Override
	public long getItemId(int position) {
		return position;
	}

	@Override
	public View getView(int position, View convertView, ViewGroup parent) {
		ViewHolder holder = new ViewHolder();
        // 获取与此用户/群组的会话
        final EMConversation conversation = getItem(position);
        // 获取用户username或者群组groupid
        final String username = conversation.getUserName();
        List<EMGroup> groups = EMGroupManager.getInstance().getAllGroups();
        
        boolean isGroup = false;
        String nick = "";
        String groupName = "";
        String[] avatars = new String[5];
        int membersNum = 0;
        
        for (EMGroup group : groups) {
            if (group.getGroupId().equals(username)) {
                isGroup = true;
                
                String groupName_temp = group.getGroupName();
                JSONObject jsonObject = JSONObject.parseObject(groupName_temp);
                JSONArray jsonarray = jsonObject.getJSONArray("jsonArray");
                groupName = jsonObject.getString("groupname");

                String groupName_temp2 = "";
                membersNum = jsonarray.size();

                for (int i = 0; i < membersNum; i++) {
                    JSONObject json = (JSONObject) jsonarray.get(i);
                    if (i < 5) {
                        avatars[i] = json.getString("avatar");
                        Log.e("avatars[i]----->>>", avatars[i]);
                    }
                    if (i == 0) {
                        groupName_temp2 = json.getString("nick");
                    } else if (i < 4) {
                        groupName_temp2 += "、" + json.getString("nick");

                    } else if (i == 4) {
                        groupName_temp2 += "。。。";
                    }
                }

                if (groupName.equals("未命名")) {
                    groupName = groupName_temp2;
                }
                break;
            }
        }
        
        convertView = creatConvertView(membersNum);
        holder.tv_name = (TextView) convertView.findViewById(R.id.tv_name);  // 昵称
        holder.tv_unread = (TextView) convertView.findViewById(R.id.tv_unread); // 未读消息
        holder.tv_content = (TextView) convertView.findViewById(R.id.tv_content);  // 最近一条消息
        holder.tv_time = (TextView) convertView.findViewById(R.id.tv_time);  // 时间
        holder.msgState = (ImageView) convertView.findViewById(R.id.msg_state);  // 发送状态
        
        // 单聊数据加载
        if (!isGroup) {
            holder.iv_avatar = (ImageView) convertView.findViewById(R.id.iv_avatar);
            // 从好友列表中加载该用户的资料
            User user = MyApplication.getInstance().getContactList().get(username);
            if (user != null) {
                nick = user.getNick();
                String avatar = user.getAvatar();
                holder.tv_name.setText(nick); // 显示昵称
                showUserAvatar(holder.iv_avatar, avatar); // 显示头像
            }else{
                EMMessage message=conversation.getLastMessage();
                if(message.direct==EMMessage.Direct.RECEIVE){
                    try {
                        nick=message.getStringAttribute("myUserNick");
                        String avatar=message.getStringAttribute("myUserAvatar");
                        holder.tv_name.setText(nick); // 显示昵称
                        showUserAvatar(holder.iv_avatar, avatar); // 显示头像
                    } catch (EaseMobException e) {
                        e.printStackTrace();
                    }
                }else{
                    try {
                        nick=message.getStringAttribute("toUserNick");
                        String avatar=message.getStringAttribute("toUserAvatar");
                        holder.tv_name.setText(nick);  // 显示昵称
                        showUserAvatar(holder.iv_avatar, avatar); // 显示头像
                    } catch (EaseMobException e) {
                        e.printStackTrace();
                    }
                }
            }
        }else{
        	// 群聊对话
        }
        
        if (conversation.getUnreadMsgCount() > 0) {
            // 显示与此用户的消息未读数
            holder.tv_unread.setText(String.valueOf(conversation.getUnreadMsgCount()));
            holder.tv_unread.setVisibility(View.VISIBLE);
        } else {
            holder.tv_unread.setVisibility(View.INVISIBLE);
        }
        
        if (conversation.getMsgCount() != 0) {
            // 把最后一条消息的内容作为item的message内容
            EMMessage lastMessage = conversation.getLastMessage();
            holder.tv_content.setText(SmileUtils.getSmiledText(context,
            		getMessageDigest(lastMessage, context)),BufferType.SPANNABLE);
            holder.tv_time.setText(DateUtils.getTimestampString(new Date(lastMessage.getMsgTime())));
            if (lastMessage.direct == EMMessage.Direct.SEND && lastMessage.status == EMMessage.Status.FAIL) {
                holder.msgState.setVisibility(View.VISIBLE);
            } else {
                holder.msgState.setVisibility(View.GONE);
            }
        }
        
        final String groupName_temp = groupName;
        final boolean isGroup_temp = isGroup;
        final String nick_temp = nick;
        RelativeLayout re_parent = (RelativeLayout) convertView.findViewById(R.id.re_parent);

        re_parent.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                if (username.equals(MyApplication.getInstance().getUserName())){
                    Toast.makeText(context, "不能和自己聊天...", Toast.LENGTH_SHORT).show();
                }else {
                    // 进入聊天页面
                    Intent intent = new Intent(context, ChatActivity.class);
                    if (isGroup_temp) {
                        // 进入群聊模式
                    } else {
                        // 进入单聊模式
                        intent.putExtra("userId", username);
                        intent.putExtra("userNick", nick_temp);
                    }
                    context.startActivity(intent);
                }
            }
        });
        
        return convertView;
	}
	
	/**
     * 根据消息内容和消息类型获取消息内容提示
     * @param message
     * @param context
     */
    private String getMessageDigest(EMMessage message, Context context) {
    	String digest = "";
        switch (message.getType()) {
        	case TXT: // 文本消息
        		if (!message.getBooleanAttribute(Constant.MESSAGE_ATTR_IS_VOICE_CALL, false)) { //纯文本信息
                    TextMessageBody txtBody = (TextMessageBody) message.getBody();
                    digest = txtBody.getMessage();
                }else { //语音信息
                    TextMessageBody txtBody = (TextMessageBody) message.getBody();
                    digest = context.getString(R.string.voice_call) + txtBody.getMessage();
                }
        		break;
        	 default:
                 System.err.println("error, unknow type");
                 return "";
        }
        return digest;
    }
	
	/**
	 * 显示用户头像
	 * @param imageView
	 * @param avatar
	 */
	private void showUserAvatar(ImageView imageView, String avatar) {
        final String url_avatar = Constant.URL_Avatar + avatar;
        imageView.setTag(url_avatar);
        if (url_avatar != null && !url_avatar.equals("")) {
            Bitmap bitmap = avatarLoader.loadImage(imageView, url_avatar,
                    new ImageDownloadedCallBack() {
                        public void onImageDownloaded(ImageView imageView,Bitmap bitmap) {
                            if (imageView.getTag() == url_avatar) {
                                imageView.setImageBitmap(bitmap);
                            }
                        }
                    });
            if (bitmap != null){
                imageView.setImageBitmap(bitmap);
            }
        }
    }
	
	private View creatConvertView(int size) {
        View convertView = null;
        if (size == 0) {
            convertView = inflater.inflate(R.layout.item_conversation_single,null, false);
        }
        return convertView;
	}

	
	private static class ViewHolder {
        /** 和谁的聊天记录 */
        TextView tv_name;
        /** 消息未读数 */
        TextView tv_unread;
        /** 最后一条消息的内容 */
        TextView tv_content;
        /** 最后一条消息的时间 */
        TextView tv_time;
        /** 用户头像 */
        ImageView iv_avatar;
        ImageView iv_avatar1;
        ImageView iv_avatar2;
        ImageView iv_avatar3;
        ImageView iv_avatar4;
        ImageView iv_avatar5;
        ImageView msgState;
    }
}
