package com.iana.api.config;

import java.util.Arrays;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.config.annotation.authentication.builders.AuthenticationManagerBuilder;
import org.springframework.security.config.annotation.method.configuration.EnableGlobalMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.builders.WebSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configuration.WebSecurityConfigurerAdapter;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;

import com.iana.api.controller.EPRest;
import com.iana.api.controller.LoginRest;
import com.iana.api.security.CustomAuthenticationProvider;
import com.iana.api.security.JwtAuthenticationEntryPoint;
import com.iana.api.security.JwtAuthorizationTokenFilter;
import com.iana.api.utils.GlobalVariables;

@Configuration
@EnableWebSecurity
@EnableGlobalMethodSecurity(prePostEnabled = true)
public class WebSecurityConfig extends WebSecurityConfigurerAdapter {


    @Autowired
    private JwtAuthenticationEntryPoint unauthorizedHandler;

    // Custom JWT based security filter
    @Autowired
    private JwtAuthorizationTokenFilter authenticationTokenFilter;

    @Autowired
    private CustomAuthenticationProvider authProvider;
 
    @Override
    protected void configure(AuthenticationManagerBuilder auth) throws Exception {
        auth.authenticationProvider(authProvider);
    }

    @Bean
    @Override
    public AuthenticationManager authenticationManagerBean() throws Exception {
        return super.authenticationManagerBean();
    }

    /**
	 * Override this method to configure the {@link HttpSecurity}. Typically subclasses
	 * should not invoke this method by calling super as it may override their
	 * configuration. The default configuration is:
	 *
	 * <pre>
	 * http.authorizeRequests().anyRequest().authenticated().and().formLogin().and().httpBasic();
	 * </pre>
	 *
	 * @param http the {@link HttpSecurity} to modify
	 * @throws Exception if an error occurs
	 */
    
    @Override
    protected void configure(HttpSecurity httpSecurity) throws Exception {
        httpSecurity

        	.cors().and() //https://docs.spring.io/spring-security/site/docs/5.0.x/reference/html/cors.html

        	// we don't need CSRF because our token is invulnerable
            .csrf().disable()

            .exceptionHandling().authenticationEntryPoint(unauthorizedHandler).and()

            // don't create session
            .sessionManagement().sessionCreationPolicy(SessionCreationPolicy.STATELESS).and()

            .authorizeRequests()

            .antMatchers("/" + GlobalVariables.REST_URI_UIIA + LoginRest.URI_AUTH_PATH).permitAll()
            .antMatchers("/" + GlobalVariables.REST_URI_UIIA + LoginRest.URI_FORGOT_PASSWORD).permitAll()
            .antMatchers("/" + GlobalVariables.REST_URI_UIIA + LoginRest.URI_VALID_FORGOT_PASSWORD).permitAll()
            .antMatchers("/" + GlobalVariables.REST_URI_UIIA + LoginRest.URI_RESET_PASSWORD).permitAll()
            .antMatchers("/" + GlobalVariables.REST_URI_UIIA + EPRest.URI_EP_TEMPLATES + EPRest.URI_SETUP).permitAll()
            
            .anyRequest().authenticated();

       httpSecurity
            .addFilterBefore(authenticationTokenFilter, UsernamePasswordAuthenticationFilter.class);

        // disable page caching
//        httpSecurity
//            .headers()
//            .frameOptions().sameOrigin()  // required to set for H2 else H2 Console will be blank.
//            .cacheControl();
    }

    /*https://stackoverflow.com/questions/40418441/spring-security-cors-filter/43559288#43559288 */  
    /* THIS BEAN WIL	L BE USED BY .cors().and() in above method.*/
    @Bean
	CorsConfigurationSource corsConfigurationSource() {
		CorsConfiguration configuration = new CorsConfiguration();
		configuration.setAllowedOrigins(Arrays.asList("*"));
		configuration.setAllowedMethods(Arrays.asList("*"));
		configuration.setAllowedHeaders(Arrays.asList("*"));
//		configuration.setAllowCredentials(true);
		configuration.setExposedHeaders(Arrays.asList("Content-Disposition"));
		UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
		source.registerCorsConfiguration("/**", configuration);
		return source;
	}
    

    /**
	 * Override this method to configure {@link WebSecurity}. For example, if you wish to
	 * ignore certain requests.
	 */
    
    @Override
    public void configure(WebSecurity web) throws Exception {
        // AuthenticationTokenFilter will ignore the below paths
        web
            .ignoring()
            .antMatchers(
                HttpMethod.POST,
                GlobalVariables.REST_URI_UIIA + LoginRest.URI_AUTH_PATH
            )

            // allow anonymous resource requests
            .and()
            .ignoring()
            .antMatchers(
                HttpMethod.GET,
                "/",
                "/*.html",
                "/favicon.ico",
                "/**/*.html",
                "/**/*.css",
                "/**/*.js",
                "/swagger-ui.html/**",
                "/webjars/**",
                "/images/**",
                "/configuration/**",
                "/v2/api-docs",
                "/swagger-resources/**"
            );
    }
}
