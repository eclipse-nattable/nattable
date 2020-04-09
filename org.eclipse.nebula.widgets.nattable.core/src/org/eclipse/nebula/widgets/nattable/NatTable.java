/*******************************************************************************
 * Copyright (c) 2012, 2020 Original authors and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Original authors and others - initial API and implementation
 ******************************************************************************/
package org.eclipse.nebula.widgets.nattable;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Properties;
import java.util.concurrent.locks.ReadWriteLock;
import java.util.concurrent.locks.ReentrantReadWriteLock;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.eclipse.nebula.widgets.nattable.command.DisposeResourcesCommand;
import org.eclipse.nebula.widgets.nattable.command.ILayerCommand;
import org.eclipse.nebula.widgets.nattable.command.ILayerCommandHandler;
import org.eclipse.nebula.widgets.nattable.command.StructuralRefreshCommand;
import org.eclipse.nebula.widgets.nattable.command.VisualRefreshCommand;
import org.eclipse.nebula.widgets.nattable.config.ConfigRegistry;
import org.eclipse.nebula.widgets.nattable.config.DefaultNatTableStyleConfiguration;
import org.eclipse.nebula.widgets.nattable.config.IConfigRegistry;
import org.eclipse.nebula.widgets.nattable.config.IConfiguration;
import org.eclipse.nebula.widgets.nattable.config.NatTableConfigAttributes;
import org.eclipse.nebula.widgets.nattable.conflation.EventConflaterChain;
import org.eclipse.nebula.widgets.nattable.conflation.IEventConflater;
import org.eclipse.nebula.widgets.nattable.conflation.VisualChangeEventConflater;
import org.eclipse.nebula.widgets.nattable.coordinate.Range;
import org.eclipse.nebula.widgets.nattable.copy.InternalCellClipboard;
import org.eclipse.nebula.widgets.nattable.edit.CellEditorCreatedEvent;
import org.eclipse.nebula.widgets.nattable.edit.editor.ICellEditor;
import org.eclipse.nebula.widgets.nattable.grid.command.ClientAreaResizeCommand;
import org.eclipse.nebula.widgets.nattable.grid.command.InitializeGridCommand;
import org.eclipse.nebula.widgets.nattable.layer.AbstractLayer;
import org.eclipse.nebula.widgets.nattable.layer.DefaultHorizontalDpiConverter;
import org.eclipse.nebula.widgets.nattable.layer.DefaultVerticalDpiConverter;
import org.eclipse.nebula.widgets.nattable.layer.IDpiConverter;
import org.eclipse.nebula.widgets.nattable.layer.ILayer;
import org.eclipse.nebula.widgets.nattable.layer.ILayerListener;
import org.eclipse.nebula.widgets.nattable.layer.LabelStack;
import org.eclipse.nebula.widgets.nattable.layer.cell.ILayerCell;
import org.eclipse.nebula.widgets.nattable.layer.command.ConfigureScalingCommand;
import org.eclipse.nebula.widgets.nattable.layer.event.CellVisualUpdateEvent;
import org.eclipse.nebula.widgets.nattable.layer.event.ColumnVisualUpdateEvent;
import org.eclipse.nebula.widgets.nattable.layer.event.ILayerEvent;
import org.eclipse.nebula.widgets.nattable.layer.event.IVisualChangeEvent;
import org.eclipse.nebula.widgets.nattable.layer.event.RowVisualUpdateEvent;
import org.eclipse.nebula.widgets.nattable.layer.stack.DummyGridLayerStack;
import org.eclipse.nebula.widgets.nattable.painter.IOverlayPainter;
import org.eclipse.nebula.widgets.nattable.painter.cell.ICellPainter;
import org.eclipse.nebula.widgets.nattable.painter.layer.ILayerPainter;
import org.eclipse.nebula.widgets.nattable.painter.layer.NatLayerPainter;
import org.eclipse.nebula.widgets.nattable.persistence.IPersistable;
import org.eclipse.nebula.widgets.nattable.selection.SelectionLayer.MoveDirectionEnum;
import org.eclipse.nebula.widgets.nattable.selection.event.CellSelectionEvent;
import org.eclipse.nebula.widgets.nattable.selection.event.ISelectionEvent;
import org.eclipse.nebula.widgets.nattable.selection.event.RowSelectionEvent;
import org.eclipse.nebula.widgets.nattable.style.theme.ThemeConfiguration;
import org.eclipse.nebula.widgets.nattable.style.theme.ThemeManager;
import org.eclipse.nebula.widgets.nattable.ui.binding.UiBindingRegistry;
import org.eclipse.nebula.widgets.nattable.ui.mode.ConfigurableModeEventHandler;
import org.eclipse.nebula.widgets.nattable.ui.mode.Mode;
import org.eclipse.nebula.widgets.nattable.ui.mode.ModeSupport;
import org.eclipse.nebula.widgets.nattable.util.GUIHelper;
import org.eclipse.nebula.widgets.nattable.util.IClientAreaProvider;
import org.eclipse.nebula.widgets.nattable.viewport.command.RecalculateScrollBarsCommand;
import org.eclipse.swt.SWT;
import org.eclipse.swt.custom.BusyIndicator;
import org.eclipse.swt.dnd.DragSource;
import org.eclipse.swt.dnd.DragSourceEvent;
import org.eclipse.swt.dnd.DragSourceListener;
import org.eclipse.swt.dnd.DropTarget;
import org.eclipse.swt.dnd.DropTargetListener;
import org.eclipse.swt.dnd.Transfer;
import org.eclipse.swt.events.DisposeEvent;
import org.eclipse.swt.events.DisposeListener;
import org.eclipse.swt.events.FocusEvent;
import org.eclipse.swt.events.FocusListener;
import org.eclipse.swt.events.PaintEvent;
import org.eclipse.swt.events.PaintListener;
import org.eclipse.swt.graphics.Rectangle;
import org.eclipse.swt.widgets.Canvas;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Event;
import org.eclipse.swt.widgets.Listener;
import org.eclipse.swt.widgets.ScrollBar;

