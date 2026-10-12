package com.train.game;

import java.util.ArrayList;
import java.util.List;

public class Station {
    public final int index;
    public final String nameEn;
    public final String nameBn;
    public final String code;
    public final int km;
    public final double distPx; // position along the railway line in pixels
    public final int platformLeft;
    public final int platformRight;
    public final String roofType;
    public final String roofColor;
    public final int crowd;
    public final int alight;
    public final int idle;
    public final List<String> extras;

    public Station(int index, String nameEn, String nameBn, String code, int km, double distPx,
                   int platformLeft, int platformRight, String roofType, String roofColor,
                   int crowd, int alight, int idle, List<String> extras) {
        this.index = index;
        this.nameEn = nameEn;
        this.nameBn = nameBn;
        this.code = code;
        this.km = km;
        this.distPx = distPx;
        this.platformLeft = platformLeft;
        this.platformRight = platformRight;
        this.roofType = roofType;
        this.roofColor = roofColor;
        this.crowd = crowd;
        this.alight = alight;
        this.idle = idle;
        this.extras = extras != null ? extras : new ArrayList<>();
    }
}
