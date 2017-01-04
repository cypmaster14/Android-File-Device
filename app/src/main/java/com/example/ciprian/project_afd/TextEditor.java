package com.example.ciprian.project_afd;

import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Typeface;
import android.os.Bundle;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.KeyEvent;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Scroller;
import android.widget.Toast;

import org.apache.commons.io.FileUtils;

import java.io.File;
import java.io.IOException;
import java.nio.charset.Charset;
import java.util.Arrays;
import java.util.LinkedHashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Stack;

import io.github.mthli.knife.KnifeText;

public class TextEditor extends AppCompatActivity implements TextWatcher {

    public static final String FILE_CURRENT_CONTENT_BUNDLE = "FileCurrentContent";
    public static final String SAVE_BUTTON_SHOW_BUNDLE = "SaveButtonShow";
    private KnifeText textEditor;
    private String fileInitialContent = "";
    private String fileCurrentContent;
    private MenuItem saveMenuItem;
    private Stack<String> stackOfContentForUndo;
    private Stack<String> getStackOfContentForRedo;
    private File file;
    private String[] fonts;
    private List<Typeface> fontTypes;
    private boolean saveButtonShow = false;

    int selectedItem = 0;

    @Override

    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_text_editor);

        stackOfContentForUndo = new Stack<>();
        this.setResult(200);
        populateFonts();

        setupTextEditor(savedInstanceState);
    }


    private void populateFonts() {
        fonts = new String[]{"Serif", "Monospace", "Default", "Default Bold"};
        fontTypes = new LinkedList<>(Arrays.asList(Typeface.SERIF, Typeface.MONOSPACE, Typeface.DEFAULT, Typeface.DEFAULT_BOLD));
    }

    private void setupTextEditor(Bundle savedInstanceState) {
        Intent intent = getIntent();
        String fileName = intent.getStringExtra("Name");
        file = new File(fileName);
        textEditor = (KnifeText) findViewById(R.id.textEditor);
        try {
            fileInitialContent = FileUtils.readFileToString(file, Charset.defaultCharset());
        } catch (Exception e) {
            Log.v("Excetption", e.toString());
        }

        if (savedInstanceState == null) {
            fileCurrentContent = fileInitialContent;
            Log.v("Bundle", "Is null");
        } else {
            Log.v("Bundle", "Is not null");
            fileCurrentContent = savedInstanceState.getString(FILE_CURRENT_CONTENT_BUNDLE);
            saveButtonShow = savedInstanceState.getBoolean(SAVE_BUTTON_SHOW_BUNDLE);
        }

        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setHomeButtonEnabled(true);
        getSupportActionBar().setTitle(file.getName());
        textEditor.setScroller(new Scroller(TextEditor.this));
        textEditor.setVerticalScrollBarEnabled(true);
        textEditor.setText(fileCurrentContent);
        textEditor.addTextChangedListener(this);
    }


    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        if (keyCode == KeyEvent.KEYCODE_BACK) {
            if (fileInitialContent.compareTo(fileCurrentContent) != 0) {
                promptSaveDialog();
            }
            return true;
        }
        return super.onKeyDown(keyCode, event);
    }

    @Override
    public boolean onPrepareOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.editor_menu, menu);
        saveMenuItem = menu.getItem(0);
        Log.v("Show button", String.valueOf(!fileInitialContent.equals(fileCurrentContent)));
        saveMenuItem.setVisible(!fileInitialContent.equals(fileCurrentContent));
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {

        switch (item.getItemId()) {
            case android.R.id.home:
                if (fileInitialContent.compareTo(fileCurrentContent) != 0) {
                    promptSaveDialog();
                    return true;
                }
                this.finish();
                return true;
            case R.id.save:
                Log.v("Save", "Save button pressed");
                saveFile();
                Toast.makeText(TextEditor.this, "Content was saved", Toast.LENGTH_SHORT).show();
                break;
            case R.id.font:
                AlertDialog.Builder builder =
                        new AlertDialog.Builder(this);
                builder.setTitle("Select One Letter");
                int selected = selectedItem;
                builder.setSingleChoiceItems(
                        fonts,
                        selected,
                        new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                selectedItem = which;
                                Toast.makeText(TextEditor.this, "You Select Letter " + fonts[selectedItem], Toast.LENGTH_SHORT).show();
                                dialog.dismiss();
                                textEditor.setTypeface(fontTypes.get(selectedItem));
                            }
                        });
                AlertDialog alert = builder.create();
                alert.show();

            default:

        }
        return true;
    }


    private void promptSaveDialog() {
        new AlertDialog.Builder(TextEditor.this)
                .setTitle("Close file")
                .setMessage("Do you want to save the content of the file?")
                .setPositiveButton("Yes", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        saveFile();

                        TextEditor.super.finish();
                    }
                })
                .setNegativeButton("No", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        dialog.cancel();
                        TextEditor.super.finish();
                    }
                })
                .show();
    }

    private void saveFile() {
        saveMenuItem.setVisible(false);
        try {
            FileUtils.write(file, fileCurrentContent, Charset.defaultCharset(), false);
        } catch (IOException e) {
            Log.v("Exception", "Failed to save the file");
        }
        fileInitialContent = fileCurrentContent;
    }

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        outState.putString(FILE_CURRENT_CONTENT_BUNDLE, fileCurrentContent);
        outState.putBoolean(SAVE_BUTTON_SHOW_BUNDLE, saveMenuItem != null);
        super.onSaveInstanceState(outState);
    }

    @Override
    public void beforeTextChanged(CharSequence s, int start, int count, int after) {
        Log.v("Before", "Text Changed");
    }

    @Override
    public void onTextChanged(CharSequence s, int start, int before, int count) {
        Log.v("On Text", "Text Changed");
    }

    @Override
    public void afterTextChanged(Editable s) {
        Log.v("After", "Text Changed");
        fileCurrentContent = textEditor.getText().toString();
        if (saveMenuItem != null) {
            if (!saveMenuItem.isVisible()) {
                saveMenuItem.setVisible(true);
            } else if (fileInitialContent.compareTo(textEditor.getText().toString()) == 0) {
                saveMenuItem.setVisible(false);
            }
        }
    }
}
