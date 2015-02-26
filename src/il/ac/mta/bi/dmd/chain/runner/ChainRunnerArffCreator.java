package il.ac.mta.bi.dmd.chain.runner;

import java.util.HashMap;
import java.util.Map;

import il.ac.mta.bi.dmd.common.Feature;
import il.ac.mta.bi.dmd.common.ProcessChain;

import org.apache.log4j.Logger;

import weka.classifiers.bayes.NaiveBayesUpdateable;
import weka.core.Attribute;
import weka.core.FastVector;
import weka.core.Instances;

public class ChainRunnerArffCreator extends ProcessChain {
	private static Logger logger = Logger.getLogger(ChainRunnerArffCreator.class);
	private static ChainRunnerArffCreator theChainRunnerArffCreator;
	private FastVector fvWekaAttributes;
	
	public static ChainRunnerArffCreator getChainRunnerArffCreator() {
		if (theChainRunnerArffCreator == null) {
			theChainRunnerArffCreator = new ChainRunnerArffCreator();
		}
		return theChainRunnerArffCreator;
	}

	private ChainRunnerArffCreator() {
		setChainName("Arff Consturctor");
	}

	@Override
	public void run() {
		fvWekaAttributes = createFvWekaAttributes();
		flush();
	}
	
	private FastVector createFvWekaAttributes() {
		HashMap<String, Feature> featuresMap = domainToAnalyze.getFeaturesMap();
		FastVector fvWekaAttributes = new FastVector();
		
		for (Feature feature : featuresMap.values()) {
			fvWekaAttributes.addElement(feature.toAttribute().toString());
			logger.info("adding attribute to model: " + feature.toAttribute().toString());
		}
		
	   FastVector fvClassVal = new FastVector();
	   fvClassVal.addElement("malicious");
	   fvClassVal.addElement("benign");
	   Attribute ClassAttribute = new Attribute("domainClassification", fvClassVal);
	   fvWekaAttributes.addElement(ClassAttribute);
	
	   logger.info("created Attribute-Relation File Format (arff) successfully!");
	   
	   return fvClassVal;
	}
}
