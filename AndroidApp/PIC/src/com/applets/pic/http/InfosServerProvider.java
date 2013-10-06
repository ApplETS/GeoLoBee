package com.applets.pic.http;

import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

public class InfosServerProvider {

	public static String[] getClosestServerInfos() {
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
