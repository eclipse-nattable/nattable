/*******************************************************************************
 * Copyright (c) 2012 Original authors and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 * 
 * Contributors:
 *     Original authors and others - initial API and implementation
 ******************************************************************************/
package org.eclipse.nebula.widgets.nattable.extension.builder.configuration;

import org.eclipse.nebula.widgets.nattable.config.DefaultNatTableStyleConfiguration;
import org.eclipse.nebula.widgets.nattable.extension.builder.model.TableStyle;


public class TableStyleConfiguration extends DefaultNatTableStyleConfiguration {

	public TableStyleConfiguration(TableStyle tableProperties) {
		super.font = tableProperties.tableFont;
		super.hAlign = tableProperties.defaultHorizontalAlign;
		super.vAlign = tableProperties.defaultVerticalAlign;
	}
}
