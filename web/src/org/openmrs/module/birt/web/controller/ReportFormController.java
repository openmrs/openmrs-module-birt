package org.openmrs.module.birt.web.controller;

import java.io.File;
import java.io.FileInputStream;
import java.io.InputStream;
import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.SortedMap;
import java.util.TreeMap;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.lang3.StringUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.openmrs.Cohort;
import org.openmrs.api.CohortService;
import org.openmrs.api.context.Context;
import org.openmrs.module.birt.BirtReport;
import org.openmrs.module.birt.BirtReportService;
import org.openmrs.module.htmlwidgets.web.WidgetUtil;
import org.openmrs.module.reporting.common.MessageUtil;
import org.openmrs.module.reporting.common.ReflectionUtil;
import org.openmrs.module.reporting.data.encounter.definition.PatientToEncounterDataDefinition;
import org.openmrs.module.reporting.data.encounter.definition.PersonToEncounterDataDefinition;
import org.openmrs.module.reporting.data.patient.definition.PatientDataSetDataDefinition;
import org.openmrs.module.reporting.data.patient.definition.PersonToPatientDataDefinition;
import org.openmrs.module.reporting.data.person.definition.AgeAtDateOfOtherDataDefinition;
import org.openmrs.module.reporting.dataset.definition.CohortIndicatorDataSetDefinition;
import org.openmrs.module.reporting.dataset.definition.DataSetDefinition;
import org.openmrs.module.reporting.dataset.definition.EncounterDataSetDefinition;
import org.openmrs.module.reporting.dataset.definition.MultiPeriodIndicatorDataSetDefinition;
import org.openmrs.module.reporting.definition.DefinitionContext;
import org.openmrs.module.reporting.evaluation.Definition;
import org.openmrs.module.reporting.evaluation.parameter.Mapped;
import org.openmrs.module.reporting.evaluation.parameter.Parameter;
import org.openmrs.module.reporting.evaluation.parameter.Parameterizable;
import org.openmrs.module.reporting.evaluation.parameter.ParameterizableUtil;
import org.openmrs.module.reporting.report.ReportDesign;
import org.openmrs.module.reporting.report.definition.ReportDefinition;
import org.openmrs.module.reporting.report.definition.service.ReportDefinitionService;
import org.openmrs.module.reporting.report.service.ReportService;
import org.openmrs.module.reporting.web.controller.ManageDefinitionsController.DefinitionNameComparator;
import org.openmrs.propertyeditor.CohortEditor;
import org.openmrs.util.OpenmrsUtil;
import org.openmrs.web.WebConstants;
import org.springframework.beans.propertyeditors.CustomNumberEditor;
import org.springframework.util.FileCopyUtils;
import org.springframework.validation.BindException;
import org.springframework.validation.Errors;
import org.springframework.web.bind.ServletRequestDataBinder;
import org.springframework.web.bind.ServletRequestUtils;
import org.springframework.web.servlet.ModelAndView;
import org.springframework.web.servlet.mvc.SimpleFormController;
import org.springframework.web.servlet.view.RedirectView;
//import org.openmrs.propertyeditor.DataExportReportObjectEditor;
//import org.openmrs.reporting.export.DataExportReportObject;


/**
 * Simple form controller used to process most of the BIRT report module 
 * use cases. 
 * 
 * TODO Refactor the BIRT use cases into separate controllers.
 *
 * @author Justin Miranda
 *
 */
public class ReportFormController extends SimpleFormController {
	
    /** Logger for this class and subclasses */
    protected final Log log = LogFactory.getLog(getClass()); 

	/**
	 * Allows for Integers to be used as values in input tags.
	 *   Normally, only strings and lists are expected 
	 * 
	 * @see org.springframework.web.servlet.mvc.BaseCommandController#initBinder(javax.servlet.http.HttpServletRequest, org.springframework.web.bind.ServletRequestDataBinder)
	 */
	protected void initBinder(HttpServletRequest request, ServletRequestDataBinder binder) throws Exception {
		super.initBinder(request, binder);
        binder.registerCustomEditor(Integer.class, new CustomNumberEditor(Integer.class, true));
		binder.registerCustomEditor(Cohort.class, new CohortEditor());
	}
	
