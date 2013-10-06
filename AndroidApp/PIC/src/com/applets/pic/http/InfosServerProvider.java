package com.applets.pic.http;

import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

import android.location.LocationManager;

public class InfosServerProvider {
	
	private double latitude;
	private double longitude;
	private boolean positionHasBenSet = false;
	private LocationManager locManager;
	
	public InfosServerProvider(LocationManager locManager) {		
		this.locManager = locManager;
		
		/* Use the LocationManager class to obtain GPS locations */
	    /*locManager.requestLocationUpdates( LocationManager.GPS_PROVIDER, 0, 0, new LocationListener() {
			
			@Override
			public void onStatusChanged(String arg0, int arg1, Bundle arg2) {
				Log.i("Location", "StatusChanged to " + String.valueOf(arg1));
			}
			
			@Override
			public void onProviderEnabled(String arg0) {
				Log.i("Location", "ProviderEnabled");
			}
			
			@Override
			public void onProviderDisabled(String arg0) {
				// TODO Tell the user he has to put it back on
				Log.i("Location", "ProviderDisabled");
			}
			
			@Override
			public void onLocationChanged(Location location) {
				latitude = location.getLatitude();
				longitude = location.getLongitude();
				positionHasBenSet = true;
				Log.i("Location", "Lat: " + String.valueOf(latitude) + " Long:" + String.valueOf(longitude));
				
			}
		});*/
	}

	public String[] getClosestServerInfos() {
		HttpReader task = (HttpReader)new HttpReader().execute("http://18.111.95.249/php/api.php?method=getDefaultChannel&lat=42.357&lon=-71.0901");
		String[] availableChannels = new String[] {};
		String serverInfos;
		try {
			serverInfos = task.get(15, TimeUnit.SECONDS);
			if(serverInfos != null && !serverInfos.isEmpty()) {
				availableChannels = serverInfos.split(",");
				return availableChannels;
			}
		} catch (TimeoutException e) {
			e.printStackTrace();
		}catch (Exception e) {
			e.printStackTrace();
		}
		return availableChannels;
	}
	
//	public String[] getSubChannels(String channel) {
//		
//	}
	
}
