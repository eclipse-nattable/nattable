/*******************************************************************************
 * Copyright (c) 2012, 2016 Original authors and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Original authors and others - initial API and implementation
 ******************************************************************************/
package org.eclipse.nebula.widgets.nattable.print;

import java.text.SimpleDateFormat;
import java.util.Date;

import org.eclipse.nebula.widgets.nattable.Messages;
import org.eclipse.nebula.widgets.nattable.config.CellConfigAttributes;
import org.eclipse.nebula.widgets.nattable.config.Direction;
import org.eclipse.nebula.widgets.nattable.config.IConfigRegistry;
import org.eclipse.nebula.widgets.nattable.formula.command.DisableFormulaCachingCommand;
import org.eclipse.nebula.widgets.nattable.formula.command.EnableFormulaCachingCommand;
import org.eclipse.nebula.widgets.nattable.layer.ILayer;
import org.eclipse.nebula.widgets.nattable.print.command.PrintEntireGridCommand;
import org.eclipse.nebula.widgets.nattable.print.command.TurnViewportOffCommand;
import org.eclipse.nebula.widgets.nattable.print.command.TurnViewportOnCommand;
import org.eclipse.nebula.widgets.nattable.print.config.PrintConfigAttributes;
import org.eclipse.nebula.widgets.nattable.resize.AutoResizeHelper;
import org.eclipse.nebula.widgets.nattable.style.CellStyleAttributes;
import org.eclipse.nebula.widgets.nattable.style.DisplayMode;
import org.eclipse.nebula.widgets.nattable.style.IStyle;
import org.eclipse.nebula.widgets.nattable.summaryrow.command.CalculateSummaryRowValuesCommand;
import org.eclipse.nebula.widgets.nattable.util.GUIHelper;
import org.eclipse.nebula.widgets.nattable.util.IClientAreaProvider;
import org.eclipse.swt.graphics.Color;
import org.eclipse.swt.graphics.Font;
import org.eclipse.swt.graphics.GC;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.graphics.Rectangle;
import org.eclipse.swt.graphics.Transform;
import org.eclipse.swt.printing.PrintDialog;
import org.eclipse.swt.printing.Printer;
import org.eclipse.swt.printing.PrinterData;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Shell;

/**
 * This class is used to print a layer. Usually you create an instance by using
 * the top most layer in the layer stack. For grids this is the GridLayer,
 * otherwise the ViewportLayer is a good choice.
 */
public class LayerPrinter {

    private final IConfigRegistry configRegistry;
    private final ILayer layer;
    private final IClientAreaProvider originalClientAreaProvider;
    public static final int FOOTER_HEIGHT_IN_PRINTER_DPI = 300;

    final SimpleDateFormat dateFormat;
    private final String footerDate;

    private final int footerHeight;

    /**
     * @since 1.4
     */
    protected boolean preRender = true;

    private final Direction fittingMode;

    /**
     *
     * @param layer
     *            The layer to print. Usually the top most layer in the layer
     *            stack. For grids this should be the GridLayer, for custom
     *            CompositeLayer compositions the CompositeLayer, otherwise the
     *            ViewportLayer is a good choice.
     * @param configRegistry
     *            The ConfigRegistry needed for rendering to the print GC.
     */
    public LayerPrinter(ILayer layer, IConfigRegistry configRegistry) {
        this.layer = layer;
        this.configRegistry = configRegistry;
        this.originalClientAreaProvider = layer.getClientAreaProvider();

        // configure the footer height
        Integer fh = configRegistry.getConfigAttribute(
                PrintConfigAttributes.FOOTER_HEIGHT,
                DisplayMode.NORMAL);
        this.footerHeight = (fh != null) ? fh : FOOTER_HEIGHT_IN_PRINTER_DPI;

        // configure the footer date
        String configuredFormat = configRegistry.getConfigAttribute(
                PrintConfigAttributes.DATE_FORMAT,
                DisplayMode.NORMAL);
        if (configuredFormat != null) {
            this.dateFormat = new SimpleDateFormat(configuredFormat);
        } else {
            this.dateFormat = new SimpleDateFormat("EEE, d MMM yyyy HH:mm a"); //$NON-NLS-1$
        }

        this.footerDate = this.dateFormat.format(new Date());

        // configure the fitting mode
        Direction configuredFittingMode = configRegistry.getConfigAttribute(
                PrintConfigAttributes.FITTING_MODE,
                DisplayMode.NORMAL);
        this.fittingMode = (configuredFittingMode != null) ? configuredFittingMode : Direction.NONE;
    }

