package com.jdasilva.socialweb.zuul.filters;

import javax.servlet.http.HttpServletRequest;

import com.netflix.zuul.http.HttpServletRequestWrapper;

/**
 * A Servlet 3.0 compliant wrapper.
 */
class Servlet30RequestWrapper extends HttpServletRequestWrapper {

	private HttpServletRequest request;

	Servlet30RequestWrapper(HttpServletRequest request) {
		super(request);
		this.request = request;
	}

	/**
	 * There is a bug in zuul 1.2.2 where HttpServletRequestWrapper.getRequest returns a
	 * wrapped request rather than the raw one.
	 * @return the original HttpServletRequest
	 */
	@Override
	public HttpServletRequest getRequest() {
		return this.request;
	}

}
