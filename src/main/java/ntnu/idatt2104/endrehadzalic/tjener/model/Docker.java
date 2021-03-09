package ntnu.idatt2104.endrehadzalic.tjener.model;

import org.apache.commons.lang3.SystemUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.*;
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
    private static Logger logger = LoggerFactory.getLogger(Docker.class);


    /**
     * Checks the servers host-OS and returns a corresponding value.
     *
     * @return -1 if the identified OS is unsupported
     */
    private static int checkOS() {
        int os = -1;
        if (SystemUtils.IS_OS_WINDOWS)
            os = WINDOWS;
        else if (SystemUtils.IS_OS_LINUX)
            os = LINUX;
        return os;
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
            ProcessBuilder builder = new ProcessBuilder(new String[]{"cmd.exe", "/c", dockerBuildCommand});
            builder.directory(new File("src/main/resources/docker/windowsimage"));
            logger.info("Building a docker image for Windows");
            return builder;
        }
        if (os == LINUX) {
            ProcessBuilder builder = new ProcessBuilder(new String[]{"/bin/bash", "-c", dockerBuildCommand});
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
        logger.info("Opening streams and creating a temporary c++ file");
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
            Process p = builder.start();

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
            // System.out.println("exit: " + process.exitValue());
            p.destroy();

            output = result.toString();
        } catch (IOException | InterruptedException e) {
            logger.error("Problem during execution of c++ code in docker container " + e.toString());
            output = null;
        } finally {
            logger.info("Closing streams and deleting temporary c++ file");
            closeQuietly(stdOut);
            closeQuietly(stdErr);
            deleteQuietly(path);
        }

        return Optional.ofNullable(output);
    }

    /**
     * Method used for starting a OS-specific on the host server.
     */
    public static void startDockerImages() {
        try {
            ProcessBuilder builder = getDockerImageBuilder();
            Process p = builder.start();

            p.waitFor();
            p.destroy();
            logger.info("Docker image initializaion done");
        } catch (IOException | InterruptedException e) {
            logger.error("Problem during initialization of docker images: " + e.toString());
        }

    }


    private static void closeQuietly(Closeable closeable) {
        if (closeable == null)
            return;
        try {
            closeable.close();
        } catch (IOException e) {
            logger.error(e.toString());
        }
    }

    private static void deleteQuietly(Path path) {
        if (path == null)
            return;
        try {
            Files.deleteIfExists(path);
        } catch (IOException e) {
            logger.error(e.toString());
        }
    }
}
