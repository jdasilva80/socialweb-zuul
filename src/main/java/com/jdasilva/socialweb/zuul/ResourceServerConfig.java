package com.jdasilva.socialweb.zuul;

import java.util.Arrays;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.web.servlet.FilterRegistrationBean;
import org.springframework.cloud.context.config.annotation.RefreshScope;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.Ordered;
import org.springframework.http.HttpMethod;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.oauth2.config.annotation.web.configuration.EnableResourceServer;
import org.springframework.security.oauth2.config.annotation.web.configuration.ResourceServerConfigurer;
import org.springframework.security.oauth2.config.annotation.web.configurers.ResourceServerSecurityConfigurer;
import org.springframework.security.oauth2.provider.token.store.JwtAccessTokenConverter;
import org.springframework.security.oauth2.provider.token.store.JwtTokenStore;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;
import org.springframework.web.filter.CorsFilter;

@RefreshScope
@Configuration
@EnableResourceServer
public class ResourceServerConfig implements ResourceServerConfigurer {

	@Value("${config.security.oauth.jwt.key}")
	private String jwtKey;

	@Override
	public void configure(ResourceServerSecurityConfigurer resources) throws Exception {

		resources.tokenStore(tokenStoreJwt());

	}

	@Override
	public void configure(HttpSecurity http) throws Exception {
        //protección de endpoints.
		http.authorizeRequests()
				.antMatchers("/login**").permitAll()
				//.antMatchers("/api/security/oauth/**").permitAll()
				//.antMatchers("/api/security/**").permitAll()
				.antMatchers("/api/socialweb/images/**").permitAll()
				.antMatchers("/api/socialweb/publicaciones/uploads/img/**").permitAll()
				.antMatchers("/api/socialweb-reclamaciones/js/**","/api/socialweb-reclamaciones/css/**","/api/socialweb-reclamaciones/img/**","/api/socialweb-reclamaciones/uploads/**").permitAll()
				.antMatchers("/api/socialweb-productos/js/**","/api/socialweb-productos/css/**","/api/socialweb-productos/img/**","/api/socialweb-productos/uploads/**").permitAll()
				.antMatchers("/api/socialweb-tienda/js/**","/api/socialweb-tienda/css/**","/api/socialweb-tienda/img/**","/api/socialweb-tienda/uploads/**").permitAll()
//				.antMatchers(HttpMethod.GET, "/api/socialweb/**", "/api/socialweb-usuarios/usuarios").hasAnyRole("USER","ADMIN")
//				.antMatchers(HttpMethod.POST, "/api/socialweb/**", "/api/socialweb-usuarios/usuarios").hasAnyRole("ADMIN")
//				.antMatchers(HttpMethod.PUT, "/api/socialweb/**", "/api/socialweb-usuarios/usuarios").hasAnyRole("ADMIN")
//				.antMatchers(HttpMethod.DELETE, "/api/socialweb/**", "/api/socialweb-usuarios/usuarios").hasAnyRole("ADMIN")
				.antMatchers(HttpMethod.GET, "/api/socialweb/**", "/api/socialweb-usuarios/**").permitAll()
				.antMatchers(HttpMethod.POST, "/api/socialweb/**", "/api/socialweb-usuarios/**").permitAll()
				.antMatchers(HttpMethod.PUT, "/api/socialweb/**", "/api/socialweb-usuarios/**").permitAll()
				.antMatchers(HttpMethod.DELETE, "/api/socialweb/**", "/api/socialweb-usuarios/**").permitAll()
				.antMatchers("/api/socialweb-reclamaciones/**").permitAll()
				.antMatchers("/api/socialweb-productos/**").permitAll()
				.antMatchers("/api/socialweb-tienda/**").permitAll()
				.anyRequest().authenticated()
				.and().cors()// aqui se aplica la configuracion de cors solo a spring security.
				.configurationSource(configurationSource());
			//	.and().csrf().disable();
			//	.formLogin().permitAll();
	}

	@Bean
	public CorsConfigurationSource configurationSource() {

		CorsConfiguration corsConfiguration = new CorsConfiguration();
		corsConfiguration.setAllowedOrigins(Arrays.asList("*"));

		corsConfiguration.setAllowCredentials(true);
		corsConfiguration.setAllowedMethods(Arrays.asList("POST", "GET", "PUT", "DELETE", "OPTIONS"));
		corsConfiguration.setAllowedHeaders(Arrays.asList("Authorization", "Content-Type", "Access-Control-Allow-Origin"));
		//corsConfiguration.setAllowedHeaders(Arrays.asList("Authorization", "Content-Type", "content-type", "x-requested-with", "Access-Control-Allow-Origin", "Access-Control-Allow-Methods", "Access-Control-Allow-Credentials","Access-Control-Allow-Headers", "x-auth-token", "x-app-id", "Origin","Accept", "X-Requested-With", "Access-Control-Request-Method", "Access-Control-Request-Headers"));
        

		UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
		source.registerCorsConfiguration("/**", corsConfiguration);// para que se aplique a todos los endpoints.

		return source;
	}

	@Bean
	public FilterRegistrationBean<CorsFilter> filterCors() {
		// aqui se aplica el filtro de cors a nivel global, no solo en las rutas de
		// spring security
		FilterRegistrationBean<CorsFilter> bean = new FilterRegistrationBean<>();
		CorsFilter corsFilter = new CorsFilter(configurationSource());
		bean.setFilter(corsFilter);
		bean.setOrder(Ordered.HIGHEST_PRECEDENCE);

		return bean;
	}

	@Bean
	public JwtTokenStore tokenStoreJwt() {

		return new JwtTokenStore(accessTokenConverterJwt());
	}

	@Bean
	public JwtAccessTokenConverter accessTokenConverterJwt() {

		JwtAccessTokenConverter tokenConverterJwt = new JwtAccessTokenConverter();
		tokenConverterJwt.setSigningKey(jwtKey);

		return tokenConverterJwt;
	}
	

}
