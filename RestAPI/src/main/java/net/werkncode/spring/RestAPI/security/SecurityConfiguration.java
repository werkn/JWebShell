package net.werkncode.spring.RestAPI.security;

import org.springframework.context.annotation.Configuration;  
import org.springframework.security.config.annotation.authentication.builders.AuthenticationManagerBuilder;  
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configuration.WebSecurityConfigurerAdapter;  
  
//To disable Spring Boot Auto-config, we need to add
//@SpringBootApplication(exclude = { SecurityAutoConfiguration.class })
//as an annotation on the SpringApplication.run() launcher class
//adapted from Spring Boot Starter &
//this Okta setup guide:https://developer.okta.com/blog/2019/05/31/spring-security-authentication#:~:text=Spring%20Security%20Authentication%20with%20Okta%20Okta%20is%20an,Boot%20app%20easy.%20Our%20API%20enables%20you%20to%3A  
@Configuration  
@EnableWebSecurity
public class SecurityConfiguration extends WebSecurityConfigurerAdapter {  
      
    @Override  
    public void configure(HttpSecurity http) throws Exception {  
        
    	//configure basic auth, in clients we can specify this when connecting using
    	http  
            .authorizeRequests()  
            .anyRequest().authenticated()  
            .and()  
            .httpBasic();  
    }  
    
    /**
     * Setup a user/password for performing basic authentication to the RestAPI
     */
    @Override  
    protected void configure(AuthenticationManagerBuilder auth) throws Exception {  
        auth.inMemoryAuthentication()  
            .withUser("server")  
            //{noop} is required here if were not configuring the password storage format directly
            .password("{noop}password1234") //{noop} here marks the password storage format as plaintext
            .roles("USER");
    }  
      
}