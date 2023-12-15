package me.the1withspaghetti.compiler;

import javax.tools.DiagnosticCollector;
import javax.tools.JavaCompiler;
import javax.tools.JavaFileObject;
import javax.tools.ToolProvider;
import java.io.IOException;
import java.util.Collections;
import java.util.List;

public class ClassCompiler {

    public static Class<?> compileCode(String className, String code) throws IOException, ClassNotFoundException {

        JavaCompiler compiler = ToolProvider.getSystemJavaCompiler();
        DiagnosticCollector<JavaFileObject> diagnostics = new DiagnosticCollector<>();
        InMemoryFileManager manager = new InMemoryFileManager(compiler.getStandardFileManager(null, null, null));

        List<JavaFileObject> sourceFiles = Collections.singletonList(new JavaSourceFromString(className, code));
        JavaCompiler.CompilationTask task = compiler.getTask(null, manager, diagnostics, null, null, sourceFiles);

        boolean result = task.call();

        if (!result) {
            diagnostics.getDiagnostics()
                    .forEach(d -> System.err.print(String.valueOf(d)));
            throw new RuntimeException("Could not compile code, aborting tests");
        } else {
            ClassLoader classLoader = manager.getClassLoader(null);

            try {
                return classLoader.loadClass(className);
            } catch (ClassNotFoundException e) {
                throw new RuntimeException("Could not compile code, aborting tests", e);
            }
        }
    }
}
