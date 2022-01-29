package com.jmorla.tradingboot.service;

import com.fasterxml.jackson.core.exc.StreamReadException;
import com.fasterxml.jackson.databind.DatabindException;
import com.fasterxml.jackson.databind.json.JsonMapper;
import com.jmorla.tradingboot.model.Stock;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import yahoofinance.YahooFinance;

import java.io.File;
import java.io.IOException;
import java.nio.file.Paths;
import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.Stream;

@Service
public class StockWrService {

    private static final Logger log = LoggerFactory.getLogger(StockWrService.class);

    private static final String STOCK_STORAGE_PATH = "stocks.json";
    private static Set<Stock> stocks = new HashSet<>(0);
    private JsonMapper mapper;

    public StockWrService(JsonMapper mapper) {
        this.mapper = mapper;
        loadState();
    }

    private void loadState() {
        File file = Paths.get(STOCK_STORAGE_PATH).toFile();
        try {
            if (file.exists()) {
                StocksModel model = mapper.readValue(file, StocksModel.class);
                model.getStocks().stream().forEach(stocks::add);
            } else {
                file.createNewFile();
                mapper.writeValue(file, new StocksModel());
            }
        } catch (StreamReadException | DatabindException ex) {
            log.error("unable to load state, parsing error");
            ex.printStackTrace();
        } catch (IOException e) {
            log.error("unable to load state, file reading error");
            e.printStackTrace();
        }
    }

    private void saveState() {
        File file = Paths.get(STOCK_STORAGE_PATH).toFile();
        try {
            if (!file.exists()) {
                file.createNewFile();
            }
        } catch (IOException ex) {
            log.error("unable to create state file, check filesystem permissions");
        }
        writeState(file, stocks);
    }



    private void writeState(File file, Set<Stock> stocks) {
        try {
            StocksModel model = new StocksModel();
            model.setStocks(stocks);
            mapper.writeValue(file, model);
        } catch (StreamReadException | DatabindException ex) {
            ex.printStackTrace();
        } catch (IOException ex) {
            ex.printStackTrace();
        }
    }


    public Optional<Stock> getStock(String symbol) {
        return stocks.stream().filter(s -> s.getSymbol().equals(symbol)).findFirst();
    }

    public boolean hasStock(String symbol) {
        return stocks.stream().filter(s -> s.getSymbol().equals(symbol)).count() == 1;
    }

    public boolean addStock(String symbol) {
        if (hasStock(symbol)) {
            return false;
        }
        Optional<Stock> stock = findStockBySymbol(symbol);
        if (stock.isPresent()) {
            stocks.add(stock.get());
            saveState(); // saving stocks on disk
            return true;
        }
        return true;
    }

    public void addStocks(String... symbols) {
        String[] filteredSymbols = Stream.of(symbols).filter(s -> !hasStock(s)).toArray(String[]::new);
        List<Stock> _stocks = findStocksBySymbols(filteredSymbols);
        if(!_stocks.isEmpty()) {
            stocks.addAll(_stocks);
            saveState();
        }
    }

    public List<Stock> findStocksBySymbols(String... symbols) {
        List<Stock> stocks = new ArrayList<>();
        try {
            YahooFinance.get(symbols)
                    .entrySet()
                    .stream()
                    .map(Map.Entry::getValue)
                    .map(Stock::new)
                    .forEach(stocks::add);
        } catch (IOException ex) {
            ex.printStackTrace();
        }

        return stocks;
    }

    public Optional<Stock> findStockBySymbol(String symbol) {
        Stock stock = null;
        try {
            stock = new Stock(YahooFinance.get(symbol));
        } catch (IOException ex) {
            ex.printStackTrace();
        }

        return Optional.ofNullable(stock);
    }

    public boolean removeStock(String symbol) {
        boolean removed =  stocks.removeIf(s -> s.getSymbol().equals(symbol));
        if(removed) {
            saveState();
        }
        return  removed;
    }

    public Set<Stock> getAll() {
        // returning a new copy
        return stocks.stream().collect(Collectors.toSet());
    }
}
