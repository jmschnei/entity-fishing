package com.scienceminer.nerd.kb;

import com.scienceminer.nerd.exceptions.NerdException;
import com.scienceminer.nerd.utilities.NerdProperties;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.*;
import java.util.*;

import java.io.BufferedReader;
import java.util.List;    

//import org.wikipedia.miner.model.Category;

import org.apache.commons.lang3.StringUtils;
import com.fasterxml.jackson.core.io.*;

/**
 * Representation of a category (here an uncontrolled typing, as in Wikipedia) for a concept or similar 
 * entity associated to a given source.
 * 
 * @author Patrice Lopez
 * 
 */
public class Category { 
	
	protected static final Logger LOGGER = LoggerFactory.getLogger(Category.class);
	
	private String name = null;
	private org.wikipedia.miner.model.Category wikiCategory = null;
	private int wikiPageID = -1;
	
	// an optional weight to be associated to the category
	private double weight = 0.0;
	
	public Category() {}

	public Category(String name) {
		this.name= name;
	}

    public Category(String name, org.wikipedia.miner.model.Category wikiCategory, int wikiPageID) {
		this.wikiCategory = wikiCategory;
		this.name = name;
		this.wikiPageID = wikiPageID;
    }
	
	public Category(org.wikipedia.miner.model.Category wikiCategory) {
		this.wikiCategory = wikiCategory;
		name = wikiCategory.getTitle();
		wikiPageID = wikiCategory.getId();
    }

	public String toString() {
		return wikiCategory.toString();
	}
	
	public String getName() {
		return name;
	}
	
	public int getWikiPageID() {
		return wikiPageID;
	}
	
	public org.wikipedia.miner.model.Category getWikiCategory() {
		return wikiCategory;
	}
	
	public double getWeight() {
		return weight;
	}
	
	public void setWeight(double weight) {
		this.weight = weight;
	}
}