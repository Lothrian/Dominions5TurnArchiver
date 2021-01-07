/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.mycompany.dominions5turnarchiver;

import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FilenameFilter;
import java.io.IOException;
import javax.swing.JOptionPane;

/**
 *
 * @author Rafael
 */
public class Turn {
 
    protected File directory;
    protected int turnNumber;
    protected String gameName;

    public Turn(File directory) {
	this.directory = directory;
	File trnFile = this.findATurnFile();
	this.gameName = this.readGameName(trnFile);
	this.turnNumber = this.readTurnNumber(trnFile);
	Main.logWriter.log("Creating new Turn number " + turnNumber + " for: " + gameName + " (name extracted from " + trnFile + ")");
    }
    
    
   
    
    public int getTurnNumber() {
	return this.turnNumber;
    }
    
    public boolean isArchived() {
	return false;
    }
    
    public void archive() {
	
    }
    
    public void moveToLongTimeStorage() {
	
    }
    
    public boolean containsMapFile() {
	return false;
    }
    
    public void extractMapFile(File mapDirectory) {
	
    }
    
    private File findATurnFile() {
	File[] files = this.directory.listFiles(new FilenameFilter() {
            @Override
            public boolean accept(File current, String name) {
                File f = new File(current, name);
                return !f.isDirectory() && f.getName().contains(".trn");
            }
        });
	if(files.length < 1) {
	    Main.logWriter.error("Could not find any .trn files at: " + this.directory);
	}
	return files[0];
    }
    
    public final String getGameName() {
	return this.gameName;
    }
    
    public final int readTurnNumber(File trnFile) {
        try {
            BufferedInputStream read = new BufferedInputStream(new FileInputStream(trnFile));
            byte[] content = new byte[1];
            read.skip(0x0E);   //Skip to game name
            read.read(content, 0, content.length);  //Read turn number
            read.close();

            return (int) content[0];
        } catch (IOException ex) {
	    Main.logWriter.error("Failed reading file at: " + this.directory + " ; Error: " + ex.getMessage());
        }
        return -1;
    }
    
    public final String readGameName(File trnFile) {
	try {
            BufferedInputStream read = new BufferedInputStream(new FileInputStream(trnFile));
            byte[] content = new byte[27];
            read.skip(0x26);   //Skip to game name
            read.read(content, 0, content.length);  //Read name
            String result = "";

            // Magic code to get text, credit goes to garegoylant
            for (int i = 0; i < content.length; i++) {
                int delta = (((content[i] & 0xF0) % 0x40) * 2) + 0x40;
                char valC = (char) (delta + 0xF - content[i]);
                if (valC == 0) {
                    break;
                }
                result += valC;
            }

            read.close();
            return result;
        } catch (IOException ex) {
	    Main.logWriter.error("Failed reading file " + trnFile.getAbsolutePath());
        }
        return "";
    }
    
}
