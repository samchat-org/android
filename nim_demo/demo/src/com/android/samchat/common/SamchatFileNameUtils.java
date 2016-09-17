package com.android.samchat.common;

import android.os.Environment;

import com.android.samservice.SamService;
import com.netease.nim.demo.DemoCache;
import com.netease.nim.uikit.NimUIKit;
import com.netease.nim.uikit.common.util.file.FileUtil;
import com.netease.nim.uikit.common.util.storage.StorageType;
import com.netease.nim.uikit.common.util.storage.StorageUtil;
import com.netease.nim.uikit.common.util.string.StringUtil;
import com.netease.nim.uikit.common.util.sys.TimeUtil;

public class SamchatFileNameUtils{
	public static String getS3FileNameOfOrigin(String originPath){
		String extension = FileUtil.getExtensionName(originPath);
		String account = SamService.getInstance().get_current_user().getAccount();
		if(extension.equals("")){
			return "orig_" + account + "_" + TimeUtil.currentTimeMillis();
		}else{
			return "orig_" + account + "_" + TimeUtil.currentTimeMillis() + "." + extension;
		}
	}

	public static String getTempFileName(String absPath, String s3nameOrig){
		String extension = FileUtil.getExtensionName(absPath);
		String MD5 = StringUtil.makeMd5(s3nameOrig);

		if(extension.equals("")){
			return StorageUtil.getWritePath(NimUIKit.getContext(),"temp_image_"+MD5, StorageType.TYPE_TEMP);
		}else{
			return StorageUtil.getWritePath(NimUIKit.getContext(),"temp_image_"+MD5+"."+extension,StorageType.TYPE_TEMP);
		}
	}

	public static String getMD5Path(StorageType type,String path){
		return Environment.getExternalStorageDirectory() + "/" + DemoCache.getContext().getPackageName() + "/nim/"
									+type.getStoragePath()+"/"+StringUtil.makeMd5(path);
	}

}
