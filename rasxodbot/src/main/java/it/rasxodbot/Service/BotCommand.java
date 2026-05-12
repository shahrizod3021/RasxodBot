package it.rasxodbot.Service;

import it.rasxodbot.Entity.Chiqimlar;
import it.rasxodbot.Entity.Enum.UserState;
import it.rasxodbot.Entity.User;
import it.rasxodbot.Repositories.AuthRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.InlineKeyboardMarkup;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.ReplyKeyboardMarkup;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.buttons.InlineKeyboardButton;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.buttons.KeyboardButton;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.buttons.KeyboardRow;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

@Service
@RequiredArgsConstructor
public class BotCommand {

    private final AuthRepository authRepository;

    public InlineKeyboardMarkup menu(){
        InlineKeyboardMarkup inlineKeyboardMarkup = new InlineKeyboardMarkup();
        List<List<InlineKeyboardButton>> rowsInline = new ArrayList<>();
        List<InlineKeyboardButton> rowInline = new ArrayList<>();
        List<InlineKeyboardButton> rowInline1 = new ArrayList<>();
        List<InlineKeyboardButton> rowInline2 = new ArrayList<>();
        InlineKeyboardButton inlineKeyboardButton = new InlineKeyboardButton();
        InlineKeyboardButton inlineKeyboardButton1 = new InlineKeyboardButton();
        InlineKeyboardButton inlineKeyboardButton2 = new InlineKeyboardButton();
        InlineKeyboardButton inlineKeyboardButton3 = new InlineKeyboardButton();
        InlineKeyboardButton inlineKeyboardButton4 = new InlineKeyboardButton();
        inlineKeyboardButton.setText("Pul kiritish");
        inlineKeyboardButton.setCallbackData("put");
        inlineKeyboardButton1.setText("Harajatlar kiritish");
        inlineKeyboardButton1.setCallbackData("get");
        inlineKeyboardButton2.setText("Kirimlarim");
        inlineKeyboardButton2.setCallbackData("kirim");
        inlineKeyboardButton3.setText("Chiqimlarim");
        inlineKeyboardButton3.setCallbackData("chiqim");
        inlineKeyboardButton4.setText("Account");
        inlineKeyboardButton4.setCallbackData("account");
        rowInline.add(inlineKeyboardButton);
        rowInline.add(inlineKeyboardButton1);
        rowInline1.add(inlineKeyboardButton2);
        rowInline1.add(inlineKeyboardButton3);
        rowInline2.add(inlineKeyboardButton4);
        rowsInline.add(rowInline);
        rowsInline.add(rowInline1);
        rowsInline.add(rowInline2);
        inlineKeyboardMarkup.setKeyboard(rowsInline);
        return inlineKeyboardMarkup;
    }

    public InlineKeyboardMarkup addDailyChiqim(Integer id){
        InlineKeyboardMarkup inlineKeyboardMarkup = new InlineKeyboardMarkup();
        List<List<InlineKeyboardButton>> rowsInline = new ArrayList<>();
        List<InlineKeyboardButton> rowInline = new ArrayList<>();
        List<InlineKeyboardButton> rowInline1 = new ArrayList<>();
        List<InlineKeyboardButton> rowInline2 = new ArrayList<>();
        InlineKeyboardButton inlineKeyboardButton = new InlineKeyboardButton();
        InlineKeyboardButton inlineKeyboardButton1 = new InlineKeyboardButton();
        InlineKeyboardButton inlineKeyboardButton2 = new InlineKeyboardButton();
        inlineKeyboardButton.setText("Harajat qildim");
        inlineKeyboardButton.setCallbackData("daily_" + id);
        inlineKeyboardButton2.setText("Harajat turini o'chirish 🗑️");
        inlineKeyboardButton2.setCallbackData("deleteChiqim_" + id);
        inlineKeyboardButton1.setText("Orqaga qaytish");
        inlineKeyboardButton1.setCallbackData("chiqim");
        rowInline.add(inlineKeyboardButton);
        rowInline1.add(inlineKeyboardButton2);
        rowInline2.add(inlineKeyboardButton1);
        rowsInline.add(rowInline);
        rowsInline.add(rowInline1);
        rowsInline.add(rowInline2);
        inlineKeyboardMarkup.setKeyboard(rowsInline);
        return inlineKeyboardMarkup;
    }

