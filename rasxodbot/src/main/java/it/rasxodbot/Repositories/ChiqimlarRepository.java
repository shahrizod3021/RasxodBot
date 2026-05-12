package it.rasxodbot.Repositories;

import it.rasxodbot.Entity.Chiqimlar;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.List;

public interface ChiqimlarRepository extends JpaRepository<Chiqimlar, Integer> {

    @Query("""
            select ch
            from Chiqimlar ch
            where ch.user.chatId = :chatId
            """)
    List<Chiqimlar> getChiqimlarByUserChatId(Long chatId);
}
