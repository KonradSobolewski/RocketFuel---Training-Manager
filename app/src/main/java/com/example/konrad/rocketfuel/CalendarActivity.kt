package com.example.konrad.rocketfuel

import android.Manifest
import android.accounts.AccountManager
import android.app.Activity
import android.content.Context
import android.content.Intent
import android.net.ConnectivityManager
import android.os.AsyncTask
import android.os.Bundle
import android.support.v7.app.AppCompatActivity
import android.view.View
import android.widget.ProgressBar
import com.google.android.gms.common.ConnectionResult
import com.google.android.gms.common.GoogleApiAvailability
import com.google.api.client.extensions.android.http.AndroidHttp
import com.google.api.client.googleapis.extensions.android.gms.auth.GoogleAccountCredential
import com.google.api.client.googleapis.extensions.android.gms.auth.GooglePlayServicesAvailabilityIOException
import com.google.api.client.googleapis.extensions.android.gms.auth.UserRecoverableAuthIOException
import com.google.api.client.http.HttpTransport
import com.google.api.client.json.JsonFactory
import com.google.api.client.json.jackson2.JacksonFactory
import com.google.api.client.util.DateTime
import com.google.api.client.util.ExponentialBackOff
import com.google.api.services.calendar.Calendar
import com.google.api.services.calendar.CalendarScopes
import com.google.api.services.calendar.model.Event
import com.google.api.services.calendar.model.EventDateTime
import pub.devrel.easypermissions.AfterPermissionGranted
import pub.devrel.easypermissions.EasyPermissions
import java.text.DateFormat
import java.text.SimpleDateFormat
import java.util.*


class CalendarActivity : AppCompatActivity(), EasyPermissions.PermissionCallbacks {
    private var mCredential: GoogleAccountCredential? = null
    private val REQUEST_ACCOUNT_PICKER = 1000
    private val REQUEST_AUTHORIZATION = 1001
    private val REQUEST_GOOGLE_PLAY_SERVICES = 1002
    private val REQUEST_PERMISSION_GET_ACCOUNTS = 1003
    private val PREF_ACCOUNT_NAME = "accountName"

