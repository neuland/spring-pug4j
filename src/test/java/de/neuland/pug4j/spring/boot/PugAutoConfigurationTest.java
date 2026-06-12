package de.neuland.pug4j.spring.boot;

import de.neuland.pug4j.PugEngine;
import de.neuland.pug4j.spring.template.SpringTemplateLoader;
import de.neuland.pug4j.spring.view.PugViewResolver;
import org.junit.Test;
import org.springframework.boot.autoconfigure.AutoConfigurations;
import org.springframework.boot.test.context.runner.WebApplicationContextRunner;
import org.springframework.core.Ordered;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;
import org.springframework.web.servlet.DispatcherServlet;
import org.springframework.web.servlet.View;

import java.util.HashMap;
import java.util.Locale;

import static org.junit.Assert.*;

public class PugAutoConfigurationTest {

    private final WebApplicationContextRunner contextRunner = new WebApplicationContextRunner()
            .withConfiguration(AutoConfigurations.of(PugAutoConfiguration.class));

    @Test
    public void shouldConfigureTemplateLoaderEngineAndViewResolverByDefault() {
        contextRunner.run(context -> {
            assertNotNull(context.getBean(SpringTemplateLoader.class));
            assertNotNull(context.getBean(PugEngine.class));
            PugViewResolver resolver = context.getBean(PugViewResolver.class);
            assertEquals("text/html;charset=UTF-8", resolver.getContentType());
            assertNotNull("render context should be built from properties", resolver.getRenderContext());
            assertEquals(Ordered.LOWEST_PRECEDENCE - 5, resolver.getOrder());
        });
    }

    @Test
    public void shouldBackOffWhenDisabled() {
        contextRunner.withPropertyValues("spring.pug4j.enabled=false").run(context -> {
            assertEquals(0, context.getBeansOfType(SpringTemplateLoader.class).size());
            assertEquals(0, context.getBeansOfType(PugEngine.class).size());
            assertEquals(0, context.getBeansOfType(PugViewResolver.class).size());
        });
    }

    @Test
    public void shouldApplyCustomProperties() {
        contextRunner.withPropertyValues(
                "spring.pug4j.suffix=.jade",
                "spring.pug4j.encoding=ISO-8859-1",
                "spring.pug4j.content-type=application/xhtml+xml",
                "spring.pug4j.cache=false",
                "spring.pug4j.mode=XHTML",
                "spring.pug4j.produce-partial-output-while-processing=true"
        ).run(context -> {
            SpringTemplateLoader loader = context.getBean(SpringTemplateLoader.class);
            assertEquals("jade", loader.getExtension());
            assertEquals("ISO-8859-1", loader.getEncoding());
            PugViewResolver resolver = context.getBean(PugViewResolver.class);
            assertEquals("application/xhtml+xml", resolver.getContentType());
            assertTrue(resolver.isProducePartialOutputWhileProcessing());
        });
    }

    @Test
    public void shouldBackOffWhenUserDefinesOwnEngine() {
        contextRunner
                .withBean("myEngine", PugEngine.class, () -> PugEngine.builder()
                        .templateLoader(new SpringTemplateLoader())
                        .build())
                .run(context -> {
                    assertEquals(1, context.getBeansOfType(PugEngine.class).size());
                    assertSame("resolver should use the user-defined engine",
                            context.getBean("myEngine"), context.getBean(PugViewResolver.class).getEngine());
                });
    }

    @Test
    public void shouldBackOffWhenUserDefinesOwnViewResolver() {
        contextRunner
                .withBean("viewResolver", PugViewResolver.class, PugViewResolver::new)
                .run(context -> assertEquals(1, context.getBeansOfType(PugViewResolver.class).size()));
    }

    @Test
    public void shouldRenderTemplateFromDefaultLocation() {
        contextRunner.run(context -> {
            PugViewResolver resolver = context.getBean(PugViewResolver.class);
            View view = resolver.resolveViewName("mode", Locale.GERMAN);
            assertNotNull(view);

            MockHttpServletRequest request = new MockHttpServletRequest();
            request.setAttribute(DispatcherServlet.WEB_APPLICATION_CONTEXT_ATTRIBUTE, context);
            MockHttpServletResponse response = new MockHttpServletResponse();
            view.render(new HashMap<>(), request, response);

            assertEquals("<input checked>", response.getContentAsString().trim());
            assertEquals("text/html;charset=UTF-8", response.getContentType());
        });
    }

    @Test
    public void shouldReturnNullForMissingTemplateSoOtherResolversApply() {
        contextRunner.run(context ->
                assertNull(context.getBean(PugViewResolver.class).resolveViewName("does-not-exist", Locale.GERMAN)));
    }

    @Test
    public void shouldRestrictToConfiguredViewNames() {
        contextRunner.withPropertyValues("spring.pug4j.view-names=pug-*").run(context ->
                assertNull(context.getBean(PugViewResolver.class).resolveViewName("mode", Locale.GERMAN)));
    }
}
