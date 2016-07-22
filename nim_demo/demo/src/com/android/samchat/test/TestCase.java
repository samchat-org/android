package com.android.samchat.test;

import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.AdapterView.OnItemLongClickListener;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;
import com.netease.nim.uikit.common.fragment.TFragment;
import java.util.ArrayList;
import java.util.List;
import com.netease.nim.demo.R;
import com.android.samchat.callback.SendQuestionCallback;
import android.widget.LinearLayout;
import com.android.samservice.info.SendQuestion;
import com.android.samchat.adapter.ReceivedQuestionAdapter;
import com.android.samservice.info.ReceivedQuestion;
import com.android.samchat.callback.ReceivedQuestionCallback;
import com.netease.nim.uikit.common.activity.UI;
import com.android.samservice.SamService;
import com.android.samchat.adapter.SendQuestionAdapter;
import com.android.samchat.SamchatGlobal;
import java.util.Collections;
import java.util.Comparator;
import com.netease.nim.uikit.common.util.log.LogUtil;
import com.android.samservice.Constants;

/**
 * Main Fragment in SamchatRequestListFragment
 */
public class TestCase{
	static public void testInitDB(){
	//SendQuestion DB
		if(TestPreferences.getUserTest() == null || Long.valueOf(TestPreferences.getUserTest())%3 == 0){
			SamService.getInstance().getDao().delete_SendQuestion_db_ALL();
			SendQuestion question1 = new SendQuestion(1L, "test question 1", System.currentTimeMillis(),"Bay Area");
			SendQuestion question2 = new SendQuestion(2L, "test question 2", System.currentTimeMillis()-1000L,"Bay Area");
			SendQuestion question3 = new SendQuestion(3L, "test question 3", System.currentTimeMillis()-2000L,"Bay Area");
			SendQuestion question4 = new SendQuestion(4L, "test question 4", System.currentTimeMillis()-3000L,"Bay Area");
		
			SamService.getInstance().getDao().update_SendQuestion_db(question1);
			SamService.getInstance().getDao().update_SendQuestion_db(question2);
			SamService.getInstance().getDao().update_SendQuestion_db(question3);
			SamService.getInstance().getDao().update_SendQuestion_db(question4);
		}else if(Long.valueOf(TestPreferences.getUserTest())%3 == 1){
			SamService.getInstance().getDao().delete_SendQuestion_db_ALL();
			SendQuestion question1 = new SendQuestion(1L, "test question 1", System.currentTimeMillis()-8*24*60*60*1000L,"Bay Area");
			SendQuestion question2 = new SendQuestion(2L, "test question 2", System.currentTimeMillis()-8*24*60*60*1000L,"Bay Area");
			SendQuestion question3 = new SendQuestion(3L, "test question 3", System.currentTimeMillis()-8*24*60*60*1000L,"Bay Area");
			SendQuestion question4 = new SendQuestion(4L, "test question 4", System.currentTimeMillis()-9*24*60*60*1000L,"Bay Area");
		
			SamService.getInstance().getDao().update_SendQuestion_db(question1);
			SamService.getInstance().getDao().update_SendQuestion_db(question2);
			SamService.getInstance().getDao().update_SendQuestion_db(question3);
			SamService.getInstance().getDao().update_SendQuestion_db(question4);
		}else{
			SamService.getInstance().getDao().delete_SendQuestion_db_ALL();
			SendQuestion question1 = new SendQuestion(1L, "test question 1", System.currentTimeMillis(),"Bay Area");
			SendQuestion question2 = new SendQuestion(2L, "test question 2", System.currentTimeMillis()-1000L,"Bay Area");
			SendQuestion question3 = new SendQuestion(3L, "test question 3", System.currentTimeMillis()-10*24*60*60*1000L,"Bay Area");
			SendQuestion question4 = new SendQuestion(4L, "test question 4", System.currentTimeMillis()-2000L,"Bay Area");
			SendQuestion question5 = new SendQuestion(5L, "test question 5", System.currentTimeMillis()-3000L,"Bay Area");
			SendQuestion question6 = new SendQuestion(6L, "test question 6", System.currentTimeMillis()-9*24*60*60*1000L,"Bay Area");
			SamService.getInstance().getDao().update_SendQuestion_db(question1);
			SamService.getInstance().getDao().update_SendQuestion_db(question2);
			SamService.getInstance().getDao().update_SendQuestion_db(question3);
			SamService.getInstance().getDao().update_SendQuestion_db(question4);
			SamService.getInstance().getDao().update_SendQuestion_db(question5);
			SamService.getInstance().getDao().update_SendQuestion_db(question6);
		}

		//ReceivedQuestion DB
		if(TestPreferences.getUserTest() == null || Long.valueOf(TestPreferences.getUserTest())%3 == 0){
			SamService.getInstance().getDao().delete_ReceivedQuestion_db_All();
			ReceivedQuestion rquestion1 = new ReceivedQuestion(1L, 1000L, "received test question 1", System.currentTimeMillis(),"Bay Area");
			ReceivedQuestion rquestion2 = new ReceivedQuestion(2L, 1000L, "received test question 2", System.currentTimeMillis()-1000L,"Bay Area");
			ReceivedQuestion rquestion3 = new ReceivedQuestion(3L, 3000L, "received test question 3", System.currentTimeMillis()-2000L,"Bay Area");
			ReceivedQuestion rquestion4 = new ReceivedQuestion(4L, 4000L, "received test question 4", System.currentTimeMillis()-3000L,"Bay Area");
			ReceivedQuestion rquestion5 = new ReceivedQuestion(5L, 4000L, "received test question 5", System.currentTimeMillis()-8*24*60*60*1000L,"Bay Area");
		
			SamService.getInstance().getDao().update_ReceivedQuestion_db(rquestion1);
			SamService.getInstance().getDao().update_ReceivedQuestion_db(rquestion2);
			SamService.getInstance().getDao().update_ReceivedQuestion_db(rquestion3);
			SamService.getInstance().getDao().update_ReceivedQuestion_db(rquestion4);
			SamService.getInstance().getDao().update_ReceivedQuestion_db(rquestion5);
		}else if(Long.valueOf(TestPreferences.getUserTest())%3 == 1){
			SamService.getInstance().getDao().delete_ReceivedQuestion_db_All();
			ReceivedQuestion rquestion1 = new ReceivedQuestion(1L, 1000L, "received test question 1", System.currentTimeMillis(),"Bay Area");
			rquestion1.setstatus(Constants.QUESTION_RESPONSED);
			ReceivedQuestion rquestion2 = new ReceivedQuestion(2L, 1000L, "received test question 2", System.currentTimeMillis()-1000L,"Bay Area");
			rquestion2.setstatus(Constants.QUESTION_RESPONSED);
			ReceivedQuestion rquestion3 = new ReceivedQuestion(3L, 3000L, "received test question 3", System.currentTimeMillis()-2000L,"Bay Area");
			rquestion3.setstatus(Constants.QUESTION_RESPONSED);
			ReceivedQuestion rquestion4 = new ReceivedQuestion(4L, 4000L, "received test question 4", System.currentTimeMillis()-3000L,"Bay Area");
			rquestion4.setstatus(Constants.QUESTION_RESPONSED);
			ReceivedQuestion rquestion5 = new ReceivedQuestion(5L, 4000L, "received test question 5", System.currentTimeMillis()-8*24*60*60*1000L,"Bay Area");
			rquestion5.setstatus(Constants.QUESTION_RESPONSED);
		
			SamService.getInstance().getDao().update_ReceivedQuestion_db(rquestion1);
			SamService.getInstance().getDao().update_ReceivedQuestion_db(rquestion2);
			SamService.getInstance().getDao().update_ReceivedQuestion_db(rquestion3);
			SamService.getInstance().getDao().update_ReceivedQuestion_db(rquestion4);
			SamService.getInstance().getDao().update_ReceivedQuestion_db(rquestion5);
		}else{
			SamService.getInstance().getDao().delete_ReceivedQuestion_db_All();
			ReceivedQuestion rquestion1 = new ReceivedQuestion(1L, 1000L, "received test question 1", System.currentTimeMillis(),"Bay Area");
			ReceivedQuestion rquestion2 = new ReceivedQuestion(2L, 1000L, "received test question 2", System.currentTimeMillis()-1000L,"Bay Area");
			rquestion2.setstatus(Constants.QUESTION_RESPONSED);
			ReceivedQuestion rquestion3 = new ReceivedQuestion(3L, 3000L, "received test question 3", System.currentTimeMillis()-2000L,"Bay Area");
			ReceivedQuestion rquestion4 = new ReceivedQuestion(4L, 4000L, "received test question 4", System.currentTimeMillis()-3000L,"Bay Area");
			rquestion4.setstatus(Constants.QUESTION_RESPONSED);
			ReceivedQuestion rquestion5 = new ReceivedQuestion(5L, 4000L, "received test question 5", System.currentTimeMillis()-8*24*60*60*1000L,"Bay Area");
		
			SamService.getInstance().getDao().update_ReceivedQuestion_db(rquestion1);
			SamService.getInstance().getDao().update_ReceivedQuestion_db(rquestion2);
			SamService.getInstance().getDao().update_ReceivedQuestion_db(rquestion3);
			SamService.getInstance().getDao().update_ReceivedQuestion_db(rquestion4);
			SamService.getInstance().getDao().update_ReceivedQuestion_db(rquestion5);
		}

		if(TestPreferences.getUserTest() == null){
			TestPreferences.saveUserTest("1");
		}else{
			TestPreferences.saveUserTest((Long.valueOf(TestPreferences.getUserTest())+1)+"");
		}
	}
}


