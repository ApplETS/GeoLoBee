package com.applets.pic.http;

import java.util.ArrayList;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

import org.json.JSONArray;
import org.json.JSONObject;

import android.content.Context;
import android.location.Location;
import android.location.LocationManager;
import android.util.Log;
import android.widget.Toast;

public class InfosServerProvider {
	
	private Location lastLocation;
	private long lastprovidertimestamp;
	private Context context;
	
	public InfosServerProvider(Context context) {		
		this.context = context;
	}
	
	private Location getBestLocation() {
	    Location gpslocation = getLocationByProvider(LocationManager.GPS_PROVIDER);
	    Location networkLocation =
	            getLocationByProvider(LocationManager.NETWORK_PROVIDER);
	    // if we have only one location available, the choice is easy
	    if (gpslocation == null) {
	        Log.d("Erreur", "No GPS Location available.");
	        return networkLocation;
	    }
	    if (networkLocation == null) {
	        Log.d("Erreur", "No Network Location available");
	        return gpslocation;
	    }
	    // a locationupdate is considered 'old' if its older than the configured
	    // update interval. this means, we didn't get a
	    // update from this provider since the last check
	    long old = System.currentTimeMillis() - 5000;
	    boolean gpsIsOld = (gpslocation.getTime() < old);
	    boolean networkIsOld = (networkLocation.getTime() < old);
	    // gps is current and available, gps is better than network
	    if (!gpsIsOld) {
	        Log.d("Error", "Returning current GPS Location");
	        return gpslocation;
	    }
	    // gps is old, we can't trust it. use network location
	    if (!networkIsOld) {
	        Log.d("Error", "GPS is old, Network is current, returning network");
	        return networkLocation;
	    }
	    // both are old return the newer of those two
	    if (gpslocation.getTime() > networkLocation.getTime()) {
	        Log.d("Error", "Both are old, returning gps(newer)");
	        return gpslocation;
	    } else {
	        Log.d("Error", "Both are old, returning network(newer)");
	        return networkLocation;
	    }
	}

	/**
	 * get the last known location from a specific provider (network/gps)
	 */
	private Location getLocationByProvider(String provider) {
	    Location location = null;
	    LocationManager locationManager = (LocationManager) context.getSystemService(Context.LOCATION_SERVICE);
	    try {
	        if (locationManager.isProviderEnabled(provider)) {
	            location = locationManager.getLastKnownLocation(provider);
	        }
	    } catch (IllegalArgumentException e) {
	        Log.d("Error", "Cannot acces Provider " + provider);
	    }
	    return location;
	}
	
	public void doLocationUpdate(Location l, boolean force) {
	    long minDistance = 1000;
	    Log.d("Error", "update received:" + l);
	    if (l == null) {
	        Log.d("Error", "Empty location");
	        if (force)
	        	Toast.makeText(context, "Current location not available",
	        			Toast.LENGTH_SHORT).show();
	        return;
	    }
	    if (lastLocation != null) {
	        float distance = l.distanceTo(lastLocation);
	        Log.d("Error", "Distance to last: " + distance);
	        if (l.distanceTo(lastLocation) < minDistance && !force) {
	            Log.d("Error", "Position didn't change");
	            return;
	        }
	        if (l.getAccuracy() >= lastLocation.getAccuracy()
	                && l.distanceTo(lastLocation) < l.getAccuracy() && !force) {
	            Log.d("Error",
	                    "Accuracy got worse and we are still "
	                      + "within the accuracy range.. Not updating");
	            return;
	        }
	        if (l.getTime() <= lastprovidertimestamp && !force) {
	            Log.d("Error", "Timestamp not never than last");
	            return;
	        }
	    }
	    // upload/store your location here
	}

	public String[] getClosestServerInfos() {
		Location location = getBestLocation();
		HttpReader task = (HttpReader)new HttpReader().execute("http://18.111.95.249/php/api.php?method=getDefaultChannel&lat=" + location.getLatitude() + "&lon=" + location.getLongitude());
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
	
//	public String[] getSubChannels(String channel) {
//		
//	}
	
}
