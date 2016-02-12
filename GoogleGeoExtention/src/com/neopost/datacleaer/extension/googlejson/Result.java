package com.neopost.datacleaer.extension.googlejson;

public class Result {
	private AddressComponents[] address_components;
	private String formatted_address;
	private String place_id;
	private Geometry geometry;
	//private String types;
	@Override
    public String toString() {
		String lineSep = System.getProperty("line.separator");
		if (address_components.length > 0)
			return "Formatted : "+formatted_address+lineSep+
					"Place ID : "+place_id+lineSep+
					"Components : "+address_components[0].toString()+lineSep+
					"Geometry : "+geometry;
        return "Formatted : "+formatted_address+lineSep+
				"Place ID : "+place_id+lineSep+
				"Geometry : "+geometry;

    }
	public String getFormatted_address() {
		return formatted_address;
	}
	public String getPlace_id() {
		return place_id;
	}
	public Geometry getGeometry() {
		return geometry;
	}
	//public String getTypes() {
//		return types;
//	}

}
