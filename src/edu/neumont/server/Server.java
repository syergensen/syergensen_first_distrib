package edu.neumont.server;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.lang.reflect.Constructor;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.URISyntaxException;
import java.net.URL;
import java.nio.file.DirectoryStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * User: Sean Yergensen
 */
public class Server extends Thread {
    public static final int PORT = 12345;
    public static final String DEFAULT_SERVICE_PACKAGE = "edu.neumont.server.services";

    private Socket socket;

    public Server(Socket socket) {
        this.socket = socket;
    }

    public static void main(String[] args) {
        try {
            ServerSocket serverSocket = new ServerSocket(PORT);
            while(true) {
                Socket socket = serverSocket.accept();
                new Server(socket).start();
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @Override
    public void run() {
        try (PrintWriter writer = new PrintWriter(socket.getOutputStream());
             BufferedReader reader = new BufferedReader(new InputStreamReader(socket.getInputStream()))) {

            String line, response = null;
            while((line = reader.readLine()) != null) {
                if(line.trim().startsWith("help")) {
                    response = "classes, methods Class, constructors Class\n" +
                            "Class.method({constructor-param1, constructor-param2}, 1, 0.5, \"string\", [array-value1, array-value2])";
                }
                else if(line.trim().startsWith("classes")) {
                    response = getClasses();
                }
                else if(line.trim().startsWith("methods")) {
                    String[] methodCommand = line.trim().split(" ");
                    if(methodCommand.length > 1) {
                        response = getMethods(methodCommand[1]);
                    }
                    else {
                        response = "Not a valid methods command, include the class you wish to view methods of";
                    }
                }
                else if(line.trim().startsWith("constructors")) {
                    String[] constructorCommand = line.trim().split(" ");
                    if(constructorCommand.length > 1) {
                        response = getConstructors(constructorCommand[1]);
                    }
                    else {
                        response = "Not a valid list command, include the class you wish to view constructors of";
                    }
                }
                else {
                    try {
                        response = invokeMethod(line);
                    } catch (Exception e) {
                        response = "Unable to invoke method, try again";
                    }
                }
                writer.println(response);
                // I hate this, figure out how to do it better
                writer.println("$");
                writer.flush();
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private String getMethods(String className) {
        String fullClassName = className;
        if(!className.startsWith(DEFAULT_SERVICE_PACKAGE)) {
            fullClassName = DEFAULT_SERVICE_PACKAGE + "." + className;
        }
        try {
            Class serviceClass = Class.forName(fullClassName);
            Method[] methods = serviceClass.getDeclaredMethods();
            StringBuilder builder = new StringBuilder();
            for (Method method : methods) {
                if(Modifier.isPublic(method.getModifiers()) && !Modifier.isStatic(method.getModifiers())) {
                    builder.append(method.getName()).append("(");
                    for (int i = 0; i < method.getParameterTypes().length; i++) {
                        Class<?> type = method.getParameterTypes()[i];
                        builder.append(printType(type.getName()));
                        if(i < method.getParameterTypes().length - 1) {
                            builder.append(",");
                        }
                    }
                    builder.append(")\n");
                }
            }
            return builder.toString();
        } catch (ClassNotFoundException e) {
            return "Not a valid service class, use list classes to lookup valid classes";
        }
    }

    private String getConstructors(String className) {
        try {
            Class paramClass = Class.forName(className);
            Constructor[] constructors = paramClass.getDeclaredConstructors();
            StringBuilder builder = new StringBuilder();
            for (Constructor constructor : constructors) {
                if(Modifier.isPublic(constructor.getModifiers())) {
                    builder.append(constructor.getName()).append("(");
                    for (int i = 0; i < constructor.getParameterTypes().length; i++) {
                        Class<?> type = constructor.getParameterTypes()[i];
                        builder.append(printType(type.getName()));
                        if(i < constructor.getParameterTypes().length - 1) {
                            builder.append(",");
                        }
                    }
                    builder.append(")\n");
                }
            }
            return builder.toString();
        } catch (ClassNotFoundException e) {
            return "Not a valid class, please include the fully qualified name";
        }
    }

    private static final Map<String, String> ARRAY_SYNTAX;
    static {
        ARRAY_SYNTAX = new HashMap<>();
        ARRAY_SYNTAX.put("[I", "int[]");
        ARRAY_SYNTAX.put("[F", "float[]");
        ARRAY_SYNTAX.put("[D", "double[]");
        ARRAY_SYNTAX.put("[J", "long[]");
        ARRAY_SYNTAX.put("[S", "short[]");
        ARRAY_SYNTAX.put("[Z", "boolean[]");
        ARRAY_SYNTAX.put("[B", "byte[]");

    }
    private String printType(String name) {
        if(name.startsWith("[L")) {
            return name.replace("[L", "").replace(";", "[]").replace("java.lang.", "");
        }
        else if(name.startsWith("[")) {
            return ARRAY_SYNTAX.get(name.substring(0, 2));
        }
        return name.replace("java.lang.", "");
    }

    private String getClasses() {
        try {
            List<Class> services = getClassesInPackage(DEFAULT_SERVICE_PACKAGE);
            StringBuilder builder = new StringBuilder();
            for (Class service : services) {
                builder.append(service.getName()).append("\n");
            }
            return builder.toString();
        } catch (Exception e) {
            return "An error occurred retrieving a list of classes";
        }
    }

    private List<Class> getClassesInPackage(String packageName) throws URISyntaxException, IOException, ClassNotFoundException {
        URL packageUrl = this.getClass().getClassLoader().getResource(packageName.replace(".", "/"));
        List<Class> allClasses = new ArrayList<>();
        if(packageUrl != null) {

            Path packagePath = Paths.get(packageUrl.toURI());
            if(Files.isDirectory(packagePath)) {
                try(DirectoryStream<Path> ds = Files.newDirectoryStream(packagePath, "*.class")) {
                    for(Path d : ds) {
                        allClasses.add(Class.forName(packageName + "." + d.getFileName().toString().replace(".class", "")));
                    }
                }
            }
            return allClasses;
        }
        return null;
    }

    private String invokeMethod(String line) {
        return "";
    }
}
