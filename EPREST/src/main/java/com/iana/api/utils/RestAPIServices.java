package com.iana.api.utils;

import java.io.File;
import java.io.IOException;

import org.springframework.core.io.FileSystemResource;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.http.client.ClientHttpRequestFactory;
import org.springframework.http.client.ClientHttpResponse;
import org.springframework.http.client.HttpComponentsClientHttpRequestFactory;
import org.springframework.stereotype.Service;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.client.ResponseErrorHandler;
import org.springframework.web.client.RestTemplate;

import com.google.gson.Gson;

@Service
public class RestAPIServices {
	
	private static ClientHttpRequestFactory getClientHttpRequestFactory() {
	    int timeout = 10000;
	    HttpComponentsClientHttpRequestFactory clientHttpRequestFactory = new HttpComponentsClientHttpRequestFactory();
	    clientHttpRequestFactory.setConnectTimeout(timeout);
	    return clientHttpRequestFactory;
	}

	private <T> void sendAPIExceptionMail(String url, HttpMethod httpMethod, T postRequestObject) {

//		MailSender mailSender = new MailSender();

		StringBuilder exceptionLogs = new StringBuilder();
		exceptionLogs.append("<br/>");
		exceptionLogs.append("<br/>");
		exceptionLogs.append("<strong>REQUEST TYPE:</strong>");
		exceptionLogs.append(httpMethod);
		exceptionLogs.append("<br/>");
		exceptionLogs.append("<strong>URL:</strong>");
		exceptionLogs.append(url);
		exceptionLogs.append("<br/>");
		if(httpMethod == HttpMethod.GET) {
//			mailSender.sendMail(new String[]{""}, GlobalVariables.INTERMODAL_EMAIL_FROM, "password", GlobalVariables.UIIA_REST_EXEPTION_TITLE, exceptionLogs.toString(), GlobalVariables.EMAIL_TYPE_HTML, null, false, null, null);
		
		} else {
			exceptionLogs.append("DATA:");
			exceptionLogs.append((null != postRequestObject) ? postRequestObject.toString() : "");
//			mailSender.sendMail(new String[]{""}, GlobalVariables.INTERMODAL_EMAIL_FROM, "password", GlobalVariables.UIIA_REST_EXEPTION_TITLE, exceptionLogs.toString(), GlobalVariables.EMAIL_TYPE_HTML, null, false, null, null);
		}
	}

	
	public <T> ResponseEntity<byte[]> callRestAPI(final String url, final HttpMethod httpMethod, final T postRequestObject ) throws Exception{

		ClientHttpRequestFactory requestFactory = getClientHttpRequestFactory();
		RestTemplate restTemplate = new RestTemplate(requestFactory);
		restTemplate.setErrorHandler(new ResponseErrorHandler() {
			
			@Override
			public boolean hasError(ClientHttpResponse response) throws IOException {
				if(response.getStatusCode() != HttpStatus.OK) {
					return true;
				}
			    return false;
			}
			@Override
			public void handleError(ClientHttpResponse response) throws IOException {
				sendAPIExceptionMail(url, httpMethod, postRequestObject);
			}
		});
		
		if((HttpMethod.POST == httpMethod || HttpMethod.PUT == httpMethod) && postRequestObject == null){
			throw new IllegalArgumentException("postRequestObject should not be null");
	}
	
		try {
			if(HttpMethod.POST == httpMethod){
				
				HttpEntity<T> request = new HttpEntity<T>(postRequestObject);
				ResponseEntity<byte[]> res = restTemplate.postForEntity(url, request, byte[].class);
				return res;
			
			}else if(HttpMethod.GET == httpMethod){
				
				return restTemplate.getForEntity(url, byte[].class);
	
			}else{
				//PUT request -handle here.
				HttpEntity<T> request = new HttpEntity<T>(postRequestObject);
				return restTemplate.exchange(url, HttpMethod.PUT, request, byte[].class);
				
			}
			
		} catch(Exception e) {
			sendAPIExceptionMail(url, httpMethod, postRequestObject);
			return null;
		}
	}	
	
	public <T> ResponseEntity<String> callRestAPIFileData(final String url, final HttpMethod httpMethod, final T postRequestObject, File file) throws Exception{

		ClientHttpRequestFactory requestFactory = getClientHttpRequestFactory();
		RestTemplate restTemplate = new RestTemplate(requestFactory);
		restTemplate.setErrorHandler(new ResponseErrorHandler() {
			
			@Override
			public boolean hasError(ClientHttpResponse response) throws IOException {
				if(response.getStatusCode() != HttpStatus.OK) {
					return true;
				}
			    return false;
			}
			@Override
			public void handleError(ClientHttpResponse response) throws IOException {
				sendAPIExceptionMail(url, httpMethod, postRequestObject);
			}
		});
		
		if((HttpMethod.POST == httpMethod || HttpMethod.PUT == httpMethod) && postRequestObject == null){
			throw new IllegalArgumentException("postRequestObject should not be null");
	}
	
		try {
			if(HttpMethod.POST == httpMethod){
				return post(restTemplate, url, postRequestObject, file);
				
			}else if(HttpMethod.GET == httpMethod){
				
				return restTemplate.getForEntity(url, String.class);
	
			}else{
				//PUT request -handle here.
				HttpEntity<T> request = new HttpEntity<T>(postRequestObject);
				return restTemplate.exchange(url, HttpMethod.PUT, request, String.class);
				
			}
			
		} catch(Exception e) {
			e.printStackTrace();
			sendAPIExceptionMail(url, httpMethod, postRequestObject);
			return null;
		}
	}

	private <T> ResponseEntity<String> post(RestTemplate restTemplate, String url, final T postRequestObject, File file) {
		
		HttpHeaders headers = new HttpHeaders();
		headers.setContentType(MediaType.MULTIPART_FORM_DATA);

		MultiValueMap<String, Object> map= new LinkedMultiValueMap<String, Object>();
		map.add(GlobalVariables.JSON_REQ_DETAILS, new Gson().toJson(postRequestObject));
		map.add("file", new FileSystemResource(file));

		HttpEntity<MultiValueMap<String, Object>> request = new HttpEntity<MultiValueMap<String, Object>>(map, headers);

		return restTemplate.postForEntity(url, request, String.class);

	}
}
