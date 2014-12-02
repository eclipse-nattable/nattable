package org.eclipse.nebula.widgets.nattable.edit;

import org.eclipse.nebula.widgets.nattable.edit.editor.ICellEditor;
import org.eclipse.nebula.widgets.nattable.layer.ILayer;
import org.eclipse.nebula.widgets.nattable.layer.event.ILayerEvent;
import org.eclipse.swt.events.DisposeListener;

/**
 * The {@code CellEditorCreatedEvent} is fired by the
 * {@linkplain EditController} whenever a {@linkplain ICellEditor editor} has
 * been created.
 * <p>
 * Please keep in mind that there is no event fired when the editor is
 * destroyed. Clients should associate a {@linkplain DisposeListener dispose
 * listener} to the {@linkplain ICellEditor#getEditorControl() editor widget} to
 * be informed whenever the editor get destroyed.
 * </p>
 * <p>
 * This event is typically consumed by the {@code NatTable} itself to keep track
 * of the active editor. Any other layer may also consume this event to do
 * whatever necessary.
 * </p>
 */
public class CellEditorCreatedEvent implements ILayerEvent {

    private final ICellEditor editor;

    /**
     * Creates a new event passing the created editor
     *
     * @param editor
     *            the new editor
     */
    public CellEditorCreatedEvent(ICellEditor editor) {
        this.editor = editor;
    }

    @Override
    public boolean convertToLocal(ILayer localLayer) {
        return true; // no conversion needed
    }

    @Override
    public ILayerEvent cloneEvent() {
        return this; // cloning not needed
    }

    /**
     * Returns the editor associated with this event.
     *
     * @return the created editor
     */
    public ICellEditor getEditor() {
        return this.editor;
    }

}
