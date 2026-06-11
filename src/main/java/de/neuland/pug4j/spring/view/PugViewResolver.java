package de.neuland.pug4j.spring.view;

import de.neuland.pug4j.Pug4J.Mode;
import de.neuland.pug4j.PugEngine;
import de.neuland.pug4j.RenderContext;
import org.springframework.util.Assert;
import org.springframework.web.servlet.view.AbstractTemplateViewResolver;
import org.springframework.web.servlet.view.AbstractUrlBasedView;

public class PugViewResolver extends AbstractTemplateViewResolver {

	private PugEngine engine;
	private RenderContext renderContext;
	private Mode defaultMode = Mode.HTML;
	private boolean renderExceptions = false;
	private String contentType = "text/html;charset=UTF-8";

	public PugViewResolver() {
		setViewClass(requiredViewClass());
	}

	@Override
	@SuppressWarnings("rawtypes")
	protected Class requiredViewClass() {
		return PugView.class;
	}

	@Override
	protected AbstractUrlBasedView buildView(String viewName) throws Exception {
		Assert.state(this.engine != null,
				"Property 'engine' is required: configure a PugEngine via PugViewResolver.setEngine(..)");
		PugView view = (PugView) super.buildView(viewName);
		view.setEngine(this.engine);
		view.setRenderContext(obtainRenderContext());
		view.setContentType(contentType);
		view.setRenderExceptions(renderExceptions);
		return view;
	}

	/**
	 * Returns the RenderContext for the views: an explicitly configured context wins,
	 * otherwise one is built from {@link #setDefaultMode(Mode)}.
	 */
	private RenderContext obtainRenderContext() {
		if (renderContext != null) {
			return renderContext;
		}
		return RenderContext.builder().defaultMode(defaultMode).build();
	}

	public PugEngine getEngine() {
		return engine;
	}

	public void setEngine(PugEngine engine) {
		this.engine = engine;
	}

	public RenderContext getRenderContext() {
		return renderContext;
	}

	public void setRenderContext(RenderContext renderContext) {
		this.renderContext = renderContext;
	}

	public Mode getDefaultMode() {
		return defaultMode;
	}

	/**
	 * Sets the default output mode used for templates without a doctype. Defaults to
	 * {@link Mode#HTML} for backwards compatibility with earlier spring-pug4j output
	 * (pug4j 3.0.0's own default is {@link Mode#XHTML}, matching pug.js). Ignored if
	 * a full {@link RenderContext} is set via {@link #setRenderContext(RenderContext)}.
	 */
	public void setDefaultMode(Mode defaultMode) {
		this.defaultMode = defaultMode;
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
