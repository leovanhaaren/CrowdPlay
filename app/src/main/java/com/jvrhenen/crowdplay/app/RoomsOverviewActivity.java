package com.jvrhenen.crowdplay.app;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.os.Bundle;
import android.support.v7.app.ActionBarActivity;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.Toast;

import com.jvrhenen.crowdplay.app.Adapters.RoomListAdapter;
import com.jvrhenen.crowdplay.app.data.RoomsRepository;
import com.jvrhenen.crowdplay.app.model.Room;

import java.util.ArrayList;
import java.util.Date;


public class RoomsOverviewActivity extends ActionBarActivity implements ListView.OnItemClickListener, ListView.OnItemLongClickListener {

    private ArrayList<Room> rooms;
    private RoomsRepository roomsRepository;
    private RoomListAdapter roomListAdapter;

    private ListView roomListView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_rooms_overview);

        roomsRepository = new RoomsRepository(this);

        roomListView = (ListView)findViewById(R.id.listView);
        roomListView.setOnItemClickListener(this);
        roomListView.setOnItemLongClickListener(this);

        loadData();
    }

    public void loadData() {
        rooms = roomsRepository.getAll();

        roomListAdapter = new RoomListAdapter(this, rooms);

        roomListView = (ListView)findViewById(R.id.listView);
        roomListView.setAdapter(roomListAdapter);
    }

    public void openRoom(Room room) {
        Toast.makeText(getApplicationContext(), "TODO: Open room", Toast.LENGTH_LONG).show();
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
                    Log.i("Create Room", ""+name.getText());
                    String roomName = name.getText().toString();
                    if(roomName.length() > 0) {
                        Room room = new Room();
                        room.setName(roomName);
                        room.setDate(new Date());

                        roomsRepository.save(room);
                        loadData();
                    } else {
                        Toast.makeText(getApplicationContext(), "Vul een Room naam in", Toast.LENGTH_LONG).show();
                    }
//                    Toast.makeText(getApplicationContext(), "TODO: Create Room", Toast.LENGTH_SHORT).show();
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

    @Override
    public void onItemClick(AdapterView<?> adapterView, View view, int position, long id) {
        Room room  = roomListAdapter.getItem(position);
        openRoom(room);
    }

    @Override
    public boolean onItemLongClick(AdapterView<?> adapterView, View view, int position, long id) {
        Room room = roomListAdapter.getItem(position);
        roomsRepository.delete(room);

        loadData();

        Toast.makeText(getApplicationContext(), "Removing Room", Toast.LENGTH_LONG).show();

        return true;
    }
}
