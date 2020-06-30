/*
 * Copyright 2013-2019 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      https://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.jdasilva.socialweb.zuul.filters;

import static org.springframework.cloud.netflix.zuul.filters.support.FilterConstants.FORM_BODY_WRAPPER_FILTER_ORDER;
import static org.springframework.cloud.netflix.zuul.filters.support.FilterConstants.PRE_TYPE;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.lang.reflect.Field;
import java.util.List;
import java.util.Map.Entry;

import javax.servlet.ServletInputStream;
import javax.servlet.ServletRequest;
import javax.servlet.ServletRequestWrapper;
import javax.servlet.http.HttpServletRequest;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.cloud.netflix.zuul.util.RequestContentDataExtractor;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpOutputMessage;
import org.springframework.http.InvalidMediaTypeException;
import org.springframework.http.MediaType;
import org.springframework.http.converter.FormHttpMessageConverter;
import org.springframework.http.converter.support.AllEncompassingFormHttpMessageConverter;
import org.springframework.stereotype.Component;
import org.springframework.util.Assert;
import org.springframework.util.MultiValueMap;
import org.springframework.util.ReflectionUtils;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.multipart.MultipartHttpServletRequest;
import org.springframework.web.servlet.DispatcherServlet;

import com.netflix.zuul.ZuulFilter;
import com.netflix.zuul.context.RequestContext;
import com.netflix.zuul.http.HttpServletRequestWrapper;
import com.netflix.zuul.http.ServletInputStreamWrapper;

//@Component
public class PreFormBodyWrapperFilter extends ZuulFilter {

	Logger log = LoggerFactory.getLogger(PreFormBodyWrapperFilter.class);

	private FormHttpMessageConverter formHttpMessageConverter;

	private Field requestField;

	private Field servletRequestField;

	public PreFormBodyWrapperFilter() {
		this(new AllEncompassingFormHttpMessageConverter());
	}

	public PreFormBodyWrapperFilter(FormHttpMessageConverter formHttpMessageConverter) {
		this.formHttpMessageConverter = formHttpMessageConverter;
		this.requestField = ReflectionUtils.findField(HttpServletRequestWrapper.class, "req", HttpServletRequest.class);
		this.servletRequestField = ReflectionUtils.findField(ServletRequestWrapper.class, "request",
				ServletRequest.class);
		Assert.notNull(this.requestField, "HttpServletRequestWrapper.req field not found");
		Assert.notNull(this.servletRequestField, "ServletRequestWrapper.request field not found");
		this.requestField.setAccessible(true);
		this.servletRequestField.setAccessible(true);
	}

	@Override
	public String filterType() {
		return PRE_TYPE;
	}

	@Override
	public int filterOrder() {
		return FORM_BODY_WRAPPER_FILTER_ORDER;
	}

	@Override
	public boolean shouldFilter() {
		RequestContext ctx = RequestContext.getCurrentContext();
		HttpServletRequest request = ctx.getRequest();
		String contentType = request.getContentType();
		// Don't use this filter on GET method
		if (contentType == null) {
			return false;
		}
		// Only use this filter for form data and only for multipart data in a
		// DispatcherServlet handler
		try {
			MediaType mediaType = MediaType.valueOf(contentType);
			return MediaType.APPLICATION_FORM_URLENCODED.includes(mediaType)
					|| (isDispatcherServletRequest(request) && MediaType.MULTIPART_FORM_DATA.includes(mediaType));
		} catch (InvalidMediaTypeException ex) {
			return false;
		}
	}

	private boolean isDispatcherServletRequest(HttpServletRequest request) {
		return request.getAttribute(DispatcherServlet.WEB_APPLICATION_CONTEXT_ATTRIBUTE) != null;
	}

	@Override
	public Object run() {
		RequestContext ctx = RequestContext.getCurrentContext();
		HttpServletRequest request = ctx.getRequest();
		FormBodyRequestWrapper wrapper = null;
		if (request instanceof HttpServletRequestWrapper) {
			HttpServletRequest wrapped = (HttpServletRequest) ReflectionUtils.getField(this.requestField, request);
			wrapper = new FormBodyRequestWrapper(wrapped);
			ReflectionUtils.setField(this.requestField, request, wrapper);
			if (request instanceof ServletRequestWrapper) {
				ReflectionUtils.setField(this.servletRequestField, request, wrapper);
			}
		} else {
			wrapper = new FormBodyRequestWrapper(request);
			ctx.setRequest(wrapper);
		}
		if (wrapper != null) {
			ctx.getZuulRequestHeaders().put("content-type", wrapper.getContentType());
		}
		return null;
	}

	private class FormBodyRequestWrapper extends Servlet30RequestWrapper {

		private HttpServletRequest request;

		private volatile byte[] contentData;

		private MediaType contentType;

		private int contentLength;

		FormBodyRequestWrapper(HttpServletRequest request) {
			super(request);
			this.request = request;
		}

		@Override
		public String getContentType() {
			if (this.contentData == null) {
				buildContentData();
			}
			return this.contentType.toString();
		}

		@Override
		public int getContentLength() {
			if (super.getContentLength() <= 0) {
				return super.getContentLength();
			}
			if (this.contentData == null) {
				buildContentData();
			}
			return this.contentLength;
		}

		public long getContentLengthLong() {
			return getContentLength();
		}

		@Override
		public ServletInputStream getInputStream() throws IOException {
			if (this.contentData == null) {
				buildContentData();
			}
			return new ServletInputStreamWrapper(this.contentData);
		}

		private synchronized void buildContentData() {

			try {
				if (this.contentData != null) {
					return;
				}

				/*
				 * inicio:: he tenido que añadir este código porque sino daba error al enviar
				 * formularios multipart con archivos vaciosademás de desabilitar el filtro por
				 * defecto en el archivo de properties-->zuul.FormBodyWrapperFilter.pre.disable=true
				 *  y sustituirlo añadiendo esta modificación.
				 */
				log.info("1.$$  request.getParts().size() " + this.request.getParts().size());

				this.request.getParts().removeIf(
						(part) -> (part.getSubmittedFileName() != null && part.getSubmittedFileName().isEmpty()));

				log.info("2.&&  request.getParts().size() " + this.request.getParts().size());

				for (Entry<String, List<MultipartFile>> parts : ((MultipartHttpServletRequest) request)
						.getMultiFileMap().entrySet()) {
					for (MultipartFile file : parts.getValue()) {

						if (file.getOriginalFilename() == null || file.getOriginalFilename().isEmpty()) {

							log.info("3.** file.getOriginalFilename() --> hay uno vacío " + file.isEmpty());
						}
					}
				}				

				((MultipartHttpServletRequest) request).getMultiFileMap().entrySet()
						.removeIf((e) -> e.getValue().removeIf(
								(file) -> file.getOriginalFilename() == null || file.getOriginalFilename().isEmpty()));
				/*
				 * fin:: he tenido que añadir este código porqe sino daba error al enviar
				 * formularios multipart con archivos vacios
				 */

				MultiValueMap<String, Object> builder = RequestContentDataExtractor.extract(this.request);
				FormHttpOutputMessage data = new FormHttpOutputMessage();

				this.contentType = MediaType.valueOf(this.request.getContentType());
				data.getHeaders().setContentType(this.contentType);
				PreFormBodyWrapperFilter.this.formHttpMessageConverter.write(builder, this.contentType, data);
				// copy new content type including multipart boundary
				this.contentType = data.getHeaders().getContentType();
				byte[] input = data.getInput();
				this.contentLength = input.length;
				this.contentData = input;

			} catch (Exception e) {
				throw new IllegalStateException("Cannot convert form data", e);
			}
		}

		private class FormHttpOutputMessage implements HttpOutputMessage {

			private HttpHeaders headers = new HttpHeaders();

			private ByteArrayOutputStream output = new ByteArrayOutputStream();

			@Override
			public HttpHeaders getHeaders() {
				return this.headers;
			}

			@Override
			public OutputStream getBody() throws IOException {
				return this.output;
			}

			public byte[] getInput() throws IOException {
				this.output.flush();
				return this.output.toByteArray();
			}

		}

	}
}