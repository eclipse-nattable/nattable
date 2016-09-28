/*******************************************************************************
 * Copyright (c) 2012, 2016 Original authors and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Original authors and others - initial API and implementation
 *     Dirk Fauth <dirk.fauth@googlemail.com> - made commandHandlers and eventHandlers
 *                                              visible to subclasses for testing
 *     Dirk Fauth <dirk.fauth@googlemail.com> - Bug 447145
 ******************************************************************************/
package org.eclipse.nebula.widgets.nattable.layer;

import java.util.Collection;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.Set;
import java.util.concurrent.locks.ReadWriteLock;
import java.util.concurrent.locks.ReentrantReadWriteLock;

import org.eclipse.nebula.widgets.nattable.command.ILayerCommand;
import org.eclipse.nebula.widgets.nattable.command.ILayerCommandHandler;
import org.eclipse.nebula.widgets.nattable.config.CellConfigAttributes;
import org.eclipse.nebula.widgets.nattable.config.ConfigRegistry;
import org.eclipse.nebula.widgets.nattable.config.IConfigRegistry;
import org.eclipse.nebula.widgets.nattable.config.IConfiguration;
import org.eclipse.nebula.widgets.nattable.layer.cell.IConfigLabelAccumulator;
import org.eclipse.nebula.widgets.nattable.layer.cell.IConfigLabelProvider;
import org.eclipse.nebula.widgets.nattable.layer.cell.ILayerCell;
import org.eclipse.nebula.widgets.nattable.layer.cell.LayerCell;
import org.eclipse.nebula.widgets.nattable.layer.event.ILayerEvent;
import org.eclipse.nebula.widgets.nattable.layer.event.ILayerEventHandler;
import org.eclipse.nebula.widgets.nattable.painter.cell.ICellPainter;
import org.eclipse.nebula.widgets.nattable.painter.layer.GridLineCellLayerPainter;
import org.eclipse.nebula.widgets.nattable.painter.layer.ILayerPainter;
import org.eclipse.nebula.widgets.nattable.persistence.IPersistable;
import org.eclipse.nebula.widgets.nattable.style.DisplayMode;
import org.eclipse.nebula.widgets.nattable.ui.binding.UiBindingRegistry;
import org.eclipse.nebula.widgets.nattable.util.IClientAreaProvider;
import org.eclipse.swt.graphics.Rectangle;

/**
 * Base layer implementation with common methods for managing listeners and
 * caching, etc.
 */
public abstract class AbstractLayer implements ILayer {

    private String regionName;
    protected ILayerPainter layerPainter;
    private IClientAreaProvider clientAreaProvider = IClientAreaProvider.DEFAULT;
    private IConfigLabelAccumulator configLabelAccumulator;

    protected final Map<Class<? extends ILayerCommand>, ILayerCommandHandler<? extends ILayerCommand>> commandHandlers =
            new LinkedHashMap<Class<? extends ILayerCommand>, ILayerCommandHandler<? extends ILayerCommand>>();
    protected Map<Class<? extends ILayerEvent>, ILayerEventHandler<? extends ILayerEvent>> eventHandlers =
            new HashMap<Class<? extends ILayerEvent>, ILayerEventHandler<? extends ILayerEvent>>();

    private final List<IPersistable> persistables = new LinkedList<IPersistable>();
    private final Set<ILayerListener> listeners = new LinkedHashSet<ILayerListener>();
    private final Collection<IConfiguration> configurations = new LinkedList<IConfiguration>();

    private boolean configurationApplied = false;

    /**
     * {@link ReadWriteLock} that is used to ensure that no concurrent
     * modifications happen on event handling
     *
     * @since 1.4
     */
    protected ReadWriteLock eventHelperLock = new ReentrantReadWriteLock();

    // Dispose

    @Override
    public void dispose() {}

    // Regions

    @Override
    public LabelStack getRegionLabelsByXY(int x, int y) {
        LabelStack regionLabels = new LabelStack();
        if (this.regionName != null) {
            regionLabels.addLabel(this.regionName);
        }
        return regionLabels;
    }

    public String getRegionName() {
        return this.regionName;
    }

    public void setRegionName(String regionName) {
        this.regionName = regionName;
    }

    // Config lables

    @Override
    public LabelStack getConfigLabelsByPosition(int columnPosition, int rowPosition) {
        LabelStack configLabels = new LabelStack();
        if (this.configLabelAccumulator != null) {
            this.configLabelAccumulator.accumulateConfigLabels(configLabels, columnPosition, rowPosition);
        }
        if (this.regionName != null) {
            configLabels.addLabel(this.regionName);
        }
        return configLabels;
    }

    public IConfigLabelAccumulator getConfigLabelAccumulator() {
        return this.configLabelAccumulator;
    }

