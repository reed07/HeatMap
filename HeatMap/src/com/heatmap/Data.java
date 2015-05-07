package com.heatmap;

import java.io.Serializable;

public class Data implements Serializable
{
	private static final long serialVersionUID = 69L;
	int positive, negative;
	
	public Data()
	{
		positive = 0;
		negative = 0;
	}
	
	public double getWeight()
	{
		return (positive-negative)/((double)positive+negative);
	}
	
	public void addWeight(int i)
	{
		if (i>0)
			positive+=i;
		else
			negative+=-i;
	}
	
	public int getSum()
	{
		return positive - negative;
	}
}
