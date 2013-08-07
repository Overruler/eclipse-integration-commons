/*******************************************************************************
 *  Copyright (c) 2013 VMware, Inc.
 *  All rights reserved. This program and the accompanying materials
 *  are made available under the terms of the Eclipse Public License v1.0
 *  which accompanies this distribution, and is available at
 *  http://www.eclipse.org/legal/epl-v10.html
 *
 *  Contributors:
 *      VMware, Inc. - initial API and implementation
 *******************************************************************************/
package org.springsource.ide.eclipse.commons.gettingstarted.github.auth;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URLConnection;
import java.util.List;
import java.util.regex.Pattern;

import org.apache.commons.codec.binary.Base64;
import org.eclipse.swt.browser.AuthenticationEvent;
import org.eclipse.swt.browser.AuthenticationListener;
import org.eclipse.swt.browser.Browser;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpRequest;
import org.springframework.http.client.ClientHttpRequestExecution;
import org.springframework.http.client.ClientHttpRequestInterceptor;
import org.springframework.http.client.ClientHttpResponse;
import org.springframework.web.client.RestTemplate;
import org.springsource.ide.eclipse.commons.gettingstarted.GettingStartedActivator;

/**
 * Uses basic authentication username and passwd to access github rest api.
 * 
 * @author Kris De Volder
 */
public class BasicAuthCredentials extends Credentials {

	private Pattern host;
	private String username;
	private String passwd;

	public BasicAuthCredentials(Pattern host, String username, String passwd) {
		this.host = host;
		this.username = username;
		this.passwd = passwd;
	}
	
	@Override
	public String toString() {
		return "BasicAuthCredentials("+username+")";
	}

	private String computeAuthString() throws UnsupportedEncodingException {
		String authorisation = username + ":" + passwd;
		byte[] encodedAuthorisation = Base64.encodeBase64(authorisation.getBytes("utf8"));
		String authString = "Basic " + new String(encodedAuthorisation);
		return authString;
	}
	
	@Override
	public RestTemplate apply(RestTemplate rest) {
		List<ClientHttpRequestInterceptor> interceptors = rest.getInterceptors();
		interceptors.add(new ClientHttpRequestInterceptor() {
			public ClientHttpResponse intercept(HttpRequest request, byte[] body,
					ClientHttpRequestExecution execution) throws IOException {
				if (matchHost(request.getURI().getHost())) {
					HttpHeaders headers = request.getHeaders();
					if (!headers.containsKey("Authorization")) {
						String authString = computeAuthString();
						headers.add("Authorization", authString);
					}
				}
				return execution.execute(request, body);
			}

		});
		return rest;
	}

	@Override
	public void apply(URLConnection conn) {
		try {
			if (matchHost(conn.getURL().getHost())) {
				conn.setRequestProperty("Authorization", computeAuthString());
			}
		} catch (UnsupportedEncodingException e) {
			//Shouldn't really be possible...
			GettingStartedActivator.log(e);
		}
	}

	private boolean matchHost(String host) {
		if (this.host!=null) {
			return this.host.matcher(host).matches();
		} else {
			return true;
		}
	}

}
