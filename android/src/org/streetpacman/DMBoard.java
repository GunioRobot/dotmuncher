/*
 * Copyright (C) 2007 The Android Open Source Project
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.streetpacman;

import java.util.List;
import java.util.UUID;

import org.json.JSONException;
import org.streetpacman.core.DMCore;
import org.streetpacman.core.DMConstants;
import org.streetpacman.util.DMUtils;

import com.google.android.maps.GeoPoint;
import com.google.android.maps.MapActivity;
import com.google.android.maps.MapView;
import com.google.android.maps.Overlay;

import android.content.Context;
import android.graphics.Matrix;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Bundle;
import android.telephony.TelephonyManager;
import android.util.Log;
import android.view.ViewGroup.LayoutParams;
import android.view.Window;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.AbsoluteLayout;
import android.widget.FrameLayout;
import android.widget.ImageView.ScaleType;
import android.widget.Toast;

public class DMBoard extends MapActivity {
	private static DMBoard instance;
	private DMOverlay dmOverlay;
	private Location currentLocation;
	private LocationManager locationManager;
	private SensorManager sensorManager;
	private boolean keepMyLocationVisible;
	MapView mapView;

	@Override
	public void onCreate(Bundle savedInstanceState) {
		this.instance = this;
		Log.d(DMConstants.TAG, "DMBoard.onCreate");
		super.onCreate(savedInstanceState);
		requestWindowFeature(Window.FEATURE_NO_TITLE);
		setContentView(R.layout.mapview);
		mapView = (MapView) findViewById(R.id.map);

		this.dmOverlay = new DMOverlay(this);

		List<Overlay> listOfOverlays = mapView.getOverlays();
		listOfOverlays.clear();
		listOfOverlays.add(dmOverlay);
		sensorManager = (SensorManager) getSystemService(SENSOR_SERVICE);
		locationManager = (LocationManager) getSystemService(Context.LOCATION_SERVICE);

		mapView.setBuiltInZoomControls(true);
		DMCore.getCore().net(DMConstants.update_phone_settings,
				r_update_phone_settings, rEmpty);

	}

	@Override
	protected boolean isRouteDisplayed() {
		return false;
	}

	@Override
	protected boolean isLocationDisplayed() {
		return true;
	}

	@Override
	protected void onPause() {
		// Called when activity is going into the background, but has not (yet)
		// been
		// killed. Shouldn't block longer than approx. 2 seconds.
		Log.d(DMConstants.TAG, "DM.onPause");
		unregisterLocationAndSensorListeners();
		super.onPause();
	}

	@Override
	protected void onResume() {
		// Called when the current activity is being displayed or re-displayed
		// to the user.
		Log.d(DMConstants.TAG, "DM.onResume");
		super.onResume();

		registerLocationAndSensorListeners();

		// zoom and pan
		// showPoints();
	}

	public void addSprite(DMSprite dmSprite) {
		((AbsoluteLayout) findViewById(R.id.spriteOverlay)).addView(dmSprite);
	}

	/**
	 * Registers to receive location updates from the GPS location provider and
	 * sensor updated from the compass.
	 */
	void registerLocationAndSensorListeners() {
		if (locationManager != null) {
			locationManager.requestLocationUpdates(
					LocationManager.GPS_PROVIDER, 0, 0, locationListener);
			try {
				locationManager.requestLocationUpdates(
						LocationManager.NETWORK_PROVIDER,
						1000 * 60 * 5 /* minTime */, 0 /* minDist */,
						locationListener);
			} catch (RuntimeException e) {
				// If anything at all goes wrong with getting a cell location do
				// not
				// abort. Cell location is not essential to this app.
				Log.w(DMConstants.TAG,
						"Could not register network location listener.");
			}
		}
		if (sensorManager == null) {
			return;
		}
		Sensor compass = sensorManager
				.getDefaultSensor(Sensor.TYPE_ORIENTATION);
		if (compass == null) {
			return;
		}
		Log.d(DMConstants.TAG, "DM: Now registering sensor listeners.");
		sensorManager.registerListener(sensorListener, compass,
				SensorManager.SENSOR_DELAY_UI);
	}

	public void alert(String txt) {
		Toast.makeText(this, txt, Toast.LENGTH_LONG).show();
	}

	/**
	 * Moves the location pointer to the current location and center the map if
	 * the current location is outside the visible area.
	 */
	private void showCurrentLocation() {
		if (currentLocation == null || dmOverlay == null || mapView == null) {
			return;
		}
		dmOverlay.setMyLocation(currentLocation);
		mapView.invalidate();
		/*
		 * MapController controller = mapView.getController(); GeoPoint geoPoint
		 * = DMUtils.getGeoPoint(currentLocation);
		 * controller.animateTo(geoPoint);
		 */
	}

	private Runnable r_update_phone_settings = new Runnable() {

		@Override
		public void run() {
			DMCore.getCore().net(DMConstants.find_games, r_find_games, rEmpty);

		}

	};

	private Runnable r_find_games = new Runnable() {

		@Override
		public void run() {
			DMCore.getCore().dmPhone.game = DMCore.getCore().alGames.get(0);
			DMCore.getCore().net(DMConstants.join_game, r_join_game, rEmpty);
		}

	};
	private Runnable r_join_game = new Runnable() {

		@Override
		public void run() {
			// zoom and pan
			showPoints();

		}

	};
	private Runnable rEmpty = new Runnable() {

		@Override
		public void run() {
			// TODO Auto-generated method stub

		}

	};
	private final LocationListener locationListener = new LocationListener() {
		@Override
		public void onLocationChanged(Location location) {
			currentLocation = location;

			showCurrentLocation();

			if (location != null) {
				Toast.makeText(
						getBaseContext(),
						"Location changed : Lat: " + location.getLatitude()
								+ " Lng: " + location.getLongitude(),
						Toast.LENGTH_SHORT).show();
				DMCore.getCore().dmPhone.setLocation(location);
				DMCore.getCore().net(DMConstants.update, rEmpty, rEmpty);
			}
		}

		@Override
		public void onProviderDisabled(String provider) {
			// TODO Auto-generated method stub

		}

		@Override
		public void onProviderEnabled(String provider) {
			// TODO Auto-generated method stub

		}

		@Override
		public void onStatusChanged(String provider, int status, Bundle extras) {
			// TODO Auto-generated method stub

		}
	};

	/**
	 * Unregisters all location and sensor listeners
	 */
	void unregisterLocationAndSensorListeners() {
		if (locationManager != null) {
			Log.d(DMConstants.TAG, "DM: Now unregistering location listeners.");
			locationManager.removeUpdates(locationListener);
		}

		if (sensorManager != null) {
			Log.d(DMConstants.TAG, "DM: Now unregistering sensor listeners.");
			sensorManager.unregisterListener(sensorListener);
		}

	}

	public void showPoints() {
		if (mapView == null) {
			return;
		}

		int bottom = DMCore.getCore().dmMap.getBottom();
		int left = DMCore.getCore().dmMap.getLeft();
		int latSpanE6 = DMCore.getCore().dmMap.getTop() - bottom;
		int lonSpanE6 = DMCore.getCore().dmMap.getRight() - left;
		if (latSpanE6 > 0 && latSpanE6 < 180E6 && lonSpanE6 > 0
				&& lonSpanE6 < 360E6) {
			keepMyLocationVisible = false;
			GeoPoint center = new GeoPoint(bottom + latSpanE6 / 2, left
					+ lonSpanE6 / 2);
			if (DMUtils.isValidGeoPoint(center)) {
				mapView.getController().setCenter(center);
				mapView.getController().setZoom(20);
				mapView.getController().zoomToSpan(latSpanE6, lonSpanE6);
			}
		}
	}

	private final SensorEventListener sensorListener = new SensorEventListener() {
		@Override
		public void onSensorChanged(SensorEvent se) {
			synchronized (this) {
				float magneticHeading = se.values[0];
				DMSprite.getFactory().getSprite(DMCore.getCore().myPhoneIndex)
						.setHeading(magneticHeading);
			}
		}

		@Override
		public void onAccuracyChanged(Sensor s, int accuracy) {
			// do nothing
		}
	};

	public static DMBoard getInstance() {
		return instance;
	}
}
