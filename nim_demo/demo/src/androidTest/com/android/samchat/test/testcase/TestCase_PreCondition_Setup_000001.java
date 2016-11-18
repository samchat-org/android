package com.android.samchat.test.testcase;

import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.SystemClock;
import android.test.InstrumentationTestCase;
import android.util.Log;
import android.widget.EditText;
import android.widget.TextView;

import com.android.samchat.R;
import com.android.samchat.activity.SamchatLoginActivity;
import com.android.samservice.Constants;
import com.android.samservice.SamService;
import com.android.samservice.info.Contact;
import com.android.samservice.info.ContactUser;
import com.netease.nim.demo.config.preference.Preferences;
import com.netease.nim.uikit.common.util.file.AttachmentStore;
import com.netease.nim.uikit.common.util.log.LogUtil;
import com.netease.nim.uikit.common.util.storage.StorageType;
import com.netease.nim.uikit.common.util.storage.StorageUtil;

/**
 * Created by dkw1225 on 10/18/16.
 */
public class TestCase_PreCondition_Setup_000001 extends InstrumentationTestCase {
    private SamchatLoginActivity loginActivity;
    private EditText logininput_et;
    private EditText password_et;
    private TextView signin_tv;

    @Override
	protected void setUp() {
        try {
            super.setUp();
        } catch (Exception e) {
            e.printStackTrace();
        }
        Intent intent = new Intent();
        //System.out.println("---------------------------------------------------");
        intent.setClassName("com.android.samchat", SamchatLoginActivity.class.getName());
        Log.e("samchatTest","launch login activity");
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);

        loginActivity = (SamchatLoginActivity) getInstrumentation().startActivitySync(intent);
        logininput_et = (EditText) loginActivity.findViewById(R.id.logininput);
        password_et = (EditText) loginActivity.findViewById(R.id.password);
        signin_tv = (TextView) loginActivity.findViewById(R.id.signin);
	}

    /*
     * 垃圾清理与资源回收 ,测试用例完成时调用
     *
     */
    @Override
    protected void tearDown() {
        try {
            super.tearDown();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private class PerformClick implements Runnable {
        TextView btn;
        private PerformClick(TextView button) {
            btn = button;
        }

        public void run() {
            btn.performClick();
        }
    }

	private void setupEnviroment(){
		//db setup: 1000 customers
		LogUtil.i(TestCaseConstants.TAG, "setupEnviroment start");
		Bitmap avatar = BitmapFactory.decodeResource(getInstrumentation().getContext().getResources(), TestCaseConstants.resid_avatar_default);
		for(int i=0;i<TestCaseConstants.countOfCustomer;i++){
			ContactUser user = new ContactUser();
			user.setunique_id(i+10000L);
			user.setusername("test"+(10000L+i));
			user.setusertype(Constants.USER);
			user.setlastupdate((10000L+i));
			AttachmentStore.saveBitmap(avatar, StorageUtil.getWritePath("avatar_"+(10000L+i), StorageType.TYPE_IMAGE), false);
			user.setavatar("file://"+StorageUtil.getWritePath("avatar_"+(10000L+i),StorageType.TYPE_IMAGE));
			user.setavatar_original("file://"+StorageUtil.getWritePath("avatar_"+(10000L+i),StorageType.TYPE_IMAGE));
			user.setcountrycode("1");
			user.setcellphone("139117"+(10000L+i));
			SamService.getInstance().getDao().update_ContactUser_db(user);

			Contact customer = new Contact(i+10000L,"test"+(10000L+i),user.getavatar());
			SamService.getInstance().getDao().update_ContactList_db(customer,true);
		}
		avatar.recycle();
		LogUtil.i(TestCaseConstants.TAG, "setupEnviroment finished");
	}
	
    public void testLogin() throws Exception {
		LogUtil.i(TestCaseConstants.TAG, "test the Activity");
		try{
			runTestOnUiThread(new Runnable() { // THIS IS THE KEY TO SUCCESS
				@Override
				public void run() {
					logininput_et.setText(TestCaseConstants.testAccount);
					password_et.setText(TestCaseConstants.testPwd);
				}
			});
		}catch (Throwable e){
			e.printStackTrace();
		}
		SystemClock.sleep(5000);
		getInstrumentation().runOnMainSync(new PerformClick(signin_tv));
		SystemClock.sleep(5000);
		setupEnviroment();
        //assertEquals(test_username, logininput_et.getText().toString());
	}

}