package il.ac.mta.bi.dmd.common;

import il.ac.mta.bi.dmd.common.DomainToAnalyze.Classification;
import il.ac.mta.bi.dmd.common.ProcessingChain.chainStatus;

import java.io.Serializable;
import java.util.ArrayList;

public class domainResult implements Serializable {

	private String domainName;
	private chainStatus chainStat;
	private String lastChain;
	private ArrayList<String> arrFeatures;
	private Classification classification = Classification.UNKNOWN;
	
	private double maliciousChance;
	private double benignChance;
	public domainResult(DomainToAnalyze domain)
	{
		domainName = domain.getDomainName();
		chainStat = domain.getChain().getStatus();
		lastChain = domain.getChain().getCurrChain().getChainName();

		arrFeatures = new ArrayList<String>();
		
		for (Feature feature : domain.getFeaturesMap().values()) {
			arrFeatures.add(feature.getName() + ": " + feature.getValue());
		}
		
		maliciousChance = domain.getMaliciousChance();
		benignChance = domain.getBenignChance();
		classification = domain.getClassification();
	}

	/**Gets the domain name
	 * @return the domain name
	 */
	public String getDomainName() {
		return domainName;
	}

	/**Gets the processing chain Status
	 * @return the processing chain Status
	 */
	public chainStatus getRunStatus() {
		return chainStat;
	}
	
	public Classification getClassification() {
		return classification;
	}

	public double getMaliciousChance() {
		return maliciousChance;
	}

	public double getBenignChance() {
		return benignChance;
	}
	
	
	@Override
	public String toString() {
		StringBuilder sb = new StringBuilder();
		sb.append(domainName + " classification:" + '\n');
		sb.append("chain status: " + chainStat + '\n');
		sb.append("last chain: " + lastChain+ '\n');
		sb.append('\n');
		
		for (int i = 0; i < arrFeatures.size(); i++) {
			sb.append(arrFeatures.get(i)  + '\n');
		}
		
		sb.append('\n');
		
		sb.append("malicious chance=" + maliciousChance + '\n');
		sb.append("benign chance=" + benignChance + '\n');
		sb.append("domain is " + classification + '\n');
		
		return sb.toString();
	}
}