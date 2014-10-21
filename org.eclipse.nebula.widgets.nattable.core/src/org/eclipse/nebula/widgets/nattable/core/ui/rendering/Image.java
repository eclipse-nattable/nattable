/*******************************************************************************
 * Copyright (c) 2015 Dirk Fauth.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Dirk Fauth <dirk.fauth@googlemail.com>   - initial API and implementation
 ******************************************************************************/
package org.eclipse.nebula.widgets.nattable.core.ui.rendering;

import java.net.URL;

import org.eclipse.nebula.widgets.nattable.util.GUIHelper;

/**
 * UI independent implementation for an image that should be rendered in a
 * NatTable. It simply transports the {@link URL} that points to the image. The
 * {@link GraphicsContext} implementation for a specific UI toolkit is
 * responsible to load the corresponding image object.
 * <p>
 * For optimization purposes regarding object creation and resource allocation,
 * the {@link GraphicsContext} will set the UI toolkit native image
 * representation to the {@link Image}. This way images only need to be loaded
 * once.
 * </p>
 */
public class Image {

    /**
     * the {@link URL} that points to the image file
     */
    public final URL url;
    /**
     * The native image object for the UI toolkit in use, that matches this
     * {@link Image} instance. Used to reduce the amount of UI toolkit image
     * objects and corresponding resource allocation on rendering.
     */
    private Object nativeImage;

    // TODO boolean flag need upscaling

    // TODO on init check for scaling

    /**
     * Create an {@link Image} that points to a NatTable internal image.
     *
     * @param name
     *            the name of the NatTable internal image
     */
    public Image(String name) {
        this(GUIHelper.getInternalImageUrl(name));
    }

    /**
     * Create an {@link Image} that points to an image located at the specified
     * {@link URL}.
     *
     * @param url
     *            the {@link URL} that points to the image file
     */
    public Image(URL url) {
        this.url = url;
    }

    /**
     * @return the native image object for the UI toolkit in use, that matches
     *         this {@link Image} instance.
     */
    public Object getNativeImage() {
        return this.nativeImage;
    }

    /**
     * Set the native image object for the UI toolkit in use, that matches this
     * {@link Image} instance. Will be called by the {@link GraphicsContext}
     * implementation the first time the corresponding native image object is
     * retrieved.
     *
     * @param nativeImage
     *            the native image object for the UI toolkit in use, that
     *            matches this {@link Image} instance.
     */
    public void setNativeImage(Object nativeImage) {
        this.nativeImage = nativeImage;
    }
}
