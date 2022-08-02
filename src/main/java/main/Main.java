/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package main;

import static main.Main.logWriter;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FilenameFilter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Scanner;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import javax.swing.JFrame;
import javax.swing.JOptionPane;
import java.io.StringWriter;
import java.io.PrintWriter;

/**
 *
 * @author mergele
 */
public class Main {

    public static String version = "1.4";
    public static boolean expressiveDebugs = false;

    /*
	Internals
     */
    public static Dom5ArchiverLogger logWriter;
    private File archiverJarDirectory;
    private boolean launchGame;
    private boolean createLog;

    /*
	Required Options
     */
    private File dominionsExecutablePath;

    /*
	Optional Options
     */
    public static File saveDirectoryPath;
    public static MapExtractionOption mapFileExtractionModus;
    public static File mapDirectoryPath;
    public static String archiveNameSchema;
    public static int archiveTurnNumberAppendixMinimumLength;
    public static File longTermStorageDirectory;
    public static int readyArchiveDuration = -1;
    public static LongTermStorageOption longTermStorageModus;
    public static String arbitraryDomArguments;

    private ArrayList<String> whitelist;
    private ArrayList<String> blacklist;

    public void run(boolean launchGame, boolean createLog) {
	this.launchGame = launchGame;
	this.createLog = createLog;
	/*
	    Initialise values
	 */
	this.initialiseAdmin();
	this.setConfigDefaults();
	this.readConfig();
	this.sanityCheckConfigs();

	/*
	    Execute game
	 */
	if (this.launchGame) {
	    runDominions();
	}

	/*
	    Read Turns from directories
	 */
	ArrayList<Turn> turns = readTurns();

	/*
	    Create games out of these Turns
	 */
	ArrayList<Game> games = generateGamesFromTurns(turns);
	logWriter.startNewSection("Games currently found: " + games.size());
	for (int i = 0; i < games.size(); i++) {
	    logWriter.log(games.get(i).getName()); 
	}
	
	/*
	    Go through games, Do Archiving
	 */
	try{
	    for (int i = 0; i < games.size(); i++) {
		Game game =  games.get(i);
		logWriter.startNewSection("ARCHIVING GAME " + game.getName());
		if (shallBeArchivedBasedOnBlackWhiteList(game)) {
		    game.doArchiving();
		    Main.logWriter.log("Finished archiving game " + game.getName());
		} else {
		    logWriter.log("Skipped because of black/whitelist");
		}
	    }
	    logWriter.startNewSection("Games currently found: " + games.size());
	    for (int i = 0; i < games.size(); i++) {
		logWriter.log(games.get(i).getName()); 
	    }
	    logWriter.startNewSection("FINISHED ARCHIVING SUCCESSFULLY");
	}catch(Exception e) {
	    logWriter.startNewSection("RAN INTO EXCEPTION WHILE ARCHIVING");
	    logWriter.log(e.getMessage());
	    StringWriter sw = new StringWriter();
	    PrintWriter pw = new PrintWriter(sw);
	    e.printStackTrace(pw);
	    String sStackTrace = sw.toString(); // stack trace as a string
	    logWriter.log(sStackTrace);
	    throw e;
	}
    }

    public boolean shallBeArchivedBasedOnBlackWhiteList(Game game) {
	String gameName = game.getName();
	for (int i = 0; i < blacklist.size(); i++) {
	    if (gameName.matches(blacklist.get(i))) {
		Main.logWriter.log(gameName + " matches " + whitelist.get(i) + " from blacklist");
		return false;
	    }
	}
	if (blacklist.size() > 0) {
	    Main.logWriter.log(gameName + " did not match any from blacklist");
	}

	if (whitelist.size() > 0) {
	    for (int i = 0; i < whitelist.size(); i++) {
		if (gameName.matches(whitelist.get(i))) {
		    Main.logWriter.log(gameName + " matches " + whitelist.get(i) + " from whitelist");
		    return true;
		}
	    }
	    Main.logWriter.log(gameName + " did not match any from whitelist");
	    return false;
	}

	Main.logWriter.log("No white/blacklist set");
	return true;
    }

    public ArrayList<Game> generateGamesFromTurns(ArrayList<Turn> turns) {
	logWriter.startNewSection("GENERATING GAMES");
	ArrayList<Game> games = new ArrayList<>();
	for (Turn turn : turns) {
	    String gameName = turn.getGameName();
	    boolean isNewGame = true;
	    for (Game game : games) {
		if (gameName.matches(game.getName())) {
		    game.registerTurn(turn);
		    isNewGame = false;
		    break;
		}
	    }
	    if (isNewGame) {
		Game g = new Game(gameName);
		games.add(g);
		g.registerTurn(turn);
	    }
	}
	return games;
    }

