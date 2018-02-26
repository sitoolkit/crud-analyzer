package org.sitoolkit.util.crudanalyzer.infra.filescan;

import java.io.IOException;
import java.nio.file.Path;
import java.util.Scanner;

import org.sitoolkit.util.crudanalyzer.infra.config.Config;

import lombok.extern.slf4j.Slf4j;

@Slf4j
public class FileScanner {

	public ScanningContext scan(Path filePath, LineProcessor<?>... processors) {
		
		ScanningContext context = new ScanningContext(filePath);
		
		try (Scanner scanner = new Scanner(filePath, Config.getInstance().getFileEncoding())) {
			while (scanner.hasNextLine()) {
				
				String line = scanner.nextLine();
				
				log.trace("rawLine : {}", line);
				
				for (LineProcessor<?> processor : processors) {
					processor.process(context, line);
				}
			}
		} catch (IOException e) {
			throw new IllegalStateException(e);
		}
		
		return context;
	}
}
