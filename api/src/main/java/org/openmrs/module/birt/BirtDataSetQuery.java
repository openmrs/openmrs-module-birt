/**
 * The contents of this file are subject to the OpenMRS Public License
 * Version 1.0 (the "License"); you may not use this file except in
 * compliance with the License. You may obtain a copy of the License at
 * http://license.openmrs.org
 *
 * Software distributed under the License is distributed on an "AS IS"
 * basis, WITHOUT WARRANTY OF ANY KIND, either express or implied. See the
 * License for the specific language governing rights and limitations
 * under the License.
 *
 * Copyright (C) OpenMRS, LLC.  All Rights Reserved.
 */
package org.openmrs.module.birt;

/**
 * 
 * Simple class that helps parse a pseudo-SQL query.  
 * 
 * TODO:  Should be using the DTP query parser from Eclipse.
 */
public class BirtDataSetQuery { 
	
	
	
	// SELECT column1, column2 FROM filename : { column info }
	private String table;		// "filename"
	private String columns;		// "column1, column2"
	private String columnsInfo;  // "{column info}
	
	
	public BirtDataSetQuery(String queryText) { 
		this.columns = BirtQueryUtil.getColumns(queryText);
		this.table = BirtQueryUtil.getTable(queryText);
		this.columnsInfo = BirtQueryUtil.getColumnsInfo(queryText);
	}
	
	
	/** 
	 * returns the new 
	 */
	public String getQueryText() {
		StringBuffer queryText = new StringBuffer().
			append(BirtConstants.QUERY_BEGIN_DELIMITER).append(BirtConstants.WHITESPACE).
			append(columns).append(BirtConstants.WHITESPACE).
			append(BirtConstants.TABLE_BEGIN_DELIMITER).append(BirtConstants.WHITESPACE).
			append(table);		
		
		if(columnsInfo!=null) { 
			queryText.
				append(BirtConstants.WHITESPACE).
				append(BirtConstants.QUERY_TEXT_DELIMITER).
				append(BirtConstants.WHITESPACE).
				append(BirtConstants.COLUMNSINFO_BEGIN_DELIMITER).
				append(columnsInfo).
				append(BirtConstants.COLUMNSINFO_END_DELIMITER);
		}
		
		return queryText.toString();
		
	}
	

	
	public String getTable() { 
		return this.table;
	}
	
	public void setTable(String table) { 
		this.table = table;
	}
	
	public String getColumns() { 
		return columns;
	}
	
	public String [] getColumnArray() { 
		return columns.split(BirtConstants.COLUMN_DELIMITER);
	}
	
	public void setColumns(String columns) { 
		this.columns = columns;
	}
	
	public String getColumnsInfo() { 
		return this.columnsInfo;
	}
	
	public void setColumnsInfo(String columnInfo) { 
		this.columnsInfo = columnInfo;
	}
	
	public String toString() { 
		return getQueryText();
	}
}