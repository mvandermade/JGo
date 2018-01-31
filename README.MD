# JGo
Java application that allows to host dedicated servers and attach human players or AI clients. Up to 2 players per game.

# Software Required:
Java Runtime is already installed
http://www.oracle.com/technetwork/java/javase/downloads/jre8-downloads-2133155.html

# Installation instructions
At the time of writing no build is yet shipped with the project.
The manner of running is via Eclipse, which you can download for free.
https://www.eclipse.org/downloads/packages/eclipse-ide-java-developers/oxygenr

Eclipse has a built-in "run from source" option.
The .java files of interest in running the program are in main/main:
- StartClient.java
- StartServer.java

After installing eclipse on your PC, follow these steps to download the files:

# Step by step
Option A: via Eclipse built-in git loader

In eclipse
File > Import (click). Git > Projects from Git (next>). Clone URI (next>).
Paste the .git link in the URI box: https://github.com/mvandermade/JGo.git
Keep pressing next, and confirm to search for existing project.

Navigate to main/main and either StartClient.java or StartServer.java
Click the green play button.
Interact with the console window that shows up.

Note: you can run multiple files at a time.
Hint: the console window can be pinned and switched between using the blue-lined screen icon.
Also: the program is terminated by clicking the red stop button.


Option B Via git
git clone https://github.com/mvandermade/JGo.git

--use any program you like

# Option C Via .zip
Download the .zip of 'final' via the GitHub Page https://github.com/mvandermade/JGo using a webbrowser
Click clone or download > Download ZIP. Then extract the file.

--use any program you like