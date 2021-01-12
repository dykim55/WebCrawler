package com.cyberone.cams.crawler.core;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileWriter;
import java.net.URL;
import java.security.MessageDigest;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import org.apache.commons.io.FileUtils;
import org.apache.commons.io.IOUtils;
import org.json.JSONObject;
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
		
		private Map<String, String> data;
		
	    public WorkerThread(Map<String, String> data) {
	    	this.data = data;
	    }

	    @Override
	    public void run() {
	        
	    	lLastTime = System.currentTimeMillis();
	    	
	    	try {
	    		
	    		logger.debug("WorkerThread: " + data.toString());
	    		getForgeryOriginalData(data.get("_id"), data.get("host"));
	    		
	    		
	    	} catch(Exception e) {
	    	}
	    	
            millis = System.currentTimeMillis() - lLastTime;
            logger.debug("WorkerThread End... [소요시간: " + millis + "]");

	    }

	    @Override
	    public String toString(){
	        return "";
	    }
	    
		@SuppressWarnings("unchecked")
		public void getForgeryOriginalData(String strMonitorId, String strHost) throws Exception {

			if (StringUtil.isBlank(strHost)) {
				return;
			}
			
			try {
	            Connection conn = Jsoup.connect(strHost);
	            Document html = conn.get(); // conn.post();
	            Response res = conn.response();
	            URL url = res.url();
	            
	            String strProtocol = "http://";
	            if (url.getProtocol().equals("https")) {
	            	strProtocol = "https://";
	            }
				
	            strHost = strProtocol + url.getHost();
	            System.out.println(strHost);

	            Elements allEl = html.getAllElements();
	            for (Element el : allEl) {
	            	if (el.hasAttr("src")) {
	            		if (el.attr("src").indexOf("http://") < 0 && el.attr("src").indexOf("https://") < 0) {
		            		el.attr("src", strHost + el.attr("src"));
		            	}
	            	}
	            	if (el.normalName().equals("link")) {
	            		if (el.attr("href").indexOf("http://") < 0 && el.attr("href").indexOf("https://") < 0) {
		            		el.attr("href", strHost + el.attr("href"));
		            	}
	            	}
	            }
	            
	            File monitorDir = new File(CAMS_HOME + "forgery/" + strMonitorId);
	            if (!monitorDir.exists()) {
	            	monitorDir.mkdir();
	            }

	            File originalDir = new File(CAMS_HOME + "forgery/" + strMonitorId + "/original");
	            if (!originalDir.exists()) {
	            	originalDir.mkdir();
	            }

	            FileWriter file = new FileWriter(originalDir.getAbsolutePath() + "/" + "urlPage.html");
	    		file.write(html.html());
	    		file.flush();
	    		file.close();
	            
	            JSONObject jsonObj = new JSONObject();
	            
	            for (Element el : allEl) {
	            	if (el.hasAttr("src")) {
		            	String strSrc = el.attr("src");
		            	if (el.normalName().equals("img")) {
		            		String strFileName = strSrc.replace(strHost + "/", "").replace("/", "_");
		            		el.attr("src", strFileName);
		            		
		                	File fileImage = new File(originalDir.getAbsolutePath() + "/" + strFileName);
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
	            
	            file = new FileWriter(originalDir.getAbsolutePath() + "/" + "imageHash.json");
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
