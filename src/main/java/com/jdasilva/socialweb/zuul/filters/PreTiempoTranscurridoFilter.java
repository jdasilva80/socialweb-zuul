package com.jdasilva.socialweb.zuul.filters;

import javax.servlet.http.HttpServletRequest;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import com.netflix.zuul.ZuulFilter;
import com.netflix.zuul.context.RequestContext;
import com.netflix.zuul.exception.ZuulException;

@Component
public class PreTiempoTranscurridoFilter extends ZuulFilter {

	Logger log = LoggerFactory.getLogger(PreTiempoTranscurridoFilter.class);

	@Override
	public boolean shouldFilter() {

		return true;
	}

	@Override
	public Object run() throws ZuulException {

		RequestContext context = RequestContext.getCurrentContext();
		HttpServletRequest request = context.getRequest();
		log.info(String.format("%s request enrutado a %s", request.getMethod(), request.getRequestURL().toString()));
		request.setAttribute("tiempoInicio", System.currentTimeMillis());
		return null;
	}

	@Override
	public String filterType() {

		return "pre";// es palabra clave
	}

	@Override
	public int filterOrder() {

		return 1;
	}

}
