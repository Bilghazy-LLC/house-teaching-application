package io.codelabs.digitutor.core.util;

/**
 * Interface for handling click events
 * @param <Item>
 */
public interface OnClickListener<Item> {
    void onClick(Item item, boolean isLongClick);
}
