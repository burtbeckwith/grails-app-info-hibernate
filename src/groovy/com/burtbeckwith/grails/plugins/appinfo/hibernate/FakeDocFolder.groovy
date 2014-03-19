package com.burtbeckwith.grails.plugins.appinfo.hibernate

import org.springframework.util.Assert

/**
 * @author <a href='mailto:burt@burtbeckwith.com'>Burt Beckwith</a>
 */
class FakeDocFolder {

	/**
	 * The name of the folder.
	 */
	String name

	/**
	 * The parent folder.
	 */
	FakeDocFolder parent

	/**
	 * The File instance.
	 */
	final File file

	/**
	 * Holds a list with the folders that are between this folder and root.
	 */
	final List<FakeDocFolder> pathFolders = []

	/**
	 * Constructor for the root folder.
	 *
	 * @param root the File that represents the root for the documentation.
	 */
	FakeDocFolder(File root) {
		Assert.notNull root, 'Root File cannot be null'
		file = root
		pathFolders << this
	}

	/**
	 * Constructor.
	 *
	 * @param name  the name of the file.
	 * @param parent  the parent folder.
	 */
	FakeDocFolder(String name, FakeDocFolder parent) {
		Assert.notNull name, 'Name cannot be null'
		Assert.notNull parent, 'Parent folder cannot be null'

		this.name = name
		this.parent = parent
		file = new File(parent.file, name)

		if (parent) {
			pathFolders.addAll(parent.pathFolders)
			pathFolders << this
		}
	}

	@Override
	String toString() { name }
}
