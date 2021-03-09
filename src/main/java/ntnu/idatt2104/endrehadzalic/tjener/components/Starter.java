package ntnu.idatt2104.endrehadzalic.tjener.components;

import ntnu.idatt2104.endrehadzalic.tjener.model.Docker;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Component;


@Component
public class Starter {
    Logger logger = LoggerFactory.getLogger(Starter.class);

    @EventListener(ApplicationReadyEvent.class)
    public void startDockerImages() {
        logger.info("Starting Docker image on server");
        Docker.startDockerImages();
    }

}
