package com.ixerion.mercybot.util;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.io.IOException;
import java.util.Properties;

public enum PropConstants {

    SITE,

    ONE_DAY_FILTER,
    THREE_DAYS_FILTER,
    ONE_WEEK_FILTER,
    ONE_MONTH_FILTER,

    QUERY,

    IMAGE_LIMIT,

    BOT_NAME,
    BOT_TOKEN;

    private static Properties properties;

    private String value;

    PropConstants() {
    }

    private static Logger logger = LogManager.getLogger(PropConstants.class);

    private void init() {
        if (properties == null) {
            properties = new Properties();

            try {
                properties.load(Thread.currentThread().getContextClassLoader().getResourceAsStream("jsoup.properties"));
            } catch (IOException e) {
                logger.catching(e);
            }
        }
        value = (String) properties.get(this.toString());
    }

    public String getValue() {
        if (value == null) {
            init();
        }
        return value;
    }
}
