package com.cyberone.cams.crawler.config;

import java.io.IOException;

import org.apache.http.HttpHost;
import org.elasticsearch.client.RestClient;
import org.elasticsearch.client.RestClientBuilder;
import org.elasticsearch.client.RestHighLevelClient;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

//@Configuration
public class ElasticSearchConfig {
	
    private final Logger LOGGER = LoggerFactory.getLogger(ElasticSearchConfig.class);

    @Value("${spring.elasticsearch.httphost}")
    private String httphost;

    @Value("${spring.elasticsearch.transporthost}")
    private String transporthost;

    @Value("${spring.elasticsearch.nodename}")
    private String nodename;
    
    @Bean
    public RestHighLevelClient client() throws Exception {
    	
    	RestHighLevelClient restClient = null;
    	try {
    		int defaultPort = 9200;
    		String hostName = null;
    		int portNum = 0;

    		String[] hostStr = httphost.split(",");
    		HttpHost[] httpHost = new HttpHost[hostStr.length];
    		for (int i = 0; i < hostStr.length; i++) {
    			if (hostStr[i].lastIndexOf(":") != -1) {
    				hostName = hostStr[i].substring(0, hostStr[i].lastIndexOf(":"));
    				try {
        				portNum = Integer.parseInt(hostStr[i].substring(hostStr[i].lastIndexOf(":") + 1));
    				} catch (Exception e) {
        				portNum = defaultPort;
    				}
    			} else {
    				hostName = hostStr[i];
    				portNum = defaultPort;
    			}
    			httpHost[i] = new HttpHost(hostName, portNum);
    		}

    		RestClientBuilder  builder = RestClient.builder(httpHost);
    		//builder.setMaxRetryTimeoutMillis(10000);

    		restClient = new RestHighLevelClient(builder);

        	LOGGER.debug("[client] elasticsearch connected...");
    	} catch (Exception e) {
    		if (restClient != null) {
        		try {
					restClient.close();
					restClient = null;
				} catch (IOException e1) {
				}
    		}
    		throw e;
    	}

        return restClient;
    }
    
}