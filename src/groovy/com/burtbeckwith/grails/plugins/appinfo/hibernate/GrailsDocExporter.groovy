package com.burtbeckwith.grails.plugins.appinfo.hibernate

import org.hibernate.HibernateException
import org.hibernate.cfg.Configuration
import org.hibernate.mapping.Property
import org.hibernate.mapping.Table
import org.hibernate.tool.hbm2x.AbstractExporter
import org.hibernate.tool.hbm2x.ExporterException
import org.hibernate.tool.hbm2x.GenericExporter
import org.hibernate.tool.hbm2x.TemplateHelper
import org.hibernate.tool.hbm2x.TemplateProducer
import org.hibernate.tool.hbm2x.doc.DocHelper
import org.hibernate.tool.hbm2x.pojo.POJOClass
import org.springframework.util.FileCopyUtils

/**
 * @author <a href='mailto:burt@burtbeckwith.com'>Burt Beckwith</a>
 */
class GrailsDocExporter extends AbstractExporter {

	private final String dotExePath

	private DocHelper docHelper
	private FakeDocFileManager docFileManager
	private GrailsTemplateProducer producer

	GrailsDocExporter(Configuration cfg, String dotExePath) {
		super(cfg, new File(''))
		this.dotExePath = dotExePath
	}

	@Override
	void start() {
		setTemplateHelper(new TemplateHelper())
		setupTemplates()
		setupContext()
		producer = new GrailsTemplateProducer(templateHelper, artifactCollector, false)

		doStart()
		cleanUpContext()
		setTemplateHelper(null)
		getArtifactCollector().formatFiles()
	}

	@Override
	void doStart() throws ExporterException {

		boolean graphsGenerated = generateDot()
		generateTablesIndex()
		generateTablesSummary(graphsGenerated)
		generateTablesDetails()
		generateTablesAllSchemasList()
		generateTablesAllTablesList()
		generateTablesSchemaTableList()
		generateTablesSchemaDetailedInfo()

		generateEntitiesIndex()
		generatePackageSummary(graphsGenerated)
		generateEntitiesDetails()
		generateEntitiesAllPackagesList()
		generateEntitiesAllEntitiesList()
		generateEntitiesPackageEntityList()
		generateEntitiesPackageDetailedInfo()
	}

	Map<String, String> export() {
		start()
		results
	}

	Map<String, String> getResults() { producer.results }

	boolean generateDot() {
		if (!dotExePath) {
			log.info 'Skipping entitygraph creation since dot.executable is empty or not-specified.'
			return false
		}

		try {
			def exporter = new DotExporter(getConfiguration())
			exporter.templateName = locateTemplate('entitygraph.dot', 'dot/')
			exporter.filePattern = 'entities/entitygraph.dot'
			exporter.artifactCollector = artifactCollector
			exporter.properties = getProperties()
			exporter.templatePath = templatePaths

			String entityDot = exporter.export()
			createImage entityDot, 'entities/entitygraph', 'png'
			createImage entityDot, 'entities/entitygraph', 'svg'
			createImage entityDot, 'entities/entitygraph', 'cmap'

			exporter.templateName = locateTemplate('tablegraph.dot', 'dot/')
			exporter.filePattern = 'tables/tablegraph.dot'
			exporter.properties = getProperties()

			String tableDot = exporter.export()
			createImage tableDot, 'tables/tablegraph', 'png'
			createImage tableDot, 'tables/tablegraph', 'svg'
			createImage tableDot, 'tables/tablegraph', 'cmap'

			return true
		}
		catch (IOException e) {
			throw new HibernateException('Problem while generating DOT graph for Configuration', e)
		}
	}

	static final String OS_NAME = System.getProperty('os.name')
	static final boolean IS_WINDOWS = OS_NAME.contains('Windows')

