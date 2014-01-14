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
package org.eclipse.nebula.widgets.nattable;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Properties;

import org.eclipse.nebula.widgets.nattable.command.DisposeResourcesCommand;
import org.eclipse.nebula.widgets.nattable.command.ILayerCommand;
import org.eclipse.nebula.widgets.nattable.command.ILayerCommandHandler;
import org.eclipse.nebula.widgets.nattable.command.StructuralRefreshCommand;
import org.eclipse.nebula.widgets.nattable.config.ConfigRegistry;
import org.eclipse.nebula.widgets.nattable.config.DefaultNatTableStyleConfiguration;
import org.eclipse.nebula.widgets.nattable.config.IConfigRegistry;
import org.eclipse.nebula.widgets.nattable.config.IConfiguration;
import org.eclipse.nebula.widgets.nattable.conflation.EventConflaterChain;
import org.eclipse.nebula.widgets.nattable.conflation.IEventConflater;
import org.eclipse.nebula.widgets.nattable.conflation.VisualChangeEventConflater;
import org.eclipse.nebula.widgets.nattable.coordinate.Range;
import org.eclipse.nebula.widgets.nattable.edit.ActiveCellEditorRegistry;
import org.eclipse.nebula.widgets.nattable.edit.command.EditUtils;
import org.eclipse.nebula.widgets.nattable.grid.command.ClientAreaResizeCommand;
import org.eclipse.nebula.widgets.nattable.grid.command.InitializeGridCommand;
import org.eclipse.nebula.widgets.nattable.layer.ILayer;
import org.eclipse.nebula.widgets.nattable.layer.ILayerListener;
import org.eclipse.nebula.widgets.nattable.layer.LabelStack;
import org.eclipse.nebula.widgets.nattable.layer.cell.ILayerCell;
import org.eclipse.nebula.widgets.nattable.layer.event.CellVisualUpdateEvent;
import org.eclipse.nebula.widgets.nattable.layer.event.ILayerEvent;
import org.eclipse.nebula.widgets.nattable.layer.event.IVisualChangeEvent;
import org.eclipse.nebula.widgets.nattable.layer.stack.DummyGridLayerStack;
import org.eclipse.nebula.widgets.nattable.painter.IOverlayPainter;
import org.eclipse.nebula.widgets.nattable.painter.cell.ICellPainter;
import org.eclipse.nebula.widgets.nattable.painter.layer.ILayerPainter;
import org.eclipse.nebula.widgets.nattable.painter.layer.NatLayerPainter;
import org.eclipse.nebula.widgets.nattable.persistence.IPersistable;
import org.eclipse.nebula.widgets.nattable.selection.event.CellSelectionEvent;
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
import org.eclipse.swt.widgets.Event;
import org.eclipse.swt.widgets.Listener;
import org.eclipse.swt.widgets.ScrollBar;

public class NatTable extends Canvas implements ILayer, PaintListener, IClientAreaProvider, ILayerListener, IPersistable {

	public static final int DEFAULT_STYLE_OPTIONS = SWT.NO_BACKGROUND | SWT.NO_REDRAW_RESIZE | SWT.DOUBLE_BUFFERED  | SWT.V_SCROLL | SWT.H_SCROLL;

	/**
	 * Key that is used for loading NatTable states.
	 * Is set to <code>true</code> in case the initial painting is not finished yet.
	 * In this case there is no need to call refresh commands on loading.
	 */
	public static final String INITIAL_PAINT_COMPLETE_FLAG = "NatTable.initialPaintComplete"; //$NON-NLS-1$
	
	private UiBindingRegistry uiBindingRegistry;

	private ModeSupport modeSupport;

	private final EventConflaterChain conflaterChain = new EventConflaterChain();

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
	 * It is added to the parent composite and will close an active cell editor in case the parent is resized. 
	 * We need to listen to the parent composite resize, because resizing a shell or a part in e4 does not 
	 * cause loosing the focus. Therefore the editor will stay open in such cases. As this causes rendering issues
	 * when using percentage sizing, this listener closes an editor on parent composite resize.<br/>
	 * It is not registered as listener to NatTable itself, because this would have impact when filtering or
	 * dynamic updates cause scrollbars to become visible/invisible, which result in resizing of the NatTable.
	 */
	private Listener closeEditorOnParentResize = new Listener() {
		@Override
		public void handleEvent(Event event) {
			//as resizing doesn't cause the current active editor to loose focus
			//we are closing the current active editor manually
			if (!EditUtils.commitAndCloseActiveEditor()) {
				//if committing didn't work out we need to perform a hard close
				//otherwise the state of the table would be unstale
				ActiveCellEditorRegistry.getActiveCellEditor().close();
			}
		}
	};
	
