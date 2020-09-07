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
package org.eclipse.nebula.widgets.nattable.dataset.pricing.valuegenerator;

import org.eclipse.nebula.widgets.nattable.dataset.valuegenerator.AbstractListValueGenerator;

public class BidAskTypeValueGenerator extends AbstractListValueGenerator<String> {

    public BidAskTypeValueGenerator() {
        super(new String[] { "Bid-BA", "" });
    }

}
