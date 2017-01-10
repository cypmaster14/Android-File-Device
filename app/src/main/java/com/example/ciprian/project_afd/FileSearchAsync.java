package com.example.ciprian.project_afd;

import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.os.AsyncTask;
import android.support.v7.widget.SearchView;
import android.view.MenuItem;
import android.widget.Button;

import java.io.File;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * Created by Ciprian on 10/01/2017.
 */

public class FileSearchAsync extends AsyncTask<FileSearch, Integer, List<String>> {

    private ProgressDialog progressDialog;
    private MainActivity context;
    private FileSearch fileSearch;
    private SearchView searchView;
    private MenuItem searchButton;

    public FileSearchAsync(MainActivity activity, SearchView searchView, MenuItem searchButton) {
        this.context = activity;
        this.searchView = searchView;
        this.searchButton = searchButton;
    }


    @Override
    protected void onPreExecute() {
        progressDialog = ProgressDialog.show(context, "Search", "Searching for files", false, true);
    }

    @Override
    protected List<String> doInBackground(FileSearch... params) {

        fileSearch = params[0];
        return fileSearch.getListOfFoundFiles();

    }


    @Override
    protected void onPostExecute(List<String> strings) {
        progressDialog.dismiss();
        Intent intent = new Intent(context, SearchActivity.class);

        searchView.setIconified(true);
        searchView.clearFocus();
        searchButton.collapseActionView();
        searchView.setQuery("", false);
        searchView.setIconified(true);

        intent.putExtra(MainActivity.FILES_FOUND, new ArrayList<>(strings));
        context.startActivityForResult(intent, 300);

    }
}
