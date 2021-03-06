package com.android.samchat.factory;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.location.LocationProvider;
import android.os.Bundle;
import android.telephony.TelephonyManager;
import android.content.Context;
import android.telephony.cdma.CdmaCellLocation;
import android.telephony.gsm.GsmCellLocation;
import android.text.TextUtils;
import android.widget.Toast;

import java.util.UUID;

import com.android.samchat.common.SCell;
import com.netease.nim.uikit.common.util.log.LogUtil;
import com.netease.nim.uikit.common.util.string.StringUtil;
import com.netease.nim.demo.DemoCache;
public class LocationFactory {
	private static final String TAG="SamchatLocationFactory";
	private static LocationFactory factory;

	private Location currentBestLocation;
	private LocationManager lm;
	private GPSLocationListener gpsListener;
	private NetworkLocationListener networkListener;

	private boolean monitorStart;
	
	public LocationFactory(){
		 lm = (LocationManager) DemoCache.getContext().getSystemService(Context.LOCATION_SERVICE);
		 gpsListener = new GPSLocationListener();
		 networkListener = new NetworkLocationListener();
		 monitorStart = false;
	}
	
	public static LocationFactory getInstance(){
		if(factory == null){
			factory = new LocationFactory();
		}
		return factory;
	}

	public Location getCurrentBestLocation(){
		try{
			if(currentBestLocation != null){
				return currentBestLocation;
			}else{
				Location location =  lm.getLastKnownLocation(LocationManager.NETWORK_PROVIDER);
				if(location != null){
					return location;
				}else{
					return lm.getLastKnownLocation(LocationManager.GPS_PROVIDER);
				}
			}
		}catch(Exception e){
			e.printStackTrace(); 
			return null;
		}
	}
	
	public void startLocationMonitor(){
		try{
			if(!monitorStart){
				LogUtil.i(TAG,"start LocationMonitor");
				Toast.makeText(DemoCache.getContext(), "startLocationMonitor", Toast.LENGTH_SHORT).show();
				lm.requestLocationUpdates(LocationManager.NETWORK_PROVIDER, 60*1000 * 2L, 5000F,networkListener); 
				lm.requestLocationUpdates(LocationManager.GPS_PROVIDER,1000 * 2L,100F,gpsListener);
			}
			monitorStart = true;
		}catch(Exception e){
			e.printStackTrace();
		}finally{
			monitorStart = true;
		}
	}

	public void stopLocationMonitor(){
		try{
			if(monitorStart){
				LogUtil.i(TAG,"stop LocationMonitor");
				Toast.makeText(DemoCache.getContext(), "stopLocationMonitor", Toast.LENGTH_SHORT).show();
				lm.removeUpdates(gpsListener);
				lm.removeUpdates(networkListener);
			}
		}catch(Exception e){
			e.printStackTrace();
		}finally{
			monitorStart = false;
		}
	}
	  
	private class GPSLocationListener implements LocationListener {
		private boolean isRemove = false;
		
  		@Override  
		public void onLocationChanged(Location location) {
			try{
				LogUtil.i(TAG,"GPS Location:"+location);
				Toast.makeText(DemoCache.getContext(), "onLocationChanged", Toast.LENGTH_SHORT).show();
				boolean flag = isBetterLocation(location,currentBestLocation);
  
				if (flag) {
					currentBestLocation = location;
				}

				if (location !=null && !isRemove) {
					lm.removeUpdates(networkListener);
					isRemove = true;
				}
			}catch(Exception e){
				e.printStackTrace();
			}
	}  
  
	@Override  
	public void onProviderDisabled(String provider) {
		try{
			LogUtil.i(TAG,"GPS onProviderDisabled");
			Toast.makeText(DemoCache.getContext(), "onProviderDisabled", Toast.LENGTH_SHORT).show();
			if(isRemove){
				lm.requestLocationUpdates(LocationManager.NETWORK_PROVIDER, 0, 10F,networkListener);
				isRemove = false;
			}
		}catch(Exception e){
			e.printStackTrace();
		}
	}  
  
	@Override  
	public void onProviderEnabled(String provider) {  
		LogUtil.i(TAG,"GPS onProviderEnabled");
		Toast.makeText(DemoCache.getContext(), "onProviderEnabled", Toast.LENGTH_SHORT).show();
	}  
  
	@Override  
	public void onStatusChanged(String provider, int status, Bundle extras) {
		try{
			LogUtil.i(TAG,"GPS onStatusChanged");
			Toast.makeText(DemoCache.getContext(), "onStatusChanged", Toast.LENGTH_SHORT).show();
			if (LocationProvider.OUT_OF_SERVICE == status) {
				lm.requestLocationUpdates(LocationManager.NETWORK_PROVIDER, 0, 10F, networkListener);
				isRemove = false;
			}
		}catch(Exception e){
			e.printStackTrace();
		}
	}  
 } 

	private class NetworkLocationListener implements LocationListener {  
  		@Override  
		public void onLocationChanged(Location location) {
			LogUtil.i(TAG,"Network Location:"+location);
			boolean flag =isBetterLocation(location,currentBestLocation);  
  
			if (flag) {  
				currentBestLocation = location;  
			}  
	}  
  
    @Override  
    public void onProviderDisabled(String provider) {  
		LogUtil.i(TAG,"Network onProviderDisabled");
    }  
  
    @Override  
    public void onProviderEnabled(String provider) {  
		LogUtil.i(TAG,"Network onProviderEnabled");
    }  
  
    @Override  
    public void onStatusChanged(String provider, int status, Bundle extras) {
		LogUtil.i(TAG,"Network onStatusChanged");
    }  
 } 


