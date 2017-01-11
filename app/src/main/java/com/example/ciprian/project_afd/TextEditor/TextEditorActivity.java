package com.example.ciprian.project_afd.TextEditor;

import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Typeface;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.KeyEvent;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.Scroller;
import android.widget.Toast;

import com.example.ciprian.project_afd.MainActivity;
import com.example.ciprian.project_afd.R;

import org.apache.commons.io.FileUtils;

import java.io.File;
import java.io.IOException;
import java.nio.charset.Charset;
import java.util.Arrays;
import java.util.LinkedList;
import java.util.List;
import java.util.Stack;

import io.github.mthli.knife.KnifeText;

public class TextEditorActivity extends AppCompatActivity implements TextWatcher {

    public static final String FILE_CURRENT_CONTENT_BUNDLE = "FileCurrentContent";
    public static final String SAVE_BUTTON_SHOW_BUNDLE = "SaveButtonShow";
    public static final int RESULT_SETTINGS = 700;
    public static final String FONT_PREF_KEY = "fontPref";
    public static final String FONT_SIZE_KEY = "fontSize";
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

    private SharedPreferences sharedPreferences;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_text_editor);

        stackOfContentForUndo = new Stack<>();
        this.setResult(MainActivity.FILE_MODIFIED);
        populateFonts();

        sharedPreferences = PreferenceManager.getDefaultSharedPreferences(this);
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
        textEditor.setScroller(new Scroller(TextEditorActivity.this));
        textEditor.setVerticalScrollBarEnabled(true);
        textEditor.setText(fileCurrentContent);
        textEditor.addTextChangedListener(this);
        textEditor.setTypeface(getFontByName(sharedPreferences.getString(FONT_PREF_KEY, fonts[0])));
        textEditor.setTextSize(Float.valueOf(sharedPreferences.getString(FONT_SIZE_KEY, "12.0f")));
    }

    private Typeface getFontByName(String font) {
        for (int i = 0; i < fonts.length; i++) {
            if (fonts[i].equals(font)) {
                return fontTypes.get(i);
            }
        }
        return fontTypes.get(0);
    }


    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        if (keyCode == KeyEvent.KEYCODE_BACK) {
            if (fileInitialContent.compareTo(fileCurrentContent) != 0) {
                promptSaveDialog();
                return true;
            }
        }
        return super.onKeyDown(keyCode, event);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.editor_menu, menu);
        saveMenuItem = menu.findItem(R.id.save);
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
                Toast.makeText(TextEditorActivity.this, "Content was saved", Toast.LENGTH_SHORT).show();
                break;
            case R.id.menu_settings:
                Intent intent = new Intent(TextEditorActivity.this, TextEditorPreferencesActivity.class);
                startActivityForResult(intent, RESULT_SETTINGS);

            default:

        }
        return true;
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        switch (requestCode) {
            case RESULT_SETTINGS:
                textEditor.setTypeface(getFontByName(sharedPreferences.getString(FONT_PREF_KEY, fonts[0])));
                textEditor.setTextSize(Float.valueOf(sharedPreferences.getString(FONT_SIZE_KEY, "12.0f")));
                break;
            default:
        }
    }

    private void promptSaveDialog() {
        new AlertDialog.Builder(TextEditorActivity.this)
                .setTitle("Close file")
                .setMessage("Do you want to save the content of the file?")
                .setPositiveButton("Yes", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        saveFile();

                        TextEditorActivity.super.finish();
                    }
                })
                .setNegativeButton("No", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        dialog.cancel();
                        TextEditorActivity.super.finish();
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
