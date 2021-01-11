package com.cyberone.cams.crawler.core;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

@Component 
public class WebCrawlerSchedule { 
    
    private final Logger logger = LoggerFactory.getLogger(WebCrawlerSchedule.class);
    
    private long lLastTime;
    private long millis;    

    @Scheduled(cron = "0/10 * * * * ?")
    public void WebCrawlingJob() { 

        try {
            logger.debug("WebCrawlingJob Start...");
            lLastTime = System.currentTimeMillis();
            
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            millis = System.currentTimeMillis() - lLastTime;
            logger.debug("WebCrawlingJob End... [소요시간: " + millis + "]");
        }
    }

}
