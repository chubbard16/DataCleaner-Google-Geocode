package com.neopost.datacleaner.extension;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.UnsupportedEncodingException;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLEncoder;

import javax.inject.Inject;
import javax.inject.Named;

import org.apache.commons.lang3.exception.ExceptionUtils;
import org.datacleaner.api.Categorized;
import org.datacleaner.components.categories.LocationCategory;
import org.datacleaner.api.Configured;
import org.datacleaner.api.Description;
import org.datacleaner.api.InputColumn;
import org.datacleaner.api.InputRow;
import org.datacleaner.api.MappedProperty;
import org.datacleaner.api.OutputColumns;
import org.datacleaner.api.OutputRowCollector;
import org.datacleaner.api.Provided;
import org.datacleaner.api.Transformer;


import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.neopost.datacleaer.extension.googlejson.GoogleReturnTopLevel;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
/**
 * 
 * <p>This is a DataCleaner component that calls the google geocode service to obtain the longitude and latitude for a given address.</p> <p>More information is returned from the call to google and if required it would be possible to return this information to Data Cleaner along with the long/lat. Some of this information is already captured within 
 * the {@link com.neopost.datacleaer.extension.googlejson} classes.</p>
 * <p>The class, via various methods can report, in the status column, four types of error groupings <i>URL Encoding Error,IO Error, Response Error, Google Error</i>. 
 * Each error may have a specific root cause but will have been reported by a specific action.</p>
 * <ul>
 * <li><p><b>URL Encoding Error : </b>When building the url to be sent to google it is encoded with {@link java.net.URLEncoder.encode} which may throw an exception when encoding.</p></li>
 * <li><p><b>IO Error : </b>If while connecting to the google service or reading the response stream this error will be reported.</p></li>
 * <li><p><b>Response Error : </b>After establishing a connection it is possible a none OK response (not 200) is returned, this error captures that response code and message</p></li>
 * <li><p><b>Google Error : </b>A google status is returned if for example the calls exceeding the tick limit or exceeding the throughput limit on your account.</p>
 * </ul>
 * <p> A simple error status is also returned which can be used to test the if any error was detected when calling the service.</p>
 * 
 *
 */

@Named("Google GeoCode")
@Description("Returns google GeoCode information for a provided address")
@Categorized(LocationCategory.class) //need to appear in the improver category, probably under google, but location is good for now
public class GoogleGeoCode implements Transformer {
	
	public enum AddressInputType {

		ADDRESS1,ADDRESS2,CITY,POSTALCODE,COUNTRY
	}

	private static final Logger logger = LoggerFactory.getLogger(GoogleGeoCode.class);
	
    public static final String PROPERTY_COLUMNS = "Address part columns";
    private static final String GOOGLE_STRING = "(Google GeoCode)";
    private static final String LONGITUDE_OUTPUT_COLUMN = "Longitude "+GOOGLE_STRING;
    private static final String LATITUDE_OUTPUT_COLUMN = "Latitude "+GOOGLE_STRING;
    private static final String STATUS_OUTPUT_COLUMN = "Status "+GOOGLE_STRING;
    private static final String STATUS_MESSAGE_OUTPUT_COLUMN = "Status Message "+GOOGLE_STRING;
    private static final String STATUS_OUTPUT_OK = "OK"; //Default status output
    private static final String STATUS_OUTPUT_ERROR = "ERROR"; //Default status output
    private static final String REQUEST_STRING_KEY= "&key="; //Appended to the url and followed by the google key
    
    private static final String GOOGLE_ERROR_STRING = "Google Error : ";
    private static final String RESPONSE_ERROR_STRING = "Response Error : ";
    private static final String ENCODING_ERROR_STRING = "URL Encoding Error :";
    private static final String IO_ERROR_STRING ="IO Error : ";
	@Configured(order = 1)
	@Description("Select the address columns to send")
	InputColumn<String>[] addressPartColumns;
	 
	@Configured
	@MappedProperty(PROPERTY_COLUMNS)
	AddressInputType[] addressParts;
	 
	@Inject
	@Provided
	OutputRowCollector _outputRowCollector;
	
	@Inject
	@Configured(value="API Key", required=false)
	@Description("Google Geo authentication API key")
	String API_Key = "";
	
