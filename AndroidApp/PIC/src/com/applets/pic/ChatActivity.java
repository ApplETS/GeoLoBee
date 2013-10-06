package com.applets.pic;

import android.os.Bundle;
import android.app.Activity;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.support.v4.widget.DrawerLayout;
import android.view.Menu;
import android.widget.ArrayAdapter;
import android.widget.ListView;
public class ChatActivity extends Activity {

	private ListView drawerList;
	private String[] availableChannels;
	private String displayName;
	private DrawerLayout drawerLayout;
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_chat);
		displayName = getIntent().getExtras().getString("DISPLAY_NAME");
		availableChannels = getIntent().getExtras().getStringArray("CHANNELS");
		
		drawerList = (ListView)findViewById(R.id.left_drawer);
	
		if(availableChannels != null && availableChannels.length > 0) {
			getActionBar().setTitle(availableChannels[0]);
			drawerList.setAdapter(new ArrayAdapter<String>(this, R.layout.drawer_list_layout, availableChannels));
		}
		
		/* Use the LocationManager class to obtain GPS locations */
//	    LocationManager mlocManager = (LocationManager)getSystemService(Context.LOCATION_SERVICE);
//	    mlocManager.requestLocationUpdates( LocationManager.NETWORK_PROVIDER, 0, 0, new LocationListener() {
//			
//			@Override
//			public void onStatusChanged(String arg0, int arg1, Bundle arg2) {
//				Log.i("Location", "StatusChanged to " + String.valueOf(arg1));
//			}
//			
//			@Override
//			public void onProviderEnabled(String arg0) {
//				Log.i("Location", "ProviderEnabled");
//			}
//			
//			@Override
//			public void onProviderDisabled(String arg0) {
//				// TODO Tell the user he has to put it back on
//				Log.i("Location", "ProviderDisabled");
//			}
//			
//			@Override
//			public void onLocationChanged(Location location) {
//				double latitude = location.getLatitude();
//				double longitude = location.getLongitude();
//				Log.i("Location", "Lat: " + String.valueOf(latitude) + " Long:" + String.valueOf(longitude));
//			}
//		});
		
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		// Inflate the menu; this adds items to the action bar if it is present.
		getMenuInflater().inflate(R.menu.chat, menu);
		return true;
	}
	
	
	private void showServerUnknownError() {
		// Unknown exception
		AlertDialog alertDialog = new AlertDialog.Builder(this).create();
		alertDialog.setTitle(getResources().getString(R.string.error_connection_infos_server_title));
		alertDialog.setMessage(getResources().getString(R.string.error_connection_infos_server_unknown));
		alertDialog.setButton(AlertDialog.BUTTON_POSITIVE, "OK", new DialogInterface.OnClickListener() {
			
			@Override
			public void onClick(DialogInterface arg0, int arg1) {
				Intent intent = new Intent(ChatActivity.this, SplashActivity.class);
				ChatActivity.this.finish();
				startActivity(intent);
			}
		});
	}

}
