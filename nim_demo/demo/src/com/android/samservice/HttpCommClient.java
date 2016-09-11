package com.android.samservice;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.util.LinkedList;
import java.util.List;

import org.apache.http.Header;
import org.apache.http.HttpResponse;
import org.apache.http.HttpStatus;
import org.apache.http.NameValuePair;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.client.utils.URLEncodedUtils;
import org.apache.http.client.entity.*;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.params.BasicHttpParams;
import org.apache.http.params.HttpConnectionParams;
import org.apache.http.params.HttpParams;
import org.apache.http.protocol.HTTP;
import org.apache.http.util.EntityUtils;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.apache.http.message.BasicNameValuePair;

import com.android.samchat.SamVendorInfo;
import com.android.samservice.info.Advertisement;
import com.android.samservice.info.Contact;
import com.android.samservice.info.ContactUser;
import com.android.samservice.info.FollowedSamPros;
import com.android.samservice.info.MultipleContact;
import com.android.samservice.info.MultipleFollowUser;
import com.android.samservice.info.MultipleSyncAdv;
import com.android.samservice.info.MultipleUserProfile;
import com.android.samservice.info.MultiplePlacesInfo;
import com.android.samservice.info.PlacesInfo;
import com.android.samservice.info.ReceivedQuestion;
import com.android.samservice.info.ContactUser;
import com.netease.nim.uikit.common.util.log.LogUtil;

public class HttpCommClient {
	public static final String TAG="HttpCommClient";
	
	public static final String ROOT_URL = "http://139.129.57.77/sw/";
	public static final String URL = "http://139.129.57.77/sw/api/1.0/UserAPI";
	public static final String URL_QUESTION = "http://139.129.57.77/sw/api/1.0/QuestionAPI";
	public static final String PUSH_URL = "http://139.129.57.77/sw/push";
	public static final String URL_AVATAR = "http://139.129.57.77/sw/api/1.0/UserAvatarAPI";
	public static final String URL_ATICLE = "http://139.129.57.77/sw/api/1.0/ArticleApi";
	public static final String URL_HOTTOPIC = "http://139.129.57.77/sw/api_1.0_HotTopicAPI.do";
	public static final String URL_VENDOR_WEB = "http://139.129.57.77/sw/skservicer/setting/info";


	public static final String URL_registerCodeRequest = "http://ec2-54-222-170-218.cn-north-1.compute.amazonaws.com.cn:8081/sam_svr/api_1.0_user_registerCodeRequest.do";
	public static final String URL_registerCodeVerify = "http://ec2-54-222-170-218.cn-north-1.compute.amazonaws.com.cn:8081/sam_svr/api_1.0_user_signupCodeVerify.do";
	public static final String URL_signup="http://ec2-54-222-170-218.cn-north-1.compute.amazonaws.com.cn:8081/sam_svr/api_1.0_user_register.do";
	public static final String URL_signin="http://ec2-54-222-170-218.cn-north-1.compute.amazonaws.com.cn:8081/sam_svr/api_1.0_user_login.do";
	public static final String URL_signout="http://ec2-54-222-170-218.cn-north-1.compute.amazonaws.com.cn:8081/sam_svr/api_1.0_user_logout.do";
	public static final String URL_getappkey="http://ec2-54-222-170-218.cn-north-1.compute.amazonaws.com.cn:8081/sam_svr/api_1.0_profile_appkeyGet.do";
	public static final String URL_createsp="http://ec2-54-222-170-218.cn-north-1.compute.amazonaws.com.cn:8081/sam_svr/api_1.0_user_createSamPros.do";
	public static final String URL_findpwdCodeRequest="http://ec2-54-222-170-218.cn-north-1.compute.amazonaws.com.cn:8081/sam_svr/api_1.0_user_findpwdCodeRequest.do";
	public static final String URL_findpwdCodeVerify="http://ec2-54-222-170-218.cn-north-1.compute.amazonaws.com.cn:8081/sam_svr/api_1.0_user_findpwdCodeVerify.do";
	public static final String URL_findpwdUpdate="http://ec2-54-222-170-218.cn-north-1.compute.amazonaws.com.cn:8081/sam_svr/api_1.0_user_findpwdUpdate.do";
	public static final String URL_pwdUpdate="http://ec2-54-222-170-218.cn-north-1.compute.amazonaws.com.cn:8081/sam_svr/api_1.0_user_pwdUpdate.do";

	public static final String URL_questionSend="http://ec2-54-222-170-218.cn-north-1.compute.amazonaws.com.cn:8081/sam_svr/api_1.0_question_question.do";

	public static final String URL_follow="http://ec2-54-222-170-218.cn-north-1.compute.amazonaws.com.cn:8081/sam_svr/api_1.0_officialAccount_follow.do";
	public static final String URL_block="http://ec2-54-222-170-218.cn-north-1.compute.amazonaws.com.cn:8081/sam_svr/api_1.0_officialAccount_block.do";
	public static final String URL_favourite="http://ec2-54-222-170-218.cn-north-1.compute.amazonaws.com.cn:8081/sam_svr/api_1.0_officialAccount_favourite.do";
	public static final String URL_syncFollowList="http://ec2-54-222-170-218.cn-north-1.compute.amazonaws.com.cn:8081/sam_svr/api_1.0_officialAccount_followListQuery.do";
	public static final String URL_queryPublic="http://ec2-54-222-170-218.cn-north-1.compute.amazonaws.com.cn:8081/sam_svr/api_1.0_officialAccount_publicQuery.do";
	public static final String URL_queryUserFuzzy="http://ec2-54-222-170-218.cn-north-1.compute.amazonaws.com.cn:8081/sam_svr/api_1.0_user_queryFuzzy.do";
	public static final String URL_queryUserPrecise="http://ec2-54-222-170-218.cn-north-1.compute.amazonaws.com.cn:8081/sam_svr/api_1.0_user_queryAccurate.do";
	public static final String URL_sendInviteMsg="http://ec2-54-222-170-218.cn-north-1.compute.amazonaws.com.cn:8081/sam_svr/api_1.0_common_sendInviteMsg.do";
	public static final String URL_editProfile="http://ec2-54-222-170-218.cn-north-1.compute.amazonaws.com.cn:8081/sam_svr/api_1.0_profile_profileUpdate.do";
	public static final String URL_writeAdvertisement="http://ec2-54-222-170-218.cn-north-1.compute.amazonaws.com.cn:8081/sam_svr/api_1.0_advertisement_advertisementWrite.do";

	public static final String URL_contact="http://ec2-54-222-170-218.cn-north-1.compute.amazonaws.com.cn:8081/sam_svr/api_1.0_contact_contact.do";
	public static final String URL_synccontact="http://ec2-54-222-170-218.cn-north-1.compute.amazonaws.com.cn:8081/sam_svr/api_1.0_contact_contactListQuery.do";
	public static final String URL_updateAvatar="http://ec2-54-222-170-218.cn-north-1.compute.amazonaws.com.cn:8081/sam_svr/api_1.0_profile_avatarUpdate.do";

	
	public static final int CONNECTION_TIMEOUT = 10000;
	public static final int HTTP_TIMEOUT = 20000;
	
	public int statusCode;
	public int ret;
	public boolean exception;
	
	public String token_id;
	public String question_id;
	public ContactUser userinfo;
	public appkey ak;
	public QuestionInfo qinfo;
	public MultiplePlacesInfo placesinfos;
	public MultipleUserProfile users;
	public MultipleContact contacts;
	public MultipleFollowUser followusers;
	public Advertisement adv;
	public MultipleSyncAdv syncadvs;
	public long latest_lastupdate;
	public ReceivedQuestion rq;
	public byte [] data;
	
	
	HttpCommClient(){
		statusCode = 0;
		ret = 0;
		exception = false;
		token_id = null;
		question_id=null;
		userinfo = null;
		ak = null;
		qinfo = null;
		placesinfos = null;
		users = null;
		followusers = null;
		adv = null;
		syncadvs = null;
		latest_lastupdate = 0;
		rq = null;
		data = null;
	}

	private HttpResponse httpCmdStart(String url, JSONObject jsonData)throws ClientProtocolException,IOException{
		if(Constants.POST_CMD){
			HttpPost requestPost = new HttpPost(url);
			
			List<BasicNameValuePair> params = new LinkedList<BasicNameValuePair>();
			params.add(new BasicNameValuePair("data",jsonData.toString()));
			requestPost.setEntity(new UrlEncodedFormEntity(params, HTTP.UTF_8));
			
			DefaultHttpClient client = new DefaultHttpClient();
			HttpParams http_params = client.getParams();
			if(http_params==null){
				http_params = new BasicHttpParams();
			}
			HttpConnectionParams.setConnectionTimeout(http_params, CONNECTION_TIMEOUT);
			HttpConnectionParams.setSoTimeout(http_params, HTTP_TIMEOUT);
			
			return client.execute(requestPost);
			
		}else{
			List<BasicNameValuePair> params = new LinkedList<BasicNameValuePair>();
			params.add(new BasicNameValuePair("data",jsonData.toString()));
			String param = URLEncodedUtils.format(params, "UTF-8"); 			
			String final_url = url + "?" + param;

			SamLog.i(TAG,final_url);	

			HttpGet requestGet = new HttpGet(final_url);
			DefaultHttpClient client = new DefaultHttpClient();
			HttpParams http_params = client.getParams();
			if(http_params==null){
				http_params = new BasicHttpParams();
			}
			HttpConnectionParams.setConnectionTimeout(http_params, CONNECTION_TIMEOUT);
			HttpConnectionParams.setSoTimeout(http_params, HTTP_TIMEOUT);

			return client.execute(requestGet);
		}
	}

	private boolean isHttpOK(){
		return (statusCode == HttpStatus.SC_OK) ? true : false;
	}

	private boolean isRetOK(){
		return (ret == 0) ? true : false;
	}

	private String getContentThumb(JSONObject jo){
		String thumb = null;
		try{
			thumb = jo.getString("content_thumb");
			return thumb;
		}catch(JSONException e){
			SamLog.i(TAG,"no content_thumb for this adv");	
			return null;
		}
	}

	private String getAvatarOrigin(JSONObject jo){
		String imgFileName = null;
		
		try{
			JSONObject avatar = jo.getJSONObject("avatar");
			imgFileName = avatar.getString("origin");
			return imgFileName;
		}catch(JSONException e){
			SamLog.i(TAG,"no origin avatar for this user");	
			return null;
		}
	}

	private String getAvatarThumb(JSONObject jo){
		String imgFileName = null;
		
		try{
			JSONObject avatar = jo.getJSONObject("avatar");
			imgFileName = avatar.getString("thumb");
			return imgFileName;
		}catch(JSONException e){
			SamLog.i(TAG,"no thumb avatar for this user");	
			return null;
		}
	}

