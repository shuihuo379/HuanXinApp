package com.itheima.huanxin.adapter;

import java.io.File;
import java.util.Date;

import android.app.Activity;
import android.content.Context;
import android.graphics.Bitmap;
import android.text.Spannable;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.View.OnLongClickListener;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.TextView.BufferType;
import android.widget.Toast;

import com.easemob.EMCallBack;
import com.easemob.chat.EMChatManager;
import com.easemob.chat.EMConversation;
import com.easemob.chat.EMMessage;
import com.easemob.chat.EMMessage.Direct;
import com.easemob.chat.EMMessage.Type;
import com.easemob.chat.TextMessageBody;
import com.easemob.exceptions.EaseMobException;
import com.easemob.util.DateUtils;
import com.itheima.app.Constant;
import com.itheima.huanxin.R;
import com.itheima.huanxin.other.LoadUserAvatar;
import com.itheima.huanxin.other.LoadUserAvatar.ImageDownloadedCallBack;
import com.itheima.huanxin.other.LocalUserInfo;
import com.itheima.util.SDCardUtil;
import com.itheima.util.SmileUtils;

public class MessageAdapter extends BaseAdapter{
	private String username;
	private Context context;
    private EMConversation conversation;
    private LoadUserAvatar avatarLoader;
    private LayoutInflater inflater;
	
	public MessageAdapter(Context context, String username) {
        this.username = username;
        this.context = context;
        this.conversation = EMChatManager.getInstance().getConversation(username);
        this.inflater = LayoutInflater.from(context);
        
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
		return conversation.getMsgCount();
	}

	@Override
	public EMMessage getItem(int position) {
		return conversation.getMessage(position);
	}

	@Override
	public long getItemId(int position) {
		return position;
	}

