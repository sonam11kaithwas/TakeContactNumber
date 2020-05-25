package com.example.mycontactnumber

import android.Manifest
import android.app.Activity
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import android.provider.ContactsContract
import android.telephony.TelephonyManager
import android.util.Log
import android.view.View
import android.widget.Button
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat


class ContactActivity : AppCompatActivity(), View.OnClickListener {
    private val CONTACT_FROM_LIST = 100
    private val CURRENT_MOBILE_NUMBER = 200
    private lateinit var txt: TextView
    private lateinit var btn_self: Button
    private lateinit var btn: Button
    private var wantPermission = Manifest.permission.READ_PHONE_STATE


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)


        /***find view's**/
        initializeMyViews()
    }

    private fun initializeMyViews() {
        txt = findViewById(R.id.txt)
        btn_self = findViewById(R.id.btn_self)
        btn_self.setOnClickListener(this)
        btn = findViewById(R.id.btn)
        btn.setOnClickListener(this)
    }

    override fun onClick(p0: View?) {
        when (p0?.id) {
            R.id.btn_self -> {
                /******current mobile number***/
                if (!check_Permission()) {
                    askPermissionCurrentMobileNumber()
                } else {
                    getPhone()
                }
            }
            R.id.btn -> {
                if (!check_ContactList_Permission()) {
                    askPermissionContactList()
                } else {
                    getContactFromContactList()
                }
            }


        }
    }

    /******  Ask permissiion for getContact from contact list  ******/
    private fun askPermissionContactList() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M && checkSelfPermission(Manifest.permission.READ_CONTACTS) !=
                PackageManager.PERMISSION_GRANTED) {
            /***   "Phone state permission allows us to get phone number. Please allow it for additional functionality."**/
        }
        ActivityCompat.requestPermissions(this, arrayOf(Manifest.permission.READ_CONTACTS), CONTACT_FROM_LIST)
    }

    /**
     * check permission for get contact from contact list
     ***/
    private fun check_ContactList_Permission(): Boolean {
        return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            var result: Int = ContextCompat.checkSelfPermission(this, Manifest.permission.READ_CONTACTS)
            result == PackageManager.PERMISSION_GRANTED
        } else {
            return true
        }
    }


    /**
     * check current mobile number permission
     ***/
    private fun check_Permission(): Boolean {
        return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            var result: Int = ContextCompat.checkSelfPermission(this, wantPermission)
            result == PackageManager.PERMISSION_GRANTED
        } else {
            return true
        }
    }

    /***Ask permission for Current mobile number**/
    private fun askPermissionCurrentMobileNumber() {
        if (ActivityCompat.shouldShowRequestPermissionRationale(this, wantPermission)) {
            /***   "Phone state permission allows us to get phone number. Please allow it for additional functionality."**/
        }
        ActivityCompat.requestPermissions(this, arrayOf(wantPermission), CURRENT_MOBILE_NUMBER)
    }

    private fun getPhone() {
        val tm = getSystemService(Context.TELEPHONY_SERVICE) as TelephonyManager
        if (ActivityCompat.checkSelfPermission(this, wantPermission) != PackageManager.PERMISSION_GRANTED) {
            return
        }
        txt.text = "Current Mobile Number: " + tm.line1Number
    }

    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<out String>, grantResults: IntArray) {
        when (requestCode) {
            CURRENT_MOBILE_NUMBER -> {
                if (grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    getPhone()
                } else {
                    Toast.makeText(this, "Permission Denied. We can't get phone number.", Toast.LENGTH_SHORT).show()
                }
            }
            CONTACT_FROM_LIST -> {
                if (grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    getContactFromContactList()
                } else {
                    Toast.makeText(this, "Permission Denied. We can't get phone number.", Toast.LENGTH_SHORT).show()
                }
            }
        }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        if (requestCode == 101) {
            if (resultCode == Activity.RESULT_OK) {
                val uri = data!!.data
                val contentResolver = contentResolver
                val contentCursor = contentResolver.query(uri!!, null, null, null, null)
                if (contentCursor!!.moveToFirst()) {
                    val id = contentCursor.getString(contentCursor.getColumnIndexOrThrow(ContactsContract.Contacts._ID))
                    val hasPhone = contentCursor.getString(contentCursor.getColumnIndex(ContactsContract.Contacts.HAS_PHONE_NUMBER))
                    if (hasPhone.equals("1", ignoreCase = true)) {
                        val phones = getContentResolver().query(ContactsContract.CommonDataKinds.Phone.CONTENT_URI, null,
                                ContactsContract.CommonDataKinds.Phone.CONTACT_ID + " = " + id, null, null)
                        phones!!.moveToFirst()
                        val contactNumber = phones.getString(phones.getColumnIndex(ContactsContract.CommonDataKinds.Phone.NUMBER))
                        Log.i("phoneNUmber", "The phone number is $contactNumber")
                        txt.text = """
                            Contact name & number:${phones.getString(phones.getColumnIndex(ContactsContract.CommonDataKinds.Phone.DISPLAY_NAME))}$contactNumber""".trimIndent()
                    }
                }
                super.onActivityResult(requestCode, resultCode, data)
            }
        }
    }

    /**
     * Intent for Contact List
     **/
    private fun getContactFromContactList() {
        intent = Intent(Intent.ACTION_PICK, ContactsContract.Contacts.CONTENT_URI)
        startActivityForResult(intent, 101)
    }
}

