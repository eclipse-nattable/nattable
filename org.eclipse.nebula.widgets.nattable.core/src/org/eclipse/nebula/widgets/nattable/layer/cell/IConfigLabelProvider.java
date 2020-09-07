/*****************************************************************************
 * Copyright (c) 2015, 2020 CEA LIST.
 *
 * This program and the accompanying materials are made
 * available under the terms of the Eclipse Public License 2.0
 * which is available at https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 * Contributors:
 *      Dirk Fauth <dirk.fauth@googlemail.com> - Initial API and implementation
 *****************************************************************************/
package org.eclipse.nebula.widgets.nattable.layer.cell;

import java.util.Collection;

/**
 * Specialization of {@link IConfigLabelAccumulator} that provides information
 * about the labels that are added. This interface was introduced to support CSS
 * styling and is used to determine the children that can be used in CSS
 * selectors for NatTable styling.
 *
 * @since 1.4
 */
public interface IConfigLabelProvider extends IConfigLabelAccumulator {

    /**
     * Returns the labels that are provided by this
     * {@link IConfigLabelAccumulator}. It needs to return all labels that might
     * be applied to support the usage of corresponding selectors in NatTable
     * CSS styling.
     *
     * @return The labels that are provided by this
     *         {@link IConfigLabelAccumulator}.
     */
    Collection<String> getProvidedLabels();
}
