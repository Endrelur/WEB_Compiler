package ntnu.idatt2104.endrehadzalic.tjener.model;

import org.apache.commons.lang3.SystemUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Optional;

/**
 * A class that is meant to take in runnable c++ code, execute it in a docker container and return the result.
 * this is for safeguarding the host-os against possible malicious software.
 * Created by Torstein Øvstedal & Endré Hadzalic
 */
public class Docker {

    public static final int WINDOWS = 0;
    public static final int LINUX = 1;
    private static final Logger logger = LoggerFactory.getLogger(Docker.class);

    /**
     * Checks the servers host-OS and returns a corresponding value.
     *
     * @return -1 if the identified OS is unsupported
     */
    private static int checkOS() {
        if (SystemUtils.IS_OS_WINDOWS)
            return WINDOWS;
        else if (SystemUtils.IS_OS_LINUX)
            return LINUX;
        else
            return -1;
    }

    /**
     * Used for generating a docker launch command corresponding to the server host OS.
     *
     * @param cppPath the path to the c++ file to be run
     * @return an array of OS-specific system commands.
     */
    private static String[] getLaunchCommand(String cppPath) {
        int os = checkOS();

        String dockerCommand = "docker run -i --rm oving-image < " + cppPath;

        if (os == WINDOWS) {
            logger.info("Returned a run command for Windows");
            return new String[]{"cmd.exe", "/c", dockerCommand};
        } else if (os == LINUX) {
            logger.info("Returned a run command for Linux");
            return new String[]{"/bin/bash", "-c", dockerCommand};
        } else
            throw new IllegalStateException("Unsupported OS");
    }

    /**
     * Creates a OS-specific builder with the purpose of initializing a docker container.
     *
     * @return a OS-specific builder with the purpose of initializing a docker container.
     */
    private static ProcessBuilder getDockerImageBuilder() {
        int os = checkOS();

        String dockerBuildCommand = "docker build . -t oving-image";

        if (os == WINDOWS) {
            ProcessBuilder builder = new ProcessBuilder("cmd.exe", "/c", dockerBuildCommand);
            builder.directory(new File("src/main/resources/docker/windowsimage"));
            logger.info("Building a docker image for Windows");
            return builder;
        }
        if (os == LINUX) {
            ProcessBuilder builder = new ProcessBuilder("/bin/bash", "-c", dockerBuildCommand);
            builder.directory(new File("src/main/resources/docker/linuximage"));
            logger.info("Building a docker image for Linux");
            return builder;
        } else
            throw new IllegalStateException("Unsupported OS was detected during building of docker image.");

    }

    /**
     * Returns null on server error
     *
     * @param cppSourceCode the cpp source-code to be compiled
     */
    public static Optional<String> executeInDocker(String cppSourceCode) {
        logger.info("Initializing c++ code execution in docker");

        Process p = null;
        BufferedReader stdOut = null;
        BufferedReader stdErr = null;
        Path path = null;

        String output = null;

        try {
            // Create a temp-file for holding the cpp source code
            path = Files.createTempFile(null, ".cpp");
            Files.write(path, cppSourceCode.getBytes(StandardCharsets.UTF_8));

            // Create docker launch command
            String[] commands = getLaunchCommand(path.toAbsolutePath().toString());

            // Launch docker
            ProcessBuilder builder = new ProcessBuilder(commands);
            p = builder.start();

            stdOut = new BufferedReader(new InputStreamReader(p.getInputStream()));
            stdErr = new BufferedReader(new InputStreamReader(p.getErrorStream()));

            StringBuilder result = new StringBuilder();

            // Append output, if present
            for (String line = stdOut.readLine(); line != null; line = stdOut.readLine())
                result.append(line).append("\n");

            // Append errors, if present
            for (String line = stdErr.readLine(); line != null; line = stdErr.readLine())
                result.append(line).append("\n");

            //  Wait until the process has terminated.
            p.waitFor();
            if (p.exitValue() == 124) {
                output = "Code timed out after 10s" + "\n\n" + "Exit Code: " + p.exitValue();
            } else {
                output = result.toString() + "\n\n" + "Exit Code: " + p.exitValue();
            }

        } catch (IOException e) {
            logger.error("Problem during execution of c++ code in docker container: " + e.toString());
            output = null;
        } catch (InterruptedException e) {
            output = "Error: Execution took too long. Process timed out";
        } finally {
            logger.info("Cleaning up resources");
            try {
                if (stdOut != null) stdOut.close();
                if (stdErr != null) stdErr.close();
                if (path != null) Files.deleteIfExists(path);
                if (p != null) p.destroy();
            } catch (IOException e) {
                logger.warn(e.toString());
            }
        }

        return Optional.ofNullable(output);
    }

    /**
     * Method used for starting a OS-specific on the host server.
     */
    public static void startDockerImages() {
        Process p = null;
        try {
            p = getDockerImageBuilder().start();
            p.waitFor();
            p.destroy();
            logger.info("Docker image initialization done");
        } catch (IOException | InterruptedException e) {
            logger.error("Problem during initialization of docker images: " + e.toString());
        } finally {
            if (p != null) p.destroy();
        }

    }
}
