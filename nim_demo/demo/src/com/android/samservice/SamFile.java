package com.android.samservice;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;

import org.apache.http.util.EncodingUtils;

public class SamFile {
	public SamFile(){

	}
	
	public String readSamFile(String path,String fileName) throws IOException {
        File filePath = new File(path);

       if(!filePath.exists()){
           return null;
       }
		
        File file = new File(path + "/" + fileName);  
	 if(!file.exists()){
		return null;
	 }
        FileInputStream fis = new FileInputStream(file);    
        String res;
        int length = fis.available();   

	 if(length == 0){
		fis.close();   
		return null;
	 }
	 
         byte [] buffer = new byte[length];   
         fis.read(buffer);       
  
         res = EncodingUtils.getString(buffer, "UTF-8");   
  
         fis.close();       
         return res;    
}    
  

    public void writeSamFile(String path, String fileName, String write_str) throws IOException{    

       File filePath = new File(path);

       if(!filePath.exists()){
            filePath.mkdirs();
       }

        File file = new File(path + "/" + fileName);

	 if(!file.exists()){
		file.createNewFile();
	 }
  
        FileOutputStream fos = new FileOutputStream(file);    
  
        byte [] bytes = write_str.getBytes("UTF-8");   
  
        fos.write(bytes);   
  
        fos.close();   
    }
    
    public boolean isSamFileEmpty( File filex) throws IOException {
        FileInputStream fis = new FileInputStream(filex);    
        int length = fis.available(); 
        fis.close();   
        if(length!=0){
        	return false;
        }else{
        	return true;
        }
    }

	public boolean isSamFileEmpty(String path, String file) throws IOException{
		if(file==null || path == null){
			return false;
		}
		
		String file_path = path +"/"+file;
		
		File filex = new File(file);

		if (!filex.exists()) {
			return false;
		}else{
			FileInputStream fis = new FileInputStream(filex);    
			int length = fis.available(); 
			fis.close();   
			if(length!=0){
				return false;
			}else{
				return true;
			}
		}
	}
}
