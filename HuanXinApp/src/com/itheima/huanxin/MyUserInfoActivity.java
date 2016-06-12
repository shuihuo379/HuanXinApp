package com.itheima.huanxin;

import java.io.File;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

import android.annotation.SuppressLint;
import android.app.AlertDialog;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.Window;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.alibaba.fastjson.JSONException;
import com.alibaba.fastjson.JSONObject;
import com.itheima.app.Constant;
import com.itheima.huanxin.other.LoadDataFromServer;
import com.itheima.huanxin.other.LoadDataFromServer.DataCallBack;
import com.itheima.huanxin.other.LoadUserAvatar;
import com.itheima.huanxin.other.LoadUserAvatar.ImageDownloadedCallBack;
import com.itheima.huanxin.other.LocalUserInfo;
import com.itheima.util.SDCardUtil;

/**
 * 我的信息界面
 * @author zhangming
 * @date 2016/06/07
 */
public class MyUserInfoActivity extends BaseActivity{
    private RelativeLayout re_avatar;
    private RelativeLayout re_name;
    private RelativeLayout re_fxid;
    private RelativeLayout re_sex;
    private RelativeLayout re_region;

    private ImageView iv_avatar;
    private TextView tv_name;
    private TextView tv_fxid;
    private TextView tv_sex;
    private TextView tv_sign;
    
    private LoadUserAvatar avatarLoader;
    private String MyAppDir;
    
    private String hxid;
    private String fxid;
    private String sex;
    private String sign;
    private String nick;
    private String imageName;
    private static final String cutPhotoName = "userinfo_cut_photo.png";
    
