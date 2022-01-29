package com.jmorla.tradingboot.service;

import com.jmorla.tradingboot.model.Stock;

import java.util.HashSet;
import java.util.Set;

public class StocksModel {

    private Set<Stock> stocks = new HashSet<>(0);

    public Set<Stock> getStocks() {
        return stocks;
    }

    public void setStocks(Set<Stock> stocks) {
        this.stocks = stocks;
    }
}