//this warning suppression is because of the ActiveCellEditorRegistry usage to ensure backwards compatibility
public class NatTable extends Canvas implements ILayer, PaintListener, IClientAreaProvider, ILayerListener, IPersistable {

    private static final Log LOG = LogFactory.getLog(NatTable.class);

    public static final int DEFAULT_STYLE_OPTIONS = SWT.NO_BACKGROUND
            | SWT.NO_REDRAW_RESIZE | SWT.DOUBLE_BUFFERED | SWT.V_SCROLL
            | SWT.H_SCROLL;

    /**
     * Key that is used for loading NatTable states. Is set to <code>true</code>
     * in case the initial painting is not finished yet. In this case there is
     * no need to call refresh commands on loading.
     */
    public static final String INITIAL_PAINT_COMPLETE_FLAG = "NatTable.initialPaintComplete"; //$NON-NLS-1$

    private UiBindingRegistry uiBindingRegistry;

    private ModeSupport modeSupport;

    private final EventConflaterChain conflaterChain;

    private final List<IOverlayPainter> overlayPainters = new ArrayList<IOverlayPainter>();

    private final List<IPersistable> persistables = new LinkedList<IPersistable>();

    private ILayer underlyingLayer;

    private IConfigRegistry configRegistry;

    protected final Collection<IConfiguration> configurations = new LinkedList<IConfiguration>();

    protected String id = GUIHelper.getSequenceNumber();

    private ILayerPainter layerPainter = new NatLayerPainter(this);

    private final boolean autoconfigure;

    /**
     * Listener that is added because of Bug 415459.<br/>
     * It is added to the parent composite and will close an active cell editor
     * in case the parent is resized. We need to listen to the parent composite
     * resize, because resizing a shell or a part in e4 does not cause loosing
     * the focus. Therefore the editor will stay open in such cases. As this
     * causes rendering issues when using percentage sizing, this listener
     * closes an editor on parent composite resize.<br/>
     * It is not registered as listener to NatTable itself, because this would
     * have impact when filtering or dynamic updates cause scrollbars to become
     * visible/invisible, which result in resizing of the NatTable.
     */
    private Listener closeEditorOnParentResize = new Listener() {
        @Override
        public void handleEvent(Event event) {
            // as resizing doesn't cause the current active editor to loose
            // focus we are closing the current active editor manually
            if (!commitAndCloseActiveCellEditor()) {
                // if committing didn't work out we need to perform a hard close
                // otherwise the state of the table would be unstale
                getActiveCellEditor().close();
            }
        }
    };

    /**
     * This flag is used to deal with runtime issues on loading states while the
     * initial rendering is not finished yet.
     */
    private boolean initialPaintComplete = false;

    /**
     * The {@link ThemeManager} that is used to switch
     * {@link ThemeConfiguration}s at runtime.
     */
    private ThemeManager themeManager;

    /**
     * The {@link InternalCellClipboard} that is used for internal copy &amp;
     * paste functionality.
     */
    private InternalCellClipboard clipboard = new InternalCellClipboard();

    /**
     * The active cell editor or {@code null} if there is no one.
     */
    private ICellEditor activeCellEditor;

    public NatTable(Composite parent) {
        this(parent, DEFAULT_STYLE_OPTIONS);
    }

    /**
     * @param parent
     *            a composite control which will be the parent of the new
     *            instance (cannot be null)
     * @param autoconfigure
     *            if set to <code>false</code> no auto configuration is done.
     *            Default settings are <i>not</i> loaded. Configuration(s) have
     *            to be manually added by invoking
     *            {@link #addConfiguration(IConfiguration)}. At the minimum the
     *            {@link DefaultNatTableStyleConfiguration} must be added for
     *            the table to render.
     */
    public NatTable(Composite parent, boolean autoconfigure) {
        this(parent, DEFAULT_STYLE_OPTIONS, autoconfigure);
    }

    public NatTable(Composite parent, ILayer layer) {
        this(parent, DEFAULT_STYLE_OPTIONS, layer);
    }

    public NatTable(Composite parent, ILayer layer, boolean autoconfigure) {
        this(parent, DEFAULT_STYLE_OPTIONS, layer, autoconfigure);
    }

    public NatTable(Composite parent, final int style) {
        this(parent, style, new DummyGridLayerStack());
    }

    public NatTable(Composite parent, final int style, boolean autoconfigure) {
        this(parent, style, new DummyGridLayerStack(), autoconfigure);
    }

    public NatTable(final Composite parent, final int style, ILayer layer) {
        this(parent, style, layer, true);
    }

    public NatTable(
            final Composite parent, final int style,
            final ILayer layer, boolean autoconfigure) {
        this(parent, style, layer, new EventConflaterChain(), autoconfigure);
    }

