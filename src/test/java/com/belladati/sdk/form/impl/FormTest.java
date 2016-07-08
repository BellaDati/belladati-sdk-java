package com.belladati.sdk.form.impl;

import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertFalse;
import static org.testng.Assert.assertNotEquals;
import static org.testng.Assert.assertTrue;

import java.util.ArrayList;
import java.util.List;

import org.testng.annotations.Test;

import com.belladati.sdk.form.Form;
import com.belladati.sdk.form.FormElement;
import com.belladati.sdk.test.SDKTest;
import com.belladati.sdk.util.CachedList;
import com.fasterxml.jackson.databind.JsonNode;

@Test
public class FormTest extends SDKTest {

	private final String id = "123";
	private final String name = "form name";
	private final String recordTimestamp = "true";

	private final String baseUri = "/api/import/forms";
	private final String formUri = baseUri + "/" + id;

	private final String elemId = "ABCD1234";
	private final String elemName = "element name";
	private final String elemType = "textfield";

	public void loadForms_withoutElements() {
		CachedList<Form> list = service.getImportForms();

		server.registerPaginatedItem(baseUri, "importForms", builder.buildFormNode(id, name, recordTimestamp));

		list.load();
		server.assertRequestUris(baseUri);
		assertEquals(list.get().size(), 1);

		Form form = list.get().get(0);
		assertEquals(form.getId(), id);
		assertEquals(form.getName(), name);
		assertEquals(form.getRecordTimestamp() + "", recordTimestamp);
		assertEquals(form.getElements().size(), 0);
		assertEquals(form.toString(), name);
	}

	public void loadForm_withoutElements() {
		server.register(formUri, builder.buildFormNode(id, name, recordTimestamp).toString());

		Form form = service.loadImportForm(id);
		server.assertRequestUris(formUri);

		assertEquals(form.getId(), id);
		assertEquals(form.getName(), name);
		assertEquals(form.getRecordTimestamp() + "", recordTimestamp);
		assertEquals(form.getElements().size(), 0);
		assertEquals(form.toString(), name);
	}

	public void loadForms_withElements() {
		CachedList<Form> list = service.getImportForms();

		server.registerPaginatedItem(baseUri, "importForms", builder.buildFormNode(id, name, recordTimestamp, testElements()));

		list.load();
		server.assertRequestUris(baseUri);
		assertEquals(list.get().size(), 1);

		Form form = list.get().get(0);
		assertEquals(form.getId(), id);
		assertEquals(form.getName(), name);
		assertEquals(form.getRecordTimestamp() + "", recordTimestamp);
		assertEquals(form.toString(), name);
		assertEquals(form.getElements().size(), 6);
		assertElements(form.getElements());
	}

	public void loadForm_withElements() {
		server.register(formUri, builder.buildFormNode(id, name, recordTimestamp, testElements()).toString());

		Form form = service.loadImportForm(id);
		server.assertRequestUris(formUri);

		assertEquals(form.getId(), id);
		assertEquals(form.getName(), name);
		assertEquals(form.getRecordTimestamp() + "", recordTimestamp);
		assertEquals(form.toString(), name);
		assertEquals(form.getElements().size(), 6);
		assertElements(form.getElements());
	}

	public void formEquality() {
		Form o1 = new FormImpl(builder.buildFormNode(id, name, recordTimestamp));
		Form o2 = new FormImpl(builder.buildFormNode(id, "", ""));
		Form o3 = new FormImpl(builder.buildFormNode("otherId", name, recordTimestamp));

		assertEquals(o1, o2);
		assertEquals(o1.hashCode(), o2.hashCode());

		assertFalse(o1.equals(new Object()));
		assertNotEquals(o1, o3);
	}

	public void formElementEquality() {
		FormElement o1 = new FormElementImpl(builder.buildFormElementNode(elemId, elemName, elemType, null));
		FormElement o2 = new FormElementImpl(builder.buildFormElementNode(elemId, "", "", null));
		FormElement o3 = new FormElementImpl(builder.buildFormElementNode("otherId", elemName, elemType, null));

		assertEquals(o1, o2);
		assertEquals(o1.hashCode(), o2.hashCode());

		assertFalse(o1.equals(new Object()));
		assertNotEquals(o1, o3);
	}

	private List<JsonNode> testElements() {
		List<JsonNode> elements = new ArrayList<>();

		elements.add(builder.buildFormElementNode(elemId, elemName, elemType, null));
		elements.add(builder.buildFormElementNode("e01", "e01 name", "username", null));
		elements.add(builder.buildFormElementNode("e02", "e02 name", "checkbox", null));
		elements.add(builder.buildFormElementNode("e03", "e03 name", "timestamp", null));
		elements.add(builder.buildFormElementNode("e04", "e04 name", "datefield", "true"));
		elements.add(builder.buildFormElementNode("e05", "e05 name", "select", null, "item1", "item2", "item3"));

		return elements;
	}

	private void assertElements(List<FormElement> elements) {
		assertElement(elements.get(0), elemId, elemName, elemType, null);
		assertElement(elements.get(1), "e01", "e01 name", "username", null);
		assertElement(elements.get(2), "e02", "e02 name", "checkbox", null);
		assertElement(elements.get(3), "e03", "e03 name", "timestamp", null);
		assertElement(elements.get(4), "e04", "e04 name", "datefield", true);
		assertElement(elements.get(5), "e05", "e05 name", "select", null, "item1", "item2", "item3");
	}

	private void assertElement(FormElement element, String id, String name, String type, Boolean mapToDateColumn,
		String... items) {
		assertEquals(element.getId(), id);
		assertEquals(element.getName(), name);
		assertEquals(element.getType().getJsonValue(), type);
		assertEquals(element.getMapToDateColumn(), mapToDateColumn);

		if (items == null) {
			assertEquals(element.getItems().size(), 0);
		} else {
			assertEquals(element.getItems().size(), items.length);
			for (String item : items) {
				assertTrue(element.getItems().contains(item), "Element " + id + " is missing item: " + item);
			}
		}
	}

}
