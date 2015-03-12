package il.ac.mta.bi.dmd.chain.runner;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.Collection;
import java.util.Map;
import java.util.concurrent.Callable;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.Semaphore;
import java.util.regex.Pattern;

import org.apache.log4j.Logger;
import org.xbill.DNS.Record;

import il.ac.mta.bi.dmd.common.DomainToAnalyze;
import il.ac.mta.bi.dmd.common.Feature;
import il.ac.mta.bi.dmd.common.ProcessChain;
import il.ac.mta.bi.dmd.common.ProcessingChain;
import il.ac.mta.bi.dmd.common.ProcessingChain.chainStatus;
import il.ac.mta.bi.dmd.dictionary.ahocorasick.trie.Emit;
import il.ac.mta.bi.dmd.dictionary.ahocorasick.trie.Trie;
import il.ac.mta.bi.dmd.infra.Factory;

/**
 * This chain runner calculate the ratio of the domain name string by
 * number of appearance of words in english are on the domain name string
 * the result number is the ratio of the numbers appearance with the number of characters */
public class ChainRunnerDictRatio extends ProcessChain {
	static Logger logger = Logger.getLogger(ChainRunnerDictRatio.class);

	private static Boolean 			isFirstRun = true;
	private static final Trie 		trie			  = new Trie();
	private static final String DICTIONARY_FILE = "data\\US.dic";
	
	public ChainRunnerDictRatio() {
		setChainName("Domain Name Dictionary Ratio");
		
		chainFeaturesList.add(Factory.getFactory().getIntegerFeature("dictRatio"));
		
		if (isFirstRun == true) {
			firstChainInit();
		}
	}

	private void firstChainInit() {
		if (isFirstRun == true) {
			
			try {
				readFileIntoTrie(new File(DICTIONARY_FILE), trie);
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			
			isFirstRun = false;
		}
	}
	

	private  void readFileIntoTrie(File file, Trie myTrie) throws IOException {
		FileInputStream fis = new FileInputStream(file);
	 
		//Construct BufferedReader from InputStreamReader
		BufferedReader br = new BufferedReader(new InputStreamReader(fis));
	 
		String line = null;
		while ((line = br.readLine()) != null) {
			myTrie.addKeyword(line);
			//System.out.println(line);
		}
	 
		br.close();
	}
	
	@Override
	public void run() {
		float percent = 0;
		chainStatus chainRunnerStatus = ProcessingChain.chainStatus.OK;
		try {
				logger.info("creating Dictionary Ratio request for " + domainToAnalyze.getDomainName());
				
				Collection<Emit> emits = trie.parseText(domainToAnalyze.getDomainName().split(Pattern.quote("."))[0]);
			    percent = (100* emits.size()) / domainToAnalyze.getDomainName().length();
			    System.out.println(domainToAnalyze.getDomainName() + " = " + percent + "%");
			
				logger.info("inserting " + percent + "% domain name ration by dictionary for " +
						domainToAnalyze.getDomainName());
				
				Map<String, Feature> featuresMap = domainToAnalyze.getFeaturesMap();
				Map<String, Object> propertiesMap = domainToAnalyze.getPropertiesMap();
				
				Feature feature = featuresMap.get("dictRatio");
				feature.setValue(Math.round(percent));
				featuresMap.put(feature.getName(), feature);
				propertiesMap.put(feature.getName(), percent);
		} catch (Exception e) {
			logger.error("caught exception ", e);
			chainRunnerStatus = ProcessingChain.chainStatus.ERROR;
		}
		finally {
			setStatus(chainRunnerStatus);
			flush();
		}
	}
}
