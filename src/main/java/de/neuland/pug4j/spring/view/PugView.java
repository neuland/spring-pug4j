package de.neuland.pug4j.spring.view;

import java.io.IOException;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.io.Writer;
import java.util.Locale;
import java.util.Map;

import de.neuland.pug4j.PugEngine;
import de.neuland.pug4j.RenderContext;
import de.neuland.pug4j.exceptions.PugCompilerException;
import de.neuland.pug4j.exceptions.PugException;
import de.neuland.pug4j.template.PugTemplate;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.web.servlet.view.AbstractTemplateView;


public class PugView extends AbstractTemplateView {

	private String encoding;
	private PugEngine engine;
	private RenderContext renderContext;
	private boolean renderExceptions = false;
	private String contentType;

	@Override
	protected void renderMergedTemplateModel(Map<String, Object> model, HttpServletRequest request, HttpServletResponse response) throws Exception {
		doRender(model, response);
	}

	private void doRender(Map<String, Object> model, HttpServletResponse response) throws IOException {
		logger.trace("Rendering Pug template [" + getUrl() + "] in PugView '" + getBeanName() + "'");

		if (contentType != null) {
			response.setContentType(contentType);
		}

		PrintWriter responseWriter = response.getWriter();
		RenderContext context = getRenderContext();

		if (renderExceptions) {
			Writer writer = new StringWriter();
			try {
				engine.render(getTemplate(), model, context, writer);
				responseWriter.write(writer.toString());
			} catch (PugException e) {
				String htmlString = e.toHtmlString(writer.toString());
				responseWriter.write(htmlString);
				logger.error("failed to render template [" + getUrl() + "]", e);
			} catch (IOException e) {
				responseWriter.write("<pre>could not find template: " + getUrl() + "\n");
				e.printStackTrace(responseWriter);
				responseWriter.write("</pre>");
				logger.error("could not find template", e);
			}
		} else {
			try {
				engine.render(getTemplate(), model, context, responseWriter);
			} catch (Throwable e) {
				logger.error("failed to render template [" + getUrl() + "]\n", e);
			}
		}
	}

	protected PugTemplate getTemplate() throws IOException, PugException {
		return engine.getTemplate(getUrl());
	}

	@Override
	public boolean checkResource(Locale locale) throws Exception {
		return engine.templateExists(getUrl());
	}

	protected void processTemplate(PugTemplate template, Map<String, Object> model, HttpServletResponse response) throws IOException {
		try {
			RenderContext context = getRenderContext();
			engine.render(template, model, context, response.getWriter());
		} catch (PugCompilerException e) {
			e.printStackTrace();
		}
	}

	/**
	 * Returns the RenderContext to use for rendering. If no custom context is set,
	 * returns a default context.
	 */
	private RenderContext getRenderContext() {
		return renderContext != null ? renderContext : RenderContext.defaults();
	}

	/* Configuration Handling */
	public PugEngine getEngine() {
		return engine;
	}

	public void setEngine(PugEngine engine) {
		this.engine = engine;
	}

	public RenderContext getRenderContextConfig() {
		return renderContext;
	}

	public void setRenderContext(RenderContext renderContext) {
		this.renderContext = renderContext;
	}

	public String getEncoding() {
		return encoding;
	}

	public void setEncoding(String encoding) {
		this.encoding = encoding;
	}

	public void setRenderExceptions(boolean renderExceptions) {
		this.renderExceptions = renderExceptions;
	}

	public String getContentType() {
		return contentType;
	}

	public void setContentType(String contentType) {
		this.contentType = contentType;
	}

}
