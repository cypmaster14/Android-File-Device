package com.example.ciprian.project_afd;

import android.Manifest;
import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.support.design.widget.BottomSheetDialog;
import android.support.design.widget.Snackbar;
import android.support.v4.app.ActivityCompat;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.SearchView;
import android.text.InputType;
import android.util.Log;
import android.view.ContextMenu;
import android.view.KeyEvent;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import org.apache.commons.io.FileUtils;

import java.io.File;
import java.text.DecimalFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.List;


public class MainActivity extends AppCompatActivity implements AdapterView.OnItemClickListener {

    public static final String ROOT_BUNDLE = "Root";
    public static final String CURRENT_FILE_BUNDLE = "CurrentFile";
    public static final String FILE_TO_BE_COPIED_BUNDLE = "FileToBeCopied";
    public static final String FILES_FOUND = "FileFound";
    public static final int FILE_MODIFIED = 200;
    public static final int SEARCH_FOLDER_CLICKED = 300;
    public static final String FILE_TO_OPEN = "Name";


    private ListView listView;
    private ItemAdapter adapter;
    private File root;
    private List<Item> items = new ArrayList<>();
    private List<File> folders = new ArrayList<>();
    private List<File> files = new ArrayList<>();
    private FilesComparator filesComparator = new FilesComparator();
    public View coordinatorLayout;
    private TextView txtHeader;
    public File currentFile;
    private File fileToBeCopied = null;

    private MenuItem pasteItemButton = null;
    private MenuItem searchButton = null;


    public MySnackBar mySnackBar;

