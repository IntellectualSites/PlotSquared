package com.intellectualcrafters.plot;

import org.apache.commons.lang.StringUtils;

/**
 * Created by Citymonstret on 2014-09-23.
 */
public class AbstractFlag {

	private final String key;

	/**
	 * AbstractFlag is a parameter used in creating a new Flag
	 * 
	 * @param key
	 *            The key must be alphabetical characters and <= 16 characters
	 *            in length
	 */
	public AbstractFlag(String key) {
		if (!StringUtils.isAlpha(key.replaceAll("_", "").replaceAll("-", ""))) {
			throw new IllegalArgumentException("Flag must be alphabetic characters");
		}
		if (key.length() > 16) {
			throw new IllegalArgumentException("Key must be <= 16 characters");
		}
		this.key = key.toLowerCase();
	}
	
	public String parseValue(String value) {
	    return value;
	}
	
	public String getValueDesc() {
	    return "Flag value must be alphanumeric";
	}
	

	/**
	 * AbstractFlag key
	 * 
	 * @return String
	 */
	public String getKey() {
		return this.key;
	}

	@Override
	public String toString() {
		return this.key;
	}
	
	@Override
	public boolean equals(Object other){
	    if (other == null) return false;
	    if (other == this) return true;
	    if (!(other instanceof AbstractFlag)) return false;
	    AbstractFlag otherObj = (AbstractFlag)other;
	    return (otherObj.key.equals(this.key));
	}

}
