package com.belladati.sdk.export;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;

import com.belladati.sdk.dashboard.Dashboard;
import com.belladati.sdk.dashboard.Dashlet;
import com.belladati.sdk.report.Report;
import com.belladati.sdk.view.View;

/**
 * Stores reports and dashboards as self-contained objects.
 * <p />
 * This class is a specialization of {@link PageStorage} that makes multiple
 * requests in parallel when loading view or dashlet data to speed up loading.
 * <p />
 * All returned objects are {@link Serializable}.
 * 
 * @author Chris Hennigfeld
 */
public class ConcurrentPageStorage extends PageStorage {

	public ConcurrentPageStorage() {
		super(new ConcurrentViewStorage());
	}

	public ConcurrentPageStorage(ViewStorage viewStorage) {
		super(viewStorage);
	}

	@Override
	protected List<View> storeViews(Report report) {
		ExecutorService service = Executors.newCachedThreadPool();

		try {
			List<Future<View>> futures = new ArrayList<Future<View>>();
			for (final View view : report.getViews()) {
				futures.add(service.submit(new Callable<View>() {
					@Override
					public View call() throws Exception {
						return viewStorage.storeView(view);
					}
				}));
			}
			List<View> stored = new ArrayList<View>();
			for (Future<View> future : futures) {
				stored.add(future.get());
			}
			return stored;
		} catch (InterruptedException e) {} catch (ExecutionException e) {} finally {
			service.shutdown();
		}
		return Collections.emptyList();
	}

	@Override
	protected List<Dashlet> storeDashlets(Dashboard dashboard) {
		ExecutorService service = Executors.newCachedThreadPool();

		try {
			List<Future<Dashlet>> futures = new ArrayList<Future<Dashlet>>();
			for (final Dashlet dashlet : dashboard.getDashlets()) {
				futures.add(service.submit(new Callable<Dashlet>() {
					@Override
					public Dashlet call() throws Exception {
						return storeDashlet(dashlet);
					}
				}));
			}
			List<Dashlet> stored = new ArrayList<Dashlet>();
			for (Future<Dashlet> future : futures) {
				stored.add(future.get());
			}
			return stored;
		} catch (InterruptedException e) {} catch (ExecutionException e) {} finally {
			service.shutdown();
		}
		return Collections.emptyList();
	}
}