    private static final int PHOTO_REQUEST_TAKEPHOTO = 1; // 拍照
    private static final int PHOTO_REQUEST_GALLERY = 2; // 从相册中选择
    private static final int PHOTO_REQUEST_CUT = 3; //裁切
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_myinfo);
		
		if(SDCardUtil.ExistSDCard()){
			String sdRootDir = SDCardUtil.getNormalSDPath();
			MyAppDir= sdRootDir+File.separator+"HuanXinApp";
		}else{
			String memRootDir = SDCardUtil.getPhoneCardPath();
			MyAppDir = memRootDir+File.separator+"HuanXinApp";
		}
		
		File cut_temp_file = new File(MyAppDir,cutPhotoName);
		if(cut_temp_file!=null && cut_temp_file.exists()){
			cut_temp_file.delete();
		}
		
		avatarLoader = new LoadUserAvatar(this, MyAppDir);
		initView();
	}

	private void initView() {
		hxid = LocalUserInfo.getInstance(MyUserInfoActivity.this).getUserInfo("hxid");
        nick = LocalUserInfo.getInstance(MyUserInfoActivity.this).getUserInfo("nick");
    	fxid = LocalUserInfo.getInstance(MyUserInfoActivity.this).getUserInfo("fxid");
        sex = LocalUserInfo.getInstance(MyUserInfoActivity.this).getUserInfo("sex");
        sign = LocalUserInfo.getInstance(MyUserInfoActivity.this).getUserInfo("sign");
        String avatar = LocalUserInfo.getInstance(MyUserInfoActivity.this).getUserInfo("avatar");
        
        re_avatar = (RelativeLayout) this.findViewById(R.id.re_avatar);
        re_name = (RelativeLayout) this.findViewById(R.id.re_name);
        re_fxid = (RelativeLayout) this.findViewById(R.id.re_fxid);
        re_sex = (RelativeLayout) this.findViewById(R.id.re_sex);
        re_region = (RelativeLayout) this.findViewById(R.id.re_region);
        
        MyListener listener = new MyListener();
        re_avatar.setOnClickListener(listener);
        re_name.setOnClickListener(listener);
        re_fxid.setOnClickListener(listener);
        re_sex.setOnClickListener(listener);
        re_region.setOnClickListener(listener);
        
        // 头像
        iv_avatar = (ImageView) this.findViewById(R.id.iv_avatar);
        tv_name = (TextView) this.findViewById(R.id.tv_name);
        tv_fxid = (TextView) this.findViewById(R.id.tv_fxid);
        tv_sex = (TextView) this.findViewById(R.id.tv_sex);
        tv_sign = (TextView) this.findViewById(R.id.tv_sign);
        tv_name.setText(nick);
        
        if (fxid.equals("0")) {
            tv_fxid.setText("未设置");
        } else {
            tv_fxid.setText(fxid);
        }
        if (sex.equals("1")) {
            tv_sex.setText("男");
        } else if (sex.equals("2")) {
            tv_sex.setText("女");
        } else {
            tv_sex.setText("");
        }
        if (sign.equals("0")) {
            tv_sign.setText("未填写");
        } else {
            tv_sign.setText(sign);
        }
        showUserAvatar(iv_avatar, avatar);
	}
	
	private void showUserAvatar(ImageView imageView, String avatar) {
        final String url_avatar = Constant.URL_Avatar + avatar;
        imageView.setTag(url_avatar);
        if (url_avatar != null && !url_avatar.equals("")) {
            Bitmap bitmap = avatarLoader.loadImage(imageView, url_avatar,
                new ImageDownloadedCallBack() {
                    @Override
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
	
	
	class MyListener implements OnClickListener {
        @Override
        public void onClick(View v) {
            switch (v.getId()) {
            case R.id.re_avatar: //点击头像
            	showPhotoDialog();
                break;
            case R.id.re_name: //点击昵称
            	startActivity(new Intent(MyUserInfoActivity.this,UpdateNickActivity.class));
                break;
            case R.id.re_fxid: //点击微信号
            	/**
            	if (LocalUserInfo.getInstance(MyUserInfoActivity.this)
                        .getUserInfo("fxid").equals("0")) {
            		startActivity(new Intent(MyUserInfoActivity.this,UpdateFxidActivity.class));
            	}
            	**/
            	startActivity(new Intent(MyUserInfoActivity.this,UpdateFxidActivity.class));
                break;
            case R.id.re_sex:  //点击性别
            	showSexDialog(); 
                break;
            case R.id.re_region: //点击区域
                break;
            }
        }
    }

	/**
	 * 显示拍照或相册的选择对话框
	 */
	public void showPhotoDialog() {
		final AlertDialog dialog = new AlertDialog.Builder(this).create();
		dialog.show();
		Window window = dialog.getWindow(); //注意:在此之前需要调用AlertDialog中的show方法
		window.setContentView(R.layout.alertdialog);
		
		// 为确认按钮添加事件,执行退出应用操作
        TextView tv_paizhao = (TextView) window.findViewById(R.id.tv_content1);
        tv_paizhao.setText("拍照");
        tv_paizhao.setOnClickListener(new View.OnClickListener() {
            @SuppressLint("SdCardPath")
            public void onClick(View v) {
            	imageName = getNowTime() + ".png";
            	Intent intent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
            	// 指定调用相机拍照后照片的储存路径
            	intent.putExtra(MediaStore.EXTRA_OUTPUT,Uri.fromFile(new File(MyAppDir,imageName)));
            	startActivityForResult(intent, PHOTO_REQUEST_TAKEPHOTO);
            	dialog.cancel();
            }
        });
        
        TextView tv_xiangce = (TextView) window.findViewById(R.id.tv_content2);
        tv_xiangce.setText("相册");
        tv_xiangce.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
            	imageName = getNowTime() + ".png";
            	Intent intent = new Intent(Intent.ACTION_PICK);
            	intent.setDataAndType(MediaStore.Images.Media.EXTERNAL_CONTENT_URI,"image/*");
            	startActivityForResult(intent, PHOTO_REQUEST_GALLERY);
            	dialog.cancel();
            }
        });
	}
	
    private void startPhotoZoom(Uri uri, int size) {
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

        intent.putExtra(MediaStore.EXTRA_OUTPUT,Uri.fromFile(new File(MyAppDir,cutPhotoName)));
        intent.putExtra("outputFormat", Bitmap.CompressFormat.PNG.toString());
        intent.putExtra("noFaceDetection", true); // no face detection
        startActivityForResult(intent, PHOTO_REQUEST_CUT);
    }
	
    @Override
    protected void onRestart() {
    	super.onRestart();
    	String fxId = LocalUserInfo.getInstance(MyUserInfoActivity.this).getUserInfo("fxid");
    	if (fxId.equals("0")) {
    		tv_fxid.setText("微信号：未设置");
    	}else{
    		tv_fxid.setText(fxId);
    	}
    	
    	String nick = LocalUserInfo.getInstance(MyUserInfoActivity.this).getUserInfo("nick");
    	tv_name.setText(nick);
    }
    
	@Override
	protected void onActivityResult(int requestCode, int resultCode, Intent data) {
		 if (resultCode == RESULT_OK) {
            switch (requestCode) {
            case PHOTO_REQUEST_TAKEPHOTO:
                startPhotoZoom(Uri.fromFile(new File(MyAppDir, imageName)),480);
                break;
            case PHOTO_REQUEST_GALLERY:
                if (data != null){
                    startPhotoZoom(data.getData(), 480);
                }
                break;
            case PHOTO_REQUEST_CUT:
                // BitmapFactory.Options options = new BitmapFactory.Options();
                ///**
                // * 最关键在此，把options.inJustDecodeBounds = true;
                // * 这里再decodeFile()，返回的bitmap为空,但此时调用options.outHeight时，已经包含了图片的高了
                // */
                // options.inJustDecodeBounds = true;
            	File temp_file = new File(MyAppDir,imageName);
            	if(temp_file!=null && temp_file.exists()){
					temp_file.delete(); //删除裁切前保存的临时文件,确保myAppDir目录下只保留裁切后的文件
				}
                //Bitmap bitmap = BitmapFactory.decodeFile(MyAppDir + File.separator +cutPhotoName);
                //iv_avatar.setImageBitmap(bitmap); //此处改为头像更新成功后再设置
                updateAvatarInServer(cutPhotoName);  //上传头像到服务器中
                break;
            }
            super.onActivityResult(requestCode, resultCode, data);
	     }
	}
	
	/**
	 * 更新用户头像
	 * @param image 头像文件的名称
	 */
    private void updateAvatarInServer(final String image) {
        Map<String, String> map = new HashMap<String, String>();
        if ((new File(MyAppDir,image)).exists()) {
            map.put("file", MyAppDir + File.separator + image);
            map.put("image",image);
        } else {
            return;
        }
        map.put("hxid", hxid);
        LoadDataFromServer task = new LoadDataFromServer(MyUserInfoActivity.this, Constant.URL_UPDATE_Avatar, map);
        task.getData(new DataCallBack() {
            @Override
            public void onDataCallBack(JSONObject data) { 
                try {
                    int code = data.getInteger("code");
                    if (code == 1) {
                    	//保存用户头像信息
                        LocalUserInfo.getInstance(MyUserInfoActivity.this).setUserInfo("avatar", image); 
                        Bitmap bitmap = BitmapFactory.decodeFile(MyAppDir + File.separator +cutPhotoName);
                        iv_avatar.setImageBitmap(bitmap);  //此回调方法onDataCallBack是在主线程中调用的,直接设置即可
                    } else if (code == 2) {
                        Toast.makeText(MyUserInfoActivity.this, "更新失败...",Toast.LENGTH_SHORT).show();
                    } else if (code == 3) {
                        Toast.makeText(MyUserInfoActivity.this, "图片上传失败...",Toast.LENGTH_SHORT).show();
                    } else {
                        Toast.makeText(MyUserInfoActivity.this, "服务器繁忙请重试...",Toast.LENGTH_SHORT).show();
                    }
                } catch (JSONException e) {
                    Toast.makeText(MyUserInfoActivity.this, "数据解析错误...",Toast.LENGTH_SHORT).show();
                    e.printStackTrace();
                }
            }
        });
    }
    
    /**
     * 显示更改用户性别对话框
     */
    private void showSexDialog() {
    	 final AlertDialog dialog = new AlertDialog.Builder(this).create();
    	 dialog.show();
         Window window = dialog.getWindow();
         // *** 主要就是在这里实现这种效果的.
         // 设置窗口的内容页面,shrew_exit_dialog.xml文件中定义view内容
         window.setContentView(R.layout.alertdialog);
         
         LinearLayout ll_title = (LinearLayout) window.findViewById(R.id.ll_title);
         ll_title.setVisibility(View.VISIBLE);
         
         TextView tv_title = (TextView) window.findViewById(R.id.tv_title);
         tv_title.setText("性别");
         // 为确认按钮添加事件,执行退出应用操作
         TextView tv_paizhao = (TextView) window.findViewById(R.id.tv_content1);
         tv_paizhao.setText("男");
         tv_paizhao.setOnClickListener(new View.OnClickListener() {
             @SuppressLint("SdCardPath")
             public void onClick(View v) {
                 if (!sex.equals("1")) {
                     tv_sex.setText("男");
                     updateSexInServer("1");
                 }
                 dialog.cancel();
             }
         });
         
         TextView tv_xiangce = (TextView) window.findViewById(R.id.tv_content2);
         tv_xiangce.setText("女");
         tv_xiangce.setOnClickListener(new View.OnClickListener() {
             public void onClick(View v) {
                 if (!sex.equals("2")) {
                     tv_sex.setText("女");
                     updateSexInServer("2");
                 }
                 dialog.cancel();
             }
         });
    }
    
    /**
     * 更新用户性别
     * @param sexnum
     */
    private void updateSexInServer(final String sexnum){
    	 Map<String, String> map = new HashMap<String, String>();
         map.put("hxid", hxid);
         map.put("sex", sexnum);
         LoadDataFromServer task = new LoadDataFromServer(MyUserInfoActivity.this, Constant.URL_UPDATE_Sex, map);
         task.getData(new DataCallBack() {
             @Override
             public void onDataCallBack(JSONObject data) {
                 try {
                     int code = data.getInteger("code");
                     if (code == 1) {
                         LocalUserInfo.getInstance(MyUserInfoActivity.this).setUserInfo("sex", sexnum);
                     } else if (code == 2) {
                         Toast.makeText(MyUserInfoActivity.this, "更新失败...",Toast.LENGTH_SHORT).show();
                     } else {
                         Toast.makeText(MyUserInfoActivity.this, "服务器繁忙请重试...",Toast.LENGTH_SHORT).show();
                     }
                 } catch (JSONException e) {
                     Toast.makeText(MyUserInfoActivity.this, "数据解析错误...",Toast.LENGTH_SHORT).show();
                     e.printStackTrace();
                 }
             }
         });
    }
	
	@SuppressLint("SimpleDateFormat")
	private String getNowTime() {
        Date date = new Date(System.currentTimeMillis());
        SimpleDateFormat dateFormat = new SimpleDateFormat("MMddHHmmssSS");
        return dateFormat.format(date);
    }

    public void back(View view) {
        finish();
    }

}
