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
package org.eclipse.nebula.widgets.nattable.painter;

import org.eclipse.nebula.widgets.nattable.NatTable;
import org.eclipse.nebula.widgets.nattable.layer.ILayer;
import org.eclipse.swt.graphics.GC;

/**
 * An overlay painter is given a chance to paint the canvas once the layers have
 * finished rendering.
 *
 * @see NatTable#addOverlayPainter(IOverlayPainter)
 */
public interface IOverlayPainter {

    public void paintOverlay(GC gc, ILayer layer);

}
