package com.example.ciprian.project_afd;

import java.io.File;

/**
 * Created by Ciprian on 24/12/2016.
 */

public class Item {

    public int icon;
    public String title;
    public String data;
    public String path;

    public Item() {
        super();
    }

    public Item(int icon, String title, String data, String path) {
        super();
        this.title = title;
        this.icon = icon;
        this.data = data;
        this.path = path;
    }
}
