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
import android.support.v7.widget.LinearLayoutManager
import android.support.v7.widget.RecyclerView
import com.example.konrad.rocketfuel.Adapters.CalendarAdapter
import com.example.konrad.rocketfuel.Models.CalendarItem
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
import kotlinx.android.synthetic.main.activity_calendar.*
import pub.devrel.easypermissions.AfterPermissionGranted
import pub.devrel.easypermissions.EasyPermissions
import java.text.SimpleDateFormat
import java.util.*
import kotlin.collections.ArrayList


class CalendarActivity : AppCompatActivity(), EasyPermissions.PermissionCallbacks {
    enum class CalendarOperation {ReadEvents, CreateEvent, RemoveEvent}

    private val REQUEST_ACCOUNT_PICKER = 1000
    private val REQUEST_AUTHORIZATION = 1001
    private val REQUEST_GOOGLE_PLAY_SERVICES = 1002
    private val REQUEST_PERMISSION_GET_ACCOUNTS = 1003
    private val PREF_ACCOUNT_NAME = "accountName"

    private val calendarItems: ArrayList<CalendarItem> = ArrayList()

    private val newEvent: CalendarItem? by lazy {
        intent.getParcelableExtra("calendarItem") as? CalendarItem
    }

    private val mCredential: GoogleAccountCredential by lazy {
        GoogleAccountCredential.usingOAuth2(
                this, listOf(CalendarScopes.CALENDAR)
        )
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_calendar)

        mCredential.backOff = ExponentialBackOff()

        fabCalendar.setOnClickListener {
            startActivity(Intent(this,EventCalenderActivity::class.java))
            finish()
        }

        recycleCalender.run {
            adapter = CalendarAdapter(this@CalendarActivity, calendarItems)
            adapter.registerAdapterDataObserver(object: RecyclerView.AdapterDataObserver() {
                override fun onItemRangeChanged(positionStart: Int, itemCount: Int, payload: Any?) {
                    runGoogleCalendarEvent(CalendarOperation.RemoveEvent, payload as CalendarItem)
                }
            })
            setHasFixedSize(true)
            layoutManager = LinearLayoutManager(this@CalendarActivity)
        }

