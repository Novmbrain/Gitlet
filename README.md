- [Project Gitlet](#project-gitlet)
  * [Introduction](#introduction)
  * [Usage](#usage)
  * [Command Descriptions](#command-descriptions))
  * [Implementation](#implementation)
  * [Testing](#testing)


# Project Gitlet
## Introduction
CS 61B Project 2 Website: [Gitlet](https://sp21.datastructur.es/materials/proj/proj2/proj2)

This project is a simplified version of the popular version control system, Git. It is a command-line program that allows users to create, modify, and view snapshots of their files. It also allows users to view the history of their files and revert to previous versions of their files.

## Usage

To use Gitlet, you must first compile the program by running the following command in the terminal:
```
javac gitlet/Main.java
```
After compiling the program, you can run the program by running the following command in the terminal:

You can then use the following commands to interact with Gitlet:
```
java gitlet.Main init
java gitlet.Main add [file name]
java gitlet.Main commit [message]
java gitlet.Main rm [file name]
java gitlet.Main rm-branch [branch name]
java gitlet.Main log
java gitlet.Main global-log
java gitlet.Main status
java gitlet.Main checkout [file name]
java gitlet.Main checkout [commit id] [file name]
java gitlet.Main checkout [branch name]
java gitlet.Main branch [branch name]
java gitlet.Main find [commit message]
java gitlet.Main reset [commit id]
java gitlet.Main merge [branch name]
```

A more detailed description of each command can be found in the official website of course [CS61B Spring2021](https://sp21.datastructur.es/materials/proj/proj2/proj2)


## Implementation

## Testing
A series of tests were written to test the behavior of the Gitlet program. 

Backend by these tests, the refactoring work could be done easily and safely. The tests are located in the `proj2/testing/student_tests` directory and can be exctued by running the following command in the terminal under the `proj2/testing` directory:

```
make check
```

## To go further

One spec that I have not implemented is the remote repository. The following is a list of features that I would like to implement in the future:

- [ ] Add a remote repository
- [ ] Push changes to a remote repository
- [ ] Pull changes from a remote repository
- [ ] Clone a remote repository



