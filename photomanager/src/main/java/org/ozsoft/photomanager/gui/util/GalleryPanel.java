package org.ozsoft.photomanager.gui.util;

import java.util.HashSet;
import java.util.Set;

import javax.swing.JPanel;
import javax.swing.border.LineBorder;

import org.ozsoft.photomanager.gui.UIConstants;

/**
 * Gallery panel for browsing selectable items in a top-left to bottom-right layout.
 * 
 * @author Oscar Stigter
 */
public abstract class GalleryPanel<T extends GalleryItem<?>> extends JPanel {

    private static final long serialVersionUID = 6576478313016045049L;

    private final Set<GalleryItem<T>> items;

    private GalleryItem<T> selectedItem;

    /**
     * Constructor.
     */
    public GalleryPanel() {
        items = new HashSet<GalleryItem<T>>();
        setLayout(new GalleryLayout());
        setBorder(new LineBorder(UIConstants.LINE_COLOR));
    }

    /**
     * Adds an item to the gallery.
     * 
     * @param item
     *            The item.
     */
    protected final void addItem(GalleryItem<T> item) {
        add(item);
        items.add(item);
        revalidate();
    }

    protected void itemSelected(GalleryItem<T> item) {
        if (selectedItem != null) {
            selectedItem.setSelected(false);
        }
        selectedItem = item;
        item.setSelected(true);
    }

    protected void itemOpened(GalleryItem<T> item) {
        // Empty implementation by default.
    }

}