    /**
     * Only use this constructor to specify a custom EventConflaterChain with
     * different refresh interval settings in case you are facing issues on
     * rendering, e.g. low FPS <i>(laggy)</i> behavior on scrolling (refresh
     * interval too high) or flickering UI (refresh interval too low).
     *
     * @param parent
     *            a composite control which will be the parent of the new
     *            instance (cannot be null)
     * @param style
     *            the style of control to construct
     *            {@link #DEFAULT_STYLE_OPTIONS}
     * @param layer
     *            the {@link ILayer} that should be rendered by this NatTable
     * @param chain
     *            the {@link EventConflaterChain} used to conflate events that
     *            trigger for example repainting. By default an
     *            {@link EventConflaterChain} is registered with a refresh
     *            interval specified via
     *            {@link EventConflaterChain#DEFAULT_REFRESH_INTERVAL}.
     * @param autoconfigure
     *            if set to <code>false</code> no auto configuration is done.
     *            Default settings are <i>not</i> loaded. Configuration(s) have
     *            to be manually added by invoking
     *            {@link #addConfiguration(IConfiguration)}. At the minimum the
     *            {@link DefaultNatTableStyleConfiguration} must be added for
     *            the table to render.
     *
     * @since 1.5
     */
    public NatTable(
            final Composite parent, final int style,
            final ILayer layer, EventConflaterChain chain, boolean autoconfigure) {
        super(parent, style);

        // Disable scroll bars by default; if a Viewport is available, it will
        // enable the scroll bars
        disableScrollBar(getHorizontalBar());
        disableScrollBar(getVerticalBar());

        initInternalListener();

        internalSetLayer(layer);

        this.autoconfigure = autoconfigure;
        if (autoconfigure) {
            this.configurations.add(new DefaultNatTableStyleConfiguration());
            configure();
        }

        this.conflaterChain = chain;
        this.conflaterChain.add(getVisualChangeEventConflater());
        this.conflaterChain.start();

        parent.addListener(SWT.Resize, this.closeEditorOnParentResize);

        addDisposeListener(new DisposeListener() {

            @Override
            public void widgetDisposed(DisposeEvent e) {
                doCommand(new DisposeResourcesCommand());
                NatTable.this.conflaterChain.stop();
                layer.dispose();

                parent.removeListener(SWT.Resize, NatTable.this.closeEditorOnParentResize);
            }

        });
    }

    protected IEventConflater getVisualChangeEventConflater() {
        return new VisualChangeEventConflater(this);
    }

    private void disableScrollBar(ScrollBar scrollBar) {
        if (scrollBar != null) {
            scrollBar.setMinimum(0);
            scrollBar.setMaximum(1);
            scrollBar.setThumb(1);
            scrollBar.setEnabled(false);
        }
    }

    public ILayer getLayer() {
        return this.underlyingLayer;
    }

    public void setLayer(ILayer layer) {
        if (this.autoconfigure) {
            throw new IllegalStateException("May only set layer post construction if autoconfigure is turned off"); //$NON-NLS-1$
        }

        internalSetLayer(layer);
    }

    private void internalSetLayer(ILayer layer) {
        if (layer != null) {
            this.underlyingLayer = layer;
            this.underlyingLayer.setClientAreaProvider(new IClientAreaProvider() {

                @Override
                public Rectangle getClientArea() {
                    final Rectangle clientArea = new Rectangle(0, 0, 0, 0);
                    if (!isDisposed()) {
                        getDisplay().syncExec(new Runnable() {
                            @Override
                            public void run() {
                                Rectangle natClientArea = NatTable.this.getClientArea();
                                clientArea.x = natClientArea.x;
                                clientArea.y = natClientArea.y;
                                clientArea.width = natClientArea.width;
                                clientArea.height = natClientArea.height;
                            }
                        });
                    }
                    return clientArea;
                }

            });
            this.underlyingLayer.addLayerListener(this);

            // register the DPI scaling on the layers
            this.underlyingLayer.doCommand(
                    new ConfigureScalingCommand(
                            new DefaultHorizontalDpiConverter(),
                            new DefaultVerticalDpiConverter()));
        }
    }

    /**
     * Adds a configuration to the table.
     * <p>
     * Configurations are processed when the {@link #configure()} method is
     * invoked. Each configuration object then has a chance to configure the
     * <ol>
     * <li>ILayer</li>
     * <li>ConfigRegistry</li>
     * <li>UiBindingRegistry</li>
     * </ol>
     *
     * @param configuration
     *            The {@link IConfiguration} to add.
     */
    public void addConfiguration(IConfiguration configuration) {
        if (this.autoconfigure) {
            throw new IllegalStateException("May only add configurations post construction if autoconfigure is turned off"); //$NON-NLS-1$
        }

        this.configurations.add(configuration);
    }

    /**
     * @return {@link IConfigRegistry} used to hold the configurations.
     */
    public IConfigRegistry getConfigRegistry() {
        if (this.configRegistry == null) {
            this.configRegistry = new ConfigRegistry();
            this.themeManager = new ThemeManager(this.configRegistry);
            configureScaling(new DefaultHorizontalDpiConverter(), new DefaultVerticalDpiConverter());
        }
        return this.configRegistry;
    }

