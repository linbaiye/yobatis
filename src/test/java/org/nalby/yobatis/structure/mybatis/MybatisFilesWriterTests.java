package org.nalby.yobatis.structure.mybatis;

import static org.junit.Assert.fail;
import static org.mockito.Matchers.anyString;
import static org.mockito.Mockito.doAnswer;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.io.ByteArrayInputStream;
import java.util.LinkedList;
import java.util.List;

import org.junit.Before;
import org.junit.Test;
import org.mockito.invocation.InvocationOnMock;
import org.mockito.stubbing.Answer;
import org.mybatis.generator.api.GeneratedJavaFile;
import org.mybatis.generator.api.GeneratedXmlFile;
import org.mybatis.generator.api.LibraryRunner;
import org.nalby.yobatis.exception.InvalidMybatisGeneratorConfigException;
import org.nalby.yobatis.mybatis.MybatisFilesWriter;
import org.nalby.yobatis.mybatis.MybatisGeneratorAnalyzer;
import org.nalby.yobatis.structure.File;
import org.nalby.yobatis.structure.Project;
import org.nalby.yobatis.xml.SqlMapperParserTests;

public class MybatisFilesWriterTests {
	
	private LibraryRunner mockedRunner;
	
	private List<GeneratedJavaFile> javaFiles;
	
	private List<GeneratedXmlFile> xmlFiles;
	
	private final static String DAO_PATH = "/src/main/java/yobatis/dao";

	private final static String MODEL_PATH = "/src/main/java/yobatis/model";

	private final static String CRITERIA_PATH = "/src/main/java/yobatis/model/criteria";

	private final static String MODEL_PACKAGE_NAME = "yobatis.model";

	private final static String MAPPER_PACKAGE_NAME = "yobatis.dao";

	private final static String XML_MAPPER_PATH = "/src/main/resource/mybatis-mappers";
	
	private MybatisGeneratorAnalyzer analyzer;
	
	private Project project;
	
	private MybatisFilesWriter filesWriter;

	private File file;
	

	@Before
	public void setup() {
		mockedRunner = mock(LibraryRunner.class);
		javaFiles = new LinkedList<>();
		xmlFiles = new LinkedList<>();
		when(mockedRunner.getGeneratedJavaFiles()).thenReturn(javaFiles);
		when(mockedRunner.getGeneratedXmlFiles()).thenReturn(xmlFiles);
		
		analyzer = mock(MybatisGeneratorAnalyzer.class);
		when(analyzer.getCriteriaDirPath()).thenReturn(CRITERIA_PATH);
		when(analyzer.getDaoDirPath()).thenReturn(DAO_PATH);
		when(analyzer.getModelDirPath()).thenReturn(MODEL_PATH);
		when(analyzer.getDaoPackageName()).thenReturn(MAPPER_PACKAGE_NAME);
		when(analyzer.getModelPackageName()).thenReturn(MODEL_PACKAGE_NAME);
		when(analyzer.getXmlMapperDirPath()).thenReturn(XML_MAPPER_PATH);

		file = mock(File.class);
		doNothing().when(file).write(anyString());

		project = mock(Project.class);
		when(project.createFile(anyString())).thenReturn(file);
	}
	
	
	private GeneratedJavaFile mockJavaFile(String name, String content) {
		GeneratedJavaFile javaFile = mock(GeneratedJavaFile.class);
		when(javaFile.getFileName()).thenReturn(name);
		when(javaFile.getFormattedContent()).thenReturn(content);
		javaFiles.add(javaFile);
		return javaFile;
	}
	
	
	private GeneratedXmlFile mockXmlFile(String name, String content) {
		GeneratedXmlFile xmlFile = mock(GeneratedXmlFile.class);
		when(xmlFile.getFileName()).thenReturn(name);
		when(xmlFile.getFormattedContent()).thenReturn(content);
		xmlFiles.add(xmlFile);
		return xmlFile;
	}
	
	
	private void build() {
		filesWriter = new MybatisFilesWriter(project, analyzer, mockedRunner);
	}
	
	@Test(expected = InvalidMybatisGeneratorConfigException.class)
	public void emptyLists() {
		try {
			when(mockedRunner.getGeneratedJavaFiles()).thenReturn(null);
			build();
			fail();
		} catch (InvalidMybatisGeneratorConfigException e) {
			//Expected
		}
		when(mockedRunner.getGeneratedJavaFiles()).thenReturn(javaFiles);

		when(mockedRunner.getGeneratedXmlFiles()).thenReturn(null);
		build();
	}
	
	@Test
	public void writeModelFile() {
		mockJavaFile("Test.java", "test content");
		build();
		filesWriter.writeAll();
		verify(project, times(1)).createFile(MODEL_PATH + "/Test.java");
		verify(file, times(1)).write("test content");
	}
	