	private String getThumb(JSONObject user){
		String imgFileName = null;
		
		try{
			imgFileName = user.getString("thumb");
			return imgFileName;
		}catch(JSONException e){
			SamLog.i(TAG,"no thumb return");	
			return null;
		}
	}

	private String getEmail(JSONObject jo){
		String email = null;
		try{
			email = jo.getString("email");
			return email;
		}catch(JSONException e){
			SamLog.i(TAG,"no email for this user");	
			return null;
		}
	}

	private String getAddress(JSONObject jo){
		String address = null;
		try{
			address = jo.getString("address");
			return address;
		}catch(JSONException e){
			SamLog.i(TAG,"no address for this user");	
			return null;
		}
	}

	private String getCountryCode(JSONObject jo){
		String countrycode = null;
		try{
			countrycode = jo.getString("countrycode");
			return countrycode;
		}catch(JSONException e){
			SamLog.i(TAG,"no countrycode for this sam user");	
			return null;
		}
	}

	private String getPhone(JSONObject jo){
		String phone = null;
		try{
			phone = jo.getString("phone");
			return phone;
		}catch(JSONException e){
			SamLog.i(TAG,"no phone for this sam user");	
			return null;
		}
	}

	private JSONObject getSamProsJson(JSONObject user){
		try{
			JSONObject sam_pros = user.getJSONObject("sam_pros_info");
			return sam_pros;
		}catch(JSONException e){
			SamLog.i(TAG,"no sam pros json");	
			return null;
		}
	}
		
	private void parseUsersJson(MultipleUserProfile users,JSONArray jsonUsers) throws JSONException{
		for (int i = 0; i < jsonUsers.length(); i++) {
			JSONObject user = (JSONObject) jsonUsers.get(i);
			ContactUser ui = new ContactUser();;
			
			ui.setunique_id(user.getLong("id"));
			ui.setusername(user.getString("username"));
			ui.setusertype(user.getInt("type"));
			ui.setlastupdate(user.getLong("lastupdate"));
			ui.setavatar(getAvatarThumb(user));
			ui.setavatar_original(getAvatarOrigin(user));
			ui.setcountrycode(user.getString("countrycode"));
			ui.setcellphone(user.getString("cellphone"));
			ui.setemail(getEmail(user));
			ui.setaddress(getAddress(user));

			if(ui.getusertype() == Constants.SAM_PROS){
				JSONObject sam_pros = user.getJSONObject("sam_pros_info");
				ui.setcompany_name(sam_pros.getString("company_name"));
				ui.setservice_category(sam_pros.getString("service_category"));
				ui.setservice_description(sam_pros.getString("service_description"));
				ui.setcountrycode_sp(getCountryCode(sam_pros));
				ui.setphone_sp(getPhone(sam_pros));
				ui.setemail_sp(getEmail(sam_pros));
				ui.setaddress_sp(getAddress(sam_pros));
				
			}
			users.adduser(ui);				
		}
		users.setcount(jsonUsers.length());
	}

	private ContactUser parseUserJson(JSONObject user) throws JSONException{
		ContactUser ui = new ContactUser();
		ui.setunique_id(user.getLong("id"));
		ui.setusername(user.getString("username"));
		ui.setlastupdate(user.getLong("lastupdate"));

		return ui;
	}

	private ReceivedQuestion parseReceivedQuestionJson(JSONObject body) throws JSONException{
		ReceivedQuestion rq = new ReceivedQuestion();
		rq.setquestion_id(body.getLong("question_id"));
		rq.setquestion(body.getString("question"));
		rq.setdatetime(body.getLong("datetime"));
		rq.setstatus(Constants.QUESTION_NOT_RESPONSED);
		rq.setaddress(body.getString("address"));
		rq.setsender_unique_id(body.getJSONObject("user").getLong("id"));
		rq.setsender_username(body.getJSONObject("user").getString("username"));

		return rq;
	}

	private Advertisement parseReceivedAdvertisementJson(JSONObject body) throws JSONException{
		Advertisement ad = new Advertisement();
		ad.setsender_unique_id(body.getLong("id"));
		ad.setadv_id(body.getLong("adv_id"));
		ad.setpublish_timestamp(body.getLong("publish_timestamp"));
		ad.settype(body.getInt("type"));
		ad.setcontent(body.getString("content"));
		ad.setcontent_thumb(getContentThumb(body));

		return ad;
	}




	private JSONObject constructRegisterCodeReqeustJson(VerifyCodeCoreObj vcobj) throws JSONException{
			JSONObject header = new JSONObject();
			header.putOpt("action", "register-code-request");
			
			JSONObject body = new JSONObject();
			body.putOpt("countrycode", vcobj.countrycode);
			body.putOpt("cellphone",vcobj.cellphone);
			body.putOpt("deviceid",vcobj.deviceid);
			
			JSONObject data = new JSONObject();
			data.put("header", header);
			data.put("body", body);

			return data;
	}
	
	public boolean register_code_request(VerifyCodeCoreObj vcobj){			
		try{
			JSONObject  data = constructRegisterCodeReqeustJson(vcobj);

			HttpResponse response = httpCmdStart(URL_registerCodeRequest,data);
			
			statusCode = response.getStatusLine().getStatusCode();
			
			if(isHttpOK()){
				String rev = EntityUtils.toString(response.getEntity());
				SamLog.i(TAG,"rev:" + rev);
				
				JSONObject obj = new JSONObject(rev); 
				ret = obj.getInt("ret");
				return true;
				
			}else{
				SamLog.i(TAG,"register code request http status code:"+statusCode);
				return false;
			}
		
		}catch (JSONException e) {  
			exception = true;
			e.printStackTrace();
			SamLog.e(TAG,"register code request :JSONException");
			return false;
		} catch (ClientProtocolException e) {
			exception = true;
			SamLog.e(TAG,"register code request :ClientProtocolException");
			e.printStackTrace(); 
			return false;
		} catch (IOException e) { 
			exception = true;
			SamLog.e(TAG,"register code request :IOException");
			e.printStackTrace(); 
			return false;
		} catch (Exception e) { 
			exception = true;
			SamLog.e(TAG,"register code request :Exception");
			e.printStackTrace(); 
			return false;
		}
		
	}

	private JSONObject constructRegisterCodeVerifyJson(VerifyCodeCoreObj vcobj) throws JSONException{
			JSONObject header = new JSONObject();
			header.putOpt("action", "signup-code-verify");
			
			JSONObject body = new JSONObject();
			body.putOpt("countrycode", vcobj.countrycode);
			body.putOpt("cellphone",vcobj.cellphone);
			body.putOpt("verifycode",vcobj.verifycode);
			body.putOpt("deviceid",vcobj.deviceid);
			
			JSONObject data = new JSONObject();
			data.put("header", header);
			data.put("body", body);

			return data;
	}
	
	public boolean register_code_verify(VerifyCodeCoreObj vcobj){			
		try{
			JSONObject  data = constructRegisterCodeVerifyJson(vcobj);

			HttpResponse response = httpCmdStart(URL_registerCodeVerify,data);
			
			statusCode = response.getStatusLine().getStatusCode();
			
			if(isHttpOK()){
				String rev = EntityUtils.toString(response.getEntity());
				SamLog.i(TAG,"rev:" + rev);
				
				JSONObject obj = new JSONObject(rev); 
				ret = obj.getInt("ret");
				return true;
				
			}else{
				SamLog.i(TAG,"register code verify http status code:"+statusCode);
				return false;
			}
		
		}catch (JSONException e) {  
			exception = true;
			e.printStackTrace();
			SamLog.e(TAG,"register code verify :JSONException");
			return false;
		} catch (ClientProtocolException e) {
			exception = true;
			SamLog.e(TAG,"register code verify :ClientProtocolException");
			e.printStackTrace(); 
			return false;
		} catch (IOException e) { 
			exception = true;
			SamLog.e(TAG,"register code verify :IOException");
			e.printStackTrace(); 
			return false;
		} catch (Exception e) { 
			exception = true;
			SamLog.e(TAG,"register code verify :Exception");
			e.printStackTrace(); 
			return false;
		}
		
	}

	private JSONObject constructSignUpJson(SignUpCoreObj suobj) throws JSONException{
			JSONObject header = new JSONObject();
			header.putOpt("action", "register");
			
			JSONObject body = new JSONObject();
			body.putOpt("countrycode",suobj.countrycode);
			body.putOpt("cellphone",suobj.cellphone);
			body.putOpt("verifycode",suobj.verifycode);
			body.putOpt("username",suobj.username);
			body.putOpt("pwd",suobj.password);
			body.putOpt("deviceid",suobj.deviceid);
			
			JSONObject data = new JSONObject();
			data.put("header", header);
			data.put("body", body);

			return data;
	}
	
	public boolean signup(SignUpCoreObj suobj){			
		try{
			SamLog.i(TAG,"signup start");
			JSONObject  signup_data = constructSignUpJson(suobj);

			HttpResponse response = httpCmdStart(URL_signup,signup_data);
			
			statusCode = response.getStatusLine().getStatusCode();
			
			if(isHttpOK()){
				String rev = EntityUtils.toString(response.getEntity());
				SamLog.i(TAG,"rev:" + rev);
				
				JSONObject obj = new JSONObject(rev); 
				ret = obj.getInt("ret");

				if(isRetOK()){
					token_id = obj.getString("token");
					JSONObject user = obj.getJSONObject("user");
					userinfo = new ContactUser();
					userinfo.setunique_id(user.getLong("id"));
					userinfo.setusername(suobj.username);
					userinfo.setusertype(Constants.USER);
					userinfo.setlastupdate(user.getLong("lastupdate"));
					userinfo.setcountrycode(suobj.countrycode);
					userinfo.setcellphone(suobj.cellphone); 
				}

				SamLog.i(TAG,"signup end");
				return true;
				
			}else{
				SamLog.i(TAG,"signup http status code:"+statusCode);
				return false;
			}
		
		}catch (JSONException e) {  
			exception = true;
			e.printStackTrace();
			SamLog.e(TAG,"signup: JSONException");
			return false;
		} catch (ClientProtocolException e) {
			exception = true;
			SamLog.e(TAG,"signup:ClientProtocolException");
			e.printStackTrace(); 
			return false;
		} catch (IOException e) { 
			exception = true;
			SamLog.e(TAG,"signup:IOException");
			e.printStackTrace(); 
			return false;
		} catch (Exception e) { 
			exception = true;
			SamLog.e(TAG,"signup:Exception");
			e.printStackTrace(); 
			return false;
		}
		
	}


	private JSONObject constructSignInJson(SignInCoreObj siobj) throws JSONException{
			JSONObject header = new JSONObject();
			header.putOpt("action", "login");
			
			JSONObject body = new JSONObject();
			body.putOpt("countrycode",siobj.countrycode);
			body.putOpt("account",siobj.account);
			body.putOpt("pwd",siobj.password);
			body.putOpt("deviceid",siobj.deviceid);
			
			JSONObject data = new JSONObject();
			data.put("header", header);
			data.put("body", body);

			return data;
	}
	
