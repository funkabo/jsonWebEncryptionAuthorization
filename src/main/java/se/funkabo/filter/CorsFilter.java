package se.funkabo.filter;

import java.io.IOException;

import javax.servlet.Filter;
import javax.servlet.FilterChain;
import javax.servlet.FilterConfig;
import javax.servlet.ServletException;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import javax.servlet.http.HttpServletResponse;

import org.springframework.stereotype.Component;

@Component
public class CorsFilter implements Filter {

	@Override
    public void init(FilterConfig filterConfig) throws ServletException {}
	
	@Override
	public void doFilter(ServletRequest request, ServletResponse response, FilterChain filterChain) throws IOException, ServletException {
		HttpServletResponse responseHeader = (HttpServletResponse) response;

		responseHeader.setHeader("Access-Control-Allow-Origin", "*");
		responseHeader.setHeader("Access-Control-Expose-Headers", "Authorization");
		responseHeader.setHeader("Access-Control-Allow-Methods", "GET, POST, PUT, DELETE, OPTIONS");
		responseHeader.setHeader("Access-Control-Max-Age", "3600");
		responseHeader.setHeader("Access-Control-Allow-Credentials", "true");
		responseHeader.setHeader("Access-Control-Allow-Headers", "Content-Type, Authorization, Date");
		filterChain.doFilter(request, response);  
	}
	
	@Override
    public void destroy() {}

}
