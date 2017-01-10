package com.example.ciprian.project_afd;

import java.io.File;
import java.io.FileFilter;

/**
 * Created by Ciprian on 10/01/2017.
 */

public class MyFileFilter implements FileFilter {

    private String query_file;

    public MyFileFilter(String query_file) {
        this.query_file = query_file;
    }


    @Override
    public boolean accept(File pathname) {
        return pathname.getName().toLowerCase().contains(query_file) || pathname.isDirectory();
    }
}