	public boolean signin(SignInCoreObj siobj){			
		try{
			JSONObject  data = constructSignInJson(siobj);

			HttpResponse response = httpCmdStart(URL_signin,data);
			
			statusCode = response.getStatusLine().getStatusCode();
			
			if(isHttpOK()){
				String rev = EntityUtils.toString(response.getEntity());
				SamLog.i(TAG,"rev:" + rev);
				
				JSONObject obj = new JSONObject(rev); 
				ret = obj.getInt("ret");

				if(isRetOK()){
					token_id = obj.getString("token");
					JSONObject user = obj.getJSONObject("user");
					userinfo = new ContactUser();
					userinfo.setunique_id(user.getLong("id"));
					userinfo.setusername(user.getString("username"));
					userinfo.setusertype(user.getInt("type"));
					userinfo.setlastupdate(user.getLong("lastupdate"));
					userinfo.setavatar(getAvatarThumb(user));
					userinfo.setavatar_original(getAvatarOrigin(user));
					userinfo.setcountrycode(user.getString("countrycode"));
					userinfo.setcellphone(user.getString("cellphone"));
					userinfo.setemail(getEmail(user));
					userinfo.setaddress(getAddress(user));

					if(userinfo.getusertype() == Constants.SAM_PROS){
						JSONObject sam_pros = user.getJSONObject("sam_pros_info");
						userinfo.setcompany_name(sam_pros.getString("company_name"));
						userinfo.setservice_category(sam_pros.getString("service_category"));
						userinfo.setservice_description(sam_pros.getString("service_description"));
						userinfo.setcountrycode_sp(getCountryCode(sam_pros));
						userinfo.setphone_sp(getPhone(sam_pros));
						userinfo.setemail_sp(getEmail(sam_pros));
						userinfo.setaddress_sp(getAddress(sam_pros));
					}
				}
				
				return true;
				
			}else{
				SamLog.i(TAG,"signin http status code:"+statusCode);
				return false;
			}
		
		}catch (JSONException e) {  
			exception = true;
			e.printStackTrace();
			SamLog.e(TAG,"signin:JSONException");
			return false;
		} catch (ClientProtocolException e) {
			exception = true;
			SamLog.e(TAG,"signin:ClientProtocolException");
			e.printStackTrace(); 
			return false;
		} catch (IOException e) { 
			exception = true;
			SamLog.e(TAG,"signin:IOException");
			e.printStackTrace(); 
			return false;
		} catch (Exception e) { 
			exception = true;
			SamLog.e(TAG,"signin:Exception");
			e.printStackTrace(); 
			return false;
		}
		
	}

	private JSONObject constructGetAppKeyJson(GetAppKeyCoreObj gakobj) throws JSONException{
			JSONObject header = new JSONObject();
			header.putOpt("action", "appkey-get");
			header.putOpt("token", gakobj.token);
			
			JSONObject body = new JSONObject();
			
			JSONObject data = new JSONObject();
			data.put("header", header);
			data.put("body", body);

			return data;
	}
	
	public boolean get_app_key(GetAppKeyCoreObj gakobj){			
		try{
			JSONObject  data = constructGetAppKeyJson(gakobj);

			HttpResponse response = httpCmdStart(URL_getappkey,data);
			
			statusCode = response.getStatusLine().getStatusCode();
			
			if(isHttpOK()){
				String rev = EntityUtils.toString(response.getEntity());
				SamLog.i(TAG,"rev:" + rev);
				
				JSONObject obj = new JSONObject(rev); 
				ret = obj.getInt("ret");

				if(isRetOK()){
					String appi = obj.getString("appi");
					String appk = obj.getString("appk");
					String apps = obj.getString("apps");
					ak = new appkey(appi,appk,apps);
				}
				
				return true;
				
			}else{
				SamLog.i(TAG,"getappkey http status code:"+statusCode);
				return false;
			}
		
		}catch (JSONException e) {  
			exception = true;
			e.printStackTrace();
			SamLog.e(TAG,"getappkey:JSONException");
			return false;
		} catch (ClientProtocolException e) {
			exception = true;
			SamLog.e(TAG,"getappkey:ClientProtocolException");
			e.printStackTrace(); 
			return false;
		} catch (IOException e) { 
			exception = true;
			SamLog.e(TAG,"getappkey:IOException");
			e.printStackTrace(); 
			return false;
		} catch (Exception e) { 
			exception = true;
			SamLog.e(TAG,"getappkey:Exception");
			e.printStackTrace(); 
			return false;
		}
		
	}

	private JSONObject constructSignOutJson(SignOutCoreObj soobj) throws JSONException{
			JSONObject header = new JSONObject();
			header.putOpt("action", "logout");
			header.putOpt("token", soobj.token);
			SamLog.i("test","constructSignOutJson token:"+soobj.token);
			
			JSONObject body = new JSONObject();
			
			JSONObject data = new JSONObject();
			data.put("header", header);
			data.put("body", body);

			return data;
	}
	
	public boolean signout(SignOutCoreObj soobj){			
		try{
			JSONObject  data = constructSignOutJson(soobj);

			HttpResponse response = httpCmdStart(URL_signout,data);
			
			statusCode = response.getStatusLine().getStatusCode();
			
			if(isHttpOK()){
				String rev = EntityUtils.toString(response.getEntity());
				SamLog.i(TAG,"rev:" + rev);
				
				JSONObject obj = new JSONObject(rev); 
				ret = obj.getInt("ret");
				
				return true;
				
			}else{
				SamLog.i(TAG,"signout http status code:"+statusCode);
				return false;
			}
		
		}catch (JSONException e) {  
			exception = true;
			e.printStackTrace();
			SamLog.e(TAG,"signout:JSONException");
			return false;
		} catch (ClientProtocolException e) {
			exception = true;
			SamLog.e(TAG,"signout:ClientProtocolException");
			e.printStackTrace(); 
			return false;
		} catch (IOException e) { 
			exception = true;
			SamLog.e(TAG,"signout:IOException");
			e.printStackTrace(); 
			return false;
		} catch (Exception e) { 
			exception = true;
			SamLog.e(TAG,"signout:Exception");
			e.printStackTrace(); 
			return false;
		}
		
	}

	private JSONObject constructCreateSamProsJson(CreateSamProsCoreObj cspobj) throws JSONException{
			JSONObject header = new JSONObject();
			header.putOpt("action", "create-sam-pros");
			header.putOpt("token", cspobj.token);
			
			JSONObject body = new JSONObject();
			if(cspobj.sam_pros.getcompany_name() != null){
				body.putOpt("company_name",cspobj.sam_pros.getcompany_name());
			}
			body.putOpt("service_category",cspobj.sam_pros.getservice_category());
			body.putOpt("service_description",cspobj.sam_pros.getservice_description());
			if(cspobj.sam_pros.getcountrycode_sp() != null){
				body.putOpt("countrycode",cspobj.sam_pros.getcountrycode_sp());
			}

			if(cspobj.sam_pros.getphone_sp() != null){
				body.putOpt("phone",cspobj.sam_pros.getphone_sp());
			}

			if(cspobj.sam_pros.getemail_sp() != null){
				body.putOpt("email",cspobj.sam_pros.getemail_sp());
			}

			if(cspobj.sam_pros.getaddress_sp() != null){
				JSONObject location = new JSONObject();
				location.putOpt("address",cspobj.sam_pros.getaddress_sp());
				body.put("location",location);
			}

			/*JSONObject avatar = new JSONObject();
			boolean avatar_existed = false;
			if(cspobj.sam_pros.getavatar_sampros() != null){
				avatar.putOpt("thumb",cspobj.sam_pros.getavatar_sampros());
				avatar_existed = true;
			}

			if(cspobj.sam_pros.getavatar_original_sampros() != null){
				avatar.putOpt("origin",cspobj.sam_pros.getavatar_original_sampros());
				avatar_existed = true;
			}

			if(avatar_existed){
				body.put("avatar",avatar);
			}*/
			
			JSONObject data = new JSONObject();
			data.put("header", header);
			data.put("body", body);

			return data;
	}
	
	public boolean create_sam_pros(CreateSamProsCoreObj cspobj){			
		try{
			JSONObject  data = constructCreateSamProsJson(cspobj);

			HttpResponse response = httpCmdStart(URL_createsp,data);
			
			statusCode = response.getStatusLine().getStatusCode();
			
			if(isHttpOK()){
				String rev = EntityUtils.toString(response.getEntity());
				SamLog.i(TAG,"rev:" + rev);
				
				JSONObject obj = new JSONObject(rev); 
				ret = obj.getInt("ret");

				if(isRetOK()){
					JSONObject user = obj.getJSONObject("user");
					userinfo = new ContactUser(cspobj.sam_pros);
					userinfo.setlastupdate(user.getLong("lastupdate"));
					userinfo.setusertype(user.getInt("type"));
					userinfo.setcompany_name(cspobj.sam_pros.getcompany_name());
					userinfo.setservice_category(cspobj.sam_pros.getservice_category());
					userinfo.setservice_description(cspobj.sam_pros.getservice_description());
					userinfo.setcountrycode_sp(cspobj.sam_pros.getcountrycode_sp());
					userinfo.setphone_sp(cspobj.sam_pros.getphone_sp());
					userinfo.setemail_sp(cspobj.sam_pros.getemail_sp());
					userinfo.setaddress_sp(cspobj.sam_pros.getaddress_sp());
					
				}
				
				
				return true;
				
			}else{
				SamLog.i(TAG,"create sam pros http status code:"+statusCode);
				return false;
			}
		
		}catch (JSONException e) {  
			exception = true;
			e.printStackTrace();
			SamLog.e(TAG,"create sam pros:JSONException");
			return false;
		} catch (ClientProtocolException e) {
			exception = true;
			SamLog.e(TAG,"create sam pros:ClientProtocolException");
			e.printStackTrace(); 
			return false;
		} catch (IOException e) { 
			exception = true;
			SamLog.e(TAG,"create sam pros:IOException");
			e.printStackTrace(); 
			return false;
		} catch (Exception e) { 
			exception = true;
			SamLog.e(TAG,"create sam pros:Exception");
			e.printStackTrace(); 
			return false;
		}
		
	}

	private JSONObject constructFindpwdCodeRequestJson(VerifyCodeCoreObj vcobj) throws JSONException{
			JSONObject header = new JSONObject();
			header.putOpt("action", "findpwd-code-request");
			
			JSONObject body = new JSONObject();

			body.putOpt("countrycode",vcobj.countrycode);
			body.putOpt("cellphone",vcobj.cellphone);
			body.putOpt("deviceid",vcobj.deviceid);
			
			JSONObject data = new JSONObject();
			data.put("header", header);
			data.put("body", body);

			return data;
	}
	
