- [Project Gitlet](#project-gitlet)
  * [Introduction](#introduction)
  * [Usage](#usage)
  * [Command Descriptions](#command-descriptions)
    + [1. Initialize Repository (`init`)](#1-initialize-repository---init--)
    + [2. Add File (`add`)](#2-add-file---add--)
    + [3. Commit Changes (`commit`)](#3-commit-changes---commit--)
    + [4. Remove File (`rm`)](#4-remove-file---rm--)
    + [5. View Commit History (`log`)](#5-view-commit-history---log--)
    + [6. View All Commits (`global-log`)](#6-view-all-commits---global-log--)
    + [7. Find Commits by Message (`find`)](#7-find-commits-by-message---find--)
    + [8. Check Repository Status (`status`)](#8-check-repository-status---status--)
    + [9. Checkout Files or Branches (`checkout`)](#9-checkout-files-or-branches---checkout--)
    + [10. Create a Branch (`branch`)](#10-create-a-branch---branch--)
    + [11. Remove a Branch (`rm-branch`)](#11-remove-a-branch---rm-branch--)
    + [12. Reset to a Commit (`reset`)](#12-reset-to-a-commit---reset--)
    + [13. Merge Branches (`merge`)](#13-merge-branches---merge--)
    + [Additional Notes](#additional-notes)
  * [Implementation](#implementation)
  * [Testing](#testing)


# Project Gitlet

CS 61B Project 2 Website: [Gitlet](https://sp21.datastructur.es/materials/proj/proj2/proj2)


## Introduction
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
java gitlet.Main log
java gitlet.Main status
java gitlet.Main checkout [file name]
java gitlet.Main checkout [commit id] [file name]
java gitlet.Main checkout [branch name]
java gitlet.Main branch [branch name]
```

## Command Descriptions

### 1. Initialize Repository (`init`)

* **Usage:** `java gitlet.Main init`
* **Description:** Sets up a new Gitlet version control system in the current directory. This creates an initial setup with a "master" branch and an initial commit.
* **Note:** If a Gitlet system already exists in the directory, this command will not proceed.

### 2. Add File (`add`)

* **Usage:** `java gitlet.Main add [file name]`
* **Description:** Prepares the specified file for inclusion in the next commit by adding it to the staging area. If the file hasn't changed since the last commit, it won't be added.
* **Note:** Only one file can be added at a time.

### 3. Commit Changes (`commit`)

* **Usage:** `java gitlet.Main commit [message]`
* **Description:** Saves a snapshot of currently tracked files in the staging area, creating a new commit. This commit will include any new files added or existing files updated since the last commit.
* **Note:** Each commit must have a unique message.

### 4. Remove File (`rm`)

* **Usage:** `java gitlet.Main rm [file name]`
* **Description:** Removes a file from the staging area or marks it for removal in the next commit if it is currently being tracked.
* **Note:** This action is irreversible.

### 5. View Commit History (`log`)

* **Usage:** `java gitlet.Main log`
* **Description:** Displays the history of commits starting from the current head commit back to the initial commit.
* **Note:** This only shows the history for the current branch.

### 6. View All Commits (`global-log`)

* **Usage:** `java gitlet.Main global-log`
* **Description:** Shows information about all commits made in the repository, irrespective of branches.
* **Note:** The order of commits is not specified.

### 7. Find Commits by Message (`find`)

* **Usage:** `java gitlet.Main find [commit message]`
* **Description:** Lists the IDs of all commits that have the given commit message.
* **Note:** Useful for locating specific changes.

### 8. Check Repository Status (`status`)

* **Usage:** `java gitlet.Main status`
* **Description:** Shows the current status of the repository, including branches, staged files, and files marked for removal.
* **Note:** Does not display changes not staged for commit.

### 9. Checkout Files or Branches (`checkout`)

* **Usage:**
    * To restore a file: `java gitlet.Main checkout -- [file name]`
    * To restore a file from a specific commit: `java gitlet.Main checkout [commit id] -- [file name]`
    * To switch branches: `java gitlet.Main checkout [branch name]`
* **Description:** Restores files to their state at a specific commit or switches to another branch.
* **Note:** Overwrites files in the working directory.

### 10. Create a Branch (`branch`)

* **Usage:** `java gitlet.Main branch [branch name]`
* **Description:** Creates a new branch pointing to the current commit.
* **Note:** Does not switch to the new branch automatically.

### 11. Remove a Branch (`rm-branch`)

* **Usage:** `java gitlet.Main rm-branch [branch name]`
* **Description:** Deletes a branch.
* **Note:** You cannot remove the branch you are currently on.

### 12. Reset to a Commit (`reset`)

* **Usage:** `java gitlet.Main reset [commit id]`
* **Description:** Restores all files to their state at the specified commit and moves the current branch pointer to it.
* **Note:** Clears the staging area.

### 13. Merge Branches (`merge`)

* **Usage:** `java gitlet.Main merge [branch name]`
* **Description:** Merges changes from the specified branch into the current branch, applying various rules based on the changes made in each branch.
* **Note:** Can lead to merge conflicts that need to be resolved manually.

### Additional Notes

* **Failure Cases:** Each command has specific conditions under which it will not execute, often related to the state of the file system or the repository's history.
* **Dangerous Commands:** Some commands, like `rm`, `reset`, and `merge`, can alter your repository in significant ways. Use them carefully.

## Implementation

## Testing


