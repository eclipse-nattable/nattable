package org.eclipse.nebula.widgets.nattable.core.ui.rendering;

/**
 * Wrapper class to transport transformation informations that can be used to
 * create a UI toolkit dependent transformation.
 */
public class Transform {

    private Float angle;

    private Float scaleX;
    private Float scaleY;

    private Float offsetX;
    private Float offsetY;

    /**
     * Modifies the receiver so that it represents a transformation that is
     * equivalent to its previous transformation rotated by the specified angle.
     * The angle is specified in degrees and for the identity transform 0
     * degrees is at the 3 o'clock position. A positive value indicates a
     * clockwise rotation while a negative value indicates a counter-clockwise
     * rotation.
     *
     * @param angle
     *            the angle to rotate the transformation by
     */
    public void rotate(float angle) {
        this.angle = angle;
    }

    /**
     * Modifies the receiver so that it represents a transformation that is
     * equivalent to its previous transformation scaled by (scaleX, scaleY).
     *
     * @param scaleX
     *            the amount to scale in the X direction
     * @param scaleY
     *            the amount to scale in the Y direction
     */
    public void scale(float scaleX, float scaleY) {
        this.scaleX = scaleX;
        this.scaleY = scaleY;
    }

    /**
     * Modifies the receiver so that it represents a transformation that is
     * equivalent to its previous transformation translated by (offsetX,
     * offsetY).
     *
     * @param offsetX
     *            the distance to translate in the X direction
     * @param offsetY
     *            the distance to translate in the Y direction
     */
    public void translate(float offsetX, float offsetY) {
        this.offsetX = offsetX;
        this.offsetY = offsetY;
    }

    /**
     * @return <code>true</code> if this transformation is configured for
     *         rotation
     */
    public boolean hasRotation() {
        return this.angle != null;
    }

    /**
     * @return <code>true</code> if this transformation is configured for
     *         scaling
     */
    public boolean hasScaling() {
        return this.scaleX != null && this.scaleY != null;
    }

    /**
     * @return <code>true</code> if this transformation is configured for
     *         translation
     */
    public boolean hasTranslation() {
        return this.offsetX != null && this.offsetY != null;
    }

    /**
     * @return the angle to rotate the transformation by
     */
    public float getAngle() {
        return this.angle;
    }

    /**
     * @return the amount to scale in the X direction
     */
    public float getScaleX() {
        return this.scaleX;
    }

    /**
     * @return the amount to scale in the Y direction
     */
    public float getScaleY() {
        return this.scaleY;
    }

    /**
     * @return the distance to translate in the X direction
     */
    public float getOffsetX() {
        return this.offsetX;
    }

    /**
     * @return the distance to translate in the Y direction
     */
    public float getOffsetY() {
        return this.offsetY;
    }
}
