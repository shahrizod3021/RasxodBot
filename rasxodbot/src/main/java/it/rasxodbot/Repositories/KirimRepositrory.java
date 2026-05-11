package it.rasxodbot.Repositories;

import it.rasxodbot.Entity.Chiqimlar;
import it.rasxodbot.Entity.Kirim;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.Date;
import java.util.List;

public interface KirimRepositrory extends JpaRepository<Kirim, Integer> {

    @Query("""
       select COALESCE(sum(k.miqdor), 0)
       from Kirim k
       where k.user.chatId = :chatId
       and k.vahti between :startDate and :endDate
       """)
    Double getTotalKirimBetweenDates(
            @Param("chatId") Long chatId,
            @Param("startDate") Date startDate,
            @Param("endDate") Date endDate);

    @Query("""
            select COALESCE(sum(k.miqdor), 0)
            from Kirim k
            where k.user.chatId = :chatId
                 and EXTRACT(MONTH FROM k.vahti) = EXTRACT(MONTH FROM CURRENT_DATE)
                        and EXTRACT(YEAR FROM k.vahti) = EXTRACT(YEAR FROM CURRENT_DATE)
            """)
    Double getTotalMiqdorByUserId(
            @Param("chatId") Long chatId);

    @Query("""
       select k
       from Kirim k
       where k.user.chatId = :chatId
       and k.vahti between :startDate and :endDate
       order by k.vahti desc
       """)
    List<Kirim> findKirimlarByMonth(
            @Param("chatId") Long chatId,
            @Param("startDate") Date startDate,
            @Param("endDate") Date endDate
    );
}
