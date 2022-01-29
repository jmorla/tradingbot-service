package com.jmorla.tradingboot.commands;

import org.telegram.telegrambots.extensions.bots.commandbot.commands.BotCommand;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.objects.Chat;
import org.telegram.telegrambots.meta.api.objects.User;
import org.telegram.telegrambots.meta.bots.AbsSender;
import org.telegram.telegrambots.meta.exceptions.TelegramApiException;

public class StockHelpCommand extends BotCommand {

    private final String chatId;

    public StockHelpCommand(String chatId) {
        super("help", "Display bot help information");
        this.chatId = chatId;
    }

    @Override
    public void execute(AbsSender absSender, User user, Chat chat, String[] arguments) {

        StringBuilder builder = new StringBuilder();

        builder.append("Available commands \n\n");
        builder.append("<b>/add [STOCKS, SEPARATED, BY, COMMA]:</b> Add stock/s to watchlist\n");
        builder.append("<b>/remove [STOCKS, SEPARATED, BY, COMMA]: </b> Remove stock/s to watchlist\n");
        builder.append("<b>/watchlist :</b> display stocks watchlist\n");
        builder.append("<b>/query [SINGLE STOCK] :</b> display stock information\n");
        SendMessage message = new SendMessage();

        message.setChatId(chatId);
        message.setText(builder.toString());
        message.enableHtml(true);

        try {
            absSender.execute(message);
        } catch (TelegramApiException ex) {
        }
    }
}
