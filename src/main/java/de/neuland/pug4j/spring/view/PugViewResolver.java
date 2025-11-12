package de.neuland.pug4j.spring.view;

import de.neuland.pug4j.PugEngine;
import de.neuland.pug4j.RenderContext;
import org.springframework.web.servlet.view.AbstractTemplateViewResolver;
import org.springframework.web.servlet.view.AbstractUrlBasedView;

public class PugViewResolver extends AbstractTemplateViewResolver {

	private PugEngine engine;
	private RenderContext renderContext;
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
		PugView view = (PugView) super.buildView(viewName);
		view.setEngine(this.engine);
		view.setRenderContext(this.renderContext);
		view.setContentType(contentType);
		view.setRenderExceptions(renderExceptions);
		return view;
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
