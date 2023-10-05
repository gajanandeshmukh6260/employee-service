package com.happiest.assignment.es.entity;

import lombok.*;
import javax.persistence.*;

import java.util.List;


@Getter
@Setter
@ToString
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Table(name = "employee")
public class Employee {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id")
    private Integer id;


    @Column(name = "employee_Name")
    private String employeeName;


    @Column(name = "mail_Id")
    private String mailId;

    @Column(name = "mobile_Number")
    private String mobileNumber;

    @OneToMany(targetEntity = Department.class, cascade = CascadeType.ALL)
    @JoinColumn(name = "ed_fk", referencedColumnName = "id")
    private List<Department> department;

    @OneToMany(targetEntity = com.happiest.assignment.es.entity.Address.class, cascade = CascadeType.ALL)
    @JoinColumn(name = "ea_fk", referencedColumnName = "id")
    private List<Address> address;

}
