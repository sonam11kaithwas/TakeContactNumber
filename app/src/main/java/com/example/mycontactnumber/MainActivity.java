package com.example.mycontactnumber;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import android.Manifest;
import android.app.Activity;
import android.content.ContentResolver;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.provider.ContactsContract;
import android.telephony.TelephonyManager;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

public class MainActivity extends AppCompatActivity implements View.OnClickListener {
    final int CONTACT_FROM_LIST = 100;
    final int CURRENT_MOBILE_NUMBER = 200;
    TextView txt;
    Button btn_self, btn;
    String wantPermission = Manifest.permission.READ_PHONE_STATE;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        initializeMyViews();
    }


    /**
     * check permission for get contact from contact list
     ***/
    private boolean check_ContactList_Permission() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            int result = ContextCompat.checkSelfPermission(this, Manifest.permission.READ_CONTACTS);
            return result == PackageManager.PERMISSION_GRANTED;
        } else {
            return true;
        }
    }


    /**
     * check current mobile number permission
     ***/
    private boolean check_Permission() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            int result = ContextCompat.checkSelfPermission(this, wantPermission);
            return result == PackageManager.PERMISSION_GRANTED;
        } else {
            return true;
        }
    }

    /***Ask permission for Current mobile number**/
    private void askPermissionCurrentMobileNumber() {
        if (ActivityCompat.shouldShowRequestPermissionRationale(this, wantPermission)) {
            /***   "Phone state permission allows us to get phone number. Please allow it for additional functionality."**/
        }
        ActivityCompat.requestPermissions(this, new String[]{wantPermission}, CURRENT_MOBILE_NUMBER);
    }

    /******  Ask permissiion for getContact from contact list  ******/
    private void askPermissionContactList() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M && checkSelfPermission(Manifest.permission.READ_CONTACTS) !=
                PackageManager.PERMISSION_GRANTED) {
            /***   "Phone state permission allows us to get phone number. Please allow it for additional functionality."**/
        }
        ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.READ_CONTACTS}, CONTACT_FROM_LIST);
    }


    @Override
    public void onRequestPermissionsResult(int requestCode, String[] permissions, int[] grantResults) {
        switch (requestCode) {
            case CURRENT_MOBILE_NUMBER:
                if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    getPhone();
                } else {
                    Toast.makeText(this, "Permission Denied. We can't get phone number.", Toast.LENGTH_LONG).show();
                }
                break;
            case CONTACT_FROM_LIST:
                if (grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    getContactFromContactList();
                } else {
                    Toast.makeText(this, "Permission Denied. We can't get phone number.", Toast.LENGTH_LONG).show();
                }
                break;
        }
    }

    /**
     * get current mobile number
     ****/
    private void getPhone() {
        TelephonyManager phoneMgr = (TelephonyManager) getSystemService(Context.TELEPHONY_SERVICE);
        if (ActivityCompat.checkSelfPermission(this, wantPermission) != PackageManager.PERMISSION_GRANTED) {
            return;
        }
        txt.setText("Current Mobile Number: " + phoneMgr.getLine1Number());
    }

    private void initializeMyViews() {
        txt = findViewById(R.id.txt);
        btn_self = findViewById(R.id.btn_self);
        btn_self.setOnClickListener(this);
        btn = findViewById(R.id.btn);
        btn.setOnClickListener(this);
    }


    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (requestCode == 101) {
            if (resultCode == Activity.RESULT_OK) {
                Uri uri = data.getData();
                ContentResolver contentResolver = getContentResolver();
                Cursor contentCursor = contentResolver.query(uri, null, null, null, null);

                if (contentCursor.moveToFirst()) {
                    String id = contentCursor.getString(contentCursor.getColumnIndexOrThrow(ContactsContract.Contacts._ID));

                    String hasPhone = contentCursor.getString(contentCursor.getColumnIndex(ContactsContract.Contacts.HAS_PHONE_NUMBER));

                    if (hasPhone.equalsIgnoreCase("1")) {
                        Cursor phones = getContentResolver().query(ContactsContract.CommonDataKinds.Phone.CONTENT_URI, null,
                                ContactsContract.CommonDataKinds.Phone.CONTACT_ID + " = " + id, null, null);
                        phones.moveToFirst();
                        String contactNumber = phones.getString(phones.getColumnIndex(ContactsContract.CommonDataKinds.Phone.NUMBER));
                        Log.i("phoneNUmber", "The phone number is " + contactNumber);
                        txt.setText("Contact name & number: \n" +
                                phones.getString(phones.getColumnIndex(ContactsContract.CommonDataKinds.Phone.DISPLAY_NAME))
                                + contactNumber);
                    }
                }
                super.onActivityResult(requestCode, resultCode, data);
            }
        }
    }

    /**
     * Intent for Contact List
     **/
    public void getContactFromContactList() {
        Intent contactPickerIntent = new Intent(Intent.ACTION_PICK, ContactsContract.Contacts.CONTENT_URI);
        startActivityForResult(contactPickerIntent, 101);
    }


    @Override
    public void onClick(View view) {
        switch (view.getId()) {
            case R.id.btn_self:
                /******current mobile number***/
                if (!check_Permission()) {
                    askPermissionCurrentMobileNumber();
                } else {
                    getPhone();
                }
                break;
            case R.id.btn:
                if (!check_ContactList_Permission()) {
                    askPermissionContactList();
                } else {
                    getContactFromContactList();
                }
                break;
        }
    }
}
