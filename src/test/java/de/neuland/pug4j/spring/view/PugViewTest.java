package de.neuland.pug4j.spring.view;

import de.neuland.pug4j.Pug4J.Mode;
import de.neuland.pug4j.PugEngine;
import de.neuland.pug4j.RenderContext;
import de.neuland.pug4j.exceptions.PugException;
import de.neuland.pug4j.spring.template.SpringTemplateLoader;
import org.junit.Before;
import org.junit.Test;
import org.springframework.core.io.DefaultResourceLoader;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;
import org.springframework.mock.web.MockServletContext;
import org.springframework.web.context.support.StaticWebApplicationContext;
import org.springframework.web.servlet.DispatcherServlet;

import java.util.HashMap;
import java.util.Map;

import static org.junit.Assert.*;

public class PugViewTest {

    private PugEngine engine;
    private MockHttpServletRequest request;
    private MockHttpServletResponse response;
    private Map<String, Object> model;

    @Before
    public void setUp() {
        SpringTemplateLoader templateLoader = new SpringTemplateLoader();
        templateLoader.setResourceLoader(new DefaultResourceLoader());
        templateLoader.setTemplateLoaderPath("classpath:/templates");
        engine = PugEngine.builder()
                .templateLoader(templateLoader)
                .caching(false)
                .build();
        request = new MockHttpServletRequest();
        response = new MockHttpServletResponse();
        model = new HashMap<>();
    }

    private PugView view(String url) {
        PugView view = new PugView();
        view.setEngine(engine);
        view.setUrl(url);
        return view;
    }

    /** Wires the minimal web context required by the full AbstractView.render() path. */
    private PugView fullRenderView(String url) {
        PugView view = view(url);
        StaticWebApplicationContext context = new StaticWebApplicationContext();
        context.setServletContext(new MockServletContext());
        view.setApplicationContext(context);
        request.setAttribute(DispatcherServlet.WEB_APPLICATION_CONTEXT_ATTRIBUTE, context);
        return view;
    }

    @Test
    public void shouldRenderTerseHtmlByDefaultForBackwardsCompatibility() throws Exception {
        PugView view = view("mode");

        view.renderMergedTemplateModel(model, request, response);

        assertEquals("<input checked>", response.getContentAsString().trim());
    }

    @Test
    public void shouldRespectXhtmlModeFromRenderContext() throws Exception {
        PugView view = view("mode");
        view.setRenderContext(RenderContext.builder().defaultMode(Mode.XHTML).build());

        view.renderMergedTemplateModel(model, request, response);

        assertEquals("<input checked=\"checked\"/>", response.getContentAsString().trim());
    }

    @Test
    public void shouldRenderErrorPageWhenRenderExceptionsEnabled() throws Exception {
        PugView view = view("broken");
        view.setRenderExceptions(true);

        view.renderMergedTemplateModel(model, request, response);

        String content = response.getContentAsString();
        assertTrue("error page should mention the failing mixin", content.contains("unknownMixin"));
        assertTrue("error page should be styled html", content.contains("<html"));
    }

    @Test
    public void shouldPropagateExceptionAndWriteNothingWhenRenderExceptionsDisabled() throws Exception {
        PugView view = view("broken");
        view.setRenderExceptions(false);

        try {
            view.renderMergedTemplateModel(model, request, response);
            fail("expected PugException");
        } catch (PugException expected) {
            // no partial output may reach the response
            assertEquals("", response.getContentAsString());
        }
    }

    @Test
    public void shouldSetConfiguredContentType() throws Exception {
        PugView view = view("mode");
        view.setContentType("text/html;charset=UTF-8");

        view.renderMergedTemplateModel(model, request, response);

        assertEquals("text/html;charset=UTF-8", response.getContentType());
    }

    @Test
    public void shouldNotPresetContentTypeWhenExceptionPropagates() throws Exception {
        PugView view = view("broken");
        view.setContentType("text/html;charset=UTF-8");
        view.setRenderExceptions(false);

        try {
            view.renderMergedTemplateModel(model, request, response);
            fail("expected PugException");
        } catch (PugException expected) {
            // a preset Content-Type would break content negotiation in the error dispatch
            assertNull(response.getContentType());
        }
    }

    @Test
    public void shouldNotPresetContentTypeViaFullRenderPathWhenExceptionPropagates() throws Exception {
        // The full render() path includes AbstractTemplateView.applyContentType(),
        // which PugView defers until the template rendered successfully.
        PugView view = fullRenderView("broken");
        view.setContentType("text/html;charset=UTF-8");
        view.setRenderExceptions(false);

        try {
            view.render(model, request, response);
            fail("expected PugException");
        } catch (PugException expected) {
            assertNull(response.getContentType());
        }
    }

    @Test
    public void shouldSetContentTypeViaFullRenderPathOnSuccess() throws Exception {
        PugView view = fullRenderView("mode");
        view.setContentType("text/html;charset=UTF-8");

        view.render(model, request, response);

        assertEquals("text/html;charset=UTF-8", response.getContentType());
        assertEquals("<input checked>", response.getContentAsString().trim());
    }
}
