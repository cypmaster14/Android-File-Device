package com.example.ciprian.project_afd.FileOperations;

import java.io.File;
import java.util.Comparator;

/**
 * Created by Ciprian on 24/12/2016.
 */

public class FilesComparator implements Comparator<File> {
    @Override
    public int compare(File o1, File o2) {
        return o1.getName().toLowerCase().compareTo(o2.getName().toLowerCase());
    }
}
