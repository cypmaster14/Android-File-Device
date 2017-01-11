package com.example.ciprian.project_afd.CustomListView;

/**
 * Created by Ciprian on 24/12/2016.
 */

public class Item {

    public int icon;
    public String title;
    public String data;
    public String size;

    public Item() {
        super();
    }

    public Item(int icon, String title, String data, String size) {
        super();
        this.title = title;
        this.icon = icon;
        this.data = data;
        this.size = size;
    }
}
