/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.mycompany.dominions5turnarchiver;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 *
 * @author Rafael
 */
public class Game {
    
    protected HashMap<Integer, Turn> archivedTurns;
    protected int numberOfDirectlyAccessibleArchivedGames;
    protected String name;
    protected Turn currentTurn;
    protected String archiveShema;
    protected int archiveTurnNumberMinimumlength;
   
    
    public Game(String name, String archiveShema, int archiveTurnNumberMinimumlength) {
	archivedTurns = new HashMap<>();
	this.name = name;
	currentTurn = null;
	this.archiveTurnNumberMinimumlength = archiveTurnNumberMinimumlength;
	this.archiveShema = archiveShema.replaceAll("%name%", name);
	Main.logWriter.log("Creating new Game with Name: " + name);
    }
    
    public String getName() {
	return this.name;
    }
    
    public void doArchiving() {
	Main.logWriter.startNewSection("archiving");
	this.currentTurn.archive();
	
    }
    
    public void doLongTimeStoraging() {
	
    }
    
    public void registerTurn(Turn newTurn){
	Main.logWriter.log("Registering Turn " + newTurn.getTurnNumber() + " to game " + this.name);
	
	/*
	Read directory name
	if it matches shema it is archived, register as such
	else if it matches gameName it is the current turn
	else error
	*/
	String directoyName = newTurn.getDirectoryName();
	String specificShemaExpectedTurnNumber = String.format("%1$" + archiveTurnNumberMinimumlength + "s", Integer.toString(newTurn.getTurnNumber())).replace(' ', '0');
	String specificTurnArchiveShema = this.archiveShema.replaceAll("%turn%", specificShemaExpectedTurnNumber);
	Main.logWriter.log("Using specific turn archive shema " + specificTurnArchiveShema);
	
	Matcher currentGameNameMatcher = Pattern.compile(this.name).matcher(directoyName);
	Matcher archiveShemaMatcher = Pattern.compile(specificTurnArchiveShema).matcher(directoyName);
	
	if(currentGameNameMatcher.matches()){
	    if(this.currentTurn != null) {
		Main.logWriter.error("Duplicate detected as current turn in directory name " + directoyName + " when registering turn " + newTurn.getTurnNumber() + " to game " + this.name);
	    }
	    Main.logWriter.log("Noting turn " + newTurn.getTurnNumber() + " in directory " + directoyName + " as current to game " + this.name);
	    this.currentTurn = newTurn;
	}else if(archiveShemaMatcher.matches()){
	    this.archivedTurns.put(newTurn.getTurnNumber(), newTurn);
	}else {
	    Main.logWriter.error("Could not interpret directory name " + directoyName + " when registering turn " + newTurn.getTurnNumber() + " to game " + this.name);
	}
	
	Main.logWriter.log("Registered turn " + newTurn.getTurnNumber() + " to game " + this.name);
    }
    
}
