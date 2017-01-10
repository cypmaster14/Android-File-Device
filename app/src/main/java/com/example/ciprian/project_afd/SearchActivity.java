package com.example.ciprian.project_afd;

import android.content.DialogInterface;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.support.v4.app.NavUtils;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ListView;
import android.widget.Toast;

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
    public final static String FILES_FOUND = "FILES_FOUND";
    private List<String> nameFilesFound;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.search_activity);

        if (savedInstanceState != null) {
            nameFilesFound = savedInstanceState.getStringArrayList(FILES_FOUND);
        } else {
            nameFilesFound = this.getIntent().getExtras().getStringArrayList(MainActivity.FILES_FOUND);
        }

        if (nameFilesFound.size() == 0) {
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

        searchListView = (ListView) findViewById(R.id.search_listview);
        items = new ArrayList<>();
        populateListView(nameFilesFound);
        adapter = new ItemAdapter(this, R.layout.listview_item_row, items);
        searchListView.setAdapter(adapter);
        searchListView.setOnItemClickListener(this);

        getSupportActionBar().setTitle("Files Found");
    }


    private void populateListView(List<String> files) {
        getFilesFound(files);
        for (File file : filesFound) {
            if (file.isFile()) {
                addFileToListView(file);
            } else {
                addFolderToListView(file);
            }
        }
    }

    private void getFilesFound(List<String> files) {
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
        try {
            if (fileClicked.isDirectory()) {
                //Send to parent the name of directory
                sendInfoToParent(fileClicked.getAbsolutePath());
                finish();
            } else {
                if (itemClicked.title.endsWith(".txt")) {
                    Intent textEditorActivity = new Intent(SearchActivity.this, TextEditor.class);
                    textEditorActivity.putExtra(MainActivity.FILE_TO_OPEN, fileClicked.getAbsolutePath());
                    startActivityForResult(textEditorActivity, MainActivity.FILE_MODIFIED);
                } else {
                    openOtherFiles(getFileByName(itemClicked.title));
                }
            }
        } catch (Exception e) {
            Log.v("Excetion", e.toString());
            Toast.makeText(SearchActivity.this, "Some error occured", Toast.LENGTH_SHORT).show();
        }


    }

    private void openOtherFiles(File url) {

        Uri uri = Uri.fromFile(url);

        Intent intent = new Intent(Intent.ACTION_VIEW);
        // Check what kind of file you are trying to open, by comparing the url with extensions.
        // When the if condition is matched, plugin sets the correct intent (mime) type,
        // so Android knew what application to use to open the file
        if (url.toString().contains(".doc") || url.toString().contains(".docx")) {
            // Word document
            intent.setDataAndType(uri, "application/msword");
        } else if (url.toString().contains(".pdf")) {
            // PDF file
            intent.setDataAndType(uri, "application/pdf");
        } else if (url.toString().contains(".ppt") || url.toString().contains(".pptx")) {
            // Powerpoint file
            intent.setDataAndType(uri, "application/vnd.ms-powerpoint");
        } else if (url.toString().contains(".xls") || url.toString().contains(".xlsx")) {
            // Excel file
            intent.setDataAndType(uri, "application/vnd.ms-excel");
        } else if (url.toString().contains(".zip") || url.toString().contains(".rar")) {
            // WAV audio file
            intent.setDataAndType(uri, "application/x-wav");
        } else if (url.toString().contains(".rtf")) {
            // RTF file
            intent.setDataAndType(uri, "application/rtf");
        } else if (url.toString().contains(".wav") || url.toString().contains(".mp3")) {
            // WAV audio file
            intent.setDataAndType(uri, "audio/x-wav");
        } else if (url.toString().contains(".gif")) {
            // GIF file
            intent.setDataAndType(uri, "image/gif");
        } else if (url.toString().contains(".jpg") || url.toString().contains(".jpeg") || url.toString().contains(".png")) {
            // JPG file
            intent.setDataAndType(uri, "image/jpeg");
        } else if (url.toString().contains(".txt")) {
            // Text file
            intent.setDataAndType(uri, "text/plain");
        } else if (url.toString().contains(".3gp") || url.toString().contains(".mpg") || url.toString().contains(".mpeg") || url.toString().contains(".mpe") || url.toString().contains(".mp4") || url.toString().contains(".avi")) {
            // Video files
            intent.setDataAndType(uri, "video/*");
        } else {
            //if you want you can also define the intent type for any other file

            //additionally use else clause below, to manage other unknown extensions
            //in this case, Android will show all applications installed on the device
            //so you can choose which application to use
            intent.setDataAndType(uri, "*/*");
        }

        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        startActivity(intent);
    }


    private void sendInfoToParent(String folderName) {
        Intent intent = new Intent();
        intent.putExtra(FOUND_FOLDER_NAME, folderName);
        setResult(MainActivity.SEARCH_FOLDER_CLICKED, intent);
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


    private ArrayList<String> getNameOfFilesFound() {
        ArrayList<String> files = new ArrayList<>();
        for (File file : filesFound) {
            files.add(file.getAbsolutePath());
        }
        Log.v("List with file names:", String.valueOf(files.size()));
        return files;
    }

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        ArrayList<String> names = getNameOfFilesFound();
        Log.v("Names:", String.valueOf(names.size()));
        outState.putStringArrayList(FILES_FOUND, names);
        super.onSaveInstanceState(outState);
    }
}
