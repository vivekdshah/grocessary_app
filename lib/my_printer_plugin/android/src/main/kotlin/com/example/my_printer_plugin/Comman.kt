package com.example.my_printer_plugin

import java.text.SimpleDateFormat
import java.util.Locale
import java.util.TimeZone

fun Double.formatToTwoDecimalPlaces(): String {
    return "%.2f".format(this)
}

fun String.formatDateTime(): String {
    val inputFormat = SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSSSSS'Z'", Locale.getDefault())
    val outputDateFormat = SimpleDateFormat("MM/dd/yyyy", Locale.getDefault())
    val outputTimeFormat = SimpleDateFormat("hh:mm a", Locale.getDefault())

    // Set time zone to UTC for input
    inputFormat.timeZone = TimeZone.getTimeZone("UTC")

    // Parse the input date
    val date = inputFormat.parse(this)

    // Set time zone to Eastern Time Zone (EST) for output
    outputDateFormat.timeZone = TimeZone.getDefault()
    outputTimeFormat.timeZone = TimeZone.getDefault()

    // Format the date and time
    val formattedDate = outputDateFormat.format(date)
    val formattedTime = outputTimeFormat.format(date)

    return "Date: ${formattedDate}   Time: ${formattedTime}"
}


fun String.formatDate(): String {
    val inputFormat = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
    val outputDateFormat = SimpleDateFormat("MM/dd/yyyy", Locale.getDefault())

    // Parse the input date
    val date = inputFormat.parse(this)

    // Format the date
    val formattedDate = outputDateFormat.format(date)

    return formattedDate
}

fun String.formatTime(): String {
    val inputFormat = SimpleDateFormat("HH:mm:ss", Locale.getDefault())
    val outputTimeFormat = SimpleDateFormat("hh:mm a", Locale.getDefault())

    // Parse the input time
    val time = inputFormat.parse(this)

    // Format the time
    val formattedTime = outputTimeFormat.format(time)

    return formattedTime
}