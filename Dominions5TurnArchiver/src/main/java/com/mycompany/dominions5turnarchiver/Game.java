/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.mycompany.dominions5turnarchiver;

import java.util.ArrayList;
import java.util.HashMap;

/**
 *
 * @author Rafael
 */
public class Game {
    
    protected HashMap<Integer, Turn> turns;
    protected int numberOfDirectlyAccessibleArchivedGames;
    protected String name;

    public Game(String name) {
	turns = new HashMap<>();
	this.name = name;
	Main.logWriter.log("Creating new Game with Name: " + name);
    }
    
    public String getName() {
	return this.name;
    }
    
    public void doArchiving() {
	Main.logWriter.startNewSection("archiving");
    }
    
    public void doLongTimeStoraging() {
	
    }
    
    public void registerTurn(Turn newTurn){
	turns.put(newTurn.getTurnNumber(), newTurn);
	Main.logWriter.log("Registered Turn " + newTurn.getTurnNumber() + " to game " + this.name);
    }
    
}
