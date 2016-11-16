package com.android.samchat.factory;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import android.graphics.Bitmap;
import android.graphics.Bitmap.Config;
import android.graphics.BitmapFactory;

public class ImageFactory {  
	  
    /** 
     * Get bitmap from specified image path 
     *  
     * @param imgPath 
     * @return 
     */  
    public Bitmap getBitmap(String imgPath) {  
        // Get bitmap through image path  
        BitmapFactory.Options newOpts = new BitmapFactory.Options();  
        newOpts.inJustDecodeBounds = false;  
        newOpts.inPurgeable = true;  
        newOpts.inInputShareable = true;  
        // Do not compress  
        newOpts.inSampleSize = 1;  
        newOpts.inPreferredConfig = Config.RGB_565;  
        return BitmapFactory.decodeFile(imgPath, newOpts);  
    }  
      
    /** 
     * Store bitmap into specified image path 
     *  
     * @param bitmap 
     * @param outPath 
     * @throws FileNotFoundException  
     */  
    public void storeImage(Bitmap bitmap, String outPath) throws FileNotFoundException {  
        FileOutputStream os = new FileOutputStream(outPath);  
        bitmap.compress(Bitmap.CompressFormat.JPEG, 100, os);  
    }  
      
    /** 
     * Compress image by pixel, this will modify image width/height.  
     * Used to get thumbnail 
     *  
     * @param imgPath image path 
     * @param pixelW target pixel of width 
     * @param pixelH target pixel of height 
     * @return 
     */  
    public Bitmap ratio(String imgPath, float pixelW, float pixelH) {  
        BitmapFactory.Options newOpts = new BitmapFactory.Options();    
        // ��ʼ����ͼƬ����ʱ��options.inJustDecodeBounds ���true����ֻ���߲�������  
        newOpts.inJustDecodeBounds = true;  
        newOpts.inPreferredConfig = Config.RGB_565;  
        // Get bitmap info, but notice that bitmap is null now    
        Bitmap bitmap = BitmapFactory.decodeFile(imgPath,newOpts);  
            
        newOpts.inJustDecodeBounds = false;    
        int w = newOpts.outWidth;    
        int h = newOpts.outHeight;    
        // ��Ҫ���ŵ�Ŀ��ߴ�  
        float hh = pixelH;// ���ø߶�Ϊ240fʱ���������Կ���ͼƬ��С��  
        float ww = pixelW;// ���ÿ��Ϊ120f���������Կ���ͼƬ��С��  
        // ���űȡ������ǹ̶��������ţ�ֻ�ø߻��߿�����һ�����ݽ��м��㼴��    
        int be = 1;//be=1��ʾ������    
        if (w > h && w > ww) {//�����ȴ�Ļ����ݿ�ȹ̶���С����    
            be = (int) (newOpts.outWidth / ww);    
        } else if (w < h && h > hh) {//����߶ȸߵĻ����ݿ�ȹ̶���С����    
            be = (int) (newOpts.outHeight / hh);    
        }    
        if (be <= 0) be = 1;    
        newOpts.inSampleSize = be;//�������ű���  
        // ��ʼѹ��ͼƬ��ע���ʱ�Ѿ���options.inJustDecodeBounds ���false��  
        bitmap = BitmapFactory.decodeFile(imgPath, newOpts);  
        // ѹ���ñ�����С���ٽ�������ѹ��  
//        return compress(bitmap, maxSize); // �����ٽ�������ѹ�������岻�󣬷�������Դ��ɾ��  
        return bitmap;  
    }  
      
