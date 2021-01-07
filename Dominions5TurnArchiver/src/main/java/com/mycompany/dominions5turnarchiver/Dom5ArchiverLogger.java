/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.mycompany.dominions5turnarchiver;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.Writer;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.swing.JFrame;
import javax.swing.JOptionPane;

/**
 *
 * @author Rafael
 */
public class Dom5ArchiverLogger {

    private JFrame errorMessagePanel;
    private Writer logWriter;

    public Dom5ArchiverLogger(JFrame errorMessagePanel, File logFile) {
	this.errorMessagePanel = errorMessagePanel;

	if (!logFile.exists()) {
	    try {
		logFile.createNewFile();
	    } catch (IOException ex) {
		JOptionPane.showMessageDialog(errorMessagePanel, "Could not create log file at: " + logFile.getAbsolutePath(), "Error", JOptionPane.ERROR_MESSAGE);
		System.exit(0);
	    }
	}
	
	try {
	    logWriter = new FileWriter(logFile, false);
	} catch (IOException ex) {
	    JOptionPane.showMessageDialog(errorMessagePanel, "Could not log file writer at: " + logFile.getAbsolutePath(), "Error", JOptionPane.ERROR_MESSAGE);
	    System.exit(0);
	}
    }

    public void log(String text) {
	try {
	    logWriter.write(text + " ;" + System.getProperty("line.separator"));
	    logWriter.flush();
	} catch (IOException ex) {
	    JOptionPane.showMessageDialog(errorMessagePanel, ex.getMessage());
	    System.exit(0);
	}
    }

    public void error(String text) {
	this.log("ERROR: " + text);
	try {
	    logWriter.close();
	} catch (IOException ex) {
	    JOptionPane.showMessageDialog(errorMessagePanel, ex.getMessage());
	}
	System.exit(0);
    }

}
