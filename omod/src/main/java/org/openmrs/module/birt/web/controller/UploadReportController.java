package org.openmrs.module.birt.web.controller;

import org.apache.commons.lang.StringUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.openmrs.api.context.Context;
import org.openmrs.module.birt.BirtReport;
import org.openmrs.module.birt.BirtReportException;
import org.openmrs.module.birt.service.BirtReportService;
import org.openmrs.module.reporting.report.ReportDesign;
import org.openmrs.module.reporting.report.ReportDesignResource;
import org.openmrs.module.reporting.report.definition.service.ReportDefinitionService;
import org.openmrs.module.reporting.report.renderer.ReportRenderer;
import org.openmrs.module.reporting.report.service.ReportService;
import org.openmrs.web.WebConstants;
import org.springframework.validation.BindException;
import org.springframework.web.bind.ServletRequestUtils;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.multipart.MultipartHttpServletRequest;
import org.springframework.web.servlet.ModelAndView;
import org.springframework.web.servlet.mvc.SimpleFormController;
import org.springframework.web.servlet.view.RedirectView;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;
import java.io.IOException;
import java.io.InputStream;


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
		String reportDesignUuid = null;
		String uuid = null;
		if (Context.isAuthenticated()) {
			try {
				if (request instanceof MultipartHttpServletRequest) {

					MultipartHttpServletRequest multipartRequest = (MultipartHttpServletRequest) request;
					MultipartFile reportFile = (MultipartFile) multipartRequest.getFile("reportFile");
					reportId = multipartRequest.getParameter("reportId");
					String fileName = reportFile.getOriginalFilename();
					reportDesignUuid = multipartRequest.getParameter("reportDesignUuid");
					String description = multipartRequest.getParameter("description");
					String name = multipartRequest.getParameter("name");
					//String reportDefinitionUuid = multipartRequest.getParameter("reportDefinitionUuid");
					String resourceUuid = multipartRequest.getParameter("resourceUuid");
					uuid = multipartRequest.getParameter("uuid");

					// Check to see if the report exists already 

					BirtReportService service = (BirtReportService) Context.getService(BirtReportService.class);						
					BirtReport report = service.getReport(ServletRequestUtils.getIntParameter(multipartRequest, "reportId"));
					if (report == null) { 


					}						
					// If the report doesn't exist, then we upload anyway and create a new report using the name of
					// the file 
					else { 

						ReportService rs = Context.getService(ReportService.class);
							
						ReportDesign design = null;
						if (StringUtils.isNotEmpty(reportDesignUuid)) {
							design = rs.getReportDesignByUuid(reportDesignUuid);
						}
						if (design == null) {
							design = new ReportDesign();
						}
						
						Class<? extends ReportRenderer> rendererType = (Class<? extends ReportRenderer>) Class.forName(request.getParameter("rendererType")); 

						design.setName(name);
						design.setDescription(description);
						design.setReportDefinition(Context.getService(ReportDefinitionService.class).getDefinitionByUuid(uuid));
						design.setRendererType(rendererType);
						
						ReportDesignResource resource = null;
						if (StringUtils.isNotEmpty(resourceUuid)) {
							resource = rs.getReportDesignByUuid(reportDesignUuid).getResourceByUuid(resourceUuid);
						}
						if (resource == null) {
							resource = new ReportDesignResource();
						}

						int index = fileName.lastIndexOf(".");
						resource.setReportDesign(design);
						resource.setContentType(reportFile.getContentType());
						resource.setName(fileName.substring(0, index));
						resource.setExtension(fileName.substring(index+1));
						resource.setContents(reportFile.getBytes());
						design.getResources().add(resource);

						design = rs.saveReportDesign(design);
						reportDesignUuid = design.getUuid();
					}				
					

					// Copy uploaded file 
					if (reportFile != null && !reportFile.isEmpty()) {

						reportFile.getName();
						InputStream inputStream = reportFile.getInputStream();
						String contentType = reportFile.getContentType();

						// TODO Handle a zip report upload 

						// Copy report design file to 
						log.debug("Content type: " + contentType);

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
				view = request.getContextPath() + "/module/birt/report.form?reportId=" + reportId + "&uuid=" + uuid + "&reportDesignUuid=" + reportDesignUuid;
			}
			else { 
				view = getSuccessView();
			}
			return new ModelAndView(new RedirectView(view));
		}

		httpSession.setAttribute(WebConstants.OPENMRS_ERROR_ATTR, "birt.reportDesign.notSaved");
		return showForm(request, response, errors);
	}
}
