package com.example.ciprian.project_afd;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

/**
 * Created by Ciprian on 07/01/2017.
 */

public class FileSearch {

    private final File root;
    private List<String> filesFound;
    private final String searchString;
    private MyFileFilter myFileFilter;

    public FileSearch(File root, String searchString) {
        this.root = root;
        this.filesFound = new ArrayList<>();
        this.searchString = searchString.toLowerCase();
        this.myFileFilter = new MyFileFilter(searchString);
    }

    private void searchForFiles(File currentFile) {

        File[] files = currentFile.listFiles(myFileFilter);
        for (File fileFound : files) {
            if (fileFound.isDirectory()) {
                if (fileFound.getName().toLowerCase().contains(searchString)) {
                    filesFound.add(fileFound.getAbsolutePath());
                }
                searchForFiles(fileFound);
            } else {
                filesFound.add(fileFound.getAbsolutePath());
            }
        }
    }


    public List<String> getListOfFoundFiles() {
        searchForFiles(root);
        return filesFound;
    }
}