    /**
     * Computes the scale factor to match the printer resolution.
     *
     * @param printer
     *            The printer that will be used.
     * @param dpi
     *            <code>true</code> if in any case the dpi scaling factor should
     *            be returned, <code>false</code> if the calculation properties
     *            need to be checked whether the scaling factor to match a page
     *            should be returned
     * @return The amount to scale the screen resolution by, to match the
     *         printer the resolution.
     */
    private float[] computeScaleFactor(Printer printer, boolean dpi) {
        Point screenDPI = Display.getDefault().getDPI();
        Point printerDPI = printer.getDPI();

        float sfX = Float.valueOf(printerDPI.x) / Float.valueOf(screenDPI.x);
        float sfY = Float.valueOf(printerDPI.y) / Float.valueOf(screenDPI.y);

        if (!dpi && (this.fittingMode != Direction.NONE)) {
            Rectangle total = getTotalArea();
            Rectangle print = computePrintArea(printer);

            float pixelX = Float.valueOf(print.width) / Float.valueOf(total.width);
            float pixelY = (Float.valueOf(print.height) - getFooterHeightInPrinterDPI()) / Float.valueOf(total.height);

            switch (this.fittingMode) {
                case HORIZONTAL:
                    return new float[] { pixelX, pixelX };
                case VERTICAL:
                    return new float[] { pixelY, pixelY };
                case BOTH:
                    return new float[] { pixelX, pixelY };
            }
        }

        return new float[] { sfX, sfY };
    }

    /**
     * @return The size of the layer to fit all the contents.
     */
    private Rectangle getTotalArea() {
        return new Rectangle(0, 0, this.layer.getWidth(), this.layer.getHeight());
    }

    /**
     * Calculates number of horizontal and vertical pages needed to print the
     * entire layer.
     *
     * @param printer
     *            The printer that will be used.
     * @return The number of horizontal and vertical pages that are needed to
     *         print the layer.
     */
    private Point getPageCount(Printer printer) {
        Rectangle layerArea = getTotalArea();
        Rectangle printArea = computePrintArea(printer);
        float[] scaleFactor = computeScaleFactor(printer, false);

        Float horizontal = Float.valueOf(layerArea.width / (printArea.width / scaleFactor[0]));
        int numOfHorizontalPages = horizontal.intValue();
        if ((horizontal % 1) != 0) {
            // the int conversion simply cuts of the decimals
            // so in case there are decimals we need to round up
            numOfHorizontalPages++;
        }
        Float vertical = Float.valueOf(layerArea.height / ((printArea.height - getFooterHeightInPrinterDPI()) / scaleFactor[1]));
        int numOfVerticalPages = vertical.intValue();
        if ((vertical % 1) != 0) {
            // the int conversion simply cuts of the decimals
            // so in case there are decimals we need to round up
            numOfVerticalPages++;
        }

        return new Point(numOfHorizontalPages, numOfVerticalPages);
    }

    /**
     * @return The footer height in printer DPI that should be used to render
     *         the footer.
     * @since 1.4
     */
    protected int getFooterHeightInPrinterDPI() {
        return this.footerHeight;
    }

    /**
     * Will first open the PrintDialog to let a user configure the print job and
     * then starts the print job.
     *
     * @param shell
     *            The shell which should be the parent of the PrintDialog.
     */
    public void print(final Shell shell) {
        // turn viewport off to ensure calculation of the print pages for the
        // whole table
        this.layer.doCommand(new TurnViewportOffCommand());

        Printer printer = null;
        try {
            printer = setupPrinter(shell);
            if (printer == null) {
                return;
            }
        } finally {
            // turn viewport on
            this.layer.doCommand(new TurnViewportOnCommand());
        }

        // Note: As we are operating on the same layer instance that is shown in
        // the UI executing the print job asynchronously will not cause a real
        // asynchronous execution. The UI will hang until the print job is done,
        // because we access the information to print from the same instance.
        // For further developments we need to ensure that for printing a deep
        // copy of the layer needs to be performed instead of operating on the
        // same instance.
        Display.getDefault().asyncExec(new PrintJob(printer));
    }

