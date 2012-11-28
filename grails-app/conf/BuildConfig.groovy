grails.project.work.dir = 'target'
grails.project.source.level = 1.6
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
		compile('org.hibernate:hibernate-tools:3.6.0.CR1') {
			excludes 'ant', 'ant-launcher', 'cglib', 'common', 'commons-logging', 'freemarker',
						'hibernate-core', 'hibernate-entitymanager', 'hibernate-jpa-2.0-api', 'hsqldb',
						'jaxen', 'javassist', 'jta', 'jtidy', 'junit', 'org.eclipse.jdt.core', 'runtime',
						'slf4j-log4j12', 'text'
		}

		compile 'freemarker:freemarker:2.3.8'

		compile 'org.hibernate:jtidy:r8-20060801'
	}

	plugins {

		compile ':app-info:1.0.2'
		compile ":hibernate:$grailsVersion"

		build(':release:2.1.0', ':rest-client-builder:1.0.2') {
			export = false
		}
	}
}
