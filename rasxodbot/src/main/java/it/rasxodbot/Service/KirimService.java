package it.rasxodbot.Service;

import it.rasxodbot.Entity.Kirim;
import it.rasxodbot.Entity.User;
import it.rasxodbot.Repositories.AuthRepository;
import it.rasxodbot.Repositories.KirimRepositrory;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.util.Date;
import java.util.List;

@Service
@RequiredArgsConstructor
public class KirimService {

    private final KirimRepositrory kirimRepositrory;
    private final AuthRepository authRepository;

    public void kirim(Long chatId, Kirim kirim) {
        User usersByChatId = authRepository.findUsersByChatId(chatId);
        kirim.setUser(usersByChatId);
        Kirim save = kirimRepositrory.save(kirim);
        usersByChatId.getKirims().add(save);
        double v = usersByChatId.getTotalPay() + kirim.getMiqdor();
        usersByChatId.setTotalPay(v);
        authRepository.save(usersByChatId);
    }

    public Double lastMonthKirim(Long chatId){
        LocalDate now = LocalDate.now();

        LocalDate firstDayOfLastMonth =
                now.minusMonths(1).withDayOfMonth(1);

        LocalDate lastDayOfLastMonth =
                now.withDayOfMonth(1).minusDays(1);

        Date startDate =
                java.sql.Date.valueOf(firstDayOfLastMonth);

        Date endDate =
                java.sql.Date.valueOf(lastDayOfLastMonth);

        return
                kirimRepositrory.getTotalKirimBetweenDates(
                        chatId,
                        startDate,
                        endDate
                );
    }

    public Double searchingResultKirim(Long chatId, int year, int month){
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
        return  kirimRepositrory.getTotalKirimBetweenDates(chatId, startDate, endDate);
    }
    public List<Kirim> getOneMonthsKirim(Long chatId, int year, int month){
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
        return kirimRepositrory.findKirimlarByMonth(chatId, startDate, endDate);
    }
}