    /**
     * Checks if a given page number should be printed. Page is allowed to print
     * if: User asked to print all pages or Page in a specified range
     *
     * @param printerData
     *            The printer settings made by the user. Needed to determine if
     *            a page should be printed dependent to the scope
     * @param currentPage
     *            The page that should be checked
     * @return <code>true</code> if the given page should be printed,
     *         <code>false</code> if not
     */
    private boolean shouldPrint(PrinterData printerData, int totalPageCount) {
        if (printerData.scope == PrinterData.PAGE_RANGE) {
            return totalPageCount >= printerData.startPage
                    && totalPageCount <= printerData.endPage;
        }
        return true;
    }

    /**
     * Opens the PrintDialog to let the user specify the printer and print
     * configurations to use.
     *
     * @param shell
     *            The Shell which should be the parent for the PrintDialog
     * @return The selected printer with the print configuration made by the
     *         user.
     */
    private Printer setupPrinter(final Shell shell) {
        Printer defaultPrinter = new Printer();
        Point pageCount = getPageCount(defaultPrinter);
        defaultPrinter.dispose();

        final PrintDialog printDialog = new PrintDialog(shell);
        printDialog.setStartPage(1);
        printDialog.setEndPage(pageCount.x * pageCount.y);
        printDialog.setScope(PrinterData.ALL_PAGES);

        PrinterData printerData = printDialog.open();
        if (printerData == null) {
            return null;
        }
        return new Printer(printerData);
    }

    /**
     * Computes the print area, including margins
     *
     * @param printer
     *            The printer that will be used.
     * @return The print area that will be used to render the table.
     */
    private Rectangle computePrintArea(Printer printer) {
        // Get the printable area
        Rectangle rect = printer.getClientArea();

        // Compute the trim
        Rectangle trim = printer.computeTrim(0, 0, 0, 0);

        // Get the printer's DPI
        Point dpi = printer.getDPI();
        dpi.x = dpi.x / 2;
        dpi.y = dpi.y / 2;

        // Calculate the printable area, using 1 inch margins
        int left = trim.x + dpi.x;
        if (left < rect.x)
            left = rect.x;

        int right = (rect.width + trim.x + trim.width) - dpi.x;
        if (right > rect.width)
            right = rect.width;

        int top = trim.y + dpi.y;
        if (top < rect.y)
            top = rect.y;

        int bottom = (rect.height + trim.y + trim.height) - dpi.y;
        if (bottom > rect.height)
            bottom = rect.height;

        return new Rectangle(left, top, right - left, bottom - top);
    }

    /**
     * Enable in-memory pre-rendering. This is necessary in case content
     * painters are used that are configured for content based auto-resizing.
     *
     * @since 1.4
     */
    public void enablePreRendering() {
        this.preRender = true;
    }

    /**
     * Disable in-memory pre-rendering. You should consider to disable
     * pre-rendering if no content painters are used that are configured for
     * content based auto-resizing.
     *
     * @since 1.4
     */
    public void disablePreRendering() {
        this.preRender = false;
    }

    /**
     * The job for printing the layer.
     */
    private class PrintJob implements Runnable {
        /**
         * The printer that will be used.
         */
        private final Printer printer;

        /**
         * @param printer
         *            The printer that will be used.
         */
        private PrintJob(Printer printer) {
            this.printer = printer;
        }

