package com.rasysbox.ws.utils;

import org.springframework.stereotype.Component;

import java.io.BufferedReader;
import java.io.InputStreamReader;

@Component
public class ExecutorCommand {

    public String executeCommand(String command) throws Exception {
        String osName = System.getProperty("os.name").toLowerCase();
        String[] finalCommand;

        if (osName.contains("win")) {
            finalCommand = new String[]{"cmd.exe", "/c", command};
        } else if (osName.contains("nix") || osName.contains("nux") || osName.contains("mac")) {
            finalCommand = new String[]{"/bin/bash", "-c", command};
        } else {
            throw new UnsupportedOperationException("Sistema operativo no soportado: " + osName);
        }

        ProcessBuilder processBuilder = new ProcessBuilder(finalCommand);
        processBuilder.redirectErrorStream(true);
        Process process = processBuilder.start();

        StringBuilder output = new StringBuilder();
        try (BufferedReader reader = new BufferedReader(new InputStreamReader(process.getInputStream()))) {
            String line;
            while ((line = reader.readLine()) != null) {
                output.append(line).append(System.lineSeparator());
            }
        }

        int exitCode = process.waitFor();
        if (exitCode != 0) {
            throw new RuntimeException("El comando falló con código de salida: " + exitCode);
        }

        return output.toString().trim();
    }
}
