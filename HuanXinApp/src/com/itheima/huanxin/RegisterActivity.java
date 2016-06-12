package com.itheima.huanxin;

import java.io.File;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.text.Editable;
import android.text.Html;
import android.text.Selection;
import android.text.Spannable;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.text.method.HideReturnsTransformationMethod;
import android.text.method.PasswordTransformationMethod;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.Window;
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
import com.itheima.util.T;

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
    private File myAppDir;  //保存图片的父目录
    private static final String cutPhotoName = "register_cut_photo.png";
    
    private static final int PHOTO_REQUEST_TAKEPHOTO = 1; // 拍照
    private static final int PHOTO_REQUEST_GALLERY = 2; // 从相册中选择
    private static final int PHOTO_REQUEST_CUT = 3; // 裁切结果
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_register);
		
		String sdRootDir = SDCardUtil.getNormalSDPath();
		myAppDir= new File(sdRootDir,"HuanXinApp");
		
		File cut_temp_file = new File(myAppDir,cutPhotoName);
		if(cut_temp_file!=null && cut_temp_file.exists()){
			cut_temp_file.delete();
		}
		
		dialog = new ProgressDialog(RegisterActivity.this);
		et_usernick = (EditText) findViewById(R.id.et_usernick);
	    et_usertel = (EditText) findViewById(R.id.et_usertel);
	    et_password = (EditText) findViewById(R.id.et_password);
	    
	    // 监听多个输入框
        et_usernick.addTextChangedListener(new TextChange());
        et_usertel.addTextChangedListener(new TextChange());
        et_password.addTextChangedListener(new TextChange());
        btn_register = (Button) findViewById(R.id.btn_register);
        
        iv_hide = (ImageView) findViewById(R.id.iv_hide);
        iv_show = (ImageView) findViewById(R.id.iv_show);
        
        tv_xieyi = (TextView) findViewById(R.id.tv_xieyi);
        iv_hide = (ImageView) findViewById(R.id.iv_hide);
        iv_show = (ImageView) findViewById(R.id.iv_show);
        iv_photo = (ImageView) findViewById(R.id.iv_photo);
        String xieyi = "<font color=" + "\"" + "#AAAAAA" + "\">" + "点击上面的"
                + "\"" + "注册" + "\"" + "按钮,即表示你同意" + "</font>" + "<u>"
                + "<font color=" + "\"" + "#576B95" + "\">" + "《腾讯微信软件许可及服务协议》"
                + "</font>" + "</u>";
        tv_xieyi.setText(Html.fromHtml(xieyi));
        
        iv_hide.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				iv_hide.setVisibility(View.GONE);
                iv_show.setVisibility(View.VISIBLE);
                et_password.setTransformationMethod(HideReturnsTransformationMethod.getInstance());
                // 切换后将EditText光标置于末尾
                CharSequence charSequence = et_password.getText();
                if(charSequence instanceof Spannable){
                	Spannable spanText = (Spannable) charSequence;
                	Selection.setSelection(spanText,charSequence.length());
                }
			}
		});
        
        iv_show.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				iv_show.setVisibility(View.GONE);
	            iv_hide.setVisibility(View.VISIBLE);
                et_password.setTransformationMethod(PasswordTransformationMethod.getInstance());
                // 切换后将EditText光标置于末尾
                CharSequence charSequence = et_password.getText();
                if (charSequence instanceof Spannable) {
                    Spannable spanText = (Spannable) charSequence;
                    Selection.setSelection(spanText, charSequence.length());
                }
			}
		});
        
        iv_photo.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				showCamera();
			}
		});
        
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
   			 	File myFile = new File(myAppDir,cutPhotoName);
                if (myFile.exists()) {
                	//TODO 本来需要继续质量压缩图片,将压缩后的图片上传,在此省略此步
                	
                    map.put("file",myAppDir+ File.separator +cutPhotoName);
                    map.put("image",cutPhotoName);
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
                                T.show(RegisterActivity.this,"该手机号码已被注册...");
                            } else if (code == 3) {
                                dialog.dismiss();
                                T.show(RegisterActivity.this,"服务器端注册失败...");
                            } else if (code == 4) {
                                dialog.dismiss();
                                T.show(RegisterActivity.this,"头像传输失败...");
                            } else if (code == 5) {
                                dialog.dismiss();
                                T.show(RegisterActivity.this,"返回环信id失败...");
                            } else {
                                dialog.dismiss();
                                T.show(RegisterActivity.this,"服务器繁忙请重试...");
                            }
                        } catch (JSONException e) {
                            dialog.dismiss();
                            T.show(RegisterActivity.this,"数据解析错误...");
                            e.printStackTrace();
                        }
                    }
                });
            }
        });
	}
	
	private void showCamera(){
		final AlertDialog dlg = new AlertDialog.Builder(this).create();
        dlg.show();
        Window window = dlg.getWindow();
        window.setContentView(R.layout.alertdialog);
        
        //为确认按钮添加事件,执行退出应用操作
        TextView tv_paizhao = (TextView) window.findViewById(R.id.tv_content1);
        tv_paizhao.setText("拍照");
        tv_paizhao.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                imageName = getNowTime() + ".png";
                Intent intent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
                intent.putExtra(MediaStore.EXTRA_OUTPUT,Uri.fromFile(new File(myAppDir,imageName)));
                startActivityForResult(intent, PHOTO_REQUEST_TAKEPHOTO);
                dlg.cancel();
            }
        });
        
        TextView tv_xiangce = (TextView) window.findViewById(R.id.tv_content2);
        tv_xiangce.setText("相册");
        tv_xiangce.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                getNowTime();
                imageName = getNowTime() + ".png";
                Intent intent = new Intent(Intent.ACTION_PICK, null);
                intent.setDataAndType(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, "image/*");
                startActivityForResult(intent, PHOTO_REQUEST_GALLERY);
                dlg.cancel();
            }
        });
	}
	
	@Override
	protected void onActivityResult(int requestCode, int resultCode, Intent data) {
		if (resultCode == RESULT_OK) {
			switch (requestCode) {
			case PHOTO_REQUEST_TAKEPHOTO:
				startPhotoZoom(Uri.fromFile(new File(myAppDir,imageName)),480);
				break;
			case PHOTO_REQUEST_GALLERY:
				if (data != null){
                    startPhotoZoom(data.getData(),480);
				}
				break;
			case PHOTO_REQUEST_CUT:
				File temp_file = new File(myAppDir,imageName);
				if(temp_file!=null && temp_file.exists()){
					temp_file.delete(); //删除裁切前保存的临时文件,确保myAppDir目录下只保留裁切后的
				}
				Bitmap bitmap = BitmapFactory.decodeFile(myAppDir+File.separator+cutPhotoName);
	            iv_photo.setImageBitmap(bitmap);
				break;
			}
			super.onActivityResult(requestCode, resultCode, data);
		}
	}
	
	/**
	 * 缩放图片
	 * @param uri
	 * @param size
	 */
	private void startPhotoZoom(Uri uri, int size){
        Intent intent = new Intent("com.android.camera.action.CROP");
        intent.setDataAndType(uri, "image/*");
        // crop为true是设置在开启的intent中设置显示的view可以剪裁
        intent.putExtra("crop", "true");

        // aspectX aspectY 是宽高的比例
        intent.putExtra("aspectX", 1);
        intent.putExtra("aspectY", 1);

        // outputX,outputY 是剪裁图片的宽高
        intent.putExtra("outputX", size);
        intent.putExtra("outputY", size);
        intent.putExtra("return-data", false);

        intent.putExtra(MediaStore.EXTRA_OUTPUT,Uri.fromFile(new File(myAppDir,cutPhotoName)));
        intent.putExtra("outputFormat", Bitmap.CompressFormat.PNG.toString());
        intent.putExtra("noFaceDetection", true); 
        startActivityForResult(intent, PHOTO_REQUEST_CUT); //执行裁切
	}
	
	
	private String getNowTime() {
        Date date = new Date(System.currentTimeMillis());
        SimpleDateFormat dateFormat = new SimpleDateFormat("MMddHHmmssSS");
        return dateFormat.format(date);
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
                                if (!RegisterActivity.this.isFinishing()){
                                    dialog.dismiss();
                                }
                                int errorCode = e.getErrorCode();
                                if (errorCode == EMError.NONETWORK_ERROR) {
                                    Toast.makeText(getApplicationContext(),st7, Toast.LENGTH_SHORT).show();
                                } else if (errorCode == EMError.USER_ALREADY_EXISTS) {
                                    Toast.makeText(getApplicationContext(),st8, Toast.LENGTH_SHORT).show();
                                } else if (errorCode == EMError.UNAUTHORIZED) {
                                    Toast.makeText(getApplicationContext(),st9, Toast.LENGTH_SHORT).show();
                                } else {
                                    Toast.makeText(getApplicationContext(),st10 + e.getMessage(),Toast.LENGTH_SHORT).show();
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
