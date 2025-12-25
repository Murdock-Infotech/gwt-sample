package murdockinfotech.server.filter;

import javax.servlet.Filter;
import javax.servlet.FilterChain;
import javax.servlet.FilterConfig;
import javax.servlet.ServletException;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;

/**
 * Prevent caching of GWT-generated *.nocache.js bootstrap files.
 *
 * <p>These files must never be cached by browsers/proxies, otherwise clients can get stuck on an old
 * permutation manifest after deployment.</p>
 */
public class GwtNoCacheFilter implements Filter {

    /**
     * RFC1123-formatted HTTP-date in the past; forces caches to treat the content as stale.
     */
    private static final String EXPIRES_IN_PAST = "Wed, 01 Nov 2000 00:00:00 GMT";

    @Override
    public void init(FilterConfig filterConfig) {
        // no-op
    }

    @Override
    public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain)
            throws IOException, ServletException {
        if (request instanceof HttpServletRequest && response instanceof HttpServletResponse) {
            HttpServletRequest httpRequest = (HttpServletRequest) request;
            HttpServletResponse httpResponse = (HttpServletResponse) response;

            String uri = httpRequest.getRequestURI();
            if (uri != null && uri.endsWith(".nocache.js")) {
                long now = System.currentTimeMillis();
                httpResponse.setDateHeader("Date", now);
                httpResponse.setHeader("Expires", EXPIRES_IN_PAST);
                httpResponse.setHeader("Pragma", "no-cache");
                httpResponse.setHeader("Cache-Control", "no-cache, no-store, must-revalidate");
            }
        }

        chain.doFilter(request, response);
    }

    @Override
    public void destroy() {
        // no-op
    }
}