	//Static address for the google service.
	private static final String GOOGLE_GEO_API_URL = "https://maps.googleapis.com/maps/api/geocode/json?address=";
	//Need when sending the request to google, without this the request fails.
	private final String USER_AGENT = "Mozilla/5.0";
	//Could make this an optional input, to enable a user to test without hitting the google service.
	private final boolean DEBUG_MODE = false;
	 
	@Override
	public OutputColumns getOutputColumns() {
		 String[] names = new String[4];
		 names[0] =LONGITUDE_OUTPUT_COLUMN;
		 names[1] =LATITUDE_OUTPUT_COLUMN;
		 names[2] =STATUS_OUTPUT_COLUMN;
		 names[3] =STATUS_MESSAGE_OUTPUT_COLUMN;
		 return new OutputColumns(String.class, names);
	}


	/**
	 * Given the address lines this method creates the google url, submits the requests, and unpacks the response into a string array
	 * @return An array of 3 string containing the latitude, longitude and request status 
	 */
	
	public String[] transform(InputRow inputRow) {
		
		 StringBuffer requestString = new StringBuffer(GOOGLE_GEO_API_URL); 
		
		 String[] orderedArray = new String[6];
		 try {
			 for (int j = 0; j < addressParts.length;j++) {
				 InputColumn<?> input = addressPartColumns[j];
				 switch (addressParts[j]) {
				 case COUNTRY:
					 orderedArray[4] = URLEncoder.encode(inputRow.getValue(input).toString().trim(),"UTF-8");
					 break;
				 case ADDRESS1:
					 orderedArray[0] = URLEncoder.encode(inputRow.getValue(input).toString().trim(),"UTF-8");
					 break;
				 case ADDRESS2:
					 orderedArray[1] = URLEncoder.encode(inputRow.getValue(input).toString().trim(),"UTF-8");
					 break;
				 case CITY:
					 orderedArray[2] = URLEncoder.encode(inputRow.getValue(input).toString().trim(),"UTF-8");
					 break;
				 case POSTALCODE:
					 orderedArray[3] = URLEncoder.encode(inputRow.getValue(input).toString().trim(),"UTF-8");
					 break;
				 default:
					 //Should never get here unless there is a programming error.
					 orderedArray[5] = orderedArray[5]+"+"+URLEncoder.encode(inputRow.getValue(input).toString().trim(),"UTF-8");	 
				 }//case
				
			 }//for j
		 } catch (UnsupportedEncodingException e) {
				logger.error("Error encoding google geocode url", e);
				return new String[] {"","",STATUS_OUTPUT_ERROR,ENCODING_ERROR_STRING+ExceptionUtils.getRootCauseMessage(e)};
		 }//try-catch
		 
		 for (int i = 0;i < orderedArray.length;i++) {
			 if (orderedArray[i] !=null) {
				requestString.append(orderedArray[i]);
			 	
			 }
			 if ((i < orderedArray.length)&&(orderedArray[i] !=null)) requestString.append("+");
		 }
		//Add the API key.
		 requestString.append(REQUEST_STRING_KEY);
		 requestString.append(API_Key);
		 logger.debug("Google request "+requestString.toString());
	
		//Create google geocode request, debug will just return a pre-built string.
		 String resultJSON ="";
		 if (DEBUG_MODE) {
			 resultJSON = doGetRequestDebug(requestString.toString());
		 } else {
			 try {
				 resultJSON = doGetRequest(requestString.toString());
			} catch (IOException  e) {
		
				logger.error("Error calling google geocode service", e);
				return new String[] {"","",STATUS_OUTPUT_ERROR,IO_ERROR_STRING+ExceptionUtils.getRootCauseMessage(e)};
			} catch (HttpRequestException httpe) {
				logger.error("Error calling google geocode service", httpe);
				return new String[] {"","",STATUS_OUTPUT_ERROR,RESPONSE_ERROR_STRING+ExceptionUtils.getRootCauseMessage(httpe)};
			}//try-catch
		 }//if debug;
		logger.debug(resultJSON);
	
		String longLat[] = readGoogleResult(resultJSON);
		return new String[] {longLat[0],longLat[1],longLat[2],longLat[3]};
	}
	/**
	 * Used for debug purposes 
	 * @param url url to the service, ignored.
	 * @return
	 */
	private  String doGetRequestDebug(String url) {
		return "{   \"results\" : [      {         \"address_components\" : [            {               \"long_name\" : \"310\",               \"short_name\" : \"310\",               \"types\" : [ \"street_number\" ]            },            {               \"long_name\" : \"Utrechtseweg\",               \"short_name\" : \"Utrechtseweg\",               \"types\" : [ \"route\" ]            },            {               \"long_name\" : \"Arnhem\",               \"short_name\" : \"Arnhem\",               \"types\" : [ \"locality\", \"political\" ]            },            {               \"long_name\" : \"Arnhem\",               \"short_name\" : \"Arnhem\",               \"types\" : [ \"administrative_area_level_2\", \"political\" ]            },            {               \"long_name\" : \"Gelderland\",               \"short_name\" : \"GE\",               \"types\" : [ \"administrative_area_level_1\", \"political\" ]            },            {               \"long_name\" : \"Netherlands\",               \"short_name\" : \"NL\",               \"types\" : [ \"country\", \"political\" ]            },            {               \"long_name\" : \"6812 AR\",               \"short_name\" : \"6812 AR\",               \"types\" : [ \"postal_code\" ]            }         ],         \"formatted_address\" : \"Utrechtseweg 310, 6812 AR Arnhem, Netherlands\",         \"geometry\" : {            \"location\" : {               \"lat\" : 51.9842339,               \"lng\" : 5.870231            },            \"location_type\" : \"ROOFTOP\",            \"viewport\" : {               \"northeast\" : {                  \"lat\" : 51.9855828802915,                  \"lng\" : 5.871579980291502               },               \"southwest\" : {                  \"lat\" : 51.9828849197085,                  \"lng\" : 5.868882019708497               }            }         },         \"place_id\" : \"ChIJyT3PoI2lx0cRlxOkK7xQdXE\",         \"types\" : [ \"street_address\" ]      }   ],   \"status\" : \"OK\"}";
	}
	/**
	 * Call the google api to retrieve the geo payload. Response codes are not currently handled, which will be an 
	 * issue if the request fails to return a JSON response.
	 * @param url the Google geocode api URL.
	 * @return A JSON string containing the response from the API
	 * @throws IOException Connection issues.
	 * @throws HttpRequestException 
	 */
	private  String doGetRequest(String url) throws IOException, HttpRequestException {
		URL obj = new URL(url);
		HttpURLConnection con = (HttpURLConnection) obj.openConnection();
		con.setRequestMethod("GET");
		con.setRequestProperty("User-Agent", USER_AGENT);
		int responseCode = con.getResponseCode();
		logger.debug("\nSending 'GET' request to URL : " + url);
		logger.debug("Response Code : " + responseCode);
		if (responseCode != HttpURLConnection.HTTP_OK) {
	
			throw new HttpRequestException(responseCode, con.getResponseMessage(),url);
		}
		BufferedReader in = new BufferedReader(new InputStreamReader(con.getInputStream()));
		String inputLine;
		StringBuffer response = new StringBuffer();

		while ((inputLine = in.readLine()) != null) {
			response.append(inputLine);
		}
		in.close();
		con.disconnect();
		return response.toString();
	}
	/**
	 * Returns the longitude and latitude extracted from a google geo json results string. This function only returns the first set of results from the response. The google 
	 * response hold many other nuggets of information that could be passed back.
	 * @param jsonString The JSON string containing the google results.
	 * @return Returns an array containing two strings, 1st contains the latitude and 2nd contains the longitude value.
	 */
	private String[] readGoogleResult(String jsonString) {
		
		String[] rtnResult = null;
		rtnResult = new String[] {"","",STATUS_OUTPUT_OK,""};
		
		if ((jsonString == null)||(jsonString.length()==0)) return rtnResult;
		Gson gson = new GsonBuilder().create();
		GoogleReturnTopLevel GoogleResult = gson.fromJson(jsonString, GoogleReturnTopLevel.class);
		if (GoogleResult.hasErrored()) {
			rtnResult[2] = STATUS_OUTPUT_ERROR;
			rtnResult[3] = GOOGLE_ERROR_STRING+GoogleResult.getError_message();
		}
		if (GoogleResult.resultLength() >0) {
			//Just read the first result not the best approach!!!
			//rtnResult = new String[2];
			rtnResult[0] = GoogleResult.getGeometryLongitude(0);
			rtnResult[1] = GoogleResult.getGeometryLatitude(0);
			
		}
		return rtnResult;
	}

}
