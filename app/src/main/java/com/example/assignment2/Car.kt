package com.example.assignment2

import android.os.Parcel
import android.os.Parcelable

data class Car(
    val id: Int,
    val name: String,
    val model: String,
    val year: Int,
    val rating: Float,          // 1..5
    val kilometres: Int,
    val dailyCost: Int,         // in credits
    val imageRes: Int,
    var isFavourite: Boolean = false,
    var isRented: Boolean = false
) : Parcelable {

    constructor(parcel: Parcel) : this(
        parcel.readInt(),
        parcel.readString() ?: "",
        parcel.readString() ?: "",
        parcel.readInt(),
        parcel.readFloat(),
        parcel.readInt(),
        parcel.readInt(),
        parcel.readInt(),
        parcel.readInt() != 0,
        parcel.readInt() != 0
    )

    override fun writeToParcel(parcel: Parcel, flags: Int) {
        parcel.writeInt(id)
        parcel.writeString(name)
        parcel.writeString(model)
        parcel.writeInt(year)
        parcel.writeFloat(rating)
        parcel.writeInt(kilometres)
        parcel.writeInt(dailyCost)
        parcel.writeInt(imageRes)
        parcel.writeInt(if (isFavourite) 1 else 0)
        parcel.writeInt(if (isRented) 1 else 0)
    }

    override fun describeContents(): Int = 0

    companion object CREATOR : Parcelable.Creator<Car> {
        override fun createFromParcel(parcel: Parcel): Car = Car(parcel)
        override fun newArray(size: Int): Array<Car?> = arrayOfNulls(size)
    }
}