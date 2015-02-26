package il.ac.mta.bi.dmd.common;

import weka.core.Attribute;
import weka.core.FastVector;

public class Feature {
	
	private String name;
	private String value;
	private Object valueFeature;
	private FeatureType type;
	
	public enum FeatureType {
		INTEGER, STRING, NOMINAL
	}
	
	public Feature (String name, FeatureType type) {
		this.name = name;
		this.type = type;
	}

	/**Returns the feature name
	 */
	public String getName() {
		return name;
	}

	/**Returns the string value of the feature
	 */
	public String getValue() {
		return value;
	}

	/**Sets the feature string value
	 * @param value the string value of the feature
	 */
	public void setValue(String value) {
		this.value = value;
	}

	/**Returns the value of the feature as returned by the runner
	 */
	public Object getValueFeature() {
		return valueFeature;
	}

	/**Returns the feature type, as stored in the value param
	 */
	public FeatureType getType() {
		return type;
	}
	
	/**Returns the feature as an Arff attribute for data-mining
	 */
	public Attribute toAttribute() {
		Attribute attr = null;
		
		switch (type) {
		case INTEGER:
			attr =  new Attribute(name);
			break;
		case NOMINAL: /* not supported yet */
			break;
		case STRING:
		default:
			attr =  new Attribute(name, (FastVector) null);
			break;
		}
		return attr;
	}
}
