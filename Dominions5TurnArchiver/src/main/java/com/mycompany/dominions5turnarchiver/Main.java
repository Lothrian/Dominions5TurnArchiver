/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.mycompany.dominions5turnarchiver;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileWriter;
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
    private JFrame errorMessagePanel = null;
    private Dom5ArchiverLogger logWriter;
    private File archiverJarDirectory;
    private File defaultDominionsDataPath;

    /*
	Required Options
     */
    private File dominionsExecutablePath;

    /*
	Optional Options
     */
    private File saveDirectoryPath;
    private boolean extractMapFiles = false;
    private File mapDirectoryPath;
    private String nameshema = "%name%_%turn% 2";
    private File longTermStorageDirectory;
    private int readyArchiveDuration = -1;
    private boolean useLongTermStorage = false;

    private ArrayList<String> whitelist;
    private ArrayList<String> blacklist;

    public void run(String[] args) {
	/*
	    Initialise values
	*/
	this.initialiseAdmin();
	this.readConfig();
	this.readBlackWhitelist(whitelist, new File(archiverJarDirectory + "\\Whitelist.txt"));
	this.readBlackWhitelist(blacklist, new File(archiverJarDirectory + "\\Blacklist.txt"));
	this.logConfigs();
	this.checkRequiredConfig();
	
	
	    

	/*
	    Execute game
	 */
	//runDominions();
	
	/*
	    Read Turns from directories
	*/
	readTurns();
	
	/*
	    Create games out of these Turns
	 */

	/*
	    Go through games, Do Archiving
	 */
    }

    public ArrayList<Turn> readTurns() {
	return null;
    }

    /**
     * Initialises stuff needed for the program itself
     */
    public void initialiseAdmin() {
	defaultDominionsDataPath = new File(System.getenv("APPDATA") + "\\Dominions5");
	archiverJarDirectory = new File(Main.class.getProtectionDomain().getCodeSource().getLocation().getFile()).getParentFile();
	errorMessagePanel = new JFrame();
	errorMessagePanel.setResizable(true);
	
	logWriter = new Dom5ArchiverLogger(errorMessagePanel, new File(archiverJarDirectory + "\\Log.txt"));
    }

    public void logConfigs() {
	logWriter.log("-----");
	logWriter.log("Used Configs:");
	logWriter.log("  archiverJarDirectory:" + this.archiverJarDirectory);
	logWriter.log("  blacklist:" + this.blacklist);
	logWriter.log("  defaultDominionsDataPath:" + this.defaultDominionsDataPath);
	logWriter.log("  dominionsExecutablePath:" + this.dominionsExecutablePath);
	logWriter.log("  extractMapFiles:" + this.extractMapFiles);
	logWriter.log("  longTermStorageDirectory:" + this.longTermStorageDirectory);
	logWriter.log("  mapDirectoryPath:" + this.mapDirectoryPath);
	logWriter.log("  readyArchiveDuration:" + this.readyArchiveDuration);
	logWriter.log("  saveDirectoryPath:" + this.saveDirectoryPath);
	logWriter.log("  useLongTermStorage:" + this.useLongTermStorage);
	logWriter.log("  whitelist:" + this.whitelist);
    }
    
    public void readConfig() {

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
		    String key = matcher.group(1).replaceAll(" ", "");
		    String value = matcher.group(2).replaceAll(" ", "");
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
	    if(value.matches("true")) {
		extractMapFiles = true;
	    }else if(value.matches("false")) {
		extractMapFiles = false;
	    } else {
		logWriter.error("Could not interprete extractMapFiles, does not match either true or false: " + value);
	    }
	} else if (key.matches("mapDirectoryPath")) {
	    mapDirectoryPath = new File(value);
	    if (!mapDirectoryPath.exists()) {
		logWriter.error("MapDirectoryPath does not exist: " + mapDirectoryPath.getAbsolutePath());
	    }
	} else if (key.matches("nameshema")) {
	    nameshema = value;
	} else if (key.matches("longTermStorageDirectory")) {
	    longTermStorageDirectory = new File(value);
	    if (!longTermStorageDirectory.exists()) {
		logWriter.error("LongTermStorageDirectory does not exist: " + longTermStorageDirectory.getAbsolutePath());
	    }
	} else if (key.matches("readyArchiveDuration")) {
	    readyArchiveDuration = Integer.parseInt(value);
	} else if (key.matches("useLongTermStorage")) {
	    if(value.matches("true")) {
		useLongTermStorage = true;
	    }else if(value.matches("false")) {
		useLongTermStorage = false;
	    } else {
		logWriter.error("Could not interprete useLongTermStorage, does not match either true or false: " + value);
	    }
	} else {
	    logWriter.log("Could not identify key:" + key);
	}
    }
    
    public void checkRequiredConfig() {
	logWriter.log(this.dominionsExecutablePath.getAbsolutePath());
	if(this.dominionsExecutablePath == null) {
	    logWriter.error("dominionsExecutablePath config not set");
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
