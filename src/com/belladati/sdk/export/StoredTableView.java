package com.belladati.sdk.export;

import java.util.Collection;

import com.belladati.sdk.filter.Filter;
import com.belladati.sdk.impl.LocalizationImpl;
import com.belladati.sdk.view.TableView;
import com.belladati.sdk.view.ViewType;

class StoredTableView extends StoredView implements TableView {

	private static final long serialVersionUID = 7322482533348762320L;

	private final Table content;

	StoredTableView(String id, String name, ViewType type, LocalizationImpl localization, Table content) {
		super(id, name, type, localization);
		this.content = content;
	}

	@Override
	public Table loadContent(Filter<?>... filters) {
		return content;
	}

	@Override
	public Table loadContent(Collection<Filter<?>> filters) {
		return content;
	}

}
