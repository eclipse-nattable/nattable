/*******************************************************************************
 * Copyright (c) 2012, 2016 Original authors and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Original authors and others - initial API and implementation
 *     Dirk Fauth <dirk.fauth@googlemail.com> - Added image scaling
 ******************************************************************************/
package org.eclipse.nebula.widgets.nattable.util;

import java.io.IOException;
import java.net.URL;
import java.net.URLConnection;
import java.util.concurrent.atomic.AtomicLong;

import org.eclipse.jface.resource.ImageDescriptor;
import org.eclipse.jface.resource.ImageRegistry;
import org.eclipse.jface.resource.JFaceResources;
import org.eclipse.swt.SWT;
import org.eclipse.swt.graphics.Color;
import org.eclipse.swt.graphics.Font;
import org.eclipse.swt.graphics.FontData;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.graphics.ImageData;
import org.eclipse.swt.graphics.RGB;
import org.eclipse.swt.widgets.Display;

public class GUIHelper {

    private static final String KEY_PREFIX = GUIHelper.class.getCanonicalName() + "."; //$NON-NLS-1$

    // Color

    public static final Color COLOR_GRAY = Display.getDefault().getSystemColor(SWT.COLOR_GRAY);
    public static final Color COLOR_WHITE = Display.getDefault().getSystemColor(SWT.COLOR_WHITE);
    public static final Color COLOR_DARK_GRAY = Display.getDefault().getSystemColor(SWT.COLOR_DARK_GRAY);
    public static final Color COLOR_BLACK = Display.getDefault().getSystemColor(SWT.COLOR_BLACK);
    public static final Color COLOR_BLUE = Display.getDefault().getSystemColor(SWT.COLOR_BLUE);
    public static final Color COLOR_RED = Display.getDefault().getSystemColor(SWT.COLOR_RED);
    public static final Color COLOR_YELLOW = Display.getDefault().getSystemColor(SWT.COLOR_YELLOW);
    public static final Color COLOR_GREEN = Display.getDefault().getSystemColor(SWT.COLOR_GREEN);

    public static final Color COLOR_LIST_BACKGROUND = Display.getDefault().getSystemColor(SWT.COLOR_LIST_BACKGROUND);
    public static final Color COLOR_LIST_FOREGROUND = Display.getDefault().getSystemColor(SWT.COLOR_LIST_FOREGROUND);
    public static final Color COLOR_LIST_SELECTION = Display.getDefault().getSystemColor(SWT.COLOR_LIST_SELECTION);
    public static final Color COLOR_LIST_SELECTION_TEXT = Display.getDefault().getSystemColor(SWT.COLOR_LIST_SELECTION_TEXT);

    public static final Color COLOR_WIDGET_BACKGROUND = Display.getDefault().getSystemColor(SWT.COLOR_WIDGET_BACKGROUND);
    public static final Color COLOR_WIDGET_FOREGROUND = Display.getDefault().getSystemColor(SWT.COLOR_WIDGET_FOREGROUND);
    public static final Color COLOR_TITLE_INACTIVE_BACKGROUND = Display.getDefault().getSystemColor(SWT.COLOR_TITLE_INACTIVE_BACKGROUND);
    public static final Color COLOR_WIDGET_BORDER = Display.getDefault().getSystemColor(SWT.COLOR_WIDGET_BORDER);
    public static final Color COLOR_WIDGET_DARK_SHADOW = Display.getDefault().getSystemColor(SWT.COLOR_WIDGET_DARK_SHADOW);
    public static final Color COLOR_WIDGET_LIGHT_SHADOW = Display.getDefault().getSystemColor(SWT.COLOR_WIDGET_LIGHT_SHADOW);
    public static final Color COLOR_WIDGET_NORMAL_SHADOW = Display.getDefault().getSystemColor(SWT.COLOR_WIDGET_NORMAL_SHADOW);
    public static final Color COLOR_WIDGET_HIGHLIGHT_SHADOW = Display.getDefault().getSystemColor(SWT.COLOR_WIDGET_HIGHLIGHT_SHADOW);

    public static Color getColor(RGB rgb) {
        return getColor(rgb.red, rgb.green, rgb.blue);
    }

    public static Color getColor(int red, int green, int blue) {
        String key = getColorKey(red, green, blue);
        if (JFaceResources.getColorRegistry().hasValueFor(key)) {
            return JFaceResources.getColorRegistry().get(key);
        } else {
            JFaceResources.getColorRegistry().put(key, new RGB(red, green, blue));
            return getColor(key);
        }
    }

