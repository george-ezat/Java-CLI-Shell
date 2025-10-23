import java.util.ArrayList;
import java.util.List;
import java.util.Scanner;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.file.Files;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.BufferedReader;
import java.util.zip.ZipOutputStream;
import static java.nio.file.StandardCopyOption.*;

// ==============================================

class Parser {
    String commandName;
    String[] args;
    boolean append = false;
    String outputFile = null;

    // ------------------------------------------

    // This method will divide the input into commandName and args
    public boolean parse(String input) {
        input = input.trim();
        if (input.contains(">>")) {
            String[] parts = input.split(">>", 2);
            input = parts[0].trim();
            outputFile = parts[1].trim();
            append = true;
        } else if (input.contains(">")) {
            String[] parts = input.split(">", 2);
            input = parts[0].trim();
            outputFile = parts[1].trim();
        }

        String[] input_parts = input.split("\\s+");

        if (input_parts.length == 0 || input_parts[0].isEmpty()) {
            return false;
        }

        commandName = input_parts[0];
        args = new String[input_parts.length - 1];
        System.arraycopy(input_parts, 1, args, 0, input_parts.length - 1);
        return true;
    }

    // ------------------------------------------

    public String getCommandName() {
        return commandName;
    }

    // ------------------------------------------

    public String[] getArgs() {
        return args;
    }
}
// ==============================================
// ==============================================

public class Terminal {
    Parser parser;
    public static final String HOME_DIRECTORY = System.getProperty("user.dir");
    public static String CurrentDirectory = System.getProperty("user.dir");

    // ------------------------------------------

    public File resolveFile(String path) {
        return new File(path).isAbsolute() ? new File(path) : new File(CurrentDirectory, path);
    }

    // ------------------------------------------

    public String pwd() {
        if (parser.args.length != 0) {
            return "Error: command takes no argument.";
        }
        return CurrentDirectory;
    }

    // ------------------------------------------

    public String ls() {
        if (parser.args.length != 0) {
            return "Error: command takes no argument.";
        }

        File currentDir = new File(CurrentDirectory);
        File[] filesAndDirectories = currentDir.listFiles();
        List <String> names = new ArrayList<>();

        for (short i = 0; i < filesAndDirectories.length; i++) {
            if (!filesAndDirectories[i].getName().startsWith(".")) {
                names.add(filesAndDirectories[i].getName());
            }
        }

        names.sort(null);
        return String.join("\t", names);
    }

    // ------------------------------------------

    public String rm(String[] args) {
        if (args.length != 1) {
            return "Error: command requires exactly one argument (file name or path).";
        }

        File fileToRemove = resolveFile(args[0]);
        if (fileToRemove.isDirectory()) {
            return "Error: It is a directory not a file.";
        } else if (fileToRemove.exists()) {
            fileToRemove.delete();
            return "";
        } else {
            return "Error: This file does not exist.";
        }
    }

    // ------------------------------------------

    public String rmdir(String[] args) {
        if (args.length != 1) {
            return "Error: command requires exactly one argument (directory name).";
        }

        File currentDir = new File(CurrentDirectory);

        if ("*".equals(args[0])) {
            for (File item : currentDir.listFiles()) {
                if (item.isDirectory() && item.listFiles().length == 0) {
                    item.delete();
                }
            }
            return "";
        } else {
            File directory = resolveFile(args[0]);
            if (directory.exists()) {
                if (directory.isDirectory()) {
                    if (directory.listFiles().length == 0) {
                        directory.delete();
                        return "";
                    } else {
                        return "Error: Directory is not empty.";
                    }
                } else {
                    return String.format("Error: '%s' is a file, not a directory.", args[0]);
                }
            } else
                return "Error: Directory does not exist.";
        }
    }

    // ------------------------------------------

