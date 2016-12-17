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

import java.text.MessageFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import org.eclipse.nebula.widgets.nattable.Messages;
import org.eclipse.nebula.widgets.nattable.config.CellConfigAttributes;
import org.eclipse.nebula.widgets.nattable.config.Direction;
import org.eclipse.nebula.widgets.nattable.config.IConfigRegistry;
import org.eclipse.nebula.widgets.nattable.formula.command.DisableFormulaCachingCommand;
import org.eclipse.nebula.widgets.nattable.formula.command.EnableFormulaCachingCommand;
import org.eclipse.nebula.widgets.nattable.layer.ILayer;
import org.eclipse.nebula.widgets.nattable.layer.cell.ILayerCell;
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

    private class PrintTarget {

        private final IConfigRegistry configRegistry;
        private final ILayer layer;
        private final ILayer repeatHeaderLayer;
        private final IClientAreaProvider originalClientAreaProvider;
        private final boolean repeat;

        PrintTarget(ILayer layer, ILayer repeatHeaderLayer, IConfigRegistry configRegistry, boolean repeat) {
            this.layer = layer;
            this.repeatHeaderLayer = repeatHeaderLayer;
            this.configRegistry = configRegistry;
            this.originalClientAreaProvider = layer.getClientAreaProvider();
            this.repeat = repeat;
        }
    }

    private final List<PrintTarget> printTargets = new ArrayList<PrintTarget>();

    public static final int FOOTER_HEIGHT_IN_PRINTER_DPI = 300;

    final SimpleDateFormat dateFormat;
    private final String footerDate;
    private final String footerPagePattern;

    private final int footerHeight;

    /**
     * @since 1.4
     */
    protected boolean preRender = true;

    private final Direction fittingMode;
    private final boolean stretch;

    private boolean join = false;

    private boolean calculatePageCount = true;

    /**
     *
     * @param layer
     *            The layer to print. Usually the NatTable instance itself or
     *            the top most layer in the layer stack.
     * @param configRegistry
     *            The {@link IConfigRegistry} that contains the general print
     *            configurations and that should be used to print the given
     *            {@link ILayer}.
     */
    public LayerPrinter(ILayer layer, IConfigRegistry configRegistry) {
        this(layer, null, configRegistry, false);
    }

    /**
     *
     * @param layer
     *            The layer to print. Usually the NatTable instance itself or
     *            the top most layer in the layer stack.
     * @param repeatHeaderLayer
     *            The layer that is part of the layer main layer composition
     *            that should be repeated on every page. Typically the column
     *            header layer.
     * @param configRegistry
     *            The {@link IConfigRegistry} that contains the general print
     *            configurations and that should be used to print the given
     *            {@link ILayer}.
     * @since 1.5
     */
    public LayerPrinter(ILayer layer, ILayer repeatHeaderLayer, IConfigRegistry configRegistry) {
        this(layer, repeatHeaderLayer, configRegistry, false);
    }

    /**
     *
     * @param layer
     *            The layer to print. Usually the NatTable instance itself or
     *            the top most layer in the layer stack.
     * @param configRegistry
     *            The {@link IConfigRegistry} that contains the general print
     *            configurations and that should be used to print the given
     *            {@link ILayer}.
     * @param repeat
     *            Flag to configure whether the given layer should be printed on
     *            every page. Needed for example in case an additional header
     *            layer is used outside the main table.
     * @since 1.5
     */
    public LayerPrinter(ILayer layer, IConfigRegistry configRegistry, boolean repeat) {
        this(layer, null, configRegistry, repeat);
    }

    /**
     *
     * @param layer
     *            The layer to print. Usually the NatTable instance itself or
     *            the top most layer in the layer stack.
     * @param repeatHeaderLayer
     *            The layer that is part of the layer main layer composition
     *            that should be repeated on every page. Typically the column
     *            header layer.
     * @param configRegistry
     *            The {@link IConfigRegistry} that contains the general print
     *            configurations and that should be used to print the given
     *            {@link ILayer}.
     * @param repeat
     *            Flag to configure whether the given layer should be printed on
     *            every page. Needed for example in case an additional header
     *            layer is used outside the main table.
     * @since 1.5
     */
    public LayerPrinter(ILayer layer, ILayer repeatHeaderLayer, IConfigRegistry configRegistry, boolean repeat) {
        this.printTargets.add(new PrintTarget(layer, repeatHeaderLayer, configRegistry, repeat));

        // configure the footer height
        Integer fh = configRegistry.getConfigAttribute(
                PrintConfigAttributes.FOOTER_HEIGHT,
                DisplayMode.NORMAL);
        this.footerHeight = (fh != null) ? fh : FOOTER_HEIGHT_IN_PRINTER_DPI;

        String pagePattern = configRegistry.getConfigAttribute(
                PrintConfigAttributes.FOOTER_PAGE_PATTERN,
                DisplayMode.NORMAL);
        this.footerPagePattern = (pagePattern != null) ? pagePattern : Messages.getString("Printer.page"); //$NON-NLS-1$

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

        Boolean configureStretching = configRegistry.getConfigAttribute(
                PrintConfigAttributes.STRETCH,
                DisplayMode.NORMAL);
        this.stretch = (configureStretching != null) ? configureStretching : false;
    }

    /**
     * Adds the given {@link ILayer} as print target. It can be used to register
     * multiple NatTable or layer stacks in one print job.
     *
     * @param layer
     *            The {@link ILayer} that should be printed together with the
     *            main {@link ILayer} registered via constructor.
     * @param configRegistry
     *            The {@link IConfigRegistry} that should be used to print the
     *            given {@link ILayer}.
     *
     * @since 1.5
     */
    public void addPrintTarget(ILayer layer, IConfigRegistry configRegistry) {
        addPrintTarget(layer, null, configRegistry);
    }

    /**
     * Adds the given {@link ILayer} as print target. It can be used to register
     * multiple NatTable or layer stacks in one print job. This method also
     * allows to specify the layer that should be repeated on every page, e.g.
     * the column header layer in a grid composition.
     *
     * @param layer
     *            The {@link ILayer} that should be printed together with the
     *            main {@link ILayer} registered via constructor.
     * @param repeatHeaderLayer
     *            The layer that is part of the layer main layer composition
     *            that should be repeated on every page. Typically the column
     *            header layer.
     * @param configRegistry
     *            The {@link IConfigRegistry} that should be used to print the
     *            given {@link ILayer}.
     *
     * @since 1.5
     */
    public void addPrintTarget(ILayer layer, ILayer repeatHeaderLayer, IConfigRegistry configRegistry) {
        this.printTargets.add(new PrintTarget(layer, repeatHeaderLayer, configRegistry, false));
    }

    /**
     * Configure whether multiple print targets should be joined for printing or
     * not.
     *
     * @param join
     *            <code>true</code> if the print targets should be printed
     *            consecutively, <code>false</code> if every print target should
     *            start with a new page. Default is <code>false</code>.
     *
     * @since 1.5
     */
    public void joinPrintTargets(boolean join) {
        this.join = join;
    }

    /**
     *
     * @return The height of the print target that should be repeated on every
     *         page. Typically only the first registered one in case of multiple
     *         print targets. If there is only one print target or no print
     *         target should be repeated it returns 0.
     */
    private int getRepeatPrintTargetHeight() {
        int result = 0;
        for (PrintTarget target : this.printTargets) {
            if (target.repeat) {
                result += target.layer.getHeight();
            }
        }
        return result;
    }

    /**
     * @param printer
     *            The printer that will be used for printing.
     * @return The scale factor used for the scaling of the repeat print target
     *         or [0, 0] of there is no print target configured for repeating.
     */
    private float[] getRepeatPrintTargetScaleFactor(Printer printer) {
        float[] result = new float[] { 0, 0 };
        // currently we only support repeating the first of the configured multi
        // print targets
        if (this.printTargets.get(0).repeat) {
            result = computeLayerScaleFactor(this.printTargets.get(0).layer, printer);
        }
        return result;
    }

    /**
     * Computes the scale factor to match the printer resolution.
     *
     * @param layer
     *            The layer for which the scale factor should be calculated.
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
    private float[] computeScaleFactor(ILayer layer, Printer printer, boolean dpi) {
        Point screenDPI = Display.getDefault().getDPI();
        Point printerDPI = printer.getDPI();

        float sfX = Float.valueOf(printerDPI.x) / Float.valueOf(screenDPI.x);
        float sfY = Float.valueOf(printerDPI.y) / Float.valueOf(screenDPI.y);

        if (!dpi && (this.fittingMode != Direction.NONE)) {
            Rectangle total = getTotalArea(layer);
            if (this.join || this.printTargets.get(0).repeat) {
                // calculate the total height of all targets
                total.height = 0;
                for (PrintTarget target : this.printTargets) {
                    total.height += target.layer.getHeight();
                }
            }
            Rectangle print = computePrintArea(printer);

            float pixelX = Float.valueOf(print.width) / Float.valueOf(total.width);
            float pixelY = (Float.valueOf(print.height) - getFooterHeightInPrinterDPI()) / Float.valueOf(total.height);

            // only support down-scaling, no stretching
            // stretching could cause serious issues, e.g. vertical
            // stretching for one row could cause a really long running
            // operation because the width gets really really big
            if (pixelX > sfX && !this.stretch) {
                pixelX = sfX;
            }
            if (pixelY > sfY) {
                pixelY = sfY;
            }

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
     * Computes the scale factor to match the printer resolution. In case of
     * fitting mode configurations and multiple print targets, the common
     * scaling factor will be calculated and returned.
     *
     * @param layer
     *            The layer for which the scale factor should be calculated.
     * @param printer
     *            The printer that will be used.
     * @return The amount to scale the screen resolution by, to match the
     *         printer the resolution.
     */
    private float[] computeLayerScaleFactor(ILayer layer, Printer printer) {
        float[] scaleFactor = null;
        if (this.fittingMode == Direction.NONE
                || (!this.join && !this.printTargets.get(0).repeat)
                || this.stretch) {
            scaleFactor = computeScaleFactor(layer, printer, false);
        } else {
            // search for the common scaling factor
            for (PrintTarget tempTarget : this.printTargets) {
                float[] tempFactor = computeScaleFactor(tempTarget.layer, printer, false);
                if (scaleFactor == null) {
                    scaleFactor = tempFactor;
                } else {
                    scaleFactor[0] = Math.min(scaleFactor[0], tempFactor[0]);
                    scaleFactor[1] = Math.min(scaleFactor[1], tempFactor[1]);
                }
            }
        }
        return scaleFactor;
    }

    /**
     * @param layer
     *            The layer for which the total area is requested.
     * @return The size of the layer to fit all the contents.
     */
    private Rectangle getTotalArea(ILayer layer) {
        return new Rectangle(0, 0, layer.getWidth(), layer.getHeight());
    }

    /**
     * Calculates number of horizontal and vertical pages needed to print all
     * registered layers.
     *
     * @param printer
     *            The printer that will be used.
     * @return The number of pages that are needed to print.
     */
    private int getPageCount(Printer printer) {
        int result = 0;
        int available = -1;
        float[] prevScaleFactor = null;
        for (PrintTarget target : this.printTargets) {
            if (!target.repeat) {
                int[] layerResult = getPageCount(target, printer, available, prevScaleFactor);
                result += (layerResult[0] * layerResult[1]);

                // as the print targets should be joined and the print was
                // started on an existing page, we need to reduce the page count
                if (this.join && available > 0) {
                    result--;
                }

                available = layerResult[2];
                if (available >= 0) {
                    prevScaleFactor = computeLayerScaleFactor(target.layer, printer);
                }
            }
        }

        return result;
    }

    /**
     * Calculates number of horizontal and vertical pages needed to print the
     * entire layer of the given print target.
     *
     * @param target
     *            The print target to print.
     * @param printer
     *            The printer that will be used.
     * @param available
     *            The remaining available space in pixel on a page after the
     *            target is printed in case the print targets should be glued,
     *            -1 otherwise.
     * @param prevScaleFactor
     *            The scale factor of the previous table in case the tables
     *            should be joined. Needed to calculate the available space
     *            correctly
     * @return The number of horizontal and vertical pages that are needed to
     *         print the layer of the given print target and the remaining space
     *         on a page if the print targets should be glued.
     */
    private int[] getPageCount(PrintTarget target, Printer printer, int available, float[] prevScaleFactor) {
        Rectangle printArea = computePrintArea(printer);
        float[] scaleFactor = computeLayerScaleFactor(target.layer, printer);

        Integer[] gridLineWidth = getGridLineWidth(target.configRegistry);

        // calculate pages based on non cut off columns/rows
        int numOfHorizontalPages = 0;
        int pageWidth = Math.round(Float.valueOf(printArea.width / scaleFactor[0]));
        int endX = 0;
        while (endX < target.layer.getWidth()) {
            endX += pageWidth;
            int colPos = target.layer.getColumnPositionByX(endX);
            if (colPos >= 0) {
                ILayerCell cell = findColumnCellForBounds(target.layer, colPos);
                if (cell != null) {
                    Rectangle cellBounds = cell.getBounds();
                    if (cellBounds.x < endX) {
                        endX -= (endX - cellBounds.x);
                    }
                }
            } else {
                endX = target.layer.getWidth();
            }

            numOfHorizontalPages++;
        }

        int numOfVerticalPages = 0;
        // if we are ourself the print target that should be repeated, we don't
        // need to consider the repeat print target height
        int repeatPrintTargetHeightInDpi = target.repeat ? 0 : Math.round(Float.valueOf(getRepeatPrintTargetHeight() * getRepeatPrintTargetScaleFactor(printer)[1]));
        int headerHeightInDpi = (target.repeatHeaderLayer != null) ? Math.round(Float.valueOf((target.repeatHeaderLayer.getHeight() * scaleFactor[1]))) : 0;
        int pageHeight = Math.round(Float.valueOf((printArea.height - repeatPrintTargetHeightInDpi - headerHeightInDpi - getFooterHeightInPrinterDPI()) / scaleFactor[1]));
        int firstPageHeight = (available < 0)
                ? Math.round(Float.valueOf((printArea.height - repeatPrintTargetHeightInDpi - getFooterHeightInPrinterDPI()) / scaleFactor[1]))
                : Math.round(Float.valueOf(Math.round(Float.valueOf(available * prevScaleFactor[1])) / scaleFactor[1]));
        int endY = 0;
        int added = 0;
        int remaining = -1;

        while (endY < target.layer.getHeight()) {
            // on the first page we don't need to take care of the repeat
            // header height
            added = (numOfVerticalPages == 0) ? firstPageHeight : pageHeight;
            endY += added;

            int rowPos = target.layer.getRowPositionByY(endY);
            if (rowPos >= 0) {
                ILayerCell cell = findRowCellForBounds(target.layer, rowPos);
                if (cell != null) {
                    Rectangle cellBounds = cell.getBounds();
                    if (cellBounds.y < endY) {
                        endY -= (endY - cellBounds.y);
                    }
                }
            } else {
                // in case the print targets should be glued we need to
                // calculate the remaining space for the following print target
                if (this.join) {
                    remaining = ((numOfVerticalPages == 0) ? firstPageHeight : pageHeight) - (target.layer.getHeight() - (endY - added));
                }
                endY = target.layer.getHeight();
            }

            numOfVerticalPages++;
        }

        if (gridLineWidth[0] == null) {
            target.configRegistry.unregisterConfigAttribute(CellConfigAttributes.GRID_LINE_WIDTH);
        }

        return new int[] { numOfHorizontalPages, numOfVerticalPages, remaining };
    }

    private ILayerCell findColumnCellForBounds(ILayer layer, int colPos) {
        int rowPos = 0;
        ILayerCell cell = layer.getCellByPosition(colPos, rowPos);
        while (cell != null && cell.isSpannedCell()) {
            // if the cell is spanned, check the cell at the next row
            rowPos++;
            cell = layer.getCellByPosition(colPos, rowPos);
        }
        return cell;
    }

    private ILayerCell findRowCellForBounds(ILayer layer, int rowPos) {
        int colPos = 0;
        ILayerCell cell = layer.getCellByPosition(colPos, rowPos);
        while (cell != null && cell.isSpannedCell()) {
            // if the cell is spanned, check the cell at the next column
            colPos++;
            cell = layer.getCellByPosition(colPos, rowPos);
        }
        return cell;
    }

    /**
     * @return The footer height in printer DPI that should be used to render
     *         the footer.
     * @since 1.5
     */
    protected int getFooterHeightInPrinterDPI() {
        return this.footerHeight;
    }

    /**
     * @param configRegistry
     *            The {@link IConfigRegistry} to retrieve the grid line width
     *            from.
     * @return Integer array that contains the original configured width at
     *         index 0 and the grid line width to use at index 1.
     * @since 1.5
     */
    protected Integer[] getGridLineWidth(IConfigRegistry configRegistry) {
        // check if a grid line width is configured
        Integer width = configRegistry.getConfigAttribute(
                CellConfigAttributes.GRID_LINE_WIDTH,
                DisplayMode.NORMAL);
        Integer gridLineWidth = width;
        // if no explicit width is set, we temporary specify a grid line
        // width of 2 for optimized grid line printing
        if (width == null) {
            configRegistry.registerConfigAttribute(
                    CellConfigAttributes.GRID_LINE_WIDTH, 2);
            gridLineWidth = 2;
        }
        return new Integer[] { width, gridLineWidth };
    }

    /**
     * Will first open the PrintDialog to let a user configure the print job and
     * then starts the print job.
     *
     * @param shell
     *            The shell which should be the parent of the PrintDialog.
     */
    public void print(final Shell shell) {
        Printer printer = setupPrinter(shell);
        if (printer == null) {
            return;
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
        final PrintDialog printDialog = new PrintDialog(shell);
        printDialog.setStartPage(1);
        printDialog.setScope(PrinterData.ALL_PAGES);

        if (this.calculatePageCount) {
            // if pre-rendering is enabled, render in-memory to
            // trigger content based auto-resizing
            if (LayerPrinter.this.preRender) {
                for (PrintTarget target : LayerPrinter.this.printTargets) {
                    AutoResizeHelper.autoResize(target.layer, target.configRegistry);
                }
            }

            // turn viewport off to ensure calculation of the print pages for
            // the whole table
            for (PrintTarget target : this.printTargets) {
                target.layer.doCommand(new TurnViewportOffCommand());
            }

            try {
                Printer defaultPrinter = new Printer();
                int pageCount = getPageCount(defaultPrinter);
                defaultPrinter.dispose();

                printDialog.setEndPage(pageCount);
            } finally {
                // turn viewport on
                for (PrintTarget target : this.printTargets) {
                    target.layer.doCommand(new TurnViewportOnCommand());
                }
            }
        }

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
     * Enable page count calculation for opening the print dialog. This will
     * show the total page count in the print dialog.
     *
     * @since 1.5
     */
    public void enablePageCountCalculation() {
        this.calculatePageCount = true;
    }

    /**
     * Disable page count calculation for opening the print dialog. The page
     * count is pre-calculated to show how many pages are printed. This
     * pre-calculation can take some time for more complicated print setups.
     * Therefore it may be useful to disable that pre-calculation to get a
     * better user experience on opening the print dialog.
     *
     * @since 1.5
     */
    public void disablePageCountCalculation() {
        this.calculatePageCount = false;
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
            if (this.printer.startJob("NatTable")) { //$NON-NLS-1$
                GC gc = new GC(this.printer);

                for (PrintTarget target : LayerPrinter.this.printTargets) {
                    // if pre-rendering is enabled, render in-memory to
                    // trigger content based auto-resizing
                    if (LayerPrinter.this.preRender) {
                        AutoResizeHelper.autoResize(target.layer, target.configRegistry);
                    }
                    // turn the viewport for all targets off to ensure
                    // everything is taken into account for calculation and
                    // printing
                    target.layer.doCommand(new TurnViewportOffCommand());
                }

                try {
                    int currentPage = 1;
                    int totalPageCount = getPageCount(this.printer);

                    Integer[] repeatHeaderGridLineWidth = null;
                    float[] repeatScaleFactor = null;

                    int available = -1;
                    float[] prevScaleFactor = null;

                    boolean newPage = true;
                    boolean pageStarted = false;
                    for (PrintTarget target : LayerPrinter.this.printTargets) {
                        if (target.repeat) {
                            // we do not render the repeat print target directly
                            // as it is handled on every page while printing
                            repeatHeaderGridLineWidth = getGridLineWidth(target.configRegistry);
                            repeatScaleFactor = computeLayerScaleFactor(target.layer, this.printer);
                            continue;
                        }

                        float[] scaleFactor = computeLayerScaleFactor(target.layer, this.printer);
                        float[] dpiFactor = computeScaleFactor(target.layer, this.printer, true);

                        int availablePixel = available;
                        if (available > 0) {
                            int prevDPI = Math.round(Float.valueOf(available * prevScaleFactor[1]));
                            availablePixel = Math.round(Float.valueOf(prevDPI / scaleFactor[1]));
                        }

                        Integer[] gridLineWidth = getGridLineWidth(target.configRegistry);

                        try {
                            // if a SummaryRowLayer is in the layer stack, we
                            // need to ensure that the values are calculated
                            target.layer.doCommand(new CalculateSummaryRowValuesCommand());

                            // ensure that formula processing is performed in
                            // the current thread
                            target.layer.doCommand(new DisableFormulaCachingCommand());

                            // set the size of the layer according to the print
                            // settings made by the user
                            setLayerSize(target, this.printer.getPrinterData());

                            final Rectangle printerClientArea = computePrintArea(this.printer);
                            final int printBoundsWidth = Math.round(Float.valueOf(printerClientArea.width / scaleFactor[0]));
                            int repeatPrintTargetHeight = getRepeatPrintTargetHeight();
                            float repeatPrintTargetHeightInDpi = repeatPrintTargetHeight * ((repeatScaleFactor != null) ? repeatScaleFactor[1] : 0);
                            int headerHeight = (target.repeatHeaderLayer != null) ? target.repeatHeaderLayer.getHeight() : 0;
                            float headerHeightDPI = headerHeight * scaleFactor[1];
                            int printBoundsHeight = Math.round(Float.valueOf((printerClientArea.height - repeatPrintTargetHeightInDpi - headerHeightDPI - getFooterHeightInPrinterDPI()) / scaleFactor[1]));

                            int firstPagePrintBoundsHeight = (available < 0)
                                    ? Math.round(Float.valueOf((printerClientArea.height - repeatPrintTargetHeightInDpi - getFooterHeightInPrinterDPI()) / scaleFactor[1]))
                                    : availablePixel;

                            final int[] pageCount = getPageCount(target, this.printer, available, prevScaleFactor);

                            // Print pages Left to Right and then Top to Down
                            int startY = 0;
                            for (int verticalPageNumber = 0; verticalPageNumber < pageCount[1]; verticalPageNumber++) {

                                // on the first page we don't need to take care
                                // of the repeat header height
                                int pbh = (verticalPageNumber == 0 ? firstPagePrintBoundsHeight : printBoundsHeight);

                                int endY = startY + pbh;
                                int rowPos = target.layer.getRowPositionByY(endY);
                                if (rowPos >= 0) {
                                    ILayerCell cell = findRowCellForBounds(target.layer, rowPos);
                                    if (cell != null) {
                                        Rectangle cellBounds = cell.getBounds();
                                        if (cellBounds.y < endY) {
                                            pbh -= (endY - cellBounds.y);
                                        }
                                    }
                                }

                                int startX = 0;
                                for (int horizontalPageNumber = 0; horizontalPageNumber < pageCount[0]; horizontalPageNumber++) {

                                    // Calculate bounds for the next page
                                    Rectangle printBounds = new Rectangle(
                                            startX,
                                            startY,
                                            printBoundsWidth,
                                            pbh);

                                    int endX = startX + printBounds.width;
                                    int colPos = target.layer.getColumnPositionByX(endX);
                                    if (colPos >= 0) {
                                        ILayerCell cell = findColumnCellForBounds(target.layer, colPos);
                                        if (cell != null) {
                                            Rectangle cellBounds = cell.getBounds();
                                            if (cellBounds.x < endX) {
                                                printBounds.width -= (endX - cellBounds.x);
                                            }
                                        }
                                    }

                                    Rectangle footerBounds = new Rectangle(
                                            Math.round(Float.valueOf((printerClientArea.width / dpiFactor[0]) * horizontalPageNumber)),
                                            Math.round(Float.valueOf(((printerClientArea.height - getFooterHeightInPrinterDPI()) / dpiFactor[1]) * verticalPageNumber)),
                                            Math.round(Float.valueOf(printerClientArea.width / dpiFactor[0])),
                                            Math.round(Float.valueOf((printerClientArea.height - getFooterHeightInPrinterDPI()) / dpiFactor[1])));

                                    if (shouldPrint(this.printer.getPrinterData(), currentPage)) {
                                        // end a page that was previously
                                        // started
                                        if (pageStarted) {
                                            this.printer.endPage();
                                            newPage = true;
                                        }

                                        // start a new page
                                        if (newPage) {
                                            this.printer.startPage();
                                            pageStarted = true;
                                            newPage = false;
                                        }

                                        // ensure there is a next page
                                        // will be set to false afterwards again
                                        // if multiple targets fit on one page
                                        if (!pageStarted && !newPage) {
                                            pageStarted = true;
                                        }

                                        Transform printerTransform = new Transform(this.printer);
                                        Transform repeatTransform = new Transform(this.printer);
                                        Transform headerTransform = new Transform(this.printer);
                                        Transform footerTransform = new Transform(this.printer);

                                        Rectangle intersect = new Rectangle(
                                                0,
                                                0,
                                                target.layer.getWidth(),
                                                target.layer.getHeight());

                                        intersect = printBounds.intersection(intersect);

                                        configureScalingTransform(printerTransform, scaleFactor, printerClientArea, intersect);
                                        configureScalingTransform(repeatTransform, (repeatScaleFactor != null ? repeatScaleFactor : scaleFactor), printerClientArea, intersect);
                                        configureScalingTransform(headerTransform, scaleFactor, printerClientArea, intersect);

                                        if (repeatPrintTargetHeight > 0) {
                                            repeatTransform.translate(0, startY);
                                            gc.setTransform(repeatTransform);

                                            Rectangle repeatIntersect = new Rectangle(
                                                    0,
                                                    0,
                                                    LayerPrinter.this.printTargets.get(0).layer.getWidth(),
                                                    LayerPrinter.this.printTargets.get(0).layer.getHeight());

                                            repeatIntersect = printBounds.intersection(repeatIntersect);

                                            printLayer(LayerPrinter.this.printTargets.get(0), gc, new Rectangle(repeatIntersect.x, 0, repeatIntersect.width, repeatPrintTargetHeight));
                                            printerTransform.translate(0, Math.round(Float.valueOf(Math.round(Float.valueOf(repeatPrintTargetHeight * repeatScaleFactor[1])) / scaleFactor[1])));
                                        }

                                        if (target.repeatHeaderLayer != null && verticalPageNumber != 0) {
                                            headerTransform.translate(0, startY + Math.round(Float.valueOf(Math.round(Float.valueOf(repeatPrintTargetHeight * ((repeatScaleFactor != null) ? repeatScaleFactor[1] : 0))) / scaleFactor[1])));
                                            gc.setTransform(headerTransform);
                                            printLayer(target, gc, new Rectangle(printBounds.x, 0, intersect.width, headerHeight));
                                            printerTransform.translate(0, headerHeight);
                                        }

                                        // on joining print targets we need to
                                        // transform for rendering the first
                                        // page on the same page as the previous
                                        // target
                                        if (LayerPrinter.this.join && available > 0 && verticalPageNumber == 0) {
                                            printerTransform.translate(0, (printBoundsHeight + headerHeight) - availablePixel);
                                        }

                                        gc.setTransform(printerTransform);
                                        printLayer(target, gc, intersect);

                                        configureScalingTransform(footerTransform, dpiFactor, printerClientArea, footerBounds);
                                        gc.setTransform(footerTransform);
                                        printFooter(gc, currentPage, totalPageCount, footerBounds, target.configRegistry);

                                        printerTransform.dispose();
                                        repeatTransform.dispose();
                                        headerTransform.dispose();
                                        footerTransform.dispose();
                                    }
                                    currentPage++;

                                    startX += printBounds.width;
                                }
                                startY += pbh;
                                available = pageCount[2];
                                prevScaleFactor = scaleFactor;
                            }

                            if (LayerPrinter.this.join && available > 0) {
                                currentPage--;
                            }

                            // after last page is printed, check if the page
                            // needs to end or if it should be used for the next
                            // print target
                            if (!LayerPrinter.this.join || available < 0) {
                                this.printer.endPage();
                                newPage = true;
                                pageStarted = false;
                            } else if (LayerPrinter.this.join && available > 0) {
                                newPage = false;
                                pageStarted = false;
                            }

                        } finally {
                            restoreLayerState(target);
                        }

                        // there was no explicit width configured, so we
                        // configured a temporary one for grid line printing.
                        // this configuration needs to be removed again
                        if (gridLineWidth[0] == null) {
                            target.configRegistry.unregisterConfigAttribute(CellConfigAttributes.GRID_LINE_WIDTH);
                        }
                    }

                    if (repeatHeaderGridLineWidth != null && repeatHeaderGridLineWidth[0] == null) {
                        LayerPrinter.this.printTargets.get(0).configRegistry.unregisterConfigAttribute(CellConfigAttributes.GRID_LINE_WIDTH);
                    }
                } finally {
                    this.printer.endJob();
                    gc.dispose();
                    this.printer.dispose();

                    // turn viewport on
                    for (PrintTarget target : LayerPrinter.this.printTargets) {
                        target.layer.doCommand(new TurnViewportOnCommand());
                    }
                }
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
         * @param target
         *            The print target to print.
         * @param printerData
         *            The PrinterData that was configured by the user on the
         *            PrintDialog.
         */
        private void setLayerSize(PrintTarget target, PrinterData printerData) {
            if (printerData != null && printerData.scope == PrinterData.SELECTION) {
                target.layer.setClientAreaProvider(target.originalClientAreaProvider);
            } else {
                final Rectangle fullLayerSize = getTotalArea(target.layer);

                target.layer.setClientAreaProvider(new IClientAreaProvider() {
                    @Override
                    public Rectangle getClientArea() {
                        return fullLayerSize;
                    }
                });

                // in case the whole layer should be printed or only the
                // selected pages, we need to ensure to set the starting point
                // to 0/0
                target.layer.doCommand(new PrintEntireGridCommand());
            }
        }

        /**
         * Print the part of the layer that matches the given print bounds.
         *
         * @param target
         *            The print target to print
         * @param gc
         *            The print GC to render the layer to.
         * @param printBounds
         *            The bounds of the print page.
         */
        private void printLayer(PrintTarget target, GC gc, Rectangle printBounds) {
            target.layer.getLayerPainter().paintLayer(
                    target.layer, gc, 0, 0, printBounds, target.configRegistry);
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
         * @param configRegistry
         *            The {@link IConfigRegistry} needed to retrieve the footer
         *            style.
         */
        private void printFooter(GC gc, int currentPage, int totalPageCount, Rectangle printBounds, IConfigRegistry configRegistry) {
            Color oldForeground = gc.getForeground();
            Color oldBackground = gc.getBackground();
            Font oldFont = gc.getFont();

            Color footerForeground = null;
            Color footerBackground = null;
            Font footerFont = null;

            IStyle style = configRegistry.getConfigAttribute(
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
                    MessageFormat.format(LayerPrinter.this.footerPagePattern, currentPage, totalPageCount),
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
         *
         * @param target
         *            The print target whose state should be restarted.
         */
        private void restoreLayerState(PrintTarget target) {
            target.layer.setClientAreaProvider(target.originalClientAreaProvider);
            target.layer.doCommand(new EnableFormulaCachingCommand());
        }

    }

}
