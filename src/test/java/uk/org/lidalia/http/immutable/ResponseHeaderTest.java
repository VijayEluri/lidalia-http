package uk.org.lidalia.http.immutable;

import static uk.org.lidalia.testutils.Assert.shouldThrow;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertSame;
import static org.powermock.api.easymock.PowerMock.createMockAndExpectNew;
import static org.powermock.api.easymock.PowerMock.replayAll;
import static org.powermock.api.easymock.PowerMock.verifyAll;

import java.util.StringTokenizer;
import java.util.concurrent.Callable;

import org.apache.commons.lang.StringUtils;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.powermock.core.classloader.annotations.PrepareForTest;
import org.powermock.modules.junit4.PowerMockRunner;

import uk.org.lidalia.http.exception.InvalidHeaderException;
import uk.org.lidalia.http.immutable.ResponseHeader;
import uk.org.lidalia.http.response.Reason;
import uk.org.lidalia.http.response.ResponseCode;

@RunWith(PowerMockRunner.class)
@PrepareForTest(ResponseHeader.class)
public class ResponseHeaderTest {
	
	@Test
	public void split() {
		assertEquals(3, "a  b".split(" ").length);
	}

	@Test
	public void stringConstructorParsesCodeAndReasonAndConstructsHeaderFieldsWithEmptyString() throws Exception {
		HeaderFields headerFieldsMock = createMockAndExpectNew(HeaderFields.class, "");
		replayAll();
		
		ResponseHeader header = new ResponseHeader("HTTP/1.1 200 OK here\r\n");
		assertSame(ResponseCode.OK, header.getCode());
		assertEquals(new Reason("OK here"), header.getReason());
		assertSame(headerFieldsMock, header.getHeaderFields());
		
		verifyAll();
	}
	
	@Test
	public void stringConstructorCanHaveEmptyReason() throws Throwable {
		HeaderFields headerFieldsMock = createMockAndExpectNew(HeaderFields.class, "");
		replayAll();
		
		ResponseHeader header = new ResponseHeader("HTTP/1.1 200 \r\n");
		assertSame(ResponseCode.OK, header.getCode());
		assertEquals(new Reason(""), header.getReason());
		assertSame(headerFieldsMock, header.getHeaderFields());
		
		verifyAll();
	}
	
	@Test
	public void stringConstructorRequiresACRLF() throws Throwable {
		InvalidHeaderException exception = shouldThrow(InvalidHeaderException.class, new Callable<Void>() {
			@Override
			public Void call() throws Exception {
				new ResponseHeader("HTTP/1.1 200 OK");
				return null;
			}
		});
		
		assertEquals("Unable to parse HTTP/1.1 200 OK into a valid HTTP Header", exception.getMessage());
		assertSame(IllegalArgumentException.class, exception.getCause().getClass());
		assertEquals("Header String should end with a CRLF", exception.getCause().getMessage());
	}
	
	@Test
	public void stringConstructorEndsWithACRLF() throws Throwable {
		InvalidHeaderException exception = shouldThrow(InvalidHeaderException.class, new Callable<Void>() {
			@Override
			public Void call() throws Exception {
				new ResponseHeader("HTTP/1.1 200 OK\r\nheader: value");
				return null;
			}
		});
		
		assertEquals("Unable to parse HTTP/1.1 200 OK\r\nheader: value into a valid HTTP Header", exception.getMessage());
		assertSame(IllegalArgumentException.class, exception.getCause().getClass());
		assertEquals("Header String should end with a CRLF", exception.getCause().getMessage());
	}
	
	@Test
	public void stringConstructorThrowInvalidHeaderExceptionWhenFirstElementInStatusLineIsNotHTTPVersion() throws Throwable {
		InvalidHeaderException exception = shouldThrow(InvalidHeaderException.class, new Callable<Void>() {
			@Override
			public Void call() throws Exception {
				new ResponseHeader("blah 200 OK\r\n");
				return null;
			}
		});
		
		assertEquals("Unable to parse blah 200 OK\r\n into a valid HTTP Header", exception.getMessage());
		assertSame(IllegalArgumentException.class, exception.getCause().getClass());
		assertEquals("Header must start with HTTP/1.1", exception.getCause().getMessage());
	}
	
	@Test
	public void stringConstructorThrowInvalidHeaderExceptionWhenOnlyOneElementInStatusLine() throws Throwable {
		InvalidHeaderException exception = shouldThrow(InvalidHeaderException.class, new Callable<Void>() {
			@Override
			public Void call() throws Exception {
				new ResponseHeader("HTTP/1.1\r\n");
				return null;
			}
		});
		
		assertEquals("Unable to parse HTTP/1.1\r\n into a valid HTTP Header", exception.getMessage());
		assertSame(IllegalArgumentException.class, exception.getCause().getClass());
		assertEquals("Status line must contain at least two spaces", exception.getCause().getMessage());
	}
	
	@Test
	public void stringConstructorThrowsInvalidHeaderExceptionWhenNoSpaceAfterCode() throws Throwable {
		InvalidHeaderException exception = shouldThrow(InvalidHeaderException.class, new Callable<Void>() {
			@Override
			public Void call() throws Exception {
				new ResponseHeader("HTTP/1.1 200\r\n");
				return null;
			}
		});
		
		assertEquals("Unable to parse HTTP/1.1 200\r\n into a valid HTTP Header", exception.getMessage());
		assertSame(IllegalArgumentException.class, exception.getCause().getClass());
		assertEquals("Status line must contain at least two spaces", exception.getCause().getMessage());
	}
}