package org.openmrs.module.birt.web.controller;

import java.io.File;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.openmrs.api.APIException;
import org.openmrs.api.AdministrationService;
import org.openmrs.api.context.Context;
import org.openmrs.module.Module;
import org.openmrs.module.birt.BirtReport;
import org.openmrs.module.birt.BirtReportUtil;
import org.openmrs.module.birt.BirtReportService;
//import org.openmrs.reporting.Report;
import org.openmrs.web.WebConstants;
import org.springframework.beans.propertyeditors.CustomNumberEditor;
import org.springframework.context.support.MessageSourceAccessor;
import org.springframework.validation.BindException;
import org.springframework.web.bind.ServletRequestDataBinder;
import org.springframework.web.servlet.ModelAndView;
import org.springframework.web.servlet.mvc.SimpleFormController;
import org.springframework.web.servlet.view.RedirectView;


public class ListReportController extends SimpleFormController {
	
    /** Logger for this class and subclasses */
    protected final Log log = LogFactory.getLog(getClass());    
    
	/**
	 * 
	 * Allows for Integers to be used as values in input tags.
	 *   Normally, only strings and lists are expected 
	 * 
	 * @see org.springframework.web.servlet.mvc.BaseCommandController#initBinder(javax.servlet.http.HttpServletRequest, org.springframework.web.bind.ServletRequestDataBinder)
	 */
	protected void initBinder(HttpServletRequest request, ServletRequestDataBinder binder) throws Exception {
		super.initBinder(request, binder);
        binder.registerCustomEditor(java.lang.Integer.class, new CustomNumberEditor(java.lang.Integer.class, true));
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
		
		String view = getFormView();


		if (Context.isAuthenticated()) {
			String[] reportList = request.getParameterValues("reportId");
			
			
			BirtReportService reportService = (BirtReportService) Context.getService(BirtReportService.class);
					
			String success = "";
			String error = "";
			
			MessageSourceAccessor msa = getMessageSourceAccessor();
			String deleted = msa.getMessage("general.deleted");
			String notDeleted = msa.getMessage("general.cannot.delete");
			String textReport = msa.getMessage("Report.report");
			String noneDeleted = msa.getMessage("Report.nonedeleted");
			
			// Delete report
			if ( reportList != null ) {
				for (String reportId : reportList) {
					//TODO convenience method deleteReport(Integer) ??
					try {
						BirtReport report = reportService.getReport(Integer.valueOf(reportId));
						reportService.deleteReport(report);
						if (!success.equals("")) success += "<br>";
						success += textReport + " " + reportId + " " + deleted;
					}
					catch (APIException e) {
						log.warn("Error deleting report", e);
						if (!error.equals("")) error += "<br>";
						error += textReport + " " + reportId + " " + notDeleted;
					}
				}
			} else {
				success += noneDeleted;
			}
			view = getSuccessView();
			if (!success.equals(""))
				httpSession.setAttribute(WebConstants.OPENMRS_MSG_ATTR, success);
			if (!error.equals(""))
				httpSession.setAttribute(WebConstants.OPENMRS_ERROR_ATTR, error);
		}
			
		return new ModelAndView(new RedirectView(view));
	}

	/**
	 * 
	 * This is called prior to displaying a form for the first time.  It tells Spring
	 *   the form/command object to load into the request
	 * 
	 * @see org.springframework.web.servlet.mvc.AbstractFormController#formBackingObject(javax.servlet.http.HttpServletRequest)
	 */
    protected Object formBackingObject(HttpServletRequest request) throws ServletException {

    	// default empty object
		List<BirtReport> reportList = new ArrayList<BirtReport>();
		
		//only fill the Object is the user has authenticated properly
		if (Context.isAuthenticated()) {
	    	reportList = ((BirtReportService)
	    		Context.getService(BirtReportService.class)).filterReports( request.getParameter("filter"));
		}		
		return reportList;
    }

}




