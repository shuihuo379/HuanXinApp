package com.itheima.util;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;

 




import java.io.OutputStream;

import com.alibaba.fastjson.util.IOUtils;

import android.annotation.SuppressLint;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Environment;
import android.util.Log;

public class FileUtil {
    private static final String TAG = FileUtil.class.getSimpleName();
    private String local_image_path;
 
    public FileUtil(Context context,String local_image_path) {
        this.local_image_path = local_image_path;
    }
 
    /* Checks if external storage is available for read and write */
    public boolean isExternalStorageWritable() {
        String state = Environment.getExternalStorageState();
        if (Environment.MEDIA_MOUNTED.equals(state)) {
            return true;
        }
        return false;
    }

    /**
     * 保存图片到制定路径
     * 
     * @param filepath
     * @param bitmap
     */
    public void saveBitmap(String filename, Bitmap bitmap) {
        if (!isExternalStorageWritable()) {
            Log.i(TAG, "SD卡不可用，保存失败");
            return;
        }

        if (bitmap == null) {
            return;
        }
     
        try {
            
            File file = new File(local_image_path,filename);
            FileOutputStream outputstream = new FileOutputStream(file);
            if((filename.indexOf("png") != -1)||(filename.indexOf("PNG") != -1))  
            {  
                bitmap.compress(Bitmap.CompressFormat.PNG, 100, outputstream);
            }  else{
                bitmap.compress(Bitmap.CompressFormat.JPEG, 100, outputstream);
            }
            
            outputstream.flush();
            outputstream.close();
            
        } catch (FileNotFoundException e) {
            Log.i(TAG, e.getMessage());
        } catch (IOException e) {
            Log.i(TAG, e.getMessage());
        } 
    }

    /**
     * 返回当前应用 SD 卡的绝对路径 like
     * /storage/sdcard0/Android/data/com.example.test/files
     */
    @SuppressLint("SdCardPath")
    public String getAbsolutePath() {
        File root = new File(local_image_path);
        if(!root.exists()){
            root.mkdirs();
            
        }
         
            
     return local_image_path;
         

    }

    @SuppressLint("SdCardPath")
    public boolean isBitmapExists(String filename) {
        File dir =new File(local_image_path);
         if(!dir.exists()){
             dir.mkdirs();
            }
                //context.getExternalFilesDir(null);
        File file = new File(dir, filename);

        return file.exists();
    }
    
    /**
     * 压缩PNG格式图片文件
     * @param srcFile
     * @return
     * @throws IOException
     */
    public static File compressPNGImage(File srcFile) throws IOException{
    	Bitmap bitmap = BitmapFactory.decodeStream(new FileInputStream(srcFile));
    	ByteArrayOutputStream baos = new ByteArrayOutputStream();  
    	bitmap.compress(Bitmap.CompressFormat.PNG, 100, baos);//质量压缩方法，这里100表示不压缩，把压缩后的数据存放到baos中  
        int options = 100;  
        while ( baos.toByteArray().length / 1024>80) {  //循环判断如果压缩后图片是否大于80kb,大于继续压缩         
            baos.reset();//重置baos即清空baos  
            bitmap.compress(Bitmap.CompressFormat.PNG, options, baos);//这里压缩options%，把压缩后的数据存放到baos中  
            options -= 10;//每次都减少10  
        }  
        ByteArrayInputStream isBm = new ByteArrayInputStream(baos.toByteArray());//把压缩后的数据baos存放到ByteArrayInputStream中  
   
        File destFile = new File("compress_temp_photo.png");
        OutputStream out = new FileOutputStream(srcFile);
        int content;
        while((content = isBm.read())!=-1){
			out.write(content);
			out.flush();
        }
        return destFile;
    }
}
