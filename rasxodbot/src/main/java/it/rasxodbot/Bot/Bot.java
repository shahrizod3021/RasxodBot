package it.rasxodbot.Bot;

import it.rasxodbot.Config.BotConfig;
import it.rasxodbot.Entity.Chiqimlar;
import it.rasxodbot.Entity.DailyChiqimlar;
import it.rasxodbot.Entity.Enum.UserState;
import it.rasxodbot.Entity.Kirim;
import it.rasxodbot.Entity.User;
import it.rasxodbot.Repo.Search;
import it.rasxodbot.Repositories.AuthRepository;
import it.rasxodbot.Repositories.ChiqimlarRepository;
import it.rasxodbot.Repositories.DailyChiqimRepository;
import it.rasxodbot.Repositories.KirimRepositrory;
import it.rasxodbot.Service.*;
import lombok.RequiredArgsConstructor;
import lombok.SneakyThrows;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;
import org.telegram.telegrambots.bots.TelegramLongPollingBot;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.methods.updatingmessages.DeleteMessage;
import org.telegram.telegrambots.meta.api.objects.CallbackQuery;
import org.telegram.telegrambots.meta.api.objects.Message;
import org.telegram.telegrambots.meta.api.objects.Update;
import org.telegram.telegrambots.meta.exceptions.TelegramApiRequestException;

import java.text.DecimalFormat;
import java.text.DecimalFormatSymbols;
import java.util.*;
import java.util.List;

@Component
@RequiredArgsConstructor
public class Bot extends TelegramLongPollingBot {

    private final BotConfig botConfig;
    private final SendMessageService sendMessage;
    private final AuthRepository authRepository;
    private final DailyChiqimRepository dailyChiqimRepository;
    private final KirimRepositrory kirimRepositrory;
    private final KirimService kirimService;
    private final ChiqimlarRepository chiqimlarRepository;
    private static final Logger LOGGER = LoggerFactory.getLogger(Bot.class);

    private final BotCommand botCommand;

    private final ChiqimlarService chiqimlarService;

    private final UserService userService;

    private final Map<Long, UserState> userState = new HashMap<>();

    private final Map<Long, Kirim> userKirimlari = new HashMap<>();
    private final Map<Long, Chiqimlar> userChiqimlari = new HashMap<>();

    private final Map<Long, DailyChiqimlar> dailyChiqimlarMap = new HashMap<>();

    private final Map<Long, User> userMap = new HashMap<>();

    Map<Long, Search> searchForKirim = new HashMap<>();

    Map<Long, Search> searchForChiqim = new HashMap<>();
    Map<Long, String> callBackId = new HashMap<>();

    Map<Long, Integer> expense = new HashMap<>();

    @Override
    public void clearWebhook() throws TelegramApiRequestException {

    }

    @Override
    public String getBotUsername() {
        return botConfig.getName();
    }

    @Override
    public String getBotToken() {
        return botConfig.getToken();
    }

