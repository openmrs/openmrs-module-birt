package org.openmrs.module.birt.web.controller;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.List;
import java.util.Vector;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.openmrs.Cohort;
import org.openmrs.api.context.Context;
//import org.openmrs.reporting.report.ReportDefinition;
import org.openmrs.web.WebConstants;
import org.openmrs.module.birt.BirtReport;
import org.openmrs.module.birt.BirtReportException;
import org.openmrs.module.birt.BirtReportService;
import org.openmrs.module.birt.BirtReportUtil;
import org.springframework.util.FileCopyUtils;
import org.springframework.validation.BindException;
import org.springframework.web.bind.ServletRequestUtils;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.multipart.MultipartHttpServletRequest;
import org.springframework.web.multipart.commons.CommonsMultipartFile;
import org.springframework.web.servlet.ModelAndView;
import org.springframework.web.servlet.mvc.SimpleFormController;
import org.springframework.web.servlet.view.RedirectView;
import org.openmrs.module.reporting.report.definition.ReportDefinition;

public class UploadReportController extends SimpleFormController {
		
    /** Logger for this class and subclasses */
    protected final Log log = LogFactory.getLog(getClass());
        
	/** 
	 * 
	 * The onSubmit function receives the form/command object that was modified
	 *   by the input form and saves it to the db
	 * 
	 * @see org.springframework.web.servlet.mvc.SimpleFormController#onSubmit(javax.servlet.http.HttpServletRequest, javax.servlet.http.HttpServletResponse, java.lang.Object, org.springframework.validation.BindException)
	 */
	protected ModelAndView onSubmit(HttpServletRequest request, HttpServletResponse response, Object obj, BindException errors) throws Exception {
		
		log.debug("Uploading report design file");
		HttpSession httpSession = request.getSession();
		String view = getFormView();
		String reportId = null;
		if (Context.isAuthenticated()) {
			try {
				if (request instanceof MultipartHttpServletRequest) {
					
			        MultipartHttpServletRequest multipartRequest = (MultipartHttpServletRequest) request;
			        MultipartFile reportFile = (MultipartFile) multipartRequest.getFile("reportFile");
					reportId = multipartRequest.getParameter("reportId");

					
					// Check to see if the report exists already 
					
					BirtReportService service = (BirtReportService) Context.getService(BirtReportService.class);						
					BirtReport report = service.getReport(ServletRequestUtils.getIntParameter(multipartRequest, "reportId"));
						
					if (report != null) { 
						
						
					}						
					// If the report doesn't exist, then we upload anyway and create a new report using the name of
					// the file 
					else { 
						ReportDefinition reportDefinition = new ReportDefinition();
						reportDefinition.setName(reportFile.getName());
						report.setReportDefinition(reportDefinition);
						service.saveReport(report);						
					}
					
					// Copy uploaded file 
					if (reportFile != null && !reportFile.isEmpty()) {
						
						reportFile.getName();
				        InputStream inputStream = reportFile.getInputStream();
				        String contentType = reportFile.getContentType();
												
						// TODO Handle a zip report upload 
				        
				        // Copy report design file to 
				        log.debug("Content type: " + contentType);
				        
						File reportDirectory = BirtReportUtil.getReportRepository();
				        String reportFilename = reportDirectory.getAbsolutePath() + System.getProperty("file.separator") + reportId + ".rptdesign";				        
				        
				        FileCopyUtils.copy(inputStream, new FileOutputStream(reportFilename));
				        
						// On successful upload, save message to request
						request.getSession().setAttribute(WebConstants.OPENMRS_MSG_ATTR, "birt.uploadReport.success");

					}
					else {
						throw new BirtReportException("Upload failed because report design does not exist");
					}
					
				}
			}
			catch (IOException e) {
				log.error(e);
				errors.reject(e.getMessage());
				httpSession.setAttribute(WebConstants.OPENMRS_ERROR_ATTR, "birt.reportDesign.notSaved");
				return showForm(request, response, errors);
			}
			
			
			// redirect to the report design page if a successful upload occurred
			if (reportId != null) { 
				view = request.getContextPath() + "/module/birt/report.form?reportId=" + reportId;
			}
			else { 
				view = getSuccessView();
			}
			return new ModelAndView(new RedirectView(view));
		}
		
		httpSession.setAttribute(WebConstants.OPENMRS_ERROR_ATTR, "birt.reportDesign.notSaved");
		return showForm(request, response, errors);
	}

	/**
	 * 
	 * This is called prior to displaying a form for the first time.  It tells Spring
	 *   the form/command object to load into the request
	 * 
	 * @see org.springframework.web.servlet.mvc.AbstractFormController#formBackingObject(javax.servlet.http.HttpServletRequest)
	 */
    protected Object formBackingObject(HttpServletRequest request) throws ServletException {

		//default empty Object
		//Set<Report> reportList = new HashSet<Report>();
		
		//only fill the Object is the user has authenticated properly
		//if (Context.isAuthenticated()) {
			//ReportService rs = Context.getReportService();
			//ReportService rs = new TestReportService();
	    	//reportList = rs.getAllReports();
		//}
		
		//reportList.addAll(getReports());
        //return reportList;
    	BirtReportService reportService = (BirtReportService)Context.getService(BirtReportService.class);
    	return reportService.getReports();
    }

	
}
