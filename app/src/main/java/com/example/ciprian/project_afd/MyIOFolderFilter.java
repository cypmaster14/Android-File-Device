package com.example.ciprian.project_afd;

import org.apache.commons.io.filefilter.IOFileFilter;

import java.io.File;

/**
 * Created by Ciprian on 10/01/2017.
 */

public class MyIOFolderFilter implements IOFileFilter {

    private String searchWord;

    public MyIOFolderFilter(String searchWord) {
        this.searchWord = searchWord.toLowerCase();
    }

    @Override
    public boolean accept(File file) {
        return false;
    }

    @Override
    public boolean accept(File dir, String name) {
        return dir.getName().toLowerCase().contains(searchWord);
    }
}
