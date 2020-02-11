package com.example.mchristi.examplerecyclerview;

import android.content.Context;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Filter;
import android.widget.Filterable;
import android.widget.ImageView;
import android.widget.TextView;

import com.bumptech.glide.Glide;
import com.bumptech.glide.request.RequestOptions;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

public class ContactsAdapter extends RecyclerView.Adapter<ContactsAdapter.MyViewHolder>
        implements Filterable {
    private Context context;
    List<Contact> contactList;
    List<Contact> contactListFiltered;
    private ContactsAdapterListener listener;

    public class MyViewHolder extends RecyclerView.ViewHolder {
        public TextView name, phone;
        public ImageView thumbnail;
        public TextView likes;

        public MyViewHolder(View view) {
            super(view);
            name = view.findViewById(R.id.name);
            phone = view.findViewById(R.id.phone);
            thumbnail = view.findViewById(R.id.thumbnail);
            likes = view.findViewById(R.id.likes);


            view.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    // send selected contact in callback
                    listener.onContactSelected(contactList.get(getAdapterPosition()));
                }
            });

        }
    }


    public ContactsAdapter(Context context, List<Contact> contactList, ContactsAdapterListener listener) {
        this.context = context;
        this.listener = listener;
        this.contactList = contactList;
        this.contactListFiltered = contactList;
    }

    @Override
    public MyViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View itemView = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.contact_item, parent, false);

        return new MyViewHolder(itemView);
    }

    @Override
    public void onBindViewHolder(MyViewHolder viewHolder, final int position) {
        final Contact contact = contactListFiltered.get(position);

            if (contact.getName() != null) {
                viewHolder.name.setText(contact.getName());
                viewHolder.phone.setVisibility(TextView.VISIBLE);
                viewHolder.thumbnail.setVisibility(ImageView.GONE);
            }
            if (contact.getImage() != null) {
                String imageUrl = contact.getImage();
                Log.i("TAG","image="+imageUrl);
                Glide.with(viewHolder.thumbnail.getContext())
                        .load(contact.getImage())
                        .apply(RequestOptions.circleCropTransform())
                        .into(viewHolder.thumbnail);

                viewHolder.thumbnail.setVisibility(ImageView.VISIBLE);
                viewHolder.phone.setVisibility(TextView.GONE);
            }

            if (contact.getPhone() != null) {
                viewHolder.phone.setText(contact.getPhone());
            }
            viewHolder.likes.setText(""+contact.getLikes());
    }

    @Override
    public int getItemCount() {
        return contactListFiltered.size();
    }

    public void removeContactWithId(String uid) {

        contactListFiltered.removeIf(contact ->  (contact.getUid().equals(uid)) );
        contactList.removeIf(contact ->  (contact.getUid().equals(uid)) );
    }

    public void updateContact(Contact updatedContact) {

        Contact oldContact = contactListFiltered.stream()
                .filter(c -> (updatedContact.getUid().equals(c.getUid())))
                .findFirst()
                .orElse(null);
        if (oldContact != null) {
            contactListFiltered.set(contactListFiltered.indexOf(oldContact), updatedContact);
            Log.i("TAG","updated likes from DB for "+updatedContact.name+" = "+ updatedContact.getLikes());
        }

        oldContact = contactList.stream()
                .filter(c -> (updatedContact.getUid().equals(c.getUid())))
                .findFirst()
                .orElse(null);
        if (oldContact != null)
            contactList.set(contactList.indexOf(oldContact),updatedContact);

    }


    @Override
    public Filter getFilter() {
        return new Filter() {
            @Override
            protected FilterResults performFiltering(CharSequence charSequence) {
                String charString = charSequence.toString();
                if (charString.isEmpty()) {
                    contactListFiltered = contactList;
                } else {
                    List<Contact> filteredList = new ArrayList<>();
                    for (Contact row : contactList) {

                        // name match condition. this might differ depending on your requirement
                        // here we are looking for name or phone number match
                        if (row.getName().toLowerCase().contains(charString.toLowerCase()) || row.getPhone().contains(charSequence)) {
                            filteredList.add(row);
                        }
                    }

                    contactListFiltered = filteredList;
                }

                FilterResults filterResults = new FilterResults();
                filterResults.values = contactListFiltered;
                return filterResults;
            }

            @Override
            protected void publishResults(CharSequence charSequence, FilterResults filterResults) {
                contactListFiltered = (ArrayList<Contact>) filterResults.values;
                notifyDataSetChanged();
            }
        };
    }

    public interface ContactsAdapterListener {
        void onContactSelected(Contact contact);
    }
}