	public boolean findpwd_code_request(VerifyCodeCoreObj vcobj){			
		try{
			JSONObject  data = constructFindpwdCodeRequestJson(vcobj);

			HttpResponse response = httpCmdStart(URL_findpwdCodeRequest,data);
			
			statusCode = response.getStatusLine().getStatusCode();
			
			if(isHttpOK()){
				String rev = EntityUtils.toString(response.getEntity());
				SamLog.i(TAG,"rev:" + rev);
				
				JSONObject obj = new JSONObject(rev); 
				ret = obj.getInt("ret");
				
				return true;
				
			}else{
				SamLog.i(TAG,"findpwd-code-request http status code:"+statusCode);
				return false;
			}
		
		}catch (JSONException e) {  
			exception = true;
			e.printStackTrace();
			SamLog.e(TAG,"findpwd-code-request:JSONException");
			return false;
		} catch (ClientProtocolException e) {
			exception = true;
			SamLog.e(TAG,"findpwd-code-request:ClientProtocolException");
			e.printStackTrace(); 
			return false;
		} catch (IOException e) { 
			exception = true;
			SamLog.e(TAG,"findpwd-code-request:IOException");
			e.printStackTrace(); 
			return false;
		} catch (Exception e) { 
			exception = true;
			SamLog.e(TAG,"findpwd-code-request:Exception");
			e.printStackTrace(); 
			return false;
		}
		
	}

	private JSONObject constructFindpwdCodeVerifyJson(VerifyCodeCoreObj vcobj) throws JSONException{
			JSONObject header = new JSONObject();
			header.putOpt("action", "findpwd-code-verify");
			
			JSONObject body = new JSONObject();
			body.putOpt("countrycode",vcobj.countrycode);
			body.putOpt("cellphone",vcobj.cellphone);
			body.putOpt("verifycode",vcobj.verifycode);
			body.putOpt("deviceid",vcobj.deviceid);
			
			JSONObject data = new JSONObject();
			data.put("header", header);
			data.put("body", body);

			return data;
	}
	
	public boolean findpwd_code_verify(VerifyCodeCoreObj vcobj){			
		try{
			JSONObject  data = constructFindpwdCodeVerifyJson(vcobj);

			HttpResponse response = httpCmdStart(URL_findpwdCodeVerify,data);
			
			statusCode = response.getStatusLine().getStatusCode();
			
			if(isHttpOK()){
				String rev = EntityUtils.toString(response.getEntity());
				SamLog.i(TAG,"rev:" + rev);
				
				JSONObject obj = new JSONObject(rev); 
				ret = obj.getInt("ret");
				
				return true;
				
			}else{
				SamLog.i(TAG,"findpwd-code-verify http status code:"+statusCode);
				return false;
			}
		
		}catch (JSONException e) {  
			exception = true;
			e.printStackTrace();
			SamLog.e(TAG,"findpwd-code-verify:JSONException");
			return false;
		} catch (ClientProtocolException e) {
			exception = true;
			SamLog.e(TAG,"findpwd-code-verify:ClientProtocolException");
			e.printStackTrace(); 
			return false;
		} catch (IOException e) { 
			exception = true;
			SamLog.e(TAG,"findpwd-code-verify:IOException");
			e.printStackTrace(); 
			return false;
		} catch (Exception e) { 
			exception = true;
			SamLog.e(TAG,"findpwd-code-verify:Exception");
			e.printStackTrace(); 
			return false;
		}
		
	}

	private JSONObject constructFindpwdUpdateJson(VerifyCodeCoreObj vcobj) throws JSONException{
			JSONObject header = new JSONObject();
			header.putOpt("action", "findpwd-update");
			
			JSONObject body = new JSONObject();

			body.putOpt("countrycode",vcobj.countrycode);
			body.putOpt("cellphone",vcobj.cellphone);
			body.putOpt("verifycode",vcobj.verifycode);
			body.putOpt("pwd",vcobj.new_password);
			body.putOpt("deviceid",vcobj.deviceid);
			
			JSONObject data = new JSONObject();
			data.put("header", header);
			data.put("body", body);

			return data;
	}
	
	public boolean findpwd_update(VerifyCodeCoreObj vcobj){			
		try{
			JSONObject  data = constructFindpwdUpdateJson(vcobj);

			HttpResponse response = httpCmdStart(URL_findpwdUpdate,data);
			
			statusCode = response.getStatusLine().getStatusCode();
			
			if(isHttpOK()){
				String rev = EntityUtils.toString(response.getEntity());
				SamLog.i(TAG,"rev:" + rev);
				
				JSONObject obj = new JSONObject(rev); 
				ret = obj.getInt("ret");
				
				return true;
				
			}else{
				SamLog.i(TAG,"findpwd-update http status code:"+statusCode);
				return false;
			}
		
		}catch (JSONException e) {  
			exception = true;
			e.printStackTrace();
			SamLog.e(TAG,"findpwd-update:JSONException");
			return false;
		} catch (ClientProtocolException e) {
			exception = true;
			SamLog.e(TAG,"findpwd-update:ClientProtocolException");
			e.printStackTrace(); 
			return false;
		} catch (IOException e) { 
			exception = true;
			SamLog.e(TAG,"findpwd-update:IOException");
			e.printStackTrace(); 
			return false;
		} catch (Exception e) { 
			exception = true;
			SamLog.e(TAG,"findpwd-update:Exception");
			e.printStackTrace(); 
			return false;
		}
		
	}

	private JSONObject constructUpdatePasswordJson(UpdatePwdCoreObj upobj) throws JSONException{
			JSONObject header = new JSONObject();
			header.putOpt("action", "pwd-update");
			header.putOpt("token", upobj.token);
			
			JSONObject body = new JSONObject();
			body.putOpt("old_pwd",upobj.old_pwd);
			body.putOpt("new_pwd", upobj.new_pwd);
			
			JSONObject data = new JSONObject();
			data.put("header", header);
			data.put("body", body);

			return data;
	}
	
	public boolean update_password(UpdatePwdCoreObj upobj){			
		try{
			JSONObject  data = constructUpdatePasswordJson(upobj);

			HttpResponse response = httpCmdStart(URL_pwdUpdate,data);
			
			statusCode = response.getStatusLine().getStatusCode();
			
			if(isHttpOK()){
				String rev = EntityUtils.toString(response.getEntity());
				SamLog.i(TAG,"rev:" + rev);
				
				JSONObject obj = new JSONObject(rev); 
				ret = obj.getInt("ret");
				
				return true;
				
			}else{
				SamLog.i(TAG,"update password http status code:"+statusCode);
				return false;
			}
		
		}catch (JSONException e) {  
			exception = true;
			e.printStackTrace();
			SamLog.e(TAG,"update password:JSONException");
			return false;
		} catch (ClientProtocolException e) {
			exception = true;
			SamLog.e(TAG,"update password:ClientProtocolException");
			e.printStackTrace(); 
			return false;
		} catch (IOException e) { 
			exception = true;
			SamLog.e(TAG,"update password:IOException");
			e.printStackTrace(); 
			return false;
		} catch (Exception e) { 
			exception = true;
			SamLog.e(TAG,"update password:Exception");
			e.printStackTrace(); 
			return false;
		}
		
	}

	private JSONObject constructSendQuestionJson(SendqCoreObj sqobj) throws JSONException{
			JSONObject header = new JSONObject();
			header.putOpt("action", "question");
			header.putOpt("token", sqobj.token);
			
			JSONObject body = new JSONObject();
			body.putOpt("opt",0);
			body.putOpt("question", sqobj.question);

			JSONObject location = new JSONObject();
			boolean location_existed = false;
			if(sqobj.longitude != Constants.CONSTANTS_LONGITUDE_LATITUDE_NULL
				&& sqobj.latitude != Constants.CONSTANTS_LONGITUDE_LATITUDE_NULL){
				JSONObject location_info = new JSONObject();
				location_info.putOpt("longitude",sqobj.longitude);
				location_info.putOpt("latitude",sqobj.latitude);
				location.put("location_info",location_info);
				location_existed = true;
			}

			if(sqobj.place_id != null){
				location.putOpt("place_id",sqobj.place_id);
				location_existed = true;
			}

			if(sqobj.address != null){
				location.putOpt("address",sqobj.address);
				location_existed = true;
			}

			if(location_existed){
				body.put("location",location);
			}
			
			JSONObject data = new JSONObject();
			data.put("header", header);
			data.put("body", body);

			return data;
	}
	
	public boolean send_question(SendqCoreObj sqobj){			
		try{
			JSONObject  data = constructSendQuestionJson(sqobj);

			HttpResponse response = httpCmdStart(URL_questionSend,data);
			
			statusCode = response.getStatusLine().getStatusCode();
			
			if(isHttpOK()){
				String rev = EntityUtils.toString(response.getEntity());
				SamLog.i(TAG,"rev:" + rev);
				
				JSONObject obj = new JSONObject(rev); 
				ret = obj.getInt("ret");

				if(isRetOK()){
					qinfo = new QuestionInfo(sqobj.question);
					qinfo.question_id = obj.getLong("question_id");
					qinfo.datetime = obj.getLong("datetime");
					qinfo.latitude = sqobj.latitude;
					qinfo.longitude = sqobj.longitude;
					qinfo.address = sqobj.address;
					qinfo.place_id = sqobj.place_id;
				}
				
				return true;
				
			}else{
				SamLog.i(TAG,"send question http status code:"+statusCode);
				return false;
			}
		
		}catch (JSONException e) {  
			exception = true;
			e.printStackTrace();
			SamLog.e(TAG,"send question:JSONException");
			return false;
		} catch (ClientProtocolException e) {
			exception = true;
			SamLog.e(TAG,"send question:ClientProtocolException");
			e.printStackTrace(); 
			return false;
		} catch (IOException e) { 
			exception = true;
			SamLog.e(TAG,"send question:IOException");
			e.printStackTrace(); 
			return false;
		} catch (Exception e) { 
			exception = true;
			SamLog.e(TAG,"send question:Exception");
			e.printStackTrace(); 
			return false;
		}
		
	}


	private JSONObject constructGetPlacesInfoJson(GetPlacesInfoCoreObj gpiobj) throws JSONException{
			JSONObject header = new JSONObject();
			header.putOpt("action", "get-places-info-request");
			header.putOpt("token", gpiobj.token);
			
			JSONObject body = new JSONObject();
			body.putOpt("key",gpiobj.key);
			
			JSONObject data = new JSONObject();
			data.put("header", header);
			data.put("body", body);

			return data;
	}

