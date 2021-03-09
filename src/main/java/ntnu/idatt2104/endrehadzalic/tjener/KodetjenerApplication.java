package ntnu.idatt2104.endrehadzalic.tjener;

import ntnu.idatt2104.endrehadzalic.tjener.model.Docker;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication
public class KodetjenerApplication  {

	public static void main(String[] args){
		if (Docker.buildImage())
		SpringApplication.run(KodetjenerApplication.class, args);
	}



}
