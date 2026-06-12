package de.neuland.pug4j.spring.boot;

import org.junit.Test;
import org.springframework.boot.autoconfigure.AutoConfigurations;
import org.springframework.boot.test.context.FilteredClassLoader;
import org.springframework.boot.test.context.runner.WebApplicationContextRunner;
import org.springframework.boot.webmvc.autoconfigure.error.ErrorViewResolver;

import static org.junit.Assert.*;

public class Boot4PugDebugErrorViewResolverAutoConfigurationTest {

    private final WebApplicationContextRunner contextRunner = new WebApplicationContextRunner()
            .withConfiguration(AutoConfigurations.of(Boot4PugDebugErrorViewResolverAutoConfiguration.class));

    @Test
    public void shouldBeDisabledByDefault() {
        contextRunner.run(context ->
                assertEquals(0, context.getBeansOfType(Boot4PugDebugErrorViewResolver.class).size()));
    }

    @Test
    public void shouldRegisterResolverWhenPropertyEnabled() {
        contextRunner.withPropertyValues("spring.pug4j.debug-error-page=true").run(context ->
                assertNotNull(context.getBean(Boot4PugDebugErrorViewResolver.class)));
    }

    @Test
    public void shouldStillHonorDeprecatedPropertyPrefix() {
        contextRunner.withPropertyValues("pug4j.spring.debug-error-page=true").run(context ->
                assertNotNull(context.getBean(Boot4PugDebugErrorViewResolver.class)));
    }

    @Test
    public void shouldStayDisabledWhenPropertyFalse() {
        contextRunner.withPropertyValues("spring.pug4j.debug-error-page=false").run(context ->
                assertEquals(0, context.getBeansOfType(Boot4PugDebugErrorViewResolver.class).size()));
    }

    @Test
    public void shouldBackOffWhenBoot4InterfaceMissing() {
        // Simulates a Spring Boot 3 classpath: the relocated interface is absent.
        contextRunner.withClassLoader(new FilteredClassLoader(ErrorViewResolver.class))
                .withPropertyValues("spring.pug4j.debug-error-page=true")
                .run(context ->
                        assertEquals(0, context.getBeansOfType(Boot4PugDebugErrorViewResolver.class).size()));
    }
}
