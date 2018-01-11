package org.nalby.yobatis.structure;

import java.io.InputStream;

public interface File {
	
	String name();
	
	String path();

	InputStream open();
	
	void write(InputStream inputStream);

	void write(String content);
	
	/**
	 * Get the folder that contains this file.
	 * @return parent folder.
	 */
	Folder parentFolder();

}
