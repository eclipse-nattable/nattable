/*******************************************************************************
 * Copyright (c) Sep 3, 2013 esp and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    esp - initial API and implementation
 *******************************************************************************/
package org.eclipse.nebula.widgets.nattable.viewport;

import org.eclipse.swt.widgets.Listener;

/**
 * Interface to abstract control of either an SWT ScrollBar or Slider.
 */
public interface IScroller<T> {

    T getUnderlying();

    boolean isDisposed();

    void addListener(int eventType, Listener listener);

    void removeListener(int eventType, Listener listener);

    int getSelection();

    void setSelection(int value);

    int getMaximum();

    void setMaximum(int value);

    int getPageIncrement();

    void setPageIncrement(int value);

    int getThumb();

    void setThumb(int value);

    int getIncrement();

    void setIncrement(int value);

    boolean getEnabled();

    void setEnabled(boolean b);

    boolean getVisible();

    void setVisible(boolean b);

}
