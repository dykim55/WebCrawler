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
	private int htmlLength;
	private int imageCount;
	private final Logger logger = LoggerFactory.getLogger(CrawlingInfo.class);
	
	public CrawlingInfo(String monitorId, String html, HashMap<String, String> imageHashMap) {
		Document doc = Jsoup.parse(html);
		this.allEl = doc.getAllElements();
		this.htmlLength = doc.html().length();
		this.imageHashMap = imageHashMap;
		this.imageCount = imageHashMap.size();
	}
	
	public CrawlingInfo(Document doc) {
		this.allEl = doc.getAllElements();
		this.htmlLength = doc.html().length();
	}
	
	public int getImageCount() {
		return imageCount;
		/*
		int imageCount = 0;
        for (Element el : allEl) {
        	if (el.normalName().equals("img") && el.hasAttr("src")) {
        		imageCount++;
        	}
        }
		return imageCount;*/
	}
	
	public String getImageHash(String key) {
		String hashValue = imageHashMap.get(key);
		if (hashValue != null) {
			imageHashMap.remove(key);	
		}
		return hashValue;
	}
	
	public void deleteImageHash(String key) {
		imageHashMap.remove(key);
	}
	
	public HashMap<String, String> getImageHash() {
		return imageHashMap;
	}
	
	public int getHtmlLength() {
		return htmlLength;
	}
}
