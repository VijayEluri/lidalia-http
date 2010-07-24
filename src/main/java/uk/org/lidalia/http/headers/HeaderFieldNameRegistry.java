package uk.org.lidalia.http.headers;

import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

import org.apache.commons.lang.Validate;

import uk.org.lidalia.http.exception.IllegalHeaderFieldNameException;
import uk.org.lidalia.http.exception.IllegalTokenException;

public class HeaderFieldNameRegistry {

	private static final ConcurrentMap<String, HeaderFieldName> headerFieldNames = new ConcurrentHashMap<String, HeaderFieldName>();

	public static HeaderFieldName get(String headerName) throws IllegalHeaderFieldNameException {
		Validate.notNull(headerName, "headerName is null");
		HeaderFieldName headerFieldName = headerFieldNames.get(headerName.toLowerCase());
		if (headerFieldName == null) {
			try {
				headerFieldName = new DefaultHeaderFieldName(headerName);
			} catch (IllegalTokenException e) {
				throw new IllegalHeaderFieldNameException(headerName + " is not a legal header name", e);
			}
			HeaderFieldName actual = headerFieldNames.putIfAbsent(headerName.toLowerCase(), headerFieldName);
			if (actual != null) {
				headerFieldName = actual;
			}
		}
		return headerFieldName;
	}
	
	public static void registerHeaderFieldName(HeaderFieldName newHeaderFieldName) {
		Validate.notNull(newHeaderFieldName, "newHeaderFieldName is null");
		headerFieldNames.putIfAbsent(newHeaderFieldName.toString().toLowerCase(), newHeaderFieldName);
	}
	
	static {
		try {
			registerHeaderFieldName(new PositiveSecondsHeaderFieldName("Age"));
		} catch (IllegalTokenException e) {
			throw new IllegalStateException("It should not be possible to get an illegal token exception from any of the predefined header field names", e);
		}
	}
}
