package it.rasxodbot.Entity;

import it.rasxodbot.Entity.template.AbsEntity;
import jakarta.persistence.*;
import lombok.*;

import java.util.List;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Builder
@Entity(name = "users")
public class User extends AbsEntity {


    @Column(name = "chat_id")
    private Long chatId;

    @Column(nullable = false)
    private String name;

    @Column(nullable = false, name = "phone_number")
    private String phoneNumber;

    @Column(name = "password")
    private String password;

    @OneToMany(fetch = FetchType.EAGER, mappedBy = "user")
    private List<Chiqimlar> chiqimlars;

    @OneToMany(fetch = FetchType.EAGER, mappedBy = "user")
    private List<Kirim> kirims;

    @Column(name = "notification_time")
    private String notificationTime;

    private Double totalPay;

}
