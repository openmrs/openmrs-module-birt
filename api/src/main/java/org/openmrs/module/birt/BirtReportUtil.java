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

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.eclipse.birt.report.engine.api.IGetParameterDefinitionTask;
import org.eclipse.birt.report.engine.api.IParameterGroupDefn;
import org.eclipse.birt.report.engine.api.IParameterSelectionChoice;
import org.eclipse.birt.report.engine.api.IReportRunnable;
import org.eclipse.birt.report.engine.api.IScalarParameterDefn;
import org.eclipse.birt.report.model.api.CascadingParameterGroupHandle;
import org.eclipse.birt.report.model.api.ReportDesignHandle;
import org.eclipse.birt.report.model.api.ScalarParameterHandle;
import org.eclipse.birt.report.model.api.elements.DesignChoiceConstants;

import java.text.DecimalFormat;
import java.text.NumberFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

public class BirtReportUtil {

	private static Log log = LogFactory.getLog(BirtReportUtil.class);
	
	/**
	 * Creates a parameter based on the BIRT report parameter.
	 */
	public static ParameterDefinition getParameter(
			IGetParameterDefinitionTask task, 
			IScalarParameterDefn scalar,
			IReportRunnable reportRunnable, 
			IParameterGroupDefn group) {   

		// Initialize and populate the parameter
		ParameterDefinition parameter = new ParameterDefinition();
		parameter.setName(scalar.getName());
		parameter.setValue(scalar.getDefaultValue());
		parameter.setDefaultValue(scalar.getDefaultValue());
		parameter.setRequired(scalar.isRequired());
		parameter.setPromptText(scalar.getPromptText());
		parameter.setAllowNull(scalar.allowNull());		
		parameter.setHelpText(scalar.getHelpText());
		parameter.setDisplayFormat(scalar.getDisplayFormat());
		parameter.setHidden(scalar.isHidden());
		parameter.setConceal(scalar.isValueConcealed());

		/**
		 * TODO: MS: Re-add the below, but in a way that htmlwidgets can use


		parameter.setControlType(BirtReportUtil.getControlType(scalar.getControlType()));

		// Interpret data type (integer, string, date, datetime)
		parameter.setDataType(BirtReportUtil.getDataType(scalar.getDataType()));

		// Interpret the parameter type (simple, multi-value, ?)
		// parameter.setParameterType(BirtReportUtil.getParameterType(scalar.getParameterType()));
		 */
		
		// Get report design and find default value, prompt text and data set expression using the DE API
		ReportDesignHandle reportHandle = (ReportDesignHandle) reportRunnable.getDesignHandle( );
		ScalarParameterHandle parameterHandle = (ScalarParameterHandle) reportHandle.findParameter(scalar.getName());
		parameter.setDefaultValue(parameterHandle.getDefaultValue());
		parameter.setPromptText(parameterHandle.getPromptText());
		
		// Sets whether we should allow multiple values or not (based on parameter type simple vs multi-value)
		parameter.setAllowMultiple("multi-value".equals(parameterHandle.getParamType())?true:false);

		// If the parameter's control type is not TEXT BOX, then it is some type of SELECT LIST
		if(scalar.getControlType() != IScalarParameterDefn.TEXT_BOX) {

			// Cascaded parameter
			if ( parameterHandle.getContainer() instanceof CascadingParameterGroupHandle ) {
				Collection sList = Collections.EMPTY_LIST;
				if ( parameterHandle.getContainer( ) instanceof CascadingParameterGroupHandle ) {
					int index = parameterHandle.getContainerSlotHandle().findPosn( parameterHandle );
					Object[] keyValue = new Object[index];
					for ( int i = 0; i < index; i++ ) {
						ScalarParameterHandle handle = (ScalarParameterHandle) 
						( (CascadingParameterGroupHandle) parameterHandle.getContainer( ) ).getParameters( ).get( i );
						//Use parameter default values
						keyValue[i] = handle.getDefaultValue();
					}
					String groupName = parameterHandle.getContainer( ).getName( );
					task.evaluateQuery( groupName );

					sList = task.getSelectionListForCascadingGroup( groupName, keyValue );
					Map<Object, String> dynamicList = new HashMap<Object, String>();       


					for ( Iterator sl = sList.iterator( ); sl.hasNext( ); ) {
						IParameterSelectionChoice sI = (IParameterSelectionChoice) sl.next( );
						Object value = sI.getValue( );
						Object label = sI.getLabel( );
						log.debug( label + "--" + value);
						dynamicList.put(value,(String) label);

					}         
					parameter.setSelectionList(dynamicList);
				}         
			}

			// Scalar parameter
			else {
				Collection selectionList = task.getSelectionList( scalar.getName() );

				if ( selectionList != null ) {
					Map<Object, String> dynamicList = new HashMap<Object, String>();       

					for ( Iterator iter = selectionList.iterator(); iter.hasNext(); ) {

						IParameterSelectionChoice selectionItem = (IParameterSelectionChoice) iter.next();
						Object value = selectionItem.getValue( );
						String label = selectionItem.getLabel( );
						dynamicList.put(value,label);

					}
					parameter.setSelectionList(dynamicList);
				}
			}
		}

		log.debug("*** Parameter = " + parameter);


		return parameter;
	}
}
