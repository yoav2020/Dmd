package il.ac.mta.bi.dmd.chain.runner;

import il.ac.mta.bi.dmd.common.Feature;
import il.ac.mta.bi.dmd.common.ProcessChain;
import il.ac.mta.bi.dmd.common.ProcessingChain;
import il.ac.mta.bi.dmd.common.ProcessingChain.chainStatus;
import il.ac.mta.bi.dmd.dictionary.ahocorasick.interval.IntervalableComparatorBySize;
import il.ac.mta.bi.dmd.dictionary.ahocorasick.trie.Emit;
import il.ac.mta.bi.dmd.dictionary.ahocorasick.trie.Trie;
import il.ac.mta.bi.dmd.infra.Factory;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.regex.Pattern;

import org.apache.log4j.Logger;

/**
 * This chain runner calculate the ratio of brand name in the domain name string by
 * number of appearances of words in a well known brand names list.
 * the result number is the ratio between the number of characters not appearing in any known
 * word to the full domain name.
 * 
 * For example, 100% means that the domain name doesn't contain a single brand name in it,
 * and 0% means the domain name is entirely made of brand names.
 * 
 * */

public class ChainRunnerBrandRatio extends ProcessChain {
	static Logger logger = Logger.getLogger(ChainRunnerBrandRatio.class);

	private static Boolean 			isFirstRun = true;
	private static final Trie 		trie			  = new Trie();
	private static final String DICTIONARY_FILE = "data\\brands.dic";
	
	public ChainRunnerBrandRatio() {
		setChainName("Domain Name Brand Ratio");
		
		chainFeaturesList.add(Factory.getFactory().getIntegerFeature("brandRatio"));
		
		if (isFirstRun == true) {
			firstChainInit();
		}
	}

	private void firstChainInit() {
		if (isFirstRun == true) {
			
			try {
				readFileIntoTrie(new File(DICTIONARY_FILE), trie);
			} catch (IOException e) {

				logger.error("caught exception ", e);
				setStatus(ProcessingChain.chainStatus.ERROR);
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
				logger.info("creating Brand Ratio request for " + domainToAnalyze.getDomainName());
				
				String tempDomain = domainToAnalyze.getDomainName().split(Pattern.quote("."))[0];
				List<Emit> listEmits = new ArrayList<Emit>(trie.parseText(tempDomain));
			    Collections.sort(listEmits , new IntervalableComparatorBySize());

			    for (Emit emit : listEmits) {
			    	if (tempDomain.length() == 0)
			    		break;
			    	tempDomain = tempDomain.replace(emit.getKeyword(), "");
				}
			    
			    percent = (100* tempDomain.length()) / domainToAnalyze.getDomainName().split(Pattern.quote("."))[0].length();
			    
			    //System.out.println(domainToAnalyze.getDomainName() + " = " + percent + "%");
			
				logger.info("inserting " + percent + "% domain name ratio by brand for " +
						domainToAnalyze.getDomainName());
				
				Map<String, Feature> featuresMap = domainToAnalyze.getFeaturesMap();
				Map<String, Object> propertiesMap = domainToAnalyze.getPropertiesMap();
				
				Feature feature = featuresMap.get("brandRatio");
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