    /**
     * Sets the {@link IConfigRegistry} that should be used to hold the
     * configurations. Can only be used if autoconfigure is turned off at
     * NatTable creation. Should only be used in cases where an
     * {@link IConfigRegistry} is needed for {@link ILayer} creation
     * <b>BEFORE</b> the NatTable can be created.
     *
     * @param configRegistry
     *            The {@link IConfigRegistry} that should be used to hold the
     *            configurations.
     */
    public void setConfigRegistry(IConfigRegistry configRegistry) {
        if (this.autoconfigure) {
            throw new IllegalStateException("May only set config registry post construction if autoconfigure is turned off"); //$NON-NLS-1$
        }

        this.configRegistry = configRegistry;
        this.themeManager = new ThemeManager(configRegistry);
        configureScaling(new DefaultHorizontalDpiConverter(), new DefaultVerticalDpiConverter());
    }

    /**
     * Add the given {@link IDpiConverter} to the {@link IConfigRegistry} so
     * they can be used by painters and set the system properties to use the
     * configured scaling for images.
     *
     * @param horizontalConverter
     *            The {@link IDpiConverter} for horizontal scaling.
     * @param verticalConverter
     *            The {@link IDpiConverter} for vertical scaling.
     * @since 2.0
     */
    protected void configureScaling(IDpiConverter horizontalConverter, IDpiConverter verticalConverter) {
        // ensure that there is no active cell editor if the scaling changes, as
        // it would leave the open editor for the previous scaling that does not
        // match the updated view
        if (!commitAndCloseActiveCellEditor()) {
            // if committing didn't work out we need to perform a hard
            // close otherwise the state of the table would be unstale
            getActiveCellEditor().close();
        }

        // set the converter to the registry
        getConfigRegistry().registerConfigAttribute(
                NatTableConfigAttributes.HORIZONTAL_DPI_CONVERTER,
                horizontalConverter);
        getConfigRegistry().registerConfigAttribute(
                NatTableConfigAttributes.VERTICAL_DPI_CONVERTER,
                verticalConverter);

        // set the dpi values to the GUIHelper to reflect scaling for images
        GUIHelper.setDpi(horizontalConverter.getDpi(), verticalConverter.getDpi());

        // register the font scaling factor
        float dpiFactor = GUIHelper.getDpiFactor(GUIHelper.getDpiX());
        float displayDpiFactor = GUIHelper.getDpiFactor(Display.getDefault().getDPI().x);
        float fontScalingFactor = (dpiFactor / displayDpiFactor);
        getConfigRegistry().registerConfigAttribute(
                NatTableConfigAttributes.FONT_SCALING_FACTOR,
                fontScalingFactor);

        this.themeManager.refreshCurrentTheme();
    }

    /**
     * @return Registry holding all the UIBindings contributed by the underlying
     *         layers
     */
    public UiBindingRegistry getUiBindingRegistry() {
        if (this.uiBindingRegistry == null) {
            this.uiBindingRegistry = new UiBindingRegistry(this);
        }
        return this.uiBindingRegistry;
    }

    public void setUiBindingRegistry(UiBindingRegistry uiBindingRegistry) {
        if (this.autoconfigure) {
            throw new IllegalStateException("May only set UI binding registry post construction if autoconfigure is turned off"); //$NON-NLS-1$
        }

        this.uiBindingRegistry = uiBindingRegistry;
    }

    public String getID() {
        return this.id;
    }

    @Override
    protected void checkSubclass() {
    }

    protected void initInternalListener() {
        this.modeSupport = new ModeSupport(this);
        this.modeSupport.registerModeEventHandler(Mode.NORMAL_MODE,
                new ConfigurableModeEventHandler(this.modeSupport, this));
        this.modeSupport.switchMode(Mode.NORMAL_MODE);

        addPaintListener(this);

        addFocusListener(new FocusListener() {

            @Override
            public void focusLost(final FocusEvent arg0) {
                redraw();
            }

            @Override
            public void focusGained(final FocusEvent arg0) {
                redraw();
            }

        });

        addListener(SWT.Resize, new Listener() {
            @Override
            public void handleEvent(final Event e) {
                doCommand(new ClientAreaResizeCommand(NatTable.this));
                redraw();
            }
        });
    }

    @Override
    public boolean forceFocus() {
        return super.forceFocus();
    }

    // Painting ///////////////////////////////////////////////////////////////

    public List<IOverlayPainter> getOverlayPainters() {
        return this.overlayPainters;
    }

    public void addOverlayPainter(IOverlayPainter overlayPainter) {
        this.overlayPainters.add(overlayPainter);
    }

    public void removeOverlayPainter(IOverlayPainter overlayPainter) {
        this.overlayPainters.remove(overlayPainter);
    }

    @Override
    public void paintControl(final PaintEvent event) {
        paintNatTable(event);
        this.initialPaintComplete = true;
    }

    private void paintNatTable(final PaintEvent event) {
        getLayerPainter().paintLayer(this, event.gc, 0, 0,
                new Rectangle(event.x, event.y, event.width, event.height),
                getConfigRegistry());
    }

    @Override
    public ILayerPainter getLayerPainter() {
        return this.layerPainter;
    }

    public void setLayerPainter(ILayerPainter layerPainter) {
        this.layerPainter = layerPainter;
    }

