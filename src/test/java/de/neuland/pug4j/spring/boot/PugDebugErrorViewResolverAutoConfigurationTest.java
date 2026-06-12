package de.neuland.pug4j.spring.boot;

import org.junit.Test;
import org.springframework.boot.autoconfigure.AutoConfigurations;
import org.springframework.boot.autoconfigure.web.servlet.error.ErrorViewResolver;
import org.springframework.boot.test.context.FilteredClassLoader;
import org.springframework.boot.test.context.runner.WebApplicationContextRunner;

import static org.junit.Assert.*;

public class PugDebugErrorViewResolverAutoConfigurationTest {

    private final WebApplicationContextRunner contextRunner = new WebApplicationContextRunner()
            .withConfiguration(AutoConfigurations.of(PugDebugErrorViewResolverAutoConfiguration.class));

    @Test
    public void shouldBeDisabledByDefault() {
        contextRunner.run(context ->
                assertEquals(0, context.getBeansOfType(PugDebugErrorViewResolver.class).size()));
    }

    @Test
    public void shouldRegisterResolverWhenPropertyEnabled() {
        contextRunner.withPropertyValues("spring.pug4j.debug-error-page=true").run(context ->
                assertNotNull(context.getBean(PugDebugErrorViewResolver.class)));
    }

    @Test
    public void shouldStillHonorDeprecatedPropertyPrefix() {
        contextRunner.withPropertyValues("pug4j.spring.debug-error-page=true").run(context ->
                assertNotNull(context.getBean(PugDebugErrorViewResolver.class)));
    }

    @Test
    public void shouldStayDisabledWhenPropertyFalse() {
        contextRunner.withPropertyValues("spring.pug4j.debug-error-page=false").run(context ->
                assertEquals(0, context.getBeansOfType(PugDebugErrorViewResolver.class).size()));
    }

    @Test
    public void shouldBackOffWhenBoot3InterfaceMissing() {
        // Simulates a Spring Boot 4 classpath: the old interface location is absent.
        contextRunner.withClassLoader(new FilteredClassLoader(ErrorViewResolver.class))
                .withPropertyValues("spring.pug4j.debug-error-page=true")
                .run(context ->
                        assertEquals(0, context.getBeansOfType(PugDebugErrorViewResolver.class).size()));
    }
}
