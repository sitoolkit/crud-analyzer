package org.sitoolkit.util.crudanalyzer.infra.filescan;

public interface LineProcessor<T> {

	void process(ScanningContext context, String line);
	
	public T getResult(ScanningContext context);	
	
}