        @Override
        public void run() {
            LayerPrinter.this.layer.doCommand(new TurnViewportOffCommand());
            float[] scaleFactor = null;
            float[] dpiFactor = null;
            try {
                scaleFactor = computeScaleFactor(this.printer, false);
                dpiFactor = computeScaleFactor(this.printer, true);
            } finally {
                LayerPrinter.this.layer.doCommand(new TurnViewportOnCommand());
            }

            // check if a grid line width is configured
            Integer width = LayerPrinter.this.configRegistry.getConfigAttribute(
                    CellConfigAttributes.GRID_LINE_WIDTH,
                    DisplayMode.NORMAL);
            // if no explicit width is set, we temporary specify a grid line
            // width of 2 for optimized grid line printing
            if (width == null) {
                LayerPrinter.this.configRegistry.registerConfigAttribute(
                        CellConfigAttributes.GRID_LINE_WIDTH, 2);
            }

            // if pre-rendering is enabled, render in-memory to trigger content
            // based auto-resizing
            if (LayerPrinter.this.preRender) {
                Transform tempTransform = new Transform(this.printer);
                tempTransform.scale(scaleFactor[0], scaleFactor[1]);
                AutoResizeHelper.autoResize(LayerPrinter.this.layer, LayerPrinter.this.configRegistry);
                tempTransform.dispose();
            }

            if (this.printer.startJob("NatTable")) { //$NON-NLS-1$
                try {
                    // if a SummaryRowLayer is in the layer stack, we need to
                    // ensure that the values are calculated
                    LayerPrinter.this.layer.doCommand(new CalculateSummaryRowValuesCommand());

                    // ensure that the viewport is turned off
                    LayerPrinter.this.layer.doCommand(new TurnViewportOffCommand());

                    // ensure that formula processing is performed in the
                    // current thread
                    LayerPrinter.this.layer.doCommand(new DisableFormulaCachingCommand());

                    // set the size of the layer according to the print
                    // settings made by the user
                    setLayerSize(this.printer.getPrinterData());

                    final Rectangle printerClientArea = computePrintArea(this.printer);
                    final Point pageCount = getPageCount(this.printer);
                    GC gc = new GC(this.printer);

                    // Print pages Left to Right and then Top to Down
                    int currentPage = 1;
                    for (int verticalPageNumber = 0; verticalPageNumber < pageCount.y; verticalPageNumber++) {

                        for (int horizontalPageNumber = 0; horizontalPageNumber < pageCount.x; horizontalPageNumber++) {

                            // Calculate bounds for the next page
                            Rectangle printBounds = new Rectangle(
                                    Float.valueOf((printerClientArea.width / scaleFactor[0]) * horizontalPageNumber).intValue(),
                                    Float.valueOf(((printerClientArea.height - getFooterHeightInPrinterDPI()) / scaleFactor[1]) * verticalPageNumber).intValue(),
                                    Float.valueOf(printerClientArea.width / scaleFactor[0]).intValue(),
                                    Float.valueOf((printerClientArea.height - getFooterHeightInPrinterDPI()) / scaleFactor[1]).intValue());

                            Rectangle footerBounds = new Rectangle(
                                    Float.valueOf((printerClientArea.width / dpiFactor[0]) * horizontalPageNumber).intValue(),
                                    Float.valueOf(((printerClientArea.height - getFooterHeightInPrinterDPI()) / dpiFactor[1]) * verticalPageNumber).intValue(),
                                    Float.valueOf(printerClientArea.width / dpiFactor[0]).intValue(),
                                    Float.valueOf((printerClientArea.height - getFooterHeightInPrinterDPI()) / dpiFactor[1]).intValue());

                            if (shouldPrint(this.printer.getPrinterData(), currentPage)) {
                                this.printer.startPage();

                                Transform printerTransform = new Transform(this.printer);
                                Transform footerTransform = new Transform(this.printer);

                                configureScalingTransform(printerTransform, scaleFactor, printerClientArea, printBounds);
                                configureScalingTransform(footerTransform, dpiFactor, printerClientArea, footerBounds);

                                gc.setTransform(printerTransform);
                                printLayer(gc, printBounds);

                                gc.setTransform(footerTransform);
                                printFooter(gc, currentPage, footerBounds);

                                this.printer.endPage();
                                printerTransform.dispose();
                                footerTransform.dispose();
                            }
                            currentPage++;
                        }
                    }

                    this.printer.endJob();

                    gc.dispose();
                    this.printer.dispose();
                } finally {
                    restoreLayerState();
                }
            }

            // there was no explicit width configured, so we configured a
            // temporary one for grid line printing. this configuration needs to
            // be removed again
            if (width == null) {
                LayerPrinter.this.configRegistry.unregisterConfigAttribute(CellConfigAttributes.GRID_LINE_WIDTH);
            }

        }

        /**
         * Configure the given {@link Transform} in order to support scaling
         * correctly on printing.
         *
         * @param transform
         *            The {@link Transform} to configure
         * @param scaleFactor
         *            The scale factor to set and to be used for translation
         * @param printerClientArea
         *            The client area of the printer
         * @param printBounds
         *            The print bounds
         */
        private void configureScalingTransform(
                Transform transform, float[] scaleFactor,
                Rectangle printerClientArea, Rectangle printBounds) {
            // Adjust for DPI difference between display and printer
            transform.scale(scaleFactor[0], scaleFactor[1]);

            // Adjust for margins
            transform.translate(
                    printerClientArea.x / scaleFactor[0],
                    printerClientArea.y / scaleFactor[1]);

            // Grid will not automatically print the pages
            // at the left margin.
            // Example: page 1 will print at x = 0, page 2
            // at x = 100, page 3 at x = 300
            // Adjust to print from the left page margin.
            // i.e x = 0
            transform.translate(-1 * printBounds.x, -1 * printBounds.y);
        }