    public static Color getColor(String key) {
        return JFaceResources.getColorRegistry().get(key);
    }

    private static String getColorKey(int red, int green, int blue) {
        return KEY_PREFIX + "_COLOR_" + red + "_" + green + "_" + blue; //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
    }

    // Font

    public static final Font DEFAULT_FONT = Display.getDefault().getSystemFont();

    public static final int DEFAULT_RESIZE_HANDLE_SIZE = 4;
    public static final int DEFAULT_MIN_DISPLAY_SIZE = 5;
    public static final int DEFAULT_ANTIALIAS = SWT.DEFAULT;;
    public static final int DEFAULT_TEXT_ANTIALIAS = SWT.DEFAULT;;

    public static Font getFont(FontData... fontDatas) {
        StringBuilder keyBuilder = new StringBuilder();
        for (FontData fontData : fontDatas) {
            keyBuilder.append(fontData.toString());
        }
        String key = keyBuilder.toString();

        if (JFaceResources.getFontRegistry().hasValueFor(key)) {
            return JFaceResources.getFont(key);
        } else {
            JFaceResources.getFontRegistry().put(key, fontDatas);
            return JFaceResources.getFont(key);
        }
    }

    public static Font getFont(String key) {
        return JFaceResources.getFont(key);
    }

    // Image

    private static final String[] IMAGE_DIRS = new String[] { "org/eclipse/nebula/widgets/nattable/images/", "" }; //$NON-NLS-1$ //$NON-NLS-2$
    private static final String[] IMAGE_EXTENSIONS = new String[] { ".png", ".gif" }; //$NON-NLS-1$ //$NON-NLS-2$

    /**
     * This method extracts the base filename out of the given {@link URL} and
     * uses it as key to search for the {@link Image} in the
     * {@link ImageRegistry}. If the {@link Image} is not yet present it will
     * create the {@link Image} instance and register it automatically. On
     * creating and registering it will respect dpi scaling.
     *
     * @param url
     *            The {@link URL} of the image for initial loading.
     * @return The {@link Image} representation of an image external to
     *         NatTable.
     *
     * @see #getImageByURL(String, URL)
     */
    public static Image getImageByURL(URL url) {
        String basename = url.toString();
        basename = basename.substring(basename.lastIndexOf('/') + 1, basename.lastIndexOf('.'));
        return getImageByURL(basename, url);
    }

    /**
     * This method returns the {@link Image} that is registered for the given
     * key. If there is no {@link Image} registered for that key already, it
     * will create and register the {@link Image} that can be loaded by the
     * given {@link URL}.
     * <p>
     * This method returns an {@link Image} that matches the dpi settings. If
     * the dpi of an axis is bigger than 96 it will search for an image for the
     * bigger dpi. For this the filenames need to carry the dpi information.
     * </p>
     * <p>
     * For example if you request <i>checkbox.png</i> this method will search
     * for a scaled version relative to the requested image. The following will
     * give an example on the scaled files:
     * </p>
     * <ul>
     * <li>checkbox.png</li>
     * <li>checkbox_120_120.png</li>
     * <li>checkbox_128_128.png</li>
     * <li>checkbox_144_144.png</li>
     * <li>checkbox_192_192.png</li>
     * <li>checkbox_288_288.png</li>
     * </ul>
     * <p>
     * If the matching scaled version is not found, it will automatically
     * upscale the base image.
     * </p>
     * <p>
     * Note: If you want to get the image later again you can directly use
     * <code>JFaceResources.getImage(key)</code>.
     * </p>
     *
     * @param key
     *            The key under which the resource is registered in the image
     *            registry.
     * @param url
     *            The {@link URL} of the image for initial loading.
     * @return The {@link Image} representation of an image external to
     *         NatTable.
     */
    public static Image getImageByURL(String key, URL url) {
        Image image = JFaceResources.getImage(key);
        if (image == null) {
            if (needScaling()) {
                // modify url to contain scaling information in filename
                // create the matching URL for the scaled image
                String urlString = url.toString();
                int extIndex = urlString.lastIndexOf('.');
                String ext = urlString.substring(extIndex, urlString.length());
                String base = urlString.substring(0, extIndex);

                // check if there is a upscaled image available
                try {
                    URL scaleURL = new URL(base + getScalingImageSuffix() + ext);
                    URLConnection con = scaleURL.openConnection();
                    con.connect();
                    // as the connection could be established, the file exists
                    // this check is working in plain SWT aswell as in the OSGi
                    // context
                    JFaceResources.getImageRegistry().put(key, ImageDescriptor.createFromURL(scaleURL));
                } catch (IOException e) {
                    // if there is no upscaled image available, we upscale
                    // ourself
                    ImageData imageData = ImageDescriptor.createFromURL(url).getImageData();
                    imageData = imageData.scaledTo(
                            convertHorizontalPixelToDpi(imageData.width),
                            convertVerticalPixelToDpi(imageData.height));
                    JFaceResources.getImageRegistry().put(key, new Image(Display.getDefault(), imageData));
                }
            } else {
                JFaceResources.getImageRegistry().put(key, ImageDescriptor.createFromURL(url));
            }
            image = JFaceResources.getImage(key);
        }
        return image;
    }

