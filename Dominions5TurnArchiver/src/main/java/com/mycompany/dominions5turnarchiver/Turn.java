/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.mycompany.dominions5turnarchiver;

import java.io.File;

/**
 *
 * @author Rafael
 */
public class Turn {
 
    protected File directory;
    
    
    public int getTurnNumber() {
	return 0;
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
    
    
}
