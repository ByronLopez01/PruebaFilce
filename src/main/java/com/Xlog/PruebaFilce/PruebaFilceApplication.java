package com.Xlog.PruebaFilce;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.ComponentScan;

@SpringBootApplication
@ComponentScan(basePackages = {"com.Xlog"})
public class PruebaFilceApplication {

	public static void main(String[] args) {
		SpringApplication.run(PruebaFilceApplication.class, args);
	}

}
