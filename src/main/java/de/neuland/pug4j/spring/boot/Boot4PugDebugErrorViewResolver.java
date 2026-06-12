package de.neuland.pug4j.spring.boot;

import java.util.Map;

import de.neuland.pug4j.PugErrorRenderer;
import de.neuland.pug4j.exceptions.PugException;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.boot.webmvc.autoconfigure.error.ErrorViewResolver;
import org.springframework.core.Ordered;
import org.springframework.http.HttpStatus;
import org.springframework.web.servlet.ModelAndView;

/**
 * Spring Boot 4 {@link ErrorViewResolver} that renders pug4j's styled debug error page
 * (via {@link PugErrorRenderer}) when the request failed with a {@link PugException}.
 *
 * <p>Functionally identical to {@link PugDebugErrorViewResolver}, but implements the
 * {@code ErrorViewResolver} interface relocated in Spring Boot 4 to
 * {@code org.springframework.boot.webmvc.autoconfigure.error} (artifact
 * {@code spring-boot-webmvc}) — Boot 4's {@code BasicErrorController} only consults
 * beans implementing the new interface.
 *
 * <p>The debug page exposes template source and file paths, so it is intended for
 * development only. It is auto-configured by
 * {@link Boot4PugDebugErrorViewResolverAutoConfiguration} and disabled unless
 * {@code pug4j.spring.debug-error-page=true} is set.
 */
public class Boot4PugDebugErrorViewResolver implements ErrorViewResolver, Ordered {

	/** Runs before Boot's DefaultErrorViewResolver (LOWEST_PRECEDENCE) so the debug page wins. */
	private int order = Ordered.LOWEST_PRECEDENCE - 10;

	@Override
	public ModelAndView resolveErrorView(HttpServletRequest request, HttpStatus status, Map<String, Object> model) {
		return PugDebugErrorPage.resolveErrorView(request);
	}

	@Override
	public int getOrder() {
		return order;
	}

	public void setOrder(int order) {
		this.order = order;
	}

}
