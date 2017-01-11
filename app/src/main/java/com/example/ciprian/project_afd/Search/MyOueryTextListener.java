package com.example.ciprian.project_afd.Search;

import android.util.Log;
import android.view.MenuItem;
import android.support.v7.widget.SearchView;

import com.example.ciprian.project_afd.FileOperations.FileSearch;
import com.example.ciprian.project_afd.FileOperations.FileSearchAsync;
import com.example.ciprian.project_afd.MainActivity;

import java.io.File;

/**
 * Created by Ciprian on 10/01/2017.
 */

public class MyOueryTextListener implements SearchView.OnQueryTextListener {


    private File root;
    private SearchView searchView;
    private MenuItem searchButton;
    private MainActivity context;

    public MyOueryTextListener(File root, SearchView searchView, MenuItem searchButton, MainActivity context) {
        this.root = root;
        this.searchView = searchView;
        this.searchButton = searchButton;
        this.context = context;
    }

    @Override
    public boolean onQueryTextSubmit(String query) {
        Log.v("File Entered", query);


        FileSearch fileSearch = new FileSearch(root, query.trim());
        new FileSearchAsync(context, searchView, searchButton).execute(fileSearch);

        return false;
    }

    @Override
    public boolean onQueryTextChange(String newText) {
        return false;
    }
}
