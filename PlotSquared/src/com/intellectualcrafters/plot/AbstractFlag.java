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
		if (!StringUtils.isAlpha(key)) {
			throw new IllegalArgumentException("Flag must be alphabetic characters");
		}
		if (key.length() > 16) {
			throw new IllegalArgumentException("Key must be <= 16 characters");
		}
		this.key = key.toLowerCase();
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

}
