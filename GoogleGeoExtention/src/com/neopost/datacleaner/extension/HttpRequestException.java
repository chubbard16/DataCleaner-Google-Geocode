package com.neopost.datacleaner.extension;

public class HttpRequestException extends Exception {

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	
	private int responseCode = 0;
	private String url = null;
	
	public HttpRequestException(int HttpResponseCode,String HttpResponseMessage,String httpUrl) {
		super("Error accessing "+httpUrl+" "+HttpResponseMessage);
		responseCode = HttpResponseCode;
		url = httpUrl;
	}
	/**
	 * Simply returns the http response code stored in this exception.
	 * @return The http response code
	 */
	public int getResponseCode() {
		return responseCode;
	}
	public String gerUrl() {
		return url;
	}
	
}
