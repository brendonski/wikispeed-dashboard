package com.wikispeed.dashboard;

import android.annotation.TargetApi;
import android.app.Activity;
import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.ServiceConnection;
import android.graphics.PixelFormat;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.IBinder;
import android.support.v4.content.LocalBroadcastManager;
import android.util.Log;
import android.view.MotionEvent;
import android.view.SurfaceHolder;
import android.view.SurfaceView;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

import com.wikispeed.dashboard.service.DataService;
import com.wikispeed.dashboard.util.SystemUiHider;

/**
 * An example full-screen activity that shows and hides the system UI (i.e.
 * status bar and navigation/system bar) with user interaction.
 * 
 * @see SystemUiHider
 */
public class DashActivity extends Activity {
	private static final String TAG = "DashActivity";

	/**
	 * Whether or not the system UI should be auto-hidden after
	 * {@link #AUTO_HIDE_DELAY_MILLIS} milliseconds.
	 */
	private static final boolean AUTO_HIDE = true;

	/**
	 * If {@link #AUTO_HIDE} is set, the number of milliseconds to wait after
	 * user interaction before hiding the system UI.
	 */
	private static final int AUTO_HIDE_DELAY_MILLIS = 3000;

	/**
	 * If set, will toggle the system UI visibility upon interaction. Otherwise,
	 * will show the system UI visibility upon interaction.
	 */
	private static final boolean TOGGLE_ON_CLICK = true;

	/**
	 * The flags to pass to {@link SystemUiHider#getInstance}.
	 */
	private static final int HIDER_FLAGS = SystemUiHider.FLAG_HIDE_NAVIGATION;

	/**
	 * The instance of the {@link SystemUiHider} for this activity.
	 */
	private SystemUiHider mSystemUiHider;
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		
		// we're not going to see the default background so don't draw it
		getWindow().setBackgroundDrawable(null);
		
		setContentView(R.layout.activity_dash);

		final View controlsView = findViewById(R.id.fullscreen_content_controls);
		final View contentView = findViewById(R.id.fullscreen_content);

