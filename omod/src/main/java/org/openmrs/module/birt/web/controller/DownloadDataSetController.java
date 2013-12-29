package org.openmrs.module.birt.web.controller;

import java.util.HashMap;
import java.util.Map;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.openmrs.api.APIException;
import org.openmrs.api.context.Context;
import org.openmrs.module.reporting.dataset.definition.DataSetDefinition;
import org.openmrs.module.reporting.dataset.definition.service.DataSetDefinitionService;
import org.openmrs.module.reporting.evaluation.EvaluationContext;
import org.openmrs.module.reporting.report.ReportData;
import org.openmrs.module.reporting.report.ReportDesign;
import org.openmrs.module.reporting.report.ReportDesignResource;
import org.openmrs.module.reporting.report.definition.ReportDefinition;
import org.openmrs.module.reporting.report.definition.service.ReportDefinitionService;
import org.openmrs.module.reporting.report.renderer.CsvReportRenderer;
import org.openmrs.module.reporting.report.renderer.ReportRenderer;
import org.openmrs.module.reporting.report.service.ReportService;
import org.springframework.web.servlet.ModelAndView;
import org.springframework.web.servlet.mvc.Controller;

public class DownloadDataSetController implements Controller {
	
    /** Logger for this class and subclasses */
    protected final Log log = LogFactory.getLog(getClass());

	public ModelAndView handleRequest(HttpServletRequest request,
			HttpServletResponse response) throws Exception {
		/**
		 * 33cfd5be-3f49-4483-9980-06e25773c8a5
		 */
		System.out.println("org.openmrs.module.birt.controller.DownloadDataSetController.handleRequest() method called");
		
		// If you want to write directly to the response
		try {
			String uuid = request.getParameter("uuid");
			DataSetDefinitionService service = Context.getService(DataSetDefinitionService.class);
	    	
	    	DataSetDefinition dataSetDefinition = null;
	    	if (uuid != null) { 
				log.debug("Retrieving dataset definition by uuid " + uuid);
	    		dataSetDefinition = service.getDefinitionByUuid(uuid);    	
	    	}

	    	
	    	log.debug("Dataset definition: " + dataSetDefinition);
	    	
	    	if (dataSetDefinition == null)
           		throw new APIException("The dataset definition that you selected could not be found.");  
	    	
	    	// Create evaluation context for running report
    		EvaluationContext context = new EvaluationContext();
	    	
	    	// Evaluate dataset report
	    	ReportDefinition reportDefinition = new ReportDefinition();
	    	reportDefinition.addDataSetDefinition(dataSetDefinition.getName(), dataSetDefinition, null);
	    	ReportData reportData = Context.getService(ReportDefinitionService.class).evaluate(reportDefinition, context);
	    	
	    	// Render using one of the given formats
	    	ReportRenderer renderer = new CsvReportRenderer();
	    	
	    	// Write rendered data to response
	    	response.setContentType(renderer.getRenderedContentType(reportDefinition, null));
			response.setHeader("Content-Disposition", "attachment; filename=\"" + dataSetDefinition.getName() + "." + "csv" + "\"");  	    	
	    	renderer.render(reportData, null, response.getOutputStream());
	    	response.getOutputStream().close();
			//response.getOutputStream().write(reportDesignResource.getContents());
			
			return null;
			
		} catch (Exception e) { 
			// If you wanted write out to a JSP, you'd uncomment the following lines
			Map<String, Object> model = new HashMap<String, Object>();
			model.put("error", e);		
			return new ModelAndView("/module/birt/downloadDataSet", "model", model);			
		}
	}

}
