package il.ac.mta.bi.dmd.common;

import org.apache.log4j.Logger;

import com.google.common.cache.Cache;

public abstract class DataTarget {
	static Logger logger = Logger.getLogger(DataSource.class);

	private String targetName;
	private Cache<String, DomainToAnalyze> globalCache;
	
	public abstract void save(DomainToAnalyze domainToAnalyze);

	public String getSourceName() {
		return targetName;
	}
	
	public void setTargetName(String targetName) {
		this.targetName = targetName;
	}

	public Cache<String, DomainToAnalyze> getGlobalCache() {
		return globalCache;
	}

	public void setGlobalCache(Cache<String, DomainToAnalyze> globalCache) {
		this.globalCache = globalCache;
	}
	
}
