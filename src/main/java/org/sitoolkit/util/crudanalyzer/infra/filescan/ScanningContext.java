package org.sitoolkit.util.crudanalyzer.infra.filescan;

import java.nio.file.Path;
import java.util.HashMap;

import lombok.Getter;

public class ScanningContext extends HashMap<Object, Object>{

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;

	@Getter
	private Path filePath;

	public ScanningContext(Path filePath) {
		super();
		this.filePath = filePath;
	}
}
