package com.cyberone.cams.crawler.core;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

@Component 
public class WebCrawlerSchedule { 
    
    private final Logger logger = LoggerFactory.getLogger(WebCrawlerSchedule.class);

    private long lLastTime;
    private long millis;    

    @Scheduled(cron = "0 0/1 * * * ?")
    public void WebCrawlingJob() { 

    	ExecutorService executor = Executors.newFixedThreadPool(1);
    	
        try {
            logger.debug("WebCrawlingJob Start...");
            lLastTime = System.currentTimeMillis();
            
            List<Map<String, Object>> dataList = generateData();
            
            for (Map<String, Object> data : dataList) {
            	
				Runnable worker = new ForgeryDetection(data);
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
	
	public List<Map<String, Object>> generateData() {
		
		List<Map<String, Object>> dataList = new ArrayList<Map<String, Object>>();

		Map<String, Object> hMap = new HashMap<String, Object>();
		hMap.put("_id", "3333333333");
		hMap.put("host", "https://edu.kcga.go.kr/kcgrc/index0.jsp");
		dataList.add(hMap);

		hMap = new HashMap<String, Object>();
		hMap.put("_id", "1111111111");
		hMap.put("host", "http://www.kcg.go.kr/kcg/main.do");
		dataList.add(hMap);

		hMap = new HashMap<String, Object>();
		hMap.put("_id", "2222222222");
		hMap.put("host", "https://edu.kcga.go.kr/main0.jsp");
		dataList.add(hMap);

		hMap = new HashMap<String, Object>();
		hMap.put("_id", "4444444444");
		hMap.put("host", "http://www.kcg.go.kr/mckcg/main.do");
		dataList.add(hMap);

		hMap = new HashMap<String, Object>();
		hMap.put("_id", "5555555555");
		hMap.put("host", "http://www.kcg.go.kr/jungbucgh/main.do");
		dataList.add(hMap);

		hMap = new HashMap<String, Object>();
		hMap.put("_id", "6666666666");
		hMap.put("host", "http://www.kcg.go.kr/inchoncgs/main.do");
		dataList.add(hMap);

		hMap = new HashMap<String, Object>();
		hMap.put("_id", "7777777777");
		hMap.put("host", "http://www.kcg.go.kr/pyeongtaekcgs/main.do");
		dataList.add(hMap);

		hMap = new HashMap<String, Object>();
		hMap.put("_id", "8888888888");
		hMap.put("host", "http://www.kcg.go.kr/seohae5docgs/main.do");
		dataList.add(hMap);

		hMap = new HashMap<String, Object>();
		hMap.put("_id", "9999999999");
		hMap.put("host", "http://www.kcg.go.kr/jejucgh/main.do");
		dataList.add(hMap);

		return dataList;
	}
	
}
