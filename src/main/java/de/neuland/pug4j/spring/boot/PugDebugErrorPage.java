package de.neuland.pug4j.spring.boot;

import de.neuland.pug4j.PugErrorRenderer;
import de.neuland.pug4j.exceptions.PugException;
import jakarta.servlet.RequestDispatcher;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.web.servlet.ModelAndView;
import org.springframework.web.servlet.View;

/**
 * Shared resolution logic for the pug4j debug error page, used by both
 * {@link PugDebugErrorViewResolver} (Spring Boot 3) and
 * {@link Boot4PugDebugErrorViewResolver} (Spring Boot 4).
 *
 * <p>Spring Boot 4 moved the {@code ErrorViewResolver} interface from
 * {@code org.springframework.boot.autoconfigure.web.servlet.error} to
 * {@code org.springframework.boot.webmvc.autoconfigure.error}; the method signature is
 * identical, so both resolvers delegate here.
 */
final class PugDebugErrorPage {

	private PugDebugErrorPage() {
	}

	/**
	 * Resolves the pug4j debug error page for the failed request, or {@code null} if the
	 * request did not fail with a {@link PugException}.
	 */
	static ModelAndView resolveErrorView(HttpServletRequest request) {
		Throwable error = (Throwable) request.getAttribute(RequestDispatcher.ERROR_EXCEPTION);
		PugException pugException = findPugException(error);
		if (pugException == null) {
			return null;
		}
		String html = PugErrorRenderer.renderHtml(pugException);
		return new ModelAndView(debugPageView(html));
	}

	private static View debugPageView(String html) {
		return (viewModel, request, response) -> {
			response.setContentType("text/html;charset=UTF-8");
			response.getWriter().write(html);
		};
	}

	private static PugException findPugException(Throwable error) {
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

}