/**

Right now, Spring (MVC) out-of-the-box supports both Excel/PDF and JasperReports views. The PDF/excel ones are useful but as the complexity increases building documents with iText becomes harder and harder. JasperReports offers a powerful alternative based on XML reports that can be designed with a GUI. There's still another tool in the Open Source Java field to create custom reports, Birt. I'm not going to compare it with Jasper as that has been made several times before as it's way beyond the scope of this entry.

But if someone finally decides to stick with Birt and is using Spring then he has to know that specific support in the framework won't be available until version 2.2 (see Jira) which could take sometime yet. Fortunately, a good solution can be implemented without much effort right now.

The first thing to do is understanding the Birt runtime engine. Birt is composed of several modules and the runtime environment is one of them. It can be downloaded separatedly and installed on its own. Once downloaded, there are some steps to follow to deploy it in a web application. The final structure should be like this:



Once deployed it can be integrated with Spring. The first consideration to make is deciding what modules of the runtime engine are going to be initialized. There are two possibilities (we'll left the Chart Engine for later): the report engine and the design engine. They can be started independently or both in common. The report engine is in charge of processing predefined reports and documents. The design engine will allow a developer to create a report from scratch (programmatically) or modify a previous report. Reports created and modified by the design engine can be transformed later with the report engine.

To start the report engine you need to declare a simple bean with start and shutdown methods. Even though the Birt engine is hefty it will take very little time to load.

public void startReportEngine(ServletContext servletContext) throws Exception {
   EngineConfig config = new EngineConfig();
   config.setEngineHome("");
   config.setLogConfig(null, Level.ALL);
   IPlatformContext context = new PlatformServletContext(servletContext);
   config.setPlatformContext(context);
   Platform.startup(config);
   IReportEngineFactory factory = (IReportEngineFactory)
      Platform.createFactoryObject(
            IReportEngineFactory.EXTENSION_REPORT_ENGINE_FACTORY);
   reportEngine = factory.createReportEngine(config);
}

public void shutdown() {
   if (reportEngine != null) reportEngine.shutdown();
   Platform.shutdown();
}

The code above is taken from the Birt documentation entirely. Remember that those methods will be called when Spring is building the context so they will be executed just once. To process a report (deployed in the Reports directory) just need to add:

public byte[] createPDF(IReportRunnable design) throws Exception {
   PDFRenderContext renderContext = new PDFRenderContext();
   HashMap contextMap = new HashMap();
   contextMap.put(EngineConstants.APPCONTEXT_PDF_RENDER_CONTEXT, renderContext);
   HTMLRenderOption options = new HTMLRenderOption();
   ByteArrayOutputStream out = new ByteArrayOutputStream();
   options.setOutputStream(out);
   options.setOutputFormat("pdf");
   IRunAndRenderTask task = reportEngine.createRunAndRenderTask(design);
   task.setAppContext(contextMap);
   task.setRenderOption(options);
   task.run();
   task.close();
   return out.toByteArray();
}

That method returns a PDF as a byte[]. If you need another format just modify it (very easy to do). It takes a IReportRunnable design as a parameter. This is very useful as it allows it to be called from a newly created design or a report file

public byte[] createPDF(String reportName) throws Exception {
   return createPDF(
      reportEngine.openReportDesign(sc.getRealPath("/Reports/" + reportName)));
}

public byte[] createPDF(ReportDesignHandle reportDesign) throws Exception {
   return createPDF(reportEngine.openReportDesign(reportDesign));
}

And that's all really. Don't forget to pass the ServletContext on initialization of course! I do it by making the bean ApplicationContextAware.

It's possible that some parameters are also required. Here's a little modification that allows them:

public byte[] createPDF(IReportRunnable design, Properties parameters)
               throws Exception {
   IRunAndRenderTask runAndRenderTask =
               reportEngine.createRunAndRenderTask(design);
   for (Object param : parameters.keySet())
      runAndRenderTask.setParameterValue((String) param,
               parameters.getProperty((String) param));
   ...
}

If you are using Spring MVC to return the PDF to the client just subclass AbstractView and use a ResourceBundleViewResolver

public class BIRTPDF extends AbstractView {

   public BIRTPDF() {
      setContentType("application/pdf");
   }

   protected void buildPdfDocument(
            Map model,
            Document document,
            PdfWriter pdfWriter,
            HttpServletRequest httpServletRequest,
            HttpServletResponse httpServletResponse) throws Exception {
      byte[] bytes = (byte[]) model.get("pdf");
      httpServletResponse.getOutputStream().write(bytes);
      httpServletResponse.getOutputStream().close();
      httpServletResponse.getOutputStream().flush();
   }

   protected void renderMergedOutputModel(
            Map map, HttpServletRequest httpServletRequest,
            HttpServletResponse httpServletResponse) throws Exception {
      buildPdfDocument(map, null, null, httpServletRequest, httpServletResponse);
   }
}

The design engine is even easier to integrate. Just add a new bean with the following code:

private IDesignEngine designEngine = null;

public void startDesignEngine() throws Exception {
   DesignConfig config = new DesignConfig();
   config.setBIRTHome("");
   Platform.startup(config);
   IDesignEngineFactory factory = (IDesignEngineFactory)
      Platform.createFactoryObject(
         IDesignEngineFactory.EXTENSION_DESIGN_ENGINE_FACTORY);
   designEngine = factory.createDesignEngine(config);
}

And use the following code to create a new report programmatically

public ReportDesignHandle createReport() throws Exception {
   SessionHandle session = designEngine.newSessionHandle(ULocale.ENGLISH);
   ReportDesignHandle reportDesignHandle = session.createDesign();
   return reportDesignHandle;
}

Finally, I must add that there is another approach here that you may like more (though it's a bit more complicated IMHO).

*/
