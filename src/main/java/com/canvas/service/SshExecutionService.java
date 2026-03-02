package com.canvas.service;

import org.springframework.stereotype.Service;

import java.io.File;
import java.nio.file.Files;
import java.util.Map;

@Service
public class SshExecutionService {
    public Map<String, Object> execute(String host, Integer port, String username, String privateKey, String cmd, int timeoutMs, String policy) throws Exception {
        long st = System.currentTimeMillis();
        File key = File.createTempFile("canvas-key", ".pem");
        Files.writeString(key.toPath(), privateKey);
        ProcessBuilder pb = new ProcessBuilder(
                "ssh", "-i", key.getAbsolutePath(), "-p", String.valueOf(port), "-o",
                "StrictHostKeyChecking=" + ("accept-new".equals(policy) ? "accept-new" : "yes"),
                username + "@" + host, cmd);
        Process p = pb.start();
        boolean done = p.waitFor(timeoutMs, java.util.concurrent.TimeUnit.MILLISECONDS);
        if (!done) p.destroyForcibly();
        String stdout = new String(p.getInputStream().readAllBytes());
        String stderr = new String(p.getErrorStream().readAllBytes());
        int exitCode = done ? p.exitValue() : 124;
        key.delete();
        return Map.of("stdout", stdout, "stderr", stderr, "exit_code", exitCode, "elapsed_ms", System.currentTimeMillis() - st);
    }
}
