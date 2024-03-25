package com.dunay.unit14.models;

import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDate;

@Entity
@Data
@Table
public class Order {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private int id;

    private int procId;

    private String fileName;

    private String clientName;

    @Temporal(TemporalType.DATE)
    private LocalDate date;

    private String format;

    private int amount;
}