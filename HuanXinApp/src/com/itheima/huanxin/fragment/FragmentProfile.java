package com.itheima.huanxin.fragment;

import java.io.File;

import android.app.ProgressDialog;
import android.content.Intent;
import android.graphics.Bitmap;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.easemob.EMCallBack;
import com.itheima.app.Constant;
import com.itheima.app.MyApplication;
import com.itheima.huanxin.LoginActivity;
import com.itheima.huanxin.MyUserInfoActivity;
import com.itheima.huanxin.R;
import com.itheima.huanxin.SettingActivity;
import com.itheima.huanxin.other.LoadUserAvatar;
import com.itheima.huanxin.other.LoadUserAvatar.ImageDownloadedCallBack;
import com.itheima.huanxin.other.LocalUserInfo;
import com.itheima.util.SDCardUtil;

/**
 * 个人信息(我)
 * @author zhangming
 * @date 2016/06/04
 */
public class FragmentProfile extends Fragment{
	private LoadUserAvatar avatarLoader;
    private String avatar = "";
    private ImageView iv_avatar;
    private TextView tv_name;
    private TextView tv_fxid;
    private String nick;
    private String fxid;
    
	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container,
			Bundle savedInstanceState) {
		 return inflater.inflate(R.layout.fragment_profile, container, false);
	}
	
	@Override
	public void onActivityCreated(Bundle savedInstanceState) {
		super.onActivityCreated(savedInstanceState);
		
		String MyAppDir = null;
		if(SDCardUtil.ExistSDCard()){
			String sdRootDir = SDCardUtil.getNormalSDPath();
			MyAppDir= sdRootDir+File.separator+"HuanXinApp";
		}else{
			String memRootDir = SDCardUtil.getPhoneCardPath();
			MyAppDir = memRootDir+File.separator+"HuanXinApp";
		}
		avatarLoader = new LoadUserAvatar(getActivity(), MyAppDir);
		
		RelativeLayout re_myinfo = (RelativeLayout) getView().findViewById(R.id.re_myinfo);
        re_myinfo.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivity(new Intent(getActivity(),MyUserInfoActivity.class));
            }
        });
        RelativeLayout re_setting = (RelativeLayout) getView().findViewById(R.id.re_setting);
        re_setting.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivity(new Intent(getActivity(),SettingActivity.class));
            }
        });
        
        nick = LocalUserInfo.getInstance(getActivity()).getUserInfo("nick");
        fxid = LocalUserInfo.getInstance(getActivity()).getUserInfo("fxid");

        avatar = LocalUserInfo.getInstance(getActivity()).getUserInfo("avatar");
        iv_avatar = (ImageView) re_myinfo.findViewById(R.id.iv_avatar);
        tv_name = (TextView) re_myinfo.findViewById(R.id.tv_name);
        tv_fxid = (TextView) re_myinfo.findViewById(R.id.tv_fxid);
        tv_name.setText(nick);
        if (fxid.equals("0")) {
            tv_fxid.setText("微信号：未设置");
        } else {
            tv_fxid.setText("微信号:" + fxid);
        }
        showUserAvatar(iv_avatar, avatar);
	}
	
	private void showUserAvatar(ImageView imageView, String avatar) {
		final String url_avatar = Constant.URL_Avatar + avatar;
		imageView.setTag(url_avatar);
		if (url_avatar != null && !url_avatar.equals("")) {
            Bitmap bitmap = avatarLoader.loadImage(imageView,url_avatar,
            	new ImageDownloadedCallBack(){
                    @Override
                    public void onImageDownloaded(ImageView imageView,Bitmap bitmap){
                        if (imageView.getTag() == url_avatar){
                            imageView.setImageBitmap(bitmap);
                        }
                    }
                });
            if (bitmap != null){
                imageView.setImageBitmap(bitmap);
            }
	    }
	}
	
	@Override
    public void onResume() {
        super.onResume();
        String vatar_temp = LocalUserInfo.getInstance(getActivity()).getUserInfo("avatar");
        if (!vatar_temp.equals(avatar)) {
            showUserAvatar(iv_avatar, avatar);
        }

        String nick_temp = LocalUserInfo.getInstance(getActivity()).getUserInfo("nick");
        String fxid_temp = LocalUserInfo.getInstance(getActivity()).getUserInfo("fxid");
        if (!nick_temp.equals(nick)) {
            tv_name.setText(nick_temp);
        }
        if (!fxid_temp.equals(fxid)) {
            if (fxid_temp.equals("0")) {
                tv_fxid.setText("微信号：未设置");
            } else {
                tv_fxid.setText("微信号:" + fxid_temp);
            }
        }
    }
}
