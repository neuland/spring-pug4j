package de.neuland.pug4j.spring.view;

import de.neuland.pug4j.Pug4J.Mode;
import de.neuland.pug4j.PugEngine;
import de.neuland.pug4j.RenderContext;
import de.neuland.pug4j.spring.template.SpringTemplateLoader;
import org.junit.Before;
import org.junit.Test;
import org.springframework.core.io.DefaultResourceLoader;

import static org.junit.Assert.*;

public class PugViewResolverTest {

    private PugViewResolver resolver;
    private PugEngine engine;

    @Before
    public void setUp() {
        SpringTemplateLoader templateLoader = new SpringTemplateLoader();
        templateLoader.setResourceLoader(new DefaultResourceLoader());
        templateLoader.setTemplateLoaderPath("classpath:/templates");
        engine = PugEngine.builder()
                .templateLoader(templateLoader)
                .caching(false)
                .build();
        resolver = new PugViewResolver();
        resolver.setEngine(engine);
    }

    @Test
    public void shouldDefaultToHtmlModeForBackwardsCompatibility() throws Exception {
        PugView view = (PugView) resolver.buildView("mode");

        RenderContext context = view.getRenderContextConfig();
        assertNotNull(context);
        assertEquals(Mode.HTML, context.getDefaultMode());
    }

    @Test
    public void shouldUseConfiguredDefaultMode() throws Exception {
        resolver.setDefaultMode(Mode.XHTML);

        PugView view = (PugView) resolver.buildView("mode");

        assertEquals(Mode.XHTML, view.getRenderContextConfig().getDefaultMode());
    }

    @Test
    public void shouldPreferExplicitRenderContextOverDefaultMode() throws Exception {
        RenderContext explicit = RenderContext.builder().defaultMode(Mode.XHTML).prettyPrint(true).build();
        resolver.setRenderContext(explicit);
        resolver.setDefaultMode(Mode.HTML);

        PugView view = (PugView) resolver.buildView("mode");

        assertSame(explicit, view.getRenderContextConfig());
    }

    @Test
    public void shouldConfigureEngineAndContentType() throws Exception {
        PugView view = (PugView) resolver.buildView("mode");

        assertSame(engine, view.getEngine());
        assertEquals("text/html;charset=UTF-8", view.getContentType());
    }

    @Test
    public void shouldFailFastWhenEngineIsNotConfigured() {
        PugViewResolver unconfigured = new PugViewResolver();

        IllegalStateException exception =
                assertThrows(IllegalStateException.class, () -> unconfigured.buildView("mode"));
        assertTrue(exception.getMessage().contains("engine"));
    }
}
