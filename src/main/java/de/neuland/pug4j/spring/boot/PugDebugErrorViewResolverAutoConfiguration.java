package de.neuland.pug4j.spring.boot;

import de.neuland.pug4j.PugErrorRenderer;
import org.springframework.boot.autoconfigure.AutoConfiguration;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnWebApplication;
import org.springframework.boot.autoconfigure.web.servlet.error.ErrorViewResolver;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Conditional;

/**
 * Auto-configuration for {@link PugDebugErrorViewResolver}.
 *
 * <p>Disabled by default — the debug error page exposes template source and paths.
 * Enable for development with {@code spring.pug4j.debug-error-page=true}
 * (the deprecated {@code pug4j.spring.debug-error-page} is still honored).
 */
@AutoConfiguration
@ConditionalOnWebApplication(type = ConditionalOnWebApplication.Type.SERVLET)
@ConditionalOnClass({ErrorViewResolver.class, PugErrorRenderer.class})
@Conditional(PugDebugErrorPageCondition.class)
public class PugDebugErrorViewResolverAutoConfiguration {

	@Bean
	@ConditionalOnMissingBean
	public PugDebugErrorViewResolver pugDebugErrorViewResolver() {
		return new PugDebugErrorViewResolver();
	}

}