    public String unzip(String[] args) {
        if (args.length != 1 && args.length != 3) {
            return "Error: Invalid arguments. Usage: unzip <file.zip> [-d <destination_directory>]";
        }

        File zipFile = resolveFile(args[0]);
        File destDir;

        if (!zipFile.getName().endsWith(".zip")) {
            return String.format("Error: '%s' It is not a zip file.", zipFile.getName());
        }

        if (!zipFile.exists()) {
            return String.format("Error: '%s' does not exist.", zipFile.getName());
        }

        // Determine the destination directory
        if (args.length == 3) {
            if (!args[1].equals("-d")) {
                return "Error: Invalid flag. Use '-d' for destination.";
            }
            destDir = resolveFile(args[2]);
        } else {
            destDir = new File(CurrentDirectory);
        }

        // Create the destination directory if it doesn't exist
        if (!destDir.exists()) {
            destDir.mkdirs();
        }

        try (ZipInputStream zis = new ZipInputStream(new FileInputStream(zipFile))) {
            ZipEntry zipEntry = zis.getNextEntry();

            while (zipEntry != null) {
                File newFile = new File(destDir, zipEntry.getName());
                if (zipEntry.isDirectory()) {
                    newFile.mkdirs();
                } else {
                    // Create parent directories if needed
                    new File(newFile.getParent()).mkdirs();
                    Files.copy(zis, newFile.toPath(), REPLACE_EXISTING);
                }
                zis.closeEntry();
                zipEntry = zis.getNextEntry();
            }
            return "";
        } catch (Exception e) {
            return "Error: " + e.getMessage();
        }
    }

    // ------------------------------------------

    public String cp(String[] args) {
        if (args.length > 0 && args[0].equals("-r")) {
            return cpr(args);
        } else {
            if (args.length != 2) {
                return "Error: command requires exactly two arguments.";
            }

            File source = resolveFile(args[0]);
            File des = resolveFile(args[1]);

            if (source.isDirectory() || des.isDirectory()) {
                return "Error: arguments must be files.";
            } else if (source.exists()) {
                try {
                    Files.copy(source.toPath(), des.toPath(), REPLACE_EXISTING);
                } catch (IOException e) {
                    return "Error: " + e.getMessage();
                }
                return "";
            } else {
                return "Error: source file does not exist.";
            }
        }
    }

    // ------------------------------------------

    public String cpr(String[] args) {
        if (args.length != 3) {
            return "Error: command requires exactly two arguments.";
        }

        File source = resolveFile(args[1]);
        File des = resolveFile(args[2]);

        if (!source.isDirectory() || !des.isDirectory()) {
            return "Error: arguments must be directories.";
        } else if (source.exists()) {
            try {
                cp_directory(source, new File(des, source.getName()));
            } catch (IOException e) {
                return "Error: " + e.getMessage();
            }
            return "";
        } else {
            return "Error: source directory does not exist.";
        }
    }

    // ------------------------------------------

    public void cp_directory(File source, File des) throws IOException {
        if (!des.exists()) {
            des.mkdirs();
        }

        File[] files = source.listFiles();

        if (files == null) {
            return;
        }

        for (File f : files) {
            File pasted = new File(des, f.getName());
            if (f.isDirectory()) {
                cp_directory(f, pasted);
            } else {
                Files.copy(f.toPath(), pasted.toPath(), REPLACE_EXISTING);
            }
        }
    }

    // ------------------------------------------

    public String cat(String[] args) {
        if (args.length < 1 || args.length > 2) {
            return "Error: command takes 1 or 2 arguments.";
        }

        String output = "";

        try {
            if (args.length == 1) {
                File file = resolveFile(args[0]);

                if (!file.exists()) {
                    return "Error: file not found.";
                }

                if (file.isDirectory()) {
                    return String.format("Error: '%s' is a directory, not a file.", args[0]);
                }

                try (BufferedReader br = new BufferedReader(new FileReader(file))) {
                    String line;
                    while ((line = br.readLine()) != null) {
                        output += line + "\n";
                    }
                }

            } else if (args.length == 2) {
                File file1 = resolveFile(args[0]);
                File file2 = resolveFile(args[1]);

                if (!file1.exists() || !file2.exists()) {
                    return "Error: one or both files not found.";
                }

                if (file1.isDirectory() || file2.isDirectory()) {
                    return "Error: one or both arguments are directories, not files.";
                }

                try (
                        BufferedReader br1 = new BufferedReader(new FileReader(file1));
                        BufferedReader br2 = new BufferedReader(new FileReader(file2))) {
                    String line;
                    while ((line = br1.readLine()) != null) {
                        output += line + "\n";
                    }
                    while ((line = br2.readLine()) != null) {
                        output += line + "\n";
                    }
                }
            }

        } catch (IOException e) {
            return "Error reading file: " + e.getMessage();
        }

        return output;
    }

