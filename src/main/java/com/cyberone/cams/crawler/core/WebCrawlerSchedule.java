package com.cyberone.cams.crawler.core;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.net.URL;
import java.security.MessageDigest;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import org.apache.commons.io.FileUtils;
import org.apache.commons.io.IOUtils;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.jsoup.Connection;
import org.jsoup.Connection.Response;
import org.jsoup.HttpStatusException;
import org.jsoup.Jsoup;
import org.jsoup.internal.StringUtil;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

@Component 
public class WebCrawlerSchedule { 
    
    private final Logger logger = LoggerFactory.getLogger(WebCrawlerSchedule.class);

    @Value("${cams.home.dir}")
    private String CAMS_HOME;

    private long lLastTime;
    private long millis;    

    @Scheduled(cron = "0 0/1 * * * ?")
    public void WebCrawlingJob() { 

    	ExecutorService executor = Executors.newFixedThreadPool(5);
    	
        try {
            logger.debug("WebCrawlingJob Start...");
            lLastTime = System.currentTimeMillis();
            
            List<Map<String, String>> dataList = generateData();
            
            for (Map<String, String> data : dataList) {
            	
				Runnable worker = new WorkerThread(data);
				executor.execute(worker);
            	
            }
            
            
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
        }
        
        executor.shutdown();
        
        millis = System.currentTimeMillis() - lLastTime;
        logger.debug("WebCrawlingJob End... [소요시간: " + millis + "]");
    }

	public class WorkerThread implements Runnable {
		  
	    private long lLastTime;
	    private long millis;    
		
		private String monitorId;
		private String host;
		
		private Date logTime;
		
	    public WorkerThread(Map<String, String> data) {
	    	this.monitorId = data.get("_id");
	    	this.host = data.get("host");
	    	this.logTime = new Date();
	    }

	    @Override
	    public void run() {
	        
	    	lLastTime = System.currentTimeMillis();
	    	
	    	logger.debug("WorkerThread[{}]:", monitorId);
	    	
	    	try {
	    		
	    		StringBuffer sBuff = new StringBuffer();
	    		
	    		File file = new File(CAMS_HOME + "forgery/" + monitorId + "/original/urlPage.html");
	    		logger.debug("file.length().length(): {}", file.length());
	    		
	    		FileReader reader = new FileReader(file);
	    		try (BufferedReader br = new BufferedReader(reader)) {
	    			String  line = null;
	    			while ((line = br.readLine()) != null) {
	    				sBuff.append(line).append("\n");
    				}
	    		} catch (IOException e) {
	    			e.printStackTrace();
	    		}

	    		JSONParser parser = new JSONParser();
	    		Object obj = parser.parse(new FileReader(CAMS_HOME + "forgery/" + monitorId + "/original/imageHash.json"));
				JSONObject jsonObject = (JSONObject) obj;	    		
	    		
				CrawlingInfo originalInfo = new CrawlingInfo(sBuff.toString(), (HashMap)jsonObject, monitorId);

	    		//imageCount
				//htmlLength
				//addImage
				//changeImage
				//deleteImage
	    		
	    	} catch(Exception e) {
	    	}
	    	
            millis = System.currentTimeMillis() - lLastTime;
            logger.debug("WorkerThread[{}][소요시간: {}]", monitorId, millis);

	    }

	    @Override
	    public String toString(){
	        return "";
	    }
	    