    /**
     * Returns the {@link Image} representation of a NatTable internal image
     * resource.
     * <p>
     * For upscaling this method checks whether there is a upscaled version of
     * the image available, otherwise it will upscale the existing image and
     * store that in the registry for further use.
     * </p>
     *
     * @param imageName
     *            The filename of the image (without extension).
     * @return The {@link Image} representation of the internal NatTable image
     *         resource or <code>null</code> if there is no image found for the
     *         given name at the internal image resource location.
     */
    public static Image getImage(String imageName) {
        Image image = JFaceResources.getImage(imageName);
        if (image == null) {
            URL imageUrl = getInternalImageUrl(imageName, needScaling());
            if (imageUrl != null) {
                ImageDescriptor imageDescriptor = ImageDescriptor.createFromURL(imageUrl);

                if (needScaling() && !imageUrl.getFile().contains(getScalingImageSuffix())) {
                    // we need to upscale the image but we have no scaled
                    // version, therefore we manually perform an upscale
                    // it won't look nice but at least it is upscaled
                    ImageData imageData = imageDescriptor.getImageData();
                    imageData = imageData.scaledTo(
                            convertHorizontalPixelToDpi(imageData.width),
                            convertVerticalPixelToDpi(imageData.height));
                    JFaceResources.getImageRegistry().put(imageName, new Image(Display.getDefault(), imageData));
                } else {
                    JFaceResources.getImageRegistry().put(imageName, imageDescriptor.createImage());
                }

                image = JFaceResources.getImage(imageName);
            }
        }
        return image;
    }

    /**
     * Returns the {@link ImageDescriptor} for a NatTable internal image
     * resource.
     *
     * @param imageName
     *            The filename of the image (without extension).
     * @return The {@link ImageDescriptor} of the internal NatTable image
     *         resource or <code>null</code> if there is no image found for the
     *         given name at the internal image resource location.
     */
    public static ImageDescriptor getImageDescriptor(String imageName) {
        ImageDescriptor imageDescriptor = null;
        URL imageUrl = getInternalImageUrl(imageName, needScaling());
        if (imageUrl != null) {
            imageDescriptor = ImageDescriptor.createFromURL(imageUrl);
        }
        return imageDescriptor;
    }

    /**
     * Searches for the image with the given filename in the NatTable internal
     * image resource folder with file extensions <i>.png</i> and <i>.gif</i>.
     *
     * @param imageName
     *            The filename of the image (without extension)
     * @return The URL of the internal NatTable image or <code>null</code> if
     *         there is no image found for the given name at the internal image
     *         resource location.
     *
     * @since 1.5
     */
    public static URL getInternalImageUrl(String imageName) {
        return getInternalImageUrl(imageName, false);
    }

    /**
     * Searches for the image with the given filename in the NatTable internal
     * image resource folder with file extensions <i>.png</i> and <i>.gif</i>.
     *
     * @param imageName
     *            The filename of the image (without extension)
     * @param needScaling
     *            <code>true</code> in case the scaled version is requested,
     *            <code>false</code> if the original version is requested
     * @return The URL of the internal NatTable image or <code>null</code> if
     *         there is no image found for the given name at the internal image
     *         resource location.
     * 
     * @since 1.5
     */
    public static URL getInternalImageUrl(String imageName, boolean needScaling) {
        for (String dir : IMAGE_DIRS) {
            for (String ext : IMAGE_EXTENSIONS) {
                // add search for scaled image
                // e.g. imageName = checkbox -->
                // org/eclipse/nebula/widgets/nattable/images/checkbox_128_128.png
                URL url = null;
                if (needScaling) {
                    url = GUIHelper.class.getClassLoader().getResource(
                            dir + imageName + getScalingImageSuffix() + ext);
                }

                // no scaling needed or no already scaled image found
                // search for the image without scaling extension
                if (url == null) {
                    url = GUIHelper.class.getClassLoader().getResource(dir + imageName + ext);
                }

                if (url != null) {
                    return url;
                }
            }
        }

        return null;
    }