    private SearchView searchView;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        initUI(savedInstanceState);
    }

    private void initUI(Bundle savedInstanceState) {

        ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE, Manifest.permission.READ_EXTERNAL_STORAGE}, 0);
        coordinatorLayout = findViewById(R.id.layout);
        loadPreviousSession(savedInstanceState);
        listView = (ListView) findViewById(R.id.listView);
        adapter = new ItemAdapter(this, R.layout.listview_item_row, items);
        listView.setAdapter(adapter);
        listView.setOnItemClickListener(this);

        buildActionSheet();

        txtHeader = (TextView) findViewById(R.id.txtHeader);
        setHeaderName();
        Log.v("Root Name", root.getName());
        Log.v("Root absolute name", root.getAbsolutePath());
        instantiateFabHandler();

        if (!currentFile.getAbsolutePath().equals(root.getAbsolutePath())) {
            addPreviousButton();
        }

        mySnackBar = new MySnackBar(coordinatorLayout);
    }

    /**
     * Function that sets the header name(current file name)
     */
    private void setHeaderName() {
        if (root.getAbsolutePath().equals(currentFile.getAbsolutePath())) {
            txtHeader.setText("Root");
        } else {
            txtHeader.setText(currentFile.getName());
        }
    }

    /**
     * Function that restore the previous session if exists and
     * get the content of the current location
     *
     * @param savedInstanceState
     */
    private void loadPreviousSession(Bundle savedInstanceState) {
        if (savedInstanceState != null) {
            String rootFileName = savedInstanceState.getString(ROOT_BUNDLE);
            if (rootFileName != null) {
                root = new File(rootFileName);
                Log.v("RootFile", rootFileName);
            }
            String currentFileName = savedInstanceState.getString(CURRENT_FILE_BUNDLE);
            if (currentFileName != null) {
                currentFile = new File(currentFileName);
                Log.v("CurrentFile", currentFileName);
            }
            String fileToCopy = savedInstanceState.getString(FILE_TO_BE_COPIED_BUNDLE);
            if (fileToCopy != null) {
                fileToBeCopied = new File(fileToCopy);
                Log.v("FileToBeCopied", fileToCopy);
            }
            getFiles();
        } else {
            Log.v("Bundle", "Is null");
            root = new File(Environment.getExternalStorageDirectory().getAbsolutePath());
            currentFile = new File(Environment.getExternalStorageDirectory().getAbsolutePath());
            getFiles();
        }
        addFilesToListView();
    }


    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (requestCode == FILE_MODIFIED) {
            if (resultCode == FILE_MODIFIED) {
                refreshListView();
            }
        } else if (requestCode == SEARCH_FOLDER_CLICKED) {
            if (resultCode == SEARCH_FOLDER_CLICKED) {
                String folderFound = data.getStringExtra(SearchActivity.FOUND_FOLDER_NAME);
                currentFile = new File(folderFound);
                refreshListView();
            }
        }
    }


    private void buildActionSheet() {
        listView.setOnItemLongClickListener(new AdapterView.OnItemLongClickListener() {
            @Override
            public boolean onItemLongClick(AdapterView<?> parent, View view, int position, long id) {

                if (position == 0 && items.get(position).title.compareTo("..") == 0) {
                    return false;
                }

                final Item chosenFile = items.get(position);

                final BottomSheetDialog bottomSheetDialog = new BottomSheetDialog(MainActivity.this);
                View sheetView = getLayoutInflater().inflate(R.layout.menu_bottom_sheet, null);
                bottomSheetDialog.setContentView(sheetView);
                bottomSheetDialog.show();

                LinearLayout copy = (LinearLayout) sheetView.findViewById(R.id.fragment_history_bottom_sheet_copy);
                copy.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        Log.v("Test click", "Button Copy was clicked");
                        fileToBeCopied = getFileByName(chosenFile.title);
                        String text = "File \"" + fileToBeCopied.getName() + "\" have been copied to clipboard!";
                        mySnackBar.showSnackBar(text, Snackbar.LENGTH_LONG);
                        pasteItemButton.setVisible(true);
                        bottomSheetDialog.hide();
                    }
                });

                LinearLayout remove = (LinearLayout) sheetView.findViewById(R.id.fragment_history_bottom_sheet_remove);
                remove.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        Log.v("Test click", "Button Remove was clicked");
                        new AlertDialog.Builder(MainActivity.this)
                                .setTitle("Remove File")
                                .setMessage("Do you want to remove file:" + chosenFile.title)
                                .setPositiveButton("Yes", new DialogInterface.OnClickListener() {
                                    @Override
                                    public void onClick(DialogInterface dialog, int which) {
                                        removeFile(chosenFile);
                                    }
                                })
                                .setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
                                    @Override
                                    public void onClick(DialogInterface dialog, int which) {
                                        dialog.cancel();
                                    }
                                })
                                .show();
                        refreshListView();
                        listView.invalidateViews();
                        bottomSheetDialog.hide();
                    }
                });

                LinearLayout rename = (LinearLayout) sheetView.findViewById(R.id.fragment_history_bottom_sheet_rename);
                rename.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        Log.v("Test click", "Button Rename was clicked");
                        final EditText input = new EditText(MainActivity.this);
                        input.setText(chosenFile.title);
                        input.setInputType(InputType.TYPE_CLASS_TEXT);
                        new AlertDialog.Builder(MainActivity.this)
                                .setTitle("Set new name")
                                .setView(input)
                                .setPositiveButton("Rename", new DialogInterface.OnClickListener() {
                                    @Override
                                    public void onClick(DialogInterface dialog, int which) {
                                        String newFileName = input.getText().toString();
                                        renameFile(chosenFile.title, newFileName);
                                    }
                                })
                                .setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
                                    @Override
                                    public void onClick(DialogInterface dialog, int which) {
                                        dialog.cancel();
                                    }
                                })
                                .show();
                        refreshListView();
                        bottomSheetDialog.hide();

                    }
                });

                return true;
            }
        });
    }


    @Override
    public boolean onCreateOptionsMenu(final Menu menu) {
        getMenuInflater().inflate(R.menu.menu_buttons, menu);
        pasteItemButton = menu.findItem(R.id.pasteButton);
        if (fileToBeCopied != null) {
            pasteItemButton.setVisible(true);
        }
        pasteItemButton.setVisible(false);
        searchButton = menu.findItem(R.id.menuSearch);
        final ActionBar actionBar = getSupportActionBar();
        searchView = (SearchView) searchButton.getActionView();
        searchView.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
            @Override
            public boolean onQueryTextSubmit(String query) {
                Log.v("File Entered", query);


                FileSearch fileSearch = new FileSearch(root, query.trim());
                new FileSearchAsync(MainActivity.this, searchView, searchButton).execute(fileSearch);

                return false;
            }

            @Override
            public boolean onQueryTextChange(String newText) {
                return false;
            }
        });


        return true;
    }

    public boolean onOptionsItemSelected(MenuItem item) {

        switch (item.getTitle().toString()) {
            case "Paste":
                Log.v("Paste Button", "Paste button was pressed");
                pasteItemButton.setVisible(false);
                pasteFile();
                refreshListView();
                break;

            default:
        }

        return true;
    }

    @Override
    public void onCreateContextMenu(ContextMenu menu, View v, ContextMenu.ContextMenuInfo menuInfo) {
        super.onCreateContextMenu(menu, v, menuInfo);
        AdapterView.AdapterContextMenuInfo info = (AdapterView.AdapterContextMenuInfo) menuInfo;
        int position = info.position;
        if (position == 0 && items.get(position).title.compareTo("..") == 0) {
            return;
        }
        menu.setHeaderTitle("Choose an option");
        menu.add(0, v.getId(), 0, "Remove");
        menu.add(1, v.getId(), 1, "Copy");
        if (fileToBeCopied != null) {
            menu.add(1, v.getId(), 1, "Paste");
        }
        menu.add(1, v.getId(), 1, "Rename");

    }

    @Override
    public boolean onContextItemSelected(MenuItem item) {

        AdapterView.AdapterContextMenuInfo info = (AdapterView.AdapterContextMenuInfo) item.getMenuInfo();
        final Item chosenFile = items.get(info.position);
        if (chosenFile.title.compareTo("..") == 0) {
            return true;
        }

        switch (item.getTitle().toString()) {
            case "Remove":
                new AlertDialog.Builder(this)
                        .setTitle("Remove File")
                        .setMessage("Do you want to remove file:" + chosenFile.title)
                        .setPositiveButton("Yes", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                removeFile(chosenFile);
                            }
                        })
                        .setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                dialog.cancel();
                            }
                        })
                        .show();
                break;
            case "Copy":
                fileToBeCopied = getFileByName(chosenFile.title);
                String text = "File \"" + fileToBeCopied.getName() + "\" have been copied to clipboard!";
                mySnackBar.showSnackBar(text, Snackbar.LENGTH_LONG);
                break;
            case "Paste":
                pasteFile();
                refreshListView();
                break;
            case "Rename":
                final EditText input = new EditText(this);
                input.setText(chosenFile.title);
                input.setInputType(InputType.TYPE_CLASS_TEXT);
                new AlertDialog.Builder(this)
                        .setTitle("Set new name")
                        .setView(input)
                        .setPositiveButton("Rename", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                String newFileName = input.getText().toString();
                                renameFile(chosenFile.title, newFileName);
                            }
                        })
                        .setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                dialog.cancel();
                            }
                        })
                        .show();

            default:
        }
        return true;
    }


    //These methods should be move to a Class FileOperations

    private void renameFile(String name, String newName) {
        try {
            File file = getFileByName(name);
            Log.v("Old", file.getAbsolutePath());
            Log.v("New", file.getParentFile().getAbsolutePath() + "/" + newName);
            if (file.isFile()) {
                FileUtils.moveFile(
                        FileUtils.getFile(file.getAbsolutePath()),
                        FileUtils.getFile(file.getParentFile().getAbsolutePath() + "/" + newName));
            } else {
                FileUtils.moveDirectory(
                        FileUtils.getFile(file.getAbsolutePath()),
                        FileUtils.getFile(file.getParentFile().getAbsolutePath() + "/" + newName));
            }
            Log.v("Rename", "True");
            Toast.makeText(getApplicationContext(), "File was renamed:" + newName, Toast.LENGTH_SHORT).show();

            refreshListView();

        } catch (Exception exception) {
            Log.v("Excetion", exception.toString());
        }

    }

    private void removeFile(Item fileToBeRemove) {

        if (fileToBeCopied != null && getFileByName(fileToBeRemove.title).getAbsolutePath().compareTo(fileToBeCopied.getAbsolutePath()) == 0) {
            fileToBeCopied = null;
        }

        FileUtils.deleteQuietly(getFileByName(fileToBeRemove.title));
        items.remove(fileToBeRemove);
        listView.invalidateViews();
        adapter.notifyDataSetChanged();
        refreshListView();
        mySnackBar.showSnackBar("File was removed", Snackbar.LENGTH_LONG);
    }

    private void pasteFile() {
        try {
            Log.v("CurrentFile:", currentFile.getAbsolutePath());
            Log.v("File to be copied", fileToBeCopied.getAbsolutePath());
            String newFilePath = currentFile.getAbsolutePath() + "/" + fileToBeCopied.getName();
            if (fileToBeCopied.isFile()) {
                File newFile = new File(newFilePath);
                Log.v("Type", "File");
                newFile.createNewFile();
                FileUtils.copyFile(fileToBeCopied, newFile);
            } else {
                Log.v("Type", "Directory");
                File newFolder = new File(newFilePath);
                newFolder.mkdir();
                FileUtils.copyDirectory(fileToBeCopied, newFolder);
            }
        } catch (Exception e) {
            Log.v("Exception", e.toString());
            mySnackBar.showSnackBar("Some errors occured!", Snackbar.LENGTH_LONG);
            return;
        }
        fileToBeCopied = null;
        mySnackBar.showSnackBar("File was copied", Snackbar.LENGTH_LONG);
    }

    /////////////////////////////////////////////////

    private File getFileByName(String name) {
        for (File folder : folders) {
            if (folder.getName().compareTo(name) == 0) {
                return folder;
            }
        }

        for (File file : files) {
            if (file.getName().compareTo(name) == 0) {
                return file;
            }
        }
        return null;
    }

    private void getFiles() {
        Log.v("GetFiles", currentFile.getAbsolutePath());
        if (currentFile.isDirectory()) {
            for (File aux : currentFile.listFiles()) {
                if (aux.isDirectory()) {
                    folders.add(aux);
                } else {
                    files.add(aux);
                }
            }
        }
        Collections.sort(folders, filesComparator);
        Collections.sort(files, filesComparator);
    }

    private void addFilesToListView() {

        for (File folder : folders) {
            Date lastModified = new Date(folder.lastModified());
            SimpleDateFormat formatter = new SimpleDateFormat("dd/MM/yyyy HH:mm");
            items.add(new Item(R.drawable.folder1, folder.getName(), formatter.format(lastModified), String.valueOf(folder.list().length) + " Files"));
        }
        for (File file : files) {
            Date lastModified = new Date(file.lastModified());
            SimpleDateFormat formatter = new SimpleDateFormat("dd/MM/yyyy HH:mm");
            DecimalFormat twoDForm = new DecimalFormat("#.##");
            String value = String.valueOf(twoDForm.format(file.length() / 1024.0));
            items.add(new Item(R.drawable.file1, file.getName(), formatter.format(lastModified), value + " KB"));
        }
    }

    public void refreshListView() {
        items.clear();
        folders.clear();
        files.clear();
        listView.invalidateViews();
        adapter.notifyDataSetChanged();
        getFiles();
        addFilesToListView();
        if (!currentFile.getAbsolutePath().equals(root.getAbsolutePath())) {
            addPreviousButton();
        }
        listView.invalidateViews();
        setHeaderName();


    }

    private void addPreviousButton() {
        items.add(0, new Item(R.drawable.back, "..", "", ""));
    }

    private void instantiateFabHandler() {
        new FabHandler(MainActivity.this);

    }

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        outState.putString(ROOT_BUNDLE, root.getAbsolutePath());
        outState.putString(CURRENT_FILE_BUNDLE, currentFile.getAbsolutePath());
        if (fileToBeCopied != null) {
            outState.putString(FILE_TO_BE_COPIED_BUNDLE, fileToBeCopied.getAbsolutePath());
        }
        Log.v("SaveRoot", root.getAbsolutePath());
        Log.v("SaveCurrentFile", currentFile.getAbsolutePath());
        super.onSaveInstanceState(outState);


    }

    @Override
    public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
        Item itemClicked = items.get(position);
        Log.v("File clicked", itemClicked.title);
        if (itemClicked.title.compareTo("..") == 0) {
            //Previous button was pressed
            goToParentDirectory();
            return;
        }
        File fileClicked = getFileByName(itemClicked.title);
        if (fileClicked.isDirectory()) {
            currentFile = fileClicked;
            txtHeader.setText(currentFile.getName());
            refreshListView();
            listView.smoothScrollToPositionFromTop(0, 0, 0);
        } else {
            //Start the activity regarding the text editor
            if (itemClicked.title.endsWith(".txt")) {
                Intent intent = new Intent(MainActivity.this, TextEditor.class);
                Log.v("File clicked", fileClicked.getName());
                intent.putExtra(FILE_TO_OPEN, fileClicked.getAbsolutePath());
                startActivityForResult(intent, MainActivity.FILE_MODIFIED);
            } else {
                openOtherFiles(getFileByName(itemClicked.title));
            }
        }
    }

    private void goToParentDirectory() {
        Log.v("Parent", currentFile.getParent());
        currentFile = new File(currentFile.getParent());
        Log.v("Current file:", currentFile.getAbsolutePath());
        refreshListView();
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

    public boolean onKeyDown(int keyCode, KeyEvent event) {
        if (keyCode == KeyEvent.KEYCODE_BACK) {
            if (!root.getAbsolutePath().equals(currentFile.getAbsolutePath())) {
                goToParentDirectory();
                return true;
            }
        }
        return super.onKeyDown(keyCode, event);
    }

}
