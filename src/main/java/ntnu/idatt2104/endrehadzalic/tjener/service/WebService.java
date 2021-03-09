package ntnu.idatt2104.endrehadzalic.tjener.service;

import ntnu.idatt2104.endrehadzalic.tjener.model.Docker;
import org.springframework.stereotype.Service;

import java.util.Optional;

@Service
public class WebService {

    private static final long TIMEOUT = 10;  // seconds

    public Optional<String> compileAndRun(String cppSourceCode) {
        Optional<String> result = Docker.executeInDocker(cppSourceCode, TIMEOUT);
        return result.map(s -> s.replace("\n", "<br>"));
    }
}
