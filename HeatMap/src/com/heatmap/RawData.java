package com.heatmap;

import java.io.Serializable;

public class RawData implements Serializable
{
	private static final long serialVersionUID = 69;
	public double Long, Lat;
	public int Weight;
	public RawData(double Long, double Lat, int Weight)
	{
		this.Long = Long;
		this.Lat = Lat;
		this.Weight = Weight;
	}
	public double getLong()
	{
		return Long;
	}
	public double getLat()
	{
		return Lat;
	}
	public double getWeight()
	{
		return Weight;
	}
}