    @Override
    @SneakyThrows
    public void onUpdateReceived(Update update) {
        CallbackQuery callbackQuery = update.getCallbackQuery();
        Message message = update.getMessage();
        if (update.hasMessage()) {
            String text = message.getText();
            Long chatId = message.getChatId();
//            LOGGER.info("user chat id  ▶️" + chatId);
            UserState state =
                    userState.getOrDefault(
                            chatId,
                            UserState.NONE
                    );
            if (message.hasText()) {
                if (text.equals("/start")) {
                    if (!authRepository.existsUserByChatId(chatId)){
                        userMap.put(chatId, new User());
                        userState.put( chatId, UserState.WAITING_NAME);
                        SendMessage sendMessage = SendMessage.builder().chatId(chatId).text("Salom ismingizni kiriting").build();
                        execute(sendMessage);
                    }else{
                         execute(sendMessage.sendMessage("Salom, bo'limni tanlang", chatId, botCommand.menu()));
                    }
                }

                if (state == UserState.WAITING_KIRIM_MIQDOR) {
                    try {
                        double v = Double.parseDouble(text);
                        Kirim kirim = userKirimlari.get(chatId);
                        kirim.setMiqdor(v);
                        execute(sendMessage.sendMessage("Nima sabab tufayli pul kiritayapsiz", chatId));
                        userState.put(chatId, UserState.WAITING_KIRIM_SABAB);
                    } catch (NumberFormatException e) {
                        execute(sendMessage.sendMessage("Miqdor faqat raqamlarda kiritlsin, orasida bo'sh joylar bo'lmasin", chatId));
                    }
                }
                if (state == UserState.WAITING_KIRIM_SABAB) {
                    Kirim kirim = userKirimlari.get(chatId);
                    kirim.setName(text);
                    kirim.setVahti(new Date());
                    kirimService.kirim(chatId, kirim);
                    execute(sendMessage.sendMessage("Pul kiritldi", chatId, botCommand.backToMenu("backToMenu")));
                    userState.remove(chatId);
                    userKirimlari.remove(chatId);
                }
                if (state == UserState.WAITING_CHIQIM_NOMI) {
                    Chiqimlar chiqimlar = userChiqimlari.get(chatId);
                    chiqimlar.setName(text);
                    chiqimlarService.chiqim(chatId, chiqimlar.getName());
                    execute(sendMessage.sendMessage("Harajat turi saqlandi", chatId, botCommand.backToMenu("backToMenu")));
                    userState.remove(chatId);
                    userChiqimlari.remove(chatId);
                }
                if (state == UserState.WAITING_DAILY_MIQDOR) {
                    try {
                        double v = Double.parseDouble(text);
                        DailyChiqimlar dailyChiqimlar = dailyChiqimlarMap.get(chatId);
                        dailyChiqimlar.setMiqdor(v);
                        dailyChiqimlar.setVahti(new Date());
                        dailyChiqimlar.setChiqimlar(chiqimlarRepository.findById(expense.get(chatId)).orElseThrow());
                        String s = chiqimlarService.AddDailyChiqim(dailyChiqimlar, chatId);
                        execute(sendMessage.sendMessage(s, chatId, botCommand.backToMenu("expense_" + expense.get(chatId))));
                        userState.remove(chatId);
                        dailyChiqimlarMap.remove(chatId);
                        expense.remove(chatId);
                    } catch (NumberFormatException e) {
                        execute(sendMessage.sendMessage("Miqdor faqat raqamlarda kiritlsin, orasida bo'sh joylar bo'lmasin", chatId));
                    }
                }
                if (state == UserState.WAITING_SEARCH) {
                    Search search = searchForKirim.get(chatId);
                    search.setSearch(text);
                    if (!search.getSearch().matches("\\d{4}-\\d{2}")) {
                        execute(sendMessage.sendMessage("Siz kiritgan format noto'g'ri\n❌Siz kiritgan format: " + text + "\n✅To'gri format: 2026-12", chatId));
                    } else {
                        String[] parts = search.getSearch().split("-");
                        int year = Integer.parseInt(parts[0]);
                        int month = Integer.parseInt(parts[1]);
                        List<Kirim> OneMonthsKirim = kirimService.getOneMonthsKirim(chatId, year, month);
                        Double v = kirimService.searchingResultKirim(chatId, year, month);
                        StringBuilder builder = new StringBuilder();
                        if (OneMonthsKirim.isEmpty()) {
                            execute(sendMessage.sendMessage("Ushbu oyda kirimlar bo'lmagan ❌", chatId));
                        } else {
                            for (Kirim kirim : OneMonthsKirim) {
                                builder.append("▪️ Miqdor: ")
                                        .append(FNumberToText(kirim.getMiqdor()))
                                        .append(" so'm\n");

                                builder.append("   Sabab: ")
                                        .append(kirim.getName())
                                        .append("\n");

                                builder.append("   Vahti: ")
                                        .append(kirim.getVahti().toString(), 0, 19)
                                        .append("\n\n");
                            }
                            builder.append("💰 Ushbu oydagi umumiy kirimlar summasi: ")
                                    .append(FNumberToText(v))
                                    .append(" so'm");
                            execute(sendMessage.sendMessage(builder.toString(), chatId, botCommand.backToMenu("kirim")));
                            userState.remove(chatId);
                            searchForKirim.remove(chatId);
                        }
                    }
                }
                if (state == UserState.WAITING_SEARCH_CHIQIM){
                    Search search = searchForChiqim.get(chatId);
                    search.setSearch(text);
                    if (!search.getSearch().matches("\\d{4}-\\d{2}")) {
                        execute(sendMessage.sendMessage("Siz kiritgan format noto'g'ri\n❌Siz kiritgan format: " + text + "\n✅To'gri format: 2026-12", chatId));
                    } else {
                        String[] parts = search.getSearch().split("-");
                        int year = Integer.parseInt(parts[0]);
                        int month = Integer.parseInt(parts[1]);
                        Double v = chiqimlarService.searchingOneChiqim(chatId, year, month);
                        StringBuilder builder = new StringBuilder();
                        if (v == null) {
                            execute(sendMessage.sendMessage("Ushbu oyda harajatlar bo'lmagan ❌", chatId));
                        } else {
                            builder.append("💰 Ushbu oydagi umumiy harajatlaringiz summasi: ")
                                    .append(FNumberToText(v))
                                    .append(" so'm");
                            execute(sendMessage.sendMessage(builder.toString(), chatId, botCommand.backToMenu("chiqim")));
                            userState.remove(chatId);
                            searchForChiqim.remove(chatId);
                        }
                    }
                }
                if (state == UserState.WAITING_NAME){
                    userState.put(chatId, UserState.WAITING_PHONE_NUMBER);
                    User user = userMap.get(chatId);
                    user.setName(text);
                    SendMessage endiTelefonRaqamniKiriting = sendMessage.sendMessage("Endi telefon raqamni kiriting", chatId, botCommand.phoneNumber());
                    execute(endiTelefonRaqamniKiriting);
                }
            } else if (message.hasContact()) {
                if (state == UserState.WAITING_PHONE_NUMBER) {
                    User user = userMap.get(chatId);
                    user.setPhoneNumber(message.getContact().getPhoneNumber());
                    execute(userService.addUser(chatId, user));
                    execute(sendMessage.sendMessage("Xush kelibsiz \uD83D\uDE0A\n" +
                            "\n" +
                            "Bot sizga kirim va chiqimlaringizni nazorat qilishda yordam beradi \uD83D\uDCCB\uD83D\uDCB5\n" +
                            "\n" +
                            "➕ Pul kiritish — daromad qo‘shish\n" +
                            "➖ Harajatlar qo‘shish — chiqimlar turlarini qo‘shish\n" +
                            "\uD83D\uDCC8 Kirimlarim — kirimlar tarixini ko‘rish\n" +
                            "\uD83D\uDCC9 Chiqimlarim — barcha harajatlarni ko‘rish\n" +
                            "👤 Account - Hisob raqamni tekshirish\n" +
                            "\n" +
                            "Bo‘limlardan birini tanlang \uD83D\uDC47\n", chatId, botCommand.menu()));
                    userMap.remove(chatId);
                    userState.remove(chatId);
                }

            }
        } else if (update.hasCallbackQuery()) {
            String data = callbackQuery.getData();
            String id = callbackQuery.getId();
            Long chatId = callbackQuery.getMessage().getChatId();
            Integer messageId = callbackQuery.getMessage().getMessageId();
            if (data.equals("put")) {
                userKirimlari.put(chatId, new Kirim());
                userState.put(chatId, UserState.WAITING_KIRIM_MIQDOR);
                execute(sendMessage.editMessage("Qancha pul kiritasiz.\nMisol uchun: 100000", chatId, messageId));
                callBackId.put(chatId, id);
            } else if (data.equals("get")) {
                userChiqimlari.put(chatId, new Chiqimlar());
                userState.put(chatId, UserState.WAITING_CHIQIM_NOMI);
                execute(sendMessage.editMessage("Harajat nomini kiriting", chatId, messageId));
            } else if (data.equals("chiqim")) {
                User usersByChatId = authRepository.findUsersByChatId(chatId);
                if (!usersByChatId.getChiqimlars().isEmpty()) {
                    Double totalMiqdorByUserId = dailyChiqimRepository.getTotalMiqdorByUserId(chatId);
                    Double lastmonthchiqim = chiqimlarService.lastmonthchiqim(chatId);
                    Double allMiqdorByChatId = dailyChiqimRepository.getAllMiqdorByChatId(chatId);
                    execute(sendMessage.editMessage("📉Umumiy harajat bu oydagi: " + FNumberToText(totalMiqdorByUserId) + " so'm" + "\n\n📉O'tgan oydagi umumiy harajatlar: " + FNumberToText(lastmonthchiqim) + " so'm" + "\n\nUmumiy harajatlar: " + FNumberToText(allMiqdorByChatId) + " so'm" + "\n\nHarajatlar turlari⏬", chatId, messageId, botCommand.chiqimlar(chatId)));
                } else {
                    execute(sendMessage.editMessage("Sizda harajatlar yo'q", chatId, messageId, botCommand.backToMenu("backToMenu")));
                }
            } else if (data.startsWith("expense_")) {
                Integer expenseId = Integer.parseInt(
                        data.replace("expense_", "")
                );
                Chiqimlar oneChiqim = chiqimlarService.getOneChiqim(expenseId);
                List<DailyChiqimlar> daily = dailyChiqimRepository.findAllByChiqimlarId(expenseId);
                StringBuilder text = new StringBuilder();
                text.append("📂 ")
                        .append(oneChiqim.getName())
                        .append(" bo‘limidagi harajatlar:\n\nEslatma‼️‼️: faqatgina shu oydagi harajatlarni ro'yhatini ko'ra olasiz 🗓️\n\n");
                if (daily.isEmpty()) {
                    text.append("Harajatlar mavjud emas.");
                } else {
                    for (DailyChiqimlar expense : daily) {
                        text.append("▪️ ")
                                .append("Harajat qilingan vahti: ")
                                .append(expense.getVahti().toString(), 0, 19)
                                .append("\n")
                                .append("   Harajat summasi: ")
                                .append(FNumberToText(expense.getMiqdor()))
                                .append(" so‘m\n\n");
                    }
                }
                execute(sendMessage.editMessage(text.toString(), chatId, messageId, botCommand.addDailyChiqim(expenseId)));
            } else if (data.startsWith("daily_")) {
                dailyChiqimlarMap.put(chatId, new DailyChiqimlar());
                userState.put(chatId, UserState.WAITING_DAILY_MIQDOR);
                Integer expenseId = Integer.parseInt(
                        data.replace("daily_", "")
                );
                expense.put(chatId, expenseId);
                execute(sendMessage.editMessage("Miqdorni kiriting", chatId, messageId));
            } else if (data.equals("kirim")) {
                User usersByChatId = authRepository.findUsersByChatId(chatId);
                List<Kirim> kirims = usersByChatId.getKirims();
                Double v = kirimService.lastMonthKirim(chatId);
                Double totalMiqdorByUserId = kirimRepositrory.getTotalMiqdorByUserId(chatId);
                StringBuilder text = new StringBuilder();
                if (kirims.isEmpty()) {
                    text.append("Sizda kirimlar mavjud emas😔");
                } else {
                    text.append("📈Ushbu oydagi umumiy kirimlar: ").append(FNumberToText(totalMiqdorByUserId)).append(" so'm\n\n");
                    text.append("📈O'tgan oydagi umumiy kirimlar: ").append(FNumberToText(v)).append(" so'm");
                }
                execute(sendMessage.editMessage(text.toString(), chatId, messageId, botCommand.searchKirimButton()));
            } else if (data.equals("searchWithdate")) {
                userState.put(chatId, UserState.WAITING_SEARCH);
                searchForKirim.put(chatId, new Search());
                execute(sendMessage.sendMessage("Yil va oyni kiriting 🗓️\n\nMisol uchun: 2026-12", chatId));
            } else if (data.equals("account")) {
                User usersByChatId = authRepository.findUsersByChatId(chatId);
                execute(sendMessage.editMessage("Salom 👋" + usersByChatId.getName() +"\n " +
                        "\nAccount ma'lumotlaringiz\n" + "Ism🧾: " + usersByChatId.getName() +
                        "\nTelefon raqam📱: " + usersByChatId.getPhoneNumber() +
                        "\nShu paytgacha daromad💸: " + FNumberToText(usersByChatId.getTotalPay()), chatId, messageId, botCommand.backToMenu("backToMenu")));
            }else if (data.equals("searchForChiqim")){
                userState.put(chatId, UserState.WAITING_SEARCH_CHIQIM);
                searchForChiqim.put(chatId, new Search());
                execute(sendMessage.sendMessage("Yil va oyni kiriting va men sizga siz xoxlagan vahtdagi harajatlaringizni chiqarib beraman 🗓️\n\nMisol uchun: 2026-12", chatId));
            }
            else if (data.equals("backToMenu")){
                execute(sendMessage.editMessage("Asosiy bo'lim", chatId, messageId, botCommand.menu()));
            }
        }
    }

    public String FNumberToText(Double miqdor) {
        DecimalFormatSymbols symbols = new DecimalFormatSymbols();
        symbols.setGroupingSeparator('.');

        DecimalFormat format =
                new DecimalFormat("#,###", symbols);
        return format.format(miqdor);
    }
}
