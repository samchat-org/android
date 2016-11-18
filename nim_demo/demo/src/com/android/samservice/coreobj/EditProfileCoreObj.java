package com.android.samservice.coreobj;
import com.android.samservice.callback.SMCallBack;
import com.android.samservice.info.ContactUser;
import com.android.samservice.info.PlacesInfo;

public class EditProfileCoreObj extends SamCoreObj{
	public static final int EDIT_PROFILE_TYPE_ALL = 0;
	public static final int EDIT_PROFILE_TYPE_CUSTOMER_EMAIL=1;
	public static final int EDIT_PROFILE_TYPE_CUSTOMER_ADDRESS=2;
	public static final int EDIT_PROFILE_TYPE_SP_EMAIL=3;
	public static final int EDIT_PROFILE_TYPE_SP_ADDRESS=4;

	public static final int EDIT_PROFILE_TYPE_SP_COMPANY_NAME=5;
	public static final int EDIT_PROFILE_TYPE_SP_SERVICE_CATEGORY=6;
	public static final int EDIT_PROFILE_TYPE_SP_SERVICE_DESCRIPTION=7;
	public static final int EDIT_PROFILE_TYPE_SP_PHONE=8;

	public String token;
	public ContactUser user;
	public int type;
	public String data;
	public PlacesInfo place_info;

	public EditProfileCoreObj(SMCallBack callback){
		super(callback);
	}

	public void init(String token, int type, ContactUser user, String data,  PlacesInfo place_info){
		this.token = token;
		this.user = user;
		this.type = type;
		this.data = data;
		this.place_info = place_info;

		switch(type){
			case EDIT_PROFILE_TYPE_CUSTOMER_EMAIL:
				user.setemail(data);
				break;
				
			case EDIT_PROFILE_TYPE_CUSTOMER_ADDRESS:
				user.setaddress(place_info != null? place_info.description : data);
				break;

			case EDIT_PROFILE_TYPE_SP_EMAIL:
				user.setemail_sp(data);
				break;

			case EDIT_PROFILE_TYPE_SP_ADDRESS:
				user.setaddress_sp(place_info != null? place_info.description : data);
				break;

			case EDIT_PROFILE_TYPE_SP_COMPANY_NAME:
				user.setcompany_name(data);
				break;

			case EDIT_PROFILE_TYPE_SP_SERVICE_CATEGORY:
				user.setservice_category(data);
				break;

			case EDIT_PROFILE_TYPE_SP_SERVICE_DESCRIPTION:
				user.setservice_description(data);
				break;
				
			case EDIT_PROFILE_TYPE_SP_PHONE:
				user.setphone_sp(data);
				break;
		}
	}
}