	/**
	 * This flag is used to deal with runtime issues on loading states while the initial
	 * rendering is not finished yet.
	 */
	private boolean initialPaintComplete = false;
	
	public NatTable(Composite parent) {
		this(parent, DEFAULT_STYLE_OPTIONS);
	}

	/**
	 * @param parent widget for the table.
	 * @param autoconfigure if set to False
	 *    - No auto configuration is done
	 *    - Default settings are <i>not</i> loaded. Configuration(s) have to be manually
	 *      added by invoking addConfiguration(). At the minimum the {@link DefaultNatTableStyleConfiguration}
	 *      must be added for the table to render.
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

	public NatTable(final Composite parent, final int style, final ILayer layer, boolean autoconfigure) {
		super(parent, style);

		// Disable scroll bars by default; if a Viewport is available, it will enable the scroll bars
		disableScrollBar(getHorizontalBar());
		disableScrollBar(getVerticalBar());

		initInternalListener();

		internalSetLayer(layer);

		this.autoconfigure = autoconfigure;
		if (autoconfigure) {
			configurations.add(new DefaultNatTableStyleConfiguration());
			configure();
		}

		conflaterChain.add(getVisualChangeEventConflater());
		conflaterChain.start();

		parent.addListener(SWT.Resize, closeEditorOnParentResize);
		
		addDisposeListener(new DisposeListener() {

			@Override
			public void widgetDisposed(DisposeEvent e) {
				doCommand(new DisposeResourcesCommand());
				conflaterChain.stop();
				ActiveCellEditorRegistry.unregisterActiveCellEditor();
				layer.dispose();
				
				parent.removeListener(SWT.Resize, closeEditorOnParentResize);
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
		return underlyingLayer;
	}

	public void setLayer(ILayer layer) {
		if (autoconfigure) {
			throw new IllegalStateException("May only set layer post construction if autoconfigure is turned off"); //$NON-NLS-1$
		}

		internalSetLayer(layer);
	}

	private void internalSetLayer(ILayer layer) {
		if (layer != null) {
			this.underlyingLayer = layer;
			underlyingLayer.setClientAreaProvider(new IClientAreaProvider() {

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
			underlyingLayer.addLayerListener(this);
		}
	}

	/**
	 * Adds a configuration to the table.
	 * <p>
	 * Configurations are processed when the {@link #configure()} method is invoked.
	 * Each configuration object then has a chance to configure the
	 * 	<ol>
	 * 		<li>ILayer</li>
	 * 		<li>ConfigRegistry</li>
	 * 		<li>UiBindingRegistry</li>
	 *  </ol>
	 */
	public void addConfiguration(IConfiguration configuration) {
		if (autoconfigure) {
			throw new IllegalStateException("May only add configurations post construction if autoconfigure is turned off"); //$NON-NLS-1$
		}

		configurations.add(configuration);
	}

	/**
	 * @return {@link IConfigRegistry} used to hold the configuration bindings
	 * 	by Layer, DisplayMode and Config labels.
	 */
	public IConfigRegistry getConfigRegistry() {
		if (configRegistry == null) {
			configRegistry = new ConfigRegistry();
		}
		return configRegistry;
	}

	public void setConfigRegistry(IConfigRegistry configRegistry) {
		if (autoconfigure) {
			throw new IllegalStateException("May only set config registry post construction if autoconfigure is turned off"); //$NON-NLS-1$
		}

		this.configRegistry = configRegistry;
	}

	/**
	 * @return Registry holding all the UIBindings contributed by the underlying layers
	 */
	public UiBindingRegistry getUiBindingRegistry() {
		if (uiBindingRegistry == null) {
			uiBindingRegistry = new UiBindingRegistry(this);
		}
		return uiBindingRegistry;
	}

	public void setUiBindingRegistry(UiBindingRegistry uiBindingRegistry) {
		if (autoconfigure) {
			throw new IllegalStateException("May only set UI binding registry post construction if autoconfigure is turned off"); //$NON-NLS-1$
		}

		this.uiBindingRegistry = uiBindingRegistry;
	}

	public String getID() {
		return id;
	}

	@Override
	protected void checkSubclass() {
	}

	protected void initInternalListener() {
		modeSupport = new ModeSupport(this);
		modeSupport.registerModeEventHandler(Mode.NORMAL_MODE, new ConfigurableModeEventHandler(modeSupport, this));
		modeSupport.switchMode(Mode.NORMAL_MODE);

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
		return overlayPainters;
	}
	
