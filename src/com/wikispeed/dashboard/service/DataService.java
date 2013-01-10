package com.wikispeed.dashboard.service;

import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Binder;
import android.os.IBinder;
import android.support.v4.content.LocalBroadcastManager;
import android.util.Log;
import android.widget.Toast;

import com.wikispeed.dashboard.R;

public class DataService extends Service {
 
    private static final String TAG = "LocalService";
    
    public static final String ACTION = "theaction";
    
    /**
     * Class for clients to access.  Because we know this service always
     * runs in the same process as its clients, we don't need to deal with
     * IPC.
     */
    public class LocalBinder extends Binder {
        public DataService getService() {
            return DataService.this;
        }
    }

    @Override
    public void onCreate() {
        new DataReceiver(this).execute("");
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        Log.i("LocalService", "Received start id " + startId + ": " + intent);
        // We want this service to continue running until it is explicitly
        // stopped, so return sticky.
        return START_STICKY;
    }

    @Override
    public void onDestroy() {
        // Tell the user we stopped.
        Toast.makeText(this, R.string.local_service_stopped, Toast.LENGTH_SHORT).show();
    }

    @Override
    public IBinder onBind(Intent intent) {
        return mBinder;
    }

    // This is the object that receives interactions from clients.  See
    // RemoteService for a more complete example.
    private final IBinder mBinder = new LocalBinder();

	private class DataReceiver extends AsyncTask<String, Void, String> {

		private Context context = null;

		public DataReceiver(Context context) {
			this.context = context;
		}

		@Override
		protected String doInBackground(String... params) {
			// TODO make stoppable
			while (true) {
				try {
					Log.d(TAG, "sending broadcast");
					Intent in = new Intent(ACTION); // you can put anything in
													// it with putExtra
					LocalBroadcastManager.getInstance(context)
							.sendBroadcast(in);
					Thread.sleep(250);
				} catch (InterruptedException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
			}
			//return null;
		}

		@Override
		protected void onPostExecute(String result) {
		}

		@Override
		protected void onPreExecute() {
		}

		@Override
		protected void onProgressUpdate(Void... values) {
		}
	}
}