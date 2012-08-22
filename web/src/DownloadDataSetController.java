import java.util.HashMap;
import java.util.Map;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.openmrs.api.context.Context;
import org.openmrs.module.reporting.report.ReportDesign;
import org.openmrs.module.reporting.report.ReportDesignResource;
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
		System.out.println("DownloadReportDesignController.handleRequest() method called");
		
		// If you want to write directly to the response
		try { 
			ReportService reportService = Context.getService(ReportService.class);
			String uuid = request.getParameter("uuid"); 
			ReportDesign reportDesign = reportService.getReportDesignByUuid(request.getParameter("uuid"));					
					
			// Get the report design by ID
			if (reportDesign == null) { 
				reportDesign = reportService.getReportDesign(Integer.valueOf(request.getParameter("id")));
			}
				
			// Get the first report design resource 
			ReportDesignResource reportDesignResource = 
					reportDesign.getResources().iterator().next();
			
			// Set headers and content type of report file
			//response.setContentType(getServletContext().getMimeType(reportDesignResource.getResourceFilename()));
			response.setHeader("Content-Disposition", "attachment; filename=\"" + reportDesignResource.getResourceFilename() + "\"");
							
			// Write report design resource to response
			//FileCopyUtils.copy(fileInputStream, response.getOutputStream());
			response.getOutputStream().write(reportDesignResource.getContents());
			
			return null;
			
		} catch (Exception e) { 
			// If you wanted write out to a JSP, you'd uncomment the following lines
			Map<String, Object> model = new HashMap<String, Object>();
			model.put("error", e);		
			return new ModelAndView("/module/birt/downloadReportDesign", "model", model);			
		}
	}

}
