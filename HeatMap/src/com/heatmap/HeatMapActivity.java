/*
 * Author: K. Reed Bialousz
 * 5/7/2015
 * 
 * This Heat Map supports destructive interference between positive and negative edge weights.
 * RawData is a view-model for weighted longitude-latitude points.
 * Data is a view-model for the clustered data.
 * 
 * To utilize this, enter your API key in the manifest and use the RawData view-model (you shouldn't
 * need to touch the Data view-model).
 */


package com.heatmap;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.Set;
import java.util.TreeMap;
import java.util.Map.Entry;

import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.GoogleMap.OnCameraChangeListener;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.CameraPosition;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.PolygonOptions;

import android.graphics.Color;
import android.location.Criteria;
import android.location.Location;
import android.location.LocationManager;
import android.os.Bundle;
import android.support.v4.app.FragmentActivity;
import android.view.Menu;

public class HeatMapActivity extends FragmentActivity
{
	GoogleMap map;
	private static float lastZoom = 1.0f;
	
	//These constants were hard to name. They can be tweaked to scale the clustering. 
	private static int customX = 300;
	public static final int CENTER_TO_ADJACENT_RATIO = 4;
	
	//This boolean determines if points affect surrounding clusters. 
	public static final boolean AOE = true; 
	
	public ArrayList<RawData> rawData;
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_map);
		SupportMapFragment mf = (SupportMapFragment) getSupportFragmentManager().findFragmentById(R.id.map);
		mf.getMap().setMyLocationEnabled(true);
		map = mf.getMap();
		map.setOnCameraChangeListener(autoZoomListener);
		
		
		//These are example points:
		//TODO: implement your points here:
		rawData = new ArrayList<RawData>();
		rawData.add(new RawData(10.0,10.0,1));
		rawData.add(new RawData(11.0,11.0,-1));
		
		
		
		Set<Entry<Point, Data>> data = geoDataToTreeMap(rawData,customX).entrySet();
		Iterator<Entry<Point, Data>> it = data.iterator();
		
		for (int q = 0; q <data.size();q++)
		{
			Entry<Point, Data> e = it.next();
			double H = 1.0/customX;
			double R = H / Math.sqrt(3);
			double S = R * 1.5;
			double W = 2*R;
			int i = e.getKey().getX();
			int j = e.getKey().getY();
			double topLeftX = e.getKey().getX() * S;
			double topLeftY = j*H + (i%2) * H / 2;
			topLeftX-=180;
			topLeftY-=80;
			PolygonOptions po = new PolygonOptions();
			int sum = e.getValue().getSum();
			double weight = 1.0 - Math.log(4)/Math.log(Math.abs(sum)+4);
			if (sum < 0)
			{
				po.fillColor(Color.argb((int) (100*weight), 0, 0, 255));
			}
			else if (sum > 0)
			{
				po.fillColor(Color.argb((int) (100*weight),255, 0, 0));
			}
			else 
			{
				po.fillColor(Color.argb(0,0,0,0));
			}
			map.addPolygon(po.strokeColor(Color.argb(0, 0, 0, 255))
					.add(new LatLng(topLeftY,topLeftX+R/2))// upper left
					.add(new LatLng(topLeftY,topLeftX+S)) //upper right
					.add(new LatLng(topLeftY+H/2,topLeftX+W)) // right 
					.add(new LatLng(topLeftY+H,topLeftX+S)) //lower right
					.add(new LatLng(topLeftY+H,topLeftX+R/2)) //lower left
					.add(new LatLng(topLeftY+H/2,topLeftX)) // left
					);
		}
		
		LocationManager locationManager = (LocationManager) getSystemService(LOCATION_SERVICE);
		if (locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER ))
		{
			Criteria crit = new Criteria();
			crit.setAccuracy(Criteria.ACCURACY_FINE);
			Location location = locationManager.getLastKnownLocation(locationManager.getBestProvider(crit, true));
			if (location != null)
				map.animateCamera(CameraUpdateFactory.newLatLngZoom(new LatLng(location.getLatitude(),location.getLongitude()),13));
		}
		
	}
	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		// Inflate the menu; this adds items to the action bar if it is present.
		getMenuInflater().inflate(R.menu.main, menu);
		
		return false;
	}
	
	private OnCameraChangeListener autoZoomListener = new OnCameraChangeListener()
	{
		@Override
		public void onCameraChange(CameraPosition pos) 
		{
			if (lastZoom != pos.zoom)
			{
				lastZoom = pos.zoom;
			}
			else
				return;  //no zoom change at all so don't do anything
			int newCustomX;
			if (lastZoom < 4.0f)
				newCustomX = 1;
			else if (lastZoom < 6.0f)
				newCustomX = 2;
			else if (lastZoom < 8.0f)
				newCustomX = 5;
			else if (lastZoom < 9.5f)
				newCustomX = 12;
			else if (lastZoom < 11.0f)
				newCustomX = 25;
			else if (lastZoom < 12.5f)
				newCustomX = 100;
			else if (lastZoom < 14.0f)
				newCustomX = 200;
			else if (lastZoom < 17.0f)
				newCustomX = 750;
			else
				newCustomX = 1000;
			if (newCustomX == customX)
				return;                 // Zoom didn't exit zoom range so don't do anything
			customX = newCustomX;
			RedrawHexagons();
		}
	};
	
	private void RedrawHexagons()
	{
		map.clear();
		Set<Entry<Point, Data>> data = geoDataToTreeMap(rawData,customX).entrySet();
		Iterator<Entry<Point, Data>> it = data.iterator();
		for (int q = 0; q <data.size();q++)
		{
			Entry<Point, Data> e = it.next();
			double H = 1.0/customX;
			double R = H / Math.sqrt(3);
			double S = R * 1.5;
			double W = 2*R;
			int i = e.getKey().getX();
			int j = e.getKey().getY();
			double topLeftX = e.getKey().getX() * S;
			double topLeftY = j*H + (i%2) * H / 2;
			topLeftX-=180;
			topLeftY-=80;
			PolygonOptions po = new PolygonOptions();
			int sum = e.getValue().getSum();
			double weight = 1.0 - Math.log(4)/Math.log(Math.abs(sum)+4);
			if (sum < 0)
			{
				po.fillColor(Color.argb((int) (100*weight), 0, 0, 255));
			}
			else if (sum > 0)
			{
				po.fillColor(Color.argb((int) (100*weight),255, 0, 0));
			}
			else 
			{
				po.fillColor(Color.argb(0,0,0,0));
			}
			map.addPolygon(po.strokeColor(Color.argb(0, 0, 0, 255))
					.add(new LatLng(topLeftY,topLeftX+R/2))// upper left
					.add(new LatLng(topLeftY,topLeftX+S)) //upper right
					.add(new LatLng(topLeftY+H/2,topLeftX+W)) // right 
					.add(new LatLng(topLeftY+H,topLeftX+S)) //lower right
					.add(new LatLng(topLeftY+H,topLeftX+R/2)) //lower left
					.add(new LatLng(topLeftY+H/2,topLeftX)) // left
					);
		}
	}
	
	
	//This function converts rawData about the lnglat points and weights into clustered Data. 
	public static TreeMap<Point,Data> geoDataToTreeMap(ArrayList<RawData> rawdata, int XValue)
	{
		TreeMap<Point,Data> treemap = new TreeMap<Point,Data>();
		if (rawdata == null || rawdata.isEmpty())
			return treemap;
		for (int q = 0; q < rawdata.size();q++)
		{
			//Math! Yay!    @.@
			double H = 1.0/XValue;
			double y = rawdata.get(q).getLat() + 80;
			double x = rawdata.get(q).getLong() + 180;
			double R = H / Math.sqrt(3);
			double S = R * 1.5;
			
			int it = (int)(x/S);
			double yts = y - (it%2)*H/2;
			int jt = (int) (yts/H);
			double xt = x - it*S;
			double yt = yts - jt*H;
			int i,j;
			if (xt > R*Math.abs(.5 - yt/H))
			{
				i = it;
			}
			else
			{
				i = it-1;
			}
			int dj = 0;
			if (yt > H/2)
			{
				dj = 1;
			}
			if (xt > R*Math.abs(.5 - yt/H))
			{
				j = jt;
			}
			else
			{
				j = jt - (i%2) + dj;
			}
			Point p = new Point(i,j);
			if (treemap.containsKey(p))
			{
				treemap.get(p).addWeight(CENTER_TO_ADJACENT_RATIO * rawdata.get(q).Weight);
			}
			else
			{
				Data d = new Data();
				treemap.put(p, d);
				d.addWeight(CENTER_TO_ADJACENT_RATIO * rawdata.get(q).Weight);
			}
			if (AOE)
			{
				Point[] adjacent = new Point[6];
				if (i%2 == 0)
				{
					adjacent[0] = new Point(i,j-1); // up
					adjacent[1] = new Point(i+1,j-1); //up right
					adjacent[2] = new Point(i+1,j); // down right
					adjacent[3] = new Point(i,j+1); // down
					adjacent[4] = new Point(i-1,j); // down left
					adjacent[5] = new Point(i-1,j-1); // up left
				}
				else
				{
					adjacent[0] = new Point(i,j-1); // up
					adjacent[1] = new Point(i+1,j); //up right
					adjacent[2] = new Point(i+1,j+1); // down right
					adjacent[3] = new Point(i,j+1); // down
					adjacent[4] = new Point(i-1,j+1); // down left
					adjacent[5] = new Point(i-1,j); // up left
				}
				for (int qq =0; qq < adjacent.length;qq++)
				{
					if (treemap.containsKey(adjacent[qq]))
					{
						treemap.get(adjacent[qq]).addWeight(rawdata.get(q).Weight);
					}
					else
					{
						Data d = new Data();
						treemap.put(adjacent[qq], d);
						d.addWeight(rawdata.get(q).Weight);
					}
				}
			}
		}
		return treemap;
	}
	
}


