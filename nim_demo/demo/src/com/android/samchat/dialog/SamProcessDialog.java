package com.android.samchat.dialog;

import android.app.Activity;
import android.app.ProgressDialog;

public class SamProcessDialog {
	private ProgressDialog mProgressDialog;

	public SamProcessDialog(Activity activity_owner){
		mProgressDialog = new ProgressDialog(activity_owner);
	}
	
	public void launchProcessDialog (Activity activity_owner,String msg)
	{
		// ����mProgressDialog���
		mProgressDialog.setProgress(ProgressDialog.STYLE_SPINNER);//Բ��
		mProgressDialog.setProgress(ProgressDialog.STYLE_HORIZONTAL);//ˮƽ
		// ����mProgressDialog����
		//mProgressDialog.setTitle("title");
		// ����mProgressDialog��ʾ
		mProgressDialog.setMessage(msg);
		// ����mProgressDialog��������ͼ��
		//mProgressDialog.setIcon(R.drawable.dialog_warning); 
		// ����mProgressDialog�Ľ������Ƿ���ȷ
		//������ʱ����ǰֵ����С�����ֵ֮���ƶ���һ���ڽ���һЩ�޷�ȷ������ʱ�������ʱ��Ϊ��ʾ����ȷʱ���Ǹ�����Ľ��ȿ����������ڵĽ���ֵ
		mProgressDialog.setIndeterminate(false);
		//mProgressDialog.setProgress(m_count++);
		// �Ƿ���԰����˼�ȡ��
		mProgressDialog.setCancelable(false);
		// ����mProgressDialog��һ��Button
		mProgressDialog.show();
	}
	
	public void dismissPrgoressDiglog()
	{
		mProgressDialog.dismiss();
	}
}
