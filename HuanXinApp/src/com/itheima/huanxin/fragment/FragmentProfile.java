package com.itheima.huanxin.fragment;

import android.app.ProgressDialog;
import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.Button;

import com.easemob.EMCallBack;
import com.itheima.app.MyApplication;
import com.itheima.huanxin.LoginActivity;

/**
 * 个人信息(我)
 * @author zhangming
 * @date 2016/06/04
 */
public class FragmentProfile extends Fragment{
	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container,
			Bundle savedInstanceState) {
		Button btn = new Button(getActivity());
		btn.setText("点击退出");
		btn.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				logout();
			}
		});
		return btn;
	}
	
	/**
	 * 注销功能
	 */
	private void logout(){
		final ProgressDialog pd = new ProgressDialog(getActivity());
        pd.setMessage("正在退出登陆..");
        pd.setCanceledOnTouchOutside(false);
        pd.show();
        MyApplication.getInstance().logout(new EMCallBack() {
            @Override
            public void onSuccess() {
                getActivity().runOnUiThread(new Runnable() {
                    public void run() {
                        pd.dismiss();
                        // 重新显示登陆页面
                        getActivity().finish();
                        startActivity(new Intent(getActivity(), LoginActivity.class));
                    }
                });
            }
            
            @Override
            public void onProgress(int progress, String status) {
                
            }
            
            @Override
            public void onError(int code, String message) {
                
            }
        });
	}
}
