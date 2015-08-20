package com.belladati.sdk.impl;

import com.belladati.sdk.util.IdElement;

/**
 * Simple implementation of an {@link IdElement} for testing.
 * 
 * @author Chris Hennigfeld
 */
public class Item implements IdElement {
	private final String id;

	public Item(String id) {
		if (id == null) {
			throw new NullPointerException("ID is null");
		}
		this.id = id;
	}

	@Override
	public String getId() {
		return id;
	}

	@Override
	public boolean equals(Object obj) {
		if (obj instanceof Item) {
			return id.equals(((Item) obj).id);
		}
		return false;
	}

	@Override
	public int hashCode() {
		return id.hashCode();
	}

	@Override
	public String toString() {
		return "Item(id: " + id + ")";
	}
}