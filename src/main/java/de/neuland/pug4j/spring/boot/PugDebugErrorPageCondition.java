package de.neuland.pug4j.spring.boot;

import org.springframework.boot.autoconfigure.condition.AnyNestedCondition;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;

/**
 * Matches when the debug error page is enabled — via {@code spring.pug4j.debug-error-page}
 * or the deprecated {@code pug4j.spring.debug-error-page} (pre-3.6.0 location).
 */
class PugDebugErrorPageCondition extends AnyNestedCondition {

	PugDebugErrorPageCondition() {
		super(ConfigurationPhase.PARSE_CONFIGURATION);
	}

	@ConditionalOnProperty(prefix = "spring.pug4j", name = "debug-error-page", havingValue = "true")
	static class Property {
	}

	@ConditionalOnProperty(prefix = "pug4j.spring", name = "debug-error-page", havingValue = "true")
	static class DeprecatedProperty {
	}

}
