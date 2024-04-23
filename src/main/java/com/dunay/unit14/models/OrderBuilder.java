package com.dunay.unit14.models;

import lombok.Setter;

import java.time.LocalDate;


@Setter
public class OrderBuilder {

    private int procId;

    private String fileName;

    private String clientName;

    private LocalDate date;

    private String format;

    private int amount;

    public Order getResult() {
        return new Order(procId, fileName, clientName, date, format, amount);
    }
}
