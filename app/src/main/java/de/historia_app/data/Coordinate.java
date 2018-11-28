package de.historia_app.data;

import android.graphics.Point;

import com.j256.ormlite.field.DataType;
import com.j256.ormlite.field.DatabaseField;

public class Coordinate {

    @DatabaseField(columnName = "id", id = true, dataType = DataType.LONG)
    private long id;

    @DatabaseField(columnName = "x")
    private float x;

    @DatabaseField(columnName = "y")
    private float y;

    @DatabaseField(columnName = "scene", foreign = true, foreignAutoRefresh = true)
    private Scene scene;

    @DatabaseField(columnName = "mapstop", foreign = true, foreignAutoRefresh = true)
    private Mapstop mapstop;

    public Coordinate() {}

    public long getId() { return id; }

    public void setId(long id) { this.id = id; }

    public float getX() { return x; }

    public void setX(float x) { this.x = x; }

    public float getY() { return y; }

    public void setY(float y) { this.y = y; }

    public Scene getScene() { return scene; }

    public void setScene(Scene scene) { this.scene = scene; }

    public Mapstop getMapstop() { return mapstop; }

    public void setMapstop(Mapstop mapstop) { this.mapstop = mapstop; }
}
