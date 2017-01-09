package com.example.ciprian.project_afd;

import android.util.Log;

import org.apache.commons.io.FileUtils;
import org.apache.commons.io.filefilter.IOFileFilter;
import org.apache.commons.io.filefilter.TrueFileFilter;

import java.io.File;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;

/**
 * Created by Ciprian on 07/01/2017.
 */

public class FileSearch implements Runnable {

    private final File root;
    public boolean finish = false;
    private List<String> filesFound;
    private final String searchString;

    public FileSearch(File root, String searchString) {
        this.root = root;
        this.filesFound = new ArrayList<>();
        this.searchString = searchString.toLowerCase();
    }

    @Override
    public void run() {
        try {
            boolean recursive = true;

            MyIOFileFilter fileFilter = new MyIOFileFilter(searchString);

            Collection collection = FileUtils.listFilesAndDirs(root, fileFilter, fileFilter);

            Iterator iterator = collection.iterator();
            while (iterator.hasNext()) {
                File file = (File) iterator.next();
                filesFound.add(file.getAbsolutePath());
            }
            filesFound.remove(root.getAbsolutePath());
        } catch (Exception e) {
            Log.v("Exception", e.toString());
        } finally {
            finish = true;
        }
    }


    public List<String> getListOfFoundFiles() {
        return filesFound;
    }
}
