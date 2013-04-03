package com.wikispeed.dashboard.service;

import java.util.StringTokenizer;

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
    
    public static final String INDICATORS = "WDIV";
	
    public static final String LIGHTS = "WDLV";
	
    public static final String GAUGES = "WDGV";

    public static final String GEA = "GEA";
    public static final String TRP = "TRP";
    public static final String ODO = "ODO";
    public static final String GAS = "GAS";
    public static final String TFT = "TFT";
    public static final String ECT = "ECT";
    public static final String BAT = "BAT";
    public static final String OILT = "OILT";
    public static final String OILP = "OILP";
    public static final String RPM = "RPM";
    public static final String MPH = "MPH";
	
    boolean running = true;
    
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
        Log.i(TAG, "Received start id " + startId + ": " + intent);
        // TODO In the future make this service sticky so we still receive notifications
        // even if the dashboard is not running.
        return START_NOT_STICKY;
    }

    @Override
    public void onDestroy() {
    	running = false;
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

//    WDGV MPH,RPM,OILP,OILT,BAT,ECT,TFT,GAS,ODO,TRP,GEA + (CR)
//    WIKISPEED Dashboard Gauge Values	WDGV
//    Speed 					MPH = 0 – 150
//    Engine e Speed				RPM = 0 – 10
//    Oil Pressure				OILP = 0 – XX
//    Oil Temp				OILT = 0 – XX
//    Bat Volts				BAT = 10 – 15
//    Engine Coolant Temp			ECT = 0 – XX
//    Transmission Fluid Temp		TFT = 0- XX
//    Fuel Reservoir %			GAS = 0 – 100
//    Odometer				ODO = 0 – 9999999
//    Trip					TRP = 0 – 9999
//    Transmission Gear			GEA = 0-6 & R
//    E.g.
//    WDGV 60,4,60,80,13.5,80,80,45,15000,500,3 + (CR)
    
	private class DataReceiver extends AsyncTask<String, Void, String> {

		private Context context = null;

		public DataReceiver(Context context) {
			this.context = context;
		}

		@Override
		protected String doInBackground(String... params) {
			int speed = 0;
			int rpm = 0;
			int speedIncr = 1;
			int rpmIncr = 2;
			int speedMult = 1;
			int rpmMult = 3;
			while (running) {
				try {
					Log.d(TAG, "sending broadcast");
					// simulate some movement
					if (speed > 80) {
						speedIncr = -1 * speedMult;
					}
					if (speed <= 0) {
						speedIncr = 1 * speedMult;
					}
					if (rpm > 60) {
						rpmIncr = -1 * rpmMult;
					}
					if (rpm <= 8) {
						rpmIncr = 1 * rpmMult;
					}
					speed = speed + speedIncr;
					rpm = rpm + rpmIncr;
					String dataLine = getDataLine(speed, rpm);
					Intent in = getIntentFromDataLine(dataLine);
					LocalBroadcastManager.getInstance(context)
							.sendBroadcast(in);
					Thread.sleep(150);
				} catch (InterruptedException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
			}
			return null;
		}

		private Intent getIntentFromDataLine(String dataLine) {
			Intent in = new Intent(ACTION); // you can put anything in it with putExtra
			String values = "";
			if (dataLine != null && dataLine.startsWith(GAUGES)) {
				in.putExtra("gauges", true);
				values = dataLine.substring(GAUGES.length());
			} else if (dataLine != null && dataLine.startsWith(LIGHTS)) {
				in.putExtra("lights", true);
				values = dataLine.substring(LIGHTS.length());
			} else if (dataLine != null && dataLine.startsWith(INDICATORS)) {
				in.putExtra("indicators", true);
				values = dataLine.substring(INDICATORS.length());
			}
			values = values.replaceAll(" ", "");
			StringTokenizer st = new StringTokenizer(values, ",");
			// MPH,RPM,OILP,OILT,BAT,ECT,TFT,GAS,ODO,TRP,GEA
			in.putExtra(MPH, Integer.valueOf(st.nextToken()));
			in.putExtra(RPM, Integer.valueOf(st.nextToken()));
			in.putExtra(OILP, Integer.valueOf(st.nextToken()));
			in.putExtra(OILT, Integer.valueOf(st.nextToken()));
			in.putExtra(BAT, Float.valueOf(st.nextToken()));
			in.putExtra(ECT, Integer.valueOf(st.nextToken()));
			in.putExtra(TFT, Integer.valueOf(st.nextToken()));
			in.putExtra(GAS, Integer.valueOf(st.nextToken()));
			in.putExtra(ODO, Integer.valueOf(st.nextToken()));
			in.putExtra(TRP, Integer.valueOf(st.nextToken()));
			in.putExtra(GEA, Integer.valueOf(st.nextToken()));
			Log.d(TAG, "built intent " + in);
			return in;
		}

		private String getDataLine(int speed, int rpm) {
			String dataLine = "WDGV " + speed + "," + rpm
					+ ",60,85,13.5,80,80,78,15000,500,3";
			return dataLine;
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