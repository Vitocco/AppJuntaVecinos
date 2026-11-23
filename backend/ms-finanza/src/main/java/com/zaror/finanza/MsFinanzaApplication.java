package com.zaror.finanza;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.openfeign.EnableFeignClients;

@EnableFeignClients
@SpringBootApplication
public class MsFinanzaApplication {

	public static void main(String[] args) {
		SpringApplication.run(MsFinanzaApplication.class, args);
	}
}
