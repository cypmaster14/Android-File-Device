package com.example.ciprian.project_afd;

import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.text.method.ScrollingMovementMethod;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.EditText;
import android.widget.Scroller;

import org.apache.commons.io.FileUtils;

import java.io.File;
import java.nio.charset.Charset;

public class TextEditor extends AppCompatActivity {

    private EditText textEditor;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_text_editor);
        Intent intent = getIntent();
        String fileName = intent.getStringExtra("Name");
        File file = new File(fileName);
        textEditor = (EditText) findViewById(R.id.textEditor);
        String fileContent = "";
        try {
            fileContent = FileUtils.readFileToString(file, Charset.defaultCharset());
        } catch (Exception e) {
            Log.v("Excetption", e.toString());
        }
        textEditor.setScroller(new Scroller(TextEditor.this));
        textEditor.setVerticalScrollBarEnabled(true);
        textEditor.setMovementMethod(new ScrollingMovementMethod());
        textEditor.setText(fileContent);

//        setTitle(file.getName());

    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.editor_menu, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {


        return true;
    }
}
