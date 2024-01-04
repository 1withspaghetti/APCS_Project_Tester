package me.the1withspaghetti.compiler;

import me.the1withspaghetti.tests.TestException;
import me.the1withspaghetti.util.ConsoleReader;
import me.the1withspaghetti.util.TeeOutputStream;
import org.apache.commons.io.input.TeeInputStream;

import java.io.*;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Comparator;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;
import java.util.stream.Stream;

public class CompiledCode implements AutoCloseable {

    private final String className;
    private final Path tempDir;
    private final Path classDir;

    private final PrintStream out, inout, err;

    private Process process = null;
    private OutputStream processIn = null;
    private ConsoleReader processOut = null;
    private ConsoleReader processErr = null;

    /**
     * Compiles a java class file to be executing in a separate JVM
     *
     * @param className - The name of the class to be compiled, should match the defined class name in the sourceCode
     * @param sourceCode - A string of java source code that will be compiled into a class file
     * @param out - The stream where the console output should be printed
     * @param inout - The stream where console input sent to the code should be printed
     * @param err - The steam where console err should be printed, but will also be used for compilation and running errors
     * @throws IOException
     * @throws ExecutionException
     * @throws InterruptedException
     */
    public CompiledCode(String className, String sourceCode, PrintStream out, PrintStream inout, PrintStream err) throws IOException, ExecutionException, InterruptedException {
        this.className = className;
        this.out = out;
        this.inout = inout;
        this.err = err;

        System.out.println("Compiling "+className);

        tempDir = Files.createTempDirectory("APCS_Project_Tester");
        Files.createDirectories(tempDir);

        Path srcFile = tempDir.resolve(className+".java");
        if (!Files.exists(srcFile)) Files.createFile(srcFile);
        Files.writeString(srcFile, sourceCode);

        classDir = tempDir.resolve("classes");
        Files.createDirectory(classDir);

        Process p = new ProcessBuilder(Path.of(System.getProperty("java.home"), "bin", "javac.exe").toString(), srcFile.toString(), "-d", classDir.toString())
                .directory(tempDir.toFile())
                .start();
        p.getErrorStream().transferTo(err);
        try {
            p.onExit().get(5000, TimeUnit.MILLISECONDS);
        } catch (TimeoutException e) {
            p.descendants().forEach(ProcessHandle::destroy);
            p.destroy();
            throw new TestException("Could not compile code, java compiler took more than 5 seconds!");
        }
        System.out.println("Finished compiling "+className);
    }


    /**
     * Runs the compiled code, all console output, error, and input can be collected/sent
     * with {@link #getProcessOutput()}, {@link #getProcessError()}, and {@link #getProcessInput()} respectively
     * @throws IOException
     */
    public synchronized void runCode() throws IOException {
        if (process != null) throw new IllegalStateException("Code is already executing");

        System.out.println("Executing "+className);

        process = new ProcessBuilder(Path.of(System.getProperty("java.home"), "bin", "java.exe").toString(), className).directory(classDir.toFile()).start();
        process.onExit().thenRun(()->{
            System.out.println("Finished executing "+className+"\n");
        });

        processIn = new TeeOutputStream(this.inout, process.getOutputStream());
        processOut = new ConsoleReader(new InputStreamReader(new TeeInputStream(process.getInputStream(), out)));
        processErr = new ConsoleReader(new InputStreamReader(new TeeInputStream(process.getErrorStream(), err)));
    }

    public ConsoleReader getProcessOutput() {
        if (process == null) throw new IllegalStateException("Code is not executing");
        return processOut;
    }

    public ConsoleReader getProcessError() {
        if (process == null) throw new IllegalStateException("Code is not executing");
        return processErr;
    }

    public OutputStream getProcessInput() {
        if (process == null) throw new IllegalStateException("Code is not executing");
        return processIn;
    }

    public void terminate() {
        process.descendants().forEach(ProcessHandle::destroy);
        process.destroy();
        if (process.isAlive()) {
            System.err.println("WARNING: "+className+" could not immediately terminate");
            process.destroyForcibly();
        }
    }

    /**
     * Terminates the process if it is still alive, closes all input/output streams, and deletes all temp files
     * @throws Exception
     */
    @Override
    public void close() throws Exception {
        terminate();
        processIn.close();
        processOut.close();
        processErr.close();
        try (Stream<Path> contents = Files.walk(tempDir)) {
            contents.sorted(Comparator.reverseOrder())
                    .forEach(path->{
                        try {
                            Files.delete(path);
                        } catch (IOException e) {
                            throw new RuntimeException(e);
                        }
                    });
        }
    }
}
