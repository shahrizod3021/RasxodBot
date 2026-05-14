package it.rasxodbot.Service;

import it.rasxodbot.Entity.Group;
import it.rasxodbot.Repositories.GroupRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;

import java.util.List;

@Service
@RequiredArgsConstructor
public class GroupService {

    private final GroupRepository groupRepository;

    private final BotCommand botCommand;

    public SendMessage saveGroup(Long chatId, String groupName){
        Group build = Group.builder()
                .groupName(groupName)
                .groupId(chatId)
                .build();
        groupRepository.save(build);
        return SendMessage.builder().text("Hammaga salom.👋👋\n\nMen sizning hisob kitoblarni qilishda yordamchingizman😉.\nMen bilan harajatlar va kirimlarni hisoblash juda oson😎💸\n\nBotni ishga tushirish uchun quyidagi tugmani bosing⬇️").chatId(chatId).replyMarkup(botCommand.LinkToBot()).build();
    }

    public List<Group> findAll (){
        return groupRepository.findAll();
    }

    public void setUpNewChatId(Long id, Long newId){
        Group byGroupId = groupRepository.findByGroupId(id);
        byGroupId.setGroupId(newId);
        groupRepository.save(byGroupId);
    }
}
