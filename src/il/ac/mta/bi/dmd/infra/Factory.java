package il.ac.mta.bi.dmd.infra;

import il.ac.mta.bi.dmd.chain.runner.ChainRunnerArffCreator;
import il.ac.mta.bi.dmd.chain.runner.ChainRunnerClassifierBuilder;
import il.ac.mta.bi.dmd.chain.runner.ChainRunnerClassify;
import il.ac.mta.bi.dmd.chain.runner.ChainRunnerDnsLookup;
import il.ac.mta.bi.dmd.chain.runner.ChainRunnerValidate;
import il.ac.mta.bi.dmd.chain.runner.ChainRunnerWhoisQuery;
import il.ac.mta.bi.dmd.common.DomainToAnalyze;
import il.ac.mta.bi.dmd.common.DomainToAnalyze.Classification;
import il.ac.mta.bi.dmd.common.Feature;
import il.ac.mta.bi.dmd.common.ProcessingChain;

import java.util.concurrent.Callable;
import java.util.concurrent.Future;
import java.util.concurrent.ScheduledThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

import org.apache.log4j.Logger;

/**
 * A singleton factory to create commonly used objects that require, 
 * configuration as well as the thread pool executer for various consumers used in the program. 
 * This class shouldn't be extended but instead expanded with new object creators; threads should
 * only be allocated by the factory
 */

public final class Factory {	
	private static Logger 	logger 	= Logger.getLogger(BackEndServer.class);
	private ScheduledThreadPoolExecutor executorService = new ScheduledThreadPoolExecutor(128);
	private static Factory 	theFactory = null;
	
	private Factory() {
	}
	
	/**
	 * Retrieves the factory instance
	 * @return the singleton factory
	 */
	public static Factory getFactory() {
		if (theFactory == null) {
			theFactory = new Factory();
		}
		return theFactory;
	}
	
	/**
	 * Gets a feature with a string value for data-mining purposes
	 * @return the string feature
	 */
	public Feature getStringFeature (String featureName) {
		return new Feature(featureName, Feature.FeatureType.STRING);
	}
	
	/**
	 * Gets a feature which an integer value for data-mining purposes
	 * @return the integer feature
	 */
	public Feature getIntegerFeature (String featureName) {
		return new Feature(featureName, Feature.FeatureType.INTEGER);
	}
	
	/**
	 * Gets a feature which a nominal for data-mining purposes
	 * @return the nominal feature
	 */
	public Feature getNominalFeature (String featureName) {
		throw new UnsupportedOperationException("not implemented");
	}
	
	/**
	 * Gets a DomainToAnalyze object, pre-configured with a valid processing chain.
	 * The features map includes all features collected from the chain
	 * @param domainName the domain name
	 * @param classification the domain class
	 * @return the nominal feature
	 */
	public DomainToAnalyze getDmainToAnalyze(String domainName, 
											 Classification classification) {
		logger.info("creating domain object for: " + domainName);
		
		ProcessingChain processingChain = new ProcessingChain();
		DomainToAnalyze domainToAnalyze = new DomainToAnalyze(domainName, classification);
		addChainRunners(processingChain);
		domainToAnalyze.setChain(processingChain);
		domainToAnalyze.init();
		
		return domainToAnalyze;
	}
	
	/**
	 * Populate the processing chain with all required feature chains
	 * @param processingChain the chain to populate
	 */
	private void addChainRunners(ProcessingChain processingChain) {
		/* ChainRunnerValidate */
		ChainRunnerValidate chainRunnderValidate = new ChainRunnerValidate();
		processingChain.addToChain(chainRunnderValidate);
		
		/* ChainRunnerDnsLookup*/
		ChainRunnerDnsLookup dnsLookupRunner = new ChainRunnerDnsLookup();
		processingChain.addToChain(dnsLookupRunner);
		
		/* ChainRunnerWhoisQuery  */
		ChainRunnerWhoisQuery chainRunnerWhoisQuery = new ChainRunnerWhoisQuery();
		processingChain.addToChain(chainRunnerWhoisQuery);
		
		/* ChainRunnerArffCreator*/
		ChainRunnerArffCreator chainRunnerArffCreator = new ChainRunnerArffCreator();
		processingChain.addToChain(chainRunnerArffCreator);
		
		/* ChainRunnerClassifierBuilder*/
		ChainRunnerClassifierBuilder chainRunnerClassifierBuilder = new ChainRunnerClassifierBuilder();
		processingChain.addToChain(chainRunnerClassifierBuilder);
		
		/* ChainRunnerClassify*/
		ChainRunnerClassify chainRunnerClassify = new ChainRunnerClassify();
		processingChain.addToChain(chainRunnerClassify);
	}
	
	/**
	 * Submit a scheduled task to run in a separate thread
	 * @param task - an object that performs the task when called
	 * @param delay - delay before task execution
	 * @param unit - time unit for the delay 
	 * @return future object returned from the completed task
	 */
	public <T> Future<T> getExecutorForCallableTask(Callable<T> task, long delay, TimeUnit unit) {
		logger.info("executing task in " + delay + " " + unit.toString() + " task " + task.toString());
		return executorService.schedule(task, delay, unit);
	}
	
	/**
	 * Submit a task to run in a separate thread
	 * @param task - an object that performs the task when called
	 * @return future object returned from the completed task 
	 */
	public <T> Future<T> getExecutorForCallableTask(Callable<T> task) {
		logger.info("executing task " + task.toString());
		return executorService.submit(task);
	}
	
	/**
	 * Executes a scheduled task to run in a separate thread at periodically, fixed times
	 * @param task - an object that performs the task when called
	 * @param initDelay - delay for initial execution
	 * @param delay - delay between task executions
	 * @param unit - time unit for the delay 
	 */
	public void getExecutorForPerodicRunnableTask(Runnable task, long initDelay, long delay, TimeUnit unit) {
		logger.info("executing task in " + initDelay + " " + unit.toString() + " periodically " + 
				delay + " " + unit.toString() + " task " + task.toString());
		executorService.scheduleWithFixedDelay(task, delay, delay, unit); 
	}
	
	/**
	 * Shutdown the factory, calling all functions required to safely close it
	 */
	public void shutDownFactory() {
		executorService.shutdown();
	}

}
