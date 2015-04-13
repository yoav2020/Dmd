package il.ac.mta.bi.dmd.infra;

import il.ac.mta.bi.dmd.common.DataSource;
import il.ac.mta.bi.dmd.common.DataTarget;
import il.ac.mta.bi.dmd.common.DomainToAnalyze;

import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.TimeUnit;

import org.apache.log4j.Logger;

import com.google.common.cache.Cache;
import com.google.common.cache.CacheBuilder;

public class Saver {
static Logger logger = Logger.getLogger(Saver.class);

	private static Saver theSaver = null;
	private List<DataTarget> targets = new CopyOnWriteArrayList<>();
	private Cache<String, DomainToAnalyze> globalCache;
	private static Integer CACHE_MAX_SIZE = 50000;
	
	public static Saver getSaver() {
		if (theSaver == null) {
			theSaver = new Saver();
		}
		return theSaver;
	}
	
	public void setDataTarget(DataTarget target) {
		logger.info("added data target " + target.getSourceName());
		target.setGlobalCache(globalCache);
		targets.add(target);
	}
	
	public void removeSaver(DataSource source) {
		targets.remove(source);
	}
	
	private Saver(){
		/* create cache to store classification results */
		globalCache = CacheBuilder.newBuilder().
				concurrencyLevel(1).
				maximumSize(CACHE_MAX_SIZE).
				expireAfterWrite(60*24, TimeUnit.MINUTES).
				build();
	}
	
	/**
	 * Saves the DomainToAnalyze object in the global cache and calls all registered
	 * target sources
	 * @param domainToAnalyze
	 */

	public void save(DomainToAnalyze domainToAnalyze) {
		logger.info("saved: " + domainToAnalyze.getDomainName() + " to global cache");
		
		globalCache.put(domainToAnalyze.getDomainName(), domainToAnalyze);
		
		for (DataTarget target : targets) {
			target.save(domainToAnalyze);
		}
	}

	public Cache<String, DomainToAnalyze> getGlobalCache() {
		return globalCache;
	}
}
