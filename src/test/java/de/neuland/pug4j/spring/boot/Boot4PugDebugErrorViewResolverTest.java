package de.neuland.pug4j.spring.boot;

import de.neuland.pug4j.exceptions.PugException;
import de.neuland.pug4j.exceptions.PugParserException;
import jakarta.servlet.RequestDispatcher;
import jakarta.servlet.ServletException;
import org.junit.Before;
import org.junit.Test;
import org.springframework.http.HttpStatus;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;
import org.springframework.web.servlet.ModelAndView;

import java.util.Collections;
import java.util.HashMap;

import static org.junit.Assert.*;

public class Boot4PugDebugErrorViewResolverTest {

    private Boot4PugDebugErrorViewResolver resolver;
    private MockHttpServletRequest request;

    @Before
    public void setUp() {
        resolver = new Boot4PugDebugErrorViewResolver();
        request = new MockHttpServletRequest();
    }

    private PugException pugException() {
        return new PugParserException("broken.pug", 2, 1, "boom", Collections.singletonList("+unknownMixin()"));
    }

    @Test
    public void shouldResolveDebugViewForPugException() throws Exception {
        request.setAttribute(RequestDispatcher.ERROR_EXCEPTION, pugException());

        ModelAndView mav = resolver.resolveErrorView(request, HttpStatus.INTERNAL_SERVER_ERROR, new HashMap<>());

        assertNotNull(mav);
        MockHttpServletResponse response = new MockHttpServletResponse();
        mav.getView().render(mav.getModel(), request, response);
        String content = response.getContentAsString();
        assertTrue("debug page should contain the message", content.contains("boom"));
        assertTrue("debug page should reference the template", content.contains("broken.pug"));
    }

    @Test
    public void shouldUnwrapPugExceptionFromCauseChain() {
        request.setAttribute(RequestDispatcher.ERROR_EXCEPTION, new ServletException("wrapper", pugException()));

        ModelAndView mav = resolver.resolveErrorView(request, HttpStatus.INTERNAL_SERVER_ERROR, new HashMap<>());

        assertNotNull(mav);
    }

    @Test
    public void shouldReturnNullForNonPugErrors() {
        request.setAttribute(RequestDispatcher.ERROR_EXCEPTION, new IllegalStateException("unrelated"));

        assertNull(resolver.resolveErrorView(request, HttpStatus.INTERNAL_SERVER_ERROR, new HashMap<>()));
    }

    @Test
    public void shouldReturnNullWithoutErrorAttribute() {
        assertNull(resolver.resolveErrorView(request, HttpStatus.NOT_FOUND, new HashMap<>()));
    }
}
