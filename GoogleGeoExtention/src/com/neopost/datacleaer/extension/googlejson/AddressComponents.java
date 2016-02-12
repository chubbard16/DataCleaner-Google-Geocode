package com.neopost.datacleaer.extension.googlejson;

public class AddressComponents {
	String long_name;
	String short_name;
	
	@Override
    public String toString() {
        return long_name+" "+short_name;
    }
}
