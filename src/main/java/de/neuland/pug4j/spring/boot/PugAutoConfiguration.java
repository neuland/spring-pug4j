package de.neuland.pug4j.spring.boot;

import de.neuland.pug4j.PugEngine;
import de.neuland.pug4j.RenderContext;
import de.neuland.pug4j.spring.template.SpringTemplateLoader;
import de.neuland.pug4j.spring.view.PugViewResolver;
import de.neuland.pug4j.template.TemplateLoader;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.autoconfigure.AutoConfiguration;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.autoconfigure.condition.ConditionalOnWebApplication;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.ApplicationContext;
import org.springframework.context.annotation.Bean;
import org.springframework.core.Ordered;

/**
 * Auto-configuration for Pug4J view resolution: with this on the classpath, a Spring Boot
 * application renders {@code .pug} templates from {@code classpath:/templates/} without
 * any manual bean configuration.
 *
 * <p>All settings are exposed under {@code spring.pug4j.*} (see {@link PugProperties}),
 * mirroring the conventions of {@code spring.thymeleaf.*}. Each bean backs off when the
 * application defines its own ({@code @ConditionalOnMissingBean}), so existing manual
 * configurations keep working unchanged.
 */
@AutoConfiguration
@ConditionalOnWebApplication(type = ConditionalOnWebApplication.Type.SERVLET)
@ConditionalOnClass(PugEngine.class)
@ConditionalOnProperty(prefix = "spring.pug4j", name = "enabled", havingValue = "true", matchIfMissing = true)
@EnableConfigurationProperties(PugProperties.class)
public class PugAutoConfiguration {

	private static final Logger logger = LoggerFactory.getLogger(PugAutoConfiguration.class);

	@Bean
	@ConditionalOnMissingBean(TemplateLoader.class)
	public SpringTemplateLoader pugTemplateLoader(PugProperties properties, ApplicationContext applicationContext) {
		if (properties.isCheckTemplateLocation() && !applicationContext.getResource(properties.getPrefix()).exists()) {
			logger.warn("Cannot find template location: {} (please add some templates, check your Pug4J "
					+ "configuration, or set spring.pug4j.check-template-location=false)", properties.getPrefix());
		}
		SpringTemplateLoader loader = new SpringTemplateLoader();
		loader.setTemplateLoaderPath(properties.getPrefix());
		loader.setSuffix(properties.getSuffix());
		loader.setEncoding(properties.getEncoding().name());
		return loader;
	}

	@Bean
	@ConditionalOnMissingBean
	public PugEngine pugEngine(TemplateLoader templateLoader, PugProperties properties) {
		return PugEngine.builder()
				.templateLoader(templateLoader)
				.caching(properties.isCache())
				.build();
	}

	@Bean
	@ConditionalOnMissingBean
	public PugViewResolver pugViewResolver(PugEngine pugEngine, PugProperties properties) {
		PugViewResolver resolver = new PugViewResolver();
		resolver.setEngine(pugEngine);
		resolver.setRenderContext(RenderContext.builder()
				.defaultMode(properties.getMode())
				.prettyPrint(properties.isPrettyPrint())
				.build());
		resolver.setContentType(properties.getContentType());
		resolver.setRenderExceptions(properties.isRenderExceptions());
		if (properties.getViewNames() != null) {
			resolver.setViewNames(properties.getViewNames());
		}
		// Same slot as Thymeleaf's view resolver: after the BeanNameViewResolver,
		// before Boot's InternalResourceViewResolver. Missing templates fall through
		// to the next resolver via PugView.checkResource().
		resolver.setOrder(Ordered.LOWEST_PRECEDENCE - 5);
		return resolver;
	}

}