	public boolean get_places_info(GetPlacesInfoCoreObj gpiobj){			
		try{
			JSONObject  data = constructGetPlacesInfoJson(gpiobj);

			HttpResponse response = httpCmdStart(URL,data);
			
			statusCode = response.getStatusLine().getStatusCode();
			
			if(isHttpOK()){
				String rev = EntityUtils.toString(response.getEntity());
				SamLog.i(TAG,"rev:" + rev);
				
				JSONObject obj = new JSONObject(rev); 
				ret = obj.getInt("ret");

				if(isRetOK()){
					placesinfos = new MultiplePlacesInfo();
					int count = obj.getInt("count");
					if(count > 0){
						JSONArray jsonArrayX = obj.getJSONArray("places_info");
						for (int i = 0; i < jsonArrayX.length(); i++) {
							JSONObject jo = (JSONObject) jsonArrayX.get(i);
							String desc = jo.getString("description");
							String place_id = jo.getString("place_id");
							PlacesInfo info = new PlacesInfo(desc,place_id);
							placesinfos.addinfo(info);
						}
						placesinfos.setcount(jsonArrayX.length());
					}
				}
				
				return true;
				
			}else{
				SamLog.i(TAG,"get places info http status code:"+statusCode);
				return false;
			}
		
		}catch (JSONException e) {  
			exception = true;
			e.printStackTrace();
			SamLog.e(TAG,"get places info:JSONException");
			return false;
		} catch (ClientProtocolException e) {
			exception = true;
			SamLog.e(TAG,"get places info:ClientProtocolException");
			e.printStackTrace(); 
			return false;
		} catch (IOException e) { 
			exception = true;
			SamLog.e(TAG,"get places info:IOException");
			e.printStackTrace(); 
			return false;
		} catch (Exception e) { 
			exception = true;
			SamLog.e(TAG,"get places info:Exception");
			e.printStackTrace(); 
			return false;
		}
		
	}

	private JSONObject constructFollowJson(FollowCoreObj fcobj) throws JSONException{
			JSONObject header = new JSONObject();
			header.putOpt("action", "follow");
			header.putOpt("token", fcobj.token);
			
			JSONObject body = new JSONObject();
			body.putOpt("opt",fcobj.isFollow?1:0);
			body.putOpt("id", fcobj.sp.getunique_id());
			
			JSONObject data = new JSONObject();
			data.put("header", header);
			data.put("body", body);

			return data;
	}
	
	public boolean follow(FollowCoreObj fcobj){			
		try{
			JSONObject  data = constructFollowJson(fcobj);

			HttpResponse response = httpCmdStart(URL_follow,data);
			
			statusCode = response.getStatusLine().getStatusCode();
			
			if(isHttpOK()){
				String rev = EntityUtils.toString(response.getEntity());
				SamLog.i(TAG,"rev:" + rev);
				
				JSONObject obj = new JSONObject(rev); 
				ret = obj.getInt("ret");

				if(isRetOK()){
					JSONObject user = obj.getJSONObject("user");
					userinfo = fcobj.sp;
					latest_lastupdate = user.getLong("lastupdate");
				}
				
				return true;
				
			}else{
				SamLog.i(TAG,"follow http status code:"+statusCode);
				return false;
			}
		
		}catch (JSONException e) {  
			exception = true;
			e.printStackTrace();
			SamLog.e(TAG,"follow:JSONException");
			return false;
		} catch (ClientProtocolException e) {
			exception = true;
			SamLog.e(TAG,"follow:ClientProtocolException");
			e.printStackTrace(); 
			return false;
		} catch (IOException e) { 
			exception = true;
			SamLog.e(TAG,"follow:IOException");
			e.printStackTrace(); 
			return false;
		} catch (Exception e) { 
			exception = true;
			SamLog.e(TAG,"follow:Exception");
			e.printStackTrace(); 
			return false;
		}
		
	}

	private JSONObject constructBlockJson(BlockCoreObj bcobj) throws JSONException{
			JSONObject header = new JSONObject();
			header.putOpt("action", "block");
			header.putOpt("token", bcobj.token);
			
			JSONObject body = new JSONObject();
			body.putOpt("opt",bcobj.isBlock?1:0);
			body.putOpt("id", bcobj.sam_pros.getunique_id());
			
			JSONObject data = new JSONObject();
			data.put("header", header);
			data.put("body", body);

			return data;
	}
	
	public boolean block(BlockCoreObj bcobj){			
		try{
			JSONObject  data = constructBlockJson(bcobj);

			HttpResponse response = httpCmdStart(URL_block,data);
			
			statusCode = response.getStatusLine().getStatusCode();
			
			if(isHttpOK()){
				String rev = EntityUtils.toString(response.getEntity());
				SamLog.i(TAG,"rev:" + rev);
				
				JSONObject obj = new JSONObject(rev); 
				ret = obj.getInt("ret");

				if(isRetOK()){
					JSONObject user = obj.getJSONObject("user");
					userinfo = bcobj.sam_pros;
					latest_lastupdate = user.getLong("lastupdate");
				}
				
				return true;
				
			}else{
				SamLog.i(TAG,"block http status code:"+statusCode);
				return false;
			}
		
		}catch (JSONException e) {  
			exception = true;
			e.printStackTrace();
			SamLog.e(TAG,"block:JSONException");
			return false;
		} catch (ClientProtocolException e) {
			exception = true;
			SamLog.e(TAG,"block:ClientProtocolException");
			e.printStackTrace(); 
			return false;
		} catch (IOException e) { 
			exception = true;
			SamLog.e(TAG,"block:IOException");
			e.printStackTrace(); 
			return false;
		} catch (Exception e) { 
			exception = true;
			SamLog.e(TAG,"block:Exception");
			e.printStackTrace(); 
			return false;
		}
		
	}

	private JSONObject constructFavouriteJson(FavouriteCoreObj fcobj) throws JSONException{
			JSONObject header = new JSONObject();
			header.putOpt("action", "favourite");
			header.putOpt("token", fcobj.token);
			
			JSONObject body = new JSONObject();
			body.putOpt("opt",fcobj.isFavourite?1:0);
			body.putOpt("id", fcobj.sam_pros.getunique_id());
			
			JSONObject data = new JSONObject();
			data.put("header", header);
			data.put("body", body);

			return data;
	}
	
	public boolean favourite(FavouriteCoreObj fcobj){			
		try{
			JSONObject  data = constructFavouriteJson(fcobj);

			HttpResponse response = httpCmdStart(URL_favourite,data);
			
			statusCode = response.getStatusLine().getStatusCode();
			
			if(isHttpOK()){
				String rev = EntityUtils.toString(response.getEntity());
				SamLog.i(TAG,"rev:" + rev);
				
				JSONObject obj = new JSONObject(rev); 
				ret = obj.getInt("ret");

				if(isRetOK()){
					JSONObject user = obj.getJSONObject("user");
					userinfo = fcobj.sam_pros;
					latest_lastupdate = user.getLong("lastupdate");
				}
				
				return true;
				
			}else{
				SamLog.i(TAG,"favourite http status code:"+statusCode);
				return false;
			}
		
		}catch (JSONException e) {  
			exception = true;
			e.printStackTrace();
			SamLog.e(TAG,"favourite:JSONException");
			return false;
		} catch (ClientProtocolException e) {
			exception = true;
			SamLog.e(TAG,"favourite:ClientProtocolException");
			e.printStackTrace(); 
			return false;
		} catch (IOException e) { 
			exception = true;
			SamLog.e(TAG,"favourite:IOException");
			e.printStackTrace(); 
			return false;
		} catch (Exception e) { 
			exception = true;
			SamLog.e(TAG,"favourite:Exception");
			e.printStackTrace(); 
			return false;
		}
		
	}

	private JSONObject constructQueryUserFuzzyJson(QueryUserFuzzyCoreObj qufobj) throws JSONException{
			JSONObject header = new JSONObject();
			header.putOpt("action", "query-fuzzy");
			header.putOpt("token", qufobj.token);

			
			JSONObject body = new JSONObject();
			body.putOpt("opt",1);

			JSONObject param = new JSONObject();
			param.putOpt("search_key",qufobj.search_key);
			
			body.put("param",param);
			
			JSONObject data = new JSONObject();
			data.put("header", header);
			data.put("body", body);

			return data;
	}
	
	public boolean query_user_fuzzy(QueryUserFuzzyCoreObj qufobj){			
		try{
			JSONObject  data = constructQueryUserFuzzyJson(qufobj);

			HttpResponse response = httpCmdStart(URL_queryUserFuzzy,data);
			
			statusCode = response.getStatusLine().getStatusCode();
			
			if(isHttpOK()){
				String rev = EntityUtils.toString(response.getEntity());
				SamLog.i(TAG,"rev:" + rev);
				
				JSONObject obj = new JSONObject(rev); 
				ret = obj.getInt("ret");

				if(isRetOK()){
					users = new MultipleUserProfile();
					int count = obj.getInt("count");
					if(count > 0){
						JSONArray jsonArrayX = obj.getJSONArray("users");
						parseUsersJson(users,jsonArrayX);
					}
				}
				
				return true;
				
			}else{
				SamLog.i(TAG,"query user fuzzy http status code:"+statusCode);
				return false;
			}
		
		}catch (JSONException e) {  
			exception = true;
			e.printStackTrace();
			SamLog.e(TAG,"query user fuzzy:JSONException");
			return false;
		} catch (ClientProtocolException e) {
			exception = true;
			SamLog.e(TAG,"query user fuzzy:ClientProtocolException");
			e.printStackTrace(); 
			return false;
		} catch (IOException e) { 
			exception = true;
			SamLog.e(TAG,"query user fuzzy:IOException");
			e.printStackTrace(); 
			return false;
		} catch (Exception e) { 
			exception = true;
			SamLog.e(TAG,"query user fuzzy:Exception");
			e.printStackTrace(); 
			return false;
		}
		
	}

	private JSONObject constructQueryUserPreciseJson(QueryUserPreciseCoreObj qupobj) throws JSONException{
			JSONObject header = new JSONObject();
			header.putOpt("action", "query-accurate");
			header.putOpt("token", qupobj.token);

			
			JSONObject body = new JSONObject();
			body.putOpt("opt",2);

			JSONObject param = new JSONObject();
			param.putOpt("type",qupobj.type.ordinal());
			if(qupobj.type == TypeEnum.CELLPHONE){
				param.putOpt("cellphone",qupobj.cellphone);
			}else if(qupobj.type == TypeEnum.UNIQUE_ID){
				param.putOpt("unique_id",qupobj.unique_id);
			}else if(qupobj.type == TypeEnum.USERNAME){
				param.putOpt("username",qupobj.username);
			}else{
				throw new RuntimeException("code error:"+qupobj.type.ordinal()+"is not support in query user precise");		
			}
			
			body.put("param",param);
			
			JSONObject data = new JSONObject();
			data.put("header", header);
			data.put("body", body);

			return data;
	}
	
