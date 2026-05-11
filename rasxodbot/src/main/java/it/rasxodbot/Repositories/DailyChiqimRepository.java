package it.rasxodbot.Repositories;

import it.rasxodbot.Entity.DailyChiqimlar;
import org.hibernate.persister.entity.SingleTableEntityPersister;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.Date;
import java.util.List;

public interface DailyChiqimRepository extends JpaRepository<DailyChiqimlar, Integer> {

    @Query("""
            select COALESCE(sum(d.miqdor), 0)
            from DailyChiqimlar d
            where d.user.chatId = :chatId
                 and EXTRACT(MONTH FROM d.vahti) = EXTRACT(MONTH FROM CURRENT_DATE)
                        and EXTRACT(YEAR FROM d.vahti) = EXTRACT(YEAR FROM CURRENT_DATE)
            """)
    Double getTotalMiqdorByUserId(
            @Param("chatId") Long chatId);

    @Query("""
       select COALESCE(sum(d.miqdor), 0)
       from DailyChiqimlar d
       where d.user.chatId = :chatId
       and d.vahti between :startDate and :endDate
       """)
    Double getTotalMiqdorByDateBetween(
            @Param("chatId") Long chatId,
            @Param("startDate") Date startDate,
            @Param("endDate") Date endDate
    );
    @Query("""
            select COALESCE(sum(d.miqdor), 0)
            from DailyChiqimlar d
            where d.user.chatId = :chatId
            and EXTRACT(YEAR FROM d.vahti) = EXTRACT(YEAR FROM CURRENT_DATE)
            """)
    Double getAllMiqdorByChatId(Long chatId);

    @Query("""
            select d
            from DailyChiqimlar d
            where d.chiqimlar.id = :id
            and EXTRACT(MONTH FROM d.vahti) = EXTRACT(MONTH FROM CURRENT_DATE)
            and EXTRACT(YEAR FROM d.vahti) = EXTRACT(YEAR FROM CURRENT_DATE)
            order by d.vahti desc
            """)
    List<DailyChiqimlar> findAllByChiqimlarId(Integer id);
}