		@SuppressWarnings("unchecked")
		public void getForgeryData(String monitorId, String host) throws Exception {

			if (StringUtil.isBlank(host)) {
				return;
			}
			
			try {
	            Connection conn = Jsoup.connect(host);
	            Document html = conn.get();
	            Response res = conn.response();
	            URL url = res.url();
	            
	            String strProtocol = "http://";
	            if (url.getProtocol().equals("https")) {
	            	strProtocol = "https://";
	            }
				
	            host = strProtocol + url.getHost();

	            Elements allEl = html.getAllElements();
	            for (Element el : allEl) {
	            	if (el.hasAttr("src")) {
	            		if (el.attr("src").indexOf("http://") < 0 && el.attr("src").indexOf("https://") < 0) {
		            		el.attr("src", host + el.attr("src"));
		            	}
	            	}
	            	if (el.normalName().equals("link")) {
	            		if (el.attr("href").indexOf("http://") < 0 && el.attr("href").indexOf("https://") < 0) {
		            		el.attr("href", host + el.attr("href"));
		            	}
	            	}
	            }
	            
	            File logDir = new File(CAMS_HOME + "forgery/" + monitorId + "/" + (new SimpleDateFormat("yyyyMMddHHmm")).format(logTime));
	            if (!logDir.exists()) {
	            	logDir.mkdir();
	            }

	            FileWriter file = new FileWriter(logDir.getAbsolutePath() + "/" + "urlPage.html");
	    		file.write(html.html());
	    		file.flush();
	    		file.close();
	            
	            JSONObject jsonObj = new JSONObject();
	            
	            for (Element el : allEl) {
	            	if (el.hasAttr("src")) {
		            	String strSrc = el.attr("src");
		            	if (el.normalName().equals("img")) {
		            		String strFileName = strSrc.replace(host + "/", "").replace("/", "_");
		            		
		                	File fileImage = new File(logDir.getAbsolutePath() + "/" + strFileName);
		                	FileUtils.copyURLToFile(new URL(strSrc), fileImage);
		                	
		                	MessageDigest mdMD5 = MessageDigest.getInstance("MD5");
		                	mdMD5.update(IOUtils.toByteArray(new FileInputStream(fileImage)));
		                	byte[] md5Hash = mdMD5.digest();
		                	StringBuilder hexMD5hash = new StringBuilder();
		                    for(byte b : md5Hash) {
		                        String hexString = String.format("%02x", b);
		                        hexMD5hash.append(hexString);
		                    }        
		                    jsonObj.put(strFileName, hexMD5hash);
		            	}
	            	}
	            }
	            
	    		Set<String> keys = jsonObj.keySet();
	    		for (String key : keys) {


	    			
	    			
	    			
	    			
	    			
	    		}
	            
	            
	            
	            
	            file = new FileWriter(logDir.getAbsolutePath() + "/" + "imageHash.json");
	    		file.write(jsonObj.toString());
	    		file.flush();
	    		file.close();
	            
	        } catch (HttpStatusException e) {
	            e.printStackTrace();
	        }		
			
		}
	    
	}	

	
	public List<Map<String, String>> generateData() {
		
		List<Map<String, String>> dataList = new ArrayList<Map<String, String>>();
		
		Map<String, String> hMap = new HashMap<String, String>();
		hMap.put("_id", "1111111111");
		hMap.put("host", "http://www.kcg.go.kr/kcg/main.do");
		dataList.add(hMap);

		hMap = new HashMap<String, String>();
		hMap.put("_id", "2222222222");
		hMap.put("host", "https://edu.kcga.go.kr/main0.jsp");
		dataList.add(hMap);

		hMap = new HashMap<String, String>();
		hMap.put("_id", "3333333333");
		hMap.put("host", "https://edu.kcga.go.kr/kcgrc/index0.jsp");
		dataList.add(hMap);

		hMap = new HashMap<String, String>();
		hMap.put("_id", "4444444444");
		hMap.put("host", "http://www.kcg.go.kr/mckcg/main.do");
		dataList.add(hMap);

		hMap = new HashMap<String, String>();
		hMap.put("_id", "5555555555");
		hMap.put("host", "http://www.kcg.go.kr/jungbucgh/main.do");
		dataList.add(hMap);

		hMap = new HashMap<String, String>();
		hMap.put("_id", "6666666666");
		hMap.put("host", "http://www.kcg.go.kr/inchoncgs/main.do");
		dataList.add(hMap);

		hMap = new HashMap<String, String>();
		hMap.put("_id", "7777777777");
		hMap.put("host", "http://www.kcg.go.kr/pyeongtaekcgs/main.do");
		dataList.add(hMap);

		hMap = new HashMap<String, String>();
		hMap.put("_id", "8888888888");
		hMap.put("host", "http://www.kcg.go.kr/seohae5docgs/main.do");
		dataList.add(hMap);

		hMap = new HashMap<String, String>();
		hMap.put("_id", "9999999999");
		hMap.put("host", "http://www.kcg.go.kr/jejucgh/main.do");
		dataList.add(hMap);

		return dataList;
	}
	
}
