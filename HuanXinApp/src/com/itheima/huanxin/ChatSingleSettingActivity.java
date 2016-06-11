package com.itheima.huanxin;

import java.io.File;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import android.app.ProgressDialog;
import android.content.Intent;
import android.graphics.Bitmap;
import android.os.Bundle;
import android.os.Handler;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.easemob.chat.EMChatManager;
import com.easemob.chat.EMContactManager;
import com.easemob.exceptions.EaseMobException;
import com.itheima.app.MyApplication;
import com.itheima.huanxin.domain.TopUser;
import com.itheima.huanxin.domain.User;
import com.itheima.huanxin.other.LoadUserAvatar;
import com.itheima.huanxin.other.LoadUserAvatar.ImageDownloadedCallBack;
import com.itheima.util.SDCardUtil;
import com.itheima.util.T;

/**
 * 单聊设置界面及其实现
 * @author zhangming
 * @date 2016/06/10
 */
public class ChatSingleSettingActivity extends BaseActivity implements OnClickListener{
    // 置顶... 
    private RelativeLayout rl_switch_chattotop;
    private RelativeLayout rl_switch_block_groupmsg;
    private RelativeLayout re_clear;

    // 状态变化
    private ImageView iv_switch_chattotop;
    private ImageView iv_switch_unchattotop;
    private ImageView iv_switch_block_groupmsg;
    private ImageView iv_switch_unblock_groupmsg;
	
	private LoadUserAvatar avatarLoader;
	private String userId;
    private String userNick;
    private String avatar;
    private String sex;
    private List<String> blackList;
    private Map<String, TopUser> topMap = new HashMap<String, TopUser>();
    