	private static final int TWO_MINUTES = 1000 * 60 * 2;  
/**  
     * Determines whether one Location reading is better than the current  
     * Location fix  
     *   
     * @param location  
     *            The new Location that you want to evaluate  
     * @param currentBestLocation  
     *            The current Location fix, to which you want to compare the new  
     *            one  
     */    
    protected boolean isBetterLocation(Location location,    
            Location currentBestLocation) {    
        if (currentBestLocation == null) {    
            // A new location is always better than no location    
            return true;    
        }    
    
        // Check whether the new location fix is newer or older    
        long timeDelta = location.getTime() - currentBestLocation.getTime();    
        boolean isSignificantlyNewer = timeDelta > TWO_MINUTES;    
        boolean isSignificantlyOlder = timeDelta < -TWO_MINUTES;    
        boolean isNewer = timeDelta > 0;    
    
        // If it's been more than two minutes since the current location, use    
        // the new location    
        // because the user has likely moved    
        if (isSignificantlyNewer) {    
            return true;    
            // If the new location is more than two minutes older, it must be    
            // worse    
        } else if (isSignificantlyOlder) {    
            return false;    
        }    
    
        // Check whether the new location fix is more or less accurate    
        int accuracyDelta = (int) (location.getAccuracy() - currentBestLocation    
                .getAccuracy());    
        boolean isLessAccurate = accuracyDelta > 0;    
        boolean isMoreAccurate = accuracyDelta < 0;    
        boolean isSignificantlyLessAccurate = accuracyDelta > 200;    
    
        // Check if the old and new location are from the same provider    
        boolean isFromSameProvider = isSameProvider(location.getProvider(),    
                currentBestLocation.getProvider());    
    
        // Determine location quality using a combination of timeliness and    
        // accuracy    
        if (isMoreAccurate) {    
            return true;    
        } else if (isNewer && !isLessAccurate) {    
            return true;    
        } else if (isNewer && !isSignificantlyLessAccurate    
                && isFromSameProvider) {    
            return true;    
        }    
        return false;    
    }    
    
    /** Checks whether two providers are the same */    
    private boolean isSameProvider(String provider1, String provider2) {    
        if (provider1 == null) {    
            return provider2 == null;    
        }    
        return provider1.equals(provider2);    
    }

	public SCell getCurrentCellInfo() {
		try{
			SCell cell = new SCell();
 
			TelephonyManager mTelNet = (TelephonyManager) DemoCache.getContext().getSystemService(Context.TELEPHONY_SERVICE);
			if (mTelNet == null){
				return null;
			}
		
			if(mTelNet.getNetworkType() == TelephonyManager.NETWORK_TYPE_GPRS
				|| mTelNet.getNetworkType() == TelephonyManager.NETWORK_TYPE_EDGE){
				cell.radioType = SCell.RADIO_TYPE_GSM;
			}else if(mTelNet.getNetworkType() == TelephonyManager.NETWORK_TYPE_UMTS
				|| mTelNet.getNetworkType() == TelephonyManager.NETWORK_TYPE_HSDPA
				|| mTelNet.getNetworkType() == TelephonyManager.NETWORK_TYPE_HSUPA
				|| mTelNet.getNetworkType() == TelephonyManager.NETWORK_TYPE_HSPA){
				cell.radioType = SCell.RADIO_TYPE_WCDMA;
			}else if(mTelNet.getNetworkType() == TelephonyManager.NETWORK_TYPE_CDMA
				|| mTelNet.getNetworkType() == TelephonyManager.NETWORK_TYPE_1xRTT
				|| mTelNet.getNetworkType() == TelephonyManager.NETWORK_TYPE_EVDO_0
				|| mTelNet.getNetworkType() == TelephonyManager.NETWORK_TYPE_EVDO_A
				|| mTelNet.getNetworkType() == TelephonyManager.NETWORK_TYPE_EVDO_B
				|| mTelNet.getNetworkType() == TelephonyManager.NETWORK_TYPE_EHRPD){
				cell.radioType = SCell.RADIO_TYPE_CDMA;
			}else if(mTelNet.getNetworkType() == TelephonyManager.NETWORK_TYPE_LTE){
				cell.radioType = SCell.RADIO_TYPE_LTE;
			}

			if(mTelNet.getCellLocation() instanceof GsmCellLocation){
				GsmCellLocation location = (GsmCellLocation) mTelNet.getCellLocation();
				if (location == null){
					return null;
    	    	}
				String operator = mTelNet.getNetworkOperator();
				if(TextUtils.isEmpty(operator)){
					return null;
				}
				int mcc = Integer.parseInt(operator.substring(0, 3));
				int mnc = Integer.parseInt(operator.substring(3));
				int cid = location.getCid();
				int lac = location.getLac();
 
				cell.mcc = mcc;
				cell.mnc = mnc;
				cell.lac = lac;
				cell.cid = cid;
 
				return cell;
			}else{
				CdmaCellLocation location = (CdmaCellLocation) mTelNet.getCellLocation();
				if(location == null){
					return null;
				}
			
				String operator = mTelNet.getNetworkOperator();
				if(TextUtils.isEmpty(operator)){
					return null;
        		}
				int mcc = Integer.parseInt(operator.substring(0, 3));
				int mnc = location.getSystemId();
				int cid = location.getBaseStationId();
				int lac = location.getNetworkId();
			
				cell.mcc = mcc;
				cell.mnc = mnc;
				cell.lac = lac;
				cell.cid = cid;
				return cell;
			}
		}catch(Exception e){
			return null;
		}
	}
}