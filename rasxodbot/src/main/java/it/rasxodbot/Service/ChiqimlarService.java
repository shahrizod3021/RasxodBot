package it.rasxodbot.Service;

import it.rasxodbot.Entity.Chiqimlar;
import it.rasxodbot.Entity.DailyChiqimlar;
import it.rasxodbot.Entity.User;
import it.rasxodbot.Repositories.AuthRepository;
import it.rasxodbot.Repositories.ChiqimlarRepository;
import it.rasxodbot.Repositories.DailyChiqimRepository;
import it.rasxodbot.Repositories.KirimRepositrory;
import jakarta.ws.rs.NotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.util.Date;

@Service
@RequiredArgsConstructor
public class ChiqimlarService {

    private final ChiqimlarRepository chiqimlarRepository;
    private final KirimRepositrory kirimRepositrory;
    private final AuthRepository authRepository;
    private final DailyChiqimRepository dailyChiqimRepository;



    public void chiqim(Long chatId, String why){
        User usersByChatId = authRepository.findUsersByChatId(chatId);
        Chiqimlar build = Chiqimlar.builder()
                .name(why)
                .user(usersByChatId)
                .build();
        Chiqimlar save = chiqimlarRepository.save(build);
        usersByChatId.getChiqimlars().add(save);
        authRepository.save(usersByChatId);

    }

    public String AddDailyChiqim(DailyChiqimlar dailyChiqimlar, Long chatId){
        dailyChiqimlar.setUser(authRepository.findUsersByChatId(chatId));
        dailyChiqimRepository.save(dailyChiqimlar);
        return "Saqlandi";
    }

    public Double lastmonthchiqim (Long chatId) {
        LocalDate now = LocalDate.now();

        LocalDate firstDayOfLastMonth =
                now.minusMonths(1).withDayOfMonth(1);

        LocalDate lastDayOfLastMonth =
                now.withDayOfMonth(1).minusDays(1);

        Date startDate = java.sql.Date.valueOf(firstDayOfLastMonth);
        Date endDate = java.sql.Date.valueOf(lastDayOfLastMonth);
        return dailyChiqimRepository.getTotalMiqdorByDateBetween(chatId, startDate, endDate);
    }

    public Double searchingOneChiqim(Long chatId, int year, int month){
        LocalDate startLocalDate =
                LocalDate.of(year, month, 1);

        LocalDate endLocalDate =
                startLocalDate.withDayOfMonth(
                        startLocalDate.lengthOfMonth()
                );

        Date startDate =
                java.sql.Date.valueOf(startLocalDate);

        Date endDate =
                java.sql.Date.valueOf(endLocalDate);
        return dailyChiqimRepository.getTotalMiqdorByDateBetween(chatId, startDate, endDate);
    }



    public Chiqimlar getOneChiqim(Integer id){
        return chiqimlarRepository.findById(id).orElseThrow(NotFoundException::new);
    }
}
