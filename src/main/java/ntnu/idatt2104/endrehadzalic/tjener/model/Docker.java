package ntnu.idatt2104.endrehadzalic.tjener.model;

import org.apache.commons.lang3.SystemUtils;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.Closeable;

import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Optional;


public class Docker {

    public static final int WINDOWS                 = 0;
    public static final int LINUX                   = 1;

    private static final String BASE_PATH           = "/src/main/resources/docker/";
    private static final String WINDOWS_RUN_SCRIPT  = BASE_PATH + "run.cmd";
    private static final String LINUX_RUN_SCRIPT    = BASE_PATH + "run.sh";

    /**
     * @return  -1 if the identified OS is unsupported
     */
    private static int checkOS() {
        int os = -1;
        if (SystemUtils.IS_OS_WINDOWS)
            os = WINDOWS;
        else if (SystemUtils.IS_OS_LINUX)
            os = LINUX;
        return os;
    }

    private static String[] getLaunchCommand() {
        int os = checkOS();

        if (os == WINDOWS)
            return null;                                    // FIXME @Endre
        else if (os == LINUX)
            return new String[]{"sh", LINUX_RUN_SCRIPT};
        else
            throw new IllegalStateException("Unsupported OS");
    }

    /**
     * Returns null on server error
     * @param cppSourceCode the cpp source-code to be compiled
     */
    public static Optional<String> executeInDocker(String cppSourceCode) {
        BufferedReader stdOut = null;
        BufferedReader stdErr = null;
        Path path = null;

        String output = null;

        try {
            // Create a temp-file for holding the cpp source code
            path = Files.createTempFile(Long.toString(System.nanoTime()), ".cpp");
            Files.write(path, cppSourceCode.getBytes(StandardCharsets.UTF_8));

            // Create docker launch command
            String[] launchScript = getLaunchCommand();
            String[] commands = { launchScript[0], launchScript[1], path.toAbsolutePath().toString() };

            // Launch docker
            Process process = Runtime.getRuntime().exec(commands);

            stdOut = new BufferedReader(new InputStreamReader(process.getInputStream()));
            stdErr = new BufferedReader(new InputStreamReader(process.getErrorStream()));

            StringBuilder result = new StringBuilder();

            // Append output, if present
            for (String line = stdOut.readLine(); line != null; line = stdOut.readLine())
                result.append(line).append("\n");

            // Append errors, if present
            for (String line = stdErr.readLine(); line != null; line = stdErr.readLine())
                result.append(line).append("\n");

            //  Wait until the process has terminated.
            process.waitFor();
            // System.out.println("exit: " + process.exitValue());
            process.destroy();

            output = result.toString();
        }
        catch(IOException | InterruptedException e) {
            output = null;
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
