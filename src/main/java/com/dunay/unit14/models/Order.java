package com.dunay.unit14.models;

import jakarta.persistence.*;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.PastOrPresent;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.Setter;

import java.time.LocalDate;
import java.util.Objects;

@Entity
@Table(name = "orders")
@RequiredArgsConstructor
@Getter
@Setter
public class Order {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private int id;
    @Min(1)
    private int procId;

    @NotBlank
    private String fileName;
    @NotBlank
    private String clientName;

    @PastOrPresent
    @Temporal(TemporalType.DATE)
    private LocalDate date;
    @NotBlank
    private String format;

    @Min(1)
    private int amount;

    public Order(int procId, String fileName, String clientName, LocalDate date, String format, int amount) {
        this.procId = procId;
        this.fileName = fileName;
        this.clientName = clientName;
        this.date = date;
        this.format = format;
        this.amount = amount;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Order order = (Order) o;
        return id == order.id
                && procId == order.procId
                && amount == order.amount
                && Objects.equals(fileName, order.fileName)
                && Objects.equals(clientName, order.clientName)
                && Objects.equals(date, order.date)
                && Objects.equals(format, order.format);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id, procId, fileName, clientName, date, format, amount);
    }
}