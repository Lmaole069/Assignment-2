package com.example.assignment2

enum class SortOption { NONE, RATING_HIGH_LOW, YEAR_NEWEST, COST_LOW_HIGH }

object CarRepository {

    private val cars = mutableListOf(
        Car(1, "Toyota", "Wigo", 2013, 4.0f, 14_000, 55, R.drawable.car1),
        Car(2, "Hyundai", "Grand 10", 2014, 4.3f, 19_000, 60, R.drawable.car2),
        Car(3, "Kia", "Morning", 2003, 4.5f, 22_000, 65, R.drawable.car3),
        Car(4, "VinFast", "Fadil", 2018, 4.0f, 30_000, 70, R.drawable.car4),
        Car(5, "Hyundai", "Accent", 2017, 5.0f, 67_000, 75, R.drawable.car5)
    )

    fun getAvailable(query: String = "", sort: SortOption = SortOption.NONE): List<Car> {
        val filtered = cars.filter {
            !it.isRented &&
                    (query.isBlank() ||
                            it.name.contains(query, ignoreCase = true) ||
                            it.model.contains(query, ignoreCase = true))
        }
        return when (sort) {
            SortOption.NONE -> filtered
            SortOption.RATING_HIGH_LOW -> filtered.sortedByDescending { it.rating }
            SortOption.YEAR_NEWEST -> filtered.sortedByDescending { it.year }
            SortOption.COST_LOW_HIGH -> filtered.sortedBy { it.dailyCost }
        }
    }

    fun getFavourites(): List<Car> = cars.filter { it.isFavourite }

    fun getById(id: Int): Car? = cars.find { it.id == id }

    fun setRented(id: Int, rented: Boolean) {
        getById(id)?.isRented = rented
    }

    fun toggleFavourite(id: Int) {
        getById(id)?.let { it.isFavourite = !it.isFavourite }
    }
}