	public void addOverlayPainter(IOverlayPainter overlayPainter) {
		overlayPainters.add(overlayPainter);
	}

	public void removeOverlayPainter(IOverlayPainter overlayPainter) {
		overlayPainters.remove(overlayPainter);
	}

	@Override
	public void paintControl(final PaintEvent event) {
		paintNatTable(event);
		initialPaintComplete = true;
	}

	private void paintNatTable(final PaintEvent event) {
		getLayerPainter().paintLayer(this, event.gc, 0, 0, new Rectangle(event.x, event.y, event.width, event.height), getConfigRegistry());
	}

	@Override
	public ILayerPainter getLayerPainter() {
		return layerPainter;
	}

	public void setLayerPainter(ILayerPainter layerPainter) {
		this.layerPainter = layerPainter;
	}

	/**
	 * Repaint only a specific column in the grid. This method is optimized so that only the specific column is
	 * repainted and nothing else.
	 *
	 * @param columnPosition column of the grid to repaint
	 */
	public void repaintColumn(int columnPosition) {
		int xOffset = getStartXOfColumnPosition(columnPosition);
		if (xOffset < 0) {
			return;
		}
		redraw(xOffset, 0, getColumnWidthByPosition(columnPosition), getHeight(), true);
	}

	/**
	 * Repaint only a specific row in the grid. This method is optimized so that only the specific row is repainted and
	 * nothing else.
	 *
	 * @param rowPosition row of the grid to repaint
	 */
	public void repaintRow(int rowPosition) {
		int yOffset = getStartYOfRowPosition(rowPosition);
		if (yOffset < 0) {
			return;
		}
		redraw(0, yOffset, getWidth(), getRowHeightByPosition(rowPosition), true);
	}

