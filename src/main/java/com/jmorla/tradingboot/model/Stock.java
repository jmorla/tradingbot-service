package com.jmorla.tradingboot.model;

import com.fasterxml.jackson.annotation.JsonIgnore;

import java.math.BigDecimal;

public class Stock {
    private String symbol;
    private String name;
    @JsonIgnore
    private BigDecimal price;

    public Stock () {

    }

    public Stock (yahoofinance.Stock stock) {
        this.symbol = stock.getSymbol();
        this.name = stock.getName();
        this.price = stock.getQuote().getPrice();
    }

    public String getSymbol() {
        return symbol;
    }

    public void setSymbol(String symbol) {
        this.symbol = symbol;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public BigDecimal getPrice() {
        return price;
    }

    public void setPrice(BigDecimal price) {
        this.price = price;
    }
}
