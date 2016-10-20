package com.android.samchat.test.testcase;

import android.content.Intent;
import android.os.SystemClock;
import android.test.InstrumentationTestCase;
import android.util.Log;
import android.widget.EditText;
import android.widget.TextView;

import com.android.samchat.R;
import com.android.samchat.activity.SamchatLoginActivity;
import com.netease.nim.demo.config.preference.Preferences;

/**
 * Created by dkw1225 on 10/18/16.
 */
public class TestCase_Create_1000SP extends InstrumentationTestCase {


    @Override
    protected void setUp() {
        try {
            super.setUp();
        } catch (Exception e) {
            e.printStackTrace();
        }
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

    /*
     * 活动功能测试
     */
    public void testCreate1000SP() throws Exception {
        Log.v("samchatTest", "testCreate1000SP");
        
        //SystemClock.sleep(15000);
        //assertEquals(test_username, logininput_et.getText().toString());
    }
}