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

import java.text.DecimalFormat;
import java.util.Random;

import org.eclipse.nebula.widgets.nattable.dataset.valuegenerator.UniqueLongValueGenerator;

public class IsinValueGenerator extends UniqueLongValueGenerator {

    private DecimalFormat format = new DecimalFormat("0000000000");

    private String[] prefixes = new String[] { "DE", "FR", "IT", "XS" };

    @Override
    public Object newValue(Random random) {
        return this.prefixes[random.nextInt(this.prefixes.length)]
                + this.format.format(super.newValue(random));
    }

}
