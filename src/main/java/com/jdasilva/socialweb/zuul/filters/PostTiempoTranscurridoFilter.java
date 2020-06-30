package com.jdasilva.socialweb.zuul.filters;

import javax.servlet.http.HttpServletRequest;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import com.netflix.zuul.ZuulFilter;
import com.netflix.zuul.context.RequestContext;
import com.netflix.zuul.exception.ZuulException;

@Component
public class PostTiempoTranscurridoFilter extends ZuulFilter {

	Logger log = LoggerFactory.getLogger(PostTiempoTranscurridoFilter.class);

	@Override
	public boolean shouldFilter() {

		return true;
	}

	@Override
	public Object run() throws ZuulException {

		RequestContext context = RequestContext.getCurrentContext();
		HttpServletRequest request = context.getRequest();
		Long tiempoInicio = (Long) request.getAttribute("tiempoInicio");
		Long tiempoFinal = System.currentTimeMillis();
		Long TiempoTranscurrido = tiempoFinal -tiempoInicio;
		
		log.info(String.format("Tiempo transcurrido en seg. %s", TiempoTranscurrido.doubleValue()/1000.00));
		log.info(String.format("Tiempo transcurrido en mseg. %s", TiempoTranscurrido));
		
		return null;
	}

	@Override
	public String filterType() {

		return "post";// es palabra clave
	}

	@Override
	public int filterOrder() {

		return 1;
	}

}
