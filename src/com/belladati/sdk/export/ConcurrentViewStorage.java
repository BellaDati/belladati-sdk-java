package com.belladati.sdk.export;

import java.io.Serializable;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;

import com.belladati.sdk.view.TableView.Table;

/**
 * Stores views as self-contained objects.
 * <p />
 * This class is a specialization of {@link ViewStorage} that makes multiple
 * requests in parallel when loading table data to speed up loading.
 * <p />
 * All returned objects are {@link Serializable}.
 * 
 * @author Chris Hennigfeld
 */
public class ConcurrentViewStorage extends ViewStorage {
	@Override
	protected TableData readAllContent(final Table table) {
		ExecutorService service = Executors.newCachedThreadPool();

		try {
			Future<String[][]> left = service.submit(new Callable<String[][]>() {
				@Override
				public String[][] call() throws Exception {
					return readLeftHeaderContent(table);
				}
			});
			Future<String[][]> top = service.submit(new Callable<String[][]>() {
				@Override
				public String[][] call() throws Exception {
					return readTopHeaderContent(table);
				}
			});
			Future<String[][]> data = service.submit(new Callable<String[][]>() {
				@Override
				public String[][] call() throws Exception {
					return readDataContent(table);
				}
			});
			return new TableData(left.get(), top.get(), data.get());
		} catch (InterruptedException e) {} catch (ExecutionException e) {} finally {
			service.shutdown();
		}
		return new TableData(new String[0][0], new String[0][0], new String[0][0]);
	}
}
