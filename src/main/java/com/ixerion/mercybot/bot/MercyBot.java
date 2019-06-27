package com.ixerion.mercybot.bot;

import com.ixerion.mercybot.builder.Query;
import com.ixerion.mercybot.parser.ImageParser;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.telegram.telegrambots.bots.TelegramLongPollingBot;
import org.telegram.telegrambots.meta.api.methods.send.SendMediaGroup;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.objects.Message;
import org.telegram.telegrambots.meta.api.objects.Update;
import org.telegram.telegrambots.meta.api.objects.media.InputMedia;
import org.telegram.telegrambots.meta.api.objects.media.InputMediaPhoto;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.ReplyKeyboard;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.ReplyKeyboardMarkup;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.buttons.KeyboardRow;
import org.telegram.telegrambots.meta.exceptions.TelegramApiException;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

import static com.ixerion.mercybot.util.PropConstants.*;
import static com.ixerion.mercybot.util.StringConstants.*;

public class MercyBot extends TelegramLongPollingBot {

    private static Logger logger = LogManager.getLogger(MercyBot.class);

    private static final int MAIN_MENU_STATE = 0;
    private static final int SETTINGS_STATE = 1;
    private static final int FILTERS_STATE = 2;

    private static Query query;

    private static int currentState;

    public MercyBot() {
        initDefaultQuery();
        currentState = MAIN_MENU_STATE;
    }

    private static void initDefaultQuery() {
        query = new Query()
                .setFilter(ONE_MONTH_FILTER.getValue())
                .setSearchObject(QUERY.getValue())
                .setAmount(Integer.valueOf(IMAGE_LIMIT.getValue()));
        logger.info("Default query " + query.toString() + " created");
    }

    @Override
    public void onUpdateReceived(Update update) {
        if (update.hasMessage()) {
            Message message = update.getMessage();
            if (message.hasText()) {
                try {
                    handleIncomingMessage(message);
                    logger.info("Current message: " + message.getText() + ", current state: " + currentState);
                } catch (TelegramApiException e) {
                    logger.catching(e);

                }
            }
        }
    }

    private void handleIncomingMessage(Message message) throws TelegramApiException {
        SendMessage sendMessage;
        switch (currentState) {
            case MAIN_MENU_STATE:
                sendMessage = messageOnMainMenu(message);
                if (sendMessage != null) {
                    execute(sendMessage);
                } else {
                    execute(messageOnSendRequest(message));
                }
                break;
            case SETTINGS_STATE:
                sendMessage = messageOnSettings(message);
                execute(sendMessage);
                break;
            case FILTERS_STATE:
                sendMessage = messageOnFilterSettings(message);
                execute(sendMessage);
                break;
            default:
                sendMessage = sendMessageDefault(message);
                execute(sendMessage);
                break;
        }
    }

    //send request message

    private SendMediaGroup messageOnSendRequest(Message message) {
        ImageParser parser = new ImageParser();
        List<String> urls = parser.getImages(query);
        List<InputMedia> media = urls.stream().map(url -> new InputMediaPhoto().setMedia(url)).collect(Collectors.toList());
        SendMediaGroup sendMediaGroup = new SendMediaGroup().setChatId(message.getChatId());
        sendMediaGroup.setMedia(media);

        return sendMediaGroup;
    }

    private static SendMessage messageOnMainMenu(Message message) {
        SendMessage sendMessage;
        if (message.getText().equals(getSettingsCommand())) {
            sendMessage = onSettingsChosen(message);
        } else if (message.getText().equals(getSearchRequestCommand())) {
            sendMessage = null;
        } else {
            sendMessage = sendChooseOptionMessage(message, getMainMenuKeyboard());
        }

        return sendMessage;
    }

    private static SendMessage onSettingsChosen(Message message) {
        currentState = SETTINGS_STATE;
        SendMessage sendMessage = new SendMessage();
        sendMessage.setText(SETTINGS_MESSAGE);
        sendMessage.setChatId(message.getChatId());
        sendMessage.setReplyMarkup(getSettingsKeyboard());

        return sendMessage;
    }

    private static SendMessage messageOnSettings(Message message) {
        SendMessage sendMessage = null;
        if (message.getText().equals(getFilterCommand())) {
            sendMessage = onFilterSettingsChosen(message);
        } else if (message.getText().equals(getBackCommand())) {
            sendMessage = sendMessageDefault(message);
        }

        return sendMessage;
    }

    private static SendMessage onFilterSettingsChosen(Message message) {
        currentState = FILTERS_STATE;
        SendMessage sendMessage = new SendMessage();
        sendMessage.setChatId(message.getChatId());
        sendMessage.setText(CHOOSE_FILTER_MESSAGE);
        sendMessage.setReplyMarkup(getFiltersKeyboard());

        return sendMessage;
    }

    private static SendMessage messageOnFilterSettings(Message message) {
        SendMessage sendMessage;
        switch (message.getText()) {
            case "one day":
                sendMessage = onOneDayFilterCommand(message);
                break;
            case "three days":
                sendMessage = onThreeDaysFilterCommand(message);
                break;
            case "one week":
                sendMessage = onOneWeekFilterCommand(message);
                break;
            case "one month":
                sendMessage = onOneMonthFilterCommand(message);
                break;
            default:
                sendMessage = onBackFilterSettingsCommand(message);
                break;
        }

        return sendMessage;
    }

