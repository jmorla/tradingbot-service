package com.jmorla.tradingboot.commands;

import com.jmorla.tradingboot.model.Stock;
import com.jmorla.tradingboot.service.StockWrService;
import org.telegram.telegrambots.extensions.bots.commandbot.commands.BotCommand;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.objects.Chat;
import org.telegram.telegrambots.meta.api.objects.User;
import org.telegram.telegrambots.meta.bots.AbsSender;
import org.telegram.telegrambots.meta.exceptions.TelegramApiException;

import java.util.Set;

public class StockWatchListCommand extends BotCommand {

    private final String chatId;
    private final StockWrService stockWrService;

    public StockWatchListCommand(String chatId, StockWrService stockWrService) {
        super("watchlist", "display stock watchlist");
        this.chatId = chatId;
        this.stockWrService = stockWrService;
    }

    @Override
    public void execute(AbsSender absSender, User user, Chat chat, String[] arguments) {
        Set<Stock> stocks = stockWrService.getAll();
        StringBuilder builder = new StringBuilder();
        builder.append("<b>Stock Watch list</b>");
        builder.append("\n");
        builder.append("\n");
        if (!stocks.isEmpty())
            stocks.stream().forEach(s -> {
                builder.append("<b>");
                builder.append(s.getSymbol());
                builder.append("</b>");
                builder.append(" : ");
                builder.append(s.getName());
                builder.append("\n");
            });
        else
            builder.append("<i>Empty</i>");

        SendMessage message = new SendMessage();
        message.enableHtml(true);
        message.setChatId(chatId);
        message.setText(builder.toString());

        try {
            absSender.execute(message);
        } catch (TelegramApiException exception) {
            exception.printStackTrace();
        }
    }
}