    /**
     * Comparator which orders Definitions based on their Display Label
     */
    public class DefinitionNameComparator implements Comparator<Class<? extends Definition>> {
		/**
		 * @see Comparator#compare(Object, Object)
		 */
		public int compare(Class<? extends Definition> o1, Class<? extends Definition> o2) {
			String key1 = MessageUtil.getDisplayLabel(o1);
			String key2 = MessageUtil.getDisplayLabel(o2);
			return key1.compareTo(key2);
		}
    }
	
	/** 
	 * 
	 * The onSubmit function receives the form/command object that was modified
	 *   by the input form and saves it to the db
	 * 
	 * @see org.springframework.web.servlet.mvc.SimpleFormController#onSubmit(javax.servlet.http.HttpServletRequest, javax.servlet.http.HttpServletResponse, java.lang.Object, org.springframework.validation.BindException)
	 */
	protected ModelAndView onSubmit(HttpServletRequest request, HttpServletResponse response, Object obj, BindException errors) throws Exception {
		
		String view = null;
		boolean formRedirect = false;
		BirtReport report = (BirtReport) obj;
		
		String mapped = ServletRequestUtils.getStringParameter(request, "mapped", "");
		String removeMappedProperty = ServletRequestUtils.getStringParameter(request, "removeMappedProperty", "");
		
		BirtReportService reportService = (BirtReportService)Context.getService(BirtReportService.class);
		log.debug("Birt report object: " + report);
		try { 

			// Save the report definition to the database
			if (request.getParameter("save") != null) { 
				log.debug("Saving report " + report);
				Integer id = report.getReportDefinition().getId();
				reportService.saveReport(report);
				request.getSession().setAttribute(WebConstants.OPENMRS_MSG_ATTR, "birt.saveReport.success");				

				// TODO redirect create report requests to the report form 
				formRedirect = true;
			}
			// Delete the report definition from the database
			else if (request.getParameter("delete") != null) { 
				log.debug("Deleting report " + report);
				reportService.deleteReport(report);
				request.getSession().setAttribute(WebConstants.OPENMRS_MSG_ATTR, "birt.deleteReport.success");

			}
			// Generate a quick preview of the report 
			else if (request.getParameter("preview") != null) {				
				log.debug("Previewing report " + report);
				reportService.previewReport(report);
				
				File file = report.getOutputFile();			
				if ( file != null) {
					try { 
						InputStream fileInputStream = new FileInputStream(file);
						String mimeType = this.getServletContext().getMimeType(file.getAbsolutePath());
						log.debug("Report preview mime type: " + mimeType);
						response.setContentType(mimeType);
						String filename = report.getReportDefinition().getId() + ".pdf";
						response.setHeader("Content-Disposition", "attachment; filename=" + filename);
						FileCopyUtils.copy(fileInputStream, response.getOutputStream());
						return null;
					} catch (Exception e) { 
						log.error("An error occurred while previewing report", e);
						request.getSession().setAttribute(WebConstants.OPENMRS_MSG_ATTR, "birt.previewReport.error");						
					}
				}			
			}
			else if (request.getParameter("downloadDataset") != null) { 	
				try { 
					response.setContentType("text/xml; charset=utf-8");
					// to do Mike
					//response.setHeader("Content-Disposition", "attachment; filename=" + report.getReportDefinition().getDataExport().getName().replace(" ", "_") + ".xml");
					response.getOutputStream().print(report.getDatasetXml());				
				} catch (Exception e) { 
					log.error("An error occurred while downloading dataset", e);
					request.getSession().setAttribute(WebConstants.OPENMRS_MSG_ATTR, "birt.datasetDownload.error");
				}
			}
			else if (request.getParameter("downloadReport") != null) { 
				// Get the report file
				String reportDesignPath = report.getReportDesignPath();
				File reportDesignFile = new File(reportDesignPath);
	
				// Write report design file to response
				InputStream fileInputStream = new FileInputStream(reportDesignFile);
				response.setContentType("text/xml; charset=utf-8");
				response.setHeader("Content-Disposition", "attachment; filename=" + report.getReportDefinition().getId() + ".rptdesign");
				FileCopyUtils.copy(fileInputStream, response.getOutputStream());
			}
			else if (request.getParameter("removeReportDesign") != null) { 
				String uuid = request.getParameter("uuid");
				ReportService rs = Context.getService(ReportService.class);
		    	ReportDesign design = rs.getReportDesignByUuid(uuid);
		    	rs.purgeReportDesign(design);		    	
			}
			else if ("mappedForm".equals(mapped)) {
				String newKey = request.getParameter("newKey");
				String definitionName = ServletRequestUtils.getStringParameter(request, "definitionName", "");
				String uuid = request.getParameter("uuid");
				String property  = request.getParameter("property");
				Class<? extends Parameterizable> type = (Class<? extends Parameterizable>) Class.forName(request.getParameter("type")); 
				String mappedUuid = request.getParameter("mappedUuid");
				String currentKey =  request.getParameter("currentKey");
				
		    	Parameterizable parent = ParameterizableUtil.getParameterizable(uuid, type);
		    	Field f = ReflectionUtil.getField(type, property);
		    	Class<?> fieldType = ReflectionUtil.getFieldType(f);
		    	
				Class<? extends Parameterizable> mappedType = null;
				if (StringUtils.isNotEmpty(property)) {
					mappedType = ParameterizableUtil.getMappedType(type, property);
				}
				
				Mapped m = null;
				Object previousValue = ReflectionUtil.getPropertyValue(parent, property);
				
				if (StringUtils.isNotEmpty(mappedUuid)) {
					Parameterizable valToSet = ParameterizableUtil.getParameterizable(mappedUuid, mappedType);
		    		
					m = new Mapped();
		    		m.setParameterizable(valToSet);
		    		
		        	for (Parameter p : valToSet.getParameters()) {
		        		String valueType = request.getParameterValues("valueType_"+p.getName())[0];
		        		String[] value = request.getParameterValues(valueType+"Value_"+p.getName());
		        		if (value != null && value.length > 0) {
		    	    		Object paramValue = null;
		    	    		if (StringUtils.isEmpty(valueType) || valueType.equals("fixed")) {
		    	    			String fixedValueString = OpenmrsUtil.join(Arrays.asList(value), ",");
		    	    			paramValue = WidgetUtil.parseInput(fixedValueString, p.getType());
		    	    		}
		    	    		else {
		    	    			paramValue = "${"+value[0]+"}";
		    	    		}
		    	    		if (paramValue != null) {
		    	    			m.addParameterMapping(p.getName(), paramValue);
		    	    		}
		        		}
		        	}
				}
				
		        if (previousValue != null || m != null) {
	        		
		    		if (List.class.isAssignableFrom(fieldType)) {
		    			List newValue = null;
		    			if (previousValue == null) {
		    				newValue = new ArrayList();
		    				newValue.add(m);
		    			}
		    			else if (m != null) {
		    				newValue = (List)previousValue;
		    				if (StringUtils.isEmpty(newKey)) {
		    					newValue.add(m);
		    				}
		    				else {
		    					int listIndex = Integer.parseInt(newKey);
		    					newValue.set(listIndex, m);
		    				}
		    			}
		    			ReflectionUtil.setPropertyValue(parent, f, newValue);
		    		}
		    		else if (Map.class.isAssignableFrom(fieldType)) {
		    			if (m != null) {
		    				Map newValue = (previousValue == null ? new HashMap() : (Map)previousValue);
		    				newValue.put(newKey, m);
		        			if (!newKey.equals(currentKey) && currentKey != null) {
		        				newValue.remove(currentKey);
		        			}
		       				ReflectionUtil.setPropertyValue(parent, f, newValue);
		    			}
		    		}
		    		else if (Mapped.class.isAssignableFrom(fieldType)) {
		    			ReflectionUtil.setPropertyValue(parent, f, m);
		    		}
		    		else {
		    			throw new IllegalArgumentException("Cannot set property of type: " + fieldType + " to " + m);
		    		}
		    	}
		    	
		    	ParameterizableUtil.saveParameterizable(parent);					    	
		    
				System.out.println("keyName " + newKey);
				System.out.println("definitionName " + definitionName);	
				System.out.println("mappedUuid " + mappedUuid);	
			}
			else if ("removeDatasetMapping".equals(removeMappedProperty)) {				
				String uuid = request.getParameter("uuid");
				String property  = request.getParameter("property");
				Class<? extends Parameterizable> type = (Class<? extends Parameterizable>) Class.forName(request.getParameter("type")); 				
				String currentKey =  request.getParameter("currentKey");
				
				Parameterizable parent = ParameterizableUtil.getParameterizable(uuid, type);
		    	Field f = ReflectionUtil.getField(type, property);
		    	Class<?> fieldType = ReflectionUtil.getFieldType(f);	
				Object previousValue = ReflectionUtil.getPropertyValue(parent, property);
				
		        if (List.class.isAssignableFrom(fieldType)) {
		        	List v = (List)previousValue;
					int listIndex = Integer.parseInt(currentKey);
					v.remove(listIndex);
		        }
				else if (Map.class.isAssignableFrom(fieldType)) {
					Map v = (Map)previousValue;
					v.remove(currentKey);
				}
				else {
		        	throw new IllegalArgumentException("Cannot remove property in fieldType: " + fieldType + " with key " + currentKey);
		    	}
		        
		        ReflectionUtil.setPropertyValue(parent, f, previousValue);
		    	ParameterizableUtil.saveParameterizable(parent); 
		    	
			}
			else { 
				request.getSession().setAttribute(WebConstants.OPENMRS_MSG_ATTR, "birt.noAction");
			} 			
			
		} catch (Exception e) { 
			request.getSession().setAttribute(WebConstants.OPENMRS_MSG_ATTR, "birt.general.error");			
			log.error(e);
		}

		// For posts that should be redirected to the form
		if (formRedirect) { 
			return showForm(request, response, errors);
		}
		// By default, we'll just go back to the list page and display an error message
		else {
			view = getSuccessView();		
		}
		
		return new ModelAndView(new RedirectView(view));
	}

