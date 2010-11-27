package org.streetpacman;

import java.util.UUID;

import org.streetpacman.core.DMCore;
import org.streetpacman.core.DMConstants;

import android.app.TabActivity;
import android.content.Context;
import android.content.Intent;
import android.content.res.Resources;
import android.os.Bundle;
import android.telephony.TelephonyManager;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;
import android.view.Window;
import android.view.View.OnTouchListener;
import android.widget.TabHost;

public class DMStreetPacman extends TabActivity implements OnTouchListener {
	private static DMStreetPacman instance;
	private DMControls dMControls;
	private DMCore dmCore;

	@Override
	public void onCreate(Bundle savedInstanceState) {
		Log.d(DMConstants.TAG, "DMStreetPacmanMap.onCreate");
		super.onCreate(savedInstanceState);
		requestWindowFeature(Window.FEATURE_NO_TITLE);

		// http://stackoverflow.com/questions/2785485/is-there-a-unique-android-device-id
		final TelephonyManager tm = (TelephonyManager) getBaseContext()
				.getSystemService(Context.TELEPHONY_SERVICE);

		final String tmDevice, tmSerial, tmPhone, androidId;
		tmDevice = "" + tm.getDeviceId();
		tmSerial = "" + tm.getSimSerialNumber();
		androidId = ""
				+ android.provider.Settings.Secure.getString(
						getContentResolver(),
						android.provider.Settings.Secure.ANDROID_ID);

		UUID deviceUuid = new UUID(androidId.hashCode(),
				((long) tmDevice.hashCode() << 32) | tmSerial.hashCode());
		String deviceId = deviceUuid.toString();

		this.dmCore = new DMCore(deviceId);

		final Resources res = getResources();
		final TabHost tabHost = getTabHost();
		tabHost.addTab(tabHost
				.newTabSpec("tab2")
				.setIndicator("Loading",
						res.getDrawable(android.R.drawable.ic_menu_mapmode))
				.setContent(new Intent(this, DMLoading.class)));

		tabHost.addTab(tabHost
				.newTabSpec("tab1")
				.setIndicator("Map",
						res.getDrawable(android.R.drawable.ic_menu_mapmode))
				.setContent(new Intent(this, DMStreetPacmanMap.class)));
		//tabHost.getTabWidget().setVisibility(View.GONE);
	}

	@Override
	public void onActivityResult(int requestCode, int resultCode,
			final Intent results) {
		switch (requestCode) {
		case DMConstants.SHOW_GAME:

			break;
		}
	}

	// @VisibleForTesting
	static void clearInstance() {
		instance = null;
	}

	@Override
	public boolean onTouch(View v, MotionEvent event) {
		if (event.getAction() == MotionEvent.ACTION_DOWN) {
			dMControls.show();
		}
		return false;
	}
}