	private void createImage(String dot, String outFileName, String extension) throws IOException {
		String exeCmd = dotExePath
		if (IS_WINDOWS) {
			// Windows needs " " around file names actually we do not
			// need it always, only when spaces are present
			// but it does not hurt to use them always
			exeCmd = '"' + exeCmd + '"'
		}
		exeCmd += ' -T' + extension

		Process p = Runtime.runtime.exec(exeCmd)
		def stdin = p.outputStream
		stdin.write dot.bytes
		stdin.flush()
		stdin.close()
		def result = FileCopyUtils.copyToByteArray(p.inputStream)
		p.waitFor()

		//		println "[STDERR] $p.err.text"
		if ('cmap' == extension) {
			result = new String(result)
		}

		producer.results["/${outFileName}.$extension"] = result
	}

	@Override
	protected void setupContext() {
		getProperties().put('jdk5', useJdk5().toString())
		super.setupContext()
		docHelper = new DocHelper(configuration, cfg2JavaTool)
		docFileManager = new FakeDocFileManager(docHelper, outputDirectory)

		templateHelper.putInContext('dochelper', docHelper)
		templateHelper.putInContext('docFileManager', docFileManager)
		templateHelper.putInContext('propertyHelper', this) // template will call getPropertyAccessorName()
	}

	// workaround for template bug
	String getPropertyAccessorName(Property prop) {
		String accessorName = prop.propertyAccessorName
		if (!accessorName) {
			// TODO fix
			accessorName = prop.name
			return 'get' + accessorName[0].toUpperCase() + accessorName[1..-1]
		}
		accessorName
	}

	/**
	 * Generate the index file of the table documentation.
	 */
	void generateTablesIndex() {
		def docFile = docFileManager.tableIndexDocFile
		processTemplate([docFile: docFile], 'tables/index', docFile.file)
	}

	/**
	 * Generate the index file of the class documentation
	 */
	void generateEntitiesIndex() {
		def docFile = docFileManager.classIndexDocFile
		processTemplate([docFile: docFile], 'entity/index', docFile.file)
	}

	/**
	 * Generate a file with an summary of all the tables.
	 *
	 * @param graphsGenerated
	 */
	void generateTablesSummary(boolean graphsGenerated) {
		def docFile = docFileManager.tableSummaryDocFile

		def parameters = [docFile: docFile, graphsGenerated: graphsGenerated, tablegrapharea: '']
		if (graphsGenerated) {
			parameters.tablegrapharea = producer.results['/tables/tablegraph.cmap']
		}

		processTemplate parameters, 'tables/summary', docFile.file
	}

	void generatePackageSummary(boolean graphsGenerated) {
		def docFile = docFileManager.classSummaryFile
		List list = docHelper.packages
		// remove 'All Classes'
		list.remove(0)
		def parameters = [docFile: docFile, packageList: list,
				graphsGenerated: graphsGenerated, entitygrapharea: '']
		if (graphsGenerated) {
			parameters.entitygrapharea = producer.results['/entities/entitygraph.cmap']
		}

		processTemplate parameters, 'entity/summary', docFile.file
	}

	void generateTablesDetails() {
		for (Table table in configuration.tableMappings) {
			def docFile = docFileManager.getTableDocFile(table)
			if (docFile) {
				def parameters = [docFile: docFile, table: table]
				processTemplate parameters, 'tables/table', docFile.file
			}
		}
	}

	void generateEntitiesDetails() {
		for (POJOClass pojoClass in docHelper.classes) {
			pojoClass.getPropertiesForMinimalConstructor()
			def docFile = docFileManager.getEntityDocFile(pojoClass)
			def parameters = [docFile: docFile, 'class': pojoClass, propertyHelper: this]
			processTemplate parameters, 'entity/entity', docFile.file
		}
	}