    // ------------------------------------------

    public String cd(String[] args) {
        if (args.length > 1) {
            return "Error: command takes at most one argument.";
        }

        String targetPath;
        if (args.length == 0) {
            targetPath = HOME_DIRECTORY;
        } else {
            targetPath = args[0];
        }

        File newDir = resolveFile(targetPath);

        // Check if the path exists and is a directory
        if (newDir.exists() && newDir.isDirectory()) {
            try {
                CurrentDirectory = newDir.getCanonicalPath();
            } catch (IOException e) {
                System.out.println("Error resolving path: " + e.getMessage());
            }
            return "";
        } else {
            return "Error: directory not found.";
        }
    }

    // ------------------------------------------

    public String wc(String[] args) {
        if (args.length != 1) {
            return "Error: command takes exactly one argument (filename).";
        }

        File file = resolveFile(args[0]);
        if (!file.exists() || file.isDirectory()) {
            return "Error: file not found or is a directory.";
        }

        int lines = 0;
        int words = 0;
        long characters = file.length();

        try (BufferedReader br = new BufferedReader(new FileReader(file))) {
            String line;
            while ((line = br.readLine()) != null) {
                lines++;
                String[] wordArray = line.trim().split("\\s+");
                if (!line.trim().isEmpty()) {
                    words += wordArray.length;
                }
            }

            return String.format("%d %d %d %s", lines, words, characters, file.getName());

        } catch (IOException e) {
            return "Error reading file: " + e.getMessage();
        }
    }

    // ------------------------------------------

    public String echo(String[] args) {
        String text = String.join(" ", args);
        text = text.replace("\"", "");
        return text;
    }

    // ------------------------------------------

    public String touch(String[] args) {
        if (args.length != 1) {
            return "Error: command requires exactly one argument.";
        }

        String filePath = args[0];
        File file = resolveFile(filePath);
        try {
            if (file.exists()) {
                if (file.setLastModified(System.currentTimeMillis())) {
                    return "";
                } else {
                    return String.format("Error: Could not update Timestamp for: '%s'", filePath);
                }
            } else {
                if (file.createNewFile()) {
                    return "";
                } else {
                    return String.format("Error: File could not be created: '%s'", filePath);
                }
            }
        } catch (IOException e) {
            return String.format("I/O Error: Could not create or update the file: '%s'", filePath);
        }
    }

    // ------------------------------------------

    public String mkdir(String[] args) {
        if (args.length < 1) {
            return "Error: command requires at least one argument.";
        }

        String output = "";
        for (String dirName : args) {
            dirName = dirName.trim();
            File newDir = resolveFile(dirName);

            try {
                if (newDir.exists()) {
                    output += String.format("Error: The directory '%s' already exists.\n", dirName);
                } else {
                    if (!newDir.mkdir()) {
                        File parent = newDir.getParentFile();
                        if (parent == null || !parent.exists()) {
                            output += "Error: Cannot create directory. Parent path does not exist.\n";
                        } else {
                            output += String.format("Error: Could not create directory: '%s'.\n", dirName);
                        }
                    }
                }
            } catch (Exception e) {
                output += String.format("Error: Could not create directory: '%s'. %s\n", dirName, e.getMessage());
            }
        }
        return output.strip();
    }

    // ------------------------------------------

    public String redirect(String input, String filename, boolean append) {
        File file = resolveFile(filename);
        try {
            if (file.isDirectory()) {
                return "Error: Cannot write to a directory.";
            } else if (!file.exists()) {
                file.createNewFile();
            }
        } catch (IOException e) {
            return String.format("Error creating file: %s", e.getMessage());
        }

        try (FileWriter fw = new FileWriter(file, append)) {
            fw.write(input + System.lineSeparator());
        } catch (IOException e) {
            return String.format("Error writing to file: %s", e.getMessage());
        }
        return "";
    }

    // ------------------------------------------