    private static SendMessage onOneMonthFilterCommand(Message message) {
        return onFilterCommand(message, ONE_MONTH_FILTER.getValue());
    }

    private static SendMessage onOneWeekFilterCommand(Message message) {
        return onFilterCommand(message, ONE_WEEK_FILTER.getValue());
    }

    private static SendMessage onThreeDaysFilterCommand(Message message) {
        return onFilterCommand(message, THREE_DAYS_FILTER.getValue());
    }

    private static SendMessage onOneDayFilterCommand(Message message) {
        return onFilterCommand(message, ONE_DAY_FILTER.getValue());
    }

    private static SendMessage onFilterCommand(Message message, String filterValue) {
        SendMessage sendMessage = new SendMessage();
        sendMessage.setChatId(message.getChatId());
        query.setFilter(filterValue);
        sendMessage.setText("changed query filter to " + filterValue);
        logger.info("Current query: " + query.toString());
        return sendMessage;
    }

    private static SendMessage onBackFilterSettingsCommand(Message message) {
        currentState = SETTINGS_STATE;
        SendMessage sendMessage = new SendMessage();
        sendMessage.enableMarkdown(true);
        sendMessage.setChatId(message.getChatId());
        sendMessage.setReplyMarkup(getSettingsKeyboard());
        sendMessage.setText(SETTINGS_MESSAGE);
        return sendMessage;
    }

    @Override
    public String getBotUsername() {
        return BOT_NAME.getValue();
    }

    @Override
    public String getBotToken() {
        return BOT_TOKEN.getValue();
    }

    // ReplyKeyboards

    private static ReplyKeyboardMarkup getMainMenuKeyboard() {
        ReplyKeyboardMarkup replyKeyboardMarkup = new ReplyKeyboardMarkup();
        replyKeyboardMarkup.setSelective(true);
        replyKeyboardMarkup.setResizeKeyboard(true);

        List<KeyboardRow> keyboard = new ArrayList<>();
        KeyboardRow keyboardFirstRow = new KeyboardRow();
        keyboardFirstRow.add(getSettingsCommand());
        keyboardFirstRow.add(getSearchRequestCommand());
        keyboard.add(keyboardFirstRow);
        replyKeyboardMarkup.setKeyboard(keyboard);

        return replyKeyboardMarkup;
    }

    private static ReplyKeyboardMarkup getSettingsKeyboard() {
        ReplyKeyboardMarkup replyKeyboardMarkup = new ReplyKeyboardMarkup();
        replyKeyboardMarkup.setSelective(true);
        replyKeyboardMarkup.setResizeKeyboard(true);

        List<KeyboardRow> keyboard = new ArrayList<>();
        KeyboardRow keyboardFirstRow = new KeyboardRow();
        keyboardFirstRow.add(getFilterCommand());
        keyboardFirstRow.add(getBackCommand());
        keyboard.add(keyboardFirstRow);

        replyKeyboardMarkup.setKeyboard(keyboard);
        return replyKeyboardMarkup;
    }

    private static ReplyKeyboardMarkup getFiltersKeyboard() {
        ReplyKeyboardMarkup replyKeyboardMarkup = new ReplyKeyboardMarkup();
        replyKeyboardMarkup.setSelective(true);
        replyKeyboardMarkup.setResizeKeyboard(true);

        List<KeyboardRow> keyboard = new ArrayList<>();
        KeyboardRow keyboardFirstRow = new KeyboardRow();
        keyboardFirstRow.add(getOneDayFilterCommand());
        keyboardFirstRow.add(getThreeDaysFilterCommand());
        keyboardFirstRow.add(getOneWeekFilterCommand());
        KeyboardRow keyboardSecondRow = new KeyboardRow();
        keyboardSecondRow.add(getOneMonthFilterCommand());
        keyboardSecondRow.add(getBackCommand());

        keyboard.add(keyboardFirstRow);
        keyboard.add(keyboardSecondRow);

        replyKeyboardMarkup.setKeyboard(keyboard);
        return replyKeyboardMarkup;
    }

    // Commands

    private static String getSearchRequestCommand() {
        return "Search request";
    }

    private static String getSettingsCommand() {
        return "Settings";
    }

    private static String getFilterCommand() {
        return "Filter";
    }

    private static String getBackCommand() {
        return "Back";
    }

    private static String getOneDayFilterCommand() {
        return "one day";
    }

    private static String getThreeDaysFilterCommand() {
        return "three days";
    }

    private static String getOneWeekFilterCommand() {
        return "one week";
    }

    private static String getOneMonthFilterCommand() {
        return "one month";
    }

    // Common messages

    private static SendMessage sendMessageDefault(Message message) {
        currentState = MAIN_MENU_STATE;
        SendMessage sendMessage = new SendMessage();
        sendMessage.setChatId(message.getChatId());
        sendMessage.setReplyMarkup(getMainMenuKeyboard());
        sendMessage.setText(MESSAGE_ON_MAIN_MENU);
        logger.info("send message default called");
        return sendMessage;
    }

    private static SendMessage sendChooseOptionMessage(Message message, ReplyKeyboard replyKeyboard) {
        SendMessage sendMessage = new SendMessage();
        sendMessage.setChatId(message.getChatId());
        sendMessage.setReplyMarkup(replyKeyboard);
        sendMessage.setText(CHOOSE_OPTION_MESSAGE);
        return sendMessage;
    }
}
