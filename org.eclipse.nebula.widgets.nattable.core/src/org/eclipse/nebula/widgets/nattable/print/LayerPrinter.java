/*******************************************************************************
 * Copyright (c) 2012 Original authors and others.
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
import org.eclipse.nebula.widgets.nattable.config.IConfigRegistry;
import org.eclipse.nebula.widgets.nattable.layer.ILayer;
import org.eclipse.nebula.widgets.nattable.print.command.PrintEntireGridCommand;
import org.eclipse.nebula.widgets.nattable.print.command.TurnViewportOffCommand;
import org.eclipse.nebula.widgets.nattable.print.command.TurnViewportOnCommand;
import org.eclipse.nebula.widgets.nattable.summaryrow.command.CalculateSummaryRowValuesCommand;
import org.eclipse.nebula.widgets.nattable.util.IClientAreaProvider;
import org.eclipse.swt.SWT;
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
 * This class is used to print a layer.
 * Usually you create an instance by using the top most layer in the layer stack.
 * For grids this is the GridLayer, otherwise the ViewportLayer is a good choice.
 */
public class LayerPrinter {

	private final IConfigRegistry configRegistry;
	private final ILayer layer;
	private final IClientAreaProvider originalClientAreaProvider;
	public static final int FOOTER_HEIGHT_IN_PRINTER_DPI = 300;

	final SimpleDateFormat dateFormat = new SimpleDateFormat("EEE, d MMM yyyy HH:mm a"); //$NON-NLS-1$
	private final String footerDate;

	/**
	 * 
	 * @param layer The layer to print. Usually the top most layer in the layer stack.
	 * 			For grids this should be the GridLayer, for custom CompositeLayer compositions
	 * 			the CompositeLayer, otherwise the ViewportLayer is a good choice.
	 * @param configRegistry The ConfigRegistry needed for rendering to the print GC.
	 */
	public LayerPrinter(ILayer layer, IConfigRegistry configRegistry) {
		this.layer = layer;
		this.configRegistry = configRegistry;
		this.originalClientAreaProvider = layer.getClientAreaProvider();
		this.footerDate = dateFormat.format(new Date());
	}

	/**
	 * Computes the scale factor to match the printer resolution.
	 * @param printer The printer that will be used.
	 * @return The amount to scale the screen resolution by, to match the 
	 * 			printer the resolution.
	 */
	private Point computeScaleFactor(Printer printer) {
		Point screenDPI = Display.getDefault().getDPI();
		Point printerDPI = printer.getDPI();

		int scaleFactorX = printerDPI.x / screenDPI.x;
		int scaleFactorY = printerDPI.y / screenDPI.y;
		return new Point(scaleFactorX, scaleFactorY);
	}

	/**
	 * @return The size of the layer to fit all the contents.
	 */
	private Rectangle getTotalArea() {
		return new Rectangle(0, 0, layer.getWidth(), layer.getHeight());
	}

	/**
	 * Calculates number of horizontal and vertical pages needed
	 * to print the entire layer.
	 * @param printer The printer that will be used.
	 * @return The number of horizontal and vertical pages that are
	 * 			needed to print the layer.
	 */
	private Point getPageCount(Printer printer){
		Rectangle layerArea = getTotalArea();
		Rectangle printArea = computePrintArea(printer);
		Point scaleFactor = computeScaleFactor(printer);

		int numOfHorizontalPages = layerArea.width / (printArea.width / scaleFactor.x);
		int numOfVerticalPages = layerArea.height / (printArea.height / scaleFactor.y);

		// Adjusting for 0 index
		return new Point(numOfHorizontalPages + 1, numOfVerticalPages + 1);
	}

	/**
	 * Will first open the PrintDialog to let a user configure the print job
	 * and then starts the print job.
	 * @param shell The shell which should be the parent of the PrintDialog.
	 */
	public void print(final Shell shell) {
		//turn viewport off to ensure calculation of the print pages for the whole table
		layer.doCommand(new TurnViewportOffCommand());
		
		final Printer printer = setupPrinter(shell);
		if (printer == null) {
			return;
		}

		//turn viewport on
		layer.doCommand(new TurnViewportOnCommand());
		
		//Note: As we are operating on the same layer instance that is shown in the UI
		//		executing the print job asynchronously will not cause a real asynchronous
		//		execution. The UI will hang until the print job is done, because we access
		//		the information to print from the same instance. 
		//		For further developments we need to ensure that for printing a deep copy 
		//		of the layer needs to be performed instead of operating on the same instance.
		Display.getDefault().asyncExec(new PrintJob(printer));
	}

	/**
	 * Checks if a given page number should be printed.
	 * Page is allowed to print if:
	 * 	  User asked to print all pages or Page in a specified range
	 * @param printerData The printer settings made by the user. Needed to determine
	 * 			if a page should be printed dependent to the scope
	 * @param currentPage The page that should be checked
	 * @return <code>true</code> if the given page should be printed, 
	 * 			<code>false</code> if not 
	 */
	private boolean shouldPrint(PrinterData printerData, int totalPageCount) {
		if (printerData.scope == PrinterData.PAGE_RANGE) {
			return totalPageCount >= printerData.startPage && totalPageCount <= printerData.endPage;
		}
		return true;
	}