		// Set up an instance of SystemUiHider to control the system UI for
		// this activity.
		mSystemUiHider = SystemUiHider.getInstance(this, contentView,
				HIDER_FLAGS);
		mSystemUiHider.setup();
		mSystemUiHider
				.setOnVisibilityChangeListener(new SystemUiHider.OnVisibilityChangeListener() {
					// Cached values.
					int mControlsHeight;
					int mShortAnimTime;

					@Override
					@TargetApi(Build.VERSION_CODES.HONEYCOMB_MR2)
					public void onVisibilityChange(boolean visible) {
						if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.HONEYCOMB_MR2) {
							// If the ViewPropertyAnimator API is available
							// (Honeycomb MR2 and later), use it to animate the
							// in-layout UI controls at the bottom of the
							// screen.
							if (mControlsHeight == 0) {
								mControlsHeight = controlsView.getHeight();
							}
							if (mShortAnimTime == 0) {
								mShortAnimTime = getResources().getInteger(
										android.R.integer.config_shortAnimTime);
							}
							controlsView
									.animate()
									.translationY(visible ? 0 : mControlsHeight)
									.setDuration(mShortAnimTime);
						} else {
							// If the ViewPropertyAnimator APIs aren't
							// available, simply show or hide the in-layout UI
							// controls.
							controlsView.setVisibility(visible ? View.VISIBLE
									: View.GONE);
						}

						if (visible && AUTO_HIDE) {
							// Schedule a hide().
							delayedHide(AUTO_HIDE_DELAY_MILLIS);
						}
					}
				});

		// Set up the user interaction to manually show or hide the system UI.
		contentView.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View view) {
				if (TOGGLE_ON_CLICK) {
					mSystemUiHider.toggle();
				} else {
					mSystemUiHider.show();
				}
			}
		});

		// make gauges transparent
		makeTransparent(findViewById(R.id.speedmeter));
		makeTransparent(findViewById(R.id.tachometer));
		makeTransparent(findViewById(R.id.fuelGauge));
		makeTransparent(findViewById(R.id.tempGauge));
				
		// Upon interacting with UI controls, delay any scheduled hide()
		// operations to prevent the jarring behavior of controls going away
		// while interacting with the UI.
		findViewById(R.id.dummy_button).setOnTouchListener(
				mDelayHideTouchListener);
        
		startDataService();
	}

	private void startDataService() {
		Intent i = new Intent(this, DataService.class);
		startService(i);
	}

	private void stopDataService() {
		Intent i = new Intent(this, DataService.class);
		stopService(i);
	}

    private void makeTransparent(View findViewById) {
		SurfaceView view = (SurfaceView) findViewById;
		view.setZOrderOnTop(true);    // necessary
		SurfaceHolder viewHolder = view.getHolder();
		viewHolder.setFormat(PixelFormat.TRANSPARENT);
	}

	private BroadcastReceiver onNotice= new BroadcastReceiver() {
        
        @Override
        public void onReceive(Context context, Intent intent) {
            // intent can contain anydata
            Log.d(TAG,"onReceive called");
            processExtras(intent.getExtras());
        }
    };
	
	@Override
	protected void onPause() {
		super.onPause();
		LocalBroadcastManager.getInstance(this).unregisterReceiver(onNotice);
		stopDataService();
	}

	protected void processExtras(Bundle extras) {
		if (extras.getBoolean("gauges")) {
			processGauges(extras);
		} else if (extras.getBoolean("lights")) {
			processLights(extras);
		} else if (extras.getBoolean("indicators")) {
			processIndicators(extras);
		} 
	}

	private void processIndicators(Bundle extras) {
		// TODO Auto-generated method stub
		
	}

	private void processLights(Bundle extras) {
		// TODO Auto-generated method stub
		
	}

	private void processGauges(Bundle extras) {
		setSpeed(extras.getInt(DataService.MPH));
		setRevs(extras.getInt(DataService.RPM));
		setFuel(extras.getInt(DataService.GAS));
		setTemp(extras.getInt(DataService.ECT));
		setBatteryCharge(extras.getFloat(DataService.BAT));
		setOilPressure(extras.getInt(DataService.OILP));
		setOilTemp(extras.getInt(DataService.OILT));
		setEngineTemp(extras.getInt(DataService.ECT));
		setTransTemp(extras.getInt(DataService.TFT));
		
	}

	private void setTemp(int int1) {
		DialGauge temp = (DialGauge) findViewById(R.id.tempGauge);
		if (temp != null) {
			temp.setHandTarget(int1);
		}
	}

	private void setFuel(int int1) {
		DialGauge fuel = (DialGauge) findViewById(R.id.fuelGauge);
		if (fuel != null) {
			fuel.setHandTarget(int1);
		}
	}

	private void setRevs(int int1) {
		DialGauge tach = (DialGauge) findViewById(R.id.tachometer);
		if (tach != null) {
			tach.setHandTarget(int1);
		}
	}

	private void setSpeed(int int1) {
		DialGauge speed = (DialGauge) findViewById(R.id.speedmeter);
		if (speed != null) {
			speed.setHandTarget(int1);
		}
	}

	private void setBatteryCharge(float int1) {
		TextView tv = (TextView) findViewById(R.id.battery_volts);
		if (tv != null) {
			tv.setText(getString(R.string.bat_volts) + int1);
		}
	}

	private void setOilPressure(int int1) {
		TextView tv = (TextView) findViewById(R.id.oil_pressure);
		if (tv != null) {
			tv.setText(getString(R.string.oil_press) + int1);
		}
	}

	private void setOilTemp(int int1) {
		TextView tv = (TextView) findViewById(R.id.oil_temp);
		if (tv != null) {
			tv.setText(getString(R.string.oil_temp) + int1);
		}
	}

	private void setEngineTemp(int int1) {
		TextView tv = (TextView) findViewById(R.id.engine_temp);
		if (tv != null) {
			tv.setText(getString(R.string.engine_temp) + int1);
		}
	}

	private void setTransTemp(int int1) {
		TextView tv = (TextView) findViewById(R.id.trans_temp);
		if (tv != null) {
			tv.setText(getString(R.string.trans_temp) + int1);
		}
	}

	@Override
	protected void onResume() {
		super.onResume();
		IntentFilter iff= new IntentFilter(DataService.ACTION);
        LocalBroadcastManager.getInstance(this).registerReceiver(onNotice, iff);
        startDataService();
	}

	@Override
	protected void onPostCreate(Bundle savedInstanceState) {
		super.onPostCreate(savedInstanceState);

		// Trigger the initial hide() shortly after the activity has been
		// created, to briefly hint to the user that UI controls
		// are available.
		delayedHide(100);
	}

	/**
	 * Touch listener to use for in-layout UI controls to delay hiding the
	 * system UI. This is to prevent the jarring behavior of controls going away
	 * while interacting with activity UI.
	 */
	View.OnTouchListener mDelayHideTouchListener = new View.OnTouchListener() {
		@Override
		public boolean onTouch(View view, MotionEvent motionEvent) {
			if (AUTO_HIDE) {
				delayedHide(AUTO_HIDE_DELAY_MILLIS);
			}
			return false;
		}
	};

	Handler mHideHandler = new Handler();
	Runnable mHideRunnable = new Runnable() {
		@Override
		public void run() {
			mSystemUiHider.hide();
		}
	};

	/**
	 * Schedules a call to hide() in [delay] milliseconds, canceling any
	 * previously scheduled calls.
	 */
	private void delayedHide(int delayMillis) {
		mHideHandler.removeCallbacks(mHideRunnable);
		mHideHandler.postDelayed(mHideRunnable, delayMillis);
	}
	
	private DataService mBoundService;
	private boolean mIsBound;
	private ServiceConnection mConnection = new ServiceConnection() {
	    public void onServiceConnected(ComponentName className, IBinder service) {
	        // This is called when the connection with the service has been
	        // established, giving us the service object we can use to
	        // interact with the service.  Because we have bound to a explicit
	        // service that we know is running in our own process, we can
	        // cast its IBinder to a concrete class and directly access it.
	        mBoundService = ((DataService.LocalBinder)service).getService();

	        // Tell the user about this for our demo.
	        Toast.makeText(DashActivity.this, R.string.local_service_connected,
	                Toast.LENGTH_SHORT).show();
	    }

	    public void onServiceDisconnected(ComponentName className) {
	        // This is called when the connection with the service has been
	        // unexpectedly disconnected -- that is, its process crashed.
	        // Because it is running in our same process, we should never
	        // see this happen.
	        mBoundService = null;
	        Toast.makeText(DashActivity.this, R.string.local_service_disconnected,
	                Toast.LENGTH_SHORT).show();
	    }
	};

	void doBindService() {
	    // Establish a connection with the service.  We use an explicit
	    // class name because we want a specific service implementation that
	    // we know will be running in our own process (and thus won't be
	    // supporting component replacement by other applications).
	    bindService(new Intent(DashActivity.this, 
	            DataService.class), mConnection, Context.BIND_AUTO_CREATE);
	    mIsBound = true;
	}

	void doUnbindService() {
	    if (mIsBound) {
	        // Detach our existing connection.
	        unbindService(mConnection);
	        mIsBound = false;
	    }
	}

	@Override
	protected void onDestroy() {
	    super.onDestroy();
	    doUnbindService();
	    stopDataService();
	}
}
