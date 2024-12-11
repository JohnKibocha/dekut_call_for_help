// app/src/main/java/com/muriithi/dekutcallforhelp/beans/User.kt
package com.muriithi.dekutcallforhelp.beans
import android.os.Parcel
import android.os.Parcelable

class User() : Parcelable {
    var userId: String? = null
    var firstName: String? = null
    var lastName: String? = null
    var course: String? = null
    var school: String? = null
    var registrationNumber: String? = null
    var idNumber: Int? = null
    var dateOfBirth: String? = null
    var emailAddress: String? = null
    var phoneNumber: String? = null
    var profilePhoto: String? = null
    var superuser: Boolean = false
    var countryCode: String = "254"
    var oneSignalPlayerId: String? = null
    var latitude: Double? = null
    var longitude: Double? = null

    constructor(parcel: Parcel) : this() {
        userId = parcel.readString()
        firstName = parcel.readString()
        lastName = parcel.readString()
        course = parcel.readString()
        school = parcel.readString()
        registrationNumber = parcel.readString()
        idNumber = parcel.readValue(Int::class.java.classLoader) as? Int
        dateOfBirth = parcel.readString()
        emailAddress = parcel.readString()
        phoneNumber = parcel.readString()
        profilePhoto = parcel.readString()
        superuser = parcel.readByte() != 0.toByte()
        countryCode = parcel.readString() ?: "254"
        oneSignalPlayerId = parcel.readString()
        latitude = parcel.readValue(Double::class.java.classLoader) as? Double
        longitude = parcel.readValue(Double::class.java.classLoader) as? Double
    }

    override fun writeToParcel(parcel: Parcel, flags: Int) {
        parcel.writeString(userId)
        parcel.writeString(firstName)
        parcel.writeString(lastName)
        parcel.writeString(course)
        parcel.writeString(school)
        parcel.writeString(registrationNumber)
        parcel.writeValue(idNumber)
        parcel.writeString(dateOfBirth)
        parcel.writeString(emailAddress)
        parcel.writeString(phoneNumber)
        parcel.writeString(profilePhoto)
        parcel.writeByte(if (superuser) 1 else 0)
        parcel.writeString(countryCode)
        parcel.writeString(oneSignalPlayerId)
        parcel.writeValue(latitude)
        parcel.writeValue(longitude)
    }

    override fun describeContents(): Int {
        return 0
    }

    companion object CREATOR : Parcelable.Creator<User> {
        override fun createFromParcel(parcel: Parcel): User {
            return User(parcel)
        }

        override fun newArray(size: Int): Array<User?> {
            return arrayOfNulls(size)
        }
    }
}