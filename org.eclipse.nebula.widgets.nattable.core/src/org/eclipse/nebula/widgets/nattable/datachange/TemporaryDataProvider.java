/*******************************************************************************
 * Copyright (c) 2018 Dirk Fauth.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Dirk Fauth <dirk.fauth@googlemail.com> - initial API and implementation
 ******************************************************************************/
package org.eclipse.nebula.widgets.nattable.datachange;

/**
 * Interface to support temporary data storage without updating the backing
 * data.
 *
 * @since 1.6
 */
public interface TemporaryDataProvider {

    /**
     * Checks if this {@link TemporaryDataProvider} contains a temporary value
     * for the given position.
     * 
     * @param columnPosition
     *            The column position to check.
     * @param rowPosition
     *            The row position to check.
     * @return <code>true</code> if a temporary data value for the given
     *         position exists, <code>false</code> if not.
     */
    boolean tracksDataChange(int columnPosition, int rowPosition);

    /**
     * Return the temporary data value for the given position.
     * 
     * @param columnPosition
     *            The column position for which the data value should be
     *            returned.
     * @param rowPosition
     *            The row position for which the data value should be returned.
     * @return The data value for the given position.
     */
    Object getDataValueByPosition(int columnPosition, int rowPosition);
}
