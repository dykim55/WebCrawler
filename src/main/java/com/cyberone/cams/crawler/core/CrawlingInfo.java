package com.cyberone.cams.crawler.core;

import java.util.HashMap;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class CrawlingInfo {

	private Elements allEl;
	
	private HashMap<String, String> imageHashMap;

	private final Logger logger = LoggerFactory.getLogger(CrawlingInfo.class);
	
	public CrawlingInfo(String html, HashMap<String, String> imageHashMap, String monitorId) {
		Document doc = Jsoup.parse(html);
		this.allEl = doc.getAllElements();
		this.imageHashMap = imageHashMap;
		
		logger.debug("[{}] allEl.html().length(): {}", monitorId, this.allEl.html().length());
		logger.debug("[{}] allEl.outerHtml().length(): {}", monitorId, this.allEl.outerHtml().length());
	}
	
	public CrawlingInfo(Document doc) {
		this.allEl = doc.getAllElements();
	}
	
	public int getImageCount() {
		int imageCount = 0;
        for (Element el : allEl) {
        	if (el.normalName().equals("img") && el.hasAttr("src")) {
        		imageCount++;
        	}
        }
		return imageCount;
	}
	
	public String getOriginalImageHash(String key) {
		String hashValue = imageHashMap.get(key);
		if (hashValue != null) {
			imageHashMap.remove(key);	
		}
		return hashValue;
	}
}
