package it.rasxodbot.Service;

import org.springframework.stereotype.Service;
import org.telegram.telegrambots.meta.api.methods.AnswerCallbackQuery;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.methods.updatingmessages.EditMessageCaption;
import org.telegram.telegrambots.meta.api.methods.updatingmessages.EditMessageText;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.InlineKeyboardMarkup;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.ReplyKeyboard;

@Service
public class SendMessageService {


    public SendMessage sendMessage(String message, Long chatId){
        return SendMessage.builder()
                .text(message)
                .chatId(chatId)
                .build();
    }

    public SendMessage sendMessage(String message, Long chatId, ReplyKeyboard replyKeyboard){
        return SendMessage.builder()
                .text(message)
                .chatId(chatId)
                .replyMarkup(replyKeyboard)
                .build();
    }

    public EditMessageText editMessage(String message, Long chatId, Integer messageId,  InlineKeyboardMarkup replyKeyboard){
        return EditMessageText.builder()
                .text(message)
                .chatId(chatId)
                .replyMarkup(replyKeyboard)
                .messageId(messageId)
                .build();
    }

    public EditMessageText editMessage(String message, Long chatId, Integer messageId){
        return EditMessageText.builder()
                .text(message)
                .chatId(chatId)
                .messageId(messageId)
                .build();
    }

    public AnswerCallbackQuery answer(String callBackId, String text){
        return AnswerCallbackQuery.builder()
                .text(text)
                .callbackQueryId(callBackId)
                .showAlert(false)
                .build();
    }
}
