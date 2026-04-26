package com.stemsep.config;

import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.PropertySource;
import org.springframework.scheduling.annotation.EnableAsync;
import org.springframework.transaction.annotation.EnableTransactionManagement;

@Configuration
@ComponentScan(basePackages = "com.stemsep")
@EnableTransactionManagement
@EnableAsync
@PropertySource(value = "classpath:hibernate.properties", encoding = "UTF-8")
public class AppConfig {
}
