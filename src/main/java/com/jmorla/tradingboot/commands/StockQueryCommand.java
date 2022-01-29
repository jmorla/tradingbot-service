package com.jmorla.tradingboot.commands;

import com.jmorla.tradingboot.model.Stock;
import com.jmorla.tradingboot.service.StockWrService;
import org.telegram.telegrambots.extensions.bots.commandbot.commands.BotCommand;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.objects.Chat;
import org.telegram.telegrambots.meta.api.objects.User;
import org.telegram.telegrambots.meta.bots.AbsSender;
import org.telegram.telegrambots.meta.exceptions.TelegramApiException;

public class StockQueryCommand extends BotCommand {

    private final String chatBot;
    private final StockWrService stockWrService;

    public StockQueryCommand(String chatBot, StockWrService stockWrService) {
        super("query", "Query last stock info");
        this.chatBot = chatBot;
        this.stockWrService = stockWrService;
    }

    @Override
    public void execute(AbsSender absSender, User user, Chat chat, String[] arguments) {
        if (arguments == null) {
            sendMissingSymbolMessage(absSender);
        }
        String symbol = arguments[0]; // only query first
        if (symbol != null && !symbol.isBlank()) {
            stockWrService.findStockBySymbol(symbol)
                    .ifPresentOrElse(
                            stock -> sendStockQuoteMessage(absSender, stock),
                            () -> sendStockNotFoundMessage(absSender));
        }
    }

    private void sendStockNotFoundMessage(AbsSender sender) {
        SendMessage message = new SendMessage();
        message.enableHtml(true);
        message.setText("<i>Stock not found<i>");
        try {
            sender.execute(message);
        } catch (TelegramApiException ex) {
            ex.printStackTrace();
        }
    }

    private void sendStockQuoteMessage(AbsSender sender, Stock stock) {
        StringBuilder builder = new StringBuilder();
        builder.append("<b>");
        builder.append(stock.getName());
        builder.append("</b>");
        builder.append("\n\n");
        builder.append("<b>Symbol: </b>");
        builder.append(stock.getSymbol());
        builder.append("\n");
        builder.append("<b>Price: </b>");
        builder.append(stock.getPrice());

        SendMessage message = new SendMessage();
        message.setChatId(chatBot);
        message.enableHtml(true);
        message.setText(builder.toString());
        try {
            sender.execute(message);
        } catch (TelegramApiException ex) {
            ex.printStackTrace();
        }
    }

    private void sendMissingSymbolMessage(AbsSender sender) {
        SendMessage message = new SendMessage();
        message.enableHtml(true);
        message.setText("Usage: <pre>/query [symbol]<pre>");
        try {
            sender.execute(message);
        } catch (TelegramApiException ex) {
            ex.printStackTrace();
        }
    }
}
