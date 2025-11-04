# Java Command Line Interpreter (CLI)

This project is a simple Command Line Interpreter (CLI) built in Java for the **Operating Systems** course assignment. It was developed in the **first term** of the **Third College Year (2025)** as a group assignment at **Cairo University, Faculty of Computers & Artificial Intelligence**. It simulates a basic terminal shell by parsing user input and executing a variety of file and directory manipulation commands without using the `exec` system call.

---

## 👥 Team & Contributions

The team members and their specific contributions to the project are listed below.

| Name                     | Tasks Completed                          |
| ------------------------ | ---------------------------------------- |
| **Doha Fathy Refaey**    | Implemented `touch`, `mkdir`, and `zip`. |
| **Marym Ali Abdelkarym** | Implemented `wc`, and `cat`              |
| **Nagham Sabry Ahmed**   | Implemented `>`, `>>`, and `echo`.       |
| **George Ezzat Hosni**   | Implemented `pwd`, `ls`, `rm`, `cd`, `rmdir`, and `unzip`.<br>**Led all code refactoring, unified path resolution, and standardized I/O & error handling.** |
| **Kerolus Akram Madi**   | Implemented `cp`, and `cp -r`.           |

---

## ✨ Features Implemented

This CLI supports a range of essential shell commands:

- **Navigation & Inspection:**
  - `pwd`: Prints the current working directory.
  - `cd [path]`: Changes the current directory. Supports `..`, home directory (no arguments), and absolute/relative paths.
  - `ls`: Lists the contents of the current directory, sorted alphabetically.

- **Directory Manipulation:**
  - `mkdir [dir_name(s)]`: Creates one or more new directories.
  - `rmdir [dir_name]`: Removes an empty directory. Supports `*` to remove all empty directories in the current location.

- **File Manipulation:**
  - `touch [file_path]`: Creates a new empty file.
  - `cp [source_file] [destination_file]`: Copies the content of one file to another.
  - `cp -r [source_dir] [destination_dir]`: Recursively copies a directory and its contents.
  - `rm [file_name]`: Removes a file.
  - `cat [file1] [file2_optional]`: Prints the content of one file or concatenates and prints the content of two files.
  - `wc [file_name]`: Counts the lines, words, and characters in a file.
  - `echo [text]`: Prints the provided text to the console (useful for redirection).

- **I/O Redirection:**
  - `> [file_name]`: Redirects the output of a command to a file, overwriting its content.
  - `>> [file_name]`: Appends the output of a command to a file.

- **Archive Commands:**
  - `zip [-r] [archive_name] [file/dir]`: Compresses files or directories into a .zip archive.
  - `unzip [archive_name] [-d destination]`: Extracts files from a .zip archive.

- **Control:**
  - `exit`: Terminates the CLI application.

---

## 🚀 Code Enhancements & Refactoring

Beyond the initial assignment requirements, the codebase was significantly enhanced to implement professional design patterns for robustness, consistency, and maintainability.

- **Advanced Path Handling:** A key design feature is the `resolveFile(String path)` helper method. This centralizes all path logic, enabling _all_ file-system commands (like `rm`, `cd`, `cp`, `zip`) to seamlessly support both **absolute and relative paths**. This creates a robust and user-friendly experience that mimics professional shell behavior.

- **Modern I/O API Standardization:** The project was refactored to use the modern `java.nio.file.Files` API (e.g., `Files.copy`) for high-level I/O operations across commands like `cp`, `zip`, and `unzip`. This design **promotes code consistency**, **improves performance**, and **reduces redundancy** by leveraging optimized, built-in Java functions.

- **Robust Error & Redirection Logic:** Error handling was standardized across all commands to provide clear, consistent feedback. The I/O redirection logic (`>` and `>>`) was engineered to be fully integrated with this system, allowing both standard command output _and_ error messages to be correctly redirected to files—a critical feature of real-world terminals.

- **High-Fidelity Command Behavior:** The logic for commands like `wc` and `cat` was meticulously aligned with the argument-handling and output behavior of their standard POSIX counterparts. This ensures that users familiar with Linux or macOS terminals will find the CLI's behavior intuitive and correct.

---

## 🏗️ Project Structure

The application is built around two main classes as required by the assignment specifications:

- `Parser.java`: This class is responsible for taking the raw string input from the user and breaking it down into a command name, an array of arguments, and any I/O redirection.

- `Terminal.java`: This class contains the core logic. It holds the implementations for each command, helper methods (like `resolveFile`), and a `chooseCommandAction` method to select and execute the correct function based on the parsed input.

---