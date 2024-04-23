package com.dunay.unit14;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.web.servlet.LocaleResolver;
import org.springframework.web.servlet.i18n.SessionLocaleResolver;

import java.nio.file.WatchEvent;
import java.util.Locale;
import java.util.concurrent.ArrayBlockingQueue;

@EnableScheduling
@SpringBootApplication
public class Unit14Application {

    public static void main(String[] args) {
        SpringApplication.run(Unit14Application.class, args);
    }

    @Bean
    public ArrayBlockingQueue<WatchEvent<?>> watchEventsQueue() {
        return new ArrayBlockingQueue<>(10, true);
    }

    @Bean
    public LocaleResolver localeResolver() {
        SessionLocaleResolver slr = new SessionLocaleResolver();
        slr.setDefaultLocale(Locale.ENGLISH);
        return slr;
    }
}