    /** 
     * Compress image by size, this will modify image width/height.  
     * Used to get thumbnail 
     *  
     * @param image 
     * @param pixelW target pixel of width 
     * @param pixelH target pixel of height 
     * @return 
     */  
    public Bitmap ratio(Bitmap image, float pixelW, float pixelH) {  
        ByteArrayOutputStream os = new ByteArrayOutputStream();  
        image.compress(Bitmap.CompressFormat.JPEG, 100, os);  
        if( os.toByteArray().length / 1024>1024) {//�ж����ͼƬ����1M,����ѹ������������ͼƬ��BitmapFactory.decodeStream��ʱ���      
            os.reset();//����baos�����baos    
            image.compress(Bitmap.CompressFormat.JPEG, 50, os);//����ѹ��50%����ѹ��������ݴ�ŵ�baos��    
        }    
        ByteArrayInputStream is = new ByteArrayInputStream(os.toByteArray());    
        BitmapFactory.Options newOpts = new BitmapFactory.Options();    
        //��ʼ����ͼƬ����ʱ��options.inJustDecodeBounds ���true��    
        newOpts.inJustDecodeBounds = true;  
        newOpts.inPreferredConfig = Config.ALPHA_8;  
        Bitmap bitmap = BitmapFactory.decodeStream(is, null, newOpts);    
        newOpts.inJustDecodeBounds = false;    
        int w = newOpts.outWidth;    
        int h = newOpts.outHeight;    
        float hh = pixelH;// ���ø߶�Ϊ240fʱ���������Կ���ͼƬ��С��  
        float ww = pixelW;// ���ÿ��Ϊ120f���������Կ���ͼƬ��С��  
        //���űȡ������ǹ̶��������ţ�ֻ�ø߻��߿�����һ�����ݽ��м��㼴��    
        int be = 1;//be=1��ʾ������    
        if (w > h && w > ww) {//�����ȴ�Ļ����ݿ�ȹ̶���С����    
            be = (int) (newOpts.outWidth / ww);    
        } else if (w < h && h > hh) {//����߶ȸߵĻ����ݿ�ȹ̶���С����    
            be = (int) (newOpts.outHeight / hh);    
        }    
        if (be <= 0) be = 1;    
        newOpts.inSampleSize = be;//�������ű���    
        //���¶���ͼƬ��ע���ʱ�Ѿ���options.inJustDecodeBounds ���false��    
        is = new ByteArrayInputStream(os.toByteArray());    
        bitmap = BitmapFactory.decodeStream(is, null, newOpts);  
        //ѹ���ñ�����С���ٽ�������ѹ��  
//      return compress(bitmap, maxSize); // �����ٽ�������ѹ�������岻�󣬷�������Դ��ɾ��  
        return bitmap;  
    }  
      
    /** 
     * Compress by quality,  and generate image to the path specified 
     *  
     * @param image 
     * @param outPath 
     * @param maxSize target will be compressed to be smaller than this size.(kb) 
     * @throws IOException  
     */  
    public byte[] compressAndGenImage(Bitmap image, String outPath, int maxSize) throws IOException {  
        ByteArrayOutputStream os = new ByteArrayOutputStream();  
        // scale  
        int options = 100;  
        // Store the bitmap into output stream(no compress)  
        image.compress(Bitmap.CompressFormat.JPEG, options, os);    
        // Compress by loop  
        while ( os.toByteArray().length / 1024 > maxSize) {  
            // Clean up os  
            os.reset();  
            // interval 10  
            options -= 10;  
            image.compress(Bitmap.CompressFormat.JPEG, options, os);  
						
	     if(options == 0){
                 break;
	     }
        }  
          
        // Generate compressed image file  
        FileOutputStream fos = new FileOutputStream(outPath);    
        fos.write(os.toByteArray());    
        fos.flush();    
        fos.close();  
	 return os.toByteArray();
    }  
      
    /** 
     * Compress by quality,  and generate image to the path specified 
     *  
     * @param imgPath 
     * @param outPath 
     * @param maxSize target will be compressed to be smaller than this size.(kb) 
     * @param needsDelete Whether delete original file after compress 
     * @throws IOException  
     */  
    public void compressAndGenImage(String imgPath, String outPath, int maxSize, boolean needsDelete) throws IOException {  
        compressAndGenImage(getBitmap(imgPath), outPath, maxSize);  
          
        // Delete original file  
        if (needsDelete) {  
            File file = new File (imgPath);  
            if (file.exists()) {  
                file.delete();  
            }  
        }  
    }  
      
    /** 
     * Ratio and generate thumb to the path specified 
     *  
     * @param image 
     * @param outPath 
     * @param pixelW target pixel of width 
     * @param pixelH target pixel of height 
     * @throws FileNotFoundException 
     */  
	public Bitmap ratioAndGenThumb(Bitmap image, String outPath, float pixelW, float pixelH) throws FileNotFoundException {  
		Bitmap bitmap = ratio(image, pixelW, pixelH);  
		storeImage( bitmap, outPath);  
		return bitmap;
    }  
      
    /** 
     * Ratio and generate thumb to the path specified 
     *  
     * @param image 
     * @param outPath 
     * @param pixelW target pixel of width 
     * @param pixelH target pixel of height 
     * @param needsDelete Whether delete original file after compress 
     * @throws FileNotFoundException 
     */  
    public void ratioAndGenThumb(String imgPath, String outPath, float pixelW, float pixelH, boolean needsDelete) throws FileNotFoundException {  
        Bitmap bitmap = ratio(imgPath, pixelW, pixelH);  
        storeImage( bitmap, outPath);  
          
        // Delete original file  
                if (needsDelete) {  
                    File file = new File (imgPath);  
                    if (file.exists()) {  
                        file.delete();  
                    }  
                }  
    }  
      
}  