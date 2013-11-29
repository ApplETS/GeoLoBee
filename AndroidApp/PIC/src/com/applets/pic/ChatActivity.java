package com.applets.pic;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.support.v4.widget.DrawerLayout;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.TextView;

import com.applets.pic.irc.IRCClient;
import com.applets.pic.irc.IRCConnecter;
import com.applets.pic.irc.IRCEvent;
import com.applets.pic.irc.IWaitingIRCClientCreated;

public class ChatActivity extends Activity implements IRCEvent, IWaitingIRCClientCreated{

	private ListView drawerList;
	private String[] availableChannels;
	private IRCClient ircClient;
	private String displayName;
	private LinearLayout messagesLayout;
	private DrawerLayout drawerLayout;
	private Boolean clientIsReadyToJoin = false;
	private String currentChannel;
	private Button sendButton;
	private EditText editMessageBlock;
	
	@Override
	protected void onDestroy() {
		ircClient.part(currentChannel);
		ircClient.quit();
		super.onDestroy();
	}

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_chat);
		displayName = getIntent().getExtras().getString("DISPLAY_NAME");
		availableChannels = getIntent().getExtras().getStringArray("CHANNELS");
		
		drawerList = (ListView)findViewById(R.id.left_drawer);
		drawerLayout = (DrawerLayout) findViewById(R.id.drawer_layout);
		messagesLayout = (LinearLayout)findViewById(R.id.layoutListView);
		sendButton = (Button)findViewById(R.id.buttonSend);
		editMessageBlock = (EditText)findViewById(R.id.editMessage);
	
		if(availableChannels != null && availableChannels.length > 0) {
			getActionBar().setTitle(availableChannels[0]);
			drawerList.setAdapter(new ArrayAdapter<String>(this, R.layout.drawer_list_layout, availableChannels));
			drawerList.setOnItemClickListener(new DrawerItemClickListener());
		}
		
		IRCConnecter connecter = new IRCConnecter();
		connecter.execute(this, displayName);
		
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		// Inflate the menu; this adds items to the action bar if it is present.
		getMenuInflater().inflate(R.menu.chat, menu);
		return true;
	}
	
	@SuppressWarnings("unused")
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
		if(ircClient != null) {
			currentChannel = availableChannels[0];
			ircClient.join(currentChannel);
			clientIsReadyToJoin = false;
			sendButton.setOnClickListener(new SendbuttonOnClickListener());
		}
		else{
			clientIsReadyToJoin = true;
		}
	}

	@Override
	public void userQuit(String user) {
		String message = "User " + user + " has left the server.";
		LayoutInflater inflater = (LayoutInflater)getSystemService(Context.LAYOUT_INFLATER_SERVICE);
	    TextView textView = (TextView)inflater.inflate(R.layout.received_red_message, null, true);
	    textView.setText(message);
	    this.runOnUiThread(new AddTextViewToMessagesListRunnable(textView));
	}

	@Override
	public void userJoin(String user, String channel) {
		String message = "User " + user + " has joined the channel.";
		LayoutInflater inflater = (LayoutInflater)getSystemService(Context.LAYOUT_INFLATER_SERVICE);
	    TextView textView = (TextView)inflater.inflate(R.layout.received_info_message, null, true);
	    textView.setText(message);
	    this.runOnUiThread(new AddTextViewToMessagesListRunnable(textView));
	}

	@Override
	public void userPart(String user, String channel) {
		String message = "User " + user + " has left the channel.";
		LayoutInflater inflater = (LayoutInflater)getSystemService(Context.LAYOUT_INFLATER_SERVICE);
	    TextView textView = (TextView)inflater.inflate(R.layout.received_red_message, null, true);
	    textView.setText(message);
	    this.runOnUiThread(new AddTextViewToMessagesListRunnable(textView));
	}

	@Override
	public void channelJoined(String channel) {
		String message = "You have joined channel " + channel + ".";
		Log.i("IRC", "Received message: " + message);
		LayoutInflater inflater = (LayoutInflater)getSystemService(Context.LAYOUT_INFLATER_SERVICE);
	    TextView textView = (TextView)inflater.inflate(R.layout.received_info_message, null, true);
	    textView.setText(message);
	    this.runOnUiThread(new AddTextViewToMessagesListRunnable(textView));
	}

	@Override
	public void channelParted(String channel) {
		/*String message = "You have left channel " + channel + ".";
		LayoutInflater inflater = (LayoutInflater)getSystemService(Context.LAYOUT_INFLATER_SERVICE);
	    TextView textView = (TextView)inflater.inflate(R.layout.received_red_message, null, true);
	    textView.setText(message);
	    this.runOnUiThread(new AddTextViewToMessagesListRunnable(textView));*/
	}

	@Override
	public void messageReceived(String channel, String user, String message) {
		String mergedMessaged = user + message;
		Log.i("IRC", "Received message: " + mergedMessaged);
		LayoutInflater inflater = (LayoutInflater)getSystemService(Context.LAYOUT_INFLATER_SERVICE);
	    TextView textView = (TextView)inflater.inflate(R.layout.received_message, null, true);
	    textView.setText(mergedMessaged);
	    this.runOnUiThread(new AddTextViewToMessagesListRunnable(textView));
	}
	

	@Override
	public void ClientCreated(IRCClient client) {
		ircClient = client;
		if(clientIsReadyToJoin) {
			registrationComplete();
		}
	}
	
	private void selectItem(int position) {
        this.messagesLayout.removeAllViews();
        drawerLayout.closeDrawer(drawerList);
        ircClient.part(currentChannel);
        currentChannel = availableChannels[position];
        ircClient.join(currentChannel);
        getActionBar().setTitle(currentChannel);
    }
	
	private void showSentMessage(String message) {
		String messageStr = displayName + ": " + message;
		Log.i("IRC", "Sent message: " + message);
		LayoutInflater inflater = (LayoutInflater)getSystemService(Context.LAYOUT_INFLATER_SERVICE);
	    TextView textView = (TextView)inflater.inflate(R.layout.sent_message_text_view, null, true);
	    textView.setText(messageStr);
	    this.runOnUiThread(new AddTextViewToMessagesListRunnable(textView));
	}
	
	private class AddTextViewToMessagesListRunnable implements Runnable{

    	private TextView message;
    	
    	public AddTextViewToMessagesListRunnable(TextView message) {
    		this.message = message;
    	}
    	
		@Override
		public void run() {
			ChatActivity.this.messagesLayout.addView(message);
		}
    	
    }
	
	private class DrawerItemClickListener implements ListView.OnItemClickListener {
		@Override
		public void onItemClick(AdapterView<?> arg0, View arg1, int position, long arg3) {
			selectItem(position);
		}	
	}
	
	private class SendbuttonOnClickListener implements OnClickListener {

		@Override
		public void onClick(View arg0) {
			String message = editMessageBlock.getText().toString();
			ircClient.sendMessage(currentChannel, message);
			showSentMessage(message);
			editMessageBlock.setText("");
		}
		
	}

}
