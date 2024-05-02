package com.example.HWSpring5_2;

import com.example.HWSpring5_2.configuration.StorageProperties;
import com.example.HWSpring5_2.service.StorageService;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;

@SpringBootApplication
public class HwSpring52Application {

	public static void main(String[] args) {
		SpringApplication.run(HwSpring52Application.class, args);
	}

	@SpringBootApplication
	@EnableConfigurationProperties(StorageProperties.class)
	public class UploadingFilesApplication {

		public static void main(String[] args) {
			SpringApplication.run(UploadingFilesApplication.class, args);
		}

		@Bean
		CommandLineRunner init(StorageService storageService) {
			return (args) -> {
				storageService.deleteAll();
				storageService.init();
			};
		}
	}

}
