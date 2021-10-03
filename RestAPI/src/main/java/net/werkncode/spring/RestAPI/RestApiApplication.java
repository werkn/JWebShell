package net.werkncode.spring.RestAPI;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.security.servlet.SecurityAutoConfiguration;

/**
 * Entrypoint for the RestAPI.  Configuration not done here can be found in 
 * Maven resource folders in application.properties (.yaml) possibly
 * 
 * @author werkn
 *
 */
@SpringBootApplication(exclude = { SecurityAutoConfiguration.class })
public class RestApiApplication {

	//we've disabled security auto-config but provided our own impl. 
	//in net.werkncode.sprint.RestAPI.security.SecurityConfiguration
	public static void main(String[] args) {
		SpringApplication.run(RestApiApplication.class, args);
	}

}
