package il.ac.mta.bi.dmd.infra;

import il.ac.mta.bi.dmd.common.DataSource;
import il.ac.mta.bi.dmd.common.DataTarget;
import il.ac.mta.bi.dmd.common.DomainToAnalyze;
import il.ac.mta.bi.dmd.common.DomainToAnalyze.Classification;
import il.mta.bi.dmd.config.ProgramProperties;

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
	private static Integer CACHE_ENTRY_TIMEOUT = 60*24;
	private static boolean CACHE_ALLOW = true;
	
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
		createCache();
	}

	private void createCache() {
		
		/* create cache to store classification results.
		 * if cache is allowed (CACHE_ALLOW is true) future queries will be checked in cache
		 * prior to classification
		 * */
		
		if(ProgramProperties.getProperties().getProperty("cache_max_size") != null)  {
			CACHE_MAX_SIZE = 
					Integer.parseInt(ProgramProperties.getProperties().getProperty("cache_max_size"));
		}
		if(ProgramProperties.getProperties().getProperty("cache_entry_timeout") != null)  {
			CACHE_ENTRY_TIMEOUT = 
					Integer.parseInt(ProgramProperties.getProperties().getProperty("cache_entry_timeout"));
		}
		if(ProgramProperties.getProperties().getProperty("cache_allow") != null)  {
			CACHE_ALLOW = 
					ProgramProperties.getProperties().
					getProperty("cache_allow").
					equalsIgnoreCase("true") ? true : false;
			
			if (CACHE_ALLOW) {
				logger.info("caching results for future queries allowed");
			}
		}
		globalCache = CacheBuilder.newBuilder().
				concurrencyLevel(1).
				maximumSize(CACHE_MAX_SIZE).
				expireAfterWrite(CACHE_ENTRY_TIMEOUT, TimeUnit.MINUTES).
				build();
	}
	
	/**
	 * Saves the DomainToAnalyze object in the global cache and calls all registered
	 * target sources. If CACHE_ALLOW is true, then prior to saving the domain in the cache,
	 * the cache is checked for it, and if it cached and the status of the classification is OK,
	 * the new domain will not overwrite the existing entry so it'll not be classified again
	 * @param domainToAnalyze
	 */

	public void save(DomainToAnalyze domainToAnalyze) {
		if (CACHE_ALLOW && getClassificationFromCache(domainToAnalyze) != null) {
			return;
		}
		
		logger.info("saved: " + domainToAnalyze.getDomainName() + " to global cache");
		
		globalCache.put(domainToAnalyze.getDomainName(), domainToAnalyze);
		
		for (DataTarget target : targets) {
			target.save(domainToAnalyze);
		}
	}

	/* If the same domain is tested on the same as two objects
	 * then the latter will always be stored in cache as they'll overwrite
	 * each other
	 */
	private DomainToAnalyze getClassificationFromCache(DomainToAnalyze domainToAnalyze) {
		DomainToAnalyze cachedDomainToAnalyze = 
				globalCache.getIfPresent(domainToAnalyze.getDomainName());
		
		if(cachedDomainToAnalyze != null && 
				cachedDomainToAnalyze.getClassification() != Classification.UNKNOWN) {
			logger.info(domainToAnalyze.getDomainName() + " is cached, no need to classify");
			domainToAnalyze.setPropertiesMap(cachedDomainToAnalyze.getPropertiesMap());
			domainToAnalyze.setFeaturesMap(cachedDomainToAnalyze.getFeaturesMap());
			domainToAnalyze.getChain().fastForward("Classifier Builder Runner");
			
			return cachedDomainToAnalyze;
		}
		return null;
	}

	public Cache<String, DomainToAnalyze> getGlobalCache() {
		return globalCache;
	}
}