    /**
     * Reads all turn directories
     *
     * @return
     */
    public ArrayList<Turn> readTurns() {
	logWriter.startNewSection("READING TURN FILES");
	logWriter.log("Extracting from: " + this.saveDirectoryPath);
	File[] directories = this.saveDirectoryPath.listFiles(new FilenameFilter() {
	    @Override
	    public boolean accept(File current, String name) {
		File f = new File(current, name);
		return f.isDirectory() && !f.getName().equals("newlords");
	    }
	});

	ArrayList<Turn> turns = new ArrayList<>(directories.length);

	for (File f : directories) {
	    try {
		turns.add(new Turn(f));
	    } catch (NotATurnDirectoryException ex) {
	    }

	}

	return turns;
    }

    /**
     * Initialises stuff needed for the program itself
     */
    public void initialiseAdmin() {
	archiverJarDirectory = new File(Main.class.getProtectionDomain().getCodeSource().getLocation().getFile()).getParentFile();

	logWriter = new Dom5ArchiverLogger(getFileInArchiverDirectory("Log.txt"), this.createLog);
    }

    public void logConfigs() {
	logWriter.log("archiverJarDirectory:" + this.archiverJarDirectory);
	logWriter.log("dominionsExecutablePath:" + this.dominionsExecutablePath);
	logWriter.log("extractMapFiles:" + Main.mapExtractionOptionToString(this.mapFileExtractionModus));
	logWriter.log("longTermStorageDirectory:" + this.longTermStorageDirectory);
	logWriter.log("mapDirectoryPath:" + this.mapDirectoryPath);
	logWriter.log("readyArchiveDuration:" + this.readyArchiveDuration);
	logWriter.log("saveDirectoryPath:" + this.saveDirectoryPath);
	logWriter.log("useLongTermStorage:" + Main.longTermStorageOptionToString(this.longTermStorageModus));
	logWriter.log("archiveNameSchema:" + this.archiveNameSchema);
	logWriter.log("archiveTurnNumberMinimumlength:" + this.archiveTurnNumberAppendixMinimumLength);
	logWriter.log("arbitraryDomArgument:" + this.arbitraryDomArguments);
	String acc = "";
	for (String s : this.blacklist) {
	    acc += s + ";";
	}
	logWriter.log("blacklist:" + acc);
	acc = "";
	for (String s : this.whitelist) {
	    acc += s + ";";
	}
	logWriter.log("whitelist:" + acc);
	if(Main.expressiveDebugs){
	    String exacc = "";
	    exacc += "archiverJarDirectory:" + this.archiverJarDirectory + "\n";
	    exacc += "dominionsExecutablePath:" + this.dominionsExecutablePath + "\n";
	    exacc += "extractMapFiles:" + Main.mapExtractionOptionToString(this.mapFileExtractionModus) + "\n";
	    exacc += "longTermStorageDirectory:" + this.longTermStorageDirectory + "\n";
	    exacc += "mapDirectoryPath:" + this.mapDirectoryPath + "\n";
	    exacc += "readyArchiveDuration:" + this.readyArchiveDuration + "\n";
	    exacc += "saveDirectoryPath:" + this.saveDirectoryPath + "\n";
	    exacc += "useLongTermStorage:" + Main.longTermStorageOptionToString(this.longTermStorageModus) + "\n";
	    exacc += "archiveNameSchema:" + this.archiveNameSchema + "\n";
	    exacc += "archiveTurnNumberMinimumlength:" + this.archiveTurnNumberAppendixMinimumLength + "\n";
	    JOptionPane.showMessageDialog(null, exacc);
	}
    }

    private void logFinalConfigs() {
	logWriter.startNewSection("FINAL CONFIGS");
	if(Main.expressiveDebugs){
	    JOptionPane.showMessageDialog(null, "Final Configs:");
	}
	this.logConfigs();
    }

    public void logInitialConfigs() {
	logWriter.startNewSection("DEFAULT CONFIGS");
	if(Main.expressiveDebugs){
	    JOptionPane.showMessageDialog(null, "Default Configs:");
	}
	this.logConfigs();
    }

