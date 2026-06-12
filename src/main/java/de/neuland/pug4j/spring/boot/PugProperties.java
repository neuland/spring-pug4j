package de.neuland.pug4j.spring.boot;

import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;

import de.neuland.pug4j.Pug4J.Mode;
import org.springframework.boot.context.properties.ConfigurationProperties;

/**
 * Configuration properties for Pug4J view resolution.
 *
 * <p>Property names follow the conventions of {@code spring.thymeleaf.*} and
 * {@code spring.freemarker.*} so the configuration feels familiar.
 */
@ConfigurationProperties(prefix = "spring.pug4j")
public class PugProperties {

	/**
	 * Whether to auto-configure Pug4J view resolution.
	 */
	private boolean enabled = true;

	/**
	 * Whether to check that the templates location exists.
	 */
	private boolean checkTemplateLocation = true;

	/**
	 * Prefix that gets prepended to view names when building a URL.
	 */
	private String prefix = "classpath:/templates/";

	/**
	 * Suffix that gets appended to view names when building a URL.
	 */
	private String suffix = ".pug";

	/**
	 * Output mode applied to templates without an explicit doctype.
	 */
	private Mode mode = Mode.HTML;

	/**
	 * Template files encoding (file reading only). Unlike Thymeleaf, the response
	 * charset is not derived from this; it is part of the content-type value.
	 */
	private Charset encoding = StandardCharsets.UTF_8;

	/**
	 * Whether to enable template caching. Disable during development to pick up
	 * template changes without a restart.
	 */
	private boolean cache = true;

	/**
	 * Content-Type value written to the HTTP response.
	 */
	private String contentType = "text/html;charset=UTF-8";

	/**
	 * Whether to pretty-print the rendered output.
	 */
	private boolean prettyPrint = false;

	/**
	 * Whether to stream output directly into the response instead of buffering the
	 * fully rendered page first. Improves time-to-first-byte for pages larger than
	 * the servlet container's response buffer, at the cost that a failing template
	 * may deliver a partial page.
	 */
	private boolean producePartialOutputWhileProcessing = false;

	/**
	 * Whether to render Pug exceptions as styled HTML error pages instead of
	 * propagating them (development only).
	 */
	private boolean renderExceptions = false;

	/**
	 * Whether to render pug4j's debug error page at /error when a request fails
	 * with a PugException (development only - exposes template source and paths).
	 */
	private boolean debugErrorPage = false;

	/**
	 * View names that can be resolved (supports simple wildcards). Unset means all.
	 */
	private String[] viewNames;

	public boolean isEnabled() {
		return enabled;
	}

	public void setEnabled(boolean enabled) {
		this.enabled = enabled;
	}

	public boolean isCheckTemplateLocation() {
		return checkTemplateLocation;
	}

	public void setCheckTemplateLocation(boolean checkTemplateLocation) {
		this.checkTemplateLocation = checkTemplateLocation;
	}

	public String getPrefix() {
		return prefix;
	}

	public void setPrefix(String prefix) {
		this.prefix = prefix;
	}

	public String getSuffix() {
		return suffix;
	}

	public void setSuffix(String suffix) {
		this.suffix = suffix;
	}

	public Mode getMode() {
		return mode;
	}

	public void setMode(Mode mode) {
		this.mode = mode;
	}

	public Charset getEncoding() {
		return encoding;
	}

	public void setEncoding(Charset encoding) {
		this.encoding = encoding;
	}

	public boolean isCache() {
		return cache;
	}

	public void setCache(boolean cache) {
		this.cache = cache;
	}

	public String getContentType() {
		return contentType;
	}

	public void setContentType(String contentType) {
		this.contentType = contentType;
	}

	public boolean isPrettyPrint() {
		return prettyPrint;
	}

	public void setPrettyPrint(boolean prettyPrint) {
		this.prettyPrint = prettyPrint;
	}

	public boolean isProducePartialOutputWhileProcessing() {
		return producePartialOutputWhileProcessing;
	}

	public void setProducePartialOutputWhileProcessing(boolean producePartialOutputWhileProcessing) {
		this.producePartialOutputWhileProcessing = producePartialOutputWhileProcessing;
	}

	public boolean isRenderExceptions() {
		return renderExceptions;
	}

	public void setRenderExceptions(boolean renderExceptions) {
		this.renderExceptions = renderExceptions;
	}

	public boolean isDebugErrorPage() {
		return debugErrorPage;
	}

	public void setDebugErrorPage(boolean debugErrorPage) {
		this.debugErrorPage = debugErrorPage;
	}

	public String[] getViewNames() {
		return viewNames;
	}

	public void setViewNames(String[] viewNames) {
		this.viewNames = viewNames;
	}

}
