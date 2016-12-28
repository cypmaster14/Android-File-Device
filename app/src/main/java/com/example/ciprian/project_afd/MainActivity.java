package com.example.ciprian.project_afd;

import android.Manifest;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.os.Environment;
import android.support.design.widget.BottomSheetDialog;
import android.support.design.widget.Snackbar;
import android.support.v4.app.ActivityCompat;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.text.InputType;
import android.util.Log;
import android.view.ContextMenu;
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
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class MainActivity extends AppCompatActivity {

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

    private MySnackBar mySnackBar;


    private FileOperations fileOperations;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);


        ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE, Manifest.permission.READ_EXTERNAL_STORAGE}, 0);
        coordinatorLayout = findViewById(R.id.layout);
        root = new File(Environment.getExternalStorageDirectory().getAbsolutePath());
        currentFile = new File(Environment.getExternalStorageDirectory().getAbsolutePath());
        getFiles(root);
        addFilesToListView();
        listView = (ListView) findViewById(R.id.listView);
        adapter = new ItemAdapter(this, R.layout.listview_item_row, items);
        listView.setAdapter(adapter);
        listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                Item itemClicked = items.get(position);
                Log.v("File clicked", itemClicked.title);
                if (itemClicked.title.compareTo("..") == 0) {
                    //Previous button was pressed
                    Log.v("Parent", currentFile.getParent());
                    currentFile = new File(currentFile.getParent());
                    txtHeader.setText(currentFile.getName());
                    Log.v("Current file:", currentFile.getAbsolutePath());
                    refreshListView(currentFile);
                    return;
                }
                File fileClicked = getFileByName(itemClicked.title);
                if (fileClicked.isDirectory()) {
                    currentFile = fileClicked;
                    txtHeader.setText(currentFile.getName());
                    refreshListView(fileClicked);
                    listView.smoothScrollToPositionFromTop(0, 0, 0);
                } else {
                    //Start the activity regarding the text editor

                    Intent intent = new Intent(MainActivity.this, TextEditor.class);
                    Log.v("File clicked", fileClicked.getName());
                    intent.putExtra("Name", fileClicked.getAbsolutePath());
                    startActivity(intent);
                }

            }
        });


        buildActionSheet();
//        registerForContextMenu(listView);

        txtHeader = (TextView) findViewById(R.id.txtHeader);
        txtHeader.setText("Root");
        Log.v("Root Name", root.getName());
        Log.v("Root absolute name", root.getAbsolutePath());
        instantiateFabHandler();
//        fileOperations = new FileOperations(MainActivity.this, coordinatorLayout, pasteItemButton, listView);


        mySnackBar = new MySnackBar(coordinatorLayout);
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
                        refreshListView(currentFile);
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
                        refreshListView(currentFile);
                        bottomSheetDialog.hide();

                    }
                });

                return true;
            }
        });
    }


    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_buttons, menu);
        pasteItemButton = menu.getItem(0);
        pasteItemButton.setVisible(false);
        return true;
    }

    public boolean onOptionsItemSelected(MenuItem item) {


        switch (item.getTitle().toString()) {
            case "Paste":
                Log.v("Paste Button", "Paste button was pressed");
                pasteItemButton.setVisible(false);
                pasteFile();
                refreshListView(currentFile);
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
                refreshListView(currentFile);
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

            refreshListView(currentFile);

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
        refreshListView(currentFile);
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

    private void getFiles(File file) {
        if (file.isDirectory()) {
            for (File aux : file.listFiles()) {
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
            items.add(new Item(R.drawable.folder1, folder.getName(), "1234KB", "22"));
        }

        for (File file : files) {
            items.add(new Item(R.drawable.file1, file.getName(), "1234KB", "33"));
        }
    }

    public void refreshListView(File file) {
        items.clear();
        folders.clear();
        files.clear();
        listView.invalidateViews();
        getFiles(file);
        addFilesToListView();
        if (file.getAbsolutePath().compareTo(root.getAbsolutePath()) != 0) {
            //I have to add previous button
            items.add(0, new Item(R.drawable.ic_action_back, "..", "", ""));
        }
        listView.invalidateViews();
    }

    private void instantiateFabHandler() {
        new FabHandler(MainActivity.this);

    }

}