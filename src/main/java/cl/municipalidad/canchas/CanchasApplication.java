package cl.municipalidad.canchas;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.client.discovery.EnableDiscoveryClient;

@SpringBootApplication
@EnableDiscoveryClient

public class CanchasApplication {

	public static void main(String[] args) {
		SpringApplication.run(CanchasApplication.class, args);
	}

}