    public void setConfigDefaults() {
	File defaultDominionsDataPath = new File(System.getenv("APPDATA") + "\\Dominions5");
	this.blacklist = new ArrayList<>();
	this.whitelist = new ArrayList<>();
	Main.mapFileExtractionModus = MapExtractionOption.never;
	Main.longTermStorageDirectory = null;
	Main.mapDirectoryPath = new File((defaultDominionsDataPath + "\\maps").replaceAll("%20", " "));
	Main.readyArchiveDuration = -1;
	Main.saveDirectoryPath = new File((defaultDominionsDataPath + "\\savedGames").replaceAll("%20", " "));
	Main.longTermStorageModus = LongTermStorageOption.deactivated;
	Main.archiveNameSchema = "%name%_%turn%";
	Main.archiveTurnNumberAppendixMinimumLength = 2;
	Main.arbitraryDomArguments = "";
	this.dominionsExecutablePath = new File("C:\\Program Files (x86)\\Steam\\steamapps\\common\\Dominions5\\Dominions5.exe");
	this.logInitialConfigs();
    }

    public void readConfig() {
	logWriter.startNewSection("READING CONFIG FILE");
	this.readConfigFile();
	this.readBlackWhitelist(whitelist, getFileInArchiverDirectory("Whitelist.txt"));
	this.readBlackWhitelist(blacklist, getFileInArchiverDirectory("Blacklist.txt"));
	this.logFinalConfigs();
    }

    public void readConfigFile() {
	File configFile = getFileInArchiverDirectory("Config.txt");
	if (!configFile.exists()) {
	    logWriter.error("Could not find Config File at: " + configFile.getAbsolutePath());
	}
	Scanner read;

	try {
	    read = new Scanner(configFile);
	    read.useDelimiter("[\n\r]");
	    String argumentPattern = "(.*)=(.*)";
	    Matcher matcher;
	    while (read.hasNext()) {
		String line = read.next();
		matcher = Pattern.compile(argumentPattern).matcher(line);
		if (matcher.matches()) {
		    String key = matcher.group(1).trim();
		    String value = matcher.group(2).trim();
		    processConfigEntry(key, value);
		} else {
		    logWriter.log("Configfile line did not match expected pattern: " + line);
		}
	    }
	} catch (FileNotFoundException ex) {
	    logWriter.error("Could not find Config File at: " + configFile.getAbsolutePath() + " (unexpected)");
	}
    }

    public void processConfigEntry(String key, String value) {
	logWriter.log("Processing Config for key:" + key + "; value:" + value);
	if (key.matches("dominionsExecutablePath")) {
	    this.dominionsExecutablePath = new File(value);
	    if (!dominionsExecutablePath.exists()) {
		logWriter.error("Could not find Dominions executable at: " + dominionsExecutablePath.getAbsolutePath());
	    }
	} else if (key.matches("saveDirectoryPath")) {
	    saveDirectoryPath = new File(value);
	    if (!saveDirectoryPath.exists()) {
		logWriter.error("SavefileDirectoryPath does not exist: " + saveDirectoryPath.getAbsolutePath());
	    }
	} else if (key.matches("extractMapFiles")) {
	    if (value.matches("never")) {
		mapFileExtractionModus = MapExtractionOption.never;
	    } else if (value.matches("cautious")) {
		mapFileExtractionModus = MapExtractionOption.cautious;
	    } else if (value.matches("force")) {
		mapFileExtractionModus = MapExtractionOption.force;
	    } else {
		logWriter.error("Could not interprete extractMapFiles, does not match never, cautious or force: " + value);
	    }
	} else if (key.matches("mapDirectoryPath")) {
	    mapDirectoryPath = new File(value);
	    if (!mapDirectoryPath.exists()) {
		logWriter.error("MapDirectoryPath does not exist: " + mapDirectoryPath.getAbsolutePath());
	    }
	} else if (key.matches("archiveNameSchema")) {
	    archiveNameSchema = value;
	    if (!archiveNameSchema.contains("%name%")) {
		archiveNameSchema = archiveNameSchema + "name";
	    }
	    if (!archiveNameSchema.contains("%turn%")) {
		archiveNameSchema = archiveNameSchema + "turn";
	    }
	} else if (key.matches("longTermStorageDirectory")) {
	    longTermStorageDirectory = new File(value);
	    if (!longTermStorageDirectory.exists()) {
		logWriter.error("LongTermStorageDirectory does not exist: " + longTermStorageDirectory.getAbsolutePath());
	    }
	} else if (key.matches("readyArchiveDuration")) {
	    readyArchiveDuration = Integer.parseInt(value);
	} else if (key.matches("useLongTermStorage")) {
	    if (value.matches("deactivated")) {
		longTermStorageModus = LongTermStorageOption.deactivated;
	    } else if (value.matches("move")) {
		longTermStorageModus = LongTermStorageOption.move;
	    } else if (value.matches("delete")) {
		longTermStorageModus = LongTermStorageOption.delete;
	    } else {
		logWriter.error("Could not interprete useLongTermStorage, does not match deactivated, move or delete: " + value);
	    }
	} else if (key.matches("archiveTurnNumberMinimumlength")) {
	    Main.archiveTurnNumberAppendixMinimumLength = Integer.parseInt(value);
	} else if(key.matches("dom5Arg")) {
	    Main.arbitraryDomArguments = value;
	} else {
	    logWriter.log("Could not identify key:" + key);
	}
    }

