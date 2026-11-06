package com.example.assignment2

import android.content.Intent
import android.os.Bundle
import android.view.View
import android.widget.Button
import android.widget.ImageView
import android.widget.RatingBar
import android.widget.TextView
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import com.google.android.material.snackbar.Snackbar
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import android.widget.ImageButton
import android.widget.LinearLayout
import android.widget.AdapterView
import android.widget.Spinner
import androidx.appcompat.widget.SearchView
import android.content.res.Configuration
import androidx.appcompat.app.AppCompatDelegate
import androidx.appcompat.widget.SwitchCompat

class MainActivity : AppCompatActivity() {

    private lateinit var root: View
    private lateinit var balanceText: TextView
    private lateinit var carCard: View
    private lateinit var carImage: ImageView
    private lateinit var carTitle: TextView
    private lateinit var carYear: TextView
    private lateinit var carKm: TextView
    private lateinit var carCost: TextView
    private lateinit var carRating: RatingBar
    private lateinit var emptyText: TextView
    private lateinit var rentButton: Button
    private lateinit var nextButton: Button
    private lateinit var bookingsText: TextView
    private lateinit var cancelBookingButton: Button
    private lateinit var favouriteButton: ImageButton
    private lateinit var noFavouritesText: TextView
    private lateinit var favouritesContainer: LinearLayout
    private lateinit var searchView: SearchView
    private lateinit var sortSpinner: Spinner
    private lateinit var darkSwitch: SwitchCompat

    private var query = ""                    // used by search (step 5)
    private var sort = SortOption.NONE        // used by sort menu (step 5)
    private var index = 0                     // which car is on screen

    // Receives the result from BookingActivity
    private val bookingLauncher =
        registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
            if (result.resultCode == RESULT_OK && result.data != null) {
                val data = result.data!!
                val car = CarRepository.getById(data.getIntExtra(BookingActivity.EXTRA_CAR_ID, -1))
                val name = data.getStringExtra(BookingActivity.EXTRA_NAME) ?: ""
                val days = data.getIntExtra(BookingActivity.EXTRA_DAYS, 1)

                if (car != null) {
                    if (BookingManager.book(car, name, days)) {
                        Snackbar.make(root,
                            getString(R.string.booked_msg, car.name, car.model, days * car.dailyCost),
                            Snackbar.LENGTH_LONG).show()
                    } else {
                        Snackbar.make(root, R.string.error_insufficient, Snackbar.LENGTH_LONG).show()
                    }
                }
            } else {
                Snackbar.make(root, R.string.not_booked_msg, Snackbar.LENGTH_SHORT).show()
            }
        }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        if (savedInstanceState != null) {
            index = savedInstanceState.getInt("index", 0)
            query = savedInstanceState.getString("query", "")
            sort = SortOption.values()[savedInstanceState.getInt("sort", 0)]
        }

        root = findViewById(android.R.id.content)
        balanceText = findViewById(R.id.balanceText)
        carCard = findViewById(R.id.carCard)
        carImage = findViewById(R.id.carImage)
        carTitle = findViewById(R.id.carTitle)
        carYear = findViewById(R.id.carYear)
        carKm = findViewById(R.id.carKm)
        carCost = findViewById(R.id.carCost)
        carRating = findViewById(R.id.carRating)
        emptyText = findViewById(R.id.emptyText)
        rentButton = findViewById(R.id.rentButton)
        nextButton = findViewById(R.id.nextButton)
        bookingsText = findViewById(R.id.bookingTexts)
        cancelBookingButton = findViewById(R.id.cancelBookingButton)
        favouriteButton = findViewById(R.id.favouriteButton)
        noFavouritesText = findViewById(R.id.noFavouritesText)
        favouritesContainer = findViewById(R.id.favouritesContainer)
        searchView = findViewById(R.id.searchView)
        sortSpinner = findViewById(R.id.sortSpinner)

        nextButton.setOnClickListener {
            index++
            showCar()
        }

        val pad = (16 * resources.displayMetrics.density).toInt()
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.rootLayout)) { v, insets ->
            val bars = insets.getInsets(
                WindowInsetsCompat.Type.systemBars() or WindowInsetsCompat.Type.displayCutout()
            )
            v.setPadding(pad + bars.left, pad + bars.top, pad + bars.right, pad + bars.bottom)
            insets
        }

        rentButton.setOnClickListener {
            val list = CarRepository.getAvailable(query, sort)
            if (list.isNotEmpty()) {
                val car = list[index]
                val intent = Intent(this, BookingActivity::class.java)
                intent.putExtra(BookingActivity.EXTRA_CAR, car)   // Parcelable
                bookingLauncher.launch(intent)
            }
        }
        favouriteButton.setOnClickListener { toggleCurrentFavourite() }
        carImage.setOnLongClickListener {
            toggleCurrentFavourite()
            true
        }
        // Search
        searchView.setIconifiedByDefault(false)
        searchView.queryHint = getString(R.string.search_hint)
        searchView.clearFocus()
        searchView.setOnQueryTextListener(object : SearchView.OnQueryTextListener {
            override fun onQueryTextSubmit(text: String?): Boolean {
                searchView.clearFocus()
                return true
            }

            override fun onQueryTextChange(newText: String?): Boolean {
                val text = newText.orEmpty()
                if (text != query) {
                    query = text
                    index = 0
                }
                showCar()
                return true
            }
        })

        // Sort
        sortSpinner.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
            override fun onItemSelected(parent: AdapterView<*>?, view: View?, position: Int, id: Long) {
                val newSort = when (position) {
                    1 -> SortOption.RATING_HIGH_LOW
                    2 -> SortOption.YEAR_NEWEST
                    3 -> SortOption.COST_LOW_HIGH
                    else -> SortOption.NONE
                }
                if (newSort != sort){
                    sort = newSort
                    index = 0
                }
                showCar()
            }
            override fun onNothingSelected(parent: AdapterView<*>?) {}
        }

        darkSwitch = findViewById(R.id.darkSwitch)

        sortSpinner.setSelection(sort.ordinal)
        if (query.isNotEmpty()) searchView.setQuery(query, false)

