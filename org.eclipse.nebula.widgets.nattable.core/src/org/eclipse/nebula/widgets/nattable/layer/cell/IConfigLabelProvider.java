/*****************************************************************************
 * Copyright (c) 2015 CEA LIST.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *      Dirk Fauth <dirk.fauth@googlemail.com> - Initial API and implementation
 *
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
