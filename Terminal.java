import java.util.Scanner;
import java.io.File;

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
            args = new String [input_parts.length - 1];
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

    // This method will choose the suitable command method to be called
    public void chooseCommandAction() {
        switch (parser.commandName) {
            case "rmdir":
                rmdir(parser.args);
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