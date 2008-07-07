package org.openmrs.module.birt;

import java.util.StringTokenizer;
import org.eclipse.datatools.sqltools.parsers.sql.*;

/**
 * 
 * Simple class that helps parse a pseudo-SQL query.  
 * 
 * TODO:  Should be using the DTP query parser from Eclipse.
 */
public class BirtDataSetQuery { 
	
	private final static String SELECT_CLAUSE = "SELECT";
	private final static String FROM_CLAUSE = " FROM "; 
	private final static String COLUMN_SEPARATOR = ",";
	private final static String WHITESPACE = " ";
	
	private String queryText;	// select column1, column2 from filename
	private String columns;		// "column1, column2"
	private String dataset;		// "filename"
	
	
	public BirtDataSetQuery(String queryText) { 
		this.queryText = queryText;
		parseColumns();
		parseDataset();
		rebuildQueryText();		
	}
	
	public void rebuildQueryText() {
		this.queryText = new StringBuffer().
			append(SELECT_CLAUSE).append(WHITESPACE).
			append(columns).append(WHITESPACE).
			append(FROM_CLAUSE).append(WHITESPACE).
			append(dataset).toString();		
	}
	
	public void parseColumns() { 			
		int startIndex = queryText.toUpperCase().indexOf(SELECT_CLAUSE) + SELECT_CLAUSE.length();
		int endIndex = queryText.toUpperCase().lastIndexOf(FROM_CLAUSE);
		this.columns = queryText.substring(startIndex, endIndex).trim();
	}
	
	public void parseDataset() { 
		int startIndex = queryText.toLowerCase().lastIndexOf(FROM_CLAUSE) + FROM_CLAUSE.length();
		this.dataset = queryText.substring(startIndex).trim();
	}
	
	public String getQueryText() { 
		return queryText;
	}
	
	public String getDataset() { 
		return dataset;
	}
	
	public void setDataset(String dataset) { 
		this.dataset = dataset;
		rebuildQueryText();
	}
	
	public String getColumns() { 
		return columns;
	}
	
	public String [] getColumnArray() { 
		return columns.split(COLUMN_SEPARATOR);
	}
	
	public void setColumns(String columns) { 
		this.columns = columns;
		rebuildQueryText();
	}
	
	public String toString() { 
		return queryText;
	}
}