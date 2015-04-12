package il.ac.mta.bi.dmd.common;

import org.apache.log4j.Logger;

public abstract class DataTarget {
	static Logger logger = Logger.getLogger(DataSource.class);

	private String targetName;
	
	public abstract void save(DomainToAnalyze domainToAnalyze);

	public String getSourceName() {
		return targetName;
	}
	
	public void setTargetName(String targetName) {
		this.targetName = targetName;
	}
	
}
