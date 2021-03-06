package il.ac.mta.bi.dmd.common;

import weka.core.Attribute;

public class Feature {
	
	private String name;
	private Object value;
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
	public Object getValue() {
		return value;
	}

	/**Sets the feature string value
	 * @param value the string value of the feature
	 */
	public void setValue(Object value) {
		this.value = value;
	}

	/**Returns the feature type, as stored in the value param
	 */
	public FeatureType getType() {
		return type;
	}
	
	/**Returns the feature as an Arff attribute for data-mining.
	 * As most classifiers are suitable to work with numeric values,
	 * the attribute is returned in num type
	 */
	public Attribute toAttribute() {
		Attribute attr = null;
		
		switch (type) {
		case INTEGER:
		case NOMINAL:
		case STRING:
		default:
			attr =  new Attribute(name);
			break;
		}
		return attr;
	}
}
