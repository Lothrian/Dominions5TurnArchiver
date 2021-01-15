/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package main;

import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FilenameFilter;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.StandardCopyOption;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.swing.JOptionPane;

/**
 *
 * @author Rafael
 */
public class Turn {

    protected File directory;
    protected int turnNumber;
    protected String gameName;
    protected File[] mapFiles;

    public Turn(File directory) throws NotATurnDirectoryException {
	this.directory = directory;
	File trnFile = this.findATurnFile();
	this.gameName = this.readGameName(trnFile);
	this.turnNumber = this.readTurnNumber(trnFile);
	this.identifyMapFiles();
	Main.logWriter.log("Creating new Turn number " + turnNumber + " for: " + gameName + " (name extracted from " + trnFile + ")");
    }

    public int getTurnNumber() {
	return this.turnNumber;
    }

    public void archive() {
	try {
	    File archiveDirectory = new File(this.directory.getParent() + "\\" + this.getNameOfArchiveDirectory());
	    Main.logWriter.log("Archiving " + this.directory + " to " + archiveDirectory);
	    copyFolder(this.directory, archiveDirectory);
	} catch (IOException ex) {
	    Main.logWriter.error("failed to copy: " + ex.getMessage());
	}
	if ((Main.mapFileExtractionModus != MapExtractionOption.never) && this.containsMapFile()) {
	    this.extractMapFile(Main.mapDirectoryPath);
	}
    }

    public void doLongTermStorage() {
	if (Main.longTermStorageModus == LongTermStorageOption.move) {
	    try {
		File targetDirectory = new File(Main.longTermStorageDirectory + "\\" + this.directory.getName());
		Main.logWriter.log("Moving " + this.directory + " to LongtimeStorage " + targetDirectory);
		copyFolder(this.directory, targetDirectory);
	    } catch (IOException ex) {
		Main.logWriter.error(ex.getMessage());
	    }
	} else if (Main.longTermStorageModus == LongTermStorageOption.delete) {
	    Main.logWriter.log("Deleting " + this.directory + " because LongtimeStorage exceeded");
	    File[] files = this.directory.listFiles();
	    for (File file : files) {
		try {
		    Files.delete(file.toPath());
		} catch (IOException ex) {
		    Main.logWriter.error(ex.getMessage());
		}
	    }
	    try {
		Files.delete(this.directory.toPath());
	    } catch (IOException ex) {
		Main.logWriter.error(ex.getMessage());
	    }
	}

    }

    public boolean containsMapFile() {
	return this.mapFiles.length > 0;
    }

    protected final void identifyMapFiles() {
	this.mapFiles = this.directory.listFiles(new FilenameFilter() {
	    @Override
	    public boolean accept(File current, String name) {
		File f = new File(current, name);
		return f.getName().endsWith(".map")
			|| f.getName().endsWith(".rgb")
			|| f.getName().endsWith(".tga");
	    }
	});
	for (File mapfile : this.mapFiles) {
	    Main.logWriter.log("Found Map File: " + mapfile);
	}
    }

    public void extractMapFile(File mapDirectory) {
	if (Main.mapFileExtractionModus == MapExtractionOption.never) {
	    return;
	}
	Main.logWriter.log("Start to extract map files:");

	File targetFile;
	for (File mapfile : this.mapFiles) {
	    targetFile = new File(Main.mapDirectoryPath.getAbsolutePath() + "\\" + mapfile.getName());
	    Main.logWriter.log("Target Path for Map File " + mapfile.getName() + ": " + targetFile.getAbsolutePath());
	    if (((Main.mapFileExtractionModus == MapExtractionOption.cautious) && !targetFile.exists())
		    || (Main.mapFileExtractionModus == MapExtractionOption.force)) {
		Main.logWriter.log("Copying map file. Target file existance: " + Boolean.toString(targetFile.exists()));
		try {
		    Files.copy(mapfile.toPath(), targetFile.toPath(), StandardCopyOption.REPLACE_EXISTING);
		    Main.logWriter.log("Copied " + mapfile);
		    Files.delete(mapfile.toPath());
		    Main.logWriter.log("Removed " + mapfile);
		} catch (IOException ex) {
		    Main.logWriter.error(ex.getMessage());
		}
	    }
	}
	Main.logWriter.log("Finished extracting map files");
    }

    public String getDirectoryName() {
	return this.directory.getName();
    }

    public String getNameOfArchiveDirectory() {
	String specificShemaExpectedTurnNumber = String.format("%1$" + Main.archiveTurnNumberAppendixMinimumLength + "s", Integer.toString(this.getTurnNumber())).replace(' ', '0');
	return Main.archiveNameSchema.replaceAll("%turn%", specificShemaExpectedTurnNumber).replaceAll("%name%", this.gameName);
    }

    private File findATurnFile() throws NotATurnDirectoryException {
	File[] files = this.directory.listFiles(new FilenameFilter() {
	    @Override
	    public boolean accept(File current, String name) {
		File f = new File(current, name);
		return !f.isDirectory() && f.getName().contains(".trn");
	    }
	});
	if (files.length < 1) {
	    Main.logWriter.log("Could not find any .trn files at: " + this.directory);
	    throw new NotATurnDirectoryException();
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

    /**
     * Copies a folder somewhere else Credit goes to https://howtodoinjava.com/java/io/how-to-copy-directories-in-java/
     *
     * @param sourceFolder
     * @param destinationFolder
     * @throws IOException
     */
    private static void copyFolder(File sourceFolder, File destinationFolder) throws IOException {
	//Check if sourceFolder is a directory or file
	//If sourceFolder is file; then copy the file directly to new location
	if (sourceFolder.isDirectory()) {
	    //Get all files from source directory
	    String files[] = sourceFolder.list();

	    //Iterate over all files and copy them to destinationFolder one by one
	    for (String file : files) {
		File srcFile = new File(sourceFolder, file);
		File destFile = new File(destinationFolder, file);

		if (!destFile.exists()) {
		    destFile.mkdirs();
		}

		//Recursive function call
		copyFolder(srcFile, destFile);
	    }
	} else {
	    //Copy the file content from one place to another
	    if (!sourceFolder.toPath().equals(destinationFolder.toPath())) {
		Files.copy(sourceFolder.toPath(), destinationFolder.toPath(), StandardCopyOption.REPLACE_EXISTING);
	    }
	}
    }

}
