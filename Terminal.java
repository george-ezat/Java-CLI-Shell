import java.util.Scanner;

// ==============================================

class Parser {
    String commandName;
    String[] args;

    // ------------------------------------------

    // This method will divide the input into commandName and args
    public boolean parse(String input) {
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

    // Implement each command in a method
    // public String pwd() {}

    // ------------------------------------------

    // This method will choose the suitable command method to be called
    public void chooseCommandAction() {
    }

    // ------------------------------------------

    public static void main(String[] args) {
    }
}

// ==============================================