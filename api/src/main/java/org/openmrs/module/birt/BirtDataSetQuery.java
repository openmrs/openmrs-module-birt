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

	/* Constants used for parsing the BIRT ODA query text */
	public static final char QUERY_TEXT_DELIMITER = ':';
	public static final char COLUMNSINFO_BEGIN_DELIMITER = '{';
	public static final char COLUMNSINFO_END_DELIMITER = '}';
	public final static String QUERY_BEGIN_DELIMITER = "SELECT";
	public final static String TABLE_BEGIN_DELIMITER = " FROM ";
	public final static String COLUMN_DELIMITER = ",";
	public final static String WHITESPACE = " ";

	// SELECT column1, column2 FROM filename : { column info }
	private String table;		// "filename"
	private String columns;		// "column1, column2"
	private String columnsInfo;  // "{column info}
	
	
	public BirtDataSetQuery(String queryText) { 
		this.columns = getColumns(queryText);
		this.table = getTable(queryText);
		this.columnsInfo = getColumnsInfo(queryText);
	}


	/**
	 * returns the new
	 */
	public String getQueryText() {
		StringBuffer queryText = new StringBuffer().
				append(QUERY_BEGIN_DELIMITER).append(WHITESPACE).
				append(columns).append(WHITESPACE).
				append(TABLE_BEGIN_DELIMITER).append(WHITESPACE).
				append(table);

		if(columnsInfo!=null) {
			queryText.
					append(WHITESPACE).
					append(QUERY_TEXT_DELIMITER).
					append(WHITESPACE).
					append(COLUMNSINFO_BEGIN_DELIMITER).
					append(columnsInfo).
					append(COLUMNSINFO_END_DELIMITER);
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
		return columns.split(COLUMN_DELIMITER);
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

	// Utility methods for splitting query text into query and columns information..  Simple pseudo-SQL query parser.
	// TODO Use BIRT's QueryTextUtil once we move to BIRT 2.3

	/**
	 * Gets the column project from the given query text.
	 *
	 * @param queryText
	 * @return
	 */
	public static String getColumns(String queryText) {
		queryText = getQuery(queryText);
		int startIndex =
			queryText.toUpperCase().indexOf(QUERY_BEGIN_DELIMITER) + QUERY_BEGIN_DELIMITER.length();

		int endIndex =
			queryText.toUpperCase().lastIndexOf(TABLE_BEGIN_DELIMITER);

		return queryText.substring(startIndex, endIndex).trim();
	}

	/**
	 * Gets the table from the given query text.
	 *
	 * @param queryText
	 * @return	the table
	 */
	public static String getTable(String queryText) {

		queryText = getQuery(queryText);

		int startIndex = queryText.toUpperCase().lastIndexOf(TABLE_BEGIN_DELIMITER) + TABLE_BEGIN_DELIMITER.length();

		return queryText.substring(startIndex).trim();
	}

	/**
	 *
	 * @param queryText
	 * @return
	 * @throws org.openmrs.module.birt.BirtReportException
	 */
	public static String getQuery( String queryText ) throws BirtReportException {
		return splitQueryText( queryText )[0];
	}

	/**
	 *
	 * @param queryText
	 * @return
	 * @throws org.openmrs.module.birt.BirtReportException
	 */
	public static String getColumnsInfo( String queryText ) throws BirtReportException {
		assert queryText != null;
		return splitQueryText( queryText )[1];

	}

	/**
	 * Taken from the flatfile ODA plugin project.
	 *
	 * @param 	queryText
	 * @return	an array of strings that represent the query text and columns info
	 */
	private static String[] splitQueryText( String queryText ) throws BirtReportException {
		int delimiterIndex = -1;
		int columnsInfoBeginIndex = -1;

		String trimmedQueryText = queryText.trim( );

		String[] splittedQueryText = {
				"", ""
		};
		boolean inQuote = false;
		boolean isEscaped = false;
		char[] chars = trimmedQueryText.toCharArray( );

		for ( int i = 0; i < chars.length; i++ )
		{
			if ( chars[i] == '"' )
			{
				if ( !isEscaped )
					inQuote = !inQuote;
				else
					isEscaped = !isEscaped;
			}
			else if ( chars[i] == '\\' )
			{
				isEscaped = !isEscaped;
			}
			else if ( ( !inQuote ) && chars[i] == QUERY_TEXT_DELIMITER)
				delimiterIndex = i;
			else if ( ( !inQuote ) && chars[i] == COLUMNSINFO_BEGIN_DELIMITER)
			{
				columnsInfoBeginIndex = i;
				break;
			}
		}

		if ( inQuote )
			throw new BirtReportException( "Invalid query text: " + queryText );

		if ( delimiterIndex != -1
				&& columnsInfoBeginIndex != -1 )
		{
			splittedQueryText[0] = trimmedQueryText.substring( 0, delimiterIndex )
			.trim( );
			splittedQueryText[1] = trimmedQueryText.substring( columnsInfoBeginIndex + 1,
					trimmedQueryText.length( )-1 )
					.trim( );
		}
		else if ( delimiterIndex == -1
				&& columnsInfoBeginIndex == -1 )
			splittedQueryText[0] = trimmedQueryText;
		else
			throw new BirtReportException( "Invalid query text: " + queryText );

		return splittedQueryText;
	}
}