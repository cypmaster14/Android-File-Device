package com.example.ciprian.project_afd;

import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Environment;
import android.support.design.widget.BottomSheetDialog;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AlertDialog;
import android.text.InputType;
import android.util.Log;
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

/**
 * Created by Ciprian on 28/12/2016.
 */

public class FileOperations implements AdapterView.OnItemClickListener, AdapterView.OnItemLongClickListener {

    private ListView listView;
    private ItemAdapter adapter;

    private List<Item> items;
    private List<File> folders;
    private List<File> files;
    private FilesComparator filesComparator;

    private MySnackBar mySnackBar;
    private TextView txtHeader;
    private MenuItem pasteItemButton;


    private File currentFile;
    private File fileToBeCopied;
    private File root;

    private MainActivity mainActivityContext;
    private View coordinatorLayout;

    public FileOperations(Context context, View coordinatorLayout, MenuItem pasteItemButton, ListView listView) {

        this.mainActivityContext = (MainActivity) context;
        this.coordinatorLayout = coordinatorLayout;
        this.pasteItemButton = pasteItemButton;
        this.listView = listView;

        root = new File(Environment.getExternalStorageDirectory().getAbsolutePath());
        currentFile = new File(Environment.getExternalStorageDirectory().getAbsolutePath());
        items = new ArrayList<>();
        folders = new ArrayList<>();
        files = new ArrayList<>();
        getFiles(root);
        addFilesToListView();
        adapter = new ItemAdapter(mainActivityContext, R.layout.listview_item_row, items);
        listView.setAdapter(adapter);
        filesComparator = new FilesComparator();
        mySnackBar = new MySnackBar(coordinatorLayout);
        txtHeader = (TextView) mainActivityContext.findViewById(R.id.txtHeader);
        txtHeader.setText("Root");
    }

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
            Toast.makeText(mainActivityContext.getApplicationContext(), "File was renamed:" + newName, Toast.LENGTH_SHORT).show();

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

            Intent intent = new Intent(mainActivityContext, TextEditor.class);
            Log.v("File clicked", fileClicked.getName());
            intent.putExtra("Name", fileClicked.getAbsolutePath());
            mainActivityContext.startActivity(intent);
        }
    }

    @Override
    public boolean onItemLongClick(AdapterView<?> parent, View view, int position, long id) {

        if (position == 0 && items.get(position).title.compareTo("..") == 0) {
            return false;
        }

        final Item chosenFile = items.get(position);

        final BottomSheetDialog bottomSheetDialog = new BottomSheetDialog(mainActivityContext);
        View sheetView = mainActivityContext.getLayoutInflater().inflate(R.layout.menu_bottom_sheet, null);
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
                new AlertDialog.Builder(mainActivityContext)
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
                final EditText input = new EditText(mainActivityContext);
                input.setText(chosenFile.title);
                input.setInputType(InputType.TYPE_CLASS_TEXT);
                new AlertDialog.Builder(mainActivityContext)
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
}
