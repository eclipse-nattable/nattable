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
package org.eclipse.nebula.widgets.nattable.edit.command;

import org.eclipse.nebula.widgets.nattable.command.AbstractContextFreeCommand;
import org.eclipse.nebula.widgets.nattable.config.IConfigRegistry;
import org.eclipse.swt.widgets.Composite;

/**
 * Command that will trigger activating the edit mode for the current selection.
 * The corresponding command handler is responsible for determining if
 * activation should proceed because of race conditions like e.g. the cell is
 * configured to be editable or another editor is active containing an invalid
 * data.
 */
public class EditSelectionCommand extends AbstractContextFreeCommand {

    /**
     * The {@link IConfigRegistry} containing the configuration of the current
     * NatTable instance the command should be executed for.
     * <p>
     * This is necessary because the edit controllers in the current
     * architecture are not aware of the instance they are running in.
     */
    private final IConfigRegistry configRegistry;
    /**
     * The Character represented by the key that was typed in case this command
     * should be executed because of typing a letter or digit key. Can be
     * <code>null</code> if this command should be executed because of a pressed
     * control key (like F2) or a programmatical execution.
     */
    private final Character character;
    /**
     * The parent Composite, needed for the creation of the editor control.
     */
    private final Composite parent;
    /**
     * Flag to determine whether this command was triggered by traversal or not.
     */
    private final boolean byTraversal;

    /**
     * @param parent
     *            The parent Composite, needed for the creation of the editor
     *            control.
     * @param configRegistry
     *            The {@link IConfigRegistry} containing the configuration of
     *            the current NatTable instance the command should be executed
     *            for. This is necessary because the edit controllers in the
     *            current architecture are not aware of the instance they are
     *            running in.
     */
    public EditSelectionCommand(Composite parent, IConfigRegistry configRegistry) {
        this(parent, configRegistry, null);
    }

    /**
     * @param parent
     *            The parent Composite, needed for the creation of the editor
     *            control.
     * @param configRegistry
     *            The {@link IConfigRegistry} containing the configuration of
     *            the current NatTable instance the command should be executed
     *            for. This is necessary because the edit controllers in the
     *            current architecture are not aware of the instance they are
     *            running in.
     * @param character
     *            The Character represented by the key that was typed in case
     *            this command should be executed because of typing a letter or
     *            digit key. Can be <code>null</code> if this command should be
     *            executed because of a pressed control key (like F2) or a
     *            programmatical execution.
     */
    public EditSelectionCommand(Composite parent,
            IConfigRegistry configRegistry, Character character) {
        this(parent, configRegistry, character, false);
    }

    public EditSelectionCommand(Composite parent,
            IConfigRegistry configRegistry, boolean byTraversal) {
        this(parent, configRegistry, null, byTraversal);
    }

    public EditSelectionCommand(Composite parent,
            IConfigRegistry configRegistry, Character character,
            boolean byTraversal) {
        this.parent = parent;
        this.configRegistry = configRegistry;
        this.character = character;
        this.byTraversal = byTraversal;
    }

    /**
     * @return The {@link IConfigRegistry} containing the configuration of the
     *         current NatTable instance the command should be executed for.
     */
    public IConfigRegistry getConfigRegistry() {
        return this.configRegistry;
    }

    /**
     * @return The Character represented by the key that was typed in case this
     *         command should be executed because of typing a letter or digit
     *         key. Can be <code>null</code> if this command should be executed
     *         because of a pressed control key (like F2) or a programmatical
     *         execution.
     */
    public Character getCharacter() {
        return this.character;
    }

    /**
     * @return The parent Composite, needed for the creation of the editor
     *         control.
     */
    public Composite getParent() {
        return this.parent;
    }

    /**
     * @return <code>true</code> if this command was triggered by traversal,
     *         <code>false</code> if it was triggered otherwise (e.g. pressing
     *         F2)
     */
    public boolean isByTraversal() {
        return this.byTraversal;
    }

}
