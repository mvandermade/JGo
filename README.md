# JGo
Java application that allows to host dedicated servers and attach human players or AI clients. Up to 2 players per game.

Go is a game for two players in which the objective is to gain as much territory as possible.
Territory is acknowledged to a player by surrounding the opponent with their 'stones' by placing them turn-by-turn.
Rules followed here are:
- Board size variable (19x19) is considered as a lengthy game.
- "Beginner rules" are applied: https://en.wikipedia.org/wiki/Rules_of_Go#Reference_statement

# Software Required:
Java Runtime is already installed
http://www.oracle.com/technetwork/java/javase/downloads/jre8-downloads-2133155.html

# Load the repository to the local disk
Make sure Java is configured as the default launcher of the Operating system.

Option A Via git client in command line/Terminal:
git clone https://github.com/mvandermade/JGo

OR

Option B Via .zip
Download the .zip of 'final' via the GitHub Page https://github.com/mvandermade/JGo using a webbrowser
Click clone or download > Download ZIP. Then extract the file.

# Running the Server
java -jar Server.jar

Will start a server on port 8585
Note: windows users can use the Server.bat file
# Running the Client
java -jar Client.jar

Note: windows users can use the Client.bat file
# Changing programs
It is recommended to use an IDE to handle compiling
Then running your own version is as above.