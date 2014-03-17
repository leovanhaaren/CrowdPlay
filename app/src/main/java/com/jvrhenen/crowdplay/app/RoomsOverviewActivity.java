package com.jvrhenen.crowdplay.app;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.os.Bundle;
import android.support.v7.app.ActionBarActivity;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.EditText;
import android.widget.Toast;


public class RoomsOverviewActivity extends ActionBarActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_rooms_overview);
    }


    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.rooms_overview, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();
        if (id == R.id.rooms_overview_action_add) {
            AlertDialog.Builder builder = new AlertDialog.Builder(this);
            builder.setTitle(R.string.rooms_overview_dialog_title);

            final EditText name = new EditText(this);
            name.setHint(R.string.rooms_overview_dialog_field);
            builder.setView(name);

            builder.setPositiveButton(R.string.rooms_overview_dialog_ok, new DialogInterface.OnClickListener() {
                public void onClick(DialogInterface dialog, int id) {
                    Toast.makeText(getApplicationContext(), "TODO: Create Room", Toast.LENGTH_SHORT).show();
                }
            });
            builder.setNegativeButton(R.string.rooms_overview_dialog_cancel, new DialogInterface.OnClickListener() {
                public void onClick(DialogInterface dialog, int id) {
                    dialog.cancel();
                }
            });

            AlertDialog dialog = builder.create();
            dialog.show();
            return true;
        }
        if (id == R.id.rooms_overview_action_refresh) {
            Toast.makeText(getApplicationContext(), "Searching for rooms", Toast.LENGTH_SHORT).show();

            return true;
        }
        return super.onOptionsItemSelected(item);
    }

}
