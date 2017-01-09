package com.example.ciprian.project_afd;

import org.apache.commons.io.filefilter.IOFileFilter;

import java.io.File;

/**
 * Created by Ciprian on 09/01/2017.
 */

public class MyIOFileFilter implements IOFileFilter {

    private String searchWord;

    public MyIOFileFilter(String searchWord) {
        this.searchWord = searchWord.toLowerCase();
    }

    @Override
    public boolean accept(File file) {
        return file.getName().toLowerCase().contains(searchWord);
    }

    @Override
    public boolean accept(File dir, String name) {
        return name.toLowerCase().contains(searchWord);
    }
}
