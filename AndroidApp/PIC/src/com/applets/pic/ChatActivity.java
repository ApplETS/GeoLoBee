package com.applets.pic;

import com.applets.pic.irc.IRCClient;
import com.applets.pic.irc.IRCEvent;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.support.v4.widget.DrawerLayout;
import android.view.LayoutInflater;
import android.view.Menu;
import android.widget.ArrayAdapter;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.TextView;

public class ChatActivity extends Activity implements IRCEvent{

	private ListView drawerList;
	private String[] availableChannels;
	private IRCClient ircClient;
	private String displayName;
	private LinearLayout messagesLayout;
	private DrawerLayout drawerLayout;
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_chat);
		displayName = getIntent().getExtras().getString("DISPLAY_NAME");
		availableChannels = getIntent().getExtras().getStringArray("CHANNELS");
		
		drawerList = (ListView)findViewById(R.id.left_drawer);
		messagesLayout = (LinearLayout)findViewById(R.id.layoutListView);
	
		if(availableChannels != null && availableChannels.length > 0) {
			getActionBar().setTitle(availableChannels[0]);
			drawerList.setAdapter(new ArrayAdapter<String>(this, R.layout.drawer_list_layout, availableChannels));
		}
		
		ircClient = new IRCClient(this, displayName);
		
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

	@Override
	public void registrationComplete() {
		ircClient.join(availableChannels[0]);
	}

	@Override
	public void userQuit(String user) {
		String message = String.format(getResources().getString(R.string.irc_user_quit), user);
		LayoutInflater inflater = (LayoutInflater)getSystemService(Context.LAYOUT_INFLATER_SERVICE);
	    TextView textView = (TextView)inflater.inflate(R.layout.sent_message_text_view, null, true);
	    textView.setText(message);
	    this.messagesLayout.addView(textView);
	}

	@Override
	public void userJoin(String user, String channel) {
		String message = String.format(getResources().getString(R.string.irc_user_join), user);
		LayoutInflater inflater = (LayoutInflater)getSystemService(Context.LAYOUT_INFLATER_SERVICE);
	    TextView textView = (TextView)inflater.inflate(R.layout.sent_message_text_view, null, true);
	    textView.setText(message);
	    this.messagesLayout.addView(textView);
	}

	@Override
	public void userPart(String user, String channel) {
		String message = String.format(getResources().getString(R.string.irc_user_part), user);
		LayoutInflater inflater = (LayoutInflater)getSystemService(Context.LAYOUT_INFLATER_SERVICE);
	    TextView textView = (TextView)inflater.inflate(R.layout.received_red_message, null, true);
	    textView.setText(message);
	    this.messagesLayout.addView(textView);
	}

	@Override
	public void channelJoined(String channel) {
		String message = String.format(getResources().getString(R.string.irc_join), channel);
		LayoutInflater inflater = (LayoutInflater)getSystemService(Context.LAYOUT_INFLATER_SERVICE);
	    TextView textView = (TextView)inflater.inflate(R.layout.received_info_message, null, true);
	    textView.setText(message);
	    this.messagesLayout.addView(textView);
	}

	@Override
	public void channelParted(String channel) {
		String message = String.format(getResources().getString(R.string.irc_part), channel);
		LayoutInflater inflater = (LayoutInflater)getSystemService(Context.LAYOUT_INFLATER_SERVICE);
	    TextView textView = (TextView)inflater.inflate(R.layout.received_red_message, null, true);
	    textView.setText(message);
	    this.messagesLayout.addView(textView);
	}

	@Override
	public void messageRecieved(String channel, String user, String message) {
		String mergedMessaged = user + ": " + message;
		LayoutInflater inflater = (LayoutInflater)getSystemService(Context.LAYOUT_INFLATER_SERVICE);
	    TextView textView = (TextView)inflater.inflate(R.layout.received_info_message, null, true);
	    textView.setText(mergedMessaged);
	    this.messagesLayout.addView(textView);
	}

}
