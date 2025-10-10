import java.util.Scanner;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
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

    // ------------------------------------------

    public void rmdir(String[] args) {
        if (args.length != 1) {
            System.out.println("Error: Invalid Args length!");
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
                    System.out.println("Error: '" + argument + "'' is a file, not a directory!");
                }
            } else
                System.out.println("Error: Directory does not exists");
        }
    }

    // ------------------------------------------

    public void unzip(String[] args) {
        if (args.length != 1 && args.length != 3) {
            System.out.println("Error: Invalid arguments for unzip.");
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

    // This method will choose the suitable command method to be called
    public void chooseCommandAction() {
        switch (parser.commandName) {
            case "rmdir":
                rmdir(parser.args);
                break;
            case "unzip":
                unzip(parser.args);
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