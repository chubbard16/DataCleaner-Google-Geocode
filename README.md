# DataCleaner-Google-Geocode
A DataCleaner Google Maps Geocoding API extension

This is a [DataCleaner] (https://github.com/datacleaner "DataCleaner") extension component that calls the google geocode api service to obtain the longitude and latitude for a given address.</p> <p>More information is returned from the call to google and if required it would be possible to return this information to Data Cleaner along with the long/lat. Some of this information is already captured within the com.neopost.datacleaer.extension.googlejson classes.</p>
<p>The class, via various methods, can report, in the status column, four types of error groupings <i>URL Encoding Error, IO Error, Response Error, Google Error</i>. Each error may have a specific root cause but will have been reported by a specific action.</p>
<p>There are a few points to consider when using this component</p>
* This component only returns the first result of from the Google API response, if a multiple response is returned on the first result is processed.
* The default/free usage of google API support a throughput of 10 requests per second, DC will exceed this throughput, no throttling is currently provided by this component.
* The google geocode API support regional variations, this is currently not supported. This feature should be easy to add.
