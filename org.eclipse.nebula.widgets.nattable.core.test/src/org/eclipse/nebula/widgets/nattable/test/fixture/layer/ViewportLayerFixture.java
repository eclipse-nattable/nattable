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
package org.eclipse.nebula.widgets.nattable.test.fixture.layer;


import org.eclipse.nebula.widgets.nattable.layer.IUniqueIndexLayer;
import org.eclipse.nebula.widgets.nattable.test.fixture.InitializeClientAreaCommandFixture;
import org.eclipse.nebula.widgets.nattable.util.IClientAreaProvider;
import org.eclipse.nebula.widgets.nattable.viewport.ViewportLayer;
import org.eclipse.swt.SWT;
import org.eclipse.swt.graphics.Rectangle;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Scrollable;
import org.eclipse.swt.widgets.Shell;

/**
 * Viewport fixtures to enable testing with various configurations.
 */
public class ViewportLayerFixture extends ViewportLayer {

	public static Rectangle DEFAULT_CLIENT_AREA = new Rectangle(0, 0, 200, 100);
	public static IClientAreaProvider DEFAULT_CLIENT_AREA_PROVIDER = getClientAreaProvider(DEFAULT_CLIENT_AREA);
	public static Scrollable DEFAULT_SCROLLABLE = scrollable();

	/**
	 * Default Xtor
	 */
	public ViewportLayerFixture() {
		super(new DataLayerFixture());
		setClientAreaProvider(getClientAreaProvider(DEFAULT_CLIENT_AREA));
	}

	public ViewportLayerFixture(IUniqueIndexLayer underlingLayer) {
		super(underlingLayer);
		setClientAreaProvider(getClientAreaProvider(new Rectangle(0, 0, 1000, 1000)));
		doCommand(new InitializeClientAreaCommandFixture());
	}

	/**
	 * Xtor Fixture with all columns equal width and all rows equal height.
	 */
	public ViewportLayerFixture(int width, int height) {
		super(new DataLayerFixture(width, height));
	}

	/**
	 * Xtor Fixture with all columns equal width and all rows equal height.
	 * 
	 * @param colCount total number of columns
	 * @param rowCount total number of rows
	 */
	public ViewportLayerFixture(int colCount, int rowCount, int defaultColWidth, int defaultRowHeight) {
		super(new DataLayerFixture(colCount, rowCount, defaultColWidth, defaultRowHeight));
		setClientAreaProvider(DEFAULT_CLIENT_AREA_PROVIDER);
	}

	/**
	 * Xtor Provide your own <i>clientArea</i>
	 */
	public ViewportLayerFixture(final Rectangle clientArea) {
		super(new DataLayerFixture());
		setClientAreaProvider(getClientAreaProvider(clientArea));
	}

	private static IClientAreaProvider getClientAreaProvider(final Rectangle clientArea) {
		return new IClientAreaProvider() {
			public Rectangle getClientArea() {
				return clientArea;
			}
		};
	}

	private static Scrollable scrollable() {
		return new Composite(new Shell(Display.getDefault()), SWT.H_SCROLL | SWT.V_SCROLL);
	}
}