	/**
	 * Gets the reference data required for the reporting use cases implemented within this controller.
	 * 
	 * @param	request
	 * @param	command
	 * @param	errors
	 * @return	a map containing data used in the presentation layer
	 */
    protected Map referenceData(HttpServletRequest request, Object command, Errors errors) {
		Map<Object, Object> data = new HashMap<Object, Object>();
		BirtReportService reportService = (BirtReportService) Context.getService(BirtReportService.class);
		
		//BirtReport report = (BirtReport) command;
		
		data.put("reports", reportService.getReports());
		data.put("cohorts", Context.getService(CohortService.class).getAllCohorts());
		
		String reportId = request.getParameter("reportId");
		String uuid = request.getParameter("uuid");
		String type = request.getParameter("type");
		
		BirtReport report = null;

    	if (reportId != null) {    		
    		report = reportService.getReport(Integer.valueOf(reportId));
    	}    	
 	    	
    	ReportDefinitionService rs = Context.getService(ReportDefinitionService.class); 
    	ReportDefinition r = rs.getDefinition(uuid, ReportDefinition.class); 
    	data.put("reportt", r);
    	
		List<DataSetDefinition> dataSetDefinitionList = DefinitionContext.getDataSetDefinitionService().getAllDefinitions(false);
    	
    	List<String> dsdNames = new ArrayList<String>();
    	SortedMap<String, String> dsdProperties = new TreeMap<String, String>();
    	for (DataSetDefinition p : dataSetDefinitionList) {
    		dsdProperties.put(p.getName(), p.getUuid()); 
    		dsdNames.add(p.getName());
    	}
    	Collections.sort(dsdNames, String.CASE_INSENSITIVE_ORDER);
    	
    	data.put("dataSetDefinitionNames", dsdNames);
    	data.put("dataSetDefinitionProperties", dsdProperties);    	
    	
    	List<String> existingKeys = new ArrayList<String>();
    	for ( String key : r.getDataSetDefinitions().keySet() ) {    		
    		existingKeys.add(key);    		
    	}
    	
    	data.put("existingKeys", existingKeys);    	
    	
		
		if (reportId != null){
			List<ReportDesign> designs = new ArrayList<ReportDesign>();
			
			//only fill the Object is the user has authenticated properly
			if (Context.isAuthenticated()) {
				//designs = reportService.getReportDesigns();
				designs = reportService.filterReportDesigns(Integer.valueOf(reportId));
			}		
			data.put("designs", designs);
		}
		
    	return data;
    }
	
	/**
	 * This is called prior to displaying a form for the first time.  It tells Spring
	 *   the form/command object to load into the request
	 * 
	 * @see org.springframework.web.servlet.mvc.AbstractFormController#formBackingObject(javax.servlet.http.HttpServletRequest)
	 */
    protected Object formBackingObject(HttpServletRequest request) throws ServletException {

		BirtReport report = null;
		
    	String reportId = request.getParameter("reportId");
    	if (reportId != null) { 	    		
    		
    		BirtReportService reportService = 
    			(BirtReportService)Context.getService(BirtReportService.class);
    		
    		report = reportService.getReport(Integer.valueOf(reportId));
    	}
		
		if (report == null)
			report = new BirtReport();		
   	
        return report;
    }
    
}

