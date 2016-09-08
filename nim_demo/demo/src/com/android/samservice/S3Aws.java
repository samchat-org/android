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


public class S3Aws {
    private static String AWS_ACCESS_KEY = "AKIAOGLPJB2XJV3WPYPA";
    private static String AWS_SECRET_KEY = "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";
    static final String bucketName = "samchat";

    private AmazonS3 s3;
    private TransferManager tx;

	public S3Aws(){

	}

    public void init_with_key() throws Exception {
        AWSCredentials credentials = null;
        credentials = new BasicAWSCredentials(AWS_ACCESS_KEY, AWS_SECRET_KEY);
        s3 = new AmazonS3Client(credentials);
        //Region usWest2 = Region.getRegion(Regions.CN_NORTH_1);
        //s3.setRegion(usWest2);
        tx = new TransferManager(s3);
    }

    public boolean isObjectExit(String key) {
        int len = key.length();
        ObjectListing objectListing = s3.listObjects(bucketName);
        String s = new String();
        for(S3ObjectSummary objectSummary : objectListing.getObjectSummaries()) {
            s = objectSummary.getKey();
            int slen = s.length();
            if(len == slen) {
                int i;
                for(i=0;i<len;i++) if(s.charAt(i) != key.charAt(i)) break;
                if(i == len) return true;
            }
        }
        return false;
    }

    public void createSampleFile(String filename) throws IOException {
        if(isObjectExit(filename) == true) {
            System.out.println(filename +" already exists in " + bucketName + "!");
            return;
        }
        System.out.println("creating file " + filename);
        File file = new File(filename);
        file.deleteOnExit();

        Writer writer = new OutputStreamWriter(new FileOutputStream(file));
        writer.write("abcdefghijklmnopqrstuvwxyz\n");
        writer.write("01234567890112345678901234\n");
        writer.write("!@#$%^&*()-=[]{};':',.<>/?\n");
        writer.write("01234567890112345678901234\n");
        writer.write("abcdefghijklmnopqrstuvwxyz\n");
        writer.close();

        s3.putObject(bucketName, filename, file);
        System.out.println("create sample file " + filename + " succeed!");
    }

    public void showContentOfAnObject(String key) {
        S3Object object = s3.getObject(new GetObjectRequest(bucketName, key));
        InputStream input = object.getObjectContent();
        BufferedReader reader = new BufferedReader(new InputStreamReader(input));
        try {
            while (true) {
                String line = reader.readLine();
                if (line == null) break;
                System.out.println("    " + line);
            }
            System.out.println();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void showContentOfAnGzipObject(String key) {
        try {
            S3Object object = s3.getObject(new GetObjectRequest(bucketName, key));
            InputStream input = new GZIPInputStream(object.getObjectContent());
            BufferedReader reader = new BufferedReader(new InputStreamReader(input));

            while (true) {
                String line = reader.readLine();
                if (line == null) break;
                System.out.println("    " + line);
            }
            System.out.println();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void listBuckets() {
        System.out.println("Listing buckets");
        int bucketNum = 0;
        for(Bucket bucket : s3.listBuckets()) {
            System.out.println(" - " + bucket.getName());
            bucketNum ++;
        }
        System.out.println("total " + bucketNum + " bucket(s).");
    }

    public void deleteBucket(String bucketName) {
        if(s3.doesBucketExist(bucketName) == false) {
            System.out.println(bucketName + " does not exists!");
            return;
        }
        System.out.println("deleting " + bucketName + " ...");
        ObjectListing objectListing = s3.listObjects(bucketName);
        for(S3ObjectSummary objectSummary : objectListing.getObjectSummaries()) {
            String key = objectSummary.getKey();
            s3.deleteObject(bucketName, key);
        }
        s3.deleteBucket(bucketName);
        System.out.println(bucketName + " has been deleted!");
    }

    public void deleteObjectsWithPrefix(String bucketName, String prefix) {
        if(s3.doesBucketExist(bucketName) == false) {
            System.out.println(bucketName + " does not exists!");
            return;
        }
        System.out.println("deleting " + prefix +"* in " + bucketName + " ...");
        int pre_len = prefix.length();
        ObjectListing objectListing = s3.listObjects(bucketName);
        for(S3ObjectSummary objectSummary : objectListing.getObjectSummaries()) {
            String key = objectSummary.getKey();
            int len = key.length();
            if(len < pre_len) continue;
            int i;
            for(i=0;i<pre_len;i++)
                if(key.charAt(i) != prefix.charAt(i))
                    break;
            if(i < pre_len) continue;
            s3.deleteObject(bucketName, key);
        }
        System.out.println("All " + prefix + "* deleted!");
    }

    public void uploadFileToBucket(String path) {
        File fileToUpload = new File(path);
        if(fileToUpload.exists() == false) {
            System.out.println(path + " not exists!");
            return;
        }
        PutObjectRequest request = new PutObjectRequest(bucketName, fileToUpload.getName(), fileToUpload);
        Upload upload = tx.upload(request);
        while((int)upload.getProgress().getPercentTransferred() < 100) {
            try {
                Thread.sleep(1000);
            } catch (InterruptedException e) {
                // TODO Auto-generated catch block
                e.printStackTrace();
            }
        }
        System.out.println(path + " upload succeed!");
    }

    public void createFolder(String folderName) {
        // Create metadata for my folder & set content-length to 0
        ObjectMetadata metadata = new ObjectMetadata();
        metadata.setContentLength(0);
        // Create empty content
        InputStream emptyContent = new ByteArrayInputStream(new byte[0]);
        // Create a PutObjectRequest passing the foldername suffixed by /
        PutObjectRequest putObjectRequest =
                new PutObjectRequest(bucketName, folderName+"/",
                        emptyContent, metadata);
        // Send request to S3 to create folder
        s3.putObject(putObjectRequest);
    }
}
