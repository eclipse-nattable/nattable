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
package org.eclipse.nebula.widgets.nattable.layer.event;

import org.eclipse.nebula.widgets.nattable.layer.ILayer;

/**
 * General event indicating that columns cached by the layers need refreshing.
 *
 * Note: As opposed to the the {@link ColumnStructuralChangeEvent} this event
 * does not indicate the specific columns which have changed.
 */
public class ColumnStructuralRefreshEvent extends StructuralRefreshEvent {

    public ColumnStructuralRefreshEvent(ILayer layer) {
        super(layer);
    }

    @Override
    public boolean isHorizontalStructureChanged() {
        return true;
    }

    @Override
    public boolean isVerticalStructureChanged() {
        return false;
    }
}
