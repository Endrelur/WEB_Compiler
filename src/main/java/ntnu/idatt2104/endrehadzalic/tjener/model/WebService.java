package ntnu.idatt2104.endrehadzalic.tjener.model;

import org.springframework.stereotype.Service;

import java.util.Optional;

@Service
public class WebService {

    public Optional<String> compileAndRun(String cppSourceCode) {

        try {
            return Docker.executeInDocker(cppSourceCode);
        }
        catch (Exception e) {
            System.err.println("lol");
            return Optional.ofNullable(null);
        }
    }
}
