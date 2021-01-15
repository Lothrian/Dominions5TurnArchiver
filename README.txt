QUICKSTART:
Execute Dominions5TurnArchiver.jar. Dominions should now be launched. That's it, have fun, archiving shall occur once you exit the game again.


-----------------------
What is this:
- This is a small Java app (and a few additional auxilliary scripts) to automate archiving Dominions turns.
- Archived turns will be copied as new saves, so you will always have immediate access to all archived turns from ingame.
- To use this you will have to start the app (or one of the various specialised .bat scripts, see the section below for more details).
- You can think of this as an additional container around the game executable. You can still start the Dominions.exe directly or through Steam like usual,  
    but won't get the archiving feature then.
- I personally recommend setting up a whitelist to only archive the games you are interested in so as to not flood your list. See section "White-/Blacklist".


-----------------------
The various scripts:
This comes packaged with a selection of scripts to ease use for the common user. They don't do anything special, just start the "real" app with
    the appropriate options for their purpose.
- dom5ArchiverDefault: Simply startrts the app. Equivalent to executing the .jar file. Mostly there out of principle.
- dom5ArchiverWithLog: Writes a log file for the app. Mainly for debugging.
- dom5ArchiveWithoutGame: Runs the archiving function without launching the game. Useful for example when playing a blitz or singleplayer game.
- dom5ArchiverServerVersionExample.bat: An example for running this with a multyplayer server. See "Using this on a local server to archive all turns" for more details.   


-----------------------
Configfile options:
Config entries follow the "key = value" format. They are all optional. They are case-sensitive.
-dominionsExecutablePath: Path to the Dominions executable. Default: C:\Program Files (x86)\Steam\steamapps\common\Dominions5\Dominions5.exe
-archiveNameSchema: schema for directories of archived games %name% is replaced with the game name, %turn% is replaced with the turn number
-archiveTurnNumberMinimumlength: The minimum amount of characters used to denote turn number in archived games. Impacts sorting in the list ingame. Default:2
-saveDirectoryPath: Path to the savedgames-directory if non-default
/Currently not active/-extractMapFiles: [true/false] wether map files hall be extracted and moved to the maps-directory, Default: false
-mapDirectoryPath: Path to the maps-directory if non-default, only used if extractMapFiles=true
/Currently not active/-readyArchiveDuration: [number] maximum number of turn files held at ready for immediate access in the savedgames directory,
     use -1 for all, Default: -1
/Currently not active/-useLongTermStorage: [true/false] determines what is done with turns outdated according to readyArchiveDuration.
     True means they will be moved to another directory, false means they will be unrecoverably deleted, CAUTION, Default: false
-longTermStorageDirectory: Path to the directory where outdated turns shall be archived. Only used when useLongTermStorage=true


-----------------------
Command line Arguments (seperated, for two use '-a -l', not '-al'):
-a Do not launch Dominions
-l Create a log file


-----------------------
White-/Blacklist:
You probably don't want to archive every game you have as that will just bloat the list. You can use either a white- or a blacklist to limit archived games.
    If you use a whitelist only games on this list will be archived, if you opt to use a blacklist all games which are NOT on this blacklist will be archived instead. 
    (If a game is on both lists Blacklist takes precedence.)
To create a whitelist create in the directory of the Dominions5TurnArchiver.jar a file named "Whitelist.txt". In it list the names of all games you want to archive, 
    seperated by blank or newline. This is case-sensitive. Replace blanks in the gamename with underscores.
Creating a blacklist is exactly the same as a whitelist, except the file has to be named "Blacklist.txt".


-----------------------
Using this on a local server to archive all turns:
Take a look at preexec.bat in a texteditor. Copy it, replace the paths to match the respective ones on your machine.
Replace the "testgamename" at the end with whatever name you want. Having this match an existing game is how you continue an already existing game btw. 
Execute your new .bat file and set up the server as usual.
If you are interested take a look at the Technical Info for Dominions 5 manual.


-----------------------
Steam:
As this program starts Dominions directly Steam is not involved. If for some reason you want Steam to know you are playing Dominions there is a
    dom5ArchiverWithoutGame.bat which starts the archiving function of this program without starting Dominions (it just executes the program with a corresponding option).
    You can start Domionions using Steam and when you are finished execute the bat manually. 


-----------------------
DOMINIONS 4:
This should work with Dominions4 if you adjust the paths accordingly without any problems. But I haven't tested so no guarantees
     (not that there are any guarantees anywhere else, but you know).
Probably even for older iterations as long as Illwinter didn't change .trn formats or directory structure, but *shrug* even less than no guarantee.
