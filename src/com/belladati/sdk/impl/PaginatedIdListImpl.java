package com.belladati.sdk.impl;

import com.belladati.sdk.util.IdElement;
import com.belladati.sdk.util.PaginatedIdList;

abstract class PaginatedIdListImpl<T extends IdElement> extends PaginatedListImpl<T> implements PaginatedIdList<T> {

	public PaginatedIdListImpl(BellaDatiServiceImpl service, String relativeUrl, String field) {
		super(service, relativeUrl, field);
	}

	@Override
	public boolean contains(String id) {
		for (T item : currentData) {
			if (id.equals(item.getId())) {
				return true;
			}
		}
		return false;
	}

	@Override
	public int indexOf(String id) {
		for (int dataIndex = 0; dataIndex < currentData.size(); dataIndex++) {
			T item = currentData.get(dataIndex);
			if (id.equals(item.getId())) {
				return getFirstLoadedIndex() + dataIndex;
			}
		}
		return -1;
	}
}
