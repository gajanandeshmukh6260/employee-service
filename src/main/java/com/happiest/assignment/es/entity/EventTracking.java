package com.happiest.assignment.es.entity;

import lombok.*;

import javax.persistence.*;

@Getter
@Setter
@ToString
@Entity
@Table(name = "event_tracking")
public class EventTracking extends Auditable {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id")
    private Integer id;

    @Column(name = "employee_id")
    private Integer employeeId;

    @Column(name = "sns_message_id")
    private String snsMessageId;

    @Column(name = "sqs_message_id")
    private String sqsMessageId;
}
