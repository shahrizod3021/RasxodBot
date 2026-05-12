package it.rasxodbot.Service;

import it.rasxodbot.Entity.User;
import it.rasxodbot.Repositories.AuthRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;

@Service
@RequiredArgsConstructor
public class UserService {

    private final AuthRepository authRepository;
    private final SendMessageService sendMessageService;
    private final BotCommand botCommand;

    public SendMessage addUser(Long chatId, User user) {
        user.setChatId(chatId);
        user.setTotalPay(0.0);
        user.setNotificationTime("09:00");
        authRepository.save(user);
        return sendMessageService.sendMessage("Ma'lumotlaringiz saqlab qolindi", chatId);
    }

    public SendMessage setNotification(String time, Long chatId){
        User usersByChatId = authRepository.findUsersByChatId(chatId);
        usersByChatId.setNotificationTime(time);
        authRepository.save(usersByChatId);
        return sendMessageService.sendMessage("Bildirishnoma yuborish vahti muvaffaqiyatli o'zgartirildi🎉", chatId, botCommand.backToMenu("account"));
    }

}
