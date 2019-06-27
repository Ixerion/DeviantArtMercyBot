package com.ixerion.mercybot.builder;

import static com.ixerion.mercybot.util.PropConstants.SITE;

public class Query {

    private String filter;
    private String searchObject;
    private int amount;

    public int getAmount() {
        return amount;
    }

    public Query setAmount(int amount) {
        this.amount = amount;
        return this;
    }

    public String getFilter() {
        return filter;
    }

    public Query setFilter(String filter) {
        this.filter = filter;
        return this;
    }

    public String getSearchObject() {
        return searchObject;
    }

    public Query setSearchObject(String searchObject) {
        this.searchObject = searchObject;
        return this;
    }

    @Override
    public String toString() {
        return SITE.getValue() + filter + searchObject;
    }

}
