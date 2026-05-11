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
public class Kirim {

    @jakarta.persistence.Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer Id;

    @Column(name = "kirim_nomi")
    private String name;

    @Column(name = "miqdor")
    private Double miqdor;

    @Column(name = "vahti")
    private Date vahti;

    @ManyToOne
    private User user;
}
