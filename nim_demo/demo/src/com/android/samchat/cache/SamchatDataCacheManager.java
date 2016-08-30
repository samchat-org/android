package com.android.samchat.cache;

import android.content.Context;
import android.os.Handler;

import com.netease.nim.uikit.common.framework.NimSingleThreadExecutor;
import com.netease.nim.uikit.common.util.log.LogUtil;

import java.util.ArrayList;
import java.util.List;

public class SamchatDataCacheManager {

    private static final String TAG = SamchatDataCacheManager.class.getSimpleName();

    public static void buildDataCacheAsync() {
        //buildDataCacheAsync(null, null);
    }

/*    public static void buildDataCacheAsync(final Context context, final Observer<Void> buildCompletedObserver) {
        NimSingleThreadExecutor.getInstance().execute(new Runnable() {
            @Override
            public void run() {
                buildDataCache();

                // callback
                if (context != null && buildCompletedObserver != null) {
                    new Handler(context.getMainLooper()).post(new Runnable() {
                        @Override
                        public void run() {
                            buildCompletedObserver.onEvent(null);
                        }
                    });
                }

                LogUtil.i(TAG, "build data cache completed");
            }
        });
    }
*/

	public static void buildDataCache() {
		// clear
		clearDataCache();

		// build data cache
		SamchatUserInfoCache.getInstance().buildCache();
		ContactDataCache.getInstance().buildCache();
		CustomerDataCache.getInstance().buildCache();
		FollowDataCache.getInstance().buildCache();
		

        // build self avatar cache
        //List<String> accounts = new ArrayList<>(1);
        //accounts.add(NimUIKit.getAccount());
        //NimUIKit.getImageLoaderKit().buildAvatarCache(accounts);
    }

	public static void clearDataCache() {
		FollowDataCache.getInstance().clearCache();
		ContactDataCache.getInstance().clearCache();
		CustomerDataCache.getInstance().clearCache();
		SamchatUserInfoCache.getInstance().clearCache();
	}

}

