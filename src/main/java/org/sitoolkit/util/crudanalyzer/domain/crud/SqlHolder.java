package org.sitoolkit.util.crudanalyzer.domain.crud;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class SqlHolder {

	private List<String> tempQueryLines = new ArrayList<>();
	
	private Map<String, String> queryMap = new HashMap<>();

	private CollectStatus collectStatus;

	private CollectStatus exCollectStatus;

	private String collectingQuery = "";

	public void startSkipping() {
		exCollectStatus = collectStatus;
		collectStatus = CollectStatus.SKIPPING;
	}
	
	public boolean isSkipping() {
		return collectStatus == CollectStatus.SKIPPING;
	}

	public void endSkiping() {
		collectStatus = exCollectStatus;
		exCollectStatus = null;
	}

	public boolean isCollecting() {
		return collectStatus == CollectStatus.COLLECTING;
	}
	
	public boolean isWaitingForGt() {
		return collectStatus == CollectStatus.WAIT_FOR_GT;
	}

	public void startCollecting() {
		collectStatus = CollectStatus.COLLECTING;
	}

	public void startForWaitingGt(String collectingQuery) {
		this.collectingQuery = collectingQuery;
		collectStatus = CollectStatus.WAIT_FOR_GT;
	}
	
	public void addQueryLine(String queryLine) {
		tempQueryLines.add(queryLine);
	}

	public void endCollecting() {
		queryMap.put(collectingQuery, String.join(" ", tempQueryLines));
		collectingQuery = "";
		tempQueryLines.clear();
		collectStatus = CollectStatus.NOT_COLLECTING;
	}

	public Map<String, String> getQueryMap() {
		return queryMap;
	}
	
	enum CollectStatus {
		NOT_COLLECTING,
		WAIT_FOR_GT,
		COLLECTING,
		SKIPPING
	}

}
