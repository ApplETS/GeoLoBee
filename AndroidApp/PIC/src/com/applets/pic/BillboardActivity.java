package com.applets.pic;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

import org.json.JSONArray;
import org.json.JSONObject;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.view.Menu;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.ListView;

import com.applets.pic.http.HttpReader;
import com.applets.pic.http.InfosServerProvider;
import com.applets.pic.model.BillboardAdapter;
import com.applets.pic.model.Post;

public class BillboardActivity extends Activity {
	private ArrayList<Post> posts;
	private InfosServerProvider infosProvider;
	private ListView listBillboard;
	private String[] availableChannels;
	private String displayName;
	private Button connectButton;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_billboard);
		displayName = getIntent().getExtras().getString("DISPLAY_NAME");
		availableChannels = getIntent().getExtras().getStringArray("CHANNELS");
		
		infosProvider = new InfosServerProvider(getApplicationContext());
		refreshPosts();
		
		connectButton = (Button)findViewById(R.id.buttonChat);
		connectButton.setOnClickListener(new OnClickListener(){

			@Override
			public void onClick(View arg0) {
					Intent intent = new Intent(BillboardActivity.this, ChatActivity.class);
					Bundle extras = new Bundle();
					extras.putStringArray("CHANNELS", availableChannels);
					extras.putString("DISPLAY_NAME", displayName);
					intent.putExtras(extras);
					startActivity(intent);
			}
		});
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		// Inflate the menu; this adds items to the action bar if it is present.
		getMenuInflater().inflate(R.menu.billboard, menu);
		return true;
	}
	
	private void refreshPosts(){
		availableChannels = infosProvider.getClosestServerInfos();
		if(availableChannels != null && availableChannels.length > 0) {
			posts = getPosts(availableChannels[0]);
			BillboardAdapter adapter = new BillboardAdapter(this, R.layout.post_message, posts.toArray(new Post[posts.size()]));
	        
	        
			listBillboard = (ListView)findViewById(R.id.listBillboard);
	        
	        listBillboard.setAdapter(adapter);
		}
	}
	
	private ArrayList<Post> getPosts(String channel_name) {
		SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd hh:mm:ss");
		ArrayList<Post> posts = new ArrayList<Post>();
		
		HttpReader task = (HttpReader)new HttpReader().execute("http://clubapplets.ca/checkinchat/api.php?method=getPosts&channel_name=" + channel_name);
		String serverInfos;
		try {
			serverInfos = task.get(15, TimeUnit.SECONDS);
			if(serverInfos != null && !serverInfos.isEmpty()) {
				JSONArray recs = new JSONArray(serverInfos);
				Post currentPost;
				for (int i = 0; i < recs.length(); ++i) {
				    JSONObject rec = recs.getJSONObject(i);
				    currentPost = new Post(rec.getString("creator"), rec.getString("content"), sdf.parse(rec.getString("dateToKill")));
				    posts.add(currentPost);
				}
			}
		} catch (TimeoutException e) {
			e.printStackTrace();
		}catch (Exception e) {
			e.printStackTrace();
		}

		return posts;
	}

}
