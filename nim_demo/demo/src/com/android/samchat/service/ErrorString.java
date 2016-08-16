package com.android.samchat.service;
import com.netease.nim.demo.R;
import com.android.samservice.Constants;
import android.app.Activity;

public class ErrorString{
	public int code;
	public String reminder;
	public String title;

	public ErrorString(Activity activity, int ret){
		switch(ret){
			case Constants.RET_HTTP_PARSED_FAILED_ERROR:
			case Constants.RET_ACTION_NOT_SUPPORT_ERROR:
			case Constants.RET_PARAM_NOT_SUPPORT_ERROR:
			case Constants.RET_TOKEN_FORMAT_ERROR:
				reminder = activity.getString(R.string.samchat_internal_error);
				break;
			case Constants.RET_SERVER_INTERNAL_ERROR:
				reminder = activity.getString(R.string.samchat_server_error);
				break;
			case Constants.RET_NUMBER_ALREADY_REGISTERED_ERROR:
				reminder = activity.getString(R.string.samchat_cellphone_registered_error);
				break;
			case Constants.RET_NUMBER_ILLEAGE_ERROR:
				reminder = activity.getString(R.string.samchat_illeage_cellphone_registered_error);
				break;
			case Constants.RET_PASSWORD_ERROR:
				reminder = activity.getString(R.string.samchat_pwd_error);
				break;
			case Constants.RET_NUMBER_NOT_REGISTERED_ERROR:
				reminder = activity.getString(R.string.samchat_cellphone_not_register_error);
				break;
			case Constants.RET_VERIFY_CODE_ERROR:
				reminder = activity.getString(R.string.samchat_verifycode_error);
				break;
			case Constants.RET_REQUEST_VERIFY_CODE_TOO_OFTEN:
				reminder = activity.getString(R.string.samchat_verifycode_request_too_offten_error);
				break;
			case Constants.RET_USER_NOT_EXISTED:
				reminder = activity.getString(R.string.samchat_user_not_existed_error);
				break;
			case Constants.RET_PASSWORD_ERROR_TOO_OFTEN:
				reminder = activity.getString(R.string.samchat_pwd_try_too_offten_error);
				break;
			case Constants.RET_QUERY_USER_TOO_OFTEN:
				reminder = activity.getString(R.string.samchat_query_too_offten_error);
				break;
			case Constants.RET_SEND_INVITE_MSG_TOO_OFTEN:
				reminder = activity.getString(R.string.samchat_send_invite_too_offten_error);
				break;
			case Constants.RET_VERIFY_CODE_EXPIRATION:
				reminder = activity.getString(R.string.samchat_verify_code_expiration_error);
				break;
			case Constants.RET_TOKEN_ILLEAGE_ERROR:
				reminder = activity.getString(R.string.samchat_token_error);
				break;
			case Constants.RET_ALREADY_UPGRADE_ERROR:
				reminder = activity.getString(R.string.samchat_upgrade_error);
				break;
			case Constants.RET_ORGINAL_PASSWORD_ERROR:
				reminder = activity.getString(R.string.samchat_original_pwd_error);
				break;
			case Constants.RET_PUBLIC_NOT_EXISTED_ERROR:
				reminder = activity.getString(R.string.samchat_not_sp_error);
				break;
			case Constants.RET_ADV_NOT_EXISTED_ERROR:
				reminder = activity.getString(R.string.samchat_adv_not_existed_error);
				break;
			case Constants.RET_WAIT_FOR_AUDIT_ERROR:
				reminder = activity.getString(R.string.samchat_wait_for_audit_error);
				break;
			case Constants.RET_FOLLOW_MAXIUM_ERROR:
				reminder = activity.getString(R.string.samchat_follow_maxium_number_error);
				break;
			case Constants.RET_HAVE_NOT_FOLLOW_ERROR:
				reminder = activity.getString(R.string.samchat_not_follow_error);
				break;
			case Constants.RET_HAVE_NOT_ADD_CONTACT_ERROR:
				reminder = activity.getString(R.string.samchat_not_add_contact_error);
				break;
			default:
				reminder = activity.getString(R.string.samchat_default_error);
				break;
		}
	}
}