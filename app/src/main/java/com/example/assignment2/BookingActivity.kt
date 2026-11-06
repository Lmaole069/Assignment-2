package com.example.assignment2

import android.content.Intent
import android.os.Bundle
import android.widget.Button
import android.widget.EditText
import android.widget.ImageView
import android.widget.RatingBar
import android.widget.TextView
import androidx.activity.OnBackPressedCallback
import androidx.appcompat.app.AppCompatActivity
import androidx.core.widget.doAfterTextChanged
import com.google.android.material.slider.Slider
import com.google.android.material.snackbar.Snackbar
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat

class BookingActivity : AppCompatActivity() {

    companion object {
        const val EXTRA_CAR = "extra_car"          // Parcelable Car (in)
        const val EXTRA_DAYS = "extra_days"        // Int (out)
        const val EXTRA_NAME = "extra_name"        // String (out)
        const val EXTRA_CAR_ID = "extra_car_id"    // Int (out)
    }

    private lateinit var car: Car

    private lateinit var balanceText: TextView
    private lateinit var carImage: ImageView
    private lateinit var carTitle: TextView
    private lateinit var carYear: TextView
    private lateinit var carKm: TextView
    private lateinit var carCost: TextView
    private lateinit var carRating: RatingBar
    private lateinit var nameInput: EditText
    private lateinit var daysText: TextView
    private lateinit var daysSlider: Slider
    private lateinit var totalText: TextView
    private lateinit var saveButton: Button
    private lateinit var cancelButton: Button

    private var defaultTotalColor = 0

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_booking)

        balanceText = findViewById(R.id.balanceText)
        carImage = findViewById(R.id.carImage)
        carTitle = findViewById(R.id.carTitle)
        carYear = findViewById(R.id.carYear)
        carKm = findViewById(R.id.carKm)
        carCost = findViewById(R.id.carCost)
        carRating = findViewById(R.id.carRating)
        nameInput = findViewById(R.id.nameInput)
        daysText = findViewById(R.id.daysText)
        daysSlider = findViewById(R.id.daysSlider)
        totalText = findViewById(R.id.totalText)
        saveButton = findViewById(R.id.saveButton)
        cancelButton = findViewById(R.id.cancelButton)

        val pad = (16 * resources.displayMetrics.density).toInt()
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.rootLayout)) { v, insets ->
            val bars = insets.getInsets(
                WindowInsetsCompat.Type.systemBars() or WindowInsetsCompat.Type.displayCutout()
            )
            v.setPadding(pad + bars.left, pad + bars.top, pad + bars.right, pad + bars.bottom)
            insets
        }

        // Receive the Parcelable sent by MainActivity
        car = intent.getParcelableExtra(EXTRA_CAR, Car::class.java)!!

        defaultTotalColor = totalText.currentTextColor

        balanceText.text = getString(R.string.balance_format, UserSession.balance)
        carImage.setImageResource(car.imageRes)
        carTitle.text = "${car.name} ${car.model}"
        carYear.text = getString(R.string.year_format, car.year)
        carKm.text = getString(R.string.km_format, car.kilometres)
        carCost.text = getString(R.string.cost_format, car.dailyCost)
        carRating.rating = car.rating

        updateTotal(daysSlider.value.toInt())
        daysSlider.addOnChangeListener { _, value, _ -> updateTotal(value.toInt()) }

        // Clear the name error as soon as the user types
        nameInput.doAfterTextChanged { nameInput.error = null }

        saveButton.setOnClickListener { save() }
        cancelButton.setOnClickListener { cancel() }

        // The system Back button counts as a cancellation
        onBackPressedDispatcher.addCallback(this, object : OnBackPressedCallback(true) {
            override fun handleOnBackPressed() = cancel()
        })
    }

    private fun updateTotal(days: Int) {
        val total = days * car.dailyCost
        daysText.text = getString(R.string.days_format, days)
        totalText.text = getString(R.string.total_format, total)

        // Red when the booking would be rejected
        val tooMuch = total > UserSession.MAX_PER_RENTAL || total > UserSession.balance
        totalText.setTextColor(
            if (tooMuch) getColor(R.color.over_limit) else defaultTotalColor
        )
    }

    private fun save() {
        val name = nameInput.text.toString().trim()
        val days = daysSlider.value.toInt()
        val total = days * car.dailyCost

        if (name.isEmpty()) {
            nameInput.error = getString(R.string.error_name_required)
            nameInput.requestFocus()
            return
        }
        if (total > UserSession.MAX_PER_RENTAL) {
            Snackbar.make(
                findViewById(R.id.saveButton),
                getString(R.string.error_over_limit, UserSession.MAX_PER_RENTAL),
                Snackbar.LENGTH_LONG
            ).show()
            return
        }
        if (total > UserSession.balance) {
            Snackbar.make(findViewById(R.id.saveButton), R.string.error_insufficient, Snackbar.LENGTH_LONG).show()
            return
        }

        // All checks passed: send the data back to MainActivity
        val result = Intent().apply {
            putExtra(EXTRA_CAR_ID, car.id)
            putExtra(EXTRA_NAME, name)
            putExtra(EXTRA_DAYS, days)
        }
        setResult(RESULT_OK, result)
        finish()
    }

    private fun cancel() {
        setResult(RESULT_CANCELED)
        finish()
    }
}