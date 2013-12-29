package org.openmrs.module.birt.web.controller;

import org.apache.commons.lang.StringUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.openmrs.Cohort;
import org.openmrs.api.CohortService;
import org.openmrs.api.context.Context;
import org.openmrs.module.birt.BirtReport;
import org.openmrs.module.birt.service.BirtReportService;
import org.openmrs.module.htmlwidgets.web.WidgetUtil;
import org.openmrs.module.reporting.common.MessageUtil;
import org.openmrs.module.reporting.common.ReflectionUtil;
import org.openmrs.module.reporting.dataset.definition.DataSetDefinition;
import org.openmrs.module.reporting.definition.DefinitionContext;
import org.openmrs.module.reporting.evaluation.Definition;
import org.openmrs.module.reporting.evaluation.parameter.Mapped;
import org.openmrs.module.reporting.evaluation.parameter.Parameter;
import org.openmrs.module.reporting.evaluation.parameter.Parameterizable;
import org.openmrs.module.reporting.evaluation.parameter.ParameterizableUtil;
import org.openmrs.module.reporting.report.ReportDesign;
import org.openmrs.module.reporting.report.ReportDesignResource;
import org.openmrs.module.reporting.report.definition.ReportDefinition;
import org.openmrs.module.reporting.report.definition.service.ReportDefinitionService;
import org.openmrs.module.reporting.report.service.ReportService;
import org.openmrs.propertyeditor.CohortEditor;
import org.openmrs.util.OpenmrsUtil;
import org.openmrs.web.WebConstants;
import org.springframework.beans.propertyeditors.CustomNumberEditor;
import org.springframework.validation.BindException;
import org.springframework.validation.Errors;
import org.springframework.web.bind.ServletRequestDataBinder;
import org.springframework.web.bind.ServletRequestUtils;
import org.springframework.web.servlet.ModelAndView;
import org.springframework.web.servlet.mvc.SimpleFormController;
import org.springframework.web.servlet.view.RedirectView;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;
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
				
		HttpSession httpSession = request.getSession();					
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
			// Update report details
			else if (request.getParameter("editReportDetails")  != null) {
				ReportDefinitionService rs = Context.getService(ReportDefinitionService.class);
				String uuid = request.getParameter("uuid");
				ReportDefinition r = rs.getDefinitionByUuid(uuid);
				String reportId = request.getParameter("reportId");
				String name = request.getParameter("name");
				String description = request.getParameter("description");
				r.setName(name);
				r.setDescription(description);
				rs.saveDefinition(r);				
				request.getSession().setAttribute(WebConstants.OPENMRS_MSG_ATTR, "birt.saveReport.success");
				view = request.getContextPath() + "/module/birt/report.form?reportId=" + reportId + "&uuid=" + uuid;
			}
			// Delete the report definition from the database
			else if (request.getParameter("delete") != null) { 
				log.debug("Deleting report " + report);
				reportService.deleteReport(report);
				request.getSession().setAttribute(WebConstants.OPENMRS_MSG_ATTR, "birt.deleteReport.success");

			}
			// Generate a quick preview of the report 
			else if (request.getParameter("preview") != null) {				
				// MS: TODO I got rid of this, can re-implement if we need it
			}
			else if (request.getParameter("downloadDataset") != null) {
				// MS: TODO I got rid of this, can re-implement if we need it
			}
			else if (request.getParameter("downloadReport") != null) {
				// MS: TODO I got rid of this, can re-implement if we need it
				// This is also in the datasetformcontroller.  There is so much repeated everywhere!
			}
			else if (request.getParameter("removeReportDesign") != null) { 
				String uuid = request.getParameter("uuid");
				ReportService rs = Context.getService(ReportService.class);
		    	ReportDesign design = rs.getReportDesignByUuid(uuid);
		    	rs.purgeReportDesign(design);
		    	//view = request.getContextPath() + "/module/birt/report.form?reportId=" + reportId + "&uuid=" + uuid;
			}
			else if ("mappedForm".equals(mapped)) {
				String newKey = request.getParameter("newKey");
				String definitionName = ServletRequestUtils.getStringParameter(request, "definitionName", "");
				String uuid = request.getParameter("uuid");
				String reportId = request.getParameter("reportId");
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
		    	
		    	view = request.getContextPath() + "/module/birt/report.form?reportId=" + reportId + "&uuid=" + uuid + "&type=" + type;

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

		if ( view != null ) {
			return new ModelAndView(new RedirectView(view));
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
		BirtReportService reportService = Context.getService(BirtReportService.class);

		data.put("reports", reportService.getAllReports());
		data.put("cohorts", Context.getService(CohortService.class).getAllCohorts());

		String uuid = request.getParameter("uuid");
				
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
    	
    	try {
    		ReportService rrs = Context.getService(ReportService.class);
    		String reportDesignUuid = request.getParameter("reportDesignUuid");
    		ReportDesign design = null;

    		if ( rrs.getReportDesignByUuid(reportDesignUuid) == null )
    			reportDesignUuid = "";
		
    		if (StringUtils.isNotEmpty(reportDesignUuid)) {
    			design = rrs.getReportDesignByUuid(reportDesignUuid);
    		}
    		else {
				design = new ReportDesign();
				if (StringUtils.isNotEmpty(uuid)) {
					design.setReportDefinition(Context.getService(ReportDefinitionService.class).getDefinitionByUuid(uuid));
				}
			}
    		data.put("design", design);

			ReportDesignResource resource = null;
			if ( design != null ) {
				if (!design.getResources().isEmpty()) {
					for ( ReportDesignResource rdr : design.getResources() ) {
						if ( rdr.getReportDesign().getId().equals(design.getId()) ) {
							resource = rdr;
							break;
						}
					}
					data.put("resource", resource);
				}
			}
    	
    	}
		catch(NumberFormatException nfe) {
 		   System.out.println("Could not parse " + nfe);
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

