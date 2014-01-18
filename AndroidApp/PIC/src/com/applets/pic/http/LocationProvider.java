package com.applets.pic.http;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

import org.json.JSONArray;
import org.json.JSONObject;

import com.applets.pic.SplashActivity;

import android.content.Context;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Bundle;
import android.util.Log;

public class LocationProvider implements LocationListener {
    private LocationManager mLocationManager;
    private Context context;
    private Location location;

    public LocationProvider(Context context) {
    	this.context = context;
        mLocationManager = (LocationManager) context.getSystemService(Context.LOCATION_SERVICE);
        mLocationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, 0, 500, this);

        Location location = mLocationManager.getLastKnownLocation(LocationManager.GPS_PROVIDER);
        if(location != null && location.getTime() > Calendar.getInstance().getTimeInMillis() - 2 * 60 * 1000) {
            // Do something with the recent location fix 
            //  if it is less than two minutes old,
            //  otherwise wait for the update below
        }
    }
    
    public String[] getClosestServerInfos() {	
    	System.out.println(location);
		HttpReader task = (HttpReader)new HttpReader().execute("http://clubapplets.ca/checkinchat/api.php?method=getDefaultChannel&lat=" + location.getLatitude() + "&lon=" + location.getLongitude());
		ArrayList<String> availableChannels = new ArrayList<String>();
		String serverInfos;
		try {
			serverInfos = task.get(15, TimeUnit.SECONDS);
			if(serverInfos != null && !serverInfos.isEmpty()) {
				JSONArray recs = new JSONArray(serverInfos);
				
				for (int i = 0; i < recs.length(); ++i) {
				    JSONObject rec = recs.getJSONObject(i);
				    availableChannels.add(rec.getString("name"));
				}
				return (String[]) availableChannels.toArray(new String[availableChannels.size()]);
			}
		} catch (TimeoutException e) {
			e.printStackTrace();
		}catch (Exception e) {
			e.printStackTrace();
		}
		return (String[]) availableChannels.toArray(new String[availableChannels.size()]);
	}

    public void onLocationChanged(Location location) {
        if (location != null) {
        	this.location = location;
        	((SplashActivity) context).connection();
            Log.i("Location Changed", location.getLatitude() + " and " + location.getLongitude());
            // You need to call this whenever you are done:
            // mLocationManager.removeUpdates(this);
        }
    }

    // Required functions    
    public void onProviderDisabled(String arg0) {}
    public void onProviderEnabled(String arg0) {}
    public void onStatusChanged(String arg0, int arg1, Bundle arg2) {}
}