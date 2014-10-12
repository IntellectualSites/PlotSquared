package com.intellectualcrafters.plot;

import org.apache.commons.lang.StringUtils;
import org.bukkit.ChatColor;

public class Flag {
	private AbstractFlag key;
	private String value;

	/**
	 * Flag object used to store basic information for a Plot. Flags are a
	 * key/value pair. For a flag to be usable by a player, you need to register
	 * it with PlotSquared.
	 * 
	 * @param key
	 *            AbstractFlag
	 * @param value
	 *            Value must be alphanumerical (can have spaces) and be <= 48
	 *            characters
	 * @throws IllegalArgumentException
	 *             if you provide inadequate inputs
	 */
	public Flag(AbstractFlag key, String value) {
		if (!StringUtils.isAlphanumericSpace(ChatColor.stripColor(value))) {
			throw new IllegalArgumentException("Flag must be alphanumerical");
		}
		if (value.length() > 48) {
			throw new IllegalArgumentException("Value must be <= 48 characters");
		}
		this.key = key;
		this.value = value;
	}

	/**
	 * Get the AbstractFlag used in creating the flag
	 * 
	 * @return AbstractFlag
	 */
	public AbstractFlag getAbstractFlag() {
		return this.key;
	}

	/**
	 * Get the key for the AbstractFlag
	 * 
	 * @return String
	 */
	public String getKey() {
		return this.key.getKey();
	}

	/**
	 * Get the value
	 * 
	 * @return String
	 */
	public String getValue() {
		return this.value;
	}

	@Override
	public String toString() {
		if (this.value.equals("")) {
			return this.key.getKey();
		}
		return this.key + ":" + this.value;
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj) {
			return true;
		}
		if (obj == null) {
			return false;
		}
		if (getClass() != obj.getClass()) {
			return false;
		}
		Flag other = (Flag) obj;
		return (this.key.getKey().equals(other.key.getKey()) && this.value.equals(other.value));
	}

	@Override
	public int hashCode() {
		return this.key.getKey().hashCode();
	}
}
