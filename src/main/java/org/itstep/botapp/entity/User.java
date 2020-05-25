package org.itstep.botapp.entity;

import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

import javax.persistence.*;
import java.time.LocalDate;
import java.util.Date;

@Getter
@Setter
@ToString
@Entity
@Table(name = "user_table")
public class User {
    @Id@GeneratedValue(strategy = GenerationType.AUTO)
    private int id;
    private String name;
    @Temporal(TemporalType.DATE)
    private Date birthDay;
    private String login;
    private String password;
    private int phoneNumber;
    private Date createDate;
}
