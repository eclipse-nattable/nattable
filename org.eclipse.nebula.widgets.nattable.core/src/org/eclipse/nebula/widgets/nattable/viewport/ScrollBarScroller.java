/*******************************************************************************
 * Copyright (c) 2013, 2025 Edwin Park and others.
 *
 * This program and the accompanying materials are made
 * available under the terms of the Eclipse Public License 2.0
 * which is available at https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 * Contributors:
 *    Edwin Park - initial API and implementation
 *******************************************************************************/
package org.eclipse.nebula.widgets.nattable.viewport;

import org.eclipse.nebula.widgets.nattable.util.PlatformHelper;
import org.eclipse.swt.SWT;
import org.eclipse.swt.widgets.Listener;
import org.eclipse.swt.widgets.ScrollBar;

/**
 * {@link IScroller} implementation that wraps an SWT {@link ScrollBar}.
 */
public class ScrollBarScroller implements IScroller<ScrollBar> {

    private ScrollBar scrollBar;

    /**
     * Create a new {@link ScrollBarScroller} that wraps the given
     * {@link ScrollBar}.
     *
     * @param scrollBar
     *            The {@link ScrollBar} to wrap. Can not be <code>null</code>.
     * @throws IllegalArgumentException
     *             if the given {@link ScrollBar} is <code>null</code>.
     */
    public ScrollBarScroller(ScrollBar scrollBar) {
        if (scrollBar == null) {
            throw new IllegalArgumentException("ScrollBar can not be null!"); //$NON-NLS-1$
        }
        this.scrollBar = scrollBar;
    }

    @Override
    public ScrollBar getUnderlying() {
        return this.scrollBar;
    }

    @Override
    public boolean isDisposed() {
        return this.scrollBar.isDisposed();
    }

    @Override
    public void addListener(int eventType, Listener listener) {
        this.scrollBar.addListener(eventType, listener);
    }

    @Override
    public void removeListener(int eventType, Listener listener) {
        this.scrollBar.removeListener(eventType, listener);
    }

    @Override
    public int getSelection() {
        return this.scrollBar.getSelection();
    }

    @Override
    public void setSelection(int value) {
        this.scrollBar.setSelection(value);
    }

    @Override
    public int getMaximum() {
        return this.scrollBar.getMaximum();
    }

    @Override
    public void setMaximum(int value) {
        this.scrollBar.setMaximum(value);
    }

    @Override
    public int getPageIncrement() {
        Object result = PlatformHelper.callGetter(this.scrollBar, "getPageIncrement"); //$NON-NLS-1$
        if (result != null) {
            return (int) result;
        }
        return 0;
    }

    @Override
    public void setPageIncrement(int value) {
        PlatformHelper.callSetter(this.scrollBar, "setPageIncrement", int.class, value); //$NON-NLS-1$
    }

    @Override
    public int getThumb() {
        return this.scrollBar.getThumb();
    }

    @Override
    public void setThumb(int value) {
        this.scrollBar.setThumb(value);
    }

    @Override
    public int getIncrement() {
        Object result = PlatformHelper.callGetter(this.scrollBar, "getIncrement"); //$NON-NLS-1$
        if (result != null) {
            return (int) result;
        }
        return 0;
    }

    @Override
    public void setIncrement(int value) {
        PlatformHelper.callSetter(this.scrollBar, "setIncrement", int.class, value); //$NON-NLS-1$
    }

    @Override
    public boolean getEnabled() {
        return this.scrollBar.getEnabled();
    }

    @Override
    public void setEnabled(boolean b) {
        this.scrollBar.setEnabled(b);
    }

    @Override
    public boolean getVisible() {
        return this.scrollBar.getVisible();
    }

    @Override
    public void setVisible(boolean b) {
        boolean visible = this.scrollBar.isVisible();
        this.scrollBar.setVisible(b);
        // if the scrollbar becomes invisible we fire a resize event to trigger
        // re-calculation of percentage sized columns to take the scrollbar
        // space
        if (!b && visible
                && !isDisposed()
                && !this.scrollBar.getParent().isDisposed()) {
            this.scrollBar.getParent().notifyListeners(SWT.Resize, null);
        }
    }

}
