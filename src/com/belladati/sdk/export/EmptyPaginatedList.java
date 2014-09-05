package com.belladati.sdk.export;

import java.util.Collections;
import java.util.Iterator;
import java.util.List;

import com.belladati.sdk.util.PaginatedList;

/**
 * An empty {@link PaginatedList} implementation. Doesn't load anything and
 * can't hold any data.
 * 
 * @author Chris Hennigfeld
 */
class EmptyPaginatedList<T> implements PaginatedList<T> {

	private final List<T> list = Collections.emptyList();

	@Override
	public Iterator<T> iterator() {
		return list.iterator();
	}

	@Override
	public PaginatedList<T> load() {
		return this;
	}

	@Override
	public PaginatedList<T> load(int size) throws IllegalArgumentException {
		return this;
	}

	@Override
	public PaginatedList<T> load(int page, int size) throws IllegalArgumentException {
		return this;
	}

	@Override
	public PaginatedList<T> loadNext() {
		return this;
	}

	@Override
	public boolean isLoaded() {
		return true;
	}

	@Override
	public boolean hasNextPage() {
		return false;
	}

	@Override
	public int getFirstLoadedPage() {
		return 0;
	}

	@Override
	public int getLastLoadedPage() {
		return 0;
	}

	@Override
	public int getFirstLoadedIndex() {
		return 0;
	}

	@Override
	public int getLastLoadedIndex() {
		return 0;
	}

	@Override
	public int getPageSize() {
		return 1;
	}

	@Override
	public boolean contains(T element) {
		return false;
	}

	@Override
	public T get(int index) throws IndexOutOfBoundsException {
		return null;
	}

	@Override
	public int indexOf(T element) {
		return -1;
	}

	@Override
	public boolean isEmpty() {
		return true;
	}

	@Override
	public int size() {
		return 0;
	}

	@Override
	public List<T> toList() {
		return list;
	}

}
