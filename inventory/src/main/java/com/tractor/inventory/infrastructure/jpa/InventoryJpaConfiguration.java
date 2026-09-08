package com.tractor.inventory.infrastructure.jpa;

import org.springframework.boot.persistence.autoconfigure.EntityScan;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;

@Configuration
@EnableJpaRepositories(basePackages = "com.tractor.inventory.infrastructure.jpa")
@EntityScan(basePackages = "com.tractor.inventory.infrastructure.jpa")
public class InventoryJpaConfiguration {

}
