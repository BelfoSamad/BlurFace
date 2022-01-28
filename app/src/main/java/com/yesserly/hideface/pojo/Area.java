package com.yesserly.hideface.pojo;

import android.graphics.Rect;

import java.util.Objects;

import htk.lib.areasimage.ClickableArea;

public class Area extends ClickableArea.State{

    private Rect rect;
    private boolean selected;
    private boolean stickered;

    public Area(Rect rect, boolean selected, boolean stickered) {
        this.rect = rect;
        this.selected = selected;
        this.stickered = stickered;
    }

    public Rect getRect() {
        return rect;
    }

    public void setRect(Rect rect) {
        this.rect = rect;
    }

    public boolean isSelected() {
        return selected;
    }

    public void setSelected(boolean selected) {
        this.selected = selected;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Area area = (Area) o;
        return Objects.equals(rect, area.rect);
    }

    public boolean isStickered() {
        return stickered;
    }

    public void setStickered(boolean stickered) {
        this.stickered = stickered;
    }
}
