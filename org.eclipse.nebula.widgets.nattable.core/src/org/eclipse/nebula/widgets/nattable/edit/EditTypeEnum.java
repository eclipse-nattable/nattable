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
package org.eclipse.nebula.widgets.nattable.edit;

/**
 * Enumeration type for specifying how a value should be processed that was
 * entered to an editor by dialog.
 */
public enum EditTypeEnum {

    /**
     * Use the value entered into the editor without any further transformation.
     */
    SET,
    /**
     * Use the value entered into the editor to increase the value that is
     * currently set in the data model.
     */
    INCREASE,
    /**
     * Use the value entered into the editor to decrease the value that is
     * currently set in the data model.
     */
    DECREASE,
    /**
     * Use the value entered into the editor to adjust the value that is
     * currently set in the data model. Using this edit type will take the
     * leading sign into account, so negative entered values will decrease the
     * current value in the data model, positive values will increase it.
     */
    ADJUST

}