    private var mProgress: ProgressBar? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_calendar)
        mCredential = GoogleAccountCredential.usingOAuth2(
                this, listOf(CalendarScopes.CALENDAR)
        ).setBackOff(ExponentialBackOff())
    }

    private fun getResultsFromApi() {
        if (!isGooglePlayServicesAvailable()) {
            acquireGooglePlayServices()
        } else if (mCredential?.selectedAccountName == null) {
            chooseAccount()
        } else if (!isDeviceOnline()) {}
        else {
            MakeRequestTask(mCredential!!).execute()
        }
    }

    private fun isGooglePlayServicesAvailable(): Boolean {
        val apiAvailability = GoogleApiAvailability.getInstance()
        val connectionStatusCode = apiAvailability.isGooglePlayServicesAvailable(this)
        return connectionStatusCode == ConnectionResult.SUCCESS
    }

    private fun acquireGooglePlayServices() {
        val apiAvailability = GoogleApiAvailability.getInstance()
        val connectionStatusCode = apiAvailability.isGooglePlayServicesAvailable(this)
        if (apiAvailability.isUserResolvableError(connectionStatusCode)) {
            showGooglePlayServicesAvailabilityErrorDialog(connectionStatusCode)
        }
    }

    private fun showGooglePlayServicesAvailabilityErrorDialog(
            connectionStatusCode: Int) {
        val apiAvailability = GoogleApiAvailability.getInstance()
        val dialog = apiAvailability.getErrorDialog(
                this@CalendarActivity,
                connectionStatusCode,
                REQUEST_GOOGLE_PLAY_SERVICES)
        dialog.show()
    }

    @AfterPermissionGranted(1003)
    private fun chooseAccount() {
        if (EasyPermissions.hasPermissions(
                        this, Manifest.permission.GET_ACCOUNTS)) {
            val accountName = getPreferences(Context.MODE_PRIVATE)
                    .getString(PREF_ACCOUNT_NAME, null)
            if (accountName != null) {
                mCredential?.selectedAccountName = accountName
                getResultsFromApi()
            } else {
                // Start a dialog from which the user can choose an account
                startActivityForResult(
                        mCredential?.newChooseAccountIntent(),
                        REQUEST_ACCOUNT_PICKER)
            }
        } else {
            // Request the GET_ACCOUNTS permission via a user dialog
            EasyPermissions.requestPermissions(
                    this,
                    "This app needs to access your Google account (via Contacts).",
                    REQUEST_PERMISSION_GET_ACCOUNTS,
                    Manifest.permission.GET_ACCOUNTS)
        }
    }

    private fun isDeviceOnline(): Boolean {
        val connMgr = getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager
        val networkInfo = connMgr.activeNetworkInfo
        return networkInfo != null && networkInfo.isConnected
    }

    override fun onActivityResult(
            requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        when (requestCode) {
            REQUEST_GOOGLE_PLAY_SERVICES -> if (resultCode != Activity.RESULT_OK) {
            } else {
                getResultsFromApi()
            }
            REQUEST_ACCOUNT_PICKER -> if (resultCode == Activity.RESULT_OK && data != null &&
                    data.extras != null) {
                val accountName = data.getStringExtra(AccountManager.KEY_ACCOUNT_NAME)
                if (accountName != null) {
                    val settings = getPreferences(Context.MODE_PRIVATE)
                    val editor = settings.edit()
                    editor.putString(PREF_ACCOUNT_NAME, accountName)
                    editor.apply()
                    mCredential?.selectedAccountName = accountName
                    getResultsFromApi()
                }
            }
            REQUEST_AUTHORIZATION -> if (resultCode == Activity.RESULT_OK) {
                getResultsFromApi()
            }
        }
    }

    override fun onRequestPermissionsResult(
            requestCode: Int,
            permissions: Array<out String>,
            grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        EasyPermissions.onRequestPermissionsResult(
                requestCode, permissions, grantResults, this
        )
    }

    override fun onPermissionsDenied(requestCode: Int, perms: MutableList<String>?) {}
    override fun onPermissionsGranted(requestCode: Int, perms: MutableList<String>?) {}


    inner class MakeRequestTask(
            credential: GoogleAccountCredential
    ) : AsyncTask<Void, Void, List<String>>() {

        private var mService: Calendar? = null
        private var mLastError: Exception? = null

        override fun doInBackground(vararg params: Void?): List<String> {
            return try {
                addEventToCalendar()
                getDataFromApi()
            } catch (e: Exception) {
                mLastError = e
                cancel(true)
                return listOf()
            }
        }

        private fun getDataFromApi(): MutableList<String> {
            val now = DateTime(System.currentTimeMillis())
            val eventStrings: MutableList<String> = mutableListOf()
            val events = mService?.events()?.list("primary")
                    ?.setMaxResults(10)
                    ?.setTimeMin(now)
                    ?.setOrderBy("startTime")
                    ?.setSingleEvents(true)
                    ?.execute()
            val items = events?.items ?: emptyList()

            for (event in items) {
                val start: DateTime = event.start.date
                eventStrings += String.format("%s (%s)", event.summary, start)
            }
            return eventStrings
        }

        private fun addEventToCalendar() {
            //TODO: pass event parameters through arguments
            val startDate = Date().time
            val endDate = Date(startDate + 86400000)

            val dateFormat: DateFormat = SimpleDateFormat("yyyy-MM-dd", Locale.ENGLISH)
            val startDateStr: String = dateFormat.format(startDate)
            val endDateStr: String = dateFormat.format(endDate)

            val startDateTime = DateTime(startDateStr)
            val endDateTime = DateTime(endDateStr)
            val event: Event = Event()
                    .setStart(EventDateTime().setDate(startDateTime))
                    .setEnd(EventDateTime().setDate(endDateTime))
                    .setSummary("klata kurła")
                    .setDescription("sobek ciągnie druta")
            mService?.events()?.insert("primary", event)?.execute()
        }


        override fun onPreExecute() {
            mProgress?.visibility = View.VISIBLE
        }

        override fun onPostExecute(result: List<String>?) {
            mProgress?.visibility = View.GONE
            if (result == null || result.isEmpty()) {
            } else {
            }
        }

        override fun onCancelled() {
            mProgress?.visibility = View.GONE
            if (mLastError != null) {
                when (mLastError) {
                    is GooglePlayServicesAvailabilityIOException ->
                        showGooglePlayServicesAvailabilityErrorDialog(
                            (mLastError as GooglePlayServicesAvailabilityIOException)
                                    .connectionStatusCode
                        )
                    is UserRecoverableAuthIOException -> startActivityForResult(
                            (mLastError as UserRecoverableAuthIOException).intent,
                            REQUEST_AUTHORIZATION
                    )
                }
            } else {
            }
        }


        init {
            val transport: HttpTransport = AndroidHttp.newCompatibleTransport()
            val jsonFactory: JsonFactory = JacksonFactory.getDefaultInstance()
            mService = Calendar.Builder(transport, jsonFactory, credential)
                    .setApplicationName("Google Calendar API Android Quickstart")
                    .build()
        }
    }

}
