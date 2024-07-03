// ContactsFragment.java
package com.example.userinterface;

import android.content.Context;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;

import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QuerySnapshot;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

public class ContactsFragment extends Fragment {

    private EditText contactName, contactNumber;
    private Button btnAddContact;
    private ListView contactsListView;
    private ArrayList<Contact> contactsList;
    private ContactsAdapter contactsAdapter;
    private FirebaseFirestore db;
    private FirebaseUser currentUser;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_contacts, container, false);

        contactName = view.findViewById(R.id.editTextContactName);
        contactNumber = view.findViewById(R.id.editTextContactNumber);
        btnAddContact = view.findViewById(R.id.btnAddContact);
        contactsListView = view.findViewById(R.id.contactsListView);

        contactsList = new ArrayList<>();
        contactsAdapter = new ContactsAdapter(requireContext(), contactsList);
        contactsListView.setAdapter(contactsAdapter);

        db = FirebaseFirestore.getInstance();
        currentUser = FirebaseAuth.getInstance().getCurrentUser();

        loadContacts();

        btnAddContact.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                String name = contactName.getText().toString();
                String number = contactNumber.getText().toString();

                if (name.isEmpty() || number.isEmpty()) {
                    Toast.makeText(getContext(), "Please fill in all fields", Toast.LENGTH_SHORT).show();
                } else {
                    saveContact(name, number);
                }
            }
        });

        return view;
    }

    private void saveContact(String name, String number) {
        Map<String, Object> contactData = new HashMap<>();
        contactData.put("name", name);
        contactData.put("number", number);

        db.collection("user_info").document(currentUser.getUid()).collection("user_contacts").document()
                .set(contactData)
                .addOnSuccessListener(new OnSuccessListener<Void>() {
                    @Override
                    public void onSuccess(Void aVoid) {
                        Toast.makeText(getContext(), "Contact saved successfully", Toast.LENGTH_SHORT).show();
                        loadContacts(); // Reload contacts after saving
                    }
                })
                .addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        Toast.makeText(getContext(), "Failed to save contact: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                    }
                });

        // Clear input fields after saving
        contactName.setText("");
        contactNumber.setText("");
    }

    private void loadContacts() {
        db.collection("user_info").document(currentUser.getUid()).collection("user_contacts")
                .get()
                .addOnSuccessListener(new OnSuccessListener<QuerySnapshot>() {
                    @Override
                    public void onSuccess(QuerySnapshot queryDocumentSnapshots) {
                        contactsList.clear();
                        for (DocumentSnapshot documentSnapshot : queryDocumentSnapshots) {
                            String id = documentSnapshot.getId();
                            String name = documentSnapshot.getString("name");
                            String number = documentSnapshot.getString("number");
                            if (name != null && number != null) {
                                contactsList.add(new Contact(id, name, number));
                            }
                        }
                        contactsAdapter.notifyDataSetChanged();
                    }
                })
                .addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        Toast.makeText(getContext(), "Failed to load contacts: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                    }
                });
    }

    // Adapter for the contacts list
    private class ContactsAdapter extends ArrayAdapter<Contact> {
        public ContactsAdapter(Context context, ArrayList<Contact> contacts) {
            super(context, 0, contacts);
        }

        @Override
        public View getView(final int position, View convertView, ViewGroup parent) {
            if (convertView == null) {
                convertView = LayoutInflater.from(getContext()).inflate(R.layout.contact_item, parent, false);
            }

            TextView contactNameTextView = convertView.findViewById(R.id.contactNameTextView);
            TextView contactNumberTextView = convertView.findViewById(R.id.contactNumberTextView);
            Button deleteButton = convertView.findViewById(R.id.btnDeleteContact);

            final Contact contact = getItem(position);

            contactNameTextView.setText(contact.getName());
            contactNumberTextView.setText(contact.getNumber());

            deleteButton.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    deleteContact(contact.getId());
                }
            });

            return convertView;
        }
    }

    // Method to delete a contact
    private void deleteContact(String contactId) {
        db.collection("user_info").document(currentUser.getUid()).collection("user_contacts").document(contactId)
                .delete()
                .addOnSuccessListener(new OnSuccessListener<Void>() {
                    @Override
                    public void onSuccess(Void aVoid) {
                        Toast.makeText(getContext(), "Contact deleted successfully", Toast.LENGTH_SHORT).show();
                        loadContacts(); // Reload contacts after deletion
                    }
                })
                .addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        Toast.makeText(getContext(), "Failed to delete contact: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                    }
                });
    }

    // Model class for a contact
    private class Contact {
        private String id;
        private String name;
        private String number;

        public Contact(String id, String name, String number) {
            this.id = id;
            this.name = name;
            this.number = number;
        }

        public String getId() {
            return id;
        }

        public String getName() {
            return name;
        }

        public String getNumber() {
            return number;
        }
    }
}
