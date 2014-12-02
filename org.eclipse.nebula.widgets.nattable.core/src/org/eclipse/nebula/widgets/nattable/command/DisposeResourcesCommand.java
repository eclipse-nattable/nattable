/*******************************************************************************
 * Copyright (c) 2012, 2013 Original authors and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Original authors and others - initial API and implementation
 ******************************************************************************/
package org.eclipse.nebula.widgets.nattable.command;

import org.eclipse.nebula.widgets.nattable.NatTable;

/**
 * Command fired by {@link NatTable} just before it is disposed. This command
 * can be handled by layers which need to dispose resources (to avoid memory
 * leaks).
 *
 * See GlazedListsEventLayer
 */
public class DisposeResourcesCommand extends AbstractContextFreeCommand {

}
