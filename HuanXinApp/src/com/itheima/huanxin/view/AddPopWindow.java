package com.itheima.huanxin.view;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.graphics.drawable.ColorDrawable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup.LayoutParams;
import android.widget.PopupWindow;
import android.widget.RelativeLayout;

import com.itheima.huanxin.AddFriendsOneActivity;
import com.itheima.huanxin.R;

public class AddPopWindow extends PopupWindow{
	private View contentView;
	 
	public AddPopWindow(final Activity activity){
		LayoutInflater inflater = (LayoutInflater) activity.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
		contentView = inflater.inflate(R.layout.add_popupwindow,null);
		
		this.setContentView(contentView);
		this.setWidth(LayoutParams.WRAP_CONTENT);
		this.setHeight(LayoutParams.WRAP_CONTENT);
		 // 设置SelectPicPopupWindow弹出窗体可点击
        this.setFocusable(true);
        this.setOutsideTouchable(true);
        this.update();  // 刷新状态
        
        // 实例化一个ColorDrawable颜色为半透明
        ColorDrawable colorDrawable = new ColorDrawable(0000000000);
        // 点back键和其他地方使其消失,设置了这个才能触发OnDismisslistener ，设置其他控件变化等操作
        this.setBackgroundDrawable(colorDrawable);
        // 设置SelectPicPopupWindow弹出窗体动画效果
        this.setAnimationStyle(R.style.AnimationPreview);
        
        RelativeLayout re_addfriends =(RelativeLayout) contentView.findViewById(R.id.re_addfriends);
        RelativeLayout re_chatroom =(RelativeLayout) contentView.findViewById(R.id.re_chatroom);
        re_addfriends.setOnClickListener(new OnClickListener(){
            @Override
            public void onClick(View v) {
            	activity.startActivity(new Intent(activity,AddFriendsOneActivity.class));  
                AddPopWindow.this.dismiss();
            }
        } );
        re_chatroom.setOnClickListener(new OnClickListener(){
            @Override
            public void onClick(View v) {
            	//activity.startActivity(new Intent(activity,CreateChatRoomActivity.class));  
                AddPopWindow.this.dismiss();
            }
        } );
	}
	
	/**
	 * 显示popupWindow
     * @param parent
     */
    public void showPopupWindow(View parent) {
    	if(!this.isShowing()){
    		this.showAsDropDown(parent, 0, 0);
    	}else{
    		this.dismiss();
    	}
    }
}
