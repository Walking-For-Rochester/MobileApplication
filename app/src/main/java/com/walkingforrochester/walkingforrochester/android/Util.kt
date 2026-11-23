package com.walkingforrochester.walkingforrochester.android

import android.Manifest
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.icu.text.NumberFormat
import androidx.annotation.StringRes
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import androidx.core.content.ContextCompat
import com.squareup.moshi.FromJson
import com.squareup.moshi.ToJson
import com.walkingforrochester.walkingforrochester.android.service.ForegroundLocationService.Companion.NOTIFICATION_CHANNEL_ID
import com.walkingforrochester.walkingforrochester.android.service.ForegroundLocationService.Companion.NOTIFICATION_ID
import timber.log.Timber
import java.math.BigInteger
import java.security.MessageDigest
import java.time.LocalDate
import java.time.format.DateTimeFormatter
import java.time.format.DateTimeParseException
import java.util.Locale
import kotlin.concurrent.getOrSet
import kotlin.time.Duration.Companion.milliseconds

fun showNotification(
    context: Context,
    @StringRes messageResId: Int
) {
    val launchActivityIntent = Intent(context, MainActivity::class.java)
    val activityPendingIntent = PendingIntent.getActivity(
        context,
        0,
        launchActivityIntent,
        PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
    )
    val title = context.getString(R.string.app_name)
    val message = context.getString(messageResId)

    val bigTextStyle = NotificationCompat.BigTextStyle()
        .bigText(message)
        .setBigContentTitle(title)

    val builder = NotificationCompat.Builder(context, NOTIFICATION_CHANNEL_ID)
        .setSmallIcon(R.drawable.ic_notification)
        .setStyle(bigTextStyle)
        .setContentTitle(title)
        .setContentText(message)
        .setContentIntent(activityPendingIntent)
        .setDefaults(NotificationCompat.DEFAULT_ALL)
        .setAutoCancel(true)

    if (ContextCompat.checkSelfPermission(context, Manifest.permission.POST_NOTIFICATIONS)
        == PackageManager.PERMISSION_GRANTED
    ) {
        with(NotificationManagerCompat.from(context)) {
            notify(NOTIFICATION_ID, builder.build())
        }
    } else {
        Timber.d("Skipping notification as permission not granted")
    }
}

fun Double.metersToMiles(): Double = this / 1609.344

private fun buildNumberFormat(locale: Locale): NumberFormat {
    return NumberFormat.getNumberInstance(locale).apply {
        minimumFractionDigits = 2
        maximumFractionDigits = 2
    }
}

private val cache = ThreadLocal<MutableMap<Locale, NumberFormat>>()

fun Double.formatDouble(locale: Locale): String {
    val map = cache.getOrSet { mutableMapOf() }
    val formatter = map.getOrPut(locale){ buildNumberFormat(locale) }
    return formatter.format(this)
}

fun Double.formatMetersToMiles(locale: Locale): String {
    return "${this.metersToMiles().formatDouble(locale)} mi"
}

private const val twoDigit = "%02d"
fun Long.formatElapsedMilli(): String {
    return this.milliseconds.toComponents { hours, minutes, seconds, _ ->
        val minSec = "${twoDigit.format(minutes)}:${twoDigit.format(seconds)}"
        if (hours > 0) {
            "$hours:$minSec"
        } else {
            minSec
        }
    }
}

fun md5(input: String): String {
    val md = MessageDigest.getInstance("MD5")
    return BigInteger(1, md.digest(input.toByteArray())).toString(16).padStart(32, '0')
}

class LocalDateAdapter {
    @ToJson
    fun toJson(value: LocalDate): String {
        return value.toString()
    }

    @FromJson
    fun fromJson(value: String): LocalDate {
        formatters.forEach { formatter ->
            try {
                return LocalDate.parse(value, formatter)
            } catch (_: DateTimeParseException) {
                //
            }
        }

        throw RuntimeException("Unable to parse date: $value")
    }

    companion object {
        val formatters =
            listOf(DateTimeFormatter.ISO_LOCAL_DATE, DateTimeFormatter.ISO_LOCAL_DATE_TIME)
    }
}

class WFRDateFormatter {
    companion object {
        val formatter: DateTimeFormatter = DateTimeFormatter.ofPattern("MM_dd_yyyy_HH_MM_SS")
    }
}
