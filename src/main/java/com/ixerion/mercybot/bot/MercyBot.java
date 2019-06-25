package com.ixerion.mercybot.bot;

import com.ixerion.mercybot.builder.Query;
import com.ixerion.mercybot.parser.ImageParser;
import org.telegram.telegrambots.bots.TelegramLongPollingBot;
import org.telegram.telegrambots.meta.api.methods.send.SendMediaGroup;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.objects.Message;
import org.telegram.telegrambots.meta.api.objects.Update;
import org.telegram.telegrambots.meta.api.objects.media.InputMedia;
import org.telegram.telegrambots.meta.api.objects.media.InputMediaPhoto;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.ReplyKeyboardMarkup;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.buttons.KeyboardRow;
import org.telegram.telegrambots.meta.exceptions.TelegramApiException;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

import static com.ixerion.mercybot.util.Constants.*;

public class MercyBot extends TelegramLongPollingBot {

    private Query query;

    public MercyBot() {
        query = new Query()
                .setFilter(THREE_DAYS_FILTER.getValue())
                .setSearchObject(QUERY.getValue())
                .setAmount(Integer.valueOf(IMAGE_LIMIT.getValue()));
    }

    @Override
    public void onUpdateReceived(Update update) {
        if (update.hasMessage()) {
            Message message = update.getMessage();
            if (message.hasText()) {
                try {
                    handleIncomingMessage(message);
                } catch (TelegramApiException e) {
                    e.printStackTrace();
                }
            }
        }
    }

    private void handleIncomingMessage(Message message) throws TelegramApiException {
        if (message.getText().equals("/start")) {
            SendMessage sendMessageRequest = messageOnMainMenu(message);
            execute(sendMessageRequest);
        } else if (message.getText().equals(getSearchRequestCommand())) {
            SendMediaGroup sendMediaGroup = messageOnSendRequest(message);
            execute(sendMediaGroup);
        } else if (message.getText().equals(getSettingsCommand())) {
            SendMessage sendMessageRequest = onSettingsChosen(message);
            execute(sendMessageRequest);
        }
    }

    private SendMediaGroup messageOnSendRequest(Message message) {
        ImageParser parser = new ImageParser();
        List<String> urls = parser.getImages(query);
        List<InputMedia> media = urls.stream().map(url -> new InputMediaPhoto().setMedia(url)).collect(Collectors.toList());
        SendMediaGroup sendMediaGroup = new SendMediaGroup().setChatId(message.getChatId());
        sendMediaGroup.setMedia(media);

        return sendMediaGroup;
    }

    private static SendMessage messageOnMainMenu(Message message) {
        SendMessage sendMessage = new SendMessage();
        sendMessage.setText("send request or change request settings");
        sendMessage.setChatId(message.getChatId());
        sendMessage.setReplyMarkup(getMainMenuKeyboard());

        return sendMessage;
    }

    private static SendMessage onSettingsChosen(Message message) {
        SendMessage sendMessage = new SendMessage();
        sendMessage.setText("choose filter for your request or go back to main menu");
        sendMessage.setChatId(message.getChatId());
        sendMessage.setReplyMarkup(getSettingsKeyboard());

        return sendMessage;
    }

    private static SendMessage messageOnSettings(Message message) {
        SendMessage sendMessage = null;
        if (message.hasText()) {
            if (message.getText().equals(getFilterCommand())) {
                sendMessage = onFilterCommand(message);
            } else if(message.getText().equals(getBackCommand())) {
                sendMessage = messageOnMainMenu(message);
            }
        }
        return sendMessage;
    }

    private static SendMessage onFilterCommand(Message message) {
        SendMessage sendMessage = new SendMessage();
        sendMessage.setChatId(message.getChatId());
        sendMessage.setText("Filter called");

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
}
