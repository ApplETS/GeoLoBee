package com.applets.pic.irc;

import android.os.AsyncTask;

 public class IRCConnecter extends AsyncTask<Object, Void, IRCClient> {

	 private IWaitingIRCClientCreated clientContainer;
	 
	@SuppressWarnings("finally")
	@Override
	protected IRCClient doInBackground(Object... args) {
		IRCClient client = null;
		try {
		client = new IRCClient((IRCEvent)args[0], (String)args[1]);
		clientContainer = (IWaitingIRCClientCreated)args[0];
		}
		catch(Exception e) {
			e.printStackTrace();
		}
		finally {
			return client;
		}
	}

	@Override
	protected void onPostExecute(IRCClient result) {
		clientContainer.ClientCreated(result);
		super.onPostExecute(result);
	}
	
	

}