        /**
         * Set the client area of the layer so it matches the print settings
         * made by the user. In case a user selected to print everything, the
         * size needs to be extended so that all the contents fit in the
         * viewport to ensure that we print the <i>entire</i> table.
         *
         * @param printerData
         *            The PrinterData that was configured by the user on the
         *            PrintDialog.
         */
        private void setLayerSize(PrinterData printerData) {
            if (printerData != null && printerData.scope == PrinterData.SELECTION) {
                LayerPrinter.this.layer.setClientAreaProvider(LayerPrinter.this.originalClientAreaProvider);
            } else {
                final Rectangle fullLayerSize = getTotalArea();

                LayerPrinter.this.layer.setClientAreaProvider(new IClientAreaProvider() {
                    @Override
                    public Rectangle getClientArea() {
                        return fullLayerSize;
                    }
                });

                // in case the whole layer should be printed or only the
                // selected pages, we need to ensure to set the starting point
                // to 0/0
                LayerPrinter.this.layer.doCommand(new PrintEntireGridCommand());
            }
        }

        /**
         * Print the part of the layer that matches the given print bounds.
         *
         * @param gc
         *            The print GC to render the layer to.
         * @param printBounds
         *            The bounds of the print page.
         */
        private void printLayer(GC gc, Rectangle printBounds) {
            LayerPrinter.this.layer.getLayerPainter().paintLayer(
                    LayerPrinter.this.layer, gc, 0, 0, printBounds, LayerPrinter.this.configRegistry);
        }

        /**
         * Print the footer to the page.
         *
         * @param gc
         *            The print GC to render the footer to.
         * @param totalPageCount
         *            The total number of pages that are printed.
         * @param printBounds
         *            The bounds of the print page.
         */
        private void printFooter(GC gc, int totalPageCount, Rectangle printBounds) {
            Color oldForeground = gc.getForeground();
            Color oldBackground = gc.getBackground();
            Font oldFont = gc.getFont();

            Color footerForeground = null;
            Color footerBackground = null;
            Font footerFont = null;

            IStyle style = LayerPrinter.this.configRegistry.getConfigAttribute(
                    PrintConfigAttributes.FOOTER_STYLE,
                    DisplayMode.NORMAL);
            if (style != null) {
                footerForeground = style.getAttributeValue(CellStyleAttributes.FOREGROUND_COLOR);
                footerBackground = style.getAttributeValue(CellStyleAttributes.BACKGROUND_COLOR);
                footerFont = style.getAttributeValue(CellStyleAttributes.FONT);
            }
            gc.setForeground(footerForeground != null ? footerForeground : GUIHelper.COLOR_BLACK);
            gc.setBackground(footerBackground != null ? footerBackground : GUIHelper.COLOR_WHITE);
            gc.setFont(footerFont != null ? footerFont : GUIHelper.DEFAULT_FONT);

            gc.drawLine(
                    printBounds.x,
                    printBounds.y + printBounds.height + 10,
                    printBounds.x + printBounds.width,
                    printBounds.y + printBounds.height + 10);

            gc.drawText(
                    Messages.getString("Printer.page") + " " + totalPageCount, //$NON-NLS-1$ //$NON-NLS-2$
                    printBounds.x,
                    printBounds.y + printBounds.height + 15);

            gc.drawText(
                    LayerPrinter.this.footerDate,
                    printBounds.x + printBounds.width - gc.textExtent(LayerPrinter.this.footerDate).x,
                    printBounds.y + printBounds.height + 15);

            gc.setForeground(oldForeground);
            gc.setBackground(oldBackground);
            gc.setFont(oldFont);
        }

        /**
         * Restores the layer state to match the display characteristics again.
         * This is done by resetting the client area provider, turning the
         * viewport on and enabling formula result caching again.
         */
        private void restoreLayerState() {
            LayerPrinter.this.layer.setClientAreaProvider(LayerPrinter.this.originalClientAreaProvider);
            LayerPrinter.this.layer.doCommand(new TurnViewportOnCommand());
            LayerPrinter.this.layer.doCommand(new EnableFormulaCachingCommand());
        }

    }

}
