/*******************************************************************************
 * Copyright (c) 2017, 2025 Original authors and others.
 *
 * This program and the accompanying materials are made
 * available under the terms of the Eclipse Public License 2.0
 * which is available at https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 * Contributors:
 *     Thanh Liem PHAN (ALL4TEC) <thanhliem.phan@all4tec.net> - Bug 509361
 ******************************************************************************/
package org.eclipse.nebula.widgets.nattable.export.image;

import java.io.IOException;
import java.io.OutputStream;

import org.eclipse.nebula.widgets.nattable.config.IConfigRegistry;
import org.eclipse.nebula.widgets.nattable.export.FileOutputStreamProvider;
import org.eclipse.nebula.widgets.nattable.export.IOutputStreamProvider;
import org.eclipse.nebula.widgets.nattable.export.ITableExporter;
import org.eclipse.nebula.widgets.nattable.layer.ILayer;
import org.eclipse.nebula.widgets.nattable.util.PlatformHelper;
import org.eclipse.swt.SWT;
import org.eclipse.swt.SWTException;
import org.eclipse.swt.graphics.GC;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.graphics.ImageData;
import org.eclipse.swt.graphics.ImageLoader;
import org.eclipse.swt.graphics.Rectangle;
import org.eclipse.swt.widgets.ProgressBar;
import org.eclipse.swt.widgets.Shell;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * This class is used to export a NatTable to different types of image.
 * Currently, 4 types of image are supported: BMP, JPEG, JPG, PNG. GIF is not
 * supported due to its limitation on image depth described in the bug 38232.
 *
 * <p>
 * <b>Warning:</b> Using this class is risky as it could cause severe damage to
 * NatTables that show huge data sets (for example: a table with more than 20k
 * rows).
 * </p>
 *
 * @since 1.5
 */
public class ImageExporter implements ITableExporter {

    private static final Logger LOG = LoggerFactory.getLogger(ImageExporter.class);

    private static final String DEFAULT_IMAGE_NAME = "table_export.bmp"; //$NON-NLS-1$

    private static final String BMP_FILTER_NAME = "BMP files (*.bmp)"; //$NON-NLS-1$
    private static final String BMP_FILTER_EXT = "*.bmp"; //$NON-NLS-1$

    private static final String JPEG_FILTER_NAME = "JPEG files (*.jpeg)"; //$NON-NLS-1$
    private static final String JPEG_FILTER_EXT = "*.jpeg"; //$NON-NLS-1$

    private static final String JPG_FILTER_NAME = "JPG files (*.jpg)"; //$NON-NLS-1$
    private static final String JPG_FILTER_EXT = "*.jpg"; //$NON-NLS-1$

    private static final String PNG_FILTER_NAME = "PNG files (*.png)"; //$NON-NLS-1$
    private static final String PNG_FILTER_EXT = "*.png"; //$NON-NLS-1$

    private static final String[] DEFAULT_FILTER_NAMES =
            new String[] { BMP_FILTER_NAME, JPEG_FILTER_NAME, JPG_FILTER_NAME, PNG_FILTER_NAME };

    private static final String[] DEFAULT_FILTER_EXTENSIONS =
            new String[] { BMP_FILTER_EXT, JPEG_FILTER_EXT, JPG_FILTER_EXT, PNG_FILTER_EXT };

    /**
     * The {@link IOutputStreamProvider} used to create new output stream on
     * beginning new export operation.
     */
    private final IOutputStreamProvider outputStreamProvider;

    /**
     * Default constructor to create a new image exporter.
     */
    public ImageExporter() {
        this(new FileOutputStreamProvider(DEFAULT_IMAGE_NAME, DEFAULT_FILTER_NAMES, DEFAULT_FILTER_EXTENSIONS));
    }

    /**
     * Create a new ImageExporter that uses the given IOutputStreamProvider for
     * retrieving the OutputStream.
     *
     * @param outputStreamProvider
     *            The IOutputStreamProvider used to retrieve the OutputStream to
     *            write the export to.
     */
    public ImageExporter(IOutputStreamProvider outputStreamProvider) {
        this.outputStreamProvider = outputStreamProvider;
    }

    @Override
    public Object getResult() {
        return this.outputStreamProvider.getResult();
    }

    /**
     * Export the given layer of the nattable to image. This method must be
     * called after the execution of the
     * {@link ImageExporter#getOutputStream(Shell)} method.
     *
     * @param shell
     *            The parent shell
     * @param layer
     *            The layer to be exported
     * @param configRegistry
     *            The configure registry of the nattable
     */
    @Override
    public void exportTable(Shell shell,
            ProgressBar progressBar,
            OutputStream outputStream,
            ILayer layer,
            IConfigRegistry configRegistry) throws IOException {

        if (null == shell || null == layer || null == configRegistry) {
            throw new IllegalArgumentException("Shell, layer or configure registry must not be null"); //$NON-NLS-1$
        }

        if (PlatformHelper.isRAP()) {
            throw new IllegalStateException("An export to image is not supported with RAP"); //$NON-NLS-1$
        }

        int width = layer.getWidth();
        int height = layer.getHeight();

        final Image image = new Image(shell.getDisplay(), width, height);
        GC gc = new GC(image);

        try {
            Rectangle layerBounds = new Rectangle(0, 0, width, height);
            layer.getLayerPainter().paintLayer(layer, gc, 0, 0, layerBounds, configRegistry);

            ImageLoader imageLoader = new ImageLoader();
            imageLoader.data = new ImageData[] { image.getImageData() };

            if (this.outputStreamProvider instanceof FileOutputStreamProvider) {
                final FileOutputStreamProvider fileOutputStreamProvider = (FileOutputStreamProvider) this.outputStreamProvider;
                final String selectedFilterExt = DEFAULT_FILTER_EXTENSIONS[fileOutputStreamProvider.getExtensionFilterIndex()];

                final int imageFormat = getImageFormatIndex(selectedFilterExt);

                if (imageFormat >= 0) {
                    try {
                        imageLoader.save(outputStream, imageFormat);
                    } catch (SWTException e) {
                        LOG.error("Error while saving the result image", e); //$NON-NLS-1$
                    }
                }
            }
        } finally {
            gc.dispose();
            image.dispose();
        }
    }

    /**
     * Get the image format index by the filter extension.
     *
     * @param selectedFilterExt
     *            The selected filter extension
     * @return The image index or -1 if not found
     */
    protected int getImageFormatIndex(String selectedFilterExt) {
        int imageFormat = -1;

        if (BMP_FILTER_EXT.equals(selectedFilterExt)) {
            imageFormat = SWT.IMAGE_BMP;
        } else if (JPEG_FILTER_EXT.equals(selectedFilterExt) || JPG_FILTER_EXT.equals(selectedFilterExt)) {
            imageFormat = SWT.IMAGE_JPEG;
        } else if (PNG_FILTER_EXT.equals(selectedFilterExt)) {
            imageFormat = SWT.IMAGE_PNG;
        }

        return imageFormat;
    }

    @Override
    public OutputStream getOutputStream(Shell shell) {
        if (shell == null) {
            throw new IllegalArgumentException("Shell must not be null"); //$NON-NLS-1$
        }

        return this.outputStreamProvider.getOutputStream(shell);
    }

}
