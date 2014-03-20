grails.project.work.dir = 'target'
grails.project.docs.output.dir = 'docs/manual' // for backwards-compatibility, the docs are checked into gh-pages branch

grails.project.dependency.resolution = {

	inherits 'global'
	log 'warn'

	repositories {
		grailsCentral()
		mavenLocal()
		mavenCentral()
	}

	dependencies {
		compile 'org.hibernate:hibernate-tools:3.6.0.CR1', {
			excludes 'ant', 'ant-launcher', 'cglib', 'common', 'commons-logging', 'freemarker',
						'hibernate-core', 'hibernate-entitymanager', 'hibernate-jpa-2.0-api', 'hsqldb',
						'jaxen', 'javassist', 'jta', 'jtidy', 'junit', 'org.eclipse.jdt.core', 'runtime',
						'slf4j-log4j12', 'text'
		}

		compile 'org.hibernate:hibernate-core:3.6.10.Final', {
			excludes 'ant', 'antlr', 'cglib', 'commons-collections', 'commons-logging', 'commons-logging-api',
			         'dom4j', 'h2', 'hibernate-commons-annotations', 'hibernate-jpa-2.0-api', 'hibernate-validator',
			         'javassist', 'jaxb-api', 'jaxb-impl', 'jcl-over-slf4j', 'jboss-jacc-api_JDK4', 'jta',
			         'junit', 'slf4j-api', 'slf4j-log4j12', 'validation-api'
		}

		compile 'org.hibernate:hibernate-commons-annotations:3.2.0.Final', {
			excludes 'commons-logging', 'commons-logging-api', 'jcl-over-slf4j', 'junit', 'slf4j-api', 'slf4j-log4j12'
		}

		runtime 'dom4j:dom4j:1.6.1', {
			excludes 'jaxen', 'jaxme-api', 'junitperf', 'pull-parser', 'relaxngDatatype', 'stax-api',
			         'stax-ri', 'xalan', 'xercesImpl', 'xpp3', 'xsdlib', 'xml-apis'
		}

		compile 'freemarker:freemarker:2.3.8'

		compile 'org.hibernate:jtidy:r8-20060801'
	}

	plugins {

		compile ':app-info:1.1'

		build ':release:2.2.1', ':rest-client-builder:1.0.3', {
			export = false
		}
	}
}