	public boolean query_user_precise(QueryUserPreciseCoreObj qupobj){			
		try{
			JSONObject  data = constructQueryUserPreciseJson(qupobj);

			HttpResponse response = httpCmdStart(URL_queryUserPrecise,data);
			
			statusCode = response.getStatusLine().getStatusCode();
			
			if(isHttpOK()){
				String rev = EntityUtils.toString(response.getEntity());
				SamLog.i(TAG,"rev:" + rev);
				
				JSONObject obj = new JSONObject(rev); 
				ret = obj.getInt("ret");

				if(isRetOK()){
					users = new MultipleUserProfile();
					int count = obj.getInt("count");
					if(count > 0){
						JSONArray jsonArrayX = obj.getJSONArray("users");
						parseUsersJson(users,jsonArrayX);
					}
				}
				
				return true;
				
			}else{
				SamLog.i(TAG,"query user precise http status code:"+statusCode);
				return false;
			}
		
		}catch (JSONException e) {  
			exception = true;
			e.printStackTrace();
			SamLog.e(TAG,"query user precise:JSONException");
			return false;
		} catch (ClientProtocolException e) {
			exception = true;
			SamLog.e(TAG,"query user precise:ClientProtocolException");
			e.printStackTrace(); 
			return false;
		} catch (IOException e) { 
			exception = true;
			SamLog.e(TAG,"query user precise:IOException");
			e.printStackTrace(); 
			return false;
		} catch (Exception e) { 
			exception = true;
			SamLog.e(TAG,"query user precise:Exception");
			e.printStackTrace(); 
			return false;
		}
		
	}

	private JSONObject constructQueryUserMultipleJson(QueryUserMultipleCoreObj qumobj) throws JSONException{
			JSONObject header = new JSONObject();
			header.putOpt("action", "query-group");
			header.putOpt("token", qumobj.token);
			
			JSONObject body = new JSONObject();
			body.putOpt("opt",3);
			JSONObject param = new JSONObject();
			JSONArray jsonArrayX = new JSONArray();
			for(long id : qumobj.unique_id_list){
				jsonArrayX.put(id);
			}
			param.put("unique_id",jsonArrayX);
			body.put("param",param);
			
			JSONObject data = new JSONObject();
			data.put("header", header);
			data.put("body", body);

			return data;
	}
	
	public boolean query_user_multiple(QueryUserMultipleCoreObj qumobj){			
		try{
			JSONObject  data = constructQueryUserMultipleJson(qumobj);

			HttpResponse response = httpCmdStart(URL,data);
			
			statusCode = response.getStatusLine().getStatusCode();
			
			if(isHttpOK()){
				String rev = EntityUtils.toString(response.getEntity());
				SamLog.i(TAG,"rev:" + rev);
				
				JSONObject obj = new JSONObject(rev); 
				ret = obj.getInt("ret");

				if(isRetOK()){
					users = new MultipleUserProfile();
					int count = obj.getInt("count");
					if(count > 0){
						JSONArray jsonArrayX = obj.getJSONArray("users");
						parseUsersJson(users,jsonArrayX);
					}
				}
				
				return true;
				
			}else{
				SamLog.i(TAG,"query user multiple http status code:"+statusCode);
				return false;
			}
		
		}catch (JSONException e) {  
			exception = true;
			e.printStackTrace();
			SamLog.e(TAG,"query user multiple:JSONException");
			return false;
		} catch (ClientProtocolException e) {
			exception = true;
			SamLog.e(TAG,"query user multiple:ClientProtocolException");
			e.printStackTrace(); 
			return false;
		} catch (IOException e) { 
			exception = true;
			SamLog.e(TAG,"query user multiple:IOException");
			e.printStackTrace(); 
			return false;
		} catch (Exception e) { 
			exception = true;
			SamLog.e(TAG,"query user multiple:Exception");
			e.printStackTrace(); 
			return false;
		}
		
	}

	private JSONObject constructQueryUserWithoutTokenJson(QueryUserWithoutTokenCoreObj quwobj) throws JSONException{
			JSONObject header = new JSONObject();
			header.putOpt("action", "query-without-token");
			
			JSONObject body = new JSONObject();
			body.putOpt("opt",4);

			JSONObject param = new JSONObject();
			param.putOpt("type",quwobj.type.ordinal());
			if(quwobj.type == TypeEnum.CELLPHONE){
				param.putOpt("countrycode",quwobj.countrycode);
				param.putOpt("cellphone",quwobj.cellphone);
			}else if(quwobj.type == TypeEnum.USERNAME){
				param.putOpt("username",quwobj.username);
			}else{
				throw new RuntimeException("code error:"+quwobj.type.ordinal()+"is not support in query user without token");		
			}
			body.put("param",param);
			
			JSONObject data = new JSONObject();
			data.put("header", header);
			data.put("body", body);

			return data;
	}
	
	public boolean query_user_without_token(QueryUserWithoutTokenCoreObj quwobj){			
		try{
			JSONObject  data = constructQueryUserWithoutTokenJson(quwobj);

			HttpResponse response = httpCmdStart(URL,data);
			
			statusCode = response.getStatusLine().getStatusCode();
			
			if(isHttpOK()){
				String rev = EntityUtils.toString(response.getEntity());
				SamLog.i(TAG,"rev:" + rev);
				
				JSONObject obj = new JSONObject(rev); 
				ret = obj.getInt("ret");

				if(isRetOK()){
					users = new MultipleUserProfile();
					users.setcount(obj.getInt("count"));
				}
				
				return true;
				
			}else{
				SamLog.i(TAG,"query user without token http status code:"+statusCode);
				return false;
			}
		
		}catch (JSONException e) {  
			exception = true;
			e.printStackTrace();
			SamLog.e(TAG,"query user without token:JSONException");
			return false;
		} catch (ClientProtocolException e) {
			exception = true;
			SamLog.e(TAG,"query user without token:ClientProtocolException");
			e.printStackTrace(); 
			return false;
		} catch (IOException e) { 
			exception = true;
			SamLog.e(TAG,"query user without token:IOException");
			e.printStackTrace(); 
			return false;
		} catch (Exception e) { 
			exception = true;
			SamLog.e(TAG,"query user without token:Exception");
			e.printStackTrace(); 
			return false;
		}
		
	}

	private JSONObject constructSendInviteMsgJson(SendInviteMsgCoreObj simobj) throws JSONException{
			JSONObject header = new JSONObject();
			header.putOpt("action", "send-invite-msg");
			header.putOpt("token", simobj.token);
			
			JSONObject body = new JSONObject();
			JSONArray jsonArrayX = new JSONArray();
			for(PhoneNumber phone:simobj.phones){
				JSONObject jo = new JSONObject();
				jo.putOpt("countrycode",phone.countrycode);
				jo.putOpt("cellphone",phone.cellphone);
				jsonArrayX.put(jo);
			}
			body.put("phones",jsonArrayX);
			body.putOpt("msg",simobj.msg);	
			
			JSONObject data = new JSONObject();
			data.put("header", header);
			data.put("body", body);

			return data;
	}
	
	public boolean send_invite_msg(SendInviteMsgCoreObj simobj){			
		try{
			JSONObject  data = constructSendInviteMsgJson(simobj);

			HttpResponse response = httpCmdStart(URL_sendInviteMsg,data);
			
			statusCode = response.getStatusLine().getStatusCode();
			
			if(isHttpOK()){
				String rev = EntityUtils.toString(response.getEntity());
				SamLog.i(TAG,"rev:" + rev);
				
				JSONObject obj = new JSONObject(rev); 
				ret = obj.getInt("ret");
				
				return true;
				
			}else{
				SamLog.i(TAG,"send invite msg http status code:"+statusCode);
				return false;
			}
		
		}catch (JSONException e) {  
			exception = true;
			e.printStackTrace();
			SamLog.e(TAG,"send invite msg:JSONException");
			return false;
		} catch (ClientProtocolException e) {
			exception = true;
			SamLog.e(TAG,"send invite msg:ClientProtocolException");
			e.printStackTrace(); 
			return false;
		} catch (IOException e) { 
			exception = true;
			SamLog.e(TAG,"send invite msg:IOException");
			e.printStackTrace(); 
			return false;
		} catch (Exception e) { 
			exception = true;
			SamLog.e(TAG,"send invite msg:Exception");
			e.printStackTrace(); 
			return false;
		}
		
	}

	private JSONObject constructEditProfileJson(EditProfileCoreObj epobj) throws JSONException{
			JSONObject header = new JSONObject();
			header.putOpt("action", "profile-update");
			header.putOpt("token", epobj.token);

			JSONObject user = new JSONObject();
			if(epobj.user.getcountrycode()!=null){
				user.putOpt("countrycode",epobj.user.getcountrycode());
			}
			if(epobj.user.getcellphone()!=null){
				user.putOpt("cellphone",epobj.user.getcellphone());
			}
			if(epobj.user.getemail()!=null){
				user.putOpt("email",epobj.user.getemail());
			}
			if(epobj.user.getaddress()!=null){
				user.putOpt("address",epobj.user.getaddress());
			}

			if(epobj.user.getusertype() == Constants.SAM_PROS){
				JSONObject sam_pros_info = new JSONObject();
				if(epobj.user.getcompany_name() != null){
					sam_pros_info.putOpt("company_name",epobj.user.getcompany_name());
				}
				if(epobj.user.getservice_category() != null){
					sam_pros_info.putOpt("service_category",epobj.user.getservice_category());
				}
				if(epobj.user.getservice_description() != null){
					sam_pros_info.putOpt("service_description",epobj.user.getservice_description());
				}
				if(epobj.user.getcountrycode_sp() != null){
					sam_pros_info.putOpt("countrycode",epobj.user.getcountrycode_sp());
				}
				if(epobj.user.getphone_sp() != null){
					sam_pros_info.putOpt("phone",epobj.user.getphone_sp());
				}
				if(epobj.user.getemail_sp() != null){
					sam_pros_info.putOpt("email",epobj.user.getemail_sp());
				}
				if(epobj.user.getaddress_sp() != null){
					sam_pros_info.putOpt("address",epobj.user.getaddress_sp());
				}
				user.put("sam_pros_info",sam_pros_info);
			}
			
			JSONObject body = new JSONObject();
			body.put("user",user);
			
			JSONObject data = new JSONObject();
			data.put("header", header);
			data.put("body", body);

			return data;
	}
	