    public String zip(String[] args) {
        String output = "";
        if (args.length < 2) {
            output += "Error: Invalid arguments.\n";
            output += "Usage: zip <output.zip> <file1> <file2> ...\n";
            output += "       zip -r <output.zip> <folder>";
            return output;
        }

        boolean recursive = false;
        String outputFilePath;
        int filesStartIndex;

        if (args[0].equals("-r")) {
            if (args.length != 3) {
                return "Error: Invalid arguments. Usage: zip -r <output.zip> <folder>";
            }
            recursive = true;
            outputFilePath = args[1];
            filesStartIndex = 2;
        } else {
            outputFilePath = args[0];
            filesStartIndex = 1;
        }

        File outputFile = resolveFile(outputFilePath);

        try (ZipOutputStream zipOutputStream = new ZipOutputStream(new FileOutputStream(outputFile))) {
            if (!recursive) {
                for (int i = filesStartIndex; i < args.length; i++) {

                    File fileToZip = resolveFile(args[i]);

                    if (!fileToZip.exists()) {
                        output += String.format("Error: File not found: '%s'\n", fileToZip.getPath());
                        continue;
                    }
                    if (fileToZip.isDirectory()) {
                        output += String.format("Warning: Skipping folder (use -r): '%s'\\n", fileToZip.getPath());
                        continue;
                    }

                    ZipEntry zipEntry = new ZipEntry(fileToZip.getName());
                    zipOutputStream.putNextEntry(zipEntry);
                    Files.copy(fileToZip.toPath(), zipOutputStream);
                    zipOutputStream.closeEntry();
                }
                return output.strip();
            } else {
                String folderPathArg = args[filesStartIndex];
                File folderToZip = resolveFile(folderPathArg);

                if (!folderToZip.exists()) {
                    return String.format("Error: Folder not found: '%s'", folderToZip.getPath());
                }
                if (!folderToZip.isDirectory()) {
                    return String.format("Error: '%s' is a file. The -r flag is for folders.", folderToZip.getPath());
                }

                addFileToZip(zipOutputStream, folderToZip, "");
                return output;
            }
        } catch (IOException e) {
            return "I/O Error: Could not compress files. " + e.getMessage();
        }
    }

    // ------------------------------------------

    private void addFileToZip(ZipOutputStream zipOut, File file, String baseName) throws IOException {
        String entryName = baseName + file.getName();

        if (file.isDirectory()) {
            zipOut.putNextEntry(new ZipEntry(entryName + "/"));
            zipOut.closeEntry();

            File[] children = file.listFiles();
            if (children != null) {
                for (File child : children) {
                    addFileToZip(zipOut, child, entryName + "/");
                }
            }
        } else {
            zipOut.putNextEntry(new ZipEntry(entryName));
            Files.copy(file.toPath(), zipOut);
            zipOut.closeEntry();
        }
    }

    // ------------------------------------------

    // This method will choose the suitable command method to be called
    public void chooseCommandAction() throws IOException {
        String output = "";
        switch (parser.commandName) {
            case "pwd":
                output = pwd();
                break;
            case "ls":
                output = ls();
                break;
            case "rm":
                output = rm(parser.args);
                break;
            case "rmdir":
                output = rmdir(parser.args);
                break;
            case "unzip":
                output = unzip(parser.args);
                break;
            case "cp":
                output = cp(parser.args);
                break;
            case "cat":
                output = cat(parser.args);
                break;
            case "cd":
                output = cd(parser.args);
                break;
            case "echo":
                output = echo(parser.args);
                break;
            case "wc":
                output = wc(parser.args);
                break;
            case "touch":
                output = touch(parser.args);
                break;
            case "mkdir":
                output = mkdir(parser.args);
                break;
            case "zip":
                output = zip(parser.args);
                break;
            default:
                output = "Error: Command not found.";
        }

        if (parser.outputFile != null && !parser.outputFile.isEmpty()) {
            String redirectError = redirect(output, parser.outputFile, parser.append);
            if (redirectError != null && !redirectError.isEmpty()) {
                System.out.println(redirectError);
            }
        } else if (output != null && !output.isEmpty()) {
            System.out.println(output);
        }
    }

    // ------------------------------------------

    public static void main(String[] args) throws IOException {
        Terminal terminal = new Terminal();
        Scanner in = new Scanner(System.in);
        String input;

        while (true) {
            System.out.printf("%s $ ", Terminal.CurrentDirectory);
            input = in.nextLine();

            if ("exit".equalsIgnoreCase(input.trim())) {
                break;
            }

            terminal.parser = new Parser();
            if (terminal.parser.parse(input)) {
                terminal.chooseCommandAction();
            }
        }
        in.close();
    }
}

// ==============================================