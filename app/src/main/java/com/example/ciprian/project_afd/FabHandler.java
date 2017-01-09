package com.example.ciprian.project_afd;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.text.InputType;
import android.util.Log;
import android.view.View;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.EditText;
import android.widget.Toast;

import org.apache.commons.io.FileUtils;

import java.io.File;
import java.io.IOException;

/**
 * Created by Ciprian on 24/12/2016.
 */

public class FabHandler {

    private FloatingActionButton fab_plus, fab_file, fab_folder;
    private Animation fabOpen, fabClose, fabClockWise, fabAntiClockWise;
    private MainActivity context;
    private boolean isOpenFab = false;

    public FabHandler(Context context) {
        this.context = (MainActivity) context;
        this.fab_plus = (FloatingActionButton) this.context.findViewById(R.id.fab_plus);
        this.fab_file = (FloatingActionButton) this.context.findViewById(R.id.fab_file);
        this.fab_folder = (FloatingActionButton) this.context.findViewById(R.id.fab_folder);
        initializeFab();
    }

    public void initializeFab() {
        fabOpen = AnimationUtils.loadAnimation(context, R.anim.fab_open);
        fabClose = AnimationUtils.loadAnimation(context, R.anim.fab_close);
        fabClockWise = AnimationUtils.loadAnimation(context, R.anim.rotate_clockwise);
        fabAntiClockWise = AnimationUtils.loadAnimation(context, R.anim.rotate_anticlockwise);
        fab_plus.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (isOpenFab) {
                    startAnimationCloseFAB();
                } else {
                    startAnimationOpenFab();
                }
            }
        });

        fab_file.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                newFileButton();
            }
        });

        fab_folder.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                newFolderButton();
            }
        });
    }

    private void newFileButton() {
        final EditText inputText = new EditText(context);
        inputText.setInputType(InputType.TYPE_CLASS_TEXT);
        new AlertDialog.Builder(context)
                .setTitle("New File")
                .setMessage("Enter the name of the new file:")
                .setView(inputText)
                .setPositiveButton("Create", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        Intent intent = new Intent(context, TextEditor.class);
                        String newFilePath = context.currentFile.getAbsolutePath() + "/" + inputText.getText().toString();
                        intent.putExtra("Name", newFilePath);
                        File newFile = new File(newFilePath);
                        try {
                            newFile.createNewFile();
                            context.startActivityForResult(intent, 200);

                        } catch (IOException e) {
                            Log.v("Exception", e.toString());
                            context.mySnackBar.showSnackBar("Failed to createa a new file", Snackbar.LENGTH_LONG);
                        }
                    }
                })
                .setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        dialog.cancel();
                    }
                })
                .show();
        startAnimationCloseFAB();
    }

    private void newFolderButton() {
        final EditText inputText = new EditText(context);
        inputText.setInputType(InputType.TYPE_CLASS_TEXT);
        new AlertDialog.Builder(context)
                .setTitle("Create new file")
                .setMessage("Enter the name of the new folder")
                .setView(inputText)
                .setPositiveButton("Create", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        String newDirectoryPath = context.currentFile.getAbsolutePath() + "/" + inputText.getText().toString();
                        try {
                            FileUtils.forceMkdir(new File(newDirectoryPath));
                            Toast.makeText(context, "Directory was created", Toast.LENGTH_SHORT).show();
                        } catch (Exception e) {
                            Log.v("Exception", "Failed to create file");
                        }
                        context.refreshListView();
                    }
                })
                .setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        dialog.cancel();
                    }
                })
                .show();

        startAnimationCloseFAB();

    }

    private void startAnimationOpenFab() {
        fab_plus.startAnimation(fabClockWise);
        fab_file.startAnimation(fabOpen);
        fab_file.setClickable(true);
        fab_folder.startAnimation(fabOpen);
        fab_folder.setClickable(true);
        isOpenFab = true;
    }

    private void startAnimationCloseFAB() {
        fab_plus.startAnimation(fabAntiClockWise);
        fab_file.startAnimation(fabClose);
        fab_file.setClickable(false);
        fab_folder.startAnimation(fabClose);
        fab_folder.setClickable(false);
        isOpenFab = false;
    }
}