	@Override
	public View getView(int position, View convertView, ViewGroup parent) {
		//TODO 
		final EMMessage message = getItem(position);
        String fromusernick = "0000";
        String fromuseravatar = "0000";
        EMMessage.Direct msg_dirct = message.direct;
        try {
            fromusernick = message.getStringAttribute("usernick");
            fromuseravatar = message.getStringAttribute("useravatar");
        } catch (EaseMobException e) {
            e.printStackTrace();
        }
        
        if (message.getFrom().equals("admin")) {
            //TODO dosomething
        } else {
    	    final ViewHolder holder;
            if (convertView == null) {
               holder = new ViewHolder();
               convertView = createViewByMessage(message, position);
               
               if (message.getType() == EMMessage.Type.TXT) { //文本内容
            	   try {
            		   holder.pb = (ProgressBar) convertView.findViewById(R.id.pb_sending);
                       holder.staus_iv = (ImageView) convertView.findViewById(R.id.msg_status);
                       holder.head_iv = (ImageView) convertView.findViewById(R.id.iv_userhead);
                       // 这里是文字内容
                       holder.tv = (TextView) convertView.findViewById(R.id.tv_chatcontent);
                       holder.tv_userId = (TextView) convertView.findViewById(R.id.tv_userid);
                   } catch (Exception e) {
                	   e.printStackTrace();
                   }
            	   
                   // TODO 语音通话
            	   if(message.getBooleanAttribute(Constant.MESSAGE_ATTR_IS_VOICE_CALL, false)){
            	    	 
            	   }
               }
               convertView.setTag(holder);
            }else{
               holder = (ViewHolder) convertView.getTag();
            }
            
            // 如果是发送的消息,显示已读textview
            if (message.direct == EMMessage.Direct.SEND) { //发送
            	 holder.tv_ack = (TextView) convertView.findViewById(R.id.tv_ack);
                 holder.tv_delivered = (TextView) convertView.findViewById(R.id.tv_delivered);
                 if (holder.tv_ack != null) {
                     if (message.isAcked) {
                         if (holder.tv_delivered != null) {
                             holder.tv_delivered.setVisibility(View.INVISIBLE);
                         }
                         holder.tv_ack.setVisibility(View.VISIBLE);
                     } else {
                         holder.tv_ack.setVisibility(View.INVISIBLE);
                         // check and display msg delivered ack status
                         if (holder.tv_delivered != null) {
                             if (message.isDelivered) {
                                 holder.tv_delivered.setVisibility(View.VISIBLE);
                             } else {
                                 holder.tv_delivered.setVisibility(View.INVISIBLE);
                             }
                         }
                     }
                 }
            }else{
            	// 如果是文本或者地图消息,显示的时候给对方发送已读回执
                if ((message.getType() == Type.TXT || message.getType() == Type.LOCATION) && !message.isAcked) {
                    // 不是语音通话记录
                    if (!message.getBooleanAttribute(Constant.MESSAGE_ATTR_IS_VOICE_CALL, false)) {
                        try {
                            EMChatManager.getInstance().ackMessageRead(message.getFrom(), message.getMsgId());
                            // 发送已读回执
                            message.isAcked = true;
                        } catch (Exception e) {
                            e.printStackTrace();
                        }
                    }
                }
            }
            
            switch (message.getType()) {
            case TXT: // 文本
            	if (!message.getBooleanAttribute(Constant.MESSAGE_ATTR_IS_VOICE_CALL, false)){
            		handleTextMessage(message, holder, position);
            	}else{
            		// 语音电话
            	}
                break;
            default:
                break;
            }
            
            TextView timestamp = (TextView) convertView.findViewById(R.id.timestamp);
            if (position == 0) {
                timestamp.setText(DateUtils.getTimestampString(new Date(message.getMsgTime())));
                timestamp.setVisibility(View.VISIBLE);
            } else {
                // 两条消息时间离得如果稍长，显示时间
                if (DateUtils.isCloseEnough(message.getMsgTime(), conversation.getMessage(position - 1).getMsgTime())) {
                    timestamp.setVisibility(View.GONE);
                } else {
                    timestamp.setText(DateUtils.getTimestampString(new Date(message.getMsgTime())));
                    timestamp.setVisibility(View.VISIBLE);
                }
            }

            if (msg_dirct == Direct.RECEIVE) { //接收
                // 对方的头像值： fromuseravatar
                final String avater = Constant.URL_Avatar + fromuseravatar;
                holder.head_iv.setTag(avater);
                if (avater != null && !avater.equals("")) {
                    Bitmap bitmap = avatarLoader.loadImage(holder.head_iv,avater, new ImageDownloadedCallBack() {
                        @Override
                        public void onImageDownloaded(ImageView imageView, Bitmap bitmap) {
                            if (imageView.getTag() == avater) {
                                imageView.setImageBitmap(bitmap);
                            }
                        }
                    });
                    if (bitmap != null) {
                        holder.head_iv.setImageBitmap(bitmap);
                    }
                }
            } else {
                // 设置自己本地的头像
                final String avater = Constant.URL_Avatar + LocalUserInfo.getInstance(context).getUserInfo("avatar");
                holder.head_iv.setTag(avater);
                if (avater != null && !avater.equals("")) {
                    Bitmap bitmap = avatarLoader.loadImage(holder.head_iv,
                            avater,new ImageDownloadedCallBack() {
                                @Override
                                public void onImageDownloaded(ImageView imageView, Bitmap bitmap) {
                                    if (imageView.getTag() == avater) {
                                        imageView.setImageBitmap(bitmap);
                                    }
                                }
                            });
                    if (bitmap != null){
                        holder.head_iv.setImageBitmap(bitmap);
                    }
                }
            }
        }
        return convertView;
	}
	
