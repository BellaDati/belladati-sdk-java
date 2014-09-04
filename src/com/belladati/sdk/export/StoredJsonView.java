package com.belladati.sdk.export;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.lang.reflect.Field;
import java.util.Collection;

import com.belladati.sdk.exception.InternalConfigurationException;
import com.belladati.sdk.filter.Filter;
import com.belladati.sdk.impl.LocalizationImpl;
import com.belladati.sdk.view.JsonView;
import com.belladati.sdk.view.ViewType;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

class StoredJsonView extends StoredView implements JsonView {

	private static final long serialVersionUID = 8473300782689471129L;

	private final transient JsonNode content;

	StoredJsonView(String id, String name, ViewType type, LocalizationImpl localization, JsonNode content) {
		super(id, name, type, localization);
		this.content = content;
	}

	@Override
	public JsonNode loadContent(Filter<?>... filters) {
		return content;
	}

	@Override
	public JsonNode loadContent(Collection<Filter<?>> filters) {
		return loadContent();
	}

	/** Deserialization. Sets up the content node. */
	private void readObject(ObjectInputStream in) throws IOException, ClassNotFoundException {
		in.defaultReadObject();
		try {
			Field content = getClass().getDeclaredField("content");
			content.setAccessible(true);
			String storedValue = (String) in.readObject();
			content.set(this, storedValue != null ? new ObjectMapper().readTree(storedValue) : null);
		} catch (NoSuchFieldException e) {
			throw new InternalConfigurationException("Failed to set service fields", e);
		} catch (IllegalAccessException e) {
			throw new InternalConfigurationException("Failed to set service fields", e);
		} catch (SecurityException e) {
			throw new InternalConfigurationException("Failed to set service fields", e);
		} catch (IllegalArgumentException e) {
			throw new InternalConfigurationException("Failed to set service fields", e);
		}
	}

	/** Serialization. Writes the content node. */
	private void writeObject(ObjectOutputStream out) throws IOException, ClassNotFoundException {
		out.defaultWriteObject();
		out.writeObject(content != null ? content.toString() : null);
	}
}
