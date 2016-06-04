package com.itheima.huanxin;

import java.io.File;
import java.util.HashMap;
import java.util.Map;

import android.app.ProgressDialog;
import android.os.Bundle;
import android.text.Editable;
import android.text.Html;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.alibaba.fastjson.JSONException;
import com.alibaba.fastjson.JSONObject;
import com.easemob.EMError;
import com.easemob.chat.EMChatManager;
import com.easemob.exceptions.EaseMobException;
import com.itheima.app.Constant;
import com.itheima.app.MyApplication;
import com.itheima.huanxin.other.LoadDataFromServer;
import com.itheima.huanxin.other.LoadDataFromServer.DataCallBack;
import com.itheima.util.SDCardUtil;

public class RegisterActivity extends BaseActivity{
	private EditText et_usernick;
	private EditText et_usertel;
	private EditText et_password;
	private Button btn_register;
	private ProgressDialog dialog;
    private TextView tv_xieyi;
    private ImageView iv_hide;
    private ImageView iv_show;
    private ImageView iv_photo;
    private String imageName;
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_register);
		
		dialog = new ProgressDialog(RegisterActivity.this);
		et_usernick = (EditText) findViewById(R.id.et_usernick);
	    et_usertel = (EditText) findViewById(R.id.et_usertel);
	    et_password = (EditText) findViewById(R.id.et_password);
	    
	    // 监听多个输入框
        et_usernick.addTextChangedListener(new TextChange());
        et_usertel.addTextChangedListener(new TextChange());
        et_password.addTextChangedListener(new TextChange());
        btn_register = (Button) findViewById(R.id.btn_register);
        
        tv_xieyi = (TextView) findViewById(R.id.tv_xieyi);
        iv_hide = (ImageView) findViewById(R.id.iv_hide);
        iv_show = (ImageView) findViewById(R.id.iv_show);
        iv_photo = (ImageView) findViewById(R.id.iv_photo);
        String xieyi = "<font color=" + "\"" + "#AAAAAA" + "\">" + "点击上面的"
                + "\"" + "注册" + "\"" + "按钮,即表示你同意" + "</font>" + "<u>"
                + "<font color=" + "\"" + "#576B95" + "\">" + "《腾讯微信软件许可及服务协议》"
                + "</font>" + "</u>";
        tv_xieyi.setText(Html.fromHtml(xieyi));
        
        btn_register.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                dialog.setMessage("正在注册...");
                dialog.setProgressStyle(ProgressDialog.STYLE_SPINNER);
                dialog.show();

                String usernick = et_usernick.getText().toString().trim();
                final String password = et_password.getText().toString().trim();
                String usertel = et_usertel.getText().toString().trim();
                Map<String, String> map = new HashMap<String, String>();
                
                // 保存用户图像
                String sdRootDir = SDCardUtil.getNormalSDPath();
   			 	File myAppDir= new File(sdRootDir,"HuanXinApp");
   			 	File myFile = new File(myAppDir+imageName);
                if (myFile.exists()) {
                    map.put("file",myAppDir+imageName);
                    map.put("image",imageName);
                } else {
                    map.put("image", "false");
                }
                map.put("usernick", usernick);
                map.put("usertel", usertel);
                map.put("password", password);
                
                LoadDataFromServer registerTask = new LoadDataFromServer(
                        RegisterActivity.this, Constant.URL_Register_Tel, map);
                registerTask.getData(new DataCallBack() {
                    @Override
                    public void onDataCallBack(JSONObject data) {
                        try {
                            int code = data.getInteger("code");
                            if (code == 1) {
                                String hxid = data.getString("hxid");
                                register(hxid, password); //注册
                            } else if (code == 2) {
                                dialog.dismiss();
                                Toast.makeText(RegisterActivity.this,
                                        "该手机号码已被注册...", Toast.LENGTH_SHORT).show();
                            } else if (code == 3) {
                                dialog.dismiss();
                                Toast.makeText(RegisterActivity.this,
                                        "服务器端注册失败...", Toast.LENGTH_SHORT).show();
                            } else if (code == 4) {
                                dialog.dismiss();
                                Toast.makeText(RegisterActivity.this,
                                        "头像传输失败...", Toast.LENGTH_SHORT).show();
                            } else if (code == 5) {
                                dialog.dismiss();
                                Toast.makeText(RegisterActivity.this,
                                        "返回环信id失败...", Toast.LENGTH_SHORT).show();
                            } else {
                                dialog.dismiss();
                                Toast.makeText(RegisterActivity.this,
                                        "服务器繁忙请重试...", Toast.LENGTH_SHORT).show();
                            }
                        } catch (JSONException e) {
                            dialog.dismiss();
                            Toast.makeText(RegisterActivity.this, "数据解析错误...",
                                    Toast.LENGTH_SHORT).show();
                            e.printStackTrace();
                        }
                    }
                });
            }
        });
	}
	
	/**
     * 注册
     * @param view
     */
    public void register(final String hxid, final String password) {
        final String st6 = getResources().getString(R.string.Registered_successfully);
        if (!TextUtils.isEmpty(hxid) && !TextUtils.isEmpty(password)) {
            final String st7 = getResources().getString(R.string.network_anomalies);
            final String st8 = getResources().getString(R.string.User_already_exists);
            final String st9 = getResources().getString(R.string.registration_failed_without_permission);
            final String st10 = getResources().getString(R.string.Registration_failed);
            new Thread(new Runnable() {
                public void run() {
                    try {
                        // 调用sdk注册方法
                        EMChatManager.getInstance().createAccountOnServer(hxid,password);
                        runOnUiThread(new Runnable() {
                            public void run() {
                                if (!RegisterActivity.this.isFinishing()){
                                    dialog.dismiss();
                                }
                                // 保存用户名
                                MyApplication.getInstance().setUserName(hxid);
                                Toast.makeText(getApplicationContext(), st6, 0).show();
                                finish();
                            }
                        });
                    } catch (final EaseMobException e) {
                        runOnUiThread(new Runnable() {
                            public void run() {
                                if (!RegisterActivity.this.isFinishing())
                                    dialog.dismiss();
                                int errorCode = e.getErrorCode();
                                if (errorCode == EMError.NONETWORK_ERROR) {
                                    Toast.makeText(getApplicationContext(),st7, Toast.LENGTH_SHORT).show();
                                } else if (errorCode == EMError.USER_ALREADY_EXISTS) {
                                    Toast.makeText(getApplicationContext(),st8, Toast.LENGTH_SHORT).show();
                                } else if (errorCode == EMError.UNAUTHORIZED) {
                                    Toast.makeText(getApplicationContext(),st9, Toast.LENGTH_SHORT).show();
                                } else {
                                    Toast.makeText(getApplicationContext(),st10 + e.getMessage(),
                                            Toast.LENGTH_SHORT).show();
                                }
                            }
                        });
                    }
                }
            }).start();
        }
    }

	
	// EditText监听器
    class TextChange implements TextWatcher {
        @Override
        public void afterTextChanged(Editable arg0) {

        }

        @Override
        public void beforeTextChanged(CharSequence arg0, int arg1, int arg2, int arg3) {

        }

        @Override
        public void onTextChanged(CharSequence cs, int start, int before,int count) {
            boolean Sign1 = et_usernick.getText().length() > 0;
            boolean Sign2 = et_usertel.getText().length() > 0;
            boolean Sign3 = et_password.getText().length() > 0;

            if (Sign1 & Sign2 & Sign3) {
                btn_register.setTextColor(0xFFFFFFFF);
                btn_register.setEnabled(true);
            }else {
            	// 在layout文件中，对Button的text属性应预先设置默认值，否则刚打开程序的时候Button是无显示的
                btn_register.setTextColor(0xFFD0EFC6);
                btn_register.setEnabled(false);
            }
        }
    }
}