	public boolean edit_profile(EditProfileCoreObj epobj){			
		try{
			JSONObject  data = constructEditProfileJson(epobj);

			HttpResponse response = httpCmdStart(URL_editProfile,data);
			
			statusCode = response.getStatusLine().getStatusCode();
			
			if(isHttpOK()){
				String rev = EntityUtils.toString(response.getEntity());
				SamLog.i(TAG,"rev:" + rev);
				
				JSONObject obj = new JSONObject(rev); 
				ret = obj.getInt("ret");
				if(isRetOK()){
					userinfo = epobj.user;
					JSONObject user = obj.getJSONObject("user");
					userinfo.setlastupdate(user.getLong("lastupdate"));
				}
				
				return true;
				
			}else{
				SamLog.i(TAG,"edit profile http status code:"+statusCode);
				return false;
			}
		
		}catch (JSONException e) {  
			exception = true;
			e.printStackTrace();
			SamLog.e(TAG,"edit profile:JSONException");
			return false;
		} catch (ClientProtocolException e) {
			exception = true;
			SamLog.e(TAG,"edit profile:ClientProtocolException");
			e.printStackTrace(); 
			return false;
		} catch (IOException e) { 
			exception = true;
			SamLog.e(TAG,"edit profile:IOException");
			e.printStackTrace(); 
			return false;
		} catch (Exception e) { 
			exception = true;
			SamLog.e(TAG,"edit profile:Exception");
			e.printStackTrace(); 
			return false;
		}
		
	}


	private JSONObject constructUpdateAvatarJson(UpdateAvatarCoreObj uaobj) throws JSONException{
			JSONObject header = new JSONObject();
			header.putOpt("action", "avatar-update");
			header.putOpt("token", uaobj.token);
			
			JSONObject body = new JSONObject();
			
			JSONObject avatar = new JSONObject();
			if(uaobj.user.getavatar() != null){
				avatar.putOpt("thumb", uaobj.user.getavatar());
			}

			if(uaobj.user.getavatar_original() != null){
				avatar.putOpt("origin",uaobj.user.getavatar_original());
			}

			body.put("avatar",avatar);
			
			JSONObject data = new JSONObject();
			data.put("header", header);
			data.put("body", body);

			return data;
	}
	
	public boolean update_avatar(UpdateAvatarCoreObj uaobj){			
		try{
			JSONObject  data = constructUpdateAvatarJson(uaobj);

			HttpResponse response = httpCmdStart(URL_updateAvatar,data);
			
			statusCode = response.getStatusLine().getStatusCode();
			
			if(isHttpOK()){
				String rev = EntityUtils.toString(response.getEntity());
				SamLog.i(TAG,"rev:" + rev);
				
				JSONObject obj = new JSONObject(rev); 
				ret = obj.getInt("ret");

				if(isRetOK()){
					JSONObject user = obj.getJSONObject("user");
					userinfo = uaobj.user;
					userinfo.setavatar(getThumb(user));
					userinfo.setlastupdate(user.getLong("lastupdate"));
				}
				
				return true;
				
			}else{
				SamLog.i(TAG,"update avatar http status code:"+statusCode);
				return false;
			}
		
		}catch (JSONException e) {  
			exception = true;
			e.printStackTrace();
			SamLog.e(TAG,"update avatar:JSONException");
			return false;
		} catch (ClientProtocolException e) {
			exception = true;
			SamLog.e(TAG,"update avatar:ClientProtocolException");
			e.printStackTrace(); 
			return false;
		} catch (IOException e) { 
			exception = true;
			SamLog.e(TAG,"update avatar:IOException");
			e.printStackTrace(); 
			return false;
		} catch (Exception e) { 
			exception = true;
			SamLog.e(TAG,"update avatar:Exception");
			e.printStackTrace(); 
			return false;
		}
		
	}


	private JSONObject constructQueryPublicJson(QueryPublicCoreObj qpobj) throws JSONException{
			JSONObject header = new JSONObject();
			header.putOpt("action", "public-query");
			header.putOpt("token", qpobj.token);
			
			JSONObject body = new JSONObject();
			if(qpobj.key != null){
				body.putOpt("key",qpobj.key);
			}

			JSONObject location = new JSONObject();
			boolean location_existed = false;
			if(qpobj.longitude != Constants.CONSTANTS_LONGITUDE_LATITUDE_NULL
				&& qpobj.latitude != Constants.CONSTANTS_LONGITUDE_LATITUDE_NULL){
				JSONObject location_info = new JSONObject();
				location_info.putOpt("longitude",qpobj.longitude);
				location_info.putOpt("latitude",qpobj.latitude);
				location.put("location_info",location_info);
				location_existed = true;
			}

			if(qpobj.place_id != null){
				location.putOpt("place_id",qpobj.place_id);
				location_existed = true;
			}

			if(qpobj.address != null){
				location.putOpt("address",qpobj.address);
				location_existed = true;
			}

			if(location_existed){
				body.put("location",location);
			}
			
			
			JSONObject data = new JSONObject();
			data.put("header", header);
			data.put("body", body);

			return data;
	}
	
	public boolean query_public(QueryPublicCoreObj qpobj){			
		try{
			JSONObject  data = constructQueryPublicJson(qpobj);

			HttpResponse response = httpCmdStart(URL_queryPublic,data);
			
			statusCode = response.getStatusLine().getStatusCode();
			
			if(isHttpOK()){
				String rev = EntityUtils.toString(response.getEntity());
				SamLog.i(TAG,"rev:" + rev);
				
				JSONObject obj = new JSONObject(rev); 
				ret = obj.getInt("ret");

				if(isRetOK()){
					users = new MultipleUserProfile();
					int count = obj.getInt("count");
					if(count > 0){
						JSONArray jsonArrayX = obj.getJSONArray("users");
						parseUsersJson(users,jsonArrayX);
					}
				}
				return true;
			}else{
				SamLog.i(TAG,"query public http status code:"+statusCode);
				return false;
			}
		}catch (JSONException e) {  
			exception = true;
			e.printStackTrace();
			SamLog.e(TAG,"query public:JSONException");
			return false;
		} catch (ClientProtocolException e) {
			exception = true;
			SamLog.e(TAG,"query public:ClientProtocolException");
			e.printStackTrace(); 
			return false;
		} catch (IOException e) { 
			exception = true;
			SamLog.e(TAG,"query public:IOException");
			e.printStackTrace(); 
			return false;
		} catch (Exception e) { 
			exception = true;
			SamLog.e(TAG,"query public:Exception");
			e.printStackTrace(); 
			return false;
		}
		
	}

	private JSONObject constructAddContactJson(ContactCoreObj opobj) throws JSONException{
			JSONObject header = new JSONObject();
			header.putOpt("action", "contact");
			header.putOpt("token", opobj.token);
			
			JSONObject body = new JSONObject();
			body.putOpt("opt",opobj.opt);
			body.putOpt("type",opobj.type);
			body.putOpt("id",opobj.user.getunique_id());
			
			JSONObject data = new JSONObject();
			data.put("header", header);
			data.put("body", body);

			return data;
	}
	
	public boolean add_contact(ContactCoreObj opobj){
		try{
			JSONObject  data = constructAddContactJson(opobj);

			HttpResponse response = httpCmdStart(URL_contact,data);
			
			statusCode = response.getStatusLine().getStatusCode();
			
			if(isHttpOK()){
				String rev = EntityUtils.toString(response.getEntity());
				SamLog.i(TAG,"rev:" + rev);
				
				JSONObject obj = new JSONObject(rev); 
				ret = obj.getInt("ret");
				userinfo = opobj.user;
				return true;
				
			}else{
				SamLog.i(TAG,"add contact http status code:"+statusCode);
				return false;
			}
		
		}catch (JSONException e) {  
			exception = true;
			e.printStackTrace();
			SamLog.e(TAG,"add contact:JSONException");
			return false;
		} catch (ClientProtocolException e) {
			exception = true;
			SamLog.e(TAG,"add contact:ClientProtocolException");
			e.printStackTrace(); 
			return false;
		} catch (IOException e) { 
			exception = true;
			SamLog.e(TAG,"add contact:IOException");
			e.printStackTrace(); 
			return false;
		} catch (Exception e) { 
			exception = true;
			SamLog.e(TAG,"add contact:Exception");
			e.printStackTrace(); 
			return false;
		}
		
	}

	private JSONObject constructRemoveContactJson(ContactCoreObj opobj) throws JSONException{
			JSONObject header = new JSONObject();
			header.putOpt("action", "contact");
			header.putOpt("token", opobj.token);
			
			JSONObject body = new JSONObject();
			body.putOpt("opt",opobj.opt);
			body.putOpt("type",opobj.type);
			body.putOpt("id",opobj.user.getunique_id());
			
			JSONObject data = new JSONObject();
			data.put("header", header);
			data.put("body", body);

			return data;
	}
	
	public boolean remove_contact(ContactCoreObj opobj){
		try{
			JSONObject  data = constructRemoveContactJson(opobj);

			HttpResponse response = httpCmdStart(URL,data);
			
			statusCode = response.getStatusLine().getStatusCode();
			
			if(isHttpOK()){
				String rev = EntityUtils.toString(response.getEntity());
				SamLog.i(TAG,"rev:" + rev);
				
				JSONObject obj = new JSONObject(rev); 
				ret = obj.getInt("ret");
				userinfo = opobj.user;
				return true;
				
			}else{
				SamLog.i(TAG,"remove contact http status code:"+statusCode);
				return false;
			}
		
		}catch (JSONException e) {  
			exception = true;
			e.printStackTrace();
			SamLog.e(TAG,"remove contact:JSONException");
			return false;
		} catch (ClientProtocolException e) {
			exception = true;
			SamLog.e(TAG,"remove contact:ClientProtocolException");
			e.printStackTrace(); 
			return false;
		} catch (IOException e) { 
			exception = true;
			SamLog.e(TAG,"remove contact:IOException");
			e.printStackTrace(); 
			return false;
		} catch (Exception e) { 
			exception = true;
			SamLog.e(TAG,"remove contact:Exception");
			e.printStackTrace(); 
			return false;
		}
		
	}



	private JSONObject constructSyncContactListJson(SyncContactListCoreObj scobj) throws JSONException{
			JSONObject header = new JSONObject();
			header.putOpt("action", "contact-list-query");
			header.putOpt("token", scobj.token);
			
			JSONObject body = new JSONObject();
			if(!scobj.isCustomer){
				body.putOpt("type",0);
			}else{
				body.putOpt("type",1);
			}
			
			JSONObject data = new JSONObject();
			data.put("header", header);
			data.put("body", body);

			return data;
	}
	