	@Test
	public void writeCriteriaFile() {
		mockJavaFile("TestCriteria.java", "test content");
		build();
		filesWriter.writeAll();
		verify(project, times(1)).createFile(MODEL_PATH + "/criteria/TestCriteria.java");
		verify(file, times(1)).write("test content");
	}
	
	//Write only not exist.
	@Test
	public void createJavaMapperFile() {
		mockJavaFile("TestMapper.java", "test content");
		when(project.findFile(anyString())).thenReturn(null);
		build();
		filesWriter.writeAll();
		verify(project, times(1)).createFile(DAO_PATH + "/TestMapper.java");
		verify(file, times(1)).write("test content");
	}
	
	@Test
	public void preserveJavaMapperFile() {
		mockJavaFile("TestMapper.java", "test content");
		when(project.findFile(anyString())).thenReturn(file);
		build();
		filesWriter.writeAll();
		verify(project, times(0)).createFile(anyString());
		verify(file, times(0)).write(anyString());
	}
	
	@Test
	public void createXmlFile() {
		mockXmlFile("test.xml", "<mapper></mapper>");
		when(project.findFile(anyString())).thenReturn(null);
		build();
		filesWriter.writeAll();
		verify(project, times(1)).createFile(XML_MAPPER_PATH + "/test.xml");
		verify(file, times(1)).write("<mapper></mapper>");
	}
	
	@Test
	public void mergeXmlFile() {
		String file1 = "<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n" + 
				"<!DOCTYPE mapper PUBLIC \"-//mybatis.org//DTD Mapper 3.0//EN\" \"http://mybatis.org/dtd/mybatis-3-mapper.dtd\">\n" + 
				"<mapper namespace=\"dao.BlogMapper\">\n" + 
				"  <resultMap id=\"BaseResultMap\" type=\"hello.world.domain.Blog\">\n" + 
				"    <!--\n" + 
				"      WARNING - @mbg.generated\n" + 
				"      This element is automatically generated by MyBatis Generator, do not modify.\n" + 
				"      This element was generated on Mon Dec 04 21:25:54 PHT 2017.\n" + 
				"    -->\n" + 
				"    <id column=\"id\" jdbcType=\"BIGINT\" property=\"id\" />\n" + 
				"    <result column=\"name\" jdbcType=\"VARCHAR\" property=\"name\" />\n" + 
				"  </resultMap>\n" + 
				"</mapper>";
		String file2 = "<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n" + 
				"<!DOCTYPE mapper PUBLIC \"-//mybatis.org//DTD Mapper 3.0//EN\" \"http://mybatis.org/dtd/mybatis-3-mapper.dtd\">\n" + 
				"<mapper namespace=\"dao.BlogMapper\">\n" + 
				"  <!--sql id=\"test\"></sql-->\n" + 
				"  <sql id=\"test\"><!-- test comment --></sql>\n" + 
				"</mapper>";
		mockXmlFile("test.xml", file1);
		File existedFile = mock(File.class);
		when(existedFile.open()).thenReturn(new ByteArrayInputStream(file2.getBytes()));
		when(project.findFile(XML_MAPPER_PATH + "/test.xml")).thenReturn(existedFile);
		build();
		doAnswer(new Answer<Object>() {
			@Override
			public Object answer(InvocationOnMock invocation) throws Throwable {
				String tmp = (String)invocation.getArguments()[0];
				SqlMapperParserTests.SqlMapper sqlMapper = 
						new SqlMapperParserTests.SqlMapper(new ByteArrayInputStream(tmp.getBytes()));
				sqlMapper.assertHasElement("test");
				sqlMapper.assertHasElement("BaseResultMap");
				return null;
			}
		}).when(file).write(anyString());
		filesWriter.writeAll();
	}
	
	@Test
	public void notOverwriteModelFile() {
		mockJavaFile("Test.java", "test content");
		when(project.findFile(anyString())).thenReturn(file);
		build();
		filesWriter.writeAll();
		verify(project, times(0)).createFile(MODEL_PATH + "/Test.java");
		verify(file, times(0)).write("test content");
	}
	
	@Test
	public void overwriteBaseModelClass() {
		String content = "package org.nalby.yobatis.mybatis;\n" + 
				"\n" + 
				"import java.io.InputStream;\n" + 
				"import java.util.LinkedList;\n" + 
				"import java.util.List;\n" + 
				"import java.util.regex.Matcher;\n" + 
				"import java.util.regex.Pattern;\n" + 
				"public abstract class MybatisFilesWriter {";
		mockJavaFile("Test.java", content);
		when(project.findFile(anyString())).thenReturn(file);
		build();
		filesWriter.writeAll();
		verify(project, times(1)).createFile(MODEL_PATH + "/base/Test.java");
		verify(file, times(1)).write(content);
	}


}
