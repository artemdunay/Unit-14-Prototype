package com.dunay.unit14.controllers;

import com.dunay.unit14.repositories.OrdersRepository;
import lombok.AllArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;

@Controller
@AllArgsConstructor
@RequestMapping("/order")
public class OrdersController {
    private OrdersRepository ordersRepository;

    @GetMapping
    public String getOrdersList(Model model) {
        model.addAttribute("ordersList", ordersRepository.findAll());
        return "orders/list";
    }

    @GetMapping("/{id}")
    public String getOrderInfo(@PathVariable int id, Model model) {
        model.addAttribute("order", ordersRepository.findById(id).orElseThrow());
        return "orders/info";
    }
}
