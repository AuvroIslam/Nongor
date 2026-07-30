package org.nongor.app.mesh

import android.Manifest
import android.bluetooth.BluetoothManager
import android.content.Context
import android.content.pm.PackageManager
import android.location.LocationManager
import android.net.wifi.WifiManager
import android.os.Build
import androidx.core.content.ContextCompat

/**
 * Why the mesh is not finding anybody.
 *
 * Nearby Connections discovers peers over Bluetooth LE and upgrades to Wi-Fi Direct for the
 * payload. If either radio is switched off at the OS level, or a permission was declined, the
 * app advertises into the void: no error, no callback, just a peer count that stays at zero
 * forever.
 *
 * That silence was the whole problem. Two phones sitting on the same table, both showing
 * "listening", neither ever seeing the other, and nothing on screen saying that Bluetooth was
 * off. A radio that cannot work must say so — "0 phones in range" is a fact about the world,
 * "your Bluetooth is off" is a fact the user can act on.
 */
enum class MeshBlocker {
    /** Runtime permissions were never granted, or were declined. */
    PERMISSIONS,

    /** Bluetooth is switched off. Nothing can be discovered without it. */
    BLUETOOTH,

    /** Wi-Fi is switched off. Discovery may still work; payload transfer is crippled. */
    WIFI,

    /** Location services are off device-wide, which blocks BLE scanning on most builds. */
    LOCATION_SERVICES,
}

object MeshReadiness {

    /** The runtime permissions Nearby needs on this API level. */
    fun requiredPermissions(): List<String> = buildList {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            add(Manifest.permission.BLUETOOTH_SCAN)
            add(Manifest.permission.BLUETOOTH_ADVERTISE)
            add(Manifest.permission.BLUETOOTH_CONNECT)
        }
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            add(Manifest.permission.NEARBY_WIFI_DEVICES)
        }
        add(Manifest.permission.ACCESS_FINE_LOCATION)
    }

    /**
     * Everything currently standing between this phone and a working mesh, worst first.
     *
     * Empty means the radio has what it needs — which is not a promise that a peer is nearby,
     * only that we are genuinely listening.
     */
    fun blockers(context: Context): List<MeshBlocker> = buildList {
        if (!hasPermissions(context)) add(MeshBlocker.PERMISSIONS)
        if (bluetoothEnabled(context) == false) add(MeshBlocker.BLUETOOTH)
        if (locationServicesEnabled(context) == false) add(MeshBlocker.LOCATION_SERVICES)
        if (wifiEnabled(context) == false) add(MeshBlocker.WIFI)
    }

    fun hasPermissions(context: Context): Boolean = requiredPermissions().all {
        ContextCompat.checkSelfPermission(context, it) == PackageManager.PERMISSION_GRANTED
    }

    /**
     * Null when we genuinely cannot tell.
     *
     * Reading the adapter state needs BLUETOOTH_CONNECT on API 31+, so before the permission
     * is granted this throws. Returning null rather than false keeps the UI from claiming
     * "Bluetooth is off" when the truth is "we are not allowed to look" — a wrong diagnosis
     * sends someone to the wrong settings screen, which is worse than no diagnosis.
     */
    fun bluetoothEnabled(context: Context): Boolean? = runCatching {
        val mgr = context.getSystemService(Context.BLUETOOTH_SERVICE) as? BluetoothManager
        mgr?.adapter?.isEnabled
    }.getOrNull()

    fun wifiEnabled(context: Context): Boolean? = runCatching {
        val mgr = context.applicationContext.getSystemService(Context.WIFI_SERVICE) as? WifiManager
        mgr?.isWifiEnabled
    }.getOrNull()

    fun locationServicesEnabled(context: Context): Boolean? = runCatching {
        val mgr = context.getSystemService(Context.LOCATION_SERVICE) as? LocationManager
        when {
            mgr == null -> null
            Build.VERSION.SDK_INT >= Build.VERSION_CODES.P -> mgr.isLocationEnabled
            else -> mgr.isProviderEnabled(LocationManager.GPS_PROVIDER) ||
                mgr.isProviderEnabled(LocationManager.NETWORK_PROVIDER)
        }
    }.getOrNull()
}
