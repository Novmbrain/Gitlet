# Project Gitlet

## Introduction
This project is a simplified version of the popular version control system, Git. It is a command-line program that allows users to create, modify, and view snapshots of their files. It also allows users to view the history of their files and revert to previous versions of their files.

## Usage
To use Gitlet, you must first compile the program by running the following command in the terminal:
```
javac gitlet/Main.java
```
After compiling the program, you can run the program by running the following command in the terminal:
```
java gitlet.Main
```
You can then use the following commands to interact with Gitlet:
```
java gitlet.Main init
java gitlet.Main add [file name]
java gitlet.Main commit [message]
java gitlet.Main rm [file name]
java gitlet.Main log
java gitlet.Main status
