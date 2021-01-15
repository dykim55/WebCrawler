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
import java.util.Date;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

import org.apache.commons.io.FileUtils;
import org.apache.commons.io.IOUtils;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.jsoup.Connection;
import org.jsoup.Connection.Response;
import org.jsoup.HttpStatusException;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.cyberone.cams.crawler.utils.StringUtil;

public class ForgeryDetection implements Runnable {

	private final Logger logger = LoggerFactory.getLogger(ForgeryDetection.class);
	
    private long lLastTime;
    private long millis;    
	
    private String CAMS_HOME = "C:/Temp/cams/web/";
    
	private String object_id;
	private String account_id;
	private String kinds;
	private String host;
	private String url;
	private String ip;
	private int port;
	private Date logTime;
	
    public ForgeryDetection(Map<String, Object> data) {
    	this.object_id = StringUtil.toString(data.get("_id"));
    	this.account_id = StringUtil.toString(data.get("account_id"));
    	this.kinds = StringUtil.toString(data.get("kinds"));
    	this.host = StringUtil.toString(data.get("host"));
    	this.url = this.host.indexOf("://") > 0 ? this.host.substring(this.host.indexOf("://") + 3) : this.host;
    	this.ip = "";
    	this.port = 0;
    	this.logTime = new Date();
    }

    @Override
    public void run() {
        
    	lLastTime = System.currentTimeMillis();
    	
    	logger.debug("WorkerThread[{}]:", object_id);
    	
    		
    	try {
			CrawlingInfo originalInfo = getOriginalInfo();
			getForgeryData(originalInfo);

    		//getForgeryOriginalData(object_id, host);
    		
    	} catch(Exception e) {
    		e.printStackTrace();
    	}
    	
        millis = System.currentTimeMillis() - lLastTime;
        logger.debug("WorkerThread[{}][소요시간: {}]", object_id, millis);

    }

