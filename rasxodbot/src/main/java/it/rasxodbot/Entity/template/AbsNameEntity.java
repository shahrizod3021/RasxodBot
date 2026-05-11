package it.rasxodbot.Entity.template;

import jakarta.persistence.*;
import lombok.Data;



@Data
@MappedSuperclass
public abstract class AbsNameEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    @Column(nullable = false, length = 1000, name = "name_uz")
    private String nameUz;


}
