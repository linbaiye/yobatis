package org.nalby.yobatis.structure.eclipse;

import java.io.ByteArrayInputStream;
import java.io.InputStream;

import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IResource;
import org.nalby.yobatis.exception.ResourceNotAvailableExeception;
import org.nalby.yobatis.structure.File;
import org.nalby.yobatis.util.Expect;
import org.nalby.yobatis.util.FolderUtil;

public final class EclipseFile implements File {
	
	private IFile iFile;
	
	private String path;
	
	public EclipseFile(String parentPath, IFile iFile) {
		Expect.notNull(iFile, "iFile must not be null.");
		Expect.notEmpty(parentPath, "path must not be null.");
		this.iFile = iFile;
		this.path = FolderUtil.concatPath(parentPath, iFile.getName());
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
	
	public static File createFile(String parentPath, IFile iFile) {
		try {
			File file = new EclipseFile(parentPath, iFile);
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
