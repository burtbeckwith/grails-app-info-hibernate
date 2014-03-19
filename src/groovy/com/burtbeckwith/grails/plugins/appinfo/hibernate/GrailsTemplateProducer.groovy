package com.burtbeckwith.grails.plugins.appinfo.hibernate

import org.apache.commons.io.output.NullWriter
import org.hibernate.tool.hbm2x.ArtifactCollector
import org.hibernate.tool.hbm2x.TemplateHelper
import org.hibernate.tool.hbm2x.TemplateProducer
import org.w3c.tidy.Tidy

/**
 * Subclass that captures output rather than writing to files.
 *
 * @author <a href='mailto:burt@burtbeckwith.com'>Burt Beckwith</a>
 */
class GrailsTemplateProducer extends TemplateProducer {

	private final TemplateHelper th
	private final Tidy tidy = new Tidy()
	private final Map<String, String> results = [:]
	private final boolean prettyPrint

	GrailsTemplateProducer(TemplateHelper th, ArtifactCollector ac, boolean prettyPrint) {
		super(th, ac)
		this.th = th
		this.prettyPrint = prettyPrint
		def properties = [
			indent: 'auto',
			'indent-spaces': '4',
			wrap: '180',
			markup: 'yes',
			clean: 'yes',
			'output-xml': 'yes',
			'input-xml': 'yes',
			'show-warnings': 'yes',
			'trim-empty-elements': 'yes']
		tidy.setConfigurationFromProps(properties as Properties)
		tidy.errout = new PrintWriter(new NullWriter())
	}

	@Override
	void produce(Map additionalContext, String templateName, File outputFile, String identifier) {
		String fileType = outputFile.name
		fileType = fileType.substring(fileType.indexOf('.') + 1)

		additionalContext.each { key, value -> th.putInContext(key, value) }

		StringWriter stringWriter = new StringWriter()
		BufferedWriter writer = new BufferedWriter(stringWriter)
		th.processTemplate templateName, writer, null

		additionalContext.each { key, value -> th.removeFromContext(key, value) }

		writer.flush()
		String result = stringWriter.toString()

		if (prettyPrint) {
			Writer printed = new StringWriter()
			tidy.parse new StringReader(result), printed
			result = printed.toString()
		}

		results.put outputFile.path, result
	}

	@Override
	void produce(Map additionalContext, String templateName, File destination, String identifier, String fileType, String rootContext) {
		produce additionalContext, templateName, destination, identifier
	}

	Map<String, String> getResults() { results }
}
