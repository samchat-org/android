package com.android.samservice;

/**
 * Created by kevin on 2016/9/6.
 */
import java.io.BufferedReader;
import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.io.Writer;
import java.util.zip.GZIPInputStream;

import com.amazonaws.AmazonClientException;
import com.amazonaws.auth.AWSCredentials;
import com.amazonaws.auth.BasicAWSCredentials;
import com.amazonaws.auth.profile.ProfileCredentialsProvider;
import com.amazonaws.regions.Region;
import com.amazonaws.regions.Regions;
import com.amazonaws.services.s3.AmazonS3;
import com.amazonaws.services.s3.AmazonS3Client;
import com.amazonaws.services.s3.model.Bucket;
import com.amazonaws.services.s3.model.GetObjectRequest;
import com.amazonaws.services.s3.model.ObjectListing;
import com.amazonaws.services.s3.model.ObjectMetadata;
import com.amazonaws.services.s3.model.PutObjectRequest;
import com.amazonaws.services.s3.model.S3Object;
import com.amazonaws.services.s3.model.S3ObjectSummary;
import com.amazonaws.services.s3.transfer.TransferManager;
import com.amazonaws.services.s3.transfer.Upload;
import com.netease.nim.uikit.S3Callback;


public class S3AwsClient{
	public static final String bucket_name = "";
	public S3Aws s3;
	
	public S3AwsClient(){

	}

    public boolean init(){
        try{
            s3 = new S3Aws();
            s3.init_with_key();
            return true;
        }catch(Exception e){
            return false;
        }
    }

	public void uploadAdvertisement(String folder, String filename, String abspath, S3Callback callback){
		try{
			callback.onResult(0);
		}catch(Exception e){
			callback.onResult(-1);
		}
	}

	

}

