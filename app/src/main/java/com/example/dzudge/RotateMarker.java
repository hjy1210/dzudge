package com.example.dzudge;

import org.mapsforge.core.graphics.Bitmap;
import org.mapsforge.core.graphics.Canvas;
import org.mapsforge.core.model.BoundingBox;
import org.mapsforge.core.model.LatLong;
import org.mapsforge.core.model.Point;
import org.mapsforge.core.model.Rectangle;
import org.mapsforge.core.util.MercatorProjection;
import org.mapsforge.map.android.graphics.AndroidGraphicFactory;
import org.mapsforge.map.layer.overlay.Marker;

class RotateMarker extends Marker {

    private Bitmap bitmap;
    private int horizontalOffset;
    private LatLong latLong;
    private int verticalOffset;

    protected float degree = 0.0f;
    protected float px = 0.0f;
    protected float py = 0.0f;

    public RotateMarker(LatLong latLong, Bitmap bitmap, int horizontalOffset, int verticalOffset) {
        super(latLong, bitmap, horizontalOffset, verticalOffset);

        this.latLong = latLong;
        this.bitmap = bitmap;
        this.horizontalOffset = horizontalOffset;
        this.verticalOffset = verticalOffset;
    }

    @Override
    public synchronized void draw(BoundingBox boundingBox, byte zoomLevel, Canvas canvas, Point topLeftPoint) {
        if (this.latLong == null || this.bitmap == null) {
            return;
        }
        long mapSize = MercatorProjection.getMapSize(zoomLevel, this.displayModel.getTileSize());
        double pixelX = MercatorProjection.longitudeToPixelX(this.latLong.longitude, mapSize);
        double pixelY = MercatorProjection.latitudeToPixelY(this.latLong.latitude, mapSize);

        int halfBitmapWidth = this.bitmap.getWidth() / 2;
        int halfBitmapHeight = this.bitmap.getHeight() / 2;

        int left = (int) (pixelX - topLeftPoint.x - halfBitmapWidth + this.horizontalOffset);
        int top = (int) (pixelY - topLeftPoint.y - halfBitmapHeight + this.verticalOffset);
        int right = left + this.bitmap.getWidth();
        int bottom = top + this.bitmap.getHeight();

        Rectangle bitmapRectangle = new Rectangle(left, top, right, bottom);
        Rectangle canvasRectangle = new Rectangle(0, 0, canvas.getWidth(), canvas.getHeight());
        if (!canvasRectangle.intersects(bitmapRectangle)) {
            return;
        }

        android.graphics.Canvas androidCanvas = AndroidGraphicFactory.getCanvas(canvas);
        androidCanvas.save();
        // How is the pivot value calculated ?
        this.px = (float) canvas.getWidth()/2;
        this.py = (float) canvas.getHeight()/2;
        //androidCanvas.rotate(this.degree, this.px, this.py);
        androidCanvas.rotate(degree, (float) (pixelX - topLeftPoint.x), (float) (pixelY - topLeftPoint.y));
        canvas.drawBitmap(this.bitmap, left, top);
        androidCanvas.restore();
    }


    public RotateMarker setDegree(float degree) {
        this.degree = degree;

        return this;
    }

    public RotateMarker setPivotPoint(float px, float py) {
        this.px = px;
        this.py = py;

        return this;
    }
}