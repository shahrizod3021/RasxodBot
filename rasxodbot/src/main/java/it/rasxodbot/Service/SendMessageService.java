package it.rasxodbot.Service;

import org.springframework.stereotype.Service;
import org.telegram.telegrambots.meta.api.methods.AnswerCallbackQuery;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
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

    public AnswerCallbackQuery answer(String callBackId, String text){
        return AnswerCallbackQuery.builder()
                .text(text)
                .callbackQueryId(callBackId)
                .showAlert(false)
                .build();
    }
}
