package com.android.samservice.utils;

/*
 * Copyright 2015 Amazon.com, Inc. or its affiliates. All Rights Reserved.
 *
 * Licensed under the Apache License, Version 2.0 (the "License").
 * You may not use this file except in compliance with the License.
 * A copy of the License is located at
 *
 *  http://aws.amazon.com/apache2.0
 *
 * or in the "license" file accompanying this file. This file is distributed
 * on an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either
 * express or implied. See the License for the specific language governing
 * permissions and limitations under the License.
 */

import android.content.Context;
import android.net.Uri;

import com.amazonaws.ClientConfiguration;
import com.amazonaws.auth.CognitoCachingCredentialsProvider;
import com.amazonaws.mobileconnectors.s3.transferutility.TransferObserver;
import com.amazonaws.mobileconnectors.s3.transferutility.TransferUtility;
import com.amazonaws.regions.Regions;
import com.amazonaws.services.s3.AmazonS3Client;
import com.amazonaws.services.s3.S3ClientOptions;
import com.netease.nim.uikit.NimConstants;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.Map;
import java.util.UUID;

/*
 * Handles basic helper functions used throughout the app.
 */
public class S3Util {

    // We only need one instance of the clients and credentials provider
    private static AmazonS3Client sS3Client;
    private static CognitoCachingCredentialsProvider sCredProvider;
    private static TransferUtility sTransferUtility;

    /**
     * Gets an instance of CognitoCachingCredentialsProvider which is
     * constructed using the given Context.
     *
     * @param context An Context instance.
     * @return A default credential provider.
     */
    private static CognitoCachingCredentialsProvider getCredProvider(Context context) {
        if (sCredProvider == null) {
            sCredProvider = new CognitoCachingCredentialsProvider(
                    context.getApplicationContext(),
                    "us-west-2:2d22de2e-5125-4d48-9dec-28367ebeda89", // Identity Pool ID
                    Regions.US_WEST_2 );
        }
        return sCredProvider;
    }

    /**
     * Gets an instance of a S3 client which is constructed using the given
     * Context.
     *
     * @param context An Context instance.
     * @return A default S3 client.
     */
    public static AmazonS3Client getS3Client(Context context) {
        if (sS3Client == null) {
            ClientConfiguration config = new ClientConfiguration();
            config.setConnectionTimeout(100000);
            //config.setMaxConnections(500);
            config.setSocketTimeout(100000);
            config.setMaxErrorRetry(500);
            sS3Client = new AmazonS3Client(getCredProvider(context.getApplicationContext()),config);
            sS3Client.setEndpoint(NimConstants.S3_ENDPOINT);
        }
        return sS3Client;
    }

    /**
     * Gets an instance of the TransferUtility which is constructed using the
     * given Context
     *
     * @param context
     * @return a TransferUtility instance
     */
    public static TransferUtility getTransferUtility(Context context) {
        if (sTransferUtility == null) {
            sTransferUtility = new TransferUtility(getS3Client(context.getApplicationContext()),
                    context.getApplicationContext());
        }

        return sTransferUtility;
    }

    /**
     * Converts number of bytes into proper scale.
     *
     * @param bytes number of bytes to be converted.
     * @return A string that represents the bytes in a proper scale.
     */
    public static String getBytesString(long bytes) {
        String[] quantifiers = new String[] {
                "KB", "MB", "GB", "TB"
        };
        double speedNum = bytes;
        for (int i = 0;; i++) {
            if (i >= quantifiers.length) {
                return "";
            }
            speedNum /= 1024;
            if (speedNum < 512) {
                return String.format("%.2f", speedNum) + " " + quantifiers[i];
            }
        }
    }

    /**
     * Copies the data from the passed in Uri, to a new file for use with the
     * Transfer Service
     *
     * @param context
     * @param uri
     * @return
     * @throws IOException
     */
    public static File copyContentUriToFile(Context context, Uri uri) throws IOException {
        InputStream is = context.getContentResolver().openInputStream(uri);
        File copiedData = new File(context.getDir("SampleImagesDir", Context.MODE_PRIVATE), UUID
                .randomUUID().toString());
        copiedData.createNewFile();

        FileOutputStream fos = new FileOutputStream(copiedData);
        byte[] buf = new byte[2046];
        int read = -1;
        while ((read = is.read(buf)) != -1) {
            fos.write(buf, 0, read);
        }

        fos.flush();
        fos.close();

        return copiedData;
    }

    /*
     * Fills in the map with information in the observer so that it can be used
     * with a SimpleAdapter to populate the UI
     */
    public static void fillMap(Map<String, Object> map, TransferObserver observer, boolean isChecked) {
        int progress = (int) ((double) observer.getBytesTransferred() * 100 / observer
                .getBytesTotal());
        map.put("id", observer.getId());
        map.put("checked", isChecked);
        map.put("fileName", observer.getAbsoluteFilePath());
        map.put("progress", progress);
        map.put("bytes",
                getBytesString(observer.getBytesTransferred()) + "/"
                        + getBytesString(observer.getBytesTotal()));
        map.put("state", observer.getState());
        map.put("percentage", progress + "%");
    }
}

