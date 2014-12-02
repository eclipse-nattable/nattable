/*******************************************************************************
 * Copyright (c) 2013 Edwin Park and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    Edwin Park <esp1@cornell.edu> - initial API and implementation
 *******************************************************************************/
package org.eclipse.nebula.widgets.nattable.util;

import org.eclipse.swt.graphics.Rectangle;

/**
 * Adapter class to support multiple viewports in one NatTable instance. It
 * needs to be created with the IClientAreaProvider that is set to the
 * ViewportLayer before applying this one as base IClientAreaProvider.
 */
public class ClientAreaAdapter implements IClientAreaProvider {

    /**
     * The base IClientAreaProvider. It should be the IClientAreaProvider that
     * was applied before to the ViewportLayer to which this ClientAreaAdapter
     * will be set to.
     */
    private final IClientAreaProvider clientAreaProvider;

    /**
     * The width that should be used by this ClientAreaAdapter. Setting the
     * value to a positive number will cause a vertical split viewport within a
     * NatTable.
     */
    private int width = -1;

    /**
     * The height that should be used by this ClientAreaAdapter. Setting the
     * value to a positive number will cause a horizontal split viewport within
     * a NatTable.
     */
    private int height = -1;

    /**
     * @param baseProvider
     *            The base IClientAreaProvider. It should be the
     *            IClientAreaProvider that was applied before to the
     *            ViewportLayer to which this ClientAreaAdapter will be set to.
     */
    public ClientAreaAdapter(IClientAreaProvider baseProvider) {
        this.clientAreaProvider = baseProvider;
    }

    /**
     * @param baseProvider
     *            The base IClientAreaProvider. It should be the
     *            IClientAreaProvider that was applied before to the
     *            ViewportLayer to which this ClientAreaAdapter will be set to.
     * @param width
     *            The width that should be used by this ClientAreaAdapter.
     *            Setting the value to a positive number will cause a vertical
     *            split viewport within a NatTable.
     * @param height
     *            The height that should be used by this ClientAreaAdapter.
     *            Setting the value to a positive number will cause a horizontal
     *            split viewport within a NatTable.
     */
    public ClientAreaAdapter(IClientAreaProvider baseProvider, int width,
            int height) {
        this.clientAreaProvider = baseProvider;
        this.width = width;
        this.height = height;
    }

    /**
     *
     * @return The width that should be used by this ClientAreaAdapter. If a
     *         negative number is returned, the width of the base
     *         IClientAreaProvider will be used internally.
     */
    public int getWidth() {
        return this.width;
    }

    /**
     *
     * @param width
     *            The width that should be used by this ClientAreaAdapter. If a
     *            negative number is set, the width of the base
     *            IClientAreaProvider will be used internally.
     */
    public void setWidth(int width) {
        this.width = width;
    }

    /**
     *
     * @return The height that should be used by this ClientAreaAdapter. If a
     *         negative number is returned, the height of the base
     *         IClientAreaProvider will be used internally.
     */
    public int getHeight() {
        return this.height;
    }

    /**
     *
     * @param height
     *            The height that should be used by this ClientAreaAdapter. If a
     *            negative number is set, the height of the base
     *            IClientAreaProvider will be used internally.
     */
    public void setHeight(int height) {
        this.height = height;
    }

    @Override
    public Rectangle getClientArea() {
        Rectangle clientArea = this.clientAreaProvider.getClientArea();

        if (this.width >= 0)
            clientArea.width = this.width;

        if (this.height >= 0)
            clientArea.height = this.height;

        return clientArea;
    }

}
