package de.neuland.pug4j.spring.view;

import java.io.IOException;
import java.io.StringWriter;
import java.util.Locale;
import java.util.Map;

import de.neuland.pug4j.Pug4J.Mode;
import de.neuland.pug4j.PugEngine;
import de.neuland.pug4j.PugErrorRenderer;
import de.neuland.pug4j.RenderContext;
import de.neuland.pug4j.exceptions.PugException;
import de.neuland.pug4j.template.PugTemplate;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.web.servlet.view.AbstractTemplateView;


public class PugView extends AbstractTemplateView {

	/**
	 * Default render context with Mode.HTML to keep backwards compatibility with
	 * earlier spring-pug4j output for templates without a doctype.
	 * (RenderContext.defaults() uses Mode.XHTML since pug4j 3.0.0.)
	 */
	private static final RenderContext COMPATIBILITY_DEFAULT_CONTEXT =
			RenderContext.builder().defaultMode(Mode.HTML).build();

	private String encoding;
	private PugEngine engine;
	private RenderContext renderContext;
	private boolean renderExceptions = false;
	private String contentType;

	@Override
	protected void renderMergedTemplateModel(Map<String, Object> model, HttpServletRequest request, HttpServletResponse response) throws Exception {
		doRender(model, response);
	}

	@Override
	protected void applyContentType(HttpServletResponse response) {
		// Deferred to writeHtml(): a propagated render exception must not leave a preset
		// Content-Type behind — it breaks content negotiation in the error dispatch
		// (e.g. Spring Boot's JSON error response fails with HttpMessageNotWritableException).
	}

	private void doRender(Map<String, Object> model, HttpServletResponse response) throws Exception {
		logger.trace("Rendering Pug template [" + getUrl() + "] in PugView '" + getBeanName() + "'");

		// Render into a buffer first so a failing template never sends a partial page.
		StringWriter buffer = new StringWriter();
		try {
			engine.render(getTemplate(), model, getRenderContext(), buffer);
		} catch (PugException e) {
			if (renderExceptions) {
				logger.error("failed to render template [" + getUrl() + "]", e);
				writeHtml(response, PugErrorRenderer.renderHtml(e, buffer.toString()));
				return;
			}
			throw e;
		} catch (IOException e) {
			if (renderExceptions) {
				logger.error("could not find template [" + getUrl() + "]", e);
				writeHtml(response, "<pre>could not find template: " + getUrl() + "</pre>");
				return;
			}
			throw e;
		}
		writeHtml(response, buffer.toString());
	}

	private void writeHtml(HttpServletResponse response, String html) throws IOException {
		if (contentType != null) {
			response.setContentType(contentType);
		}
		response.getWriter().write(html);
	}

	protected PugTemplate getTemplate() throws IOException, PugException {
		return engine.getTemplate(getUrl());
	}

	@Override
	public boolean checkResource(Locale locale) throws Exception {
		return engine.templateExists(getUrl());
	}

	/**
	 * Returns the RenderContext to use for rendering. If no custom context is set,
	 * returns a default context with Mode.HTML for backwards compatibility.
	 */
	private RenderContext getRenderContext() {
		return renderContext != null ? renderContext : COMPATIBILITY_DEFAULT_CONTEXT;
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
