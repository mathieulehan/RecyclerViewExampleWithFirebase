package com.example.mchristi.examplerecyclerview;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.SearchView;
import androidx.appcompat.widget.Toolbar;
import androidx.recyclerview.widget.ItemTouchHelper;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.app.SearchManager;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.firebase.ui.database.FirebaseRecyclerAdapter;
import com.firebase.ui.database.FirebaseRecyclerOptions;
import com.firebase.ui.database.SnapshotParser;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.firebase.database.ChildEventListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import java.util.ArrayList;
import java.util.List;

public class MainActivity extends AppCompatActivity implements ContactsAdapter.ContactsAdapterListener  {

    private RecyclerView recyclerView;
    private List<Contact> contactList;
    private ContactsAdapter mAdapter;
    private SearchView searchView;
    List<Contact> contacts;

    // STEP 1 : make a reference to the database...
    private FirebaseDatabase mFireDataBase;
    private DatabaseReference mContactsDatabaseReference;

    //STEP 4: child event lister.
    private ChildEventListener mChildEventListener;

    //STEP 7: Auth
    //private FirebaseAuth mFireBaseAuth;
    //private FirebaseAuth.AuthStateListener mAuthStatListener;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        Toolbar toolbar = findViewById(R.id.toolbar);
        //setSupportActionBar(toolbar);
        getSupportActionBar().setTitle("Firebase + Recycler");

        // STEP 2 : access the DB...
        mFireDataBase = FirebaseDatabase.getInstance();

        // STEP 2.1: and from the DB, get a reference on the child node "contacts"
        mContactsDatabaseReference = mFireDataBase.getReference().child("contacts");

        // STEP 2.2: get the recycler view
        recyclerView = findViewById(R.id.recycler_view);
        LinearLayoutManager linearLayoutManager = new LinearLayoutManager(this);
        recyclerView.setLayoutManager(linearLayoutManager);

        // STEP 2.3: create and set the adapter
        contacts = new ArrayList<>();
        mAdapter = new ContactsAdapter(getApplicationContext(), contacts, this);
        recyclerView.setAdapter(mAdapter);

        // STEP 3: enable adding a contact to Firebase
        activateAddingContact();

        // STEP 4: listen to any change on the DB
        enableUpdatesFromDB();

        // STEP 5: Enable removing a contact
        activateRemovingContact();

    }


    // STEP 3: enable adding a contact to Firebase
    public void activateAddingContact() {
        FloatingActionButton myFab = (FloatingActionButton) findViewById(R.id.myFAB);
        myFab.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {

                //STEP 3.1 : create a contact
                Contact contact = new Contact("Jacky", "https://s.yimg.com/ny/api/res/1.2/VLsXGJZMY_L.Rlgs4eHu5w--~A/YXBwaWQ9aGlnaGxhbmRlcjtzbT0xO3c9ODAw/http://media.zenfs.com/en-SG/homerun/ybrand.cinema.com.my/b4dc114cce3aaf62cc056b26bf1ce1ce", "+186 9");
                //STEP 3.2: and sync it using FireBase. push() creates a unique ID, and setValue serializes the object to sync it with the server
                mContactsDatabaseReference.push().setValue(contact);
                // no need to update the recyclerview, it will be updated by Firebase updates
            }
        });
    }

    // STEP 4: listen to any change on the DB
    public void enableUpdatesFromDB() {
        if (mChildEventListener == null) {
            mChildEventListener = new ChildEventListener() {
                @Override
                public void onChildAdded(DataSnapshot dataSnapshot, String s) {
                    Contact contact = dataSnapshot.getValue(Contact.class);
                    // don't forget to set the key to identify the Contact!
                    contact.setUid(dataSnapshot.getKey());
                    contacts.add(contact);
                    mAdapter.notifyDataSetChanged();
                }
                @Override
                public void onChildChanged(DataSnapshot dataSnapshot, String s) {
                    Contact contact = dataSnapshot.getValue(Contact.class);
                    contact.setUid(dataSnapshot.getKey());
                    mAdapter.updateContact(contact);
                    mAdapter.notifyDataSetChanged();
                }
                @Override
                public void onChildRemoved(DataSnapshot dataSnapshot) {
                    //Contact msg = dataSnapshot.getValue(Contact.class);
                    // don't forget to set the key to identify the Contact!
                    mAdapter.removeContactWithId(dataSnapshot.getKey());
                    mAdapter.notifyDataSetChanged();
                }
                @Override
                public void onChildMoved(DataSnapshot dataSnapshot, String s) {
                }
                @Override
                public void onCancelled(DatabaseError databaseError) {
                }
            };
            mContactsDatabaseReference.addChildEventListener(mChildEventListener);
        }
    }

    // STEP 5: Enable removing a contact
    public void activateRemovingContact() {
        new ItemTouchHelper(new ItemTouchHelper.SimpleCallback(0,
                ItemTouchHelper.LEFT | ItemTouchHelper.RIGHT) {
            @Override
            public boolean onMove(RecyclerView recyclerView, RecyclerView.ViewHolder viewHolder, RecyclerView.ViewHolder target) {
                return false;
            }

            @Override
            public void onSwiped(RecyclerView.ViewHolder viewHolder, int direction) {
                // STEP 5.1: get a reference to the selected entity
                Contact remove = mAdapter.contactListFiltered.get(viewHolder.getAdapterPosition());

                // STEP 5.2: use it's unique ID to remove it from Firebase
                mContactsDatabaseReference.child(remove.getUid()).removeValue();

                Toast.makeText(MainActivity.this, "Contact deleted", Toast.LENGTH_SHORT).show();
            }
        }).attachToRecyclerView(recyclerView);
    }


    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_main, menu);

        // Associate searchable configuration with the SearchView
        SearchManager searchManager = (SearchManager) getSystemService(Context.SEARCH_SERVICE);
        searchView = (SearchView) menu.findItem(R.id.action_search)
                .getActionView();
        searchView.setSearchableInfo(searchManager
                .getSearchableInfo(getComponentName()));
        searchView.setMaxWidth(Integer.MAX_VALUE);

        // listening to search query text change
        searchView.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
            @Override
            public boolean onQueryTextSubmit(String query) {
                // filter recycler view when query submitted
                mAdapter.getFilter().filter(query);
                return false;
            }

            @Override
            public boolean onQueryTextChange(String query) {
                // filter recycler view when text is changed
                mAdapter.getFilter().filter(query);
                return false;
            }
        });
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_search) {
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    @Override
    public void onBackPressed() {
        // close search view on back button pressed
        if (!searchView.isIconified()) {
            searchView.setIconified(true);
            return;
        }
        super.onBackPressed();
    }

    // STEP 6: Updating an Entity
    @Override
    public void onContactSelected(Contact contact) {

        // STEP 6.1: Updating the field in the class
        contact.setLikes(contact.getLikes()+1);

        // STEP 6.2: Updating the field on the Firebase DB
        mContactsDatabaseReference.child(contact.getUid()).child("likes").setValue(contact.getLikes());

    }

}
