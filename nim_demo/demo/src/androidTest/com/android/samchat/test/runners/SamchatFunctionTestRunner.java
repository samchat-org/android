package com.android.samchat.test.runners;

import android.test.InstrumentationTestRunner;
import android.test.InstrumentationTestSuite;
import android.test.suitebuilder.TestMethod;
import android.test.suitebuilder.annotation.SmallTest;

import com.android.samchat.test.testcase.TestCase_Create_1000SP;
import com.android.samchat.test.testcase.TestCase_PreCondition_Setup_000001;

import junit.framework.TestSuite;

import java.lang.reflect.InvocationTargetException;

@SmallTest
public class SamchatFunctionTestRunner extends InstrumentationTestRunner {
	@Override
	public TestSuite getAllTests(){
		TestSuite suite = new InstrumentationTestSuite(this);

		/*try {
            suite.addTest((new TestMethod("testLogin",TestCase_Login_Test.class)).createTest());
        } catch (InvocationTargetException e) {
            e.printStackTrace();
        } catch (IllegalAccessException e) {
            e.printStackTrace();
        } catch (InstantiationException e) {
            e.printStackTrace();
        }*/
		suite.addTestSuite(TestCase_PreCondition_Setup_000001.class);
		suite.addTestSuite(TestCase_Create_1000SP.class);
		return suite;
	}
}