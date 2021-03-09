package ntnu.idatt2104.endrehadzalic.tjener.controllers;

import ntnu.idatt2104.endrehadzalic.tjener.service.WebService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;


@RestController
public class MainController {

    private final WebService webService;
    Logger logger = LoggerFactory.getLogger(MainController.class);

    @Autowired
    public MainController(WebService webService) {
        this.webService = webService;
    }

    @PostMapping("/run")
    public String run(@RequestBody String cppSourceString) {
        logger.info("/run was posted to, initializing code compilation/running");
        String output = webService.compileAndRun(cppSourceString).get();
        logger.info("The execution of the code resulted of a output of:\n " + output);
        return output;
    }
}

