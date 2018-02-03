/*
 *    Copyright 2018 the original author or authors.
 *    
 *    Licensed under the Apache License, Version 2.0 (the "License"); you may not
 *    use this file except in compliance with the License.  You may obtain a copy
 *    of the License at
 *    
 *      http://www.apache.org/licenses/LICENSE-2.0
 *    
 *    Unless required by applicable law or agreed to in writing, software
 *    distributed under the License is distributed on an "AS IS" BASIS, WITHOUT
 *    WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.  See the
 *    License for the specific language governing permissions and limitations under
 *    the License.
 */
package org.nalby.yobatis.structure.eclipse;

import java.io.ByteArrayInputStream;
import java.io.InputStream;

import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IResource;
import org.nalby.yobatis.exception.ResourceNotAvailableExeception;
import org.nalby.yobatis.structure.File;
import org.nalby.yobatis.structure.Folder;
import org.nalby.yobatis.util.Expect;
import org.nalby.yobatis.util.FolderUtil;

public final class EclipseFile implements File {
	
	private IFile iFile;
	
	private String path;
	
	private Folder parent;
	
	public EclipseFile(Folder parent, IFile iFile) {
		Expect.notNull(iFile, "iFile must not be null.");
		Expect.notNull(parent, "path must not be null.");
		this.iFile = iFile;
		this.path = FolderUtil.concatPath(parent.path(), iFile.getName());
		this.parent = parent;
	}

	@Override
	public String name() {
		return iFile.getName();
	}

	@Override
	public String path() {
		return path;
	}

	@Override
	public InputStream open() {
		try {
			return iFile.getContents();
		} catch (Exception e) {
			throw new ResourceNotAvailableExeception(e);
		}
	}
	

	@Override
	public Folder parentFolder() {
		return parent;
	}

	@Override
	public void write(InputStream inputStream) {
		try {
			if (iFile.exists()) {
				iFile.delete(true, null);
			}
			iFile.create(inputStream, IResource.FILE, null);
			iFile.refreshLocal(0, null);
		} catch (Exception e) {
			throw new ResourceNotAvailableExeception(e);
		}
	}

	@Override
	public void write(String content) {
		Expect.notNull(content, "content must not be null.");
		try (InputStream inputStream = new ByteArrayInputStream(content.getBytes())) {
			write(inputStream);
		} catch (ResourceNotAvailableExeception e) {
			throw e;
		} catch (Exception e) {
			throw new ResourceNotAvailableExeception(e);
		}
	}
	
	public static File createFile(Folder parent, IFile iFile) {
		try {
			File file = new EclipseFile(parent, iFile);
			try (InputStream inputStream = new ByteArrayInputStream(new byte[0])) {
				file.write(inputStream);
			}
			return file;
		} catch (ResourceNotAvailableExeception e) {
			throw e;
		} catch (Exception e) {
			throw new ResourceNotAvailableExeception(e);
		}
	}

}