    private void handleTextMessage(EMMessage message, ViewHolder holder,int position) {
    	 TextMessageBody txtBody = (TextMessageBody) message.getBody();
         Spannable span = SmileUtils.getSmiledText(context, txtBody.getMessage());
         // 设置内容
         holder.tv.setText(span, BufferType.SPANNABLE);
         // 设置长按事件监听
         holder.tv.setOnLongClickListener(new OnLongClickListener() {
             @Override
             public boolean onLongClick(View v) {
            	 //TODO
            	 return false;
             }
         });

         if (message.direct == EMMessage.Direct.SEND) {
             switch (message.status) {
             case SUCCESS: // 发送成功
            	 if(holder.pb!=null){
            		 holder.pb.setVisibility(View.GONE);
            	 }
            	 if(holder.staus_iv!=null){
            		 holder.staus_iv.setVisibility(View.GONE);
            	 }
                 
                 break;
             case FAIL: // 发送失败
            	 if(holder.pb!=null){
            		 holder.pb.setVisibility(View.GONE);
            	 }
            	 if(holder.staus_iv!=null){
            		 holder.staus_iv.setVisibility(View.VISIBLE);
            	 }
                 break;
             case INPROGRESS: // 发送中
            	 if(holder.pb!=null){
            		 holder.pb.setVisibility(View.VISIBLE);
            	 }
            	 if(holder.staus_iv!=null){
            		 holder.staus_iv.setVisibility(View.GONE);
            	 }
                 break;
             default:
                 // 发送消息
                 sendMsgInBackground(message, holder);
             }
         }
	}
    
    /**
     * 发送消息
     * @param message
     * @param holder
     * @param position
     */
    public void sendMsgInBackground(final EMMessage message,final ViewHolder holder) {
    	if(holder.staus_iv!=null){
    		 holder.staus_iv.setVisibility(View.GONE);
    	}
    	if(holder.pb!=null){
    		 holder.pb.setVisibility(View.VISIBLE);
    	}

        final long start = System.currentTimeMillis();
        // 调用sdk发送异步发送方法
        EMChatManager.getInstance().sendMessage(message, new EMCallBack() {
            @Override
            public void onSuccess() {
                // umeng自定义事件，
                updateSendedView(message, holder);
            }

            @Override
            public void onError(int code, String error) {
                updateSendedView(message, holder);
            }

            @Override
            public void onProgress(int progress, String status) {
            	
            }
        });
    }
    
    
    /**
     * 更新ui上消息发送状态
     * @param message
     * @param holder
     */
    private void updateSendedView(final EMMessage message,
            final ViewHolder holder) {
        ((Activity)context).runOnUiThread(new Runnable() {
            @Override
            public void run() {
                // send success
                if (message.getType() == EMMessage.Type.VIDEO) {
                    holder.tv.setVisibility(View.GONE);
                }
                if (message.status == EMMessage.Status.SUCCESS) {
                	Log.i("test","send message success...");
                } else if (message.status == EMMessage.Status.FAIL) {
                    Toast.makeText(context,context.getString(R.string.send_fail)
                                    + context.getString(R.string.connect_failuer_toast),0).show();
                }
                notifyDataSetChanged();
            }
        });
    }
    

	private View createViewByMessage(EMMessage message, int position) {
    	//TODO LOCATION IMAGE VOICE VIDEO FILE
    	switch (message.getType()) {
    	 	default:
            return message.direct == EMMessage.Direct.RECEIVE ? 
            		inflater.inflate(R.layout.row_received_message, null) : 
            		inflater.inflate(R.layout.row_sent_message, null);
    	}
    }
	
	public static class ViewHolder {
        ImageView iv;
        TextView tv;
        ProgressBar pb;
        ImageView staus_iv;
        ImageView head_iv;
        TextView tv_userId;
        ImageView playBtn;
        TextView timeLength;
        TextView size;
        LinearLayout container_status_btn;
        LinearLayout ll_container;
        ImageView iv_read_status;
        // 显示已读回执状态
        TextView tv_ack;
        // 显示送达回执状态
        TextView tv_delivered;

        TextView tv_file_name;
        TextView tv_file_size;
        TextView tv_file_download_state;
	}

	
    /**
     * 刷新页面
     */
    public void refresh() {
        notifyDataSetChanged();
    }
}
