package com.adform.sample.app;

import android.app.Activity;
import android.content.Context;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.TextView;
import com.adform.sample.app.adapters.TestAdapter;

import java.util.ArrayList;

public class MainActivity3 extends Activity {


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main3);

        final ListView listView = (ListView) findViewById(R.id.list_view);
        final ArrayList<String> templateList = new ArrayList<String>();
        for (int i = 0; i < 200; ++i) {
            templateList.add("Template value #" + i);
        }

        final TestAdapter adapter = new TestAdapter(this, templateList);
        listView.setAdapter(adapter);
    }


}