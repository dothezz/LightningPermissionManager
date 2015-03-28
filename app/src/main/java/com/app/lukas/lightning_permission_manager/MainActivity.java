package com.app.lukas.lightning_permission_manager;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.content.pm.PermissionGroupInfo;
import android.content.pm.PermissionInfo;
import android.os.Bundle;
import android.support.v7.app.ActionBarActivity;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.AutoCompleteTextView;
import android.widget.ListView;
import android.widget.Toast;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;


public class MainActivity extends ActionBarActivity {

    private ArrayAdapter<String> adapter;
    private ArrayList<String> list;
    private AutoCompleteTextView textView;
    private SharedPreferences preferences;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        preferences = getSharedPreferences(Strings.PREF_NAME,MODE_WORLD_READABLE);
        setContentView(R.layout.activity_main);
        textView = (AutoCompleteTextView) findViewById(R.id.autoCompleteTextView);
        textView.setDropDownAnchor(R.id.linearLayout);
        textView.setThreshold(0);
        ArrayList<String> permissions = new ArrayList<>();
        PackageManager pm = getPackageManager();

        List<PermissionGroupInfo> lstGroups = pm.getAllPermissionGroups(0);
        for (PermissionGroupInfo pgi : lstGroups) {
            List<PermissionInfo> lstPermissions = null;
            try {
                lstPermissions = pm.queryPermissionsByGroup(pgi.name, 0);
            } catch (PackageManager.NameNotFoundException e) {
                e.printStackTrace();
            }
            for (PermissionInfo pi : lstPermissions) {
                    permissions.add(pi.name);
                }
        }
        list = Strings.read(preferences);
        DropdownAdapter permAdapter = new DropdownAdapter(this, permissions,list);
        textView.setAdapter(permAdapter);
        adapter = new ArrayAdapter<>(this, android.R.layout.simple_list_item_1, list);
        updateAdapter();
        ListView listView = (ListView) findViewById(R.id.listView);
        listView.setAdapter(adapter);
        listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                delete(adapter.getItem(position));
            }
        });
    }


    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();

        if (id == R.id.action_save) {
            save();
            Intent applyIntent = new Intent(Strings.PKG + ".UPDATE_PERMISSIONS");
            applyIntent.putExtra("action", Strings.ACTION_PERMISSIONS);
            applyIntent.putExtra("Kill", true);
            sendBroadcast(applyIntent, null);
            Toast.makeText(this, "Saved!", Toast.LENGTH_SHORT).show();
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    private void delete(final String item){
        new AlertDialog.Builder(this)
                .setTitle("Remove \""+item+"\" ?")
                .setNegativeButton("No",null)
                .setPositiveButton("Yes",new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        list.remove(item);
                        updateAdapter();
                        save();
                        Toast.makeText(MainActivity.this, "Permissions are removed upon reboot", Toast.LENGTH_SHORT).show();
                    }
                })
                .show();
    }

    public void onAdd(View v){
        if(list == null) return;
        list.add(textView.getText().toString());
        updateAdapter();
        save();
        textView.setText("");
    }

    public void save(){
        Strings.write(list,preferences);

    }

    private void updateAdapter(){
        adapter.sort(new Comparator<String>() {
            @Override
            public int compare(String lhs, String rhs) {
                return lhs.compareTo(rhs);
            }
        });
        adapter.notifyDataSetChanged();
    }
}
