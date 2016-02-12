package com.neopost.datacleaer.extension.googlejson;

public class LatLong {
	String lat;
	String lng;
	
	public String getLat() {
		return lat;
	}
	public String getLng() {
		return lng;
	}
	
	@Override
    public String toString() {
		String lineSep = System.getProperty("line.separator");
        return "latitude "+ getLat()+lineSep+"Longitude "+getLng();
    }
}
