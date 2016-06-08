package com.itheima.huanxin.other;

import java.io.File;
import java.io.InputStream;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import android.annotation.SuppressLint;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Handler;
import android.os.Message;
import android.util.Log;
import android.widget.ImageView;

import com.itheima.util.FileUtil;

/**
 * 用户头像图片异步加载封装
 * @author zhangming
 */
public class LoadUserAvatar {
    private BitmapCache bitmapCache; // 一级内存缓存基于 LruCache
    private FileUtil fileUtil;  // 二级文件缓存
   
    private ExecutorService threadPools;  //线程池
    private static final int MAX_THREAD_NUM = 5;  // 最大线程数
    
	public LoadUserAvatar(Context context, String local_image_path) {
	    bitmapCache = new BitmapCache();
        fileUtil = new FileUtil(context, local_image_path);
        threadPools = Executors.newFixedThreadPool(MAX_THREAD_NUM);
	}
	
	@SuppressLint("HandlerLeak")
	public Bitmap loadImage(final ImageView imageView, final String imageUrl,
	            final ImageDownloadedCallBack imageDownloadedCallBack) {
		 final String filename = imageUrl.substring(imageUrl.lastIndexOf("/") + 1);
	     final String filepath = fileUtil.getAbsolutePath() + File.separator +filename;
	     
	     // 先从内存中拿
	     Bitmap bitmap = bitmapCache.getBitmap(imageUrl);
	     if (bitmap != null) {
	    	 Log.i("test", "image exists in memory...");
	    	 return bitmap;
	     }
	     
	     // 从文件中找
	     if (fileUtil.isBitmapExists(filename)) {
	    	 Log.i("test", "image exists in file" + filename);
	    	 bitmap = BitmapFactory.decodeFile(filepath);
	    	 // 先缓存到内存
	    	 bitmapCache.putBitmap(imageUrl, bitmap);
	    	 return bitmap;
	     }
	     
	     // 内存和文件中都没有再从网络下载
	     if (imageUrl != null && !imageUrl.equals("")) {
	    	 final Handler handler = new Handler() {
                @Override
                public void handleMessage(Message msg) {
                    if (msg.what == 111 && imageDownloadedCallBack != null) {
                        Bitmap bitmap = (Bitmap) msg.obj;
                        imageDownloadedCallBack.onImageDownloaded(imageView,bitmap);
                    }
                }
	    	 };

             Thread thread = new Thread(){
                @Override
                public void run() {
                    Log.i("test",Thread.currentThread().getName() + " is running");
                    InputStream inputStream = HTTPService.getInstance().getStream(imageUrl);
                    BitmapFactory.Options options = new BitmapFactory.Options();
                    options.inJustDecodeBounds = false;
                    options.inSampleSize = 5;  // width，hight设为原来的五分之一
                    Bitmap bitmap = BitmapFactory.decodeStream(inputStream,null, options);

                    // 图片下载成功后缓存并执行回调刷新界面
                    if (bitmap != null) {
                        bitmapCache.putBitmap(imageUrl, bitmap); //先缓存到内存
                        fileUtil.saveBitmap(filename, bitmap);  //缓存到文件系统
                        Message msg = new Message();
                        msg.what = 111;
                        msg.obj = bitmap;
                        handler.sendMessage(msg);
                    }
                }
             };
	         threadPools.execute(thread);
	     }
	     return null;
	}
	
	/**
     * 图片下载完成回调接口
     */
    public interface ImageDownloadedCallBack {
        void onImageDownloaded(ImageView imageView, Bitmap bitmap);
    }
}
