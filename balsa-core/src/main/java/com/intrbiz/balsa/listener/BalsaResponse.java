package com.intrbiz.balsa.listener;

import java.io.IOException;
import java.io.OutputStream;
import java.io.Writer;
import java.nio.charset.Charset;
import java.util.Date;
import java.util.List;

import com.intrbiz.balsa.error.BalsaInternalError;
import com.intrbiz.balsa.util.HTMLWriter;
import com.intrbiz.json.writer.JSONWriter;

/**
 * The current response
 */
public interface BalsaResponse
{
    /**
     * Common charsets
     */
    public static final class Charsets
    {
        public static final Charset UTF8 = Charset.forName("UTF-8");

        public static final Charset SCGI = Charset.forName("ISO-8859-1");
    }

    /**
     * Common content types
     */
    public static final class ContentTypes
    {
        public static final String TEXT_PLAIN = "text/plain";

        public static final String TEXT_HTML = "text/html";

        public static final String TEXT_CSS = "text/css";

        public static final String TEXT_JAVASCRIPT = "text/javascript";

        public static final String APPLICATION_JSON = "application/json";
    }
    
    /**
     * The response status
     */
    public static enum Status
    {
        /* 1xx */
        Continue(100, "Continue"),
        SwitchingProtocols(101, "Switching Protocols"),
        /* 2xx */
        OK(200, "OK"),
        Created(201, "Created"),
        Accepted(202, "Accepted"),
        NonAuthoritativeInformation(203, "Non-Authoritative Information"),
        NoContent(204, "No Content"),
        ResetContent(205, "Reset Content"),
        PartialContent(206, ""),
        /* 3xx */
        MultipleChoices(300, "Multiple Choices"),
        MovedPermanently(301, "Moved Permanently"),
        Found(302, "Found"),
        SeeOther(303, "See Other"),
        NotModified(304, "Not Modified"),
        UseProxy(305, "Use Proxy"),
        TemporaryRedirect(307, "Temporary Redirect"),
        /* 4xx */
        BadRequest(400, "Bad Request"),
        Unauthorized(401, "Unauthorized"),
        PaymentRequired(402, "Payment Required"),
        Forbidden(403, "Forbidden"),
        NotFound(404, "Not Found"),
        MethodNotAllowed(405, "Method Not Allowed"),
        NotAcceptable(406, "Not Acceptable"),
        ProxyAuthenticationRequired(407, "Proxy Authentication Required"),
        RequestTimeout(408, "Request Timeout"),
        Conflict(409, "Conflict"),
        Gone(410, "Gone"),
        LengthRequired(411, "Length Required"),
        PreconditionFailed(412, "Precondition Failed"),
        RequestEntityTooLarge(413, "Request Entity Too Large"),
        RequestURITooLong(414, "Request-URI Too Long"),
        UnsupportedMediaType(415, "Unsupported Media Type"),
        RequestedRangeNotSatisfiable(416, "Requested Range Not Satisfiable"),
        ExpectationFailed(417, "Expectation Failed"),
        /* 5xx */
        InternalServerError(500, "Internal Server Error"),
        NotImplemented(501, "Not Implemented"),
        BadGateway(502, "Bad Gateway"),
        ServiceUnavailable(503, "Service Unavailable"),
        GatewayTimeout(504, "Gateway Timeout"),
        HTTPVersionNotSupported(505, "HTTP Version Not Supported");
        
        private final int    code;
        private final String message;
        
        private Status(int code, String message)
        {
            this.code = code;
            this.message = message;
        }
        
        public int getCode()
        {
            return this.code;
        }
        
        public String getMessage()
        {
            return this.message;
        }
    }
    
    public static final class CacheControl
    {
        public static final String NO_CACHE = "no-cache, no-store, max-age=0, must-revalidate";
    }
    
    public static final class Expires
    {
        public static final String EXPIRED = "Thu, 01 Jan 1970 00:00:00 GMT";
    }

