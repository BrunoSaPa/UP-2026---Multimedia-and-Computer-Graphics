package util;

import java.io.*;
import java.util.List;

//run commands with process builder, used to call the api and to use ffmpeg and exiftool aswell
public class ProcessRunnerUtil {

    public static class ProcessResult {
        private final int exitCode;
        private final String stdout;
        private final String stderr;

        public ProcessResult(int exitCode, String stdout, String stderr) {
            this.exitCode = exitCode;
            this.stdout = stdout;
            this.stderr = stderr;
        }

        public int getExitCode() { return exitCode; }
        public String getStdout()  { return stdout; }
        public String getStderr()  { return stderr; }
        public boolean isSuccess() { return exitCode == 0; }
    }

    //runs a command, waits for it to finish and returns the result which is an object of the class above (ProcessResult0
    public static ProcessResult run(List<String> command) throws IOException, InterruptedException {
        ProcessBuilder pb = new ProcessBuilder(command);
        pb.redirectErrorStream(false);
        Process process = pb.start();

        String stdout = new String(process.getInputStream().readAllBytes());
        String stderr = new String(process.getErrorStream().readAllBytes());

        process.waitFor();
        return new ProcessResult(process.exitValue(), stdout, stderr);
    }

    //runs a command an throws if the exit code is non zero
    public static ProcessResult runOrThrow(List<String> command) throws IOException, InterruptedException {
        ProcessResult result = run(command);
        if (!result.isSuccess()) {
            throw new RuntimeException(String.format(
                "Command failed (exit %d): %s\nStderr: %s",
                result.getExitCode(), String.join(" ", command), result.getStderr()));
        }
        return result;
    }
}
