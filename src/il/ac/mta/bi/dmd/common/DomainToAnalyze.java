package il.ac.mta.bi.dmd.common;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.Map;

import weka.core.Attribute;
import weka.core.FastVector;

public class DomainToAnalyze {
	private String domainName;
	private Map<String, Feature> featuresMap = new LinkedHashMap<String, Feature>();
	private Map<String, Object> propertiesMap = new HashMap<String, Object>();
	private ProcessingChain chain;
	private Classification classification = Classification.UNKNOWN;
	private double maliciousChance;
	private double benignChance;
	
	public enum Classification {
		UNKNOWN,
		MALICIOUS,
		BENIGN
	}
	
	/**Creates a DomainToAnalyze with a known
	 * classification
	 * @param domainName the domain's name
	 * @param classification the classification type
	 */
	public DomainToAnalyze (String domainName, 
							Classification classification) {
		this.domainName = domainName;
		this.classification = classification;
	}
	
	/**Creates a DomainToAnalyze with a known
	 * classification
	 * @param domainName the domain's name
	 * @param classification the classification type
	 */
	public DomainToAnalyze (String domainName, 
							String classification) {
		this.domainName = domainName;
		this.classification = Classification.UNKNOWN;

		if (classification.equals("MALICIOUS")) {
			this.classification = Classification.MALICIOUS;
			return;
		}
		if (classification.equals("BENIGN")) {
			this.classification = Classification.BENIGN;
			return;
		}
	}
	
	/**Creates a DomainToAnalyze with an unknown
	 * classification
	 * @param domainName the domain's name
	 */
	public DomainToAnalyze (String domainName) {
		this.domainName = domainName;
		this.classification = Classification.UNKNOWN;
	}

	/**Gets a map of features for the domain, to be used for data-mining
	 * purposes
	 * @return mapping between a feature name and it's value
	 */
	public Map<String, Feature> getFeaturesMap() {
		return featuresMap;
	}

	/**Gets the domain name
	 * @return the domain name
	 */
	public String getDomainName() {
		return domainName;
	}

	/**Gets the processing chain
	 * @return the processing chain
	 */
	public ProcessingChain getChain() {
		return chain;
	}

	public void setChain(ProcessingChain chain) {
		this.chain = chain;
	}

	public Classification getClassification() {
		return classification;
	}
	
	public void setClassification(double classification) {
		if (classification == 0) {
			this.classification = Classification.MALICIOUS;
			return;
		}
		if (classification == 1) {
			this.classification = Classification.BENIGN;
			return;
		}
	}

	public void setClassification(Classification classification) {
		this.classification = classification;
	}

	/**Gets a map of properties for the domain. The properties should
	 * be used for internal chain operations, or to pass data between the different chains.
	 * Unlike the features map, properties stored in this map aren't used for data-mining.
	 * @return mapping between a property name and it's value
	 */
	public Map<String, Object> getPropertiesMap() {
		return propertiesMap;
	}
	
	/**Insert all features collected by all chains attached to the domain 
	 * into a features map, which maps between the feature name and it's value
	 */
	public void init() {
		ArrayList<Feature> allFeaturesFromChain = chain.getAllFeaturesFromChain();
		for (Feature feature : allFeaturesFromChain) {
			featuresMap.put(feature.getName(), feature);
		}
		
	}
	
	public Attribute classToAttribute() {
	   FastVector fvClassVal = new FastVector();
	   fvClassVal.addElement(Classification.MALICIOUS.toString());
	   fvClassVal.addElement(Classification.BENIGN.toString());
	   
	   Attribute ClassAttribute = new Attribute("domainClassification", fvClassVal);
	   return ClassAttribute;
	}

	public void setDomainName(String domainName) {
		this.domainName = domainName;
	}
	
	@Override
	public String toString() {
		return getDomainName();
	}
	
	public String toStringFull() {
		StringBuilder sb = new StringBuilder();
		sb.append(domainName + " classification:" + '\n');
		sb.append("chain status: " + getChain().getStatus() + '\n');
		sb.append("last chain: " + getChain().getCurrChain().getChainName()+ '\n');
		sb.append('\n');
		
		for (Feature feature : featuresMap.values()) {
			sb.append(feature.getName() + ": " + feature.getValue()  + '\n');
		}
		sb.append('\n');
		
		sb.append("malicious chance=" + maliciousChance + '\n');
		sb.append("benign chance=" + benignChance + '\n');
		sb.append("domain is " + getClassification() + '\n');
		
		return sb.toString();
	}

	public double getMaliciousChance() {
		return maliciousChance;
	}

	public double getBenignChance() {
		return benignChance;
	}

	public void setMaliciousChance(double maliciousChance) {
		this.maliciousChance = maliciousChance;
	}

	public void setBenignChance(double benignChance) {
		this.benignChance = benignChance;
	}

}
