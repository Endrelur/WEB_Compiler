package ntnu.idatt2104.endrehadzalic.tjener;

import java.io.IOException;

public class Test {

    public static void main(String args[]) throws IOException {
        Runtime runtime = Runtime.getRuntime();
        String param1 = "E:\\Annet\\Kode\\Java\\tjener\\src\\main\\resources\\test.cpp";
        String runBatch = "E:\\Annet\\Kode\\Java\\tjener\\src\\main\\resources\\docker\\run.bat";
        String[] command = {"cmd.exe","/c",
                "start",runBatch, param1};
        runtime.exec(command);
    }
}