	@SuppressWarnings("unchecked")
	public void getForgeryOriginalData(String strMonitorId, String monitorUrl) throws Exception {

		if (StringUtil.isEmpty(monitorUrl)) {
			return;
		}
		
		try {
            Connection conn = Jsoup.connect(monitorUrl);
            Document html = conn.get(); // conn.post();
            Response res = conn.response();
            URL url = res.url();
            
            String strProtocol = "http://";
            if (url.getProtocol().equals("https")) {
            	strProtocol = "https://";
            }

    		String rootDir = strProtocol + url.getHost();
    		String currentDir = rootDir;
    		if (monitorUrl.replace(strProtocol, "").lastIndexOf("/") > 0) {
    			currentDir = monitorUrl.substring(0, monitorUrl.lastIndexOf("/"));
    		}
            
            Elements allEl = html.getAllElements();
            for (Element el : allEl) {

            	try {
	            	if (el.hasAttr("src") && !StringUtil.isEmpty(el.attr("src"))) {
	            		String src = el.attr("src");
	            		if (src.indexOf("http://") < 0 && src.indexOf("https://") < 0) {
	            			logger.debug("src: " + src);
	            			if (src.indexOf("//") == 0) { //protocol
	            				el.attr("src", strProtocol + ":" + src);
	            			} else if (src.indexOf("/") == 0) { //rootDir
	            				el.attr("src", rootDir + src);
	            			} else if (src.indexOf("../") == 0) { //up level
	            				String tempDir = currentDir;
	            				while(src.indexOf("../") == 0) {
	            					src = src.substring(3);
	            					if (rootDir.equals(tempDir)) continue;
	            					tempDir = tempDir.substring(0, tempDir.lastIndexOf("/"));
	            				}
	            				el.attr("src", tempDir + "/" + src);
	            			} else if (src.indexOf("./") == 0 || src.indexOf("/") > 0) { //currentDir
	            				src = src.replace("./", "");
	            				el.attr("src", currentDir + "/" + src);
	            			}
		            	}
	            	}
	            	if (el.normalName().equals("link") && el.hasAttr("href") && !StringUtil.isEmpty(el.attr("href"))) {
	            		String href = el.attr("href");
	            		if (href.indexOf("http://") < 0 && href.indexOf("https://") < 0) {
	            			logger.debug("href: " + href);
	            			if (href.indexOf("//") == 0) { //protocol
	            				el.attr("href", strProtocol + ":" + href);
	            			} else if (href.indexOf("/") == 0) { //rootDir
	            				el.attr("href", rootDir + href);
	            			} else if (href.indexOf("../") == 0) { //up level
	            				String tempDir = currentDir;
	            				while(href.indexOf("../") == 0) {
	            					href = href.substring(3);
	            					if (rootDir.equals(tempDir)) continue;
	            					tempDir = tempDir.substring(0, tempDir.lastIndexOf("/"));
	            				}
	            				el.attr("href", tempDir + "/" + href);
	            			} else if (href.indexOf("./") == 0 || href.indexOf("/") > 0) { //currentDir
	            				href = href.replace("./", "");
	            				el.attr("href", currentDir + "/" + href);
	            			}
		            	}
	            	}
            	} catch (Exception e) {
            		e.printStackTrace();
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
            	try {
	            	if (el.hasAttr("src") && !StringUtil.isEmpty(el.attr("src"))) {
		            	String src = el.attr("src");
		            	if (src.lastIndexOf("?") > 0) {
		            		src = src.substring(0, src.lastIndexOf("?"));
		            	}
		            	if (el.normalName().equals("img")) {
		            		String strFileName = src.replace(rootDir + "/", "").replace("/", "_");
		            		el.attr("src", strFileName);
		            		
		                	File fileImage = new File(originalDir.getAbsolutePath() + "/" + strFileName);
		                	FileUtils.copyURLToFile(new URL(src), fileImage);
		                	
		                	MessageDigest mdMD5 = MessageDigest.getInstance("MD5");
		                	mdMD5.update(IOUtils.toByteArray(new FileInputStream(fileImage)));
		                	byte[] md5Hash = mdMD5.digest();
		                	StringBuilder hexMD5hash = new StringBuilder();
		                    for(byte b : md5Hash) {
		                        String hexString = String.format("%02x", b);
		                        hexMD5hash.append(hexString);
		                    }        
		                    jsonObj.put(strFileName, hexMD5hash.toString());
		            	}
	            	}
            	} catch (Exception e) {
            		e.printStackTrace();
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
    
    
    public CrawlingInfo getOriginalInfo() throws Exception {
    	
    	StringBuffer sbHtml = new StringBuffer();
    	
		File file = new File(CAMS_HOME + "forgery/" + object_id + "/original/urlPage.html");
		FileReader reader = new FileReader(file);
		try (BufferedReader br = new BufferedReader(reader)) {
			String  line = null;
			while ((line = br.readLine()) != null) {
				sbHtml.append(line).append("\n");
			}
		} catch (IOException e) {
			throw e;
		}
    	
		JSONParser parser = new JSONParser();
		Object obj = parser.parse(new FileReader(CAMS_HOME + "forgery/" + object_id + "/original/imageHash.json"));
		JSONObject imageHash = (JSONObject) obj;	    		
    	
    	return new CrawlingInfo(object_id, sbHtml.toString(), (HashMap)imageHash);
    }
    
    @Override
    public String toString(){
        return "";
    }
    
	@SuppressWarnings("unchecked")
	public void getForgeryData(CrawlingInfo originalInfo) throws Exception {

		if (StringUtil.isEmpty(host)) {
			return;
		}
		
		try {
            Connection conn = Jsoup.connect(host);
            Document doc = conn.get(); // conn.post();
            Response res = conn.response();
            URL url = res.url();
            
            String strProtocol = "http://";
            if (url.getProtocol().equals("https")) {
            	strProtocol = "https://";
            }

    		String rootDir = strProtocol + url.getHost();
    		String currentDir = rootDir;
    		if (host.replace(strProtocol, "").lastIndexOf("/") > 0) {
    			currentDir = host.substring(0, host.lastIndexOf("/"));
    		}
            
            Elements allEl = doc.getAllElements();
            for (Element el : allEl) {

            	try {
	            	if (el.hasAttr("src") && !StringUtil.isEmpty(el.attr("src"))) {
	            		String src = el.attr("src");
	            		if (src.indexOf("http://") < 0 && src.indexOf("https://") < 0) {
	            			if (src.indexOf("//") == 0) { //protocol
	            				el.attr("src", strProtocol + ":" + src);
	            			} else if (src.indexOf("/") == 0) { //rootDir
	            				el.attr("src", rootDir + src);
	            			} else if (src.indexOf("../") == 0) { //up level
	            				String tempDir = currentDir;
	            				while(src.indexOf("../") == 0) {
	            					src = src.substring(3);
	            					if (rootDir.equals(tempDir)) continue;
	            					tempDir = tempDir.substring(0, tempDir.lastIndexOf("/"));
	            				}
	            				el.attr("src", tempDir + "/" + src);
	            			} else if (src.indexOf("./") == 0 || src.indexOf("/") > 0) { //currentDir
	            				src = src.replace("./", "");
	            				el.attr("src", currentDir + "/" + src);
	            			}
		            	}
	            	}
            	} catch (Exception e) {
            		e.printStackTrace();
            	}
            }
            
            File logDir = new File(CAMS_HOME + "forgery/" + object_id + "/" + (new SimpleDateFormat("yyyyMMddHHmm")).format(logTime));
            if (!logDir.exists()) {
            	logDir.mkdir();
            }

            FileWriter fw = new FileWriter(logDir.getAbsolutePath() + "/" + "urlPage.html");
            fw.write(doc.html());
            fw.flush();
            fw.close();
            
            JSONObject jsonObj = new JSONObject();
            
            for (Element el : allEl) {
            	try {
	            	if (el.hasAttr("src") && !StringUtil.isEmpty(el.attr("src"))) {
		            	String src = el.attr("src");
		            	if (src.lastIndexOf("?") > 0) {
		            		src = src.substring(0, src.lastIndexOf("?"));
		            	}
		            	if (el.normalName().equals("img")) {
		            		String strFileName = src.replace(rootDir + "/", "").replace("/", "_");
		            		el.attr("src", strFileName);
		            		
		                	File fileImage = new File(logDir.getAbsolutePath() + "/" + strFileName);
		                	FileUtils.copyURLToFile(new URL(src), fileImage);
		                	
		                	MessageDigest mdMD5 = MessageDigest.getInstance("MD5");
		                	mdMD5.update(IOUtils.toByteArray(new FileInputStream(fileImage)));
		                	byte[] md5Hash = mdMD5.digest();
		                	StringBuilder hexMD5hash = new StringBuilder();
		                    for(byte b : md5Hash) {
		                        String hexString = String.format("%02x", b);
		                        hexMD5hash.append(hexString);
		                    }        
		                    jsonObj.put(strFileName, hexMD5hash.toString());
		            	}
	            	}
            	} catch (Exception e) {
            		e.printStackTrace();
            	}
            }
            
    		File file = new File(logDir.getAbsolutePath() + "/" + "urlPage.html");

    		logger.debug("\n========================================================================");
            logger.debug("HtmlDocSize: {} / {} => {}", doc.html().length(), originalInfo.getHtmlLength(), (Math.abs(doc.html().length() - originalInfo.getHtmlLength())*100F)/originalInfo.getHtmlLength());
            logger.debug("ImageCount: {} / {} => {}", jsonObj.size(), originalInfo.getImageCount());
            
            JSONObject addImageJson = new JSONObject();
            JSONObject changeImageJson = new JSONObject();
            JSONObject deleteImageJson = new JSONObject();
            
            Iterator<String> it = jsonObj.keySet().iterator();
            while (it.hasNext()) {
            	String key = it.next();

    			String imageHash = originalInfo.getImageHash(key);
    			if (StringUtil.isEmpty(imageHash)) { //add image
    				addImageJson.put(key, jsonObj.get(key));
    			} else if (!imageHash.equals(jsonObj.get(key))) { //change image
    				changeImageJson.put(key, jsonObj.get(key));
    			} else { //same image
    				originalInfo.deleteImageHash(key);
    				it.remove();
    			}
            }            
    		
    		deleteImageJson = (JSONObject)originalInfo.getImageHash();
    		
    		
    		//imageCount
			//htmlLength
			//addImage
			//changeImage
			//deleteImage
    		

    		//로그저장
    		
    		
    		//이벤트저장
    		
    		
            
        } catch (HttpStatusException e) {
            e.printStackTrace();
        }		
		
	}	

	public void saveLog() {
		
		
		
		
	}
	
	public void saveEvent() {
		
	}
	
	
	
	
}