    /**
     * Repaint only a specific column in the grid. This method is optimized so
     * that only the specific column is repainted and nothing else.
     *
     * @param columnPosition
     *            column of the grid to repaint
     */
    public void repaintColumn(int columnPosition) {
        int xOffset = getStartXOfColumnPosition(columnPosition);
        if (xOffset < 0) {
            return;
        }
        redraw(xOffset, 0, getColumnWidthByPosition(columnPosition), getHeight(), true);
    }

    /**
     * Repaint only a specific row in the grid. This method is optimized so that
     * only the specific row is repainted and nothing else.
     *
     * @param rowPosition
     *            row of the grid to repaint
     */
    public void repaintRow(int rowPosition) {
        int yOffset = getStartYOfRowPosition(rowPosition);
        if (yOffset < 0) {
            return;
        }
        redraw(0, yOffset, getWidth(), getRowHeightByPosition(rowPosition), true);
    }

    /**
     * Repaint only a specific cell in the grid. This method is optimized so
     * that only the specific cell is repainted and nothing else.
     *
     * @param columnPosition
     *            column position of the cell to repaint
     * @param rowPosition
     *            row position of the cell to repaint
     */
    public void repaintCell(int columnPosition, int rowPosition) {
        ILayerCell cell = getCellByPosition(columnPosition, rowPosition);
        if (cell != null) {
            Rectangle bounds = cell.getBounds();
            redraw(bounds.x,
                    bounds.y,
                    bounds.width,
                    bounds.height,
                    true);
        } else {
            redraw(getStartXOfColumnPosition(columnPosition),
                    getStartYOfRowPosition(rowPosition),
                    getColumnWidthByPosition(columnPosition),
                    getRowHeightByPosition(rowPosition),
                    true);
        }
    }

    /**
     * Repaint the area to the right of the last column in case there is more
     * space available than columns to paint.
     */
    public void repaintHorizontalLeftOver() {
        int leftOverSpace = getClientArea().width - getWidth();
        if (leftOverSpace > 0)
            redraw(getWidth(), 0, leftOverSpace, getHeight(), true);
    }

    /**
     * Repaint the area to the bottom of the last row in case there is more
     * space available than rows to paint.
     */
    public void repaintVerticalLeftOver() {
        int leftOverSpace = getClientArea().height - getHeight();
        if (leftOverSpace > 0)
            redraw(0, getHeight(), getClientArea().width, leftOverSpace, true);
    }

    public void updateResize() {
        updateResize(true);
    }

    /**
     * Update the table screen by re-calculating everything again. It should not
     * be called too frequently.
     *
     * @param redraw
     *            true to redraw the table
     */
    private void updateResize(final boolean redraw) {
        if (isDisposed()) {
            return;
        }
        doCommand(new RecalculateScrollBarsCommand());
        if (redraw) {
            redraw();
        }
    }

    /**
     * Refreshes the entire NatTable as every layer will be refreshed. Used to
     * update on structural changes.
     */
    public void refresh() {
        doCommand(new StructuralRefreshCommand());
    }

    /**
     * Refreshes the entire NatTable as every layer will be refreshed.
     *
     * @param structuralChange
     *            <code>true</code> if a structural refresh should be performed
     *            (same as calling {@link #refresh()}), <code>false</code> if
     *            only a visual refresh should be performed, e.g. if
     *            configuration values have changed.
     *
     * @since 1.4
     */
    public void refresh(boolean structuralChange) {
        if (structuralChange) {
            refresh();
        } else {
            doCommand(new VisualRefreshCommand());
        }
    }

    @Override
    public void configure(ConfigRegistry configRegistry, UiBindingRegistry uiBindingRegistry) {
        throw new UnsupportedOperationException("Cannot use this method to configure NatTable. Use no-argument configure() instead."); //$NON-NLS-1$
    }

    /**
     * Processes all the registered {@link IConfiguration} (s). All the
     * underlying layers are walked and given a chance to configure. Note: all
     * desired configuration tweaks must be done <i>before</i> this method is
     * invoked.
     */
    public void configure() {
        if (this.underlyingLayer == null) {
            throw new IllegalStateException("Layer must be set before configure is called"); //$NON-NLS-1$
        }

        if (this.underlyingLayer != null) {
            this.underlyingLayer.configure((ConfigRegistry) getConfigRegistry(),
                    getUiBindingRegistry());
        }

        for (IConfiguration configuration : this.configurations) {
            configuration.configureLayer(this);
            configuration.configureRegistry(getConfigRegistry());
            configuration.configureUiBindings(getUiBindingRegistry());
        }

        // Once everything is initialized and properly configured we will
        // now formally initialize the grid
        doCommand(new InitializeGridCommand(this));
    }

    // Events /////////////////////////////////////////////////////////////////

