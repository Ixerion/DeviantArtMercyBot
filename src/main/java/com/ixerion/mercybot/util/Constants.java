package com.ixerion.mercybot.util;

import java.io.IOException;
import java.util.Properties;

public enum Constants {

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

    Constants() {
    }

    private void init() {
        if (properties == null) {
            properties = new Properties();

            try {
                properties.load(Thread.currentThread().getContextClassLoader().getResourceAsStream("jsoup.properties"));
            } catch (IOException e) {
                e.printStackTrace();
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