    private ProgressDialog progressDialog;
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_single_chat_setting);
		
		String MyAppDir = null;
		if(SDCardUtil.ExistSDCard()){
			String sdRootDir = SDCardUtil.getNormalSDPath();
			MyAppDir= sdRootDir+File.separator+"HuanXinApp";
		}else{
			String memRootDir = SDCardUtil.getPhoneCardPath();
			MyAppDir = memRootDir+File.separator+"HuanXinApp";
		}
		avatarLoader = new LoadUserAvatar(this,MyAppDir);
        userId = getIntent().getStringExtra("userId");  // 获取传过来的userId
        User user = MyApplication.getInstance().getContactList().get(userId);
        // 资料错误则不显示
        if (user == null) {
            return;
        }
        userNick = user.getNick();
        avatar = user.getAvatar();
        sex = user.getSex();
        
        // 黑名单列表
        blackList = EMContactManager.getInstance().getBlackListUsernames();
        // 置顶列表
        //topMap = MyApplication.getInstance().getTopUserList();
        
        progressDialog = new ProgressDialog(this);
        initView();
        initData();
	}
	
	private void initView() {
		rl_switch_chattotop = (RelativeLayout) findViewById(R.id.rl_switch_chattotop);
        rl_switch_block_groupmsg = (RelativeLayout) findViewById(R.id.rl_switch_block_groupmsg);
        re_clear = (RelativeLayout) findViewById(R.id.re_clear);

        iv_switch_chattotop = (ImageView) findViewById(R.id.iv_switch_chattotop);
        iv_switch_unchattotop = (ImageView) findViewById(R.id.iv_switch_unchattotop);
        iv_switch_block_groupmsg = (ImageView) findViewById(R.id.iv_switch_block_groupmsg);
        iv_switch_unblock_groupmsg = (ImageView) findViewById(R.id.iv_switch_unblock_groupmsg);

        // 初始化置顶和免打扰的状态
        if (!blackList.contains(userId)) {
            iv_switch_block_groupmsg.setVisibility(View.INVISIBLE);
            iv_switch_unblock_groupmsg.setVisibility(View.VISIBLE);
        } else {
            iv_switch_block_groupmsg.setVisibility(View.VISIBLE);
            iv_switch_unblock_groupmsg.setVisibility(View.INVISIBLE);
        }
        if (!topMap.containsKey(userId)) {
            // 当前状态是未置顶
            iv_switch_chattotop.setVisibility(View.INVISIBLE);
            iv_switch_unchattotop.setVisibility(View.VISIBLE);
        } else {
            // 当前状态是置顶
            iv_switch_chattotop.setVisibility(View.VISIBLE);
            iv_switch_unchattotop.setVisibility(View.INVISIBLE);
        }
	}
	
	private void initData() {
		rl_switch_chattotop.setOnClickListener(this);
        rl_switch_block_groupmsg.setOnClickListener(this);
        re_clear.setOnClickListener(this);

        ImageView iv_avatar = (ImageView) this.findViewById(R.id.iv_avatar);
        TextView tv_username = (TextView) this.findViewById(R.id.tv_username);
        tv_username.setText(userNick);
        iv_avatar.setImageResource(R.drawable.default_useravatar);
        iv_avatar.setTag(avatar);
        if (avatar != null && !avatar.equals("")) {
            Bitmap bitmap = avatarLoader.loadImage(iv_avatar, avatar,
                    new ImageDownloadedCallBack() {
                        @Override
                        public void onImageDownloaded(ImageView imageView,Bitmap bitmap) {
                            if (imageView.getTag() == avatar) {
                                imageView.setImageBitmap(bitmap);
                            }
                        }
                    });
            if (bitmap != null) {
                iv_avatar.setImageBitmap(bitmap);
            }
        }
        
        iv_avatar.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivity(new Intent(ChatSingleSettingActivity.this,UserInfoActivity.class)
                		.putExtra("hxid", userId).putExtra("nick", userNick)
                		.putExtra("avatar", avatar).putExtra("sex", sex));
            }
        });
        
        ImageView iv_avatar2 = (ImageView) this.findViewById(R.id.iv_avatar2);
        iv_avatar2.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
            	//TODO 进入群聊,创建聊天室
            }
        });
	}

	@Override
	public void onClick(View v) {
		 switch (v.getId()) {
	        case R.id.rl_switch_block_groupmsg: // 设置免打扰
	        	progressDialog.setMessage("正在设置免打扰...");
	            progressDialog.setCanceledOnTouchOutside(false);
	            progressDialog.show();
	            
	            if (iv_switch_block_groupmsg.getVisibility() == View.VISIBLE) {
	                new Handler().postDelayed(new Runnable() {
	                    public void run() {
	                        removeOutBlacklist(userId); //移除黑名单
	                        progressDialog.dismiss();
	                    }
	                }, 2000);
	            } else {
	                moveToBlacklist(userId); //加入黑名单
	            }
	        	break;
	        case R.id.re_clear:  // 清空聊天记录
	        	progressDialog.setMessage("正在清空消息...");
	            progressDialog.show();
	            new Handler().postDelayed(new Runnable() {
					@Override
					public void run() {
						EMChatManager.getInstance().clearConversation(userId);
						progressDialog.dismiss();
						T.show(getApplicationContext(),"清空消息成功");
					}
				},2000);
	        	break;
	        case R.id.rl_switch_chattotop:  // 当前状态是已经置顶,点击后取消置顶
	        	break;
	        default:
	        	break;
		 }
	}
	
	/**
     * 把user移入到免打扰
     */
    private void moveToBlacklist(final String username) {
        new Thread(new Runnable() {
            public void run() {
                try {
                    // 加入到黑名单
                    EMContactManager.getInstance().addUserToBlackList(username,false);
                    runOnUiThread(new Runnable() {
                        public void run() {
                            progressDialog.dismiss();
                            iv_switch_block_groupmsg.setVisibility(View.VISIBLE);
                            iv_switch_unblock_groupmsg.setVisibility(View.INVISIBLE);
                        }
                    });
                } catch (final EaseMobException e) {
                    e.printStackTrace();
                    runOnUiThread(new Runnable() {
                        public void run() {
                            progressDialog.dismiss();
                            Toast.makeText(getApplicationContext(),"设置失败，原因：" + e.toString(),Toast.LENGTH_SHORT).show();
                        }
                    });
                }
            }
        }).start();
    }
    
    /**
     * 移出免打扰
     * @param tobeRemoveUser
     */
    private void removeOutBlacklist(final String tobeRemoveUser) {
        try {
            // 移出黑名单
            EMContactManager.getInstance().deleteUserFromBlackList(tobeRemoveUser);
            iv_switch_block_groupmsg.setVisibility(View.INVISIBLE);
            iv_switch_unblock_groupmsg.setVisibility(View.VISIBLE);
        } catch (EaseMobException e) {
            e.printStackTrace();
            runOnUiThread(new Runnable() {
                public void run() {
                    Toast.makeText(getApplicationContext(), "设置失败",Toast.LENGTH_SHORT).show();
                }
            });
        }
    }
	
	public void back(View v) {
        finish();
    }
}