    public InlineKeyboardMarkup LinkToBot() {
        InlineKeyboardMarkup inlineKeyboardMarkup = new InlineKeyboardMarkup();
        List<List<InlineKeyboardButton>> rowsInline = new ArrayList<>();
        List<InlineKeyboardButton> rowInline = new ArrayList<>();
        InlineKeyboardButton button = new InlineKeyboardButton();
        button.setText("Botga o'tish");
        button.setUrl("https://t.me/oylikrasxodlar_bot");
        rowInline.add(button);
        rowsInline.add(rowInline);
        inlineKeyboardMarkup.setKeyboard(rowsInline);
        return inlineKeyboardMarkup;
    }
    public InlineKeyboardMarkup backToMenu(String text){
        InlineKeyboardMarkup inlineKeyboardMarkup = new InlineKeyboardMarkup();
        List<List<InlineKeyboardButton>> rowsInline = new ArrayList<>();
        List<InlineKeyboardButton> rowInline = new ArrayList<>();
        InlineKeyboardButton inlineKeyboardButton = new InlineKeyboardButton();
        inlineKeyboardButton.setText("Orqaga ");
        inlineKeyboardButton.setCallbackData(text);
        rowInline.add(inlineKeyboardButton);
        rowsInline.add(rowInline);
        inlineKeyboardMarkup.setKeyboard(rowsInline);
        return inlineKeyboardMarkup;
    }

    public InlineKeyboardMarkup chiqimlar(Long chatId){
        InlineKeyboardMarkup inlineKeyboardMarkup = new InlineKeyboardMarkup();
        List<List<InlineKeyboardButton>> rows = new ArrayList<>();
        List<InlineKeyboardButton> currentRow = new ArrayList<>();
        User usersByChatId = authRepository.findUsersByChatId(chatId);
        InlineKeyboardButton button2 = new InlineKeyboardButton();
        InlineKeyboardButton button = new InlineKeyboardButton();
        button.setText("Qidirish");
        button.setCallbackData("searchForChiqim");
        button2.setText("Asosiy menu");
        button2.setCallbackData("backToMenu");
        int count = 0;
        for (Chiqimlar chiqimlar : usersByChatId.getChiqimlars()) {
            InlineKeyboardButton inlineKeyboardButton = new InlineKeyboardButton();
            inlineKeyboardButton.setText(chiqimlar.getName());
            inlineKeyboardButton.setCallbackData("expense_" + chiqimlar.getId());
            currentRow.add(inlineKeyboardButton);
            count++;

            if (count % 2 == 0){
                rows.add(currentRow);
                currentRow = new ArrayList<>();
            }
        }
        if (!currentRow.isEmpty()) {
            currentRow.add(button);
            currentRow.add(button2);
            rows.add(currentRow);
        }else {
            currentRow.add(button);
            currentRow.add(button2);
            rows.add(currentRow);
        }
        inlineKeyboardMarkup.setKeyboard(rows);
        return inlineKeyboardMarkup;
    }

   public InlineKeyboardMarkup searchKirimButton(){
       InlineKeyboardMarkup inlineKeyboardMarkup = new InlineKeyboardMarkup();
       List<List<InlineKeyboardButton>> rows = new ArrayList<>();
       List<InlineKeyboardButton> currentRow = new ArrayList<>();
       InlineKeyboardButton inlineKeyboardButton = new InlineKeyboardButton();
       InlineKeyboardButton inlineKeyboardButton1 = new InlineKeyboardButton();
       inlineKeyboardButton.setText("Vaht bilan qidirish");
       inlineKeyboardButton.setCallbackData("searchWithdate");
       inlineKeyboardButton1.setText("Orqaga qaytish");
       inlineKeyboardButton1.setCallbackData("backToMenu");
       currentRow.add(inlineKeyboardButton);
       currentRow.add(inlineKeyboardButton1);
       rows.add(currentRow);
       inlineKeyboardMarkup.setKeyboard(rows);
       return inlineKeyboardMarkup;
   }

   public InlineKeyboardMarkup setNotificationTime(){
       InlineKeyboardMarkup inlineKeyboardMarkup = new InlineKeyboardMarkup();
       List<List<InlineKeyboardButton>> rows = new ArrayList<>();
       List<InlineKeyboardButton> currentRow = new ArrayList<>();
       List<InlineKeyboardButton> currentRow1 = new ArrayList<>();
       InlineKeyboardButton button = new InlineKeyboardButton();
       InlineKeyboardButton button1 = new InlineKeyboardButton();
       button.setText("O'zgartirish");
       button.setCallbackData("setNotification");
       button1.setText("Orqaga");
       button1.setCallbackData("backToMenu");
       currentRow.add(button);
       currentRow1.add(button1);
       rows.add(currentRow);
       rows.add(currentRow1);
       inlineKeyboardMarkup.setKeyboard(rows);
       return inlineKeyboardMarkup;
   }


    public ReplyKeyboardMarkup phoneNumber(){
        ReplyKeyboardMarkup replyKeyboardMarkup = new ReplyKeyboardMarkup();
        List<KeyboardRow> rows = new ArrayList<>();
        KeyboardRow row = new KeyboardRow();
        KeyboardButton keyboardButton = new KeyboardButton();
        keyboardButton.setText("Telefon raqam");
        keyboardButton.setRequestContact(true);
        row.add(keyboardButton);
        rows.add(row);
        replyKeyboardMarkup.setKeyboard(rows);
        replyKeyboardMarkup.setResizeKeyboard(true);
        replyKeyboardMarkup.setOneTimeKeyboard(true);
        return replyKeyboardMarkup;
    }
}