    public void setConfigLabelAccumulator(IConfigLabelAccumulator cellLabelAccumulator) {
        this.configLabelAccumulator = cellLabelAccumulator;
    }

    // Persistence

    @Override
    public void saveState(String prefix, Properties properties) {
        for (IPersistable persistable : this.persistables) {
            persistable.saveState(prefix, properties);
        }
    }

    @Override
    public void loadState(String prefix, Properties properties) {
        for (IPersistable persistable : this.persistables) {
            persistable.loadState(prefix, properties);
        }
    }

    @Override
    public void registerPersistable(IPersistable persistable) {
        this.persistables.add(persistable);
    }

    @Override
    public void unregisterPersistable(IPersistable persistable) {
        this.persistables.remove(persistable);
    }

    // Configuration

    public void addConfiguration(IConfiguration configuration) {
        this.configurations.add(configuration);
    }

    public void clearConfiguration() {
        this.configurations.clear();
    }

    @Override
    public void configure(ConfigRegistry configRegistry, UiBindingRegistry uiBindingRegistry) {
        if (!this.configurationApplied) {
            for (IConfiguration configuration : this.configurations) {
                configuration.configureLayer(this);
                configuration.configureRegistry(configRegistry);
                configuration.configureUiBindings(uiBindingRegistry);
            }

            // Bug 447145
            // to avoid registering configuration values multiple times, we
            // remember that this layer has already been configured
            this.configurationApplied = true;
        }
    }

    // Commands

    @Override
    @SuppressWarnings({ "unchecked", "rawtypes" })
    public boolean doCommand(ILayerCommand command) {
        for (Class<? extends ILayerCommand> commandClass : this.commandHandlers.keySet()) {
            if (commandClass.isInstance(command)) {
                ILayerCommandHandler commandHandler = this.commandHandlers.get(commandClass);
                if (commandHandler.doCommand(this, command)) {
                    return true;
                }
            }
        }

        return false;
    }

    // Command handlers

    /**
     * Layers should use this method to register their command handlers and call
     * it from their constructor. This allows easy overriding if required of
     * command handlers
     */
    protected void registerCommandHandlers() {
        // No op
    }

    @Override
    public void registerCommandHandler(ILayerCommandHandler<?> commandHandler) {
        this.commandHandlers.put(commandHandler.getCommandClass(), commandHandler);
    }

    @Override
    public void unregisterCommandHandler(Class<? extends ILayerCommand> commandClass) {
        this.commandHandlers.remove(commandClass);
    }

    // Events

    @Override
    public void addLayerListener(ILayerListener listener) {
        this.listeners.add(listener);
    }

    @Override
    public void removeLayerListener(ILayerListener listener) {
        this.listeners.remove(listener);
    }

    @Override
    public boolean hasLayerListener(Class<? extends ILayerListener> layerListenerClass) {
        for (ILayerListener listener : this.listeners) {
            if (listener.getClass().equals(layerListenerClass)) {
                return true;
            }
        }
        return false;
    }

    /**
     * Handle layer event notification. Convert it to your context and propagate
     * <i>UP</i>.
     *
     * If you override this method you <strong>MUST NOT FORGET</strong> to raise
     * the event up the layer stack by calling
     * <code>super.fireLayerEvent(event)</code> - unless you plan to eat the
     * event yourself.
     **/
    @Override
    @SuppressWarnings({ "unchecked", "rawtypes" })
    public void handleLayerEvent(ILayerEvent event) {
        Map<Class<? extends ILayerEvent>, ILayerEventHandler<? extends ILayerEvent>> currentEventHandlers;
        this.eventHelperLock.readLock().lock();
        try {
            currentEventHandlers = this.eventHandlers;
        } finally {
            this.eventHelperLock.readLock().unlock();
        }

        for (Class<? extends ILayerEvent> eventClass : currentEventHandlers.keySet()) {
            if (eventClass.isInstance(event)) {
                ILayerEventHandler eventHandler = currentEventHandlers.get(eventClass);
                eventHandler.handleLayerEvent(event);
            }
        }

        // Pass on the event to our parent
        if (event.convertToLocal(this)) {
            fireLayerEvent(event);
        }
    }

    public void registerEventHandler(ILayerEventHandler<?> eventHandler) {
        this.eventHelperLock.writeLock().lock();
        try {
            this.eventHandlers = new HashMap<Class<? extends ILayerEvent>, ILayerEventHandler<? extends ILayerEvent>>(this.eventHandlers);
            this.eventHandlers.put(eventHandler.getLayerEventClass(), eventHandler);
        } finally {
            this.eventHelperLock.writeLock().unlock();
        }
    }

    public void unregisterEventHandler(ILayerEventHandler<?> eventHandler) {
        this.eventHelperLock.writeLock().lock();
        try {
            this.eventHandlers = new HashMap<Class<? extends ILayerEvent>, ILayerEventHandler<? extends ILayerEvent>>(this.eventHandlers);
            this.eventHandlers.remove(eventHandler.getLayerEventClass());
        } finally {
            this.eventHelperLock.writeLock().unlock();
        }
    }