	/**
	 * Generates the html file containig list of packages (allpackages.html)
	 */
	void generateEntitiesAllPackagesList() {
		def docFile = docFileManager.allPackagesDocFile
		List<?> list = docHelper.packages
		// remove 'All Classes'
		list.remove(0)
		def parameters = [docFile: docFile, title: 'Package List', packageList: list]
		processTemplate parameters, 'entity/package-list', docFile.file
	}

	/**
	 * Generates the html file containing list of classes (allclases.html)
	 */
	void generateEntitiesAllEntitiesList() {
		def docFile = docFileManager.allEntitiesDocFile
		def parameters = [docFile: docFile, title: 'All Entities', classList: docHelper.classes]
		processTemplate parameters, 'entity/allEntity-list', docFile.file
	}

	/**
	 * generates the list of classes sepcific to package
	 */
	void generateEntitiesPackageEntityList() {
		for (String packageName in docHelper.packages) {
			if (!packageName.equals(DocHelper.DEFAULT_NO_PACKAGE)) {
				def docFile = docFileManager.getPackageEntityListDocFile(packageName)
				def parameters = [docFile: docFile, title: packageName,
						classList: docHelper.getClasses(packageName)]
				processTemplate parameters, 'entity/perPackageEntity-list', docFile.file
			}
		}
	}

	/**
	 * Generates the html file containing list of classes and interfaces for
	 * given package
	 */
	void generateEntitiesPackageDetailedInfo() {
		List<?> packageList = docHelper.packages
		packageList.remove(0)
		for (String packageName in packageList) {
			def summaryDocFile = docFileManager.getPackageSummaryDocFile(packageName)
			def parameters = [docFile: summaryDocFile, 'package': packageName,
					classList: docHelper.getClasses(packageName)]
			processTemplate parameters, 'entity/package-summary', summaryDocFile.file
		}
	}

	/**
	 * Generate a file with a list of all the schemas in the configuration.
	 */
	void generateTablesAllSchemasList() {
		def docFile = docFileManager.allSchemasDocFile
		def parameters = [docFile: docFile, title: 'Schema List', schemaList: docHelper.schemas]
		processTemplate parameters, 'tables/schema-list', docFile.file
	}

	/**
	 * Generate a file with a list of all the tables in the configuration.
	 */
	void generateTablesAllTablesList() {
		def docFile = docFileManager.allTablesDocFile
		def parameters = [docFile: docFile, title: 'All Tables', tableList: docHelper.tables]
		processTemplate parameters, 'tables/table-list', docFile.file
	}

	void generateTablesSchemaTableList() {
		for (String schemaName in docHelper.schemas) {
			def docFile = docFileManager.getSchemaTableListDocFile(schemaName)
			def parameters = [docFile: docFile, title: "Tables for $schemaName",
					tableList: docHelper.getTables(schemaName)]
			processTemplate parameters, 'tables/table-list', docFile.file
		}
	}

	void generateTablesSchemaDetailedInfo() {
		for (String schemaName in docHelper.schemas) {
			def summaryDocFile = docFileManager.getSchemaSummaryDocFile(schemaName)

			def parameters = [docFile: summaryDocFile, schema: schemaName]
			processTemplate parameters, 'tables/schema-summary', summaryDocFile.file

			def tableListDocFile = docFileManager.getSchemaSummaryDocFile(schemaName)

			parameters.docFile = tableListDocFile
			processTemplate parameters, 'tables/schema-summary', tableListDocFile.file
		}
	}

	private void processTemplate(Map<String, Object> parameters, String templateName, File outputFile) {
		templateName = locateTemplate(templateName, '')
		producer.produce parameters, templateName, outputFile, templateName
	}

	private String locateTemplate(String templateName, String subfolder) {
		'/' + getClass().getPackage().name.replaceAll('\\.', '/') + '/' + subfolder + templateName + '.ftl'
	}

	@Override
	String getName() { 'hbm2doc' }

	boolean useJdk5() { false }

	DocHelper getDocHelper() { docHelper }
	FakeDocFileManager getDocFileManager() { docFileManager }
}
