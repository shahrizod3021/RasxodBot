package it.rasxodbot.Entity;

import jakarta.persistence.*;
import lombok.*;

import java.util.Date;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Builder
@Entity
public class DailyChiqimlar {


    @jakarta.persistence.Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer Id;

    @Column(name = "miqdor", nullable = false)
    private Double miqdor;

    @Column(name = "vahti", nullable = false)
    private Date vahti;

    @Column(name = "description")
    private String description;

    @ManyToOne
    private Chiqimlar chiqimlar;

    @ManyToOne
    private User user;
}
