package codetail.graphics.drawables;

public interface HotspotDrawable {

    /**
     * Specifies the hotspot's location within the drawable.
     *
     * @param x The X coordinate of the center of the hotspot
     * @param y The Y coordinate of the center of the hotspot
     */
    void setHotspot(float x, float y);

    /**
     * Sets the bounds to which the hotspot is constrained, if they should be
     * different from the drawable bounds.
     */
    void setHotspotBounds(int left, int top, int right, int bottom) ;

}