	/**
	 * Repaint only a specific cell in the grid. This method is optimized so that only the specific cell is repainted and
	 * nothing else.
	 *
	 * @param columnPosition column position of the cell to repaint
	 * @param rowPosition row position of the cell to repaint
	 */
	public void repaintCell(int columnPosition, int rowPosition) {
		int xOffset = getStartXOfColumnPosition(columnPosition);
		int yOffset = getStartYOfRowPosition(rowPosition);
		
		redraw(xOffset, yOffset, getColumnWidthByPosition(columnPosition), getRowHeightByPosition(rowPosition), true);
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
	 * Refreshes the entire NatTable as every layer will be refreshed.
	 */
	public void refresh() {
		doCommand(new StructuralRefreshCommand());
	}
	
	@Override
	public void configure(ConfigRegistry configRegistry, UiBindingRegistry uiBindingRegistry) {
		throw new UnsupportedOperationException("Cannot use this method to configure NatTable. Use no-argument configure() instead."); //$NON-NLS-1$
	}

	/**
	 * Processes all the registered {@link IConfiguration} (s).
	 * All the underlying layers are walked and given a chance to configure.
	 * Note: all desired configuration tweaks must be done <i>before</i> this method is invoked.
	 */
	public void configure() {
		if (underlyingLayer == null) {
			throw new IllegalStateException("Layer must be set before configure is called"); //$NON-NLS-1$
		}

		if (underlyingLayer != null) {
			underlyingLayer.configure((ConfigRegistry) getConfigRegistry(), getUiBindingRegistry());
		}

		for (IConfiguration configuration : configurations) {
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
		for (ILayerListener layerListener : listeners) {
			layerListener.handleLayerEvent(event);
		}

		if (event instanceof CellVisualUpdateEvent) {
			CellVisualUpdateEvent update = (CellVisualUpdateEvent)event;
			repaintCell(update.getColumnPosition(), update.getRowPosition());
			return;
		}
		
	    if (event instanceof IVisualChangeEvent) {
	    	conflaterChain.addEvent(event);
	    }

		if (event instanceof CellSelectionEvent) {
			Event e = new Event();
			e.widget = this;
			try {
				notifyListeners(SWT.Selection, e);
			} catch (RuntimeException re) {
				re.printStackTrace();
			}
		}
	}


	// ILayer /////////////////////////////////////////////////////////////////

	// Persistence

	/**
	 * Save the state of the table to the properties object.
	 * {@link ILayer#saveState(String, Properties)} is invoked on all the underlying layers.
	 * This properties object will be populated with the settings of all underlying layers
	 * and any {@link IPersistable} registered with those layers.
	 */
	@Override
	public void saveState(final String prefix, final Properties properties) {
		BusyIndicator.showWhile(null, new Runnable() {
			
			@Override
			public void run() {
				underlyingLayer.saveState(prefix, properties);
			}
		});
	}

	/**
	 * Restore the state of the underlying layers from the values in the properties object.
	 * @see #saveState(String, Properties)
	 */
	@Override
	public void loadState(final String prefix, final Properties properties) {
		BusyIndicator.showWhile(null, new Runnable() {
			
			@Override
			public void run() {
				//if the initial painting is not finished yet, tell this the underlying 
				//mechanisms so there will be no refresh events fired
				if (!initialPaintComplete)
					properties.setProperty(INITIAL_PAINT_COMPLETE_FLAG, "true");  //$NON-NLS-1$
					
				underlyingLayer.loadState(prefix, properties);
			}
		});
	}

	/**
	 * @see ILayer#registerPersistable(IPersistable)
	 */
	@Override
	public void registerPersistable(IPersistable persistable) {
		persistables.add(persistable);
	}

	@Override
	public void unregisterPersistable(IPersistable persistable) {
		persistables.remove(persistable);
	}

	// Command

	@Override
	public boolean doCommand(ILayerCommand command) {
		return underlyingLayer.doCommand(command);
	}

	@Override
	public void registerCommandHandler(ILayerCommandHandler<?> commandHandler) {
		underlyingLayer.registerCommandHandler(commandHandler);
	}

	@Override
	public void unregisterCommandHandler(Class<? extends ILayerCommand> commandClass) {
		underlyingLayer.unregisterCommandHandler(commandClass);
	}
	
	// Events

	private final List<ILayerListener> listeners = new ArrayList<ILayerListener>();

	@Override
	public void fireLayerEvent(ILayerEvent event) {
		underlyingLayer.fireLayerEvent(event);
	}

	@Override
	public void addLayerListener(ILayerListener listener) {
		listeners.add(listener);
	}

	@Override
	public void removeLayerListener(ILayerListener listener) {
		listeners.remove(listener);
	}
	
	@Override
	public boolean hasLayerListener(Class<? extends ILayerListener> layerListenerClass) {
		for (ILayerListener listener : listeners) {
			if (listener.getClass().equals(layerListenerClass)) {
				return true;
			}
		}
		return false;
	}

	// Columns

	@Override
	public int getColumnCount() {
		return underlyingLayer.getColumnCount();
	}

	@Override
	public int getPreferredColumnCount() {
		return underlyingLayer.getPreferredColumnCount();
	}

	@Override
	public int getColumnIndexByPosition(int columnPosition) {
		return underlyingLayer.getColumnIndexByPosition(columnPosition);
	}

	@Override
	public int localToUnderlyingColumnPosition(int localColumnPosition) {
		return localColumnPosition;
	}

	@Override
	public int underlyingToLocalColumnPosition(ILayer sourceUnderlyingLayer, int underlyingColumnPosition) {
		if (sourceUnderlyingLayer != underlyingLayer) {
			return -1;
		}

		return underlyingColumnPosition;
	}

	@Override
	public Collection<Range> underlyingToLocalColumnPositions(ILayer sourceUnderlyingLayer, Collection<Range> underlyingColumnPositionRanges) {
		if (sourceUnderlyingLayer != underlyingLayer) {
			return null;
		}

		return underlyingColumnPositionRanges;
	}

	// Width

	@Override
	public int getWidth() {
		return underlyingLayer.getWidth();
	}

	@Override
	public int getPreferredWidth() {
		return underlyingLayer.getPreferredWidth();
	}

	@Override
	public int getColumnWidthByPosition(int columnPosition) {
		return underlyingLayer.getColumnWidthByPosition(columnPosition);
	}

	// Column resize

	@Override
	public boolean isColumnPositionResizable(int columnPosition) {
		return underlyingLayer.isColumnPositionResizable(columnPosition);
	}

	// X

	@Override
	public int getColumnPositionByX(int x) {
		return underlyingLayer.getColumnPositionByX(x);
	}

	@Override
	public int getStartXOfColumnPosition(int columnPosition) {
		return underlyingLayer.getStartXOfColumnPosition(columnPosition);
	}

	// Underlying

	@Override
	public Collection<ILayer> getUnderlyingLayersByColumnPosition(int columnPosition) {
		Collection<ILayer> underlyingLayers = new HashSet<ILayer>();
		underlyingLayers.add(underlyingLayer);
		return underlyingLayers;
	}

	// Rows

	@Override
	public int getRowCount() {
		return underlyingLayer.getRowCount();
	}

	@Override
	public int getPreferredRowCount() {
		return underlyingLayer.getPreferredRowCount();
	}

	@Override
	public int getRowIndexByPosition(int rowPosition) {
		return underlyingLayer.getRowIndexByPosition(rowPosition);
	}

	@Override
	public int localToUnderlyingRowPosition(int localRowPosition) {
		return localRowPosition;
	}

	@Override
	public int underlyingToLocalRowPosition(ILayer sourceUnderlyingLayer, int underlyingRowPosition) {
		if (sourceUnderlyingLayer != underlyingLayer) {
			return -1;
		}

		return underlyingRowPosition;
	}

	@Override
	public Collection<Range> underlyingToLocalRowPositions(ILayer sourceUnderlyingLayer, Collection<Range> underlyingRowPositionRanges) {
		if (sourceUnderlyingLayer != underlyingLayer) {
			return null;
		}

		return underlyingRowPositionRanges;
	}

	// Height

	@Override
	public int getHeight() {
		return underlyingLayer.getHeight();
	}

	@Override
	public int getPreferredHeight() {
		return underlyingLayer.getPreferredHeight();
	}

	@Override
	public int getRowHeightByPosition(int rowPosition) {
		return underlyingLayer.getRowHeightByPosition(rowPosition);
	}

	// Row resize

	@Override
	public boolean isRowPositionResizable(int rowPosition) {
		return underlyingLayer.isRowPositionResizable(rowPosition);
	}

	// Y

	@Override
	public int getRowPositionByY(int y) {
		return underlyingLayer.getRowPositionByY(y);
	}

	@Override
	public int getStartYOfRowPosition(int rowPosition) {
		return underlyingLayer.getStartYOfRowPosition(rowPosition);
	}

	// Underlying

	@Override
	public Collection<ILayer> getUnderlyingLayersByRowPosition(int rowPosition) {
		Collection<ILayer> underlyingLayers = new HashSet<ILayer>();
		underlyingLayers.add(underlyingLayer);
		return underlyingLayers;
	}

	// Cell features

	@Override
	public ILayerCell getCellByPosition(int columnPosition, int rowPosition) {
		return underlyingLayer.getCellByPosition(columnPosition, rowPosition);
	}

	@Override
	public Rectangle getBoundsByPosition(int columnPosition, int rowPosition) {
		return underlyingLayer.getBoundsByPosition(columnPosition, rowPosition);
	}

	@Override
	public String getDisplayModeByPosition(int columnPosition, int rowPosition) {
		return underlyingLayer.getDisplayModeByPosition(columnPosition, rowPosition);
	}
	
	@Override
	public LabelStack getConfigLabelsByPosition(int columnPosition, int rowPosition) {
		return underlyingLayer.getConfigLabelsByPosition(columnPosition, rowPosition);
	}

	@Override
	public Object getDataValueByPosition(int columnPosition, int rowPosition) {
		return underlyingLayer.getDataValueByPosition(columnPosition, rowPosition);
	}
	
	@Override
	public ICellPainter getCellPainter(int columnPosition, int rowPosition, ILayerCell cell, IConfigRegistry configRegistry) {
		return underlyingLayer.getCellPainter(columnPosition, rowPosition, cell, configRegistry);
	}

	// IRegionResolver

	@Override
	public LabelStack getRegionLabelsByXY(int x, int y) {
		return underlyingLayer.getRegionLabelsByXY(x, y);
	}

	@Override
	public ILayer getUnderlyingLayerByPosition(int columnPosition, int rowPosition) {
		return underlyingLayer;
	}

	@Override
	public IClientAreaProvider getClientAreaProvider() {
		return this;
	}

	@Override
	public void setClientAreaProvider(IClientAreaProvider clientAreaProvider) {
		underlyingLayer.setClientAreaProvider(clientAreaProvider);
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
	public void addDragSupport(final int operations, final Transfer[] transferTypes, final DragSourceListener listener) {
		final DragSource dragSource = new DragSource(this, operations);
		dragSource.setTransfer(transferTypes);
		dragSource.addDragListener(listener);
	}

	/**
	 * Adds support for dropping items into this control via a user drag-and-drop
	 * operation.
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
	public void addDropSupport(final int operations, final Transfer[] transferTypes, final DropTargetListener listener) {
		final DropTarget dropTarget = new DropTarget(this, operations);
		dropTarget.setTransfer(transferTypes);
		dropTarget.addDropListener(listener);
	}
}