    /**
     * Pass the event to all the {@link ILayerListener} registered on this
     * layer. A cloned copy is passed to each listener.
     */
    @Override
    public void fireLayerEvent(ILayerEvent event) {
        if (this.listeners.size() > 0) {
            Iterator<ILayerListener> it = this.listeners.iterator();
            boolean isLastListener = false;
            do {
                ILayerListener l = it.next();
                isLastListener = !it.hasNext(); // Lookahead

                // Fire cloned event to first n-1 listeners; fire original event
                // to last listener
                ILayerEvent eventToFire = isLastListener ? event : event.cloneEvent();
                l.handleLayerEvent(eventToFire);
            } while (!isLastListener);
        }
    }

    /**
     * @return {@link ILayerPainter}. Defaults to
     *         {@link GridLineCellLayerPainter}
     */
    @Override
    public ILayerPainter getLayerPainter() {
        if (this.layerPainter == null) {
            this.layerPainter = new GridLineCellLayerPainter();
        }
        return this.layerPainter;
    }

    public void setLayerPainter(ILayerPainter layerPainter) {
        this.layerPainter = layerPainter;
    }

    // Client area

    @Override
    public IClientAreaProvider getClientAreaProvider() {
        return this.clientAreaProvider;
    }

    @Override
    public void setClientAreaProvider(IClientAreaProvider clientAreaProvider) {
        this.clientAreaProvider = clientAreaProvider;
    }

    @Override
    public String toString() {
        return getClass().getSimpleName();
    }

    @Override
    public ILayerCell getCellByPosition(int columnPosition, int rowPosition) {
        if (columnPosition < 0 || columnPosition >= getColumnCount()
                || rowPosition < 0 || rowPosition >= getRowCount()) {
            return null;
        }

        return new LayerCell(this, columnPosition, rowPosition);
    }

    @Override
    public Rectangle getBoundsByPosition(int columnPosition, int rowPosition) {
        ILayerCell cell = getCellByPosition(columnPosition, rowPosition);
        ILayer cellLayer = cell.getLayer();

        int xOffset = -1;
        int yOffset = -1;
        int width = 0;
        int height = 0;
        {
            int column = cell.getOriginColumnPosition();
            int end = column + cell.getColumnSpan();
            for (; column < end; column++) {
                int columnOffset = cellLayer.getStartXOfColumnPosition(column);
                if (column < cellLayer.getColumnCount()) {
                    xOffset = columnOffset;
                    break;
                }
            }
            for (; column < end; column++) {
                width += cellLayer.getColumnWidthByPosition(column);
            }
        }
        {
            int row = cell.getOriginRowPosition();
            int end = row + cell.getRowSpan();
            for (; row < end; row++) {
                int rowOffset = cellLayer.getStartYOfRowPosition(row);
                if (row < cellLayer.getRowCount()) {
                    yOffset = rowOffset;
                    break;
                }
            }
            for (; row < end; row++) {
                height += cellLayer.getRowHeightByPosition(row);
            }
        }

        return new Rectangle(xOffset, yOffset, width, height);
    }

    @Override
    public String getDisplayModeByPosition(int columnPosition, int rowPosition) {
        return DisplayMode.NORMAL;
    }

    @Override
    public ICellPainter getCellPainter(
            int columnPosition, int rowPosition,
            ILayerCell cell, IConfigRegistry configRegistry) {
        return configRegistry.getConfigAttribute(
                CellConfigAttributes.CELL_PAINTER,
                cell.getDisplayMode(),
                cell.getConfigLabels().getLabels());
    }

    /**
     * @return <code>true</code> if the layer has a dynamic size (e.g. viewport)
     *         or a fixed size.
     * @since 1.4
     */
    public boolean isDynamicSizeLayer() {
        return false;
    }

    /**
     * @return The collection of labels that are provided by this layer.
     * @since 1.4
     */
    public Collection<String> getProvidedLabels() {
        Collection<String> labels = null;

        if (getUnderlyingLayerByPosition(0, 0) instanceof AbstractLayer) {
            labels = ((AbstractLayer) getUnderlyingLayerByPosition(0, 0)).getProvidedLabels();
        } else {
            labels = new LinkedHashSet<String>();
        }

        // add the region
        if (this.regionName != null) {
            labels.add(this.regionName);
        }

        // add the labels configured via IConfigLabelAccumulator
        if (this.configLabelAccumulator != null
                && this.configLabelAccumulator instanceof IConfigLabelProvider) {
            labels.addAll(((IConfigLabelProvider) this.configLabelAccumulator).getProvidedLabels());
        }

        return labels;
    }
}
