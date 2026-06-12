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
	private boolean producePartialOutputWhileProcessing = false;
	private String contentType;

	@Override
	protected void renderMergedTemplateModel(Map<String, Object> model, HttpServletRequest request, HttpServletResponse response) throws Exception {
		logger.trace("Rendering Pug template [" + getUrl() + "] in PugView '" + getBeanName() + "'");
		if (producePartialOutputWhileProcessing) {
			renderStreaming(model, response);
		} else {
			renderBuffered(model, response);
		}
	}

	@Override
	protected void applyContentType(HttpServletResponse response) {
		// Deferred until output is written: a propagated render exception must not leave a
		// preset Content-Type behind — it breaks content negotiation in the error dispatch
		// (e.g. Spring Boot's JSON error response fails with HttpMessageNotWritableException).
	}

	private void renderBuffered(Map<String, Object> model, HttpServletResponse response) throws Exception {
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

	/**
	 * Streams output directly into the response for a faster time-to-first-byte on large
	 * pages. Template loading and parsing happen before the first byte is written, so those
	 * errors are still handled cleanly — but a failure while rendering can leave a partial
	 * page (and a committed response) behind. That is the inherent trade-off of streaming.
	 */
	private void renderStreaming(Map<String, Object> model, HttpServletResponse response) throws Exception {
		PugTemplate template;
		try {
			template = getTemplate();
		} catch (PugException e) {
			if (renderExceptions) {
				logger.error("failed to render template [" + getUrl() + "]", e);
				writeHtml(response, PugErrorRenderer.renderHtml(e));
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

		if (contentType != null) {
			response.setContentType(contentType);
		}
		try {
			engine.render(template, model, getRenderContext(), response.getWriter());
		} catch (PugException e) {
			if (renderExceptions) {
				logger.error("failed to render template [" + getUrl() + "]", e);
				response.getWriter().write(PugErrorRenderer.renderHtml(e));
				return;
			}
			throw e;
		}
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

	public boolean isProducePartialOutputWhileProcessing() {
		return producePartialOutputWhileProcessing;
	}

	/**
	 * Streams output directly into the response instead of buffering the fully rendered
	 * page first. Improves time-to-first-byte for pages larger than the servlet
	 * container's response buffer, at the cost that a failing template may deliver a
	 * partial page. Defaults to {@code false} (buffered).
	 */
	public void setProducePartialOutputWhileProcessing(boolean producePartialOutputWhileProcessing) {
		this.producePartialOutputWhileProcessing = producePartialOutputWhileProcessing;
	}

	public String getContentType() {
		return contentType;
	}

	public void setContentType(String contentType) {
		this.contentType = contentType;
	}

}
