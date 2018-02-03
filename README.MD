# JGo
Java application that allows to host dedicated servers and attach human players or AI clients. Up to 2 players per game.

# Software Required:

Java Runtime is already installed

http://www.oracle.com/technetwork/java/javase/downloads/jre8-downloads-2133155.html


OR (Preffered by me personally, if you don't want to install java)

Download Java Portable

https://portableapps.com/apps/utilities/java_portable

Follow the instructions to extract the program (you can put it on USB, no need to install).


# User instructions
If you have Java installed AND added to the windows PATH you can run the .bat files in any directory.

Otherwise via javaPortable (unpacked in <install_location of javaPortable> ):


Look for the folder <install_location of javaPortable> /bin/.

Check that you can see the java.exe in this /bin/ folder.

Download the .zip of 'final' via the GitHub Page https://github.com/mvandermade/JGo using a webbrowser.

Click clone or download > Download ZIP. 

Then from JGo.zip extract the folder 'runnable' in /bin/.

You now have the folder /bin/:

-----------------/Java/bin/--------------
.

..

<other files>
  
java.exe         < 'Java runtime' application 

StartServer.jar  < Java application

StartClient.jar  < Java application

StartClient.bat  < Windows executable script

StartServer.bat  < Windows executable script

<other files>
  
-----------------------------------------
  
The .bat files contain the command line instructions to pass either StartClient.jar or StartServer.jar to java.exe.

It is therefore important that the .bat and .jar are in the same folder as java.exe.

*(You can either create a shortcut to- or modify the .bat file to your own folder structuring.)*


Run the StartServer.bat

- Keep pressing enter if you want the default settings

Run the StartClient.bat

- Enter your player name

*(to enable AI to type moves for you name your player: AIAI or AIAI2).*

The yellow ball is a 'hint' provided by the AI, and is always enabled at this time.


Run multiple instances of StartClient.bat on any PC in your local network.

# Developer instructions

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


Option C Via .zip

Download the .zip of 'final' via the GitHub Page https://github.com/mvandermade/JGo using a webbrowser

Click clone or download > Download ZIP. Then extract the file.

--use any program you like
