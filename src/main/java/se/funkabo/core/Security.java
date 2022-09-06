package se.funkabo.core;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;
import org.springframework.http.HttpMethod;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.config.annotation.authentication.builders.AuthenticationManagerBuilder;
import org.springframework.security.config.annotation.method.configuration.EnableGlobalMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.builders.WebSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configuration.WebSecurityConfigurerAdapter;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.security.web.session.SessionManagementFilter;

import se.funkabo.exception.UnauthorizedException;
import se.funkabo.filter.AuthenticationFilter;
import se.funkabo.filter.CorsFilter;
import se.funkabo.service.AccountDetailsService;
import se.funkabo.utils.authorization.AuthorizationUtils;


@Configuration
@EnableWebSecurity
@EnableJpaRepositories
@EnableGlobalMethodSecurity(prePostEnabled=true)
public class Security extends WebSecurityConfigurerAdapter {
	
	@Autowired
	private AccountDetailsService accountDetailsService;
	
	@Autowired
	private AuthorizationUtils authorizationUtils;

	@Autowired
	private UnauthorizedException unauthourized;
	
	
	@Autowired
	public AuthenticationFilter tokenAuthenticationFilter(){
		return new AuthenticationFilter(authorizationUtils, accountDetailsService);
	}
	
	@Autowired
	public CorsFilter corsFilter(){
		return new CorsFilter();
	}
	
	
	@Bean
    public PasswordEncoder bCryptEncoder() {
        return new BCryptPasswordEncoder();
    }
	
	@Bean
	@Override
	public AuthenticationManager authenticationManagerBean() throws Exception {
		return super.authenticationManagerBean();	
	}
	
	@Autowired
	public void configureGlobal(AuthenticationManagerBuilder authenticationManagerBuilder) throws Exception {
		authenticationManagerBuilder.userDetailsService(accountDetailsService)
									.passwordEncoder(bCryptEncoder());
	}
	
	@Override
	protected void configure(HttpSecurity http) throws Exception {		
		http.csrf()
			.disable();
		
		http.exceptionHandling()
			.authenticationEntryPoint(unauthourized)
			.and()
			.sessionManagement()
			.sessionCreationPolicy(SessionCreationPolicy.STATELESS);
			
		http.authorizeRequests()
			.antMatchers(HttpMethod.POST, "/login").permitAll()
			.antMatchers(HttpMethod.POST, "/register").permitAll()
			.antMatchers(HttpMethod.GET, "/confirmRegistration").permitAll()
			.antMatchers(HttpMethod.GET, "/index").hasRole("USER")
			.anyRequest().authenticated();
		
		http.addFilterBefore(corsFilter(), SessionManagementFilter.class);
		
		http.addFilterBefore(tokenAuthenticationFilter(), UsernamePasswordAuthenticationFilter.class);
		
	}
	
	@Override
    public void configure(WebSecurity web) throws Exception {
        web.ignoring()
           .antMatchers(HttpMethod.POST, "/login")
           .and()
           .ignoring()
           .antMatchers(HttpMethod.POST,"/register")
       
           .and()
           .ignoring()
           .antMatchers(HttpMethod.GET,
        						   "/",
        						   "/*.html",
        						   "/favicon.ico",
        						   "/**/*.html",
        						   "/**/*.css",
        						   "/**/*.js");
    }
	

}
