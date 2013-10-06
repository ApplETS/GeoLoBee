package com.applets.pic;

import com.applets.pic.http.InfosServerProvider;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.support.v4.widget.DrawerLayout;
import android.view.Menu;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.location.LocationManager;
public class ChatActivity extends Activity {

	private ListView drawerList;
	private String[] availableChannels;
	private String displayName;
	private DrawerLayout drawerLayout;
	private InfosServerProvider infosProvider;
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		infosProvider = new InfosServerProvider((LocationManager)getSystemService(Context.LOCATION_SERVICE));
		setContentView(R.layout.activity_chat);
		displayName = getIntent().getExtras().getString("DISPLAY_NAME");
		availableChannels = getIntent().getExtras().getStringArray("CHANNELS");
		
		drawerList = (ListView)findViewById(R.id.left_drawer);
	
		if(availableChannels != null && availableChannels.length > 0) {
			getActionBar().setTitle(availableChannels[0]);
			drawerList.setAdapter(new ArrayAdapter<String>(this, R.layout.drawer_list_layout, availableChannels));
		}
		
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
