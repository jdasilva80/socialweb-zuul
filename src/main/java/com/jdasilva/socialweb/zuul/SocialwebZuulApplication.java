package com.jdasilva.socialweb.zuul;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.netflix.zuul.EnableZuulProxy;
import org.springframework.context.annotation.Bean;
import org.springframework.security.web.firewall.HttpFirewall;
import org.springframework.security.web.firewall.StrictHttpFirewall;

import com.netflix.zuul.exception.ZuulException;

@EnableZuulProxy
@SpringBootApplication
public class SocialwebZuulApplication {

	private static final Logger LOG = LoggerFactory.getLogger(SocialwebZuulApplication.class);

	public static void main(String[] args) {

		try {
			SpringApplication.run(SocialwebZuulApplication.class, args);
		} catch (Exception e) {
			if (!(e instanceof ZuulException)) {
				throw e;
			} else {
				LOG.error("ZuulException:: " + e.getMessage());
			}
		}
	}

	@Bean
	public HttpFirewall looseHttpFirewall() {
		StrictHttpFirewall firewall = new StrictHttpFirewall();
		firewall.setAllowSemicolon(true);
		return firewall;
	}

}
