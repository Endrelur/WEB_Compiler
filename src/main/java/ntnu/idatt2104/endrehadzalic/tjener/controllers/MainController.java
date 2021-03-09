package ntnu.idatt2104.endrehadzalic.tjener.controllers;

import ntnu.idatt2104.endrehadzalic.tjener.model.WebService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Optional;


@RestController
public class MainController {

    private final WebService webService;

    @Autowired
    public MainController(WebService webService) {
        this.webService = webService;
    }

    @PostMapping("/run")
    public String run(@RequestBody String cppSourceString) {
        System.out.println(cppSourceString);
        return webService.compileAndRun(cppSourceString).get();
    }

}