    /**
     * <b>WARNING:</b> Do not use this method as it might cause resource
     * handling issues!
     *
     * @deprecated This method does not work correctly since it uses
     *             {@link ImageData#toString()}
     */
    @Deprecated
    public static Image getImage(ImageData data) {
        if (JFaceResources.getImage(data.toString()) == null) {
            JFaceResources.getImageRegistry().put(data.toString(), ImageDescriptor.createFromImageData(data));
        }
        return JFaceResources.getImage(data.toString());
    }

    // Sequence

    private static final AtomicLong atomicLong = new AtomicLong(0);

    public static String getSequenceNumber() {
        long id = atomicLong.addAndGet(1);
        return String.valueOf(id);
    }

    /**
     * Blend the two colour values returning a value that is halfway between
     * them.
     *
     * @param val1
     *            the first value
     * @param val2
     *            the second value
     * @return the blended colour
     */
    public static RGB blend(final RGB val1, final RGB val2) {
        final int red = blend(val1.red, val2.red);
        final int green = blend(val1.green, val2.green);
        final int blue = blend(val1.blue, val2.blue);
        return new RGB(red, green, blue);
    }

    /**
     * Blend the two colour values returning a value that is halfway between
     * them.
     *
     * @param temp1
     *            the first value
     * @param temp2
     *            the second value
     * @return the blended int value
     */
    private static int blend(final int temp1, final int temp2) {
        return (Math.abs(temp1 - temp2) / 2) + Math.min(temp1, temp2);
    }

    // DPI scaling

    /**
     * Returns the factor for scaling calculations of pixels regarding the DPI.
     *
     * @param dpi
     *            The DPI for which the factor is requested.
     * @return The factor for dpi scaling calculations.
     */
    public static float getDpiFactor(int dpi) {
        // never scale below 96 dpi
        return Math.max(1.0f, Math.round((dpi / 96f) * 100) / 100f);
    }

    /**
     * @return <code>true</code> if either the horizontal or the vertical DPI
     *         value is bigger than 96.
     */
    public static boolean needScaling() {
        return (getDpiX() > 96 || getDpiY() > 96);
    }

    /**
     * @return The horizontal dots per inch of the default display.
     */
    public static int getDpiX() {
        return Display.getDefault().getDPI().x;
    }

    /**
     * @return The vertical dots per inch of the default display.
     */
    public static int getDpiY() {
        return Display.getDefault().getDPI().y;
    }

    /**
     * @return The suffix that is used to mark an image as upscaled image.
     */
    public static String getScalingImageSuffix() {
        return "_" + getDpiX() + "_" + getDpiY(); //$NON-NLS-1$ //$NON-NLS-2$
    }

    /**
     * Converts the given amount of pixels to a DPI scaled value using the
     * factor for the horizontal DPI value.
     *
     * @param pixel
     *            the amount of pixels to convert.
     * @return The converted pixels.
     */
    public static int convertHorizontalPixelToDpi(int pixel) {
        return (int) Math.round(Double.valueOf(pixel * (double) getDpiFactor(getDpiX())));
    }

    /**
     * Converts the given DPI scaled value to a pixel value using the factor for
     * the horizontal DPI.
     *
     * @param dpi
     *            the DPI value to convert.
     * @return The pixel value related to the given DPI
     */
    public static int convertHorizontalDpiToPixel(int dpi) {
        return (int) Math.round(Double.valueOf(dpi / (double) getDpiFactor(getDpiY())));
    }

    /**
     * Converts the given amount of pixels to a DPI scaled value using the
     * factor for the vertical DPI.
     *
     * @param pixel
     *            the amount of pixels to convert.
     * @return The converted pixels.
     */
    public static int convertVerticalPixelToDpi(int pixel) {
        return (int) Math.round(Double.valueOf(pixel * (double) getDpiFactor(getDpiX())));
    }

    /**
     * Converts the given DPI scaled value to a pixel value using the factor for
     * the vertical DPI.
     *
     * @param dpi
     *            the DPI value to convert.
     * @return The pixel value related to the given DPI
     */
    public static int convertVerticalDpiToPixel(int dpi) {
        return (int) Math.round(Double.valueOf(dpi / (double) getDpiFactor(getDpiY())));
    }

}