    public void sanityCheckConfigs() {
	Main.logWriter.startNewSection("CHECKING CONFIGS FOR DETECTED PROBLEMS");

	this.checkExistence(this.dominionsExecutablePath, "DominionsExecutable");
	this.checkExistence(this.saveDirectoryPath, "SaveFilesDirectory");
	if (Main.longTermStorageModus == LongTermStorageOption.move) {
	    if (Main.longTermStorageDirectory == null) {
		logWriter.error("longTermStorageModus is 'move', yet longTermStorageDirectory is not set");
	    }
	    this.checkExistence(longTermStorageDirectory, "longTermStorageDirectory");
	}

    }

    public void checkExistence(File toCheck, String name) {
	if (toCheck == null) {
	    logWriter.error(name + " not set");
	} else if (!toCheck.exists()) {
	    logWriter.error(name + " does not exist at " + this.dominionsExecutablePath);
	} else {
	    logWriter.log(name + ": " + this.dominionsExecutablePath.getAbsolutePath());
	}
    }

    public void readBlackWhitelist(ArrayList<String> list, File toRead) {
	Scanner read;
	try {
	    read = new Scanner(toRead);
	    while (read.hasNext()) {
		String tmp = read.nextLine();
		logWriter.log("Adding " + tmp + " to list");
		list.add(tmp);
	    }
	    read.close();
	} catch (FileNotFoundException ex) {
	    logWriter.log("Could not read Black/Whitelist from " + toRead + "; " + ex.getMessage());
	}
    }

    /**
     * Runs the Dominions application and waits for it to close
     */
    public void runDominions() {
	//Runtime run = Runtime.getRuntime();
	ProcessBuilder pb = new ProcessBuilder(dominionsExecutablePath.getAbsolutePath(), Main.arbitraryDomArguments);
	Process proc = null;
	try {
	    proc = pb.start();
	    //proc = run.exec(dominionsExecutablePath.getAbsolutePath());
	} catch (IOException ex) {
	    logWriter.error("Could not launch Dominions, is the path correct? " + ex.getMessage());
	    return;
	}
	try {
	    proc.waitFor();
	} catch (InterruptedException ex) {
	    logWriter.error("Something unexpected happened while waiting for Dominions application to terminate. " + ex.getMessage());
	}
    }

    public static void main(String[] args) {
	if (Main.expressiveDebugs) {
	    new JFrame().setVisible(true);
	    JOptionPane.showMessageDialog(null, "Starting program");
	}
	boolean runGame = true;
	boolean createLog = false;
	for (int i = 0; i < args.length; i++) {
	    if (args[i].equals("-a")) {
		runGame = false;
		if (Main.expressiveDebugs) {
		    JOptionPane.showMessageDialog(null, "Found argument -a");
		}
		continue;
	    }
	    if (args[i].equals("-l")) {
		createLog = true;
		if (Main.expressiveDebugs) {
		    JOptionPane.showMessageDialog(null, "Found argument -l");
		}
		continue;
	    }
	}
	Main m = new Main();
	if(Main.expressiveDebugs) {
	    JOptionPane.showMessageDialog(null, "Finished creating Main object");
	}
	m.run(runGame, createLog);
	if(Main.expressiveDebugs) {
	    JOptionPane.showMessageDialog(null, "Finished and exiting program");
	}
	System.exit(0);
    }

    public static String mapExtractionOptionToString(MapExtractionOption option) {
	switch (option) {
	    case cautious:
		return "cautious";
	    case force:
		return "force";
	    case never:
		return "never";
	}
	return "ERROR mapExtractionOptionToString";
    }

    public static String longTermStorageOptionToString(LongTermStorageOption option) {
	switch (option) {
	    case deactivated:
		return "dactivated";
	    case delete:
		return "delete";
	    case move:
		return "move";
	}
	return "ERROR mapExtractionOptionToString";
    }

    public File getFileInArchiverDirectory(String name) {
	return new File((archiverJarDirectory.getAbsolutePath() + "\\" + name).replaceAll("%20", " "));
    }
}
