package de.neuland.pug4j.spring.boot;

import de.neuland.pug4j.PugErrorRenderer;
import org.springframework.boot.autoconfigure.AutoConfiguration;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.autoconfigure.condition.ConditionalOnWebApplication;
import org.springframework.boot.autoconfigure.web.servlet.error.ErrorViewResolver;
import org.springframework.context.annotation.Bean;

/**
 * Auto-configuration for {@link PugDebugErrorViewResolver}.
 *
 * <p>Disabled by default — the debug error page exposes template source and paths.
 * Enable for development with {@code pug4j.spring.debug-error-page=true}.
 */
@AutoConfiguration
@ConditionalOnWebApplication(type = ConditionalOnWebApplication.Type.SERVLET)
@ConditionalOnClass({ErrorViewResolver.class, PugErrorRenderer.class})
@ConditionalOnProperty(prefix = "pug4j.spring", name = "debug-error-page", havingValue = "true")
public class PugDebugErrorViewResolverAutoConfiguration {

	@Bean
	@ConditionalOnMissingBean
	public PugDebugErrorViewResolver pugDebugErrorViewResolver() {
		return new PugDebugErrorViewResolver();
	}

}
