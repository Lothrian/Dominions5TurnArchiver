/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.mycompany.dominions5turnarchiver;

import static com.mycompany.dominions5turnarchiver.Main.logWriter;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileWriter;
import java.io.FilenameFilter;
import java.io.IOException;
import java.io.Writer;
import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.Scanner;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import javax.swing.JFrame;
import javax.swing.JOptionPane;

/**
 *
 * @author Rafael
 */
public class Main {

    /*
	Internals
     */
    public static Dom5ArchiverLogger logWriter;
    private File archiverJarDirectory;

    /*
	Required Options
     */
    private File dominionsExecutablePath;
    
    /*
	Optional Options
     */
    private File saveDirectoryPath;
    private boolean extractMapFiles;
    private File mapDirectoryPath;
    public static String archiveNameShema;
    public static int archiveTurnNumberMinimumlength;
    private File longTermStorageDirectory;
    private int readyArchiveDuration = -1;
    private boolean useLongTermStorage ;

    private ArrayList<String> whitelist;
    private ArrayList<String> blacklist;

    public void run(String[] args) {
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
	runDominions();
	/*
	    Read Turns from directories
	 */
	ArrayList<Turn> turns = readTurns();

	/*
	    Create games out of these Turns
	 */
	ArrayList<Game> games = generateGamesFromTurns(turns);

	/*
	    Go through games, Do Archiving
	 */
	for(Game game : games) {
	    logWriter.startNewSection("ARCHIVING GAME " + game.getName());
	    if(shallBeArchivedBasedOnBlackWhiteList(game)){
		game.doArchiving();
		Main.logWriter.log("Finished archiving game " + game.getName());
	    }else {
		logWriter.startNewSection("Skipped because of black/whitelist");
	    }
	}
	logWriter.startNewSection("FINISHED ARCHIVING SUCCESSFULLY");
    }
    
    public boolean shallBeArchivedBasedOnBlackWhiteList(Game game) {
	String gameName = game.getName();
	for (int i = 0; i < blacklist.size(); i++) {
            if (gameName.matches(blacklist.get(i))) {
                return false;
            }
        }

        if (whitelist.size() > 0) {
            for (int i = 0; i < whitelist.size(); i++) {
                if (gameName.matches(whitelist.get(i))) {
                    return true;
                }
            }
            return false;
        }

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
	    turns.add(new Turn(f));
	}

