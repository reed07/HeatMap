package com.heatmap;

import java.io.Serializable;

public class Point implements Comparable<Point>, Serializable
{
	private static final long serialVersionUID = 69L;
	int x,y;
	public Point(int x, int y)
	{
		this.x = x;
		this.y = y;
	}
	public int getX()
	{
		return x;
	}
	public int getY()
	{
		return y;
	}
	@Override
	public int compareTo(Point another) 
	{
		
		if (this.equals(another))
			return 0;
		else
		{
			long a = (((long) another.getX())<<32) | another.getY();
			long t = (((long) this.x)<<32) | this.y;
			if (t < a)
			{
				return -1;
			}
			else
			{
				return 1;
			}
		}
	}
	public boolean equals(Point another)
	{
		if (another.getX() == this.x && another.getY() == this.y)
		{
			return true;
		}
		return false;
	}
}
