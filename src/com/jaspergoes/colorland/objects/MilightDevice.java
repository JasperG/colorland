package com.jaspergoes.colorland.objects;

public class MilightDevice implements Comparable<MilightDevice>
{

	public final String addrIP;
	public final String addrMAC;

	public MilightDevice(String addrIP, String addrMAC)
	{
		this.addrIP = addrIP;
		this.addrMAC = addrMAC;
	}

	@Override
	public int compareTo(MilightDevice compareTo)
	{
		return this.addrIP.compareTo(compareTo.addrIP);
	}

}
