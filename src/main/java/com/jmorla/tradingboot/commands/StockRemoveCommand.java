package com.jmorla.tradingboot.commands;

import com.jmorla.tradingboot.service.StockWrService;
import org.telegram.telegrambots.extensions.bots.commandbot.commands.BotCommand;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.objects.Chat;
import org.telegram.telegrambots.meta.api.objects.User;
import org.telegram.telegrambots.meta.bots.AbsSender;
import org.telegram.telegrambots.meta.exceptions.TelegramApiException;

import java.util.stream.Collectors;
import java.util.stream.Stream;

public class StockRemoveCommand extends BotCommand {

    private final String chatId;
    private final StockWrService stockWrDatabase;

    public StockRemoveCommand(String chatId, StockWrService stockWrDatabase) {
        super("remove", "Remove stock from watchlist");
        this.chatId = chatId;
        this.stockWrDatabase = stockWrDatabase;
    }

    @Override
    public void execute(AbsSender absSender, User user, Chat chat, String[] arguments) {

        var stocks = Stream.of(arguments).filter(stock -> stockWrDatabase.removeStock(stock))
                .collect(Collectors.toSet());

        StringBuilder builder = new StringBuilder();
        builder.append("<b>Removed stocks</b>");
        builder.append("\n");
        builder.append("\n");
        if (!stocks.isEmpty())
            stocks.stream().forEach(s -> {
                builder.append(s);
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
