-----------------------
Configfile options:
Config entries follow the "key = value" format.
-dominionsExecutablePath: Path to the Dominions executable. Default: C:\Program Files (x86)\Steam\steamapps\common\Dominions5\Dominions5.exe
-archiveNameShema: shema for directories of archived games %name% is replaced with the game name, %turn% is replaced with the turn number
-archiveTurnNumberMinimumlength: The minimum amount of characters used to denote turn number in archived games. Impacts sorting in the list ingame. Default:2
-saveDirectoryPath: Path to the savedgames-directory if non-default
/Currently not active/-extractMapFiles: [true/false] wether map files hall be extracted and moved to the maps-directory, Default: false
-mapDirectoryPath: Path to the maps-directory if non-default, only used if extractMapFiles=true
/Currently not active/-readyArchiveDuration: [number] maximum number of turn files held at ready for immediate access in the savedgames directory, use -1 for all, Default: -1
/Currently not active/-useLongTermStorage: [true/false] determines what is done with turns outdated according to readyArchiveDuration. True means they will be moved to another directory, false means they will be unrecoverably deleted, CAUTION, Default: false
-longTermStorageDirectory: Path to the directory where outdated turns shall be archived. Only used when useLongTermStorage=true
-----------------------
Command line Arguments (seperated, for two use '-a -l', not '-al'):
-a Do not launch Dominions
-l Create a log file
-----------------------
White-/Blacklist:
You probably don't want to archive every game you have as that will just bloat the list. You can use either a white- or a blacklist to limit archived games. If you use a whitelist only games on this list will be archived, if you opt to use a blacklist all games which are NOT on this blacklist will be archived instead. (If a game is on both lists Blacklist takes precedence.)
To create a whitelist create in the directory of the Dominions5TurnArchiver.jar a file named "Whitelist.txt". In it list the names of all games you want to archive, seperated by blank or newline. This is case-sensitive. Replace blanks in the gamename with underscores.
Creating a blacklist is exactly the same as a whitelist, except the file has to be named "Blacklist.txt".
-----------------------
Using this on a local server to archive all turns:
Take a look at preexec.bat in a texteditor. Copy it, replace the paths to match the respective ones on your machine.
Replace the "testgamename" at the end with whatever name you want. Having this match an existing game is how you continue an already existing game btw. 
Execute your new .bat file and set up the server as usual.
If you are interested take a look at the Technical Info for Dominions 5 manual.
-----------------------
STEAM:
As this program starts Dominions directly Steam is not involved. If for some reason you want Steam to know you are playing Dominions there is a dom5ArchiverWithoutGame.bat which starts the archiving function of this program without starting Dominions (it just executes the program with a corresponding option). You can start Domionions using Steam and when you are finished execute the bat manually. 
-----------------------
DOMINIONS 4:
This should work with Dominions4 if you adjust the paths accordingly without any problems. But I haven't tested so no guarantees (not that there are any guarantees anywhere else, but you know).
Probably even for older iterations as long as Illwinter didn't change .trn formats or directory structure, but *shrug* even less than no guarantee.
