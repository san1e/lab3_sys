import java.io.*;
import java.nio.file.*;
import java.nio.ByteBuffer;
import java.nio.file.attribute.UserDefinedFileAttributeView;

public class ImportantFiles {
    public static final String ANSI_RESET = "\u001B[0m";
    public static final String ANSI_GREEN = "\u001B[32m";
    public static final String ANSI_RED = "\u001B[31m";
    public static void main(String[] args) throws IOException {
        if (args.length == 0) {
            System.out.println("Usage: $ important <command> <file>");
            return;
        }

        String action = args[0];
        switch (action) {
            case "mark":
                if (args.length < 2) {
                    System.out.println("Error: File path is required.");
                    return;
                }
                markAsImportant(args[1]);
                break;
            case "unmark":
                if (args.length < 2) {
                    System.out.println("Error: File path is required.");
                    return;
                }
                unmarkAsImportant(args[1]);
                break;
            case "find":
                searchImportantFiles(args);
                break;
            case "help":
                showHelp();
                break;
            default:
                System.out.println("Unknown command: " + action);
                break;
        }
    }

    private static void markAsImportant(String filePath) throws IOException {
        Path path = Paths.get(filePath);
        if (Files.exists(path)) {
            UserDefinedFileAttributeView view = Files.getFileAttributeView(path, UserDefinedFileAttributeView.class);
            if (view != null) {  // Проверка на поддержку пользовательских атрибутов
                view.write("important", ByteBuffer.wrap("true".getBytes()));
                System.out.println(ANSI_GREEN+"File marked as important: " + filePath+ANSI_RESET);
            } else {
                System.out.println("Error: User-defined attributes are not supported on this file system.");
            }
        } else {
            System.out.println("Error: File does not exist.");
        }
    }

    private static void unmarkAsImportant(String filePath) throws IOException {
        Path path = Paths.get(filePath);
        if (Files.exists(path)) {
            UserDefinedFileAttributeView view = Files.getFileAttributeView(path, UserDefinedFileAttributeView.class);
            if (view != null && view.list().contains("important")) {
                view.delete("important");
                System.out.println(ANSI_GREEN + "File unmarked as important: " + filePath + ANSI_RESET);
            } else {
                System.out.println("File is not marked as important or user-defined attributes are not supported.");
            }
        } else {
            System.out.println("Error: File does not exist.");
        }
    }

    private static void searchImportantFiles(String[] args) throws IOException {
        String directory = ".";
        String fileExtension = null;
        String nameContains = null;

        for (int i = 1; i < args.length; i++) {
            switch (args[i]) {
                case "--dir":
                    directory = args[++i];
                    break;
                case "--ext":
                    fileExtension = args[++i];
                    break;
                case "--name-contains":
                    nameContains = args[++i];
                    break;
            }
        }

        String finalFileExtension = fileExtension;
        String finalNameContains = nameContains;
        Files.walk(Paths.get(directory))
                .filter(Files::isRegularFile)
                .forEach(path -> {
                    try {
                        UserDefinedFileAttributeView view = Files.getFileAttributeView(path, UserDefinedFileAttributeView.class);
                        if (view != null && view.list().contains("important")) {
                            String fileName = path.getFileName().toString();
                            if ((finalFileExtension == null || fileName.endsWith(finalFileExtension)) &&
                                    (finalNameContains == null || fileName.contains(finalNameContains))) {
                                System.out.println(ANSI_GREEN + path.toAbsolutePath()+ANSI_RESET);
                            }
                        }
                    } catch (IOException e) {
                        System.err.println("Error accessing file attributes: " + path);
                    }
                });
    }

    private static void showHelp() {
        System.out.println(ANSI_GREEN+"Usage: $ important <command> [options]"+ANSI_RESET);
        System.out.println();
        System.out.println("Commands:");
        System.out.println("  mark <file>            Mark a file as important.");
        System.out.println("  unmark <file>          Unmark a file as important.");
        System.out.println("  find [options]         Find important files.");
        System.out.println("  list                   Show all important files.");
        System.out.println("  help                   Show this help message.");
        System.out.println();
        System.out.println("Options for 'find':");
        System.out.println("  --dir <directory>      Directory to search (default: current directory).");
        System.out.println("  --ext <extension>      File extension to filter by.");
        System.out.println("  --name-contains <text> Partial text to match in the file name.");

    }
}
