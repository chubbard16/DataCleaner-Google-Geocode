package com.neopost.datacleaer.extension.googlejson;

public class GoogleReturnTopLevel {
	private Result results[];
	private String status;
	private String error_message;
	/**
	 * returns the error message captured.
	 * @return A string containing the error message or null
	 */
	public String getError_message() {
		return error_message;
	}
	
	@Override
    public String toString() {
		if (results.length > 0)
			return status + " - " + results[0];
		if (error_message != null)
			return error_message+" - "+status;
		return status;
    }
	/**
	 * returns the length of the result list.
	 * @return The number of results in the result list.
	 */
	public int resultLength() {
		return results.length;
	}
	/**
	 * Returns true if results are returned from the request.
	 * @return returns true if the request returned results.
	 */
	public boolean hasResults() {
		if ((status.equalsIgnoreCase("OK"))&&(resultLength() > 0))
			return true;
		return false;
	}
	/**
	 * If the returned status is other than OK this method will return true;
	 * @return Returns true if an error is returned.
	 */
	public boolean hasErrored() {
		if (!status.equalsIgnoreCase("OK"))
				return true;
		return false;
	}
	/**
	 * Return the google status 
	 * @return The google status for request.
	 */
	public String getStatus() {
		return status;
	}
	/**
	 * This will return null if the result index is not found or the formatted address was not returned.
	 * @param resultIndex
	 * @return
	 */
	public String getFormattedAddress(int resultIndex) {
		if (resultIndex > resultLength()) return null;
		return results[resultIndex].getFormatted_address();
	}
	
	public String getGeometryLatitude(int resultIndex) {
		if (resultIndex > resultLength()) return null;
		Geometry geo = results[resultIndex].getGeometry();
		return geo.getLocation().getLat();
				
	}
	public String getGeometryLongitude(int resultIndex) {
		if (resultIndex > resultLength()) return null;
		Geometry geo = results[resultIndex].getGeometry();
		return geo.getLocation().getLng();
				
	}
	
}
