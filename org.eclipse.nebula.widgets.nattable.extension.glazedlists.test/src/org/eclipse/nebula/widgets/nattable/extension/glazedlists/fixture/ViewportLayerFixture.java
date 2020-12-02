/*******************************************************************************
 * Copyright (c) 2012, 2020 Original authors and others.
 *
 * This program and the accompanying materials are made
 * available under the terms of the Eclipse Public License 2.0
 * which is available at https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 * Contributors:
 *     Original authors and others - initial API and implementation
 ******************************************************************************/
package org.eclipse.nebula.widgets.nattable.extension.glazedlists.fixture;

import org.eclipse.nebula.widgets.nattable.layer.IUniqueIndexLayer;
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

    public static final Rectangle DEFAULT_CLIENT_AREA = new Rectangle(0, 0, 200, 100);
    public static final IClientAreaProvider DEFAULT_CLIENT_AREA_PROVIDER = getClientAreaProvider(DEFAULT_CLIENT_AREA);
    public static final Scrollable DEFAULT_SCROLLABLE = scrollable();

    public ViewportLayerFixture() {
        super(new DataLayerFixture());
        setClientAreaProvider(getClientAreaProvider(DEFAULT_CLIENT_AREA));
    }

    public ViewportLayerFixture(IUniqueIndexLayer underlingLayer) {
        super(underlingLayer);
        setClientAreaProvider(getClientAreaProvider(new Rectangle(0, 0, 1000, 1000)));
        doCommand(new InitializeClientAreaCommandFixture());
    }

    public ViewportLayerFixture(int width, int height) {
        super(new DataLayerFixture(width, height));
    }

    public ViewportLayerFixture(int colCount, int rowCount, int defaultColWidth, int defaultRowHeight) {
        super(new DataLayerFixture(colCount, rowCount, defaultColWidth, defaultRowHeight));
        setClientAreaProvider(DEFAULT_CLIENT_AREA_PROVIDER);
    }

    public ViewportLayerFixture(final Rectangle clientArea) {
        super(new DataLayerFixture());
        setClientAreaProvider(getClientAreaProvider(clientArea));
    }

    private static IClientAreaProvider getClientAreaProvider(Rectangle clientArea) {
        return new IClientAreaProvider() {
            @Override
            public Rectangle getClientArea() {
                return clientArea;
            }
        };
    }

    private static Scrollable scrollable() {
        return new Composite(new Shell(Display.getDefault()), SWT.H_SCROLL | SWT.V_SCROLL);
    }
}
