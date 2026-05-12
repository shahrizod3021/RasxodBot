package it.rasxodbot.Bot;

import it.rasxodbot.Config.BotConfig;
import it.rasxodbot.Entity.Chiqimlar;
import it.rasxodbot.Entity.DailyChiqimlar;
import it.rasxodbot.Entity.Enum.UserState;
import it.rasxodbot.Entity.Kirim;
import it.rasxodbot.Entity.User;
import it.rasxodbot.Repo.Notification;
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
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.telegram.telegrambots.bots.TelegramLongPollingBot;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.objects.CallbackQuery;
import org.telegram.telegrambots.meta.api.objects.Message;
import org.telegram.telegrambots.meta.api.objects.Update;
import org.telegram.telegrambots.meta.exceptions.TelegramApiRequestException;

import java.text.DecimalFormat;
import java.text.DecimalFormatSymbols;
import java.time.LocalTime;
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
    private final Map<Long, Notification> notificationMap = new HashMap<>();

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
                    if (!authRepository.existsUserByChatId(chatId)) {
                        userMap.put(chatId, new User());
                        userState.put(chatId, UserState.WAITING_NAME);
                        SendMessage sendMessage = SendMessage.builder().chatId(chatId).text("Salom ismingizni kiriting").build();
                        execute(sendMessage);
                    } else {
                        userState.put(chatId, UserState.NONE);
                        execute(sendMessage.sendMessage("Salom, bo'limni tanlang", chatId, botCommand.menu()));
                    }
                }
                if (text.equals("/kirim@oylikrasxodlar_bot")){
                    Long id1 = update.getMessage().getFrom().getId();

                    if (authRepository.existsUserByChatId(id1)){
                        User usersByChatId = authRepository.findUsersByChatId(id1);
                        List<Kirim> kirims = usersByChatId.getKirims();
                        Double v = kirimService.lastMonthKirim(id1);
                        Double totalMiqdorByUserId = kirimRepositrory.getTotalMiqdorByUserId(chatId);
                        StringBuilder textTo = new StringBuilder();
                        if (kirims.isEmpty()) {
                            textTo.append("Sizda kirimlar mavjud emas😔");
                        } else {
                            textTo.append("📈Ushbu oydagi umumiy kirimlar: ").append(FNumberToText(totalMiqdorByUserId)).append(" so'm\n\n");
                            textTo.append("📈O'tgan oydagi umumiy kirimlar: ").append(FNumberToText(v)).append(" so'm");
                        }
                        execute(sendMessage.sendMessage(textTo.toString(), chatId, message.getMessageId(), botCommand.LinkToBot()));
                    }else {
                        execute(sendMessage.sendMessage("Siz hali botdan ro'yhatdan o'tmagansiz‼️‼️", chatId, message.getMessageId(), botCommand.LinkToBot()));
                    }
                }
                if (text.equals("/chiqim@oylikrasxodlar_bot")) {
                    Long id1 = update.getMessage().getFrom().getId();

                    if (!authRepository.existsUserByChatId(id1)) {
                        execute(sendMessage.sendMessage("Siz hali botdan ro'yhatdan o'tmagansiz‼️‼️", chatId, message.getMessageId(), botCommand.LinkToBot()));
                    }else {
                        List<Chiqimlar> chiqimlarByUserChatId = chiqimlarRepository.getChiqimlarByUserChatId(id1);
                        if (!chiqimlarByUserChatId.isEmpty()) {
                            Double totalMiqdorByUserId = dailyChiqimRepository.getTotalMiqdorByUserId(chatId);
                            Double lastmonthchiqim = chiqimlarService.lastmonthchiqim(chatId);
                            Double allMiqdorByChatId = dailyChiqimRepository.getAllMiqdorByChatId(chatId);
                            execute(sendMessage.sendMessage("📉Umumiy harajat bu oydagi: " + FNumberToText(totalMiqdorByUserId) + " so'm" + "\n\n📉O'tgan oydagi umumiy harajatlar: " + FNumberToText(lastmonthchiqim) + " so'm" + "\n\nUmumiy harajatlar: " + FNumberToText(allMiqdorByChatId) + " so'm", chatId, message.getMessageId(), botCommand.LinkToBot()));
                        } else {
                            execute(sendMessage.sendMessage("Sizda harajatlar yo'q", chatId, message.getMessageId(), botCommand.LinkToBot()));
                        }
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
                        userState.put(chatId, UserState.WAITING_DAILY_DESCRIPTION);
                        execute(sendMessage.sendMessage("Harajat haqida qisqacha sabab yozing", chatId));
                    } catch (NumberFormatException e) {
                        execute(sendMessage.sendMessage("Miqdor faqat raqamlarda kiritlsin, orasida bo'sh joylar bo'lmasin", chatId));
                    }
                }
                if (state == UserState.WAITING_DAILY_DESCRIPTION) {
                    DailyChiqimlar dailyChiqimlar = dailyChiqimlarMap.get(chatId);
                    dailyChiqimlar.setVahti(new Date());
                    dailyChiqimlar.setDescription(text);
                    dailyChiqimlar.setChiqimlar(chiqimlarRepository.findById(expense.get(chatId)).orElseThrow());
                    String s = chiqimlarService.AddDailyChiqim(dailyChiqimlar, chatId);
                    execute(sendMessage.sendMessage(s, chatId, botCommand.backToMenu("expense_" + expense.get(chatId))));
                    expense.remove(chatId);
                    userState.remove(chatId);
                    dailyChiqimlarMap.remove(chatId);
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
                if (state == UserState.WAITING_SEARCH_CHIQIM) {
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
                if (state == UserState.WAITING_NOTIFICATION_TIME) {
                    if (!text.matches("^([01]\\d|2[0-3]):([0-5]\\d)$")) {
                        execute(sendMessage.sendMessage("""
                                ❌ Noto‘g‘ri format!

                                Misol:
                                08:30
                                """, chatId));
                    }else {
                        Notification notification = notificationMap.get(chatId);
                        notification.setNotificationTime(text);
                        SendMessage sendMessage1 = userService.setNotification(text, chatId);
                        execute(sendMessage1);
                        userState.remove(chatId);
                        notificationMap.remove(chatId);
                    }
                }
                if (state == UserState.WAITING_NAME) {
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
                            "\n\n" +
                            "🔗 Meni guruhlarga qo'shing men u yeda ham ishlayman 😉\n\n" +
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
                List<Chiqimlar> chiqimlarByUserChatId = chiqimlarRepository.getChiqimlarByUserChatId(chatId);
                if (!chiqimlarByUserChatId.isEmpty()) {
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
                        text.append("\n\n▪️ ")
                                .append("Harajat qilingan vahti: ")
                                .append(expense.getVahti().toString(), 0, 19)
                                .append("\n")
                                .append("       Harajat summasi: ")
                                .append(FNumberToText(expense.getMiqdor()))
                                .append(" so‘m\n")
                                .append("       Harajat sababi: ")
                                .append(expense.getDescription());
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
                execute(sendMessage.editMessage("Salom 👋" + usersByChatId.getName() + "\n " +
                                "\nAccount ma'lumotlaringiz\n" + "Ism🧾: " + usersByChatId.getName() +
                                "\nTelefon raqam📱: " + usersByChatId.getPhoneNumber() +
                                "\nShu paytgacha daromad💸: " + FNumberToText(usersByChatId.getTotalPay()) +
                                "\nBildirishnoma yuborish vahti: " + usersByChatId.getNotificationTime()
                        , chatId, messageId, botCommand.setNotificationTime()));
            } else if (data.equals("searchForChiqim")) {
                execute(sendMessage.sendMessage("Yil va oyni kiriting va men sizga siz xoxlagan vahtdagi harajatlaringizni chiqarib beraman 🗓️\n\nMisol uchun: 2026-12", chatId));
                userState.put(chatId, UserState.WAITING_SEARCH_CHIQIM);
                searchForChiqim.put(chatId, new Search());
            } else if (data.equals("setNotification")) {
                execute(sendMessage.editMessage("⏰ Bildirishnoma vaqtini kiriting\n" +
                        "\n" +
                        "Misol:\n" +
                        "08:30", chatId, messageId));
                userState.put(chatId, UserState.WAITING_NOTIFICATION_TIME);
                notificationMap.put(chatId, new Notification());
            }else if (data.startsWith("deleteChiqim_")){
                Integer chiqimId = Integer.parseInt(data.replace("deleteChiqim_", ""));
                chiqimlarService.deleteDailyChqimlar(chiqimId);
                execute(sendMessage.editMessage("Muvaffaqiyatli o'chirildi", chatId, messageId, botCommand.backToMenu("chiqim")));
            }
            else if (data.equals("backToMenu")) {
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

    @SneakyThrows
    @Scheduled(cron = "0 * * * * *", zone = "Asia/Tashkent")
    public void checkNotification() {
        LocalTime localTime = LocalTime.now();
        String format = String.format(
                "%02d:%02d",
                localTime.getHour(),
                localTime.getMinute()
        );
        for (User user : authRepository.findAll()) {
            if (format.equals(user.getNotificationTime())) {
                execute(sendMessage.sendMessage("👋Salom " + user.getName() + "\n\nPullaringizni hisobini olish vahti keldi😎💸", user.getChatId(), botCommand.menu()));
            }
        }
    }
}
