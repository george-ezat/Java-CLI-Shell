# Java Command Line Interpreter (CLI)

This project is a simple Command Line Interpreter (CLI) built in Java for the **Operating Systems** course assignment. It simulates a basic terminal shell by parsing user input and executing a variety of file and directory manipulation commands without using the `exec` system call.

This project was developed as a group assignment at **Cairo University, Faculty of Computers & Artificial Intelligence**.

---

## 👥 Team & Contributions

The team members and their specific contributions to the project are listed below.

| Name                     | Faculty ID | Tasks Completed                                      |
| ------------------------ | ---------- | ---------------------------------------------------- |
| **Doha Fathy Refaey**    | _20230107_ | Implemented `touch`, `mkdir`, and `zip`.             |
| **Marym Ali Abdelkarym** | _20230396_ | Implemented `wc`, `cat`, and `cd`.                   |
| **Nagham Sabry Ahmed**   | _20230443_ | Implemented `>`, and `>>`.                           |
| **George Ezzat Hosni**   | _20231041_ | Implemented `pwd`, `ls`, `rm`, `rmdir`, and `unzip`. |
| **Kerolus Akram Madi**   | _20231126_ | Implemented `cp`, and `cp -r`.                       |

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
  - `rm [file_name]`: Removes a file from the current directory.
  - `cat [file1] [file2]`: Prints the content of one file or concatenates and prints the content of two files.
  - `wc [file_name]`: Counts the lines, words, and characters in a file.
- **I/O Redirection:**
  - `> [file_name]`: Redirects the output of a command to a file, overwriting its content.
  - `>> [file_name]`: Appends the output of a command to a file.
- **Archive Commands:**
  - `zip [-r] [archive_name] [file/dir]`: Compresses files or directories into a .zip archive.
  - `unzip [archive_name] [-d destination]`: Extracts files from a .zip archive.
- **Control:**
  - `exit`: Terminates the CLI application.

---

## 🏗️ Project Structure

The application is built around two main classes as required by the assignment specifications:

- `Parser.java`: This class is responsible for taking the raw string input from the user and breaking it down into a command name and an array of arguments.
- `Terminal.java`: This class contains the core logic. It holds the implementations for each command and a method to select and execute the correct function based on the parsed input.

---
