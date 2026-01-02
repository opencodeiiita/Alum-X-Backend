package com.opencode.alumxbackend.basics.Apooorv012.house.model;

import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "apoorv_member")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class ApoorvHouseModel {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, unique = true)
    private String username;

    @Column(nullable = false)
    private String password;

    @Column(nullable = false, unique = true)
    private String email;

    private String mobileNumber;

    private String streetAddress;

    private String city;
}
