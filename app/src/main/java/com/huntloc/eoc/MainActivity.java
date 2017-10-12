package com.huntloc.eoc;

import android.*;
import android.Manifest;
import android.content.DialogInterface;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.view.ContextThemeWrapper;
import android.support.v7.widget.Toolbar;
import android.telephony.SmsManager;
import android.text.format.DateFormat;
import android.util.Log;
import android.view.View;
import android.view.Menu;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.EditText;
import android.widget.ListAdapter;
import android.widget.ListView;
import android.widget.SimpleAdapter;
import android.widget.Spinner;
import android.widget.TextView;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;

public class MainActivity extends AppCompatActivity {
    Spinner spinner_group;
    CheckBox switch_drill;
    EditText editText_message;
    ListView listView_contacts = null;
    Group[] groups;
    HashMap<String, String> messages;
    final int REQUEST_SEND_SMS = 10;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        if(ContextCompat.checkSelfPermission(this, Manifest.permission.SEND_SMS)!= PackageManager.PERMISSION_GRANTED){
            ActivityCompat.requestPermissions(this,
                    new String[]{android.Manifest.permission.SEND_SMS},
                    REQUEST_SEND_SMS);
        }
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        FloatingActionButton fab = (FloatingActionButton) findViewById(R.id.fab);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                int selectedGroup = spinner_group.getSelectedItemPosition();
                if (selectedGroup == 0) {
                    setSpinnerError(spinner_group, "Required");
                    editText_message.setText("");
                    listView_contacts.setAdapter(null);
                    return;
                }
                AlertDialog.Builder alertDialogBuilder = new AlertDialog.Builder(MainActivity.this);
                alertDialogBuilder.setTitle("Messenger");
                alertDialogBuilder.setMessage("Confirm to send EOC Message.");
                alertDialogBuilder.setCancelable(false);
                alertDialogBuilder.setPositiveButton("Ok", new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int id) {
                        sendMessages();
                        dialog.cancel();
                    }
                });
                alertDialogBuilder.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int id) {
                        dialog.cancel();
                    }
                });
                alertDialogBuilder.create().show();
            }
        });
        FirebaseDatabase.getInstance().getReference().child("eocgroups").addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                saveDatabase(dataSnapshot);
                Log.d("onDataChange", "onDataChange");
            }
            @Override
            public void onCancelled(DatabaseError databaseError) {
            }
        });
        switch_drill = (CheckBox) findViewById(R.id.switch_drill);
        switch_drill.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                showGroup();
            }
        });
        spinner_group = (Spinner) findViewById(R.id.spinner_group);
        spinner_group.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parentView, View selectedItemView, int position, long id) {
                showGroup();
            }

            @Override
            public void onNothingSelected(AdapterView<?> parentView) {
                // your code here
            }
        });
        editText_message = (EditText) findViewById(R.id.editText_Message);
        listView_contacts = (ListView) findViewById(R.id.list_contacts);
        listGroups();
    }
    private void sendMessages(){
    int selectedGroup = spinner_group.getSelectedItemPosition();
    String group = groups[selectedGroup].getGroupName();
    String message = editText_message.getText().toString();
    SQLiteHelper db = new SQLiteHelper(this);
    List<Contact> contacts = db.getContacts(group);
    boolean success = true;
    try{
        for (int i = 0; i < contacts.size(); i++) {
            SmsManager sms = SmsManager.getDefault();
            sms.sendTextMessage(contacts.get(i).getContactNumber(), null, message, null, null);
        }
    }
    catch (Exception e){
        success = false;
        editText_message.setText(e.getMessage()+e.toString());
    }
    if(success){
        Snackbar.make(findViewById(android.R.id.content), "Messages sent!", Snackbar.LENGTH_LONG)
                .show();
    }
    else{
        Snackbar.make(findViewById(android.R.id.content), "Could't send messages!", Snackbar.LENGTH_LONG)
                .show();
    }
}

    private void showGroup() {
        setSpinnerError(spinner_group, null);
        int selectedGroup = spinner_group.getSelectedItemPosition();
        if (selectedGroup == 0) {
            setSpinnerError(spinner_group, "Required");
            editText_message.setText("");
            listView_contacts.setAdapter(null);
            return;
        }
        String group = groups[selectedGroup].getGroupName();
        String messageText = "";
        if (switch_drill.isChecked()) {
            messageText = "DRILL - ";
        }
        messageText = messageText + messages.get(group);
        editText_message.setText(messageText);
        listContacts(group);
    }

    private void saveDatabase(DataSnapshot dataSnapshot) {
        SQLiteHelper db = new SQLiteHelper(this);
        db.deleteGroups();
        db.deleteContacts();
        for (DataSnapshot groupDataSnapshot : dataSnapshot.getChildren()) {
            Group group = new Group(groupDataSnapshot.child("Name").getValue().toString(), groupDataSnapshot.child("Message").getValue().toString());
            db.addGroup(group);
            Log.d("Group", group.getGroupName());
            for (DataSnapshot contactsDataSnapshot : groupDataSnapshot.child("Contacts").getChildren()) {
                Contact contact = new Contact(group.getGroupName(), contactsDataSnapshot.child("Name").getValue().toString(), contactsDataSnapshot.child("Number").getValue().toString());
                db.addContact(contact);
            }
        }
        listGroups();
    }

    private void listGroups() {
        Group[] defaultOption = new Group[]{new Group("Select group", "")};
        SQLiteHelper db = new SQLiteHelper(this);
        List<Group> _groups = db.getGroups();
        groups = _groups.toArray(new Group[_groups.size()]);
        Group[] options = new Group[defaultOption.length + groups.length];
        System.arraycopy(defaultOption, 0, options, 0, defaultOption.length);
        System.arraycopy(groups, 0, options, defaultOption.length, groups.length);
        groups = options;
        messages = new HashMap<String, String>();
        for (Group group : _groups) {
            messages.put(group.getGroupName(), group.getMessage());
        }
        ArrayAdapter<Group> spinnerArrayAdapter = new ArrayAdapter<Group>(
                this, R.layout.spinner_text, options);
        spinner_group.setAdapter(spinnerArrayAdapter);
    }

    private void listContacts(String group) {
        ArrayList<HashMap<String, String>> list = new ArrayList<HashMap<String, String>>();
        SQLiteHelper db = new SQLiteHelper(this);
        List<Contact> contacts = db.getContacts(group);
        for (int i = 0; i < contacts.size(); i++) {
            HashMap<String, String> item = new HashMap<String, String>();
            item.put("contact_name", contacts.get(i).getContactName());
            item.put("contact_number", contacts.get(i).getContactNumber());
            list.add(item);
        }
        String[] columns = new String[]{"contact_name", "contact_number"};
        int[] renderTo = new int[]{R.id.contact_name, R.id.contact_number};

        ListAdapter listAdapter = new SimpleAdapter(this, list,
                R.layout.contact_list_row, columns, renderTo);

        listView_contacts.setAdapter(listAdapter);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        //getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }

    private void setSpinnerError(Spinner spinner, String error) {
        View selectedView = spinner.getSelectedView();
        if (selectedView != null && selectedView instanceof TextView) {
            TextView selectedTextView = (TextView) selectedView;
            selectedTextView.setError(error);
        }
    }
    /*@Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings) {
            return true;
        }

        return super.onOptionsItemSelected(item);
    }*/

}
