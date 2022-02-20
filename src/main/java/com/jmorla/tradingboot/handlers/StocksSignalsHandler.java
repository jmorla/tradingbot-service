package com.jmorla.tradingboot.handlers;

import com.jmorla.tradingboot.commands.*;
import com.jmorla.tradingboot.model.Operation;
import com.jmorla.tradingboot.model.Signal;
import com.jmorla.tradingboot.model.Stock;
import com.jmorla.tradingboot.service.WilliamsRStrategyService;
import com.jmorla.tradingboot.service.StockWrService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.telegram.telegrambots.extensions.bots.commandbot.TelegramLongPollingCommandBot;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.objects.Update;
import org.telegram.telegrambots.meta.bots.AbsSender;
import org.telegram.telegrambots.meta.exceptions.TelegramApiException;
import java.util.HashSet;
import java.util.Set;
import java.util.stream.Collectors;

@Component
public class StocksSignalsHandler extends TelegramLongPollingCommandBot {

    private static Logger log = LoggerFactory.getLogger(StocksSignalsHandler.class);

    @Value("${bot.name}")
    private String botUsername;

    @Value("${bot.token}")
    private String botToken;

    private String botChatId = "-739175427";

    private static Set<String> availableSymbols;

    private final WilliamsRStrategyService williamsRStrategyService;

    public StocksSignalsHandler(StockWrService stockWrDatabase) {
        availableSymbols = stockWrDatabase.getAll()
                .stream()
                .map(Stock::getSymbol)
                .collect(Collectors.toSet());

        log.info("Start watching symbols: {}", availableSymbols);

        register(new StockAddCommand(botChatId, stockWrDatabase));
        register(new StockRemoveCommand(botChatId, stockWrDatabase));
        register(new StockWatchListCommand(botChatId, stockWrDatabase));
        register(new StockQueryCommand(botChatId, stockWrDatabase));
        register(new StockHelpCommand(botChatId));

        registerDefaultAction(((absSender, message) -> sendCommandNotFoundResponse(absSender)));

        this.williamsRStrategyService = new WilliamsRStrategyService();
        availableSymbols.stream()
                .forEach(symbol -> williamsRStrategyService
                        .onMarketClose(symbol, (signal) -> sendSignalMessage(signal)));

        log.info("Handler initialized successfully");
    }


    private void sendSignalMessage(Signal signal) {
        StringBuilder builder = new StringBuilder();
        builder.append("<b>");
        builder.append(signal.getOperation().equals(Operation.LONG) ? "Buy" : "Sell");
        builder.append("</b>");
        builder.append(" Signal");
        builder.append("\n \n");
        builder.append("<b>Stock: </b>");
        builder.append(signal.getSymbol());
        builder.append("\n");
        builder.append("<b>Price: </b>");
        builder.append(signal.getPrice());

        SendMessage message = new SendMessage();
        message.setChatId(botChatId);
        message.enableHtml(true);
        message.setText(builder.toString());

        try {
            this.execute(message);
        } catch (TelegramApiException ex) {
            ex.printStackTrace();
        }
    }

    private void sendCommandNotFoundResponse(AbsSender absSender) {

        StringBuilder builder = new StringBuilder();

        builder.append("Command not found \n\n");
        builder.append("Available commands \n\n");
        builder.append("<b>/add :</b> Add stock/s to watchlist\n");
        builder.append("<b>/remove: </b> Remove stock/s to watchlist\n");
        builder.append("<b>/watchlist :</b> display stocks watchlist\n");
        SendMessage message = new SendMessage();
        System.out.println(botChatId);
        message.setChatId(botChatId);
        message.setText(builder.toString());
        message.enableHtml(true);

        try {
            absSender.execute(message);
        } catch (TelegramApiException ex) {
        }
    }

    @Override
    public String getBotUsername() {
        return botUsername;
    }

    @Override
    public void processNonCommandUpdate(Update update) {
        System.out.println(update.getMessage().getChatId());
    }

    @Override
    public String getBotToken() {
        return botToken;
    }

    @Override
    public void onRegister() {
        super.onRegister();
    }
}
