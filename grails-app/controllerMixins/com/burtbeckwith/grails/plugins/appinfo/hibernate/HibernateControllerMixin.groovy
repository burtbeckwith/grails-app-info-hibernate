package com.burtbeckwith.grails.plugins.appinfo.hibernate

/**
 * @author <a href='mailto:burt@burtbeckwith.com'>Burt Beckwith</a>
 */
class HibernateControllerMixin {

	def hibernateProperties
	def hibernateInfoService

	/**
	 * The Hibernate overview page.
	 */
	def hibernate() {
		def configuration = hibernateInfoService.configuration
		render view: '/appinfo/hibernate',
		       plugin: 'appInfoHibernate',
		       model: [configuration: configuration,
		               mappings: configuration.createMappings(),
		               dialect: hibernateInfoService.dialectClass.newInstance(),
		               hibernateProperties: hibernateProperties] +
		               hibernateInfoService.tablesAndEntities
	}

	/**
	 * Shows the Hibernate 2nd-level cache page.
	 */
	def hibernateCaching() {
		render view: '/appinfo/hibernateCaching',
		       plugin: 'appInfoHibernate',
		       model: [statistics: hibernateInfoService.secondLevelCacheStatistics] +
		               hibernateInfoService.tablesAndEntities
	}

	def hibernateCacheGraphs() {

		String cacheName = params.cacheName

		String shortName = cacheName
		int index = shortName.lastIndexOf('.')
		if (index > -1) {
			shortName = shortName.substring(index + 1)
		}

		def cacheData = hibernateInfoService.getCacheData(cacheName)
		def cacheMemoryData = hibernateInfoService.getCacheMemoryData(cacheName)

		render view: '/appinfo/hibernateCacheGraphs',
		       plugin: 'appInfoHibernate',
		       model: [name: fixCacheName(cacheName),
		               shortName: shortName,
		               cacheTypeNames: cacheData.keySet(), cacheData: cacheData,
		               cacheMemoryTypeNames: cacheMemoryData.keySet(), cacheMemoryData: cacheMemoryData] +
		               hibernateInfoService.tablesAndEntities
	}

	/**
	 * Clears the specified cache.
	 */
	def hibernateClearCache() {
		String cacheName = params.cacheName

		hibernateInfoService.clearCache(cacheName)
		redirect action: 'hibernateCaching', controller: params.controller
	}

	def hibernateTableInfo() {
		String table = params.table

		Map<String, Object> info = hibernateInfoService.lookupTableInfo(table)
		if (!info) {
			render "Table $table not found"
			return
		}

		render view: '/appinfo/hibernateTableInfo',
		       plugin: 'appInfoHibernate',
		       model: info + hibernateInfoService.tablesAndEntities
	}

	def hibernateEntityInfo() {
		String entity = params.entity

		Map<String, Object> info = hibernateInfoService.lookupEntityInfo(entity)
		if (!info) {
			render "Entity $entity not found"
			return
		}

		render view: '/appinfo/hibernateEntityInfo',
		       plugin: 'appInfoHibernate',
		       model: info + hibernateInfoService.tablesAndEntities
	}

	/**
	 * Generate the hbm.xml for the specified entity.
	 */
	def hibernateHbm() {
		String entity = params.entity
		render view: '/appinfo/hibernateHbm',
		       plugin: 'appInfoHibernate',
		       model: [hbm: hibernateInfoService.generateHbmXml(entity),
		               entity: entity] + hibernateInfoService.tablesAndEntities
	}

	/**
	 * Render the entity graph.
	 */
	def hibernateEntityImage() {
		response.contentType = 'image/png'
		response.outputStream << hibernateInfoService.generateEntityImage()
		response.outputStream.flush()
	}

	def hibernateEntityGraph() {
		String cmap = hibernateInfoService.generateEntityGraphCmap(
			g.createLink(action: 'hibernateEntityInfo').toString())
		render view: '/appinfo/hibernateEntityGraph',
		       plugin: 'appInfoHibernate',
				 model: [entitygrapharea: cmap] + hibernateInfoService.tablesAndEntities
	}

	def hibernateTableImage() {
		response.contentType = 'image/png'
		response.outputStream << hibernateInfoService.generateTableImage()
		response.outputStream.flush()
	}

	def hibernateTableGraph() {
		String cmap	= hibernateInfoService.generateTableGraphCmap(
			g.createLink(action: 'hibernateEntityGraph').toString())
		render view: '/appinfo/hibernateTableGraph',
		       plugin: 'appInfoHibernate',
		       model: [tablegrapharea: cmap] + hibernateInfoService.tablesAndEntities
	}

	def hibernateStatistics() {
		render view: '/appinfo/hibernateStatistics',
		       plugin: 'appInfoHibernate',
		       model: [stats: hibernateInfoService.lookupStatisticsValues(),
		               extra: hibernateInfoService.lookupSecondaryStatisticsValues(),
							statisticsEnabled: hibernateInfoService.statisticsEnabled] +
		               hibernateInfoService.tablesAndEntities
	}

	def hibernateStatisticsReset() {
		hibernateInfoService.resetStatistics()
		redirect action: 'hibernateStatistics', controller: params.controller
	}

	def hibernateStatisticsEnable() {
		hibernateInfoService.statisticsEnabled = params.enable?.toBoolean()
		redirect action: 'hibernateStatistics', controller: params.controller
	}

	def hibernateEntityStatistics() {
		String entity = params.entity
		
		render view: '/appinfo/hibernateEntityStatistics',
		       plugin: 'appInfoHibernate',
		       model: [entity: entity,
		               stats: hibernateInfoService.lookupEntityStatistics(entity),
		               statisticsEnabled: hibernateInfoService.statisticsEnabled] +
		               hibernateInfoService.tablesAndEntities
	}

	def hibernateCollectionStatistics() {
		String collection = params.collection

		render view: '/appinfo/hibernateCollectionStatistics',
		       plugin: 'appInfoHibernate',
		       model: [collection: collection,
		               stats: hibernateInfoService.lookupCollectionStatistics(collection),
		               statisticsEnabled: hibernateInfoService.statisticsEnabled] +
		               hibernateInfoService.tablesAndEntities
	}

	def hibernateQueryStatistics() {
		String query = params.query

		render view: '/appinfo/hibernateQueryStatistics',
		       plugin: 'appInfoHibernate',
		       model: [query: query,
		               stats: hibernateInfoService.lookupQueryStatistics(query),
		               statisticsEnabled: hibernateInfoService.statisticsEnabled] +
		               hibernateInfoService.tablesAndEntities
	}

	protected String fixCacheName(String cacheName) {
		String fixed = cacheName
		fixed -= 'org.hibernate.cache.'
		fixed
	}
}
