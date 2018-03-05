package org.sitoolkit.util.crudanalyzer.infra.config;

import java.io.IOException;
import java.net.URI;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.HashMap;
import java.util.Map;
import java.util.Properties;

import lombok.extern.slf4j.Slf4j;

@Slf4j
public class Config {


	private static Config instance = new Config();
	private Properties prop = new Properties();
	private Map<String, String> replaceMap;

	public static Config getInstance() {
		return instance;
	}

	static {
		load();
	}

	static void load() {
		Path configFile = Paths.get("crud-analyzer.properties");
		if (!configFile.toFile().exists()) {
			return;
		}
		log.info("Config {}", configFile.toAbsolutePath());
		try {
			instance.prop.load(Files.newInputStream(configFile));
		} catch (IOException e) {
			throw new RuntimeException(e);
		}

	}

	public String getSrcDir() {
		return prop.getProperty("srcDir", "src/main/java");
	}

	public String getResDir() {
		return prop.getProperty("resDir", "src/main/resources");
	}

	public String getOutFile() {
		return prop.getProperty("outFile", "crud.csv");
	}
	
	public String getFileEncoding() {
		return prop.getProperty("fileEncoding", "UTF-8");
	}
	
	public Map<String, String> getReplaceMap() {
		if (replaceMap == null) {
			replaceMap  = new HashMap<>();
			String replace = prop.getProperty("replace", "");
			
			for (String pair1 : replace.split(",")) {
				String[] pair2 = pair1.split(":");
				replaceMap.put(pair2[0], pair2[1]);
			}
		}
		return replaceMap;
	}

	public String getJarList() {
		return prop.getProperty("jarList", "jar-list.txt");
	}

	public String getRepositoryPathPattern() {
		return prop.getProperty("repositoryPathPattern");
	}
	
	public String getJavaFilePattern() {
		return prop.getProperty("javaFilePattern", ".*\\.java");
	}
}
