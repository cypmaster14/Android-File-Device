package com.example.ciprian.project_afd;

import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.NavUtils;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ListView;

import java.io.File;
import java.text.DecimalFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;


public class SearchActivity extends AppCompatActivity implements AdapterView.OnItemClickListener {

    public static final String FOUND_FOLDER_NAME = "FOLDER_NAME";
    private ItemAdapter adapter;
    private ListView searchListView;
    private List<Item> items;
    private List<File> filesFound = new ArrayList<>();


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.search_activity);

        String[] filesFound = this.getIntent().getExtras().getStringArray(MainActivity.FILES_FOUND);
        if (filesFound != null) {
            if (filesFound.length == 0) {
                new AlertDialog.Builder(SearchActivity.this)
                        .setTitle("Search")
                        .setMessage("No files were found")
                        .setPositiveButton("Ok", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                NavUtils.navigateUpFromSameTask(SearchActivity.this);
                            }
                        })
                        .show();
            }
        }

        searchListView = (ListView) findViewById(R.id.search_listview);
        items = new ArrayList<>();
        populateListView(filesFound);
        adapter = new ItemAdapter(this, R.layout.listview_item_row, items);
        searchListView.setAdapter(adapter);
        searchListView.setOnItemClickListener(this);

        getSupportActionBar().setTitle("Files Found");
    }


    private void populateListView(String[] files) {
        getFilesFound(files);
        for (File file : filesFound) {
            if (file.isFile()) {
                addFileToListView(file);
            } else {
                addFolderToListView(file);
            }
        }
    }

    private void getFilesFound(String[] files) {
        for (String file : files) {
            filesFound.add(new File(file));
        }
    }

    private void addFileToListView(File file) {
        Date lastModified = new Date(file.lastModified());
        SimpleDateFormat formatter = new SimpleDateFormat("dd/MM/yyyy HH:mm");
        DecimalFormat twoDForm = new DecimalFormat("#.##");
        String value = String.valueOf(twoDForm.format(file.length() / 1024.0));
        items.add(new Item(R.drawable.file1, file.getName(), formatter.format(lastModified), value + " KB"));
        Log.v("File added", file.getAbsolutePath());
    }

    private void addFolderToListView(File folder) {
        Date lastModified = new Date(folder.lastModified());
        SimpleDateFormat formatter = new SimpleDateFormat("dd/MM/yyyy HH:mm");
        items.add(new Item(R.drawable.folder1, folder.getName(), formatter.format(lastModified), String.valueOf(folder.list().length) + " Files"));
        Log.v("Folder added", folder.getAbsolutePath());

    }

    @Override
    public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
        Item itemClicked = items.get(position);
        Log.v("File clicked", itemClicked.title);
        File fileClicked = getFileByName(itemClicked.title);
        if (fileClicked.isDirectory()) {
            //Send to parent the name of directory
            sendInfoToParent(fileClicked.getAbsolutePath());
            finish();
        } else {
            Intent textEditorActivity = new Intent(SearchActivity.this, TextEditor.class);
            textEditorActivity.putExtra(MainActivity.FILE_TO_OPEN, fileClicked.getAbsolutePath());
            startActivityForResult(textEditorActivity, MainActivity.FILE_MODIFIED);
        }


    }

    private void sendInfoToParent(String folderName) {
        Intent intent = new Intent();
        intent.putExtra(FOUND_FOLDER_NAME, folderName);
        setResult(MainActivity.SEARCH_FOLDER_CLICKED, intent);
        Log.v("Name of folder to parent", folderName);
        finish();
    }

    private File getFileByName(String name) {
        for (File file : filesFound) {
            Log.v("FileGetName", file.getName());
            if (file.getName().equals(name)) {
                Log.v("File found", file.getName());
                return file;
            }
        }

        return null;
    }

}