// Show the current mode first, then attach the listener (so setting it doesn't trigger a change)
        val isNight = (resources.configuration.uiMode and Configuration.UI_MODE_NIGHT_MASK) ==
                Configuration.UI_MODE_NIGHT_YES
        darkSwitch.isChecked = isNight
        darkSwitch.setOnCheckedChangeListener { _, checked ->
            AppCompatDelegate.setDefaultNightMode(
                if (checked) AppCompatDelegate.MODE_NIGHT_YES else AppCompatDelegate.MODE_NIGHT_NO
            )
        }

        cancelBookingButton.setOnClickListener { cancelLastBooking() }
    }
    private fun currentCar(): Car? {
        val list = CarRepository.getAvailable(query, sort)
        if (list.isEmpty()) return null
        return list[((index % list.size) + list.size) % list.size]
    }

    private fun toggleCurrentFavourite() {
        val car = currentCar() ?: return
        CarRepository.toggleFavourite(car.id)
        val msg = if (car.isFavourite) R.string.favourite_added else R.string.favourite_removed
        Snackbar.make(root, getString(msg, "${car.name} ${car.model}"), Snackbar.LENGTH_SHORT).show()
        showCar()
    }

    private fun jumpTo(car: Car) {
        val list = CarRepository.getAvailable(query, sort)
        val position = list.indexOfFirst { it.id == car.id }
        if (position >= 0) {
            index = position
            showCar()
        }
    }

    private fun showFavourites() {
        favouritesContainer.removeAllViews()
        val favourites = CarRepository.getFavourites().filter { !it.isRented }

        noFavouritesText.visibility = if (favourites.isEmpty()) View.VISIBLE else View.GONE

        for (car in favourites) {
            val button = Button(this)
            button.text = "${car.name} ${car.model}"
            button.setOnClickListener { jumpTo(car) }
            favouritesContainer.addView(button)
        }
    }

    override fun onResume() {
        super.onResume()
        showCar()
    }

    override fun onSaveInstanceState(outState: Bundle) {
        super.onSaveInstanceState(outState)
        outState.putInt("index", index)
        outState.putString("query", query)
        outState.putInt("sort", sort.ordinal)
    }

    private fun cancelLastBooking() {
        val booking = UserSession.bookings.lastOrNull()
        if (booking == null) {
            Snackbar.make(root, R.string.no_bookings, Snackbar.LENGTH_SHORT).show()
            return
        }
        BookingManager.cancel(booking)
        Snackbar.make(root, R.string.booking_cancelled, Snackbar.LENGTH_SHORT).show()
        showCar()
    }

    private fun showBookings()  {
        bookingsText.text =
            if (UserSession.bookings.isEmpty()) getString(R.string.no_bookings)
            else UserSession.bookings.joinToString("\n") {
                "${it.renterName}: ${it.car.name} ${it.car.model}, ${it.days} day(s), ${it.total} credits"
            }
    }

    private fun showCar() {
        balanceText.text = getString(R.string.balance_format, UserSession.balance)
        showBookings()
        showFavourites()

        val list: List<Car> = CarRepository.getAvailable(query, sort)

        if (list.isEmpty()) {
            carCard.visibility = View.GONE
            emptyText.visibility = View.VISIBLE
            rentButton.isEnabled = false
            return
        }
        carCard.visibility = View.VISIBLE
        emptyText.visibility = View.GONE
        rentButton.isEnabled = true

        index = ((index % list.size) + list.size) % list.size
        val car = list[index]

        carImage.setImageResource(car.imageRes)
        carTitle.text = "${car.name} ${car.model}"
        carYear.text = getString(R.string.year_format, car.year)
        carKm.text = getString(R.string.km_format, car.kilometres)
        carCost.text = getString(R.string.cost_format, car.dailyCost)
        carRating.rating = car.rating
        favouriteButton.setImageResource(
            if (car.isFavourite) R.drawable.ic_heart else R.drawable.ic_heart_outline
        )
    }
}