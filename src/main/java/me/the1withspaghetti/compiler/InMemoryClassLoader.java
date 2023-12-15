package me.the1withspaghetti.compiler;

import java.util.Map;

import static java.util.Objects.requireNonNull;

public class InMemoryClassLoader extends ClassLoader {

    private final InMemoryFileManager manager;

    public InMemoryClassLoader(ClassLoader parent, InMemoryFileManager manager) {
        super(parent);
        this.manager = requireNonNull(manager, "manager must not be null");
    }

    @Override
    protected Class<?> findClass(String name) throws ClassNotFoundException {

        Map<String, JavaClassAsBytes> compiledClasses = manager
                .getBytesMap();
        for (String key : compiledClasses.keySet()) {
            if (key.substring(key.lastIndexOf('.')+1).equals(name.substring(name.lastIndexOf('.')+1))) {
                byte[] bytes = compiledClasses.get(key)
                        .getBytes();
                return defineClass(key, bytes, 0, bytes.length);
            }
        }
        throw new ClassNotFoundException();
    }
}