	public boolean sync_contact_list(SyncContactListCoreObj sflobj){
		try{
			JSONObject  data = constructSyncContactListJson(sflobj);

			HttpResponse response = httpCmdStart(URL_synccontact,data);
			
			statusCode = response.getStatusLine().getStatusCode();
			
			if(isHttpOK()){
				String rev = EntityUtils.toString(response.getEntity());
				SamLog.i(TAG,"rev:" + rev);
				
				JSONObject obj = new JSONObject(rev); 
				ret = obj.getInt("ret");

				if(isRetOK()){
					contacts = new MultipleContact();
					int count = obj.getInt("count");
					if(count > 0){
						JSONArray jsonArrayX = obj.getJSONArray("users");
						for (int i = 0; i < jsonArrayX.length(); i++) {
							JSONObject user = (JSONObject) jsonArrayX.get(i);
							Contact contact = new Contact(user.getLong("id"),user.getString("username"),getAvatarThumb(user));
							contact.setlastupdate(user.getLong("lastupdate"));
							if(user.getInt("type") == Constants.SAM_PROS){
								JSONObject sam_pros = user.getJSONObject("sam_pros_info");
								contact.setservice_category(sam_pros.getString("service_category"));
							}

							contacts.addcontact(contact);
						
						}

						contacts.setcount(jsonArrayX.length());
					}
					
				}
				
				return true;
				
			}else{
				SamLog.i(TAG,"sync contact list http status code:"+statusCode);
				return false;
			}
		
		}catch (JSONException e) {  
			exception = true;
			e.printStackTrace();
			SamLog.e(TAG,"sync contact list:JSONException");
			return false;
		} catch (ClientProtocolException e) {
			exception = true;
			SamLog.e(TAG,"sync contact list:ClientProtocolException");
			e.printStackTrace(); 
			return false;
		} catch (IOException e) { 
			exception = true;
			SamLog.e(TAG,"sync contact list:IOException");
			e.printStackTrace(); 
			return false;
		} catch (Exception e) { 
			exception = true;
			SamLog.e(TAG,"sync contact list:Exception");
			e.printStackTrace(); 
			return false;
		}
		
	}



	private JSONObject constructSyncFollowListJson(SyncFollowListCoreObj sflobj) throws JSONException{
			JSONObject header = new JSONObject();
			header.putOpt("action", "follow-list-query");
			header.putOpt("token", sflobj.token);
			
			JSONObject body = new JSONObject();
			
			JSONObject data = new JSONObject();
			data.put("header", header);
			data.put("body", body);

			return data;
	}
	
	public boolean sync_follow_list(SyncFollowListCoreObj sflobj){			
		try{
			JSONObject  data = constructSyncFollowListJson(sflobj);

			HttpResponse response = httpCmdStart(URL_syncFollowList,data);
			
			statusCode = response.getStatusLine().getStatusCode();
			
			if(isHttpOK()){
				String rev = EntityUtils.toString(response.getEntity());
				SamLog.i(TAG,"rev:" + rev);
				
				JSONObject obj = new JSONObject(rev); 
				ret = obj.getInt("ret");

				if(isRetOK()){
					followusers = new MultipleFollowUser();
					int count = obj.getInt("count");
					if(count > 0){
						JSONArray jsonArrayX = obj.getJSONArray("users");
						for (int i = 0; i < jsonArrayX.length(); i++) {
							JSONObject user = (JSONObject) jsonArrayX.get(i);
							FollowedSamPros fsp = new FollowedSamPros();
							fsp.setunique_id(user.getLong("id"));
							fsp.setusername(user.getString("username"));
							fsp.setavatar(getAvatarThumb(user));
							fsp.setlastupdate(user.getLong("lastupdate"));

							JSONObject sam_pros = user.getJSONObject("sam_pros_info");
							fsp.setservice_category(sam_pros.getString("service_category"));
							fsp.setfavourite_tag(user.getInt("favourite_tag"));
							fsp.setblock_tag(user.getInt("block_tag"));

							followusers.addsp(fsp);
						}

						followusers.setcount(jsonArrayX.length());
					}
					
				}
				
				return true;
				
			}else{
				SamLog.i(TAG,"sync follow list http status code:"+statusCode);
				return false;
			}
		
		}catch (JSONException e) {  
			exception = true;
			e.printStackTrace();
			SamLog.e(TAG,"sync follow list:JSONException");
			return false;
		} catch (ClientProtocolException e) {
			exception = true;
			SamLog.e(TAG,"sync follow list:ClientProtocolException");
			e.printStackTrace(); 
			return false;
		} catch (IOException e) { 
			exception = true;
			SamLog.e(TAG,"sync follow list:IOException");
			e.printStackTrace(); 
			return false;
		} catch (Exception e) { 
			exception = true;
			SamLog.e(TAG,"sync follow list:Exception");
			e.printStackTrace(); 
			return false;
		}
		
	}

	private JSONObject constructWriteAdvJson(AdvCoreObj advobj) throws JSONException{
			JSONObject header = new JSONObject();
			header.putOpt("action", "advertisement-write");
			header.putOpt("token", advobj.token);
			
			JSONObject body = new JSONObject();
			body.putOpt("type",advobj.type);
			body.putOpt("content",advobj.content);
			if(advobj.content_thumb != null){
				body.putOpt("content_thumb",advobj.content_thumb);
			}
			
			JSONObject data = new JSONObject();
			data.put("header", header);
			data.put("body", body);

			return data;
	}
	
	public boolean write_advertisement(AdvCoreObj advobj) {
        try {
            JSONObject data = constructWriteAdvJson(advobj);

            HttpResponse response = httpCmdStart(URL_writeAdvertisement, data);

            statusCode = response.getStatusLine().getStatusCode();

            if (isHttpOK()) {
                String rev = EntityUtils.toString(response.getEntity());
                SamLog.i(TAG, "rev:" + rev);

                JSONObject obj = new JSONObject(rev);
                ret = obj.getInt("ret");

                if (isRetOK()) {
                    adv = new Advertisement(advobj.type, advobj.content, advobj.content_thumb, advobj.sender_unique_id);
                    adv.setadv_id(obj.getLong("adv_id"));
                    adv.setpublish_timestamp(obj.getLong("publish_timestamp"));
                }

                return true;

            } else {
                SamLog.i(TAG, "write adv http status code:" + statusCode);
                return false;
            }

        } catch (JSONException e) {
            exception = true;
            e.printStackTrace();
            SamLog.e(TAG, "write adv :JSONException");
            return false;
        } catch (ClientProtocolException e) {
            exception = true;
            SamLog.e(TAG, "write adv :ClientProtocolException");
            e.printStackTrace();
            return false;
        } catch (IOException e) {
            exception = true;
            SamLog.e(TAG, "write adv :IOException");
            e.printStackTrace();
            return false;
        } catch (Exception e) {
            exception = true;
            SamLog.e(TAG, "write adv :Exception");
            e.printStackTrace();
            return false;
        }

    }

	private JSONObject constructDeleteAdvJson(DeleteAdvCoreObj advobj) throws JSONException{
			JSONObject header = new JSONObject();
			header.putOpt("action", "advertisement-delete");
			header.putOpt("token", advobj.token);
			
			JSONObject body = new JSONObject();
			JSONArray jsonArrayX = new JSONArray();
			for(long id : advobj.advs){
				jsonArrayX.put(id);
			}
			
			body.put("adv_ids",jsonArrayX);
			
			JSONObject data = new JSONObject();
			data.put("header", header);
			data.put("body", body);

			return data;
	}
	
	public boolean delete_advertisement(DeleteAdvCoreObj advobj){			
		try{
			JSONObject  data = constructDeleteAdvJson(advobj);

			HttpResponse response = httpCmdStart(URL,data);
			
			statusCode = response.getStatusLine().getStatusCode();
			
			if(isHttpOK()){
				String rev = EntityUtils.toString(response.getEntity());
				SamLog.i(TAG,"rev:" + rev);
				
				JSONObject obj = new JSONObject(rev); 
				ret = obj.getInt("ret");
				
				return true;
				
			}else{
				SamLog.i(TAG,"delete adv http status code:"+statusCode);
				return false;
			}
		
		}catch (JSONException e) {  
			exception = true;
			e.printStackTrace();
			SamLog.e(TAG,"delete adv :JSONException");
			return false;
		} catch (ClientProtocolException e) {
			exception = true;
			SamLog.e(TAG,"delete adv :ClientProtocolException");
			e.printStackTrace(); 
			return false;
		} catch (IOException e) { 
			exception = true;
			SamLog.e(TAG,"delete adv :IOException");
			e.printStackTrace(); 
			return false;
		} catch (Exception e) { 
			exception = true;
			SamLog.e(TAG,"delete adv :Exception");
			e.printStackTrace(); 
			return false;
		}
		
	}

	private byte[] readStream(InputStream inStream) throws Exception{
        ByteArrayOutputStream outStream = new ByteArrayOutputStream();
        byte[] buffer = new byte[1024];   
        int len = 0;   
        while( (len=inStream.read(buffer)) != -1){   
            outStream.write(buffer, 0, len);   
        }   
        outStream.close();   
        inStream.close();   
        return outStream.toByteArray();   
    } 

	public boolean download(com.android.samservice.DownloadCoreObj dlobj){
		HttpURLConnection conn =null;
		String path = dlobj.url;
		try{
			java.net.URL url = new java.net.URL(path);   
			conn = (HttpURLConnection) url.openConnection();
			conn.setRequestMethod("GET");   
			InputStream inStream = conn.getInputStream();   
			if(conn.getResponseCode() == HttpURLConnection.HTTP_OK){   
				data = readStream(inStream); 
				return true;
			}
			return false;
		}catch(Exception e){
			e.printStackTrace();
			return false;
		}finally{
			try {
				if(conn!=null) conn.disconnect();
			} catch (Exception e) {
			}
		}
	}

/*******************************Http Push Parse**********************************************/
	public int parsePushJson(String data){
		int category = Constants.PUSH_CATEGORY_UNKONW;
		try{
			JSONObject obj = new JSONObject(data);
			JSONObject header = obj.getJSONObject("header");
			JSONObject body = obj.getJSONObject("body");
			
			category = header.getInt("category");

			long dest_id;
			switch(category){
				case Constants.PUSH_CATEGORY_QUESTION:
					dest_id = body.getLong("dest_id");
					if(dest_id != SamService.getInstance().get_current_user().getunique_id()){
						return Constants.PUSH_CATEGORY_UNKONW;
					}
					rq = parseReceivedQuestionJson(body);
					userinfo = parseUserJson(body.getJSONObject("user"));
				break;

				case Constants.PUSH_CATEGORY_ADV:
                    dest_id = body.getLong("dest_id");
					if(dest_id != SamService.getInstance().get_current_user().getunique_id()){
						return Constants.PUSH_CATEGORY_UNKONW;
					}
					adv = parseReceivedAdvertisementJson(body);
				break;

				default:
					category = Constants.PUSH_CATEGORY_UNKONW;
				break;
			}

			return category;
		
		}catch (JSONException e) {  
			exception = true;
			e.printStackTrace();
			SamLog.e(TAG,"parsePushJson :JSONException");
			return Constants.PUSH_CATEGORY_UNKONW;
		}catch (Exception e) {
			exception = true;
			SamLog.e(TAG,"parsePushJson :Exception");
			e.printStackTrace(); 
			return Constants.PUSH_CATEGORY_UNKONW;
		}
	}


}
