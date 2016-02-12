package com.neopost.datacleaer.extension.googlejson;

public class Geometry {
	String location_type;
	LatLong location;
	
	public String getLocation_type() {
		return location_type;
	}
	public LatLong getLocation() {
		return location;
	}
	
	@Override
    public String toString() {
		String lineSep = System.getProperty("line.separator");
        return "Location Type"+ getLocation_type()+lineSep+getLocation();
    }
}
