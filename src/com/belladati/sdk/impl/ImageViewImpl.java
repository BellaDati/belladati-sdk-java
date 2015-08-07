package com.belladati.sdk.impl;

import java.awt.image.BufferedImage;
import java.util.Collection;

import com.belladati.sdk.filter.Filter;
import com.belladati.sdk.view.ImageView;

class ImageViewImpl extends ViewImpl implements ImageView {

	ImageViewImpl(BellaDatiServiceImpl service, BufferedImage image) throws UnknownViewTypeException {
		super(service, image);
	}

	@Override
	public BufferedImage loadContent(Filter<?>... filters) {
		return (BufferedImage) super.loadContent(filters);
	}

	@Override
	public BufferedImage loadContent(Collection<Filter<?>> filters) {
		return (BufferedImage) super.loadContent(filters);
	}

	/**
	 * This class is a holder for an image
	 * 
	 * @author pavol.kovac
	 */
	static class ImageImpl implements Image {
		private final String id;
		private final BufferedImage image;

		/**
		 * Constructor accepting image ID and image itself
		 * 
		 * @param id of the image
		 * @param image itself
		 */
		public ImageImpl(String id, BufferedImage image) {
			this.id = id;
			this.image = image;
		}

		public String getId() {
			return id;
		}

		public BufferedImage getImage() {
			return image;
		}

	}
}
