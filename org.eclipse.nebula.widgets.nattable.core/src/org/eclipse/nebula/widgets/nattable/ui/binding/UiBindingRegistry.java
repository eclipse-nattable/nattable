/*******************************************************************************
 * Copyright (c) 2012, 2024 Original authors and others.
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
package org.eclipse.nebula.widgets.nattable.ui.binding;

import java.util.HashMap;
import java.util.LinkedList;
import java.util.Map;

import org.eclipse.nebula.widgets.nattable.NatTable;
import org.eclipse.nebula.widgets.nattable.layer.LabelStack;
import org.eclipse.nebula.widgets.nattable.ui.action.DragModeEventHandler;
import org.eclipse.nebula.widgets.nattable.ui.action.IDragMode;
import org.eclipse.nebula.widgets.nattable.ui.action.IKeyAction;
import org.eclipse.nebula.widgets.nattable.ui.action.IMouseAction;
import org.eclipse.nebula.widgets.nattable.ui.action.MouseMoveAction;
import org.eclipse.nebula.widgets.nattable.ui.matcher.IKeyEventMatcher;
import org.eclipse.nebula.widgets.nattable.ui.matcher.IMouseEventMatcher;
import org.eclipse.nebula.widgets.nattable.ui.mode.MouseModeEventHandler;
import org.eclipse.swt.events.KeyEvent;
import org.eclipse.swt.events.KeyListener;
import org.eclipse.swt.events.MouseEvent;
import org.eclipse.swt.events.MouseListener;
import org.eclipse.swt.events.MouseMoveListener;
import org.eclipse.swt.events.MouseTrackListener;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class UiBindingRegistry implements IUiBindingRegistry {

    private static final Logger LOG = LoggerFactory.getLogger(UiBindingRegistry.class);

    private NatTable natTable;

    private LinkedList<KeyBinding> keyBindings = new LinkedList<>();

    private Map<MouseEventTypeEnum, LinkedList<MouseBinding>> mouseBindingsMap = new HashMap<>();

    private LinkedList<DragBinding> dragBindings = new LinkedList<>();

    public UiBindingRegistry(NatTable natTable) {
        this.natTable = natTable;
    }

    // Lookup /////////////////////////////////////////////////////////////////

    @Override
    public IKeyAction getKeyEventAction(KeyEvent event) {
        for (KeyBinding keyBinding : this.keyBindings) {
            if (keyBinding.getKeyEventMatcher().matches(event)) {
                return keyBinding.getAction();
            }
        }
        return null;
    }

    @Override
    public IDragMode getDragMode(MouseEvent event) {
        LabelStack regionLabels = this.natTable.getRegionLabelsByXY(event.x, event.y);

        for (DragBinding dragBinding : this.dragBindings) {
            if (dragBinding.getMouseEventMatcher().matches(this.natTable, event, regionLabels)) {
                return dragBinding.getDragMode();
            }
        }

        return null;
    }

    @Override
    public IMouseAction getMouseMoveAction(MouseEvent event) {
        return getMouseEventAction(MouseEventTypeEnum.MOUSE_MOVE, event);
    }

    @Override
    public IMouseAction getMouseDownAction(MouseEvent event) {
        return getMouseEventAction(MouseEventTypeEnum.MOUSE_DOWN, event);
    }

    @Override
    public IMouseAction getSingleClickAction(MouseEvent event) {
        return getMouseEventAction(MouseEventTypeEnum.MOUSE_SINGLE_CLICK, event);
    }

    @Override
    public IMouseAction getDoubleClickAction(MouseEvent event) {
        return getMouseEventAction(MouseEventTypeEnum.MOUSE_DOUBLE_CLICK, event);
    }

    @Override
    public IMouseAction getMouseHoverAction(MouseEvent event) {
        return getMouseEventAction(MouseEventTypeEnum.MOUSE_HOVER, event);
    }

    @Override
    public IMouseAction getMouseEnterAction(MouseEvent event) {
        return getMouseEventAction(MouseEventTypeEnum.MOUSE_ENTER, event);
    }

    @Override
    public IMouseAction getMouseExitAction(MouseEvent event) {
        return getMouseEventAction(MouseEventTypeEnum.MOUSE_EXIT, event);
    }

    // /////////////////////////////////////////////////////////////////////////

    private IMouseAction getMouseEventAction(MouseEventTypeEnum mouseEventType, MouseEvent event) {

        // TODO: This code can be made more performant by mapping mouse bindings
        // not only to the mouseEventType but
        // also to the region that they are interested in. That way, given an
        // area and an event we can narrow down the
        // list of mouse bindings that need to be searched. -- Azubuko.Obele

        try {
            LinkedList<MouseBinding> mouseEventBindings = this.mouseBindingsMap.get(mouseEventType);
            if (mouseEventBindings != null) {
                LabelStack regionLabels = this.natTable.getRegionLabelsByXY(event.x, event.y);

                for (MouseBinding mouseBinding : mouseEventBindings) {

                    if (mouseBinding.getMouseEventMatcher().matches(this.natTable, event, regionLabels)) {
                        return mouseBinding.getAction();
                    }
                }
            }
        } catch (Exception e) {
            LOG.error("Exception on retrieving a mouse event action", e); //$NON-NLS-1$
        }
        return null;
    }

    // Registration ///////////////////////////////////////////////////////////

    // Key

    /**
     * Register a {@link IKeyAction} that should be executed on
     * {@link KeyListener#keyPressed(KeyEvent)} if the given
     * {@link IKeyEventMatcher} matches.
     * <p>
     * Adds the binding at the first position, which increases the priority the
     * binding gets executed, if multiple bindings would match the condition.
     * </p>
     *
     * @param keyMatcher
     *            The {@link IKeyEventMatcher} for which the binding should be
     *            registered.
     * @param action
     *            The {@link IKeyAction} that should be executed on
     *            {@link KeyListener#keyPressed(KeyEvent)} if the
     *            {@link IKeyEventMatcher} matches.
     */
    public void registerFirstKeyBinding(IKeyEventMatcher keyMatcher, IKeyAction action) {
        this.keyBindings.addFirst(new KeyBinding(keyMatcher, action));
    }

    /**
     * Register a {@link IKeyAction} that should be executed on
     * {@link KeyListener#keyPressed(KeyEvent)} if the given
     * {@link IKeyEventMatcher} matches.
     *
     * @param keyMatcher
     *            The {@link IKeyEventMatcher} for which the binding should be
     *            registered.
     * @param action
     *            The {@link IKeyAction} that should be executed on
     *            {@link KeyListener#keyPressed(KeyEvent)} if the
     *            {@link IKeyEventMatcher} matches.
     */
    public void registerKeyBinding(IKeyEventMatcher keyMatcher, IKeyAction action) {
        this.keyBindings.addLast(new KeyBinding(keyMatcher, action));
    }

    /**
     * Removes the key binding for the given {@link IKeyEventMatcher}.
     *
     * @param keyMatcher
     *            The {@link IKeyEventMatcher} for which the binding should be
     *            removed.
     */
    public void unregisterKeyBinding(IKeyEventMatcher keyMatcher) {
        for (KeyBinding keyBinding : this.keyBindings) {
            if (keyBinding.getKeyEventMatcher().equals(keyMatcher)) {
                this.keyBindings.remove(keyBinding);
                return;
            }
        }
    }

    // Drag

    /**
     * Register a {@link IDragMode} that should be executed on a mouse drag
     * operation if the given {@link IMouseEventMatcher} matches. A drag
     * operation is a {@link MouseListener#mouseDown(MouseEvent)} followed by a
     * {@link MouseMoveListener#mouseMove(MouseEvent)} and finished with a
     * {@link MouseListener#mouseUp(MouseEvent)}.
     * <p>
     * Adds the binding at the first position, which increases the priority the
     * binding gets executed, if multiple bindings would match the condition.
     * </p>
     *
     * @param mouseEventMatcher
     *            The {@link IMouseEventMatcher} for which the binding should be
     *            registered.
     * @param dragMode
     *            The {@link IDragMode} that should be executed on a mouse drag
     *            operation if the {@link IMouseEventMatcher} matches.
     *
     * @see MouseModeEventHandler
     * @see DragModeEventHandler
     */
    public void registerFirstMouseDragMode(IMouseEventMatcher mouseEventMatcher, IDragMode dragMode) {
        this.dragBindings.addFirst(new DragBinding(mouseEventMatcher, dragMode));
    }

    /**
     * Register a {@link IDragMode} that should be executed on a mouse drag
     * operation if the given {@link IMouseEventMatcher} matches. A drag
     * operation is a {@link MouseListener#mouseDown(MouseEvent)} followed by a
     * {@link MouseMoveListener#mouseMove(MouseEvent)} and finished with a
     * {@link MouseListener#mouseUp(MouseEvent)}.
     * <p>
     * Adds the binding at the first position, which increases the priority the
     * binding gets executed, if multiple bindings would match the condition.
     * </p>
     *
     * @param mouseEventMatcher
     *            The {@link IMouseEventMatcher} for which the binding should be
     *            registered.
     * @param dragMode
     *            The {@link IDragMode} that should be executed on a mouse drag
     *            operation if the {@link IMouseEventMatcher} matches.
     *
     * @see MouseModeEventHandler
     * @see DragModeEventHandler
     */
    public void registerMouseDragMode(IMouseEventMatcher mouseEventMatcher, IDragMode dragMode) {
        this.dragBindings.addLast(new DragBinding(mouseEventMatcher, dragMode));
    }

    /**
     * Removes the mouse drag binding for the given {@link IMouseEventMatcher}.
     *
     * @param mouseEventMatcher
     *            The {@link IMouseEventMatcher} for which the binding should be
     *            removed.
     */
    public void unregisterMouseDragMode(IMouseEventMatcher mouseEventMatcher) {
        for (DragBinding dragBinding : this.dragBindings) {
            if (dragBinding.getMouseEventMatcher().equals(mouseEventMatcher)) {
                this.dragBindings.remove(dragBinding);
                return;
            }
        }
    }

    // Mouse move

    /**
     * Register a {@link IMouseAction} that should be executed on
     * {@link MouseMoveListener#mouseMove(MouseEvent)} if the given
     * {@link IMouseEventMatcher} matches.
     * <p>
     * Adds the binding at the first position, which increases the priority the
     * binding gets executed, if multiple bindings would match the condition.
     * </p>
     *
     * @param mouseEventMatcher
     *            The {@link IMouseEventMatcher} for which the binding should be
     *            registered.
     * @param action
     *            The {@link IMouseAction} that should be executed on
     *            {@link MouseMoveListener#mouseMove(MouseEvent)} if the
     *            {@link IMouseEventMatcher} matches.
     *
     * @deprecated Use
     *             {@link #registerFirstMouseMoveBinding(IMouseEventMatcher, IMouseAction, IMouseAction)}
     *             to directly configure enter/exit behavior on movement.
     */
    @Deprecated
    public void registerFirstMouseMoveBinding(IMouseEventMatcher mouseEventMatcher, IMouseAction action) {
        registerMouseBinding(true, MouseEventTypeEnum.MOUSE_MOVE, mouseEventMatcher, action);
    }

    /**
     * Register a {@link IMouseAction} that should be executed on
     * {@link MouseMoveListener#mouseMove(MouseEvent)} if the given
     * {@link IMouseEventMatcher} matches.
     *
     * @param mouseEventMatcher
     *            The {@link IMouseEventMatcher} for which the binding should be
     *            registered.
     * @param action
     *            The {@link IMouseAction} that should be executed on
     *            {@link MouseMoveListener#mouseMove(MouseEvent)} if the
     *            {@link IMouseEventMatcher} matches.
     *
     * @deprecated Use
     *             {@link #registerFirstMouseMoveBinding(IMouseEventMatcher, IMouseAction, IMouseAction)}
     *             to directly configure enter/exit behavior on movement.
     */
    @Deprecated
    public void registerMouseMoveBinding(IMouseEventMatcher mouseEventMatcher, IMouseAction action) {
        registerMouseBinding(false, MouseEventTypeEnum.MOUSE_MOVE, mouseEventMatcher, action);
    }

    /**
     * Register a {@link IMouseAction} that should be executed on
     * {@link MouseMoveListener#mouseMove(MouseEvent)} if the given
     * {@link IMouseEventMatcher} matches. Allows to configure the
     * {@link IMouseAction} that should be executed if the
     * {@link IMouseEventMatcher} matches the first time (enter) and the
     * {@link IMouseAction} that should be executed if the
     * {@link IMouseEventMatcher} does not match anymore (exit).
     * <p>
     * Adds the binding at the first position, which increases the priority the
     * binding gets executed, if multiple bindings would match the condition.
     * </p>
     *
     * @param mouseEventMatcher
     *            The {@link IMouseEventMatcher} for which the binding should be
     *            registered.
     * @param entryAction
     *            The {@link IMouseAction} that should be executed on
     *            {@link MouseMoveListener#mouseMove(MouseEvent)} if the
     *            {@link IMouseEventMatcher} matches.
     * @param exitAction
     *            The {@link IMouseAction} that should be executed on
     *            {@link MouseMoveListener#mouseMove(MouseEvent)} if the
     *            {@link IMouseEventMatcher} does not match anymore.
     * 
     * @since 2.4
     */
    public void registerFirstMouseMoveBinding(IMouseEventMatcher mouseEventMatcher, IMouseAction entryAction, IMouseAction exitAction) {
        registerFirstMouseMoveBinding(mouseEventMatcher, entryAction, exitAction, true);
    }

    /**
     * Register a {@link IMouseAction} that should be executed on
     * {@link MouseMoveListener#mouseMove(MouseEvent)} if the given
     * {@link IMouseEventMatcher} matches. Allows to configure the
     * {@link IMouseAction} that should be executed if the
     * {@link IMouseEventMatcher} matches the first time (enter) and the
     * {@link IMouseAction} that should be executed if the
     * {@link IMouseEventMatcher} does not match anymore (exit).
     *
     * @param mouseEventMatcher
     *            The {@link IMouseEventMatcher} for which the binding should be
     *            registered.
     * @param entryAction
     *            The {@link IMouseAction} that should be executed on
     *            {@link MouseMoveListener#mouseMove(MouseEvent)} if the
     *            {@link IMouseEventMatcher} matches.
     * @param exitAction
     *            The {@link IMouseAction} that should be executed on
     *            {@link MouseMoveListener#mouseMove(MouseEvent)} if the
     *            {@link IMouseEventMatcher} does not match anymore.
     * 
     * @since 2.4
     */
    public void registerMouseMoveBinding(IMouseEventMatcher mouseEventMatcher, IMouseAction entryAction, IMouseAction exitAction) {
        registerMouseMoveBinding(mouseEventMatcher, entryAction, exitAction, true);
    }

    /**
     * Register a {@link IMouseAction} that should be executed on
     * {@link MouseMoveListener#mouseMove(MouseEvent)} if the given
     * {@link IMouseEventMatcher} matches. Allows to configure the
     * {@link IMouseAction} that should be executed if the
     * {@link IMouseEventMatcher} matches the first time (enter) and the
     * {@link IMouseAction} that should be executed if the
     * {@link IMouseEventMatcher} does not match anymore (exit).
     * <p>
     * Adds the binding at the first position, which increases the priority the
     * binding gets executed, if multiple bindings would match the condition.
     * </p>
     *
     * @param mouseEventMatcher
     *            The {@link IMouseEventMatcher} for which the binding should be
     *            registered.
     * @param entryAction
     *            The {@link IMouseAction} that should be executed on
     *            {@link MouseMoveListener#mouseMove(MouseEvent)} if the
     *            {@link IMouseEventMatcher} matches.
     * @param exitAction
     *            The {@link IMouseAction} that should be executed on
     *            {@link MouseMoveListener#mouseMove(MouseEvent)} if the
     *            {@link IMouseEventMatcher} does not match anymore.
     * @param reexecuteEntryAction
     *            <code>true</code> if the entry action should be executed
     *            everytime as long as the {@link IMouseEventMatcher} matches,
     *            <code>false</code> if the entry action should only be executed
     *            on enter.
     * 
     * @since 2.4
     */
    public void registerFirstMouseMoveBinding(IMouseEventMatcher mouseEventMatcher, IMouseAction entryAction, IMouseAction exitAction, boolean reexecuteEntryAction) {
        MouseMoveAction action = new MouseMoveAction(mouseEventMatcher, entryAction, exitAction, reexecuteEntryAction);
        registerMouseBinding(true, MouseEventTypeEnum.MOUSE_MOVE, mouseEventMatcher, action);
    }

    /**
     * Register a {@link IMouseAction} that should be executed on
     * {@link MouseMoveListener#mouseMove(MouseEvent)} if the given
     * {@link IMouseEventMatcher} matches. Allows to configure the
     * {@link IMouseAction} that should be executed if the
     * {@link IMouseEventMatcher} matches the first time (enter) and the
     * {@link IMouseAction} that should be executed if the
     * {@link IMouseEventMatcher} does not match anymore (exit).
     *
     * @param mouseEventMatcher
     *            The {@link IMouseEventMatcher} for which the binding should be
     *            registered.
     * @param entryAction
     *            The {@link IMouseAction} that should be executed on
     *            {@link MouseMoveListener#mouseMove(MouseEvent)} if the
     *            {@link IMouseEventMatcher} matches.
     * @param exitAction
     *            The {@link IMouseAction} that should be executed on
     *            {@link MouseMoveListener#mouseMove(MouseEvent)} if the
     *            {@link IMouseEventMatcher} does not match anymore.
     * @param reexecuteEntryAction
     *            <code>true</code> if the entry action should be executed
     *            everytime as long as the {@link IMouseEventMatcher} matches,
     *            <code>false</code> if the entry action should only be executed
     *            on enter.
     * 
     * @since 2.4
     */
    public void registerMouseMoveBinding(IMouseEventMatcher mouseEventMatcher, IMouseAction entryAction, IMouseAction exitAction, boolean reexecuteEntryAction) {
        MouseMoveAction action = new MouseMoveAction(mouseEventMatcher, entryAction, exitAction, reexecuteEntryAction);
        registerMouseBinding(false, MouseEventTypeEnum.MOUSE_MOVE, mouseEventMatcher, action);
    }

    /**
     * Removes the mouse move binding for the given {@link IMouseEventMatcher}.
     *
     * @param mouseEventMatcher
     *            The {@link IMouseEventMatcher} for which the binding should be
     *            removed.
     */
    public void unregisterMouseMoveBinding(IMouseEventMatcher mouseEventMatcher) {
        unregisterMouseBinding(MouseEventTypeEnum.MOUSE_MOVE, mouseEventMatcher);
    }

    // Mouse down

    /**
     * Register a {@link IMouseAction} that should be executed on
     * {@link MouseListener#mouseDown(MouseEvent)} if the given
     * {@link IMouseEventMatcher} matches.
     * <p>
     * Adds the binding at the first position, which increases the priority the
     * binding gets executed, if multiple bindings would match the condition.
     * </p>
     *
     * @param mouseEventMatcher
     *            The {@link IMouseEventMatcher} for which the binding should be
     *            registered.
     * @param action
     *            The {@link IMouseAction} that should be executed on
     *            {@link MouseListener#mouseDown(MouseEvent)} if the
     *            {@link IMouseEventMatcher} matches.
     */
    public void registerFirstMouseDownBinding(IMouseEventMatcher mouseEventMatcher, IMouseAction action) {
        registerMouseBinding(true, MouseEventTypeEnum.MOUSE_DOWN, mouseEventMatcher, action);
    }

    /**
     * Register a {@link IMouseAction} that should be executed on
     * {@link MouseListener#mouseDown(MouseEvent)} if the given
     * {@link IMouseEventMatcher} matches.
     *
     * @param mouseEventMatcher
     *            The {@link IMouseEventMatcher} for which the binding should be
     *            registered.
     * @param action
     *            The {@link IMouseAction} that should be executed on
     *            {@link MouseListener#mouseDown(MouseEvent)} if the
     *            {@link IMouseEventMatcher} matches.
     */
    public void registerMouseDownBinding(IMouseEventMatcher mouseEventMatcher, IMouseAction action) {
        registerMouseBinding(false, MouseEventTypeEnum.MOUSE_DOWN, mouseEventMatcher, action);
    }

    /**
     * Removes the mouse down binding for the given {@link IMouseEventMatcher}.
     *
     * @param mouseEventMatcher
     *            The {@link IMouseEventMatcher} for which the binding should be
     *            removed.
     */
    public void unregisterMouseDownBinding(IMouseEventMatcher mouseEventMatcher) {
        unregisterMouseBinding(MouseEventTypeEnum.MOUSE_DOWN, mouseEventMatcher);
    }

    // Single click

    /**
     * Register a {@link IMouseAction} that should be executed on a single click
     * if the given {@link IMouseEventMatcher} matches. A single click is a
     * {@link MouseListener#mouseDown(MouseEvent)} followed by a
     * {@link MouseListener#mouseUp(MouseEvent)} and no movement or a second
     * click happens in the meanwhile.
     * <p>
     * Adds the binding at the first position, which increases the priority the
     * binding gets executed, if multiple bindings would match the condition.
     * </p>
     *
     * @param mouseEventMatcher
     *            The {@link IMouseEventMatcher} for which the binding should be
     *            registered.
     * @param action
     *            The {@link IMouseAction} that should be executed on a single
     *            click if the {@link IMouseEventMatcher} matches.
     *
     * @see MouseModeEventHandler
     */
    public void registerFirstSingleClickBinding(IMouseEventMatcher mouseEventMatcher, IMouseAction action) {
        registerMouseBinding(true, MouseEventTypeEnum.MOUSE_SINGLE_CLICK, mouseEventMatcher, action);
    }

    /**
     * Register a {@link IMouseAction} that should be executed on a single click
     * if the given {@link IMouseEventMatcher} matches. A single click is a
     * {@link MouseListener#mouseDown(MouseEvent)} followed by a
     * {@link MouseListener#mouseUp(MouseEvent)} and no movement or a second
     * click happens in the meanwhile.
     *
     * @param mouseEventMatcher
     *            The {@link IMouseEventMatcher} for which the binding should be
     *            registered.
     * @param action
     *            The {@link IMouseAction} that should be executed on a single
     *            click if the {@link IMouseEventMatcher} matches.
     *
     * @see MouseModeEventHandler
     */
    public void registerSingleClickBinding(IMouseEventMatcher mouseEventMatcher, IMouseAction action) {
        registerMouseBinding(false, MouseEventTypeEnum.MOUSE_SINGLE_CLICK, mouseEventMatcher, action);
    }

    /**
     * Removes the mouse single click binding for the given
     * {@link IMouseEventMatcher}.
     *
     * @param mouseEventMatcher
     *            The {@link IMouseEventMatcher} for which the binding should be
     *            removed.
     */
    public void unregisterSingleClickBinding(IMouseEventMatcher mouseEventMatcher) {
        unregisterMouseBinding(MouseEventTypeEnum.MOUSE_SINGLE_CLICK, mouseEventMatcher);
    }

    // Double click

    /**
     * Register a {@link IMouseAction} that should be executed on
     * {@link MouseListener#mouseDoubleClick(MouseEvent)} if the given
     * {@link IMouseEventMatcher} matches.
     * <p>
     * Adds the binding at the first position, which increases the priority the
     * binding gets executed, if multiple bindings would match the condition.
     * </p>
     *
     * @param mouseEventMatcher
     *            The {@link IMouseEventMatcher} for which the binding should be
     *            registered.
     * @param action
     *            The {@link IMouseAction} that should be executed on
     *            {@link MouseListener#mouseDoubleClick(MouseEvent)} if the
     *            {@link IMouseEventMatcher} matches.
     *
     * @see MouseModeEventHandler
     */
    public void registerFirstDoubleClickBinding(IMouseEventMatcher mouseEventMatcher, IMouseAction action) {
        registerMouseBinding(true, MouseEventTypeEnum.MOUSE_DOUBLE_CLICK, mouseEventMatcher, action);
    }

    /**
     * Register a {@link IMouseAction} that should be executed on
     * {@link MouseListener#mouseDoubleClick(MouseEvent)} if the given
     * {@link IMouseEventMatcher} matches.
     *
     * @param mouseEventMatcher
     *            The {@link IMouseEventMatcher} for which the binding should be
     *            registered.
     * @param action
     *            The {@link IMouseAction} that should be executed on
     *            {@link MouseListener#mouseDoubleClick(MouseEvent)} if the
     *            {@link IMouseEventMatcher} matches.
     *
     * @see MouseModeEventHandler
     */
    public void registerDoubleClickBinding(IMouseEventMatcher mouseEventMatcher, IMouseAction action) {
        registerMouseBinding(false, MouseEventTypeEnum.MOUSE_DOUBLE_CLICK, mouseEventMatcher, action);
    }

    /**
     * Removes the mouse double click binding for the given
     * {@link IMouseEventMatcher}.
     *
     * @param mouseEventMatcher
     *            The {@link IMouseEventMatcher} for which the binding should be
     *            removed.
     */
    public void unregisterDoubleClickBinding(IMouseEventMatcher mouseEventMatcher) {
        unregisterMouseBinding(MouseEventTypeEnum.MOUSE_DOUBLE_CLICK, mouseEventMatcher);
    }

    // Mouse hover

    /**
     * Register a {@link IMouseAction} that should be executed on
     * {@link MouseTrackListener#mouseHover(MouseEvent)} if the given
     * {@link IMouseEventMatcher} matches.
     * <p>
     * Adds the binding at the first position, which increases the priority the
     * binding gets executed, if multiple bindings would match the condition.
     * </p>
     *
     * @param mouseEventMatcher
     *            The {@link IMouseEventMatcher} for which the binding should be
     *            registered.
     * @param action
     *            The {@link IMouseAction} that should be executed on
     *            {@link MouseTrackListener#mouseHover(MouseEvent)} if the
     *            {@link IMouseEventMatcher} matches.
     */
    public void registerFirstMouseHoverBinding(IMouseEventMatcher mouseEventMatcher, IMouseAction action) {
        registerMouseBinding(true, MouseEventTypeEnum.MOUSE_HOVER, mouseEventMatcher, action);
    }

    /**
     * Register a {@link IMouseAction} that should be executed on
     * {@link MouseTrackListener#mouseHover(MouseEvent)} if the given
     * {@link IMouseEventMatcher} matches.
     *
     * @param mouseEventMatcher
     *            The {@link IMouseEventMatcher} for which the binding should be
     *            registered.
     * @param action
     *            The {@link IMouseAction} that should be executed on
     *            {@link MouseTrackListener#mouseHover(MouseEvent)} if the
     *            {@link IMouseEventMatcher} matches.
     */
    public void registerMouseHoverBinding(IMouseEventMatcher mouseEventMatcher, IMouseAction action) {
        registerMouseBinding(false, MouseEventTypeEnum.MOUSE_HOVER, mouseEventMatcher, action);
    }

    /**
     * Removes the mouse hover binding for the given {@link IMouseEventMatcher}.
     *
     * @param mouseEventMatcher
     *            The {@link IMouseEventMatcher} for which the binding should be
     *            removed.
     */
    public void unregisterMouseHoverBinding(IMouseEventMatcher mouseEventMatcher) {
        unregisterMouseBinding(MouseEventTypeEnum.MOUSE_HOVER, mouseEventMatcher);
    }

    // Mouse enter

    /**
     * Register a {@link IMouseAction} that should be executed on
     * {@link MouseTrackListener#mouseEnter(MouseEvent)} if the given
     * {@link IMouseEventMatcher} matches.
     * <p>
     * Adds the binding at the first position, which increases the priority the
     * binding gets executed, if multiple bindings would match the condition.
     * </p>
     *
     * @param mouseEventMatcher
     *            The {@link IMouseEventMatcher} for which the binding should be
     *            registered.
     * @param action
     *            The {@link IMouseAction} that should be executed on
     *            {@link MouseTrackListener#mouseEnter(MouseEvent)} if the
     *            {@link IMouseEventMatcher} matches.
     */
    public void registerFirstMouseEnterBinding(IMouseEventMatcher mouseEventMatcher, IMouseAction action) {
        registerMouseBinding(true, MouseEventTypeEnum.MOUSE_ENTER, mouseEventMatcher, action);
    }

    /**
     * Register a {@link IMouseAction} that should be executed on
     * {@link MouseTrackListener#mouseEnter(MouseEvent)} if the given
     * {@link IMouseEventMatcher} matches.
     *
     * @param mouseEventMatcher
     *            The {@link IMouseEventMatcher} for which the binding should be
     *            registered.
     * @param action
     *            The {@link IMouseAction} that should be executed on
     *            {@link MouseTrackListener#mouseEnter(MouseEvent)} if the
     *            {@link IMouseEventMatcher} matches.
     */
    public void registerMouseEnterBinding(IMouseEventMatcher mouseEventMatcher, IMouseAction action) {
        registerMouseBinding(false, MouseEventTypeEnum.MOUSE_ENTER, mouseEventMatcher, action);
    }

    /**
     * Removes the mouse enter binding for the given {@link IMouseEventMatcher}.
     *
     * @param mouseEventMatcher
     *            The {@link IMouseEventMatcher} for which the binding should be
     *            removed.
     */
    public void unregisterMouseEnterBinding(IMouseEventMatcher mouseEventMatcher) {
        unregisterMouseBinding(MouseEventTypeEnum.MOUSE_ENTER, mouseEventMatcher);
    }

    // Mouse exit

    /**
     * Register a {@link IMouseAction} that should be executed on
     * {@link MouseTrackListener#mouseExit(MouseEvent)} if the given
     * {@link IMouseEventMatcher} matches.
     * <p>
     * Adds the binding at the first position, which increases the priority the
     * binding gets executed, if multiple bindings would match the condition.
     * </p>
     *
     * @param mouseEventMatcher
     *            The {@link IMouseEventMatcher} for which the binding should be
     *            registered.
     * @param action
     *            The {@link IMouseAction} that should be executed on
     *            {@link MouseTrackListener#mouseExit(MouseEvent)} if the
     *            {@link IMouseEventMatcher} matches.
     */
    public void registerFirstMouseExitBinding(IMouseEventMatcher mouseEventMatcher, IMouseAction action) {
        registerMouseBinding(true, MouseEventTypeEnum.MOUSE_EXIT, mouseEventMatcher, action);
    }

    /**
     * Register a {@link IMouseAction} that should be executed on
     * {@link MouseTrackListener#mouseExit(MouseEvent)} if the given
     * {@link IMouseEventMatcher} matches.
     *
     * @param mouseEventMatcher
     *            The {@link IMouseEventMatcher} for which the binding should be
     *            registered.
     * @param action
     *            The {@link IMouseAction} that should be executed on
     *            {@link MouseTrackListener#mouseExit(MouseEvent)} if the
     *            {@link IMouseEventMatcher} matches.
     */
    public void registerMouseExitBinding(IMouseEventMatcher mouseEventMatcher, IMouseAction action) {
        registerMouseBinding(false, MouseEventTypeEnum.MOUSE_EXIT, mouseEventMatcher, action);
    }

    /**
     * Removes the mouse exit binding for the given {@link IMouseEventMatcher}.
     *
     * @param mouseEventMatcher
     *            The {@link IMouseEventMatcher} for which the binding should be
     *            removed.
     */
    public void unregisterMouseExitBinding(IMouseEventMatcher mouseEventMatcher) {
        unregisterMouseBinding(MouseEventTypeEnum.MOUSE_EXIT, mouseEventMatcher);
    }

    // /////////////////////////////////////////////////////////////////////////

    private void registerMouseBinding(boolean first, MouseEventTypeEnum mouseEventType, IMouseEventMatcher mouseEventMatcher, IMouseAction action) {
        LinkedList<MouseBinding> mouseEventBindings = this.mouseBindingsMap.computeIfAbsent(mouseEventType, type -> new LinkedList<>());
        if (first) {
            mouseEventBindings.addFirst(new MouseBinding(mouseEventMatcher, action));
        } else {
            mouseEventBindings.addLast(new MouseBinding(mouseEventMatcher, action));
        }
    }

    private void unregisterMouseBinding(MouseEventTypeEnum mouseEventType, IMouseEventMatcher mouseEventMatcher) {
        LinkedList<MouseBinding> mouseBindings = this.mouseBindingsMap.get(mouseEventType);
        if (mouseBindings != null) {
            for (MouseBinding mouseBinding : mouseBindings) {
                if (mouseBinding.getMouseEventMatcher().equals(mouseEventMatcher)) {
                    mouseBindings.remove(mouseBinding);
                    return;
                }
            }
        }
    }

    private enum MouseEventTypeEnum {
        MOUSE_DOWN, MOUSE_MOVE, MOUSE_SINGLE_CLICK, MOUSE_DOUBLE_CLICK, MOUSE_HOVER, MOUSE_ENTER, MOUSE_EXIT
    }

}
