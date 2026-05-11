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

    public SendMessage addUser(Long chatId, User user) {
        user.setChatId(chatId);
        user.setTotalPay(0.0);
        User save = authRepository.save(user);
        return sendMessageService.sendMessage("Ma'lumotlaringiz saqlab qolindi", chatId);
    }
}