    @Override
    public void handleLayerEvent(ILayerEvent event) {
        List<ILayerListener> currentListeners;
        this.eventListenerLock.readLock().lock();
        try {
            currentListeners = this.listeners;
        } finally {
            this.eventListenerLock.readLock().unlock();
        }
        for (ILayerListener layerListener : currentListeners) {
            layerListener.handleLayerEvent(event);
        }

        if (event instanceof CellVisualUpdateEvent) {
            CellVisualUpdateEvent update = (CellVisualUpdateEvent) event;
            repaintCell(update.getColumnPosition(), update.getRowPosition());
            return;
        }

        if (event instanceof ColumnVisualUpdateEvent) {
            ColumnVisualUpdateEvent update = (ColumnVisualUpdateEvent) event;
            // if more than one column has changed repaint the whole table
            Collection<Range> ranges = update.getColumnPositionRanges();
            if (ranges.size() == 1) {
                Range range = ranges.iterator().next();
                if (range.end - range.start == 1) {
                    repaintColumn(range.start);
                    return;
                }
            }
        }

        if (event instanceof RowVisualUpdateEvent) {
            RowVisualUpdateEvent update = (RowVisualUpdateEvent) event;
            // if more than one row has changed repaint the whole table
            Collection<Range> ranges = update.getRowPositionRanges();
            if (ranges.size() == 1) {
                Range range = ranges.iterator().next();
                if (range.end - range.start == 1) {
                    repaintRow(range.start);
                    return;
                }
            }
        }

        if (event instanceof ISelectionEvent) {
            if (event instanceof CellSelectionEvent || event instanceof RowSelectionEvent) {
                Event e = new Event();
                e.widget = this;
                try {
                    notifyListeners(SWT.Selection, e);
                } catch (RuntimeException re) {
                    LOG.error("Error on SWT selection processing", re); //$NON-NLS-1$
                }
            }

            // in case of selections we redraw immediately
            // this is because with Bug 440037 it was reported that
            // NatTable is too lazy in handling selections which
            // was caused by the EventConflaterChain that only performs
            // updates every 100ms to avoid flickering when handling too
            // many refresh operations in a short period
            redraw();
        } else if (event instanceof IVisualChangeEvent) {
            this.conflaterChain.addEvent(event);
        }

        if (event instanceof CellEditorCreatedEvent) {
            CellEditorCreatedEvent editorEvent = (CellEditorCreatedEvent) event;
            this.activeCellEditor = editorEvent.getEditor();
            Control editorControl = this.activeCellEditor.getEditorControl();
            if (editorControl != null && !editorControl.isDisposed()) {
                editorControl.addDisposeListener(new DisposeListener() {

                    @Override
                    public void widgetDisposed(DisposeEvent e) {
                        NatTable.this.activeCellEditor = null;
                    }
                });
            } else {
                this.activeCellEditor = null;
            }
        }
    }

    // ILayer /////////////////////////////////////////////////////////////////

    // Persistence

    /**
     * Save the state of the table to the properties object.
     * {@link ILayer#saveState(String, Properties)} is invoked on all the
     * underlying layers. This properties object will be populated with the
     * settings of all underlying layers and any {@link IPersistable} registered
     * with those layers.
     */
    @Override
    public void saveState(final String prefix, final Properties properties) {
        BusyIndicator.showWhile(null, new Runnable() {

            @Override
            public void run() {
                NatTable.this.underlyingLayer.saveState(prefix, properties);
            }
        });
    }

    /**
     * Restore the state of the underlying layers from the values in the
     * properties object.
     *
     * @see #saveState(String, Properties)
     */
    @Override
    public void loadState(final String prefix, final Properties properties) {
        BusyIndicator.showWhile(null, new Runnable() {

            @Override
            public void run() {
                // if the initial painting is not finished yet, tell this the
                // underlying
                // mechanisms so there will be no refresh events fired
                if (!NatTable.this.initialPaintComplete)
                    properties.setProperty(INITIAL_PAINT_COMPLETE_FLAG, "true"); //$NON-NLS-1$

                NatTable.this.underlyingLayer.loadState(prefix, properties);
            }
        });
    }

    /**
     * @see ILayer#registerPersistable(IPersistable)
     */
    @Override
    public void registerPersistable(IPersistable persistable) {
        this.persistables.add(persistable);
    }

    @Override
    public void unregisterPersistable(IPersistable persistable) {
        this.persistables.remove(persistable);
    }

    // Command

    @Override
    public boolean doCommand(ILayerCommand command) {
        if (command instanceof ConfigureScalingCommand) {
            // place the dpi converter in the ConfigRegistry for consistent
            // scaling behavior
            ConfigureScalingCommand cmd = (ConfigureScalingCommand) command;
            configureScaling(cmd.getHorizontalDpiConverter(), cmd.getVerticalDpiConverter());
        }

        return this.underlyingLayer.doCommand(command);
    }

    @Override
    public void registerCommandHandler(ILayerCommandHandler<?> commandHandler) {
        this.underlyingLayer.registerCommandHandler(commandHandler);
    }

    @Override
    public void unregisterCommandHandler(Class<? extends ILayerCommand> commandClass) {
        this.underlyingLayer.unregisterCommandHandler(commandClass);
    }

    // Events

    private List<ILayerListener> listeners = new ArrayList<ILayerListener>();

    /**
     * {@link ReadWriteLock} that is used to ensure that no concurrent
     * modifications happen on event handling
     *
     * @since 1.5
     */
    protected ReadWriteLock eventListenerLock = new ReentrantReadWriteLock();

    @Override
    public void fireLayerEvent(ILayerEvent event) {
        this.underlyingLayer.fireLayerEvent(event);
    }

    @Override
    public void addLayerListener(ILayerListener listener) {
        this.eventListenerLock.writeLock().lock();
        try {
            this.listeners = new ArrayList<ILayerListener>(this.listeners);
            this.listeners.add(listener);
        } finally {
            this.eventListenerLock.writeLock().unlock();
        }
    }

