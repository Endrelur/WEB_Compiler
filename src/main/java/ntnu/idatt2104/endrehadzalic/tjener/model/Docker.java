package ntnu.idatt2104.endrehadzalic.tjener.model;

import org.apache.commons.lang3.SystemUtils;

import java.io.BufferedReader;
import java.io.Closeable;
import java.io.IOException;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Optional;
import java.util.concurrent.TimeUnit;


public class Docker {

    public static final int WINDOWS = 0;
    public static final int LINUX   = 1;

    /**
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

    private static String[] getLaunchCommand(String cppPath) {
        int os = checkOS();

        String dockerCommand = "docker run -i --rm oving-image < " + cppPath;

        if (os == WINDOWS)
            return new String[]{"cmd.exe", "/c", dockerCommand};
        else if (os == LINUX)
            return new String[]{"/bin/bash", "-c", dockerCommand};
        else
            throw new IllegalStateException("Unsupported OS");
    }

    /**
     * Returns null on server error
     *
     * @param cppSourceCode the cpp source-code to be compiled
     */
    public static Optional<String> executeInDocker(String cppSourceCode, long timeout) {
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
            p.waitFor(timeout, TimeUnit.SECONDS);
            int exitValue = p.exitValue();
            p.destroy();

            output = "Exit Code: " + exitValue + "\n" + result.toString();
        }
        catch (IOException e) {
            System.err.println("ERROR");
            output = null;
        }
        catch (InterruptedException e) {
            output = "Execution was timed out";
        }
        finally {
            closeQuietly(stdOut);
            closeQuietly(stdErr);
            deleteQuietly(path);
        }

        return Optional.ofNullable(output);
    }


    private static void closeQuietly(Closeable closeable) {
        if (closeable == null)
            return;
        try {
            closeable.close();
        } catch (IOException e) {}
    }

    private static void deleteQuietly(Path path) {
        if (path == null)
            return;
        try {
            Files.deleteIfExists(path);
        } catch (IOException e) {}
    }
}
