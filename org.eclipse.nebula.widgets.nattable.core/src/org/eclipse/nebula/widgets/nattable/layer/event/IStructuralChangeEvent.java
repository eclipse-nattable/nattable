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

import java.util.Collection;

/**
 * An event indicating a structural change to the layer. A structural change is
 * defined as something that modifies the number of columns/rows in the layer or
 * their associated widths/heights.
 */
public interface IStructuralChangeEvent extends IVisualChangeEvent {

    public boolean isHorizontalStructureChanged();

    public Collection<StructuralDiff> getColumnDiffs();

    public boolean isVerticalStructureChanged();

    public Collection<StructuralDiff> getRowDiffs();

}