	/**
	 * Opens the PrintDialog to let the user specify the printer and print configurations to use.
	 * @param shell The Shell which should be the parent for the PrintDialog
	 * @return The selected printer with the print configuration made by the user.
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
		if (printerData == null){
			return null;
		}
		return new Printer(printerData);
	}

	/**
	 * Computes the print area, including margins
	 * @param printer The printer that will be used.
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
		if (left < rect.x) left = rect.x;

		int right = (rect.width + trim.x + trim.width) - dpi.x;
		if (right > rect.width) right = rect.width;

		int top = trim.y + dpi.y;
		if (top < rect.y) top = rect.y;

		int bottom = (rect.height + trim.y + trim.height) - dpi.y;
		if (bottom > rect.height) bottom = rect.height;

		return new Rectangle(left, top, right - left, bottom - top);
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
		 * @param printer The printer that will be used.
		 */
		private PrintJob(Printer printer) {
			this.printer = printer;
		}

		@Override
		public void run() {
			if (printer.startJob("NatTable")) { //$NON-NLS-1$
				//if a SummaryRowLayer is in the layer stack, we need to ensure that the values are calculated
				layer.doCommand(new CalculateSummaryRowValuesCommand());

				//ensure that the viewport is turned off
				layer.doCommand(new TurnViewportOffCommand());
				
				//set the size of the layer according to the print setttings made by the user
				setLayerSize(printer.getPrinterData());
				
				final Rectangle printerClientArea = computePrintArea(printer);
				final Point scaleFactor = computeScaleFactor(printer);
				final Point pageCount = getPageCount(printer);
				GC gc = new GC(printer);

				// Print pages Left to Right and then Top to Down
				int currentPage = 1;
				for (int verticalPageNumber = 0; verticalPageNumber < pageCount.y; verticalPageNumber++) {

					for (int horizontalPageNumber = 0; horizontalPageNumber < pageCount.x; horizontalPageNumber++) {

						// Calculate bounds for the next page
						Rectangle printBounds = new Rectangle(
								(printerClientArea.width / scaleFactor.x) * horizontalPageNumber,
								((printerClientArea.height - FOOTER_HEIGHT_IN_PRINTER_DPI) / scaleFactor.y) * verticalPageNumber,
								printerClientArea.width / scaleFactor.x,
								(printerClientArea.height - FOOTER_HEIGHT_IN_PRINTER_DPI) / scaleFactor.y);

						if (shouldPrint(printer.getPrinterData(), currentPage)) {
							printer.startPage();

							Transform printerTransform = new Transform(printer);

							// Adjust for DPI difference between display and printer
							printerTransform.scale(scaleFactor.x, scaleFactor.y);

							// Adjust for margins
							printerTransform.translate(printerClientArea.x / scaleFactor.x, printerClientArea.y / scaleFactor.y);

							// Grid will not automatically print the pages at the left margin.
							// Example: page 1 will print at x = 0, page 2 at x = 100, page 3 at x = 300
							// Adjust to print from the left page margin. i.e x = 0
							printerTransform.translate(-1 * printBounds.x, -1 * printBounds.y);
							gc.setTransform(printerTransform);

							printLayer(gc, printBounds);

							printFooter(gc, currentPage, printBounds);

							printer.endPage();
							printerTransform.dispose();
						}
						currentPage++;
					}
				}

				printer.endJob();

				gc.dispose();
				printer.dispose();
			}
			restoreLayerState();
		}

		/**
		 * Set the client area of the layer so it matches the print settings made by the user. 
		 * In case a user selected to print everything, the size needs to be extended so that
		 * all the contents fit in the viewport to ensure that we print the <i>entire</i> table.
		 * @param printerData The PrinterData that was configured by the user on the PrintDialog.
		 */
		private void setLayerSize(PrinterData printerData) {
			if (printerData.scope == PrinterData.SELECTION) {
				layer.setClientAreaProvider(originalClientAreaProvider);
			}
			else {
				final Rectangle fullLayerSize = getTotalArea();
				
				layer.setClientAreaProvider(new IClientAreaProvider(){
					@Override
					public Rectangle getClientArea() {
						return fullLayerSize;
					}
				});
				
				//in case the whole layer should be printed or only the selected pages,
				//we need to ensure to set the starting point to 0/0
				layer.doCommand(new PrintEntireGridCommand());
			}
		}

		/**
		 * Print the part of the layer that matches the given print bounds.
		 * @param gc The print GC to render the layer to.
		 * @param printBounds The bounds of the print page.
		 */
		private void printLayer(GC gc, Rectangle printBounds) {
			layer.getLayerPainter().paintLayer(layer, gc, 0, 0, printBounds, configRegistry);
		}

		/**
		 * Print the footer to the page.
		 * @param gc The print GC to render the footer to.
		 * @param totalPageCount The total number of pages that are printed.
		 * @param printBounds The bounds of the print page.
		 */
		private void printFooter(GC gc, int totalPageCount, Rectangle printBounds) {
			gc.setForeground(Display.getCurrent().getSystemColor(SWT.COLOR_BLACK));
			gc.setBackground(Display.getCurrent().getSystemColor(SWT.COLOR_WHITE));

			gc.drawLine(printBounds.x,
			            printBounds.y + printBounds.height+10,
			            printBounds.x + printBounds.width,
			            printBounds.y + printBounds.height+10);

			gc.drawText(Messages.getString("Printer.page") + " " + totalPageCount, //$NON-NLS-1$ //$NON-NLS-2$
			            printBounds.x,
			            printBounds.y + printBounds.height + 15);

			// Approximate width of the date string: 140
			gc.drawText(footerDate,
			            printBounds.x + printBounds.width - 140,
			            printBounds.y + printBounds.height + 15);
		}
		
		/**
		 * Restores the layer state to match the display characteristics again.
		 * This is done by resetting the client area provider and turning the viewport
		 * on again.
		 */
		private void restoreLayerState() {
			layer.setClientAreaProvider(originalClientAreaProvider);
			layer.doCommand(new TurnViewportOnCommand());
		}

	}

}
