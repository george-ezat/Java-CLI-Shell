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
import java.io.BufferedReader;
import java.util.zip.ZipOutputStream;

import static java.nio.file.StandardCopyOption.*;
// ==============================================

class Parser {
    String commandName;
    String[] args;

    // ------------------------------------------

    // This method will divide the input into commandName and args
    public boolean parse(String input) {
        String[] input_parts = input.trim().split("\\s+");

        if (input_parts.length > 0) {
            commandName = input_parts[0];
            args = new String[input_parts.length - 1];
            System.arraycopy(input_parts, 1, args, 0, input_parts.length - 1);
            return true;
        }

        return false;
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

    public void pwd() {
        if (parser.args.length != 0) {
            System.out.println("Error: command takes no argument.");
            return;
        }
        System.out.println(CurrentDirectory);
    }

    // ------------------------------------------

    public void ls() {
        if (parser.args.length != 0) {
            System.out.println("Error: command takes no argument");
            return;
        }

        File current_directory = new File(CurrentDirectory);
        File[] filesAndDirectories = current_directory.listFiles();
        String[] names = new String[filesAndDirectories.length];

        for (short i = 0; i < filesAndDirectories.length; i++) {
            names[i] = filesAndDirectories[i].getName();
        }

        Arrays.sort(names);
        System.out.println(String.join("\t", names));
    }

    // ------------------------------------------

    public void rm(String[] args) {
        if (args.length != 1) {
            System.out.println("Error: command requires exactly one argument (file name or path).");
            return;
        }

        File fileToRemove = new File(CurrentDirectory, args[0]);
        if (fileToRemove.isDirectory()) {
            System.out.println("Error: It is a directory not a file!");
        } else if (fileToRemove.exists()) {
            fileToRemove.delete();
        } else {
            System.out.println("Error: This file does not exist!");
        }
    }

    // ------------------------------------------

    public void rmdir(String[] args) {
        if (args.length != 1) {
            System.out.println("Error: command requires exactly one argument (directory name).");
            return;
        }

        File current_directory = new File(CurrentDirectory);
        File[] filesAndDirectories = current_directory.listFiles();

        if ("*".equals(args[0])) {
            for (File item : filesAndDirectories) {
                if (item.isDirectory() && item.listFiles().length == 0) {
                    item.delete();
                }
            }
        } else {
            File directory = new File(CurrentDirectory, args[0]);
            if (directory.exists()) {
                if (directory.isDirectory()) {
                    if (directory.listFiles().length == 0) {
                        directory.delete();
                    } else {
                        System.out.println("Error: Directory is not empty!");
                    }
                } else {
                    System.out.println("Error: '" + args[0] + "' is a file, not a directory!");
                }
            } else
                System.out.println("Error: Directory does not exist!");
        }
    }

    // ------------------------------------------

    public void unzip(String[] args) {
        if (args.length != 1 && args.length != 3) {
            System.out.println("Error: Invalid arguments!");
            System.out.println("Usage: unzip <file.zip> [-d <destination_directory>]");
            return;
        }

        File zipFile = new File(CurrentDirectory, args[0]);
        File destDir;

        if (!zipFile.getName().endsWith(".zip")) {
            System.out.println("Error: It is not a zip file!");
            return;
        }

        if (!zipFile.exists()) {
            System.out.println("Error: Zip file does not exist!");
            return;
        }

        // Determine the destination directory
        if (args.length == 3) {
            if (!args[1].equals("-d")) {
                System.out.println("Error: Invalid flag. Use '-d' for destination.");
                return;
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
        } catch (Exception e) {
            System.out.println("Error: " + e.getMessage());
        }
    }

    // ------------------------------------------

    public void cp(String[] args) {
        if (args.length > 0 && args[0].equals("-r")) {
            cpr(args);
        } else {
            if (args.length != 2) {
                System.out.println("Error: command requires exactly two arguments.");
                return;
            }

            File source = new File(CurrentDirectory, args[0]);
            File des = new File(CurrentDirectory, args[1]);

            if (source.isDirectory() || des.isDirectory()) {
                System.out.println("Error: arguments must be files.");
                return;
            } else if (source.exists()) {
                try {
                    Files.copy(source.toPath(), des.toPath(), REPLACE_EXISTING);
                } catch (IOException e) {
                    System.out.println("Error: " + e.getMessage());
                }
            } else {
                System.out.println("Error: source file does not exist!");
            }
        }
    }

    // ------------------------------------------

    public void cpr(String[] args) {
        if (args.length != 3) {
            System.out.println("Error: command requires exactly two arguments.");
            return;
        }

        File source = new File(CurrentDirectory, args[1]);
        File des = new File(CurrentDirectory, args[2]);

        if (!source.isDirectory() || !des.isDirectory()) {
            System.out.println("Error: arguments must be directories.");
        } else if (source.exists()) {
            try {
                cp_directory(source, new File(des, source.getName()));
            } catch (IOException e) {
                System.out.println("Error: " + e.getMessage());
            }
        } else {
            System.out.println("Error: source directory does not exist!");
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

    public void cat(String[] args) {
        if (args.length != 1 && args.length != 2) {
            System.out.println("Error: command takes 1 or 2 arguments");
            return;
        }
        if (args.length == 1) {
            File file = new File(CurrentDirectory, args[0]);

            if (!file.exists()) {
                System.out.println("Error: file not found!");
                return;
            }

            if (file.isDirectory()) {
                System.out.println("Error: " + args[0] + " is a directory, not a file!");
                return;
            }

            try (BufferedReader br = new BufferedReader(new FileReader(file))) {
                String line;
                while ((line = br.readLine()) != null) {
                    System.out.println(line);
                }
            } catch (IOException e) {
                System.out.println("Error reading file: " + e.getMessage());
            }

        } else {
            File file1 = new File(CurrentDirectory, args[0]);
            File file2 = new File(CurrentDirectory, args[1]);

            if (!file1.exists() || !file2.exists()) {
                System.out.println("Error: one or both files not found!");
                return;
            }

            if (file1.isDirectory() || file2.isDirectory()) {
                System.out.println("Error: one or both arguments are directories, not files!");
                return;
            }

            try (
                    BufferedReader br1 = new BufferedReader(new FileReader(file1));
                    BufferedReader br2 = new BufferedReader(new FileReader(file2))) {
                String line;
                while ((line = br1.readLine()) != null) {
                    System.out.println(line);
                }
                while ((line = br2.readLine()) != null) {
                    System.out.println(line);
                }
            } catch (IOException e) {
                System.out.println("Error reading files: " + e.getMessage());
            }
        }
    }

    // ------------------------------------------

    public void cd(String[] args) {
        if (args.length != 0 && args.length != 1) {
            System.out.println("Error: command takes 1 argument or not take anything");
        } else if (args.length == 0) {
            CurrentDirectory = HOME_DIRECTORY;
        } else if (args.length == 1 && args[0].equals("..")) {
            int lastslsh = CurrentDirectory.lastIndexOf("\\");
            if (lastslsh == -1 || CurrentDirectory.equals("C:")) {
                System.out.println("Already at the root directory");
            } else {
                String parent = CurrentDirectory.substring(0, lastslsh);
                CurrentDirectory = parent;
            }
        } else {
            if (args[0].contains("\\")) {
                File f = new File(args[0]);
                if (f.exists() && f.isDirectory()) {
                    CurrentDirectory = args[0];
                } else {
                    System.out.println("Error: directory not found.");
                }
            } else {
                if (args[0].contains("\\")) {
                    // Absolute path
                    File f = new File(args[0]);
                    if (f.exists() && f.isDirectory()) {
                        CurrentDirectory = args[0];
                    } else {
                        System.out.println("Error: directory not found.");
                    }
                } else {
                    // short
                    String newPath = CurrentDirectory + "\\" + args[0];
                    File f = new File(newPath);
                    if (f.exists() && f.isDirectory()) {
                        CurrentDirectory = newPath;
                    } else {
                        System.out.println("Error: directory not found.");
                    }
                }
            }
        }
    }

    // ------------------------------------------

    public void wc(String[] args) {
        if (args.length != 1) {
            System.out.println("Error: command takes exactly one argument (filename).");
            return;
        }

        File file = new File(CurrentDirectory, args[0]);
        if (!file.exists() || file.isDirectory()) {
            System.out.println("Error: file not found or is a directory!");
            return;
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

            System.out.printf("%d %d %d %s\n", lines, words, characters, file.getName());

        } catch (IOException e) {
            System.out.println("Error reading file: " + e.getMessage());
        }
    }

    // ------------------------------------------


    public void touch (String[] args){
        String filePath = args[0];
        File file = new File(filePath);
        try {
            if (file.exists()) {
                boolean success = file.setLastModified(System.currentTimeMillis());
                if (success) {
                    System.out.println("Timestamp updated successfully for: " + filePath);
                } else {
                    System.out.println("Error: Could not update Timestamp for: " + filePath);
                }
            } else {
                if (file.createNewFile()) {
                    System.out.println("File created successfully!: " + filePath );
                } else {
                    System.out.println("Error: File could not be created: " + filePath);
                }
            }
        } catch (IOException e) {
            System.out.println("I/O Error: Could not create or update the file: " + filePath);
        }
    }

    public void mkdir (String[] args){
        for (String dirName : args){
            dirName = dirName.trim();
            Path currentDir = Path.of(System.getProperty("user.dir"));
            Path newDir = currentDir.resolve(dirName);

            try {
                if (Files.exists(newDir)){
                    System.out.println("The directory \"" + dirName + "\" already exists.");
                } else{
                    Files.createDirectories(newDir);
                    System.out.println("Directory created Successfully: " + dirName);
                }
            } catch (IOException e){
                System.out.println("Error: Could not create directory: " + dirName + ". " + e.getMessage());
            }
        }
    }

    // ------------------------------------------

    public void zip(String[] args) {
        if (args.length < 2) {
            System.out.println("Please enter the command in one of the following formats");
            System.out.println("  zip <output.zip> <file1> <file2> ... to compress  files");
            System.out.println("  zip -r <output.zip> <folder>         to compress folder recursively");
            return;
        }

        boolean recursive = false;
        int startIndex = 0;

        if (args[0].equals("-r")) {
            recursive = true;
            startIndex = 1;
            if (args.length < 3) {
                System.out.println("Please enter: zip -r <output.zip> <folder>");
                return;
            }
        }

        Path outputFile = Paths.get(args[startIndex]);


        try (ZipOutputStream zipOutputStream = new ZipOutputStream(Files.newOutputStream(outputFile))) {
            if (!recursive) {
                for (int i = startIndex + 1; i < args.length; i++) {
                    Path file = Paths.get(args[i]);
                    if (!Files.exists(file)) {
                        System.out.println("Couldn't find the file: " + file);
                        continue;
                    }
                    if (Files.isDirectory(file)) {
                        System.out.println("Skipp folder : " + file);
                        continue;
                    }

                    ZipEntry zipEntry = new ZipEntry(file.getFileName().toString());
                    zipOutputStream.putNextEntry(zipEntry);
                    Files.copy(file, zipOutputStream);
                    zipOutputStream.closeEntry();
                }
            }
            else {
                Path folder = Paths.get(args[startIndex + 1]);
                if (!Files.exists(folder)) {
                    System.out.println("Couldn't find the folder: " + folder);
                    return;
                }
                addFileToZip(zipOutputStream, folder, folder.getParent());
            }

            System.out.println("Files compressed successfully into: " + outputFile.toAbsolutePath());

        } catch (IOException e) {
            System.out.println("I/O Error: Could not compress files. " + e.getMessage());
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
        }
        else {
            String zipEntryName = basePath.relativize(file).toString();
            zipOut.putNextEntry(new ZipEntry(zipEntryName));
            Files.copy(file, zipOut);
            zipOut.closeEntry();
        }
    }

    // ------------------------------------------

    // This method will choose the suitable command method to be called
    public void chooseCommandAction() {
        switch (parser.commandName) {
            case "pwd":
                pwd();
                break;
            case "ls":
                ls();
                break;
            case "rm":
                rm(parser.args);
                break;
            case "rmdir":
                rmdir(parser.args);
                break;
            case "unzip":
                unzip(parser.args);
                break;
            case "cp":
                cp(parser.args);
                break;
            case "cat":
                cat(parser.args);
                break;
            case "cd":
                cd(parser.args);
                break;
            case "wc":
                wc(parser.args);
                break;
            case "touch":
                touch(parser.args);
                break;
            case "mkdir":
                mkdir(parser.args);
                break;
            case "zip":
                zip(parser.args);
                break;
            default:
                System.out.println("Error: Command not found!");
        }
    }

    // ------------------------------------------

    public static void main(String[] args) {
        Terminal terminal = new Terminal();
        Scanner in = new Scanner(System.in);
        String input;

        while (true) {
            System.out.printf("%s $ ", CurrentDirectory);
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