package se.funkabo.filter;

import java.io.IOException;

import javax.servlet.FilterChain;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import se.funkabo.service.AccountDetailsService;
import se.funkabo.utils.authorization.AuthorizationUtils;

@Component("authenticationFilter")
public class AuthenticationFilter extends OncePerRequestFilter {
	
	private static final Logger log = LoggerFactory.getLogger(AuthenticationFilter.class);

	@Autowired
	private AuthorizationUtils authorizationUtils;
	
	@Autowired
	private AccountDetailsService accountDetailsService;
	
	public AuthenticationFilter(AuthorizationUtils authorizationUtils, AccountDetailsService accountDetailsService) {
		this.authorizationUtils = authorizationUtils;
		this.accountDetailsService = accountDetailsService;
	}

	@Override
	protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain) throws IOException, ServletException  {
		String header = request.getHeader("Authorization");
		String token = "";
		String subject = "";
		if (StringUtils.isNotBlank(header) && header.startsWith("Bearer ")) {
			log.debug("");
			token = header.substring(7);
			subject = authorizationUtils.decrypt(token);

			if (subject != null && SecurityContextHolder.getContext().getAuthentication() == null) {
				UserDetails userDetails = accountDetailsService.loadUserByUsername(subject);
		        UsernamePasswordAuthenticationToken authentication = new UsernamePasswordAuthenticationToken(userDetails, null, userDetails.getAuthorities());
				authentication.setDetails(new WebAuthenticationDetailsSource().buildDetails(request));
				SecurityContextHolder.getContext().setAuthentication(authentication);
			}	
		}
		log.debug("");
		filterChain.doFilter(request, response);		
	 }

}
