package org.nalby.yobatis.structure;

import java.io.InputStream;

public interface File {
	
	String name();
	
	String path();

	InputStream open();
	
	void write(String name, String content);

}
