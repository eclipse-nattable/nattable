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
package org.eclipse.nebula.widgets.nattable.selection;

import java.util.List;

public interface IRowSelectionModel<R> extends ISelectionModel {

    /**
     * Expose the underlying row objects
     *
     * @return The selected row objects.
     */
    public List<R> getSelectedRowObjects();

    /**
     * Removes the selected row object from the selection
     *
     * @param rowObject
     */
    public void clearSelection(R rowObject);

}
