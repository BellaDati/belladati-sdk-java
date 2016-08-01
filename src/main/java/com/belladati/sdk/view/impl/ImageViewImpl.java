package com.belladati.sdk.view.impl;

import java.awt.image.BufferedImage;
import java.util.Collection;

import com.belladati.sdk.exception.impl.UnknownViewTypeException;
import com.belladati.sdk.filter.Filter;
import com.belladati.sdk.impl.BellaDatiServiceImpl;
import com.belladati.sdk.view.ImageView;
import com.fasterxml.jackson.databind.JsonNode;

public class ImageViewImpl extends ViewImpl implements ImageView {

	public ImageViewImpl(BellaDatiServiceImpl service, JsonNode node) throws UnknownViewTypeException {
		super(service, node);
	}

	@Override
	public Image loadContent(Filter<?>... filters) {
		return (Image) super.loadContent(filters);
	}

	@Override
	public Image loadContent(Collection<Filter<?>> filters) {
		return (Image) super.loadContent(filters);
	}

	/**
	 * This class is a holder for an image
	 * 
	 * @author Pavol Kovac
	 */
	public static class ImageImpl implements Image {

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

		@Override
		public String getId() {
			return id;
		}

		@Override
		public BufferedImage getImage() {
			return image;
		}

		@Override
		public String toString() {
			return "Image(id: " + id + ")";
		}

		@Override
		public boolean equals(Object obj) {
			if (obj instanceof ImageImpl) {
				return id.equals(((ImageImpl) obj).id);
			}
			return false;
		}

		@Override
		public int hashCode() {
			return id.hashCode();
		}

	}

}
