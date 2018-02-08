package de.kinemic.example.gesturereceiver;

import android.content.Context;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import java.util.ArrayList;

import de.kinemic.toolbox.GestureFocusActivity;

public class LayoutActivity extends GestureFocusActivity implements View.OnClickListener, AdapterView.OnItemClickListener {

    private static class MyAdapter extends ArrayAdapter<String> {

        public MyAdapter(@NonNull Context context, int resource, ArrayList<String> list) {
            super(context, resource, list);
        }

        @NonNull
        @Override
        public View getView(final int position, @Nullable View convertView, @NonNull ViewGroup parent) {
            View v =  super.getView(position, convertView, parent);
            if (v instanceof TextView) {
                ((TextView) v).setTextAppearance(R.style.ListItemFont);
            }
            /*v.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    String action = !view.isFocused() ? " tapped" : " triggered";
                    Snackbar.make(view, "" + item + action, Snackbar.LENGTH_LONG).show();
                    Toast.makeText(getContext(), "" + position + " clicked (in list)", Toast.LENGTH_SHORT).show();
                }
            });*/
            return v;
        };
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_layout);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        /*FloatingActionButton fab = (FloatingActionButton) findViewById(R.id.fab);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Snackbar.make(view, "Replace with your own action", Snackbar.LENGTH_LONG)
                        .setAction("Action", null).show();
            }
        });*/

        findViewById(R.id.button_1).setOnClickListener(this);
        findViewById(R.id.button_21).setOnClickListener(this);
        findViewById(R.id.button_22).setOnClickListener(this);
        findViewById(R.id.button_23).setOnClickListener(this);
        findViewById(R.id.button_31).setOnClickListener(this);
        findViewById(R.id.button_32).setOnClickListener(this);
        findViewById(R.id.button_33).setOnClickListener(this);
        findViewById(R.id.button_4).setOnClickListener(this);
        findViewById(R.id.button_5).setOnClickListener(this);

        ArrayList<String> items = new ArrayList<>();
        for (int i = 0; i < 5; ++i) {
            items.add("List 1 item " + (i+1));
        }
        final MyAdapter adapter1 = new MyAdapter(this, android.R.layout.simple_list_item_1, items);
        final ListView list1 = findViewById(R.id.list_1);
        list1.setAdapter(adapter1);

        ArrayList<String> items2 = new ArrayList<>();
        for (int i = 0; i < 10; ++i) {
            items2.add("List 2 item " + (i+1));
        }

        final MyAdapter adapter2 = new MyAdapter(this, android.R.layout.simple_list_item_1, items2);
        final ListView list2 = findViewById(R.id.list_2);
        list2.setAdapter(adapter2);

        list1.setOnItemClickListener(this);
        list2.setOnItemClickListener(this);
    }


    @Override
    public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {
        final String item = (String) adapterView.getItemAtPosition(i);
        String action = !view.isFocused() ? " tapped" : " triggered";
        Snackbar.make(view, "" + item + action, Snackbar.LENGTH_LONG).show();
        //Toast.makeText(LayoutActivity.this, "" + item + action, Toast.LENGTH_SHORT).show();
    }

    @Override
    protected View getRootGestureView() {
        return findViewById(R.id.gesture_root);
    }

    @Override
    public void onClick(View view) {
        if (view instanceof Button) {
            Button button = (Button) view;
            String action = !view.isFocused() ? " tapped" : " triggered";
            Snackbar.make(view, "" + button.getText() + action, Snackbar.LENGTH_LONG).show();
            //Toast.makeText(this, "" + button.getText() + action, Toast.LENGTH_SHORT).show();
        }
    }
}
