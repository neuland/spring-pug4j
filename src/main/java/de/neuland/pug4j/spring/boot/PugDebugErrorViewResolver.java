package de.neuland.pug4j.spring.boot;

import java.util.Map;

import de.neuland.pug4j.PugErrorRenderer;
import de.neuland.pug4j.exceptions.PugException;
import jakarta.servlet.RequestDispatcher;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.boot.autoconfigure.web.servlet.error.ErrorViewResolver;
import org.springframework.core.Ordered;
import org.springframework.http.HttpStatus;
import org.springframework.web.servlet.ModelAndView;
import org.springframework.web.servlet.View;

/**
 * Spring Boot {@link ErrorViewResolver} that renders pug4j's styled debug error page
 * (via {@link PugErrorRenderer}) when the request failed with a {@link PugException}.
 *
 * <p>This resolver hooks into Spring Boot's regular error handling at {@code /error}:
 * the response status, logging, and the rest of the error pipeline stay untouched —
 * only the rendered error view is replaced. For non-Pug errors it returns {@code null}
 * so other resolvers (or the whitelabel page) apply.
 *
 * <p>The debug page exposes template source and file paths, so it is intended for
 * development only. It is auto-configured by {@link PugDebugErrorViewResolverAutoConfiguration}
 * and disabled unless {@code pug4j.spring.debug-error-page=true} is set.
 */
public class PugDebugErrorViewResolver implements ErrorViewResolver, Ordered {

	/** Runs before Boot's DefaultErrorViewResolver (LOWEST_PRECEDENCE) so the debug page wins. */
	private int order = Ordered.LOWEST_PRECEDENCE - 10;

	@Override
	public ModelAndView resolveErrorView(HttpServletRequest request, HttpStatus status, Map<String, Object> model) {
		Throwable error = (Throwable) request.getAttribute(RequestDispatcher.ERROR_EXCEPTION);
		PugException pugException = findPugException(error);
		if (pugException == null) {
			return null;
		}
		String html = PugErrorRenderer.renderHtml(pugException);
		return new ModelAndView(debugPageView(html));
	}

	private View debugPageView(String html) {
		return (viewModel, request, response) -> {
			response.setContentType("text/html;charset=UTF-8");
			response.getWriter().write(html);
		};
	}

	private PugException findPugException(Throwable error) {
		// The PugException may be wrapped (e.g. in a ServletException), walk the cause chain.
		while (error != null) {
			if (error instanceof PugException pugException) {
				return pugException;
			}
			if (error.getCause() == error) {
				return null;
			}
			error = error.getCause();
		}
		return null;
	}

	@Override
	public int getOrder() {
		return order;
	}

	public void setOrder(int order) {
		this.order = order;
	}

}
