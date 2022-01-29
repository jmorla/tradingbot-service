package com.jmorla.tradingboot.service;

import com.jmorla.tradingboot.model.Operation;
import com.jmorla.tradingboot.model.Signal;
import com.jmorla.tradingboot.strategies.WilliamsRStrategyFactory;
import org.ta4j.core.*;
import yahoofinance.Stock;
import yahoofinance.YahooFinance;
import yahoofinance.histquotes.HistoricalQuote;
import yahoofinance.histquotes.Interval;

import java.io.IOException;
import java.time.Duration;
import java.time.LocalDate;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.time.temporal.ChronoUnit;
import java.util.*;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import java.util.function.Consumer;
import java.util.stream.Collectors;

public class WilliamsRStrategyService {

    private List<Signal> activeSignals = new ArrayList<>(0);
    private final ScheduledExecutorService executorService;

    public WilliamsRStrategyService() {
        this.executorService = Executors.newSingleThreadScheduledExecutor();
    }

    public WilliamsRStrategyService(ScheduledExecutorService executorService) {
        this.executorService = executorService;
    }

    public void onMarketClose(String symbol, Consumer<Signal> action) {
        executorService.scheduleAtFixedRate(() ->
                runStrategyOnSymbol(symbol).ifPresent(action), getInitialDelay(18), 1440, TimeUnit.MINUTES);
    }

    private long getInitialDelay(int startHour) {
        ZoneId zoneId = ZoneId.of("America/New_York");
        ZonedDateTime current = ZonedDateTime.now().withZoneSameInstant(zoneId);
        ZonedDateTime startTime = ZonedDateTime
                .now()
                .withHour(startHour)
                .withMinute(0).withZoneSameInstant(zoneId);

        long timeLeft = current.until(startTime, ChronoUnit.MINUTES);
        return timeLeft > 0 ? timeLeft : 1;
    }

    private Optional<Signal> runStrategyOnSymbol(String symbol) {
        var series = getBarSeries(symbol);
        int lastIndex = series.getEndIndex();
        Bar currentBar = series.getBar(lastIndex);
        Signal signal = null;
        Strategy strategy = WilliamsRStrategyFactory.getDefault(series);
        if (strategy.shouldEnter(lastIndex)) {
            // only generate signal if it does not exist
            if (!activeSignals.stream().anyMatch(s -> s.getSymbol().equals(symbol))) {
                signal = new Signal(symbol, currentBar.getClosePrice().doubleValue(), Operation.LONG);
                activeSignals.add(signal); // store the signal to not show it again
            }
        } else if (strategy.shouldExit(lastIndex)) {
            // remove signal because is not valid anymore
            activeSignals.removeIf(s -> s.getSymbol().equals(symbol));
        }

        return Optional.ofNullable(signal);
    }

    private List<HistoricalQuote> getHistoricalQuoteSymbol(String symbol) {
        try {
            LocalDate to = LocalDate.now();
            LocalDate from = to.minusDays(30);
            Stock aapl = YahooFinance.get(symbol);
            return aapl.getHistory(GregorianCalendar.from(from.atStartOfDay(ZoneId.systemDefault())),
                    GregorianCalendar.from(to.atStartOfDay(ZoneId.systemDefault())), Interval.DAILY);
        } catch (IOException ex) {
            return Collections.emptyList();
        }
    }

    private List<Bar> mapToBars(List<HistoricalQuote> historicalQuotes) {
        List<Bar> bars = historicalQuotes.stream().map(q -> new BaseBar(Duration.ofDays(1),
                q.getDate().toInstant().atZone(ZoneId.systemDefault()),
                q.getOpen().doubleValue(),
                q.getHigh().doubleValue(),
                q.getLow().doubleValue(),
                q.getClose().doubleValue(),
                q.getVolume().doubleValue())).collect(Collectors.toList());
        return bars;
    }

    private BarSeries getBarSeries(String symbol) {
        var bars = mapToBars(getHistoricalQuoteSymbol(symbol));

        return new BaseBarSeriesBuilder()
                .withName(symbol)
                .withBars(bars)
                .build();
    }

}
