package it.rasxodbot.Repositories;

import it.rasxodbot.Entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.web.bind.annotation.CrossOrigin;

import java.util.UUID;

@CrossOrigin
public interface AuthRepository extends JpaRepository<User, UUID> {

    boolean existsUserByChatId(Long id);

    User findUsersByChatId(Long chatId);

    User findUsersByPhoneNumber(String phoneNumber);


}