    /**
     * Reset the response
     */
    public void deactivate();
    public void activate();
    
    /**
     * Abort the response because an error has happened while processing the request.
     * 
     * Note: A reset can only happen if the response has not sent any data to the web server.
     * As such if reset is called and isHeadersSent() returns true a balsaInternalError must be thrown
     *
     * @param t The error which caused the processing to be aborted.
     * @throws BalsaInternalError
     */
    public void abortOnError(Throwable t) throws BalsaInternalError;

    /**
     * The output stream of the response
     * 
     * @param output
     *            The output stream
     */
    public void stream(OutputStream output);

    /**
     * Get the response HTTP status
     * 
     * @return returns int
     */
    public Status getStatus();

    /**
     * Set the status of the HTTP response
     * @param status 
     * returns void
     */
    public void status(Status status);

    /**
     * Set the HTTP response status to: 200
     */
    public void ok();

    /**
     * Set the HTTP response status to: 404
     */
    public void notFound();

    /**
     * Set the HTTP response status to: 500
     */
    public void error();

    /**
     * Set the HTTP response status to: 302
     */
    public void redirect(boolean permanent);

    /**
     * Get the character set of the response
     * 
     * @return returns Charset
     */
    public Charset getCharset();

    /**
     * Set the character set of the response
     * 
     * @param charset
     *            returns void
     */
    public void charset(Charset charset);

    /**
     * Get the response content type
     * 
     * @return returns String
     */
    public String getContentType();

    /**
     * Set the response content type
     * 
     * @param contentType
     *            The MIME content type
     */
    public void contentType(String contentType);

    /**
     * Set the response content type to: text/plain
     */
    public void plain();

    /**
     * Set the response content type to: text/html
     */
    public void html();

    /**
     * Set the response content type to: text/javascript
     */
    public void javascript();

    /**
     * Set the response content type to: application/json
     */
    public void json();

    /**
     * Set the response content type to: text/css
     */
    public void css();
    
    /**
     * Get the cache control header
     * @return
     * returns String
     */
    public String getCacheControl();
    
    /**
     * Set the cache control header
     * @param value
     * returns void
     */
    public void cacheControl(String value);
    
    /**
     * Get the expires header
     * @return
     * returns String
     */
    public String getExpires();
    
    /**
     * Set the expires header
     * @param value
     * returns void
     */
    public void expires(String value);
    public void expires(Date value);

    /**
     * Add a HTTP header to the response
     * 
     * @param name
     * @param value
     */
    public void header(String name, String value);
    
    public void header(String name, Date value);

    /**
     * Issue a redirect to the given location
     * 
     * @param location
     *            The redirection URL
     */
    public void redirect(String location, boolean permanent) throws IOException;

    /**
     * Get the headers which have been added to the response
     * 
     * @return returns List<String>
     */
    public List<String> getHeaders();

    /**
     * Flush the headers to the web server
     * 
     * @throws IOException
     */
    public void sendHeaders() throws IOException;

    /**
     * Get the raw response output stream
     * 
     * @return
     * @throws IOException
     *             returns OutputStream
     */
    public OutputStream getOutput() throws IOException;

    /**
     * Get the response writer (using the selected charset)
     * 
     * @return
     * @throws IOException
     *             returns Writer
     */
    public Writer getWriter() throws IOException;

    /**
     * Get the response JSON writer
     * 
     * @return
     * @throws IOException
     *             returns JSONWriter
     */
    public JSONWriter getJsonWriter() throws IOException;

    /**
     * Get the response HTML writer
     * 
     * @return
     * @throws IOException
     *             returns HTMLWriter
     */
    public HTMLWriter getHtmlWriter() throws IOException;
    
    /**
     * Flush this response to the web server
     * @throws IOException
     * returns void
     */
    public void flush() throws IOException;
    
    /**
     * Have the headers been sent to the web server
     */
    public boolean isHeadersSent();
}
