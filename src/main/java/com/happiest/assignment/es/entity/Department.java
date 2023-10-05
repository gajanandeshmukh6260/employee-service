package com.happiest.assignment.es.entity;

import lombok.*;

import javax.persistence.*;

@Getter
@Setter
@ToString
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Table(name = "department")
public class Department {
    @Id
    @Column(name = "id")
    private Integer id;
    @Column(name = "department_Name")
    private String departmentName;
}

