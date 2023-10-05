package com.happiest.assignment.es.entity;

import lombok.*;

import javax.persistence.*;

@Getter
@Setter
@ToString
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Table(name = "address")
public class Address {

    @Id
    @Column(name = "id")
    private Integer id;
    @Column(name = "address_details")
    private String addressDetails;
}