        runGoogleCalendarEvent(CalendarOperation.CreateEvent, newEvent ?: CalendarItem())
        runGoogleCalendarEvent(CalendarOperation.ReadEvents)
    }

    private fun runGoogleCalendarEvent(calendarOperation: CalendarOperation,
                                       calendarItem: CalendarItem = CalendarItem()) {
        if (!isGooglePlayServicesAvailable()) {
            acquireGooglePlayServices()
        }
        else if (mCredential.selectedAccountName == null) {
            chooseAccount()
        }
        else if (!isDeviceOnline()) {
        }

        MakeRequestTask(
                mCredential,
                calendarOperation,
                calendarItem
        ).execute()
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
        if (EasyPermissions.hasPermissions(this, Manifest.permission.GET_ACCOUNTS)) {
            val accountName = getPreferences(Context.MODE_PRIVATE)
                    .getString(PREF_ACCOUNT_NAME, null)
            if (accountName != null) {
                mCredential.selectedAccountName = accountName
            } else {
                // Start a dialog from which the user can choose an account
                startActivityForResult(
                        mCredential.newChooseAccountIntent(),
                        REQUEST_ACCOUNT_PICKER
                )
            }
        } else {
            // Request the GET_ACCOUNTS permission via a user dialog
            EasyPermissions.requestPermissions(
                    this,
                    "This app needs to access your Google account (via Contacts).",
                    REQUEST_PERMISSION_GET_ACCOUNTS,
                    Manifest.permission.GET_ACCOUNTS
            )
        }
    }

    private fun isDeviceOnline(): Boolean {
        val connMgr = getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager
        val networkInfo = connMgr.activeNetworkInfo
        return networkInfo != null && networkInfo.isConnected
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        when (requestCode) {
            REQUEST_GOOGLE_PLAY_SERVICES -> if (resultCode == Activity.RESULT_OK) {}
            REQUEST_ACCOUNT_PICKER -> if (resultCode == Activity.RESULT_OK && data != null &&
                    data.extras != null) {
                val accountName = data.getStringExtra(AccountManager.KEY_ACCOUNT_NAME)
                if (accountName != null) {
                    val settings = getPreferences(Context.MODE_PRIVATE)
                    val editor = settings.edit()
                    editor.putString(PREF_ACCOUNT_NAME, accountName)
                    editor.apply()
                    mCredential.selectedAccountName = accountName
                }
            }
            REQUEST_AUTHORIZATION -> if (resultCode == Activity.RESULT_OK) {}
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


    inner class MakeRequestTask(credential: GoogleAccountCredential,
                                private val calendarOperation: CalendarOperation,
                                private val calendarItem: CalendarItem = CalendarItem())
        : AsyncTask<Void, Void, Unit>() {

        private val mService: Calendar
        private var mLastError: Exception? = null
        private val extendedPropertyKey = "app"
        private val extendedPropertyValue = "RocketFuelApp"
        init {
            val transport: HttpTransport = AndroidHttp.newCompatibleTransport()
            val jsonFactory: JsonFactory = JacksonFactory.getDefaultInstance()
            mService = Calendar.Builder(transport, jsonFactory, credential)
                    .setApplicationName("Google Calendar API Android Quickstart")
                    .build()
        }

        override fun doInBackground(vararg params: Void?) {
            try {
                when (calendarOperation) {
                    CalendarOperation.ReadEvents -> getDataFromApi()
                    CalendarOperation.CreateEvent -> addEventToCalendar(calendarItem)
                    CalendarOperation.RemoveEvent -> removeEventFromCalendar(calendarItem.eventID)
                }
            } catch (e: Exception) {
                mLastError = e
                cancel(true)
            }
        }

        private fun getDataFromApi() {
            val now = DateTime(System.currentTimeMillis())
            val events = mService.events()?.list("primary")
                    ?.setPrivateExtendedProperty(listOf(
                            "$extendedPropertyKey=$extendedPropertyValue"
                    ))
                    ?.setMaxResults(10)
                    ?.setTimeMin(now)
                    ?.setOrderBy("startTime")
                    ?.setSingleEvents(true)
                    ?.execute()
            val items = events?.items ?: emptyList()

            val dayDateFormat = SimpleDateFormat("dd", Locale.ENGLISH)
            val monthDateFormat = SimpleDateFormat("MM", Locale.ENGLISH)
            val yearDateFormat = SimpleDateFormat("yyyy", Locale.ENGLISH)
            val fullDateFormat = SimpleDateFormat("yyyy-MM-dd", Locale.ENGLISH)

            calendarItems.clear()
            items.forEach {event ->
                val calendarItem = CalendarItem(
                        title = event.summary,
                        day = dayDateFormat.format(
                                fullDateFormat.parse(event.start.date.toStringRfc3339())
                        ),
                        month = monthDateFormat.format(
                                fullDateFormat.parse(event.start.date.toStringRfc3339())
                        ),
                        year = yearDateFormat.format(
                                fullDateFormat.parse(event.start.date.toStringRfc3339())
                        ),
                        desc = event.description,
                        eventID = event.id
                )
                calendarItems.add(calendarItem)
            }
        }

        private fun addEventToCalendar(calendarItem: CalendarItem) {
            val startDateStr = "${calendarItem.year}-${calendarItem.month}-${calendarItem.day}"
            val endDateStr = "${calendarItem.year}-${calendarItem.month}-${String.format("%02d", calendarItem.day.toInt() + 1)}"

            val startDateTime = DateTime(startDateStr)
            val endDateTime = DateTime(endDateStr)
            val extendedProperties = Event.ExtendedProperties()
            extendedProperties.private = mapOf(Pair(extendedPropertyKey, extendedPropertyValue))
            val event: Event = Event()
                    .setStart(EventDateTime().setDate(startDateTime))
                    .setEnd(EventDateTime().setDate(endDateTime))
                    .setSummary(calendarItem.title)
                    .setDescription(calendarItem.desc)
                    .setExtendedProperties(extendedProperties)
            mService.events()?.insert("primary", event)?.execute()
        }

        private fun removeEventFromCalendar(eventID: String) {
            mService.events().delete("primary", eventID).execute()
            calendarItems.remove(calendarItem)
        }

        override fun onPreExecute() {
        }

        override fun onPostExecute(result: Unit?) {
            recycleCalender.adapter.notifyDataSetChanged()
        }

        override fun onCancelled() {
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
            }
        }
    }
}
