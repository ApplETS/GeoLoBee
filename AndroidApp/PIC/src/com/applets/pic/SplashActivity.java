package com.applets.pic;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.view.Menu;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.Window;
import android.widget.Button;
import android.widget.EditText;

import com.applets.pic.http.InfosServerProvider;
import com.applets.pic.http.LocationProvider;

public class SplashActivity extends Activity {

	private Button connectButton;
	private EditText nameEdit;	
	private LocationProvider locationProvider;
	private String[] availableChannels;
	private String name;
	private String firstChannel;
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		
		//Remove title bar
		this.requestWindowFeature(Window.FEATURE_NO_TITLE);
		
		setContentView(R.layout.activity_splash);
		
		firstChannel = "";
		locationProvider = new LocationProvider(this);
		
		nameEdit = (EditText)findViewById(R.id.editName);

		connectButton = (Button)findViewById(R.id.buttonConnect);
		connectButton.setOnClickListener(new Listener());
	}
	
	private void showServerUnknownError() {
		// Unknown exception
		AlertDialog alertDialog = new AlertDialog.Builder(this).create();
		alertDialog.setTitle(getResources().getString(R.string.error_connection_infos_server_title));
		alertDialog.setMessage(getResources().getString(R.string.error_connection_infos_server_unknown));
		alertDialog.setButton(AlertDialog.BUTTON_POSITIVE, "OK", new DialogInterface.OnClickListener() {
			
			@Override
			public void onClick(DialogInterface arg0, int arg1) {
			}
		});
	}
	
	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		// Inflate the menu; this adds items to the action bar if it is present.
		getMenuInflater().inflate(R.menu.splash, menu);
		return true;
	}
	
	public void connection(){
		availableChannels = locationProvider.getClosestServerInfos();
		if(availableChannels != null && availableChannels.length > 0)  {
			if(name != null && !firstChannel.equals(availableChannels[0])){				
				Intent intent = new Intent(SplashActivity.this, BillboardActivity.class);
				Bundle extras = new Bundle();
				extras.putStringArray("CHANNELS", availableChannels);
				extras.putString("DISPLAY_NAME", name);
				intent.putExtras(extras);
				startActivity(intent);
			}
		}
		else
		{
			showServerUnknownError();
		}
	}
	
	private class Listener implements OnClickListener{

		@Override
		public void onClick(View arg0) {
			if(arg0.getId() == connectButton.getId()){
				if(nameEdit.getText().toString()!= null && !nameEdit.getText().toString().trim().isEmpty()){
					name = nameEdit.getText().toString();
					connection();
				}
				else{
					AlertDialog alertDialog = new AlertDialog.Builder(SplashActivity.this).create();
					alertDialog.setTitle(getResources().getString(R.string.error_no_display_name_title));
					alertDialog.setMessage(getResources().getString(R.string.error_no_display_name));
					alertDialog.setButton(AlertDialog.BUTTON_POSITIVE, "OK", new DialogInterface.OnClickListener() {
						@Override
						public void onClick(DialogInterface dialog, int which) {
							SplashActivity.this.nameEdit.requestFocus();
						}
					});
					alertDialog.show();

				}
			}
		}
		
		
		
	}

}
