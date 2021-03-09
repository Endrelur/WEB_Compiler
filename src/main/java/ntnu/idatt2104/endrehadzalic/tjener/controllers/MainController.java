package ntnu.idatt2104.endrehadzalic.tjener.controllers;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.PostMapping;

@Controller
public class MainController {

    @PostMapping("/run")
    public String run(/* @RequestBody String cppSourceString */) {
        return null;
    }

}

