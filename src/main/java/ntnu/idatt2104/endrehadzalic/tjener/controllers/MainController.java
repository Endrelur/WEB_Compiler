package ntnu.idatt2104.endrehadzalic.tjener.controllers;

import ntnu.idatt2104.endrehadzalic.tjener.service.WebService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.*;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

import java.util.Optional;


@RestController
public class MainController {

    private final WebService webService;
    Logger logger = LoggerFactory.getLogger(MainController.class);

    @Autowired
    public MainController(WebService webService) {
        this.webService = webService;
    }

    @PostMapping("/run")
    public ResponseEntity<String> run(@RequestBody(required = false) String cppSourceString) {
        logger.info("POST request to /run");

        if (cppSourceString == null) {
            logger.info("Request body == null");
            return ResponseEntity.badRequest().build();
        }

        Optional<String> result = webService.compileAndRun(cppSourceString);

        if (result.isPresent()) {
            String output = result.get();
            logger.info("The execution of the code resulted of a output of:\n " + output);
            return ResponseEntity.ok(output);
        }
        else {
            logger.warn("Internal Server Error");
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }
}

