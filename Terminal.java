import java.util.Arrays;
import java.util.Scanner;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;

import static java.nio.file.StandardCopyOption.*;
// ==============================================
import java.nio.file.Files;

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

    // ------------------------------------------

    public void pwd() {
        if (parser.args.length != 0) {
            System.out.println("Error: command takes no argument.");
            return;
        }
        System.out.println(System.getProperty("user.dir"));
    }

    // ------------------------------------------

    public void ls() {
        if (parser.args.length != 0) {
            System.out.println("Error: command takes no argument");
            return;
        }

        File current_directory = new File(System.getProperty("user.dir"));
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

        File fileToRemove = new File(System.getProperty("user.dir"), args[0]);
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

        String argument = args[0];
        File current_directory = new File(System.getProperty("user.dir"));
        File[] filesAndDirectories = current_directory.listFiles();

        if ("*".equals(argument)) {
            for (File item : filesAndDirectories) {
                if (item.isDirectory() && item.listFiles().length == 0) {
                    item.delete();
                }
            }
        } else {
            File directory = new File(argument);
            if (directory.exists()) {
                if (directory.isDirectory()) {
                    if (directory.listFiles().length == 0) {
                        directory.delete();
                    } else {
                        System.out.println("Error: Directory is not empty!");
                    }
                } else {
                    System.out.println("Error: '" + argument + "' is a file, not a directory!");
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

        File zipFile = new File(args[0]);
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
            destDir = new File(args[2]);
        } else {
            destDir = new File(System.getProperty("user.dir"));
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
            System.out.println("Unzip completed.");
        } catch (Exception e) {
            System.out.println("Error: " + e.getMessage());
        }
    }

    // ------------------------------------------
    
    public void cp (String[] args){
        if (args.length != 2) {
            System.out.println("Error: command requires exactly two arguments.");
            return;
        }
    
        File source = new File(System.getProperty("user.dir"), args[0]);
        File des = new File(System.getProperty("user.dir"), args[1]);

        
        if (source.isDirectory()) {
            if (des.isDirectory()){
                System.out.println("Error: This command for copy a file not directory.");
            }else{
                System.out.println("Error: Can't copy directory to file.");
            }
        } else if (source.exists()) {
            try{
            Files.copy(source.toPath(), des.toPath(), REPLACE_EXISTING);
            System.out.println("File copied successfully to "+ des.getPath());
            }
            catch (IOException e) {
                System.out.println("Error: " + e.getMessage());
                
            }
        } else {
            System.out.println("Error: This file does not exist!");
        }
    }

    // ------------------------------------------

    public void cpr (String[] args){
        System.out.println("Under development");
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
				if (parser.args.length > 0 && parser.args[0].equals("-r")) {
					cpr(parser.args);
					break;
				} else {
					cp(parser.args);
					break;
				}
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
            System.out.printf("%s $ ", System.getProperty("user.dir"));
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