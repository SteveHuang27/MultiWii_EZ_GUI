/*  MultiWii EZ-GUI
    Copyright (C) <2012>  Bartosz Szczygiel (eziosoft)

    This program is free software: you can redistribute it and/or modify
    it under the terms of the GNU General Public License as published by
    the Free Software Foundation, either version 3 of the License, or
    (at your option) any later version.

    This program is distributed in the hope that it will be useful,
    but WITHOUT ANY WARRANTY; without even the implied warranty of
    MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
    GNU General Public License for more details.

    You should have received a copy of the GNU General Public License
    along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */

package com.ezio.multiwii.waypoints;

import java.text.DecimalFormat;
import java.text.NumberFormat;
import java.util.Iterator;
import android.app.Activity;
import android.content.Context;
import android.location.Criteria;
import android.location.GpsSatellite;
import android.location.GpsStatus;
import android.location.GpsStatus.Listener;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.view.View;
import android.view.WindowManager;
import android.widget.TextView;

import com.ezio.multiwii.App;
import com.ezio.multiwii.R;
import com.ezio.multiwii.notUsed.ComunityMap;

public class WaypointActivity extends Activity {

	int PhoneNumSat = 0;
	double SelectedLatitude = 0;
	double SelectedLongitude = 0;

	TextView TVData;
	TextView TVMWInfo;

	NumberFormat format = new DecimalFormat("0.############################################################"); // used
	// to
	// avoid
	// scientific
	// notation

	private boolean killme = false;

	App app;
	Handler mHandler = new Handler();

	private Runnable update = new Runnable() {
		@Override
		public void run() {

			app.mw.ProcessSerialData(app.loggingON);

			app.frsky.ProcessSerialData(false);
			app.Frequentjobs();

			TVData.setText("");
			displayWPs();

			app.mw.SendRequest();
			if (!killme)
				mHandler.postDelayed(update, app.RefreshRate);

			Log.d(app.TAG, "loop " + this.getClass().getName());

		}
	};

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		app = (App) getApplication();
		app.ForceLanguage();
		app.ConnectionBug();
		setContentView(R.layout.waypoint_layout);
		getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);

		TVData = (TextView) findViewById(R.id.textViewData);
		TVMWInfo = (TextView) findViewById(R.id.textViewMWInfo);

		Bundle extras = getIntent().getExtras();
		if (extras != null) {
			SelectedLatitude = extras.getLong("LAT");
			SelectedLongitude = extras.getLong("LON");

			TVMWInfo.setText(getString(R.string.GPS_latitude) + ":" + format.format(SelectedLatitude / 1e6) + "\n" + getString(R.string.GPS_longitude) + ":" + format.format(SelectedLongitude / 1e6));
		}

	}

	public void GetWPOnClick(View v) {
		for (int i = 0; i < 16; i++) {
			app.mw.SendRequestGetWayPoint(i);
		}
	}

	public void SetWPOnClick(View v) {

		app.mw.SendRequestMSP_SET_WP(new Waypoint(0, (int) (SelectedLatitude * 10), (int) (SelectedLongitude * 10), 0, 0));

		if (app.D) {
			app.mw.Waypoints[0].Lat = (int) (SelectedLatitude * 10);
			app.mw.Waypoints[0].Lon = (int) (SelectedLongitude * 10);

			app.mw.Waypoints[16].Lat = (int) (SelectedLatitude * 10 + 100);
			app.mw.Waypoints[16].Lon = (int) (SelectedLongitude * 10 + 100);
		}
	}

	void displayWPs() {
		// for (Waypoint w : app.mw.Waypoints) {
		// TVData.append("WP#" + String.valueOf(w.Number) + " " +
		// String.valueOf(w.Lat) + "x" + String.valueOf(w.Lon) + " Alt:" +
		// String.valueOf(w.Alt) + " NavFlag:" + String.valueOf(w.NavFlag) +
		// "\n");
		// }

		TVData.append("WP#" + String.valueOf(app.mw.Waypoints[0].Number) + " " + String.valueOf(app.mw.Waypoints[0].Lat) + "x" + String.valueOf(app.mw.Waypoints[0].Lon) + " Alt:" + String.valueOf(app.mw.Waypoints[0].Alt) + " NavFlag:" + String.valueOf(app.mw.Waypoints[0].NavFlag) + "\n");
		TVData.append("WP#" + String.valueOf(app.mw.Waypoints[16].Number) + " " + String.valueOf(app.mw.Waypoints[16].Lat) + "x" + String.valueOf(app.mw.Waypoints[16].Lon) + " Alt:" + String.valueOf(app.mw.Waypoints[16].Alt) + " NavFlag:" + String.valueOf(app.mw.Waypoints[16].NavFlag) + "\n");

	}

	@Override
	protected void onResume() {
		super.onResume();
		app.ForceLanguage();
		// app.Say(getString(R.string.Motors));
		killme = false;
		mHandler.postDelayed(update, app.RefreshRate);

	}

	@Override
	protected void onPause() {
		super.onPause();
		mHandler.removeCallbacks(null);
		killme = true;

	}
	
	
	public void ComunityMapOnClick(View v)
	{
		ComunityMap comunityMap= new ComunityMap();
		comunityMap.send(SelectedLatitude, SelectedLongitude, "test");
	}

}