    @Override
    public void removeLayerListener(ILayerListener listener) {
        this.eventListenerLock.writeLock().lock();
        try {
            this.listeners = new ArrayList<ILayerListener>(this.listeners);
            this.listeners.remove(listener);
        } finally {
            this.eventListenerLock.writeLock().unlock();
        }
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

    // Columns

    @Override
    public int getColumnCount() {
        return this.underlyingLayer.getColumnCount();
    }

    @Override
    public int getPreferredColumnCount() {
        return this.underlyingLayer.getPreferredColumnCount();
    }

    @Override
    public int getColumnIndexByPosition(int columnPosition) {
        return this.underlyingLayer.getColumnIndexByPosition(columnPosition);
    }

    @Override
    public int localToUnderlyingColumnPosition(int localColumnPosition) {
        return localColumnPosition;
    }

    @Override
    public int underlyingToLocalColumnPosition(
            ILayer sourceUnderlyingLayer, int underlyingColumnPosition) {
        if (sourceUnderlyingLayer != this.underlyingLayer) {
            return -1;
        }

        return underlyingColumnPosition;
    }

    @Override
    public Collection<Range> underlyingToLocalColumnPositions(
            ILayer sourceUnderlyingLayer,
            Collection<Range> underlyingColumnPositionRanges) {
        if (sourceUnderlyingLayer != this.underlyingLayer) {
            return null;
        }

        return underlyingColumnPositionRanges;
    }

    // Width

    @Override
    public int getWidth() {
        return this.underlyingLayer.getWidth();
    }

    @Override
    public int getPreferredWidth() {
        return this.underlyingLayer.getPreferredWidth();
    }

    @Override
    public int getColumnWidthByPosition(int columnPosition) {
        return this.underlyingLayer.getColumnWidthByPosition(columnPosition);
    }

    // Column resize

    @Override
    public boolean isColumnPositionResizable(int columnPosition) {
        return this.underlyingLayer.isColumnPositionResizable(columnPosition);
    }

    // X

    @Override
    public int getColumnPositionByX(int x) {
        return this.underlyingLayer.getColumnPositionByX(x);
    }

    @Override
    public int getStartXOfColumnPosition(int columnPosition) {
        return this.underlyingLayer.getStartXOfColumnPosition(columnPosition);
    }

    // Underlying

    @Override
    public Collection<ILayer> getUnderlyingLayersByColumnPosition(int columnPosition) {
        Collection<ILayer> underlyingLayers = new HashSet<ILayer>();
        underlyingLayers.add(this.underlyingLayer);
        return underlyingLayers;
    }

    // Rows

    @Override
    public int getRowCount() {
        return this.underlyingLayer.getRowCount();
    }

    @Override
    public int getPreferredRowCount() {
        return this.underlyingLayer.getPreferredRowCount();
    }

    @Override
    public int getRowIndexByPosition(int rowPosition) {
        return this.underlyingLayer.getRowIndexByPosition(rowPosition);
    }

    @Override
    public int localToUnderlyingRowPosition(int localRowPosition) {
        return localRowPosition;
    }

    @Override
    public int underlyingToLocalRowPosition(
            ILayer sourceUnderlyingLayer, int underlyingRowPosition) {
        if (sourceUnderlyingLayer != this.underlyingLayer) {
            return -1;
        }

        return underlyingRowPosition;
    }

    @Override
    public Collection<Range> underlyingToLocalRowPositions(
            ILayer sourceUnderlyingLayer,
            Collection<Range> underlyingRowPositionRanges) {
        if (sourceUnderlyingLayer != this.underlyingLayer) {
            return null;
        }

        return underlyingRowPositionRanges;
    }

    // Height

    @Override
    public int getHeight() {
        return this.underlyingLayer.getHeight();
    }

    @Override
    public int getPreferredHeight() {
        return this.underlyingLayer.getPreferredHeight();
    }

    @Override
    public int getRowHeightByPosition(int rowPosition) {
        return this.underlyingLayer.getRowHeightByPosition(rowPosition);
    }

    // Row resize

    @Override
    public boolean isRowPositionResizable(int rowPosition) {
        return this.underlyingLayer.isRowPositionResizable(rowPosition);
    }

    // Y

    @Override
    public int getRowPositionByY(int y) {
        return this.underlyingLayer.getRowPositionByY(y);
    }

    @Override
    public int getStartYOfRowPosition(int rowPosition) {
        return this.underlyingLayer.getStartYOfRowPosition(rowPosition);
    }

    // Underlying

    @Override
    public Collection<ILayer> getUnderlyingLayersByRowPosition(int rowPosition) {
        Collection<ILayer> underlyingLayers = new HashSet<ILayer>();
        underlyingLayers.add(this.underlyingLayer);
        return underlyingLayers;
    }

    // Cell features

    @Override
    public ILayerCell getCellByPosition(int columnPosition, int rowPosition) {
        return this.underlyingLayer.getCellByPosition(columnPosition, rowPosition);
    }

    @Override
    public Rectangle getBoundsByPosition(int columnPosition, int rowPosition) {
        return this.underlyingLayer.getBoundsByPosition(columnPosition, rowPosition);
    }

    @Override
    public String getDisplayModeByPosition(int columnPosition, int rowPosition) {
        return this.underlyingLayer.getDisplayModeByPosition(columnPosition,
                rowPosition);
    }

    @Override
    public LabelStack getConfigLabelsByPosition(int columnPosition, int rowPosition) {
        return this.underlyingLayer.getConfigLabelsByPosition(columnPosition,
                rowPosition);
    }

    @Override
    public Object getDataValueByPosition(int columnPosition, int rowPosition) {
        return this.underlyingLayer.getDataValueByPosition(columnPosition,
                rowPosition);
    }

    @Override
    public ICellPainter getCellPainter(int columnPosition, int rowPosition,
            ILayerCell cell, IConfigRegistry configRegistry) {
        return this.underlyingLayer.getCellPainter(columnPosition, rowPosition,
                cell, configRegistry);
    }

    // IRegionResolver

    @Override
    public LabelStack getRegionLabelsByXY(int x, int y) {
        return this.underlyingLayer.getRegionLabelsByXY(x, y);
    }

    @Override
    public ILayer getUnderlyingLayerByPosition(int columnPosition, int rowPosition) {
        return this.underlyingLayer;
    }

    @Override
    public IClientAreaProvider getClientAreaProvider() {
        return this;
    }

    @Override
    public void setClientAreaProvider(IClientAreaProvider clientAreaProvider) {
        this.underlyingLayer.setClientAreaProvider(clientAreaProvider);
    }

    // DND /////////////////////////////////////////////////////////////////

    /**
     * Adds support for dragging items out of this control via a user
     * drag-and-drop operation.
     *
     * @param operations
     *            a bitwise OR of the supported drag and drop operation types (
     *            <code>DROP_COPY</code>,<code>DROP_LINK</code>, and
     *            <code>DROP_MOVE</code>)
     * @param transferTypes
     *            the transfer types that are supported by the drag operation
     * @param listener
     *            the callback that will be invoked to set the drag data and to
     *            cleanup after the drag and drop operation finishes
     * @see org.eclipse.swt.dnd.DND
     */
    public void addDragSupport(final int operations,
            final Transfer[] transferTypes, final DragSourceListener listener) {
        final DragSource dragSource = new DragSource(this, operations);
        dragSource.setTransfer(transferTypes);

        DragSourceListener wrapper = new DragSourceListener() {

            @Override
            public void dragStart(DragSourceEvent event) {
                listener.dragStart(event);
            }

            @Override
            public void dragSetData(DragSourceEvent event) {
                listener.dragSetData(event);
            }

            @Override
            public void dragFinished(DragSourceEvent event) {
                listener.dragFinished(event);
                // ensure to stop any current active internal drag mode
                NatTable.this.modeSupport.switchMode(Mode.NORMAL_MODE);
            }
        };

        dragSource.addDragListener(wrapper);
    }

    /**
     * Adds support for dropping items into this control via a user
     * drag-and-drop operation.
     *
     * @param operations
     *            a bitwise OR of the supported drag and drop operation types (
     *            <code>DROP_COPY</code>,<code>DROP_LINK</code>, and
     *            <code>DROP_MOVE</code>)
     * @param transferTypes
     *            the transfer types that are supported by the drop operation
     * @param listener
     *            the callback that will be invoked after the drag and drop
     *            operation finishes
     * @see org.eclipse.swt.dnd.DND
     */
    public void addDropSupport(final int operations,
            final Transfer[] transferTypes, final DropTargetListener listener) {
        final DropTarget dropTarget = new DropTarget(this, operations);
        dropTarget.setTransfer(transferTypes);
        dropTarget.addDropListener(listener);
    }

    // Theme styling

    /**
     * Will unregister the style configurations that were applied before by
     * another {@link ThemeConfiguration} and register the style configurations
     * of the given {@link ThemeConfiguration}.
     *
     * @param themeConfiguration
     *            The ThemeConfiguration that contains the style configurations
     *            to apply.
     */
    public void setTheme(ThemeConfiguration themeConfiguration) {
        this.themeManager.applyTheme(themeConfiguration);
        doCommand(new VisualRefreshCommand());
    }

    /**
     *
     * @return The {@link InternalCellClipboard} that is used for internal copy
     *         &amp; paste functionality.
     * @since 1.4
     */
    public InternalCellClipboard getInternalCellClipboard() {
        return this.clipboard;
    }

    // Editor
    /**
     * Returns the active cell editor that is currently open or {@code null} if
     * there is no editor active.
     *
     * @return the active editor or {@code null}
     */
    public ICellEditor getActiveCellEditor() {
        return this.activeCellEditor;
    }

    /**
     * Checks if there is an active cell editor registered. If there is one, it
     * is tried to commit the value that is currently entered there.
     *
     * @return <code>false</code> if there is an open editor that can not be
     *         committed because of conversion/validation errors,
     *         <code>true</code> if there is no active open editor or it could
     *         be closed after committing the value.
     */
    public boolean commitAndCloseActiveCellEditor() {
        if (this.activeCellEditor != null) {
            return this.activeCellEditor.commit(MoveDirectionEnum.NONE, true);
        }
        return true;
    }

    /**
     * Returns the labels that are used within this NatTable for conditional
     * styling.
     *
     * @return The labels that are used within this NatTable for conditional
     *         styling.
     *
     * @since 1.4
     */
    public Collection<String> getProvidedLabels() {
        if (this.underlyingLayer instanceof AbstractLayer) {
            return ((AbstractLayer) this.underlyingLayer).getProvidedLabels();
        }
        return Collections.emptySet();
    }
}
