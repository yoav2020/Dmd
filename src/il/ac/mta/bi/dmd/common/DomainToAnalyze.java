package il.ac.mta.bi.dmd.common;

import java.util.ArrayList;
import java.util.HashMap;

public class DomainToAnalyze {
	private String domainName;
	private HashMap<String, Feature> featuresMap = new HashMap<String, Feature>();
	private HashMap<String, Object> propertiesMap = new HashMap<String, Object>();
	private ProcessingChain chain;
	private Classification classification = Classification.UNKNOWN;
	
	public enum Classification {
		UNKNOWN,
		MALICIOUS,
		BENIGN
	}
	
	/**Creates a DomainToAnalyze
	 * @param domainName the domain's name
	 */
	public DomainToAnalyze (String domainName) {
		this.domainName = domainName;
	}

	/**Gets a map of features for the domain, to be used for data-mining
	 * purposes
	 * @return mapping between a feature name and it's value
	 */
	public HashMap<String, Feature> getFeaturesMap() {
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

	public void setClassification(Classification classification) {
		this.classification = classification;
	}

	/**Gets a map of properties for the domain. The properties should
	 * be used for internal chain operations, or to pass data between the different chains.
	 * Unlike the features map, properties stored in this map aren't used for data-mining.
	 * @return mapping between a property name and it's value
	 */
	public HashMap<String, Object> getPropertiesMap() {
		return propertiesMap;
	}
	
	public void init() {
		ArrayList<Feature> allFeaturesFromChain = chain.getAllFeaturesFromChain();
		for (Feature feature : allFeaturesFromChain) {
			featuresMap.put(feature.getName(), feature);
		}
		
	}

}