	return turns;
    }

    /**
     * Initialises stuff needed for the program itself
     */
    public void initialiseAdmin() {
	archiverJarDirectory = new File(Main.class.getProtectionDomain().getCodeSource().getLocation().getFile()).getParentFile();

	logWriter = new Dom5ArchiverLogger(new File(archiverJarDirectory + "\\Log.txt"));
    }

    public void logConfigs() {
	logWriter.log("archiverJarDirectory:" + this.archiverJarDirectory);
	logWriter.log("blacklist:" + this.blacklist);
	logWriter.log("dominionsExecutablePath:" + this.dominionsExecutablePath);
	logWriter.log("extractMapFiles:" + this.extractMapFiles);
	logWriter.log("longTermStorageDirectory:" + this.longTermStorageDirectory);
	logWriter.log("mapDirectoryPath:" + this.mapDirectoryPath);
	logWriter.log("readyArchiveDuration:" + this.readyArchiveDuration);
	logWriter.log("saveDirectoryPath:" + this.saveDirectoryPath);
	logWriter.log("useLongTermStorage:" + this.useLongTermStorage);
	logWriter.log("archiveNameShema:" + this.archiveNameShema);
	logWriter.log("archiveTurnNumberMinimumlength:" + this.archiveTurnNumberMinimumlength);
	logWriter.log("whitelist:" + this.whitelist);
    }

    private void logFinalConfigs() {
	logWriter.startNewSection("FINAL CONFIGS");
	this.logConfigs();
    }

    public void logInitialConfigs() {
	logWriter.startNewSection("DEFAULT CONFIGS");
	this.logConfigs();
    }

    public void setConfigDefaults() {
	File defaultDominionsDataPath = new File(System.getenv("APPDATA") + "\\Dominions5");
	this.blacklist = new ArrayList<>();
	this.whitelist = new ArrayList<>();
	this.extractMapFiles = false;
	this.longTermStorageDirectory = null;
	this.mapDirectoryPath = new File(defaultDominionsDataPath + "\\maps");
	this.readyArchiveDuration = -1;
	this.saveDirectoryPath = new File(defaultDominionsDataPath + "\\savedGames");
	this.useLongTermStorage = false;
	this.archiveNameShema = "%name%%turn%";
	this.archiveTurnNumberMinimumlength = 2;
	this.logInitialConfigs();
    }

    public void readConfig() {
	logWriter.startNewSection("READING CONFIG FILE");
	this.readConfigFile();
	this.readBlackWhitelist(whitelist, new File(archiverJarDirectory + "\\Whitelist.txt"));
	this.readBlackWhitelist(blacklist, new File(archiverJarDirectory + "\\Blacklist.txt"));
	this.logFinalConfigs();
    }

    public void readConfigFile() {
	File configFile = new File(archiverJarDirectory + "\\Config.txt");
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
	    logWriter.error("Could not find Config File at: " + configFile.getAbsolutePath());
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
	    if (value.matches("true")) {
		extractMapFiles = true;
	    } else if (value.matches("false")) {
		extractMapFiles = false;
	    } else {
		logWriter.error("Could not interprete extractMapFiles, does not match either true or false: " + value);
	    }
	} else if (key.matches("mapDirectoryPath")) {
	    mapDirectoryPath = new File(value);
	    if (!mapDirectoryPath.exists()) {
		logWriter.error("MapDirectoryPath does not exist: " + mapDirectoryPath.getAbsolutePath());
	    }
	} else if (key.matches("archiveNameShema")) {
	    archiveNameShema = value;
	} else if (key.matches("longTermStorageDirectory")) {
	    longTermStorageDirectory = new File(value);
	    if (!longTermStorageDirectory.exists()) {
		logWriter.error("LongTermStorageDirectory does not exist: " + longTermStorageDirectory.getAbsolutePath());
	    }
	} else if (key.matches("readyArchiveDuration")) {
	    readyArchiveDuration = Integer.parseInt(value);
	} else if (key.matches("useLongTermStorage")) {
	    if (value.matches("true")) {
		useLongTermStorage = true;
	    } else if (value.matches("false")) {
		useLongTermStorage = false;
	    } else {
		logWriter.error("Could not interprete useLongTermStorage, does not match either true or false: " + value);
	    }
	}else if (key.matches("archiveTurnNumberMinimumlength")) {
	    this.archiveTurnNumberMinimumlength = Integer.parseInt(value);
	} else {
	    logWriter.log("Could not identify key:" + key);
	}
    }

    public void sanityCheckConfigs() {
	this.logWriter.startNewSection("CHECKING REQUIRED CONFIG");

	this.checkExistence(this.dominionsExecutablePath, "DominionsExecutable");
	this.checkExistence(this.saveDirectoryPath, "SaveFilesDirectory");
	
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
	list = new ArrayList<>();
	try {
	    read = new Scanner(toRead);
	    while (read.hasNext()) {
		list.add(read.nextLine());
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
	Runtime run = Runtime.getRuntime();
	Process proc = null;
	try {
	    proc = run.exec(dominionsExecutablePath.getAbsolutePath());
	} catch (IOException ex) {
	    logWriter.error("Could not launch Dominions, is the path correct? " + ex.getMessage());
	}
	try {
	    proc.waitFor();
	} catch (InterruptedException ex) {
	    logWriter.error("Something unexpected happened while waiting for Dominions application to terminate. " + ex.getMessage());
	}
    }

    public static void main(String[] args) {
	new Main().run(args);
    }
}
