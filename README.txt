

-----------------------
Config Options:
REQUIRED:
-dominionsExecutablePath: Path to the Dominions executable

OPTIONAL:
-archiveNameShema: shema for directories of archived games %name% is replaced with the game name, %turn% is replaced with the turn number
-archiveTurnNumberMinimumlength: The minimum amount of characters used to denote turn number in archived games. Impacts sorting in the list ingame. Default:2
-saveDirectoryPath: Path to the savedgames-directory if non-default
/Currently not active/-extractMapFiles: [true/false] wether map files hall be extracted and moved to the maps-directory, Default: false
-mapDirectoryPath: Path to the maps-directory if non-default, only used if extractMapFiles=true
/Currently not active/-readyArchiveDuration: [number] maximum number of turn files held at ready for immediate access in the savedgames directory, use -1 for all, Default: -1
/Currently not active/-useLongTermStorage: [true/false] determines what is done with turns outdated according to readyArchiveDuration. True means they will be moved to another directory, false means they will be unrecoverably deleted, CAUTION, Default: false
-longTermStorageDirectory: Path to the directory where outdated turns shall be archived. Only used when useLongTermStorage=true
-----------------------