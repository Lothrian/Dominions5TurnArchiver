/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package main;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 *
 * @author Rafael
 */
public class Game {
    
    protected HashMap<Integer, Turn> archivedTurns;
    protected String name;
    protected Turn currentTurn;
   
    
    public Game(String name) {
	archivedTurns = new HashMap<>();
	this.name = name;
	currentTurn = null;
	Main.logWriter.log("Creating new Game with Name: " + name);
    }
    
    public String getName() {
	return this.name;
    }
    
    public void doArchiving() {
	Main.logWriter.log("Starting to archive game: " + this.name);
	if(this.currentTurn == null) {
	    Main.logWriter.log("No current turn found, therefore skipping.");
	    return;
	}
	this.currentTurn.archive();	
	if(Main.longTermStorageModus != LongTermStorageOption.deactivated) {  
	    this.doLongTimeStoraging();
	}
    }
    
    public void doLongTimeStoraging() {
	if(Main.readyArchiveDuration < 0) return;
	int curTur = this.currentTurn.getTurnNumber();
	Set<Integer> turns = archivedTurns.keySet();
	for (Integer turn : turns) {
	    if((curTur - turn) > Main.readyArchiveDuration){
		Main.logWriter.log("Archiving turn: " + Integer.toString(turn) + " for game " + this.archivedTurns.get(turn).getGameName() + 
			", latest turn " + Integer.toString(turn));
		this.archivedTurns.get(turn).doLongTermStorage();
	    }
	}
    }
    
    public void registerTurn(Turn newTurn){
	Main.logWriter.log("Registering Turn " + newTurn.getTurnNumber() + " to game " + this.name);
	
	/*
	Read directory name
	if it matches shema it is archived, register as such
	else if it matches gameName it is the current turn
	else error
	*/
	String directoryName = newTurn.getDirectoryName();
	String specificTurnArchiveShema = newTurn.getNameOfArchiveDirectory();
	Main.logWriter.log("Using specific turn archive shema " + specificTurnArchiveShema);
	
	Matcher currentGameNameMatcher = Pattern.compile(this.name).matcher(directoryName);
	Matcher archiveShemaMatcher = Pattern.compile(specificTurnArchiveShema).matcher(directoryName);
	
	if(currentGameNameMatcher.matches()){
	    if(this.currentTurn != null) {
		Main.logWriter.error("Duplicate detected as current turn in directory name " + directoryName + " when registering turn " + newTurn.getTurnNumber() + " to game " + this.name);
	    }
	    Main.logWriter.log("Noting turn " + newTurn.getTurnNumber() + " in directory " + directoryName + " as current to game " + this.name);
	    this.currentTurn = newTurn;
	}else if(archiveShemaMatcher.matches()){
	    this.archivedTurns.put(newTurn.getTurnNumber(), newTurn);
	}else {
	    Main.logWriter.error("Could not interpret directory name " + directoryName + " when registering turn " + newTurn.getTurnNumber() + " to game " + this.name);
	}
	
	Main.logWriter.log("Registered turn " + newTurn.getTurnNumber() + " to game " + this.name);
    }
    

    
}
