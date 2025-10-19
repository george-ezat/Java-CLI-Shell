import java.nio.file.DirectoryStream;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Arrays;
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

    public String pwd() {
        if (parser.args.length != 0) {
            return "Error: command takes no argument.";
        }
        return CurrentDirectory;
    }

    // ------------------------------------------

    public String ls() {
        if (parser.args.length != 0) {
            return "Error: command takes no argument";
        }

        File currentDir = new File(CurrentDirectory);
        File[] filesAndDirectories = currentDir.listFiles();
        String[] names = new String[filesAndDirectories.length];

        for (short i = 0; i < filesAndDirectories.length; i++) {
            names[i] = filesAndDirectories[i].getName();
        }

        Arrays.sort(names);
        return String.join("\t", names);
    }

    // ------------------------------------------

    public String rm(String[] args) {
        if (args.length != 1) {
            return "Error: command requires exactly one argument (file name or path).";
        }

        File fileToRemove = new File(CurrentDirectory, args[0]);
        if (fileToRemove.isDirectory()) {
            return "Error: It is a directory not a file!";
        } else if (fileToRemove.exists()) {
            fileToRemove.delete();
            return "";
        } else {
            return "Error: This file does not exist!";
        }
    }

    // ------------------------------------------

    public String rmdir(String[] args) {
        if (args.length != 1) {
            return "Error: command requires exactly one argument (directory name).";
        }

        File currentDir = new File(CurrentDirectory);
        File[] filesAndDirectories = currentDir.listFiles();

        if ("*".equals(args[0])) {
            for (File item : filesAndDirectories) {
                if (item.isDirectory() && item.listFiles().length == 0) {
                    item.delete();
                }
            }
            return "";
        } else {
            File directory = new File(CurrentDirectory, args[0]);
            if (directory.exists()) {
                if (directory.isDirectory()) {
                    if (directory.listFiles().length == 0) {
                        directory.delete();
                        return "";
                    } else {
                        return "Error: Directory is not empty!";
                    }
                } else {
                    return "Error: '" + args[0] + "' is a file, not a directory!";
                }
            } else
                return "Error: Directory does not exist!";
        }
    }

    // ------------------------------------------

    public String unzip(String[] args) {
        if (args.length != 1 && args.length != 3) {
            return "Error: Invalid arguments!\nUsage: unzip <file.zip> [-d <destination_directory>]";
        }

        File zipFile = new File(CurrentDirectory, args[0]);
        File destDir;

        if (!zipFile.getName().endsWith(".zip")) {
            return "Error: It is not a zip file!";
        }

        if (!zipFile.exists()) {
            return "Error: Zip file does not exist!";
        }

        // Determine the destination directory
        if (args.length == 3) {
            if (!args[1].equals("-d")) {
                return "Error: Invalid flag. Use '-d' for destination.";
            }
            destDir = new File(CurrentDirectory, args[2]);
        } else {
            destDir = new File(CurrentDirectory);
        }

        // Create the destination directory if it doesn't exist
        if (!destDir.exists()) {
            destDir.mkdirs();
        }

        try (ZipInputStream zis = new ZipInputStream(new FileInputStream(zipFile))) {
            ZipEntry zipEntry = zis.getNextEntry();
            byte[] buffer = new byte[1024];

            while (zipEntry != null) {
                File newFile = new File(destDir, zipEntry.getName());
                if (zipEntry.isDirectory()) {
                    newFile.mkdirs();
                } else {
                    // Create parent directories if needed
                    new File(newFile.getParent()).mkdirs();
                    try (FileOutputStream fos = new FileOutputStream(newFile)) {
                        int len;
                        while ((len = zis.read(buffer)) > 0) {
                            fos.write(buffer, 0, len);
                        }
                    }
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

            File source = new File(CurrentDirectory, args[0]);
            File des = new File(CurrentDirectory, args[1]);

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
                return "Error: source file does not exist!";
            }
        }
    }

    // ------------------------------------------

    public String cpr(String[] args) {
        if (args.length != 3) {
            return "Error: command requires exactly two arguments.";
        }

        File source = new File(CurrentDirectory, args[1]);
        File des = new File(CurrentDirectory, args[2]);

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
            return "Error: source directory does not exist!";
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
        if (args.length != 1 && args.length != 2 && args.length != 3) {
            return "Error: command takes from 1 to 3 arguments";
        }

        String output = "";

        try {
            if (args.length == 1) {
                File file = new File(CurrentDirectory, args[0]);

                if (!file.exists()) {
                    return "Error: file not found!";
                }

                if (file.isDirectory()) {
                    return "Error: " + args[0] + " is a directory, not a file!";
                }

                try (BufferedReader br = new BufferedReader(new FileReader(file))) {
                    String line;
                    while ((line = br.readLine()) != null) {
                        output += line + "\n";
                    }
                }

            } else if (args.length == 2) {
                File file1 = new File(CurrentDirectory, args[0]);
                File file2 = new File(CurrentDirectory, args[1]);

                if (!file1.exists() || !file2.exists()) {
                    return "Error: one or both files not found!";
                }

                if (file1.isDirectory() || file2.isDirectory()) {
                    return "Error: one or both arguments are directories, not files!";
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

        File newDir;
        File targetFile = new File(targetPath);

        if (targetFile.isAbsolute()) {
            newDir = targetFile;
        } else {
            newDir = new File(CurrentDirectory, targetPath);
        }

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

        File file = new File(CurrentDirectory, args[0]);
        if (!file.exists() || file.isDirectory()) {
            return "Error: file not found or is a directory!";
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

            lines = lines == 0 ? lines : lines - 1;

            return String.format("%d %d %d %s\n", lines, words, characters, file.getName());

        } catch (IOException e) {
            return "Error reading file: " + e.getMessage();
        }
    }

    // ------------------------------------------

    public String echo(String[] args) {
        return String.join(" ", args);
    }

    // ------------------------------------------

    public String touch(String[] args) {
        String filePath = args[0];
        File file = new File(CurrentDirectory, filePath);
        try {
            if (file.exists()) {
                if (file.setLastModified(System.currentTimeMillis())) {
                    return "";
                } else {
                    return "Error: Could not update Timestamp for: " + filePath;
                }
            } else {
                if (file.createNewFile()) {
                    return "";
                } else {
                    return "Error: File could not be created: " + filePath;
                }
            }
        } catch (IOException e) {
            return "I/O Error: Could not create or update the file: " + filePath;
        }
    }

    // ------------------------------------------

    public String mkdir(String[] args) {
        for (String dirName : args) {
            dirName = dirName.trim();
            File newDir = new File(dirName).isAbsolute() ? new File(dirName) : new File(CurrentDirectory, dirName);

            try {
                if (newDir.exists()) {
                    return "The directory '" + dirName + "' already exists.";
                } else {
                    if (!newDir.mkdir()) {
                        return "Error: The parent directory doesn't exist";
                    }
                }
            } catch (Exception e) {
                return "Error: Could not create directory: " + dirName + ". " + e.getMessage();
            }
        }
        return "";
    }

    // ------------------------------------------

    public void redirect(String input, String filename, boolean append) throws IOException {
        File file = new File(CurrentDirectory, filename);
        try {
            if (file.isDirectory()) {
                System.out.println("Error: file is a directory");
            } else if (!file.exists()) {
                file.createNewFile();
            }
        } catch (IOException e) {
            System.out.println("Error creating file: " + e.getMessage());
        }

        try (FileWriter fw = new FileWriter(file, append)) {
            fw.write(input + System.lineSeparator());
        } catch (IOException e) {
            System.out.println("Error writing to file: " + e.getMessage());
        }
    }

    // ------------------------------------------

    public String zip(String[] args) {
        String output = "";
        if (args.length < 2) {
            output += "Please enter the command in one of the following formats";
            output += "  zip <output.zip> <file1> <file2> ... to compress  files";
            output += "  zip -r <output.zip> <folder>         to compress folder recursively";
            return output;
        }

        boolean recursive = false;
        int startIndex = 0;

        if (args[0].equals("-r")) {
            recursive = true;
            startIndex = 1;
            if (args.length < 3) {
                return "Please enter: zip -r <output.zip> <folder>";
            }
        }

        Path outputFile = Paths.get(CurrentDirectory, args[startIndex]);

        try (ZipOutputStream zipOutputStream = new ZipOutputStream(Files.newOutputStream(outputFile))) {
            if (!recursive) {
                for (int i = startIndex + 1; i < args.length; i++) {
                    Path file = Paths.get(CurrentDirectory, args[i]);
                    if (!Files.exists(file)) {
                        output += "Couldn't find the file: " + file;
                        continue;
                    }
                    if (Files.isDirectory(file)) {
                        output += "Skip folder : " + file;
                        continue;
                    }

                    ZipEntry zipEntry = new ZipEntry(file.getFileName().toString());
                    zipOutputStream.putNextEntry(zipEntry);
                    Files.copy(file, zipOutputStream);
                    zipOutputStream.closeEntry();
                }
                return output;
            } else {
                Path folder = Paths.get(CurrentDirectory, args[startIndex + 1]);
                if (!Files.exists(folder)) {
                    return "Couldn't find the folder: " + folder;
                }
                addFileToZip(zipOutputStream, folder, folder.getParent());
                return output;
            }
        } catch (IOException e) {
            return "I/O Error: Could not compress files. " + e.getMessage();
        }
    }

    // ------------------------------------------

    private void addFileToZip(ZipOutputStream zipOut, Path file, Path basePath) throws IOException {
        if (basePath == null) {
            basePath = file.getParent();
        }

        if (Files.isDirectory(file)) {
            try (DirectoryStream<Path> stream = Files.newDirectoryStream(file)) {
                for (Path child : stream) {
                    addFileToZip(zipOut, child, basePath);
                }
            }
        } else {
            String zipEntryName = basePath.relativize(file).toString();
            zipOut.putNextEntry(new ZipEntry(zipEntryName));
            Files.copy(file, zipOut);
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
                output = "Error: Command not found!";
        }
        if (parser.outputFile != null && !parser.outputFile.isEmpty()) {
            redirect(output, parser.outputFile, parser.append);
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
