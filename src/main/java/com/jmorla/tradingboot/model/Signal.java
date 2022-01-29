package com.jmorla.tradingboot.model;

public class Signal {

    private String symbol;
    private double price;
    private Operation operation;


    public Signal (String stock, double price, Operation operation) {
        this.symbol = stock;
        this.price = price;
        this.operation = operation;
    }

    public String getSymbol() {
        return symbol;
    }

    public double getPrice() {
        return price;
    }

    public Operation getOperation() {
        return operation;
    }
}
