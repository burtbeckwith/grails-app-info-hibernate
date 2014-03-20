class AppInfoHibernateGrailsPlugin {

	String version = '0.4.1'
	String grailsVersion = '2.0 > *'
	String author = 'Burt Beckwith'
	String authorEmail = 'burt@burtbeckwith.com'
	String title = 'Hibernate Application Info'
	String description = 'Hibernate Application Info'
	String documentation = 'http://grails.org/plugin/app-info-hibernate'

	String license = 'APACHE'
	def issueManagement = [system: 'Github', url: 'https://github.com/burtbeckwith/grails-app-info-hibernate/issues']
	def scm = [url: 'https://github.com/burtbeckwith/grails-app-info-hibernate']

	def doWithApplicationContext = { ctx ->

		def config = ctx.grailsApplication.config.grails.plugins.appinfo
		if (!config.additional.Hibernate) {
			// add in the menu items if there isn't already something registered
			config.additional.Hibernate = [
				hibernate:            'Overview',
				hibernateEntityGraph: 'Entity Graphs',
				hibernateTableGraph:  'Table Graphs',
				hibernateCaching:     'Caching',
				hibernateStatistics:  'Statistics'
			]
		}
	}
}
