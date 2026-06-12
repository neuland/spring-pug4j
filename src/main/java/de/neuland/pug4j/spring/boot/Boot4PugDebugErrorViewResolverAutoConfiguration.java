package de.neuland.pug4j.spring.boot;

import de.neuland.pug4j.PugErrorRenderer;
import org.springframework.boot.autoconfigure.AutoConfiguration;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnWebApplication;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Conditional;

/**
 * Auto-configuration for {@link Boot4PugDebugErrorViewResolver} (Spring Boot 4).
 *
 * <p>Guards on the Boot 4 {@code ErrorViewResolver} by name so the annotation never
 * triggers classloading of a type that is absent on a Boot 3 classpath. On any given
 * classpath exactly one of this configuration and
 * {@link PugDebugErrorViewResolverAutoConfiguration} activates — the other backs off
 * because its {@code ErrorViewResolver} interface is missing.
 *
 * <p>Disabled by default — the debug error page exposes template source and paths.
 * Enable for development with {@code spring.pug4j.debug-error-page=true}
 * (the deprecated {@code pug4j.spring.debug-error-page} is still honored).
 */
@AutoConfiguration
@ConditionalOnWebApplication(type = ConditionalOnWebApplication.Type.SERVLET)
@ConditionalOnClass(value = PugErrorRenderer.class,
		name = "org.springframework.boot.webmvc.autoconfigure.error.ErrorViewResolver")
@Conditional(PugDebugErrorPageCondition.class)
public class Boot4PugDebugErrorViewResolverAutoConfiguration {

	@Bean
	@ConditionalOnMissingBean
	public Boot4PugDebugErrorViewResolver boot4PugDebugErrorViewResolver() {
		return new Boot4PugDebugErrorViewResolver();
	}

}
