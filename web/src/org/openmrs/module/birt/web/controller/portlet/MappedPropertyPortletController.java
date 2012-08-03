package org.openmrs.module.birt.web.controller.portlet;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.openmrs.api.context.Context;
import org.openmrs.module.birt.BirtReport;
import org.openmrs.module.birt.BirtReportService;
import org.openmrs.module.reporting.common.MessageUtil;
import org.openmrs.module.reporting.data.encounter.definition.PatientToEncounterDataDefinition;
import org.openmrs.module.reporting.data.encounter.definition.PersonToEncounterDataDefinition;
import org.openmrs.module.reporting.data.patient.definition.PatientDataSetDataDefinition;
import org.openmrs.module.reporting.data.patient.definition.PersonToPatientDataDefinition;
import org.openmrs.module.reporting.data.person.definition.AgeAtDateOfOtherDataDefinition;
import org.openmrs.module.reporting.dataset.definition.CohortIndicatorDataSetDefinition;
import org.openmrs.module.reporting.dataset.definition.DataSetDefinition;
import org.openmrs.module.reporting.dataset.definition.EncounterDataSetDefinition;
import org.openmrs.module.reporting.dataset.definition.MultiPeriodIndicatorDataSetDefinition;
import org.openmrs.module.reporting.dataset.definition.service.DataSetDefinitionService;
import org.openmrs.module.reporting.definition.DefinitionContext;
import org.openmrs.module.reporting.evaluation.Definition;
import org.openmrs.module.reporting.web.controller.ManageDefinitionsController.DefinitionNameComparator;
import org.springframework.validation.BindException;
import org.springframework.web.servlet.ModelAndView;
import org.springframework.web.servlet.mvc.SimpleFormController;

public class MappedPropertyPortletController extends SimpleFormController {
	
	/**
	 * Logger for this class and subclasses
	 */
	protected static final Log log = LogFactory.getLog(MappedPropertyPortletController.class);
	
	@Override
	protected ModelAndView onSubmit(HttpServletRequest request, HttpServletResponse response, Object command, BindException errors) throws Exception {
		
		
		
		
		return null;
		
	}
	
	@Override
	protected Map<String, Object> referenceData(HttpServletRequest request) throws Exception {
		System.out.println("hello from mapped controller");
		Map<String, Object> map = new HashMap<String, Object>();
		
		BirtReportService reportService = (BirtReportService)Context.getService(BirtReportService.class);
		
		//List<? extends Definition> definitions = DefinitionContext.getAllDefinitions(type, true);
		
		map.put("reports", reportService.getAllBirtReports());
		
		//List<DataSetDefinition> definitions = Context.getService(DataSetDefinitionService.class).getAllDefinitions(true);
		
		//List<? extends Definition> definitionsf = Context.getService(DataSetDefinitionService.class).getAllDefinitions(true);

		
		// Add all dataset definition to the request (allow user to choose)
    	//map.put("dataSetDefinitions", definitionsf); 	
		
		
    	List<Class<? extends Definition>> hiddenDefinitions = new ArrayList<Class<? extends Definition>>();
    	hiddenDefinitions.add(CohortIndicatorDataSetDefinition.class);
    	hiddenDefinitions.add(MultiPeriodIndicatorDataSetDefinition.class);
    	hiddenDefinitions.add(EncounterDataSetDefinition.class);
    	hiddenDefinitions.add(PatientDataSetDataDefinition.class);
    	hiddenDefinitions.add(AgeAtDateOfOtherDataDefinition.class);
    	hiddenDefinitions.add(PersonToPatientDataDefinition.class);
    	hiddenDefinitions.add(PersonToEncounterDataDefinition.class);
    	hiddenDefinitions.add(PatientToEncounterDataDefinition.class);
    	
    	// Construct a Map of Definition Type to List of Definitions
    	Map<Class<? extends Definition>, List<Definition>> defsByType = new TreeMap<Class<? extends Definition>, List<Definition>>(new DefinitionNameComparator());
       	
    	// Initialize the Map with all known supported types
    	for (Class<? extends Definition> supportedType : DefinitionContext.getDefinitionService(DataSetDefinition.class).getDefinitionTypes()) {
    		if (!hiddenDefinitions.contains(supportedType)) {
    			defsByType.put(supportedType, new ArrayList<Definition>());
    		}
    	}
    	
    	map.put("definitions", defsByType);			
		
		return map;
	}
	
	/**
	 * This is called prior to displaying a form for the first time. It tells Spring the
	 * form/command object to load into the request
	 * 
	 * @see org.springframework.web.servlet.mvc.AbstractFormController#formBackingObject(javax.servlet.http.HttpServletRequest)
	 */
	@Override
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

}
