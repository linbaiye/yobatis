package org.nalby.yobatis.structure;

import java.util.LinkedList;
import java.util.List;

import org.mybatis.generator.api.GeneratedJavaFile;
import org.mybatis.generator.api.GeneratedXmlFile;
import org.mybatis.generator.api.LibraryRunner;
import org.nalby.yobatis.exception.ProjectException;

public class MybatisGeneratorWrapper {

	private LibraryRunner runner;

	public MybatisGeneratorWrapper(LibraryRunner runner) {
		this.runner = runner;
		if (runner.getGeneratedJavaFiles() == null) {
			throw new ProjectException("No java files generated.");
		}
		if (runner.getGeneratedXmlFiles() == null) {
			throw new ProjectException("No xml files generated.");
		}
	}

	private List<GeneratedJavaFile> listFile(String suffix) {
		List<GeneratedJavaFile> result = new LinkedList<GeneratedJavaFile>();
		List<GeneratedJavaFile> javaFiles = runner.getGeneratedJavaFiles();
		for (GeneratedJavaFile file : javaFiles) {
			if (file.getFileName() != null
					&& file.getFileName().endsWith(suffix)) {
				result.add(file);
			}
		}
		return result;
	}

	public List<GeneratedJavaFile> getMapperFiles() {
		return listFile("Mapper.java");
	}
	
	public List<GeneratedJavaFile> getCriteriaFiles() {
		return listFile("Criteria.java");
	}

	public List<GeneratedJavaFile> getDomainFiles() {
		List<GeneratedJavaFile> javaFiles = runner.getGeneratedJavaFiles();
		List<GeneratedJavaFile> result = new LinkedList<GeneratedJavaFile>();
		for (GeneratedJavaFile file : javaFiles) {
			if (file.getFileName() != null &&
				(!file.getFileName().endsWith("Mapper.java") &&
				 !file.getFileName().endsWith("Criteria.java"))) {
				result.add(file);
			}
		}
		return result;
	}
	
	public List<GeneratedXmlFile> getXmlFiles() {
		return runner.getGeneratedXmlFiles();
	}

}
