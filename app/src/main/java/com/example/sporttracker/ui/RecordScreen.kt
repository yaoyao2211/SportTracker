package com.example.sporttracker.ui

import android.Manifest
import android.annotation.SuppressLint
import android.content.Context
import android.content.pm.PackageManager
import android.location.Location
import android.location.LocationManager
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.core.content.ContextCompat
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.sporttracker.data.SportType
import com.example.sporttracker.data.TrackPoint
import com.example.sporttracker.data.Workout
import kotlinx.coroutines.delay
import org.osmdroid.config.Configuration
import org.osmdroid.tileprovider.tilesource.OnlineTileSourceBase
import org.osmdroid.util.GeoPoint
import org.osmdroid.util.MapTileIndex
import org.osmdroid.views.MapView
import org.osmdroid.views.overlay.Polyline
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun RecordScreen(
    sport: SportType,
    onBack: () -> Unit,
    viewModel: WorkoutViewModel = viewModel()
) {
    val context = LocalContext.current
    var elapsedMillis by remember { mutableStateOf(0L) }
    var isRunning by remember { mutableStateOf(false) }
    var sets by remember { mutableStateOf(0) }
    val trackPoints = remember { mutableStateListOf<TrackPoint>() }

    val permissionLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { isGranted ->
        if (isGranted) isRunning = true
    }

    LaunchedEffect(isRunning) {
        while (isRunning) {
            delay(1000L)
            elapsedMillis += 1000L
        }
    }

    val startTime = remember { System.currentTimeMillis() }
    val startDateStr = remember { SimpleDateFormat("yyyy-MM-dd HH:mm", Locale.CHINA).format(Date(startTime)) }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(sport.displayName) },
                navigationIcon = { TextButton(onClick = onBack) { Text("返回") } },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = MaterialTheme.colorScheme.background)
            )
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .padding(padding)
                .fillMaxSize()
                .padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text("开始时间：$startDateStr", style = MaterialTheme.typography.bodyMedium)
            
            if (sport.needsGps) {
                Box(modifier = Modifier.weight(1f).fillMaxWidth().padding(vertical = 8.dp)) {
                    MapDisplay(context, isRunning, trackPoints)
                }
            } else {
                Spacer(modifier = Modifier.weight(1f))
            }

            Text(
                text = formatDuration(elapsedMillis),
                fontSize = 64.sp,
                style = MaterialTheme.typography.displayLarge,
                color = MaterialTheme.colorScheme.primary
            )

            if (sport.isStrength) {
                Spacer(modifier = Modifier.height(8.dp))
                Text("已完成组数: $sets", fontSize = 20.sp)
                Button(onClick = { sets++ }, modifier = Modifier.padding(4.dp)) { Text("+1 组") }
            }

            Spacer(modifier = Modifier.height(16.dp))

            Row(horizontalArrangement = Arrangement.spacedBy(16.dp)) {
                Button(
                    onClick = {
                        if (!isRunning) {
                            if (ContextCompat.checkSelfPermission(context, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED) {
                                isRunning = true
                            } else {
                                permissionLauncher.launch(Manifest.permission.ACCESS_FINE_LOCATION)
                            }
                        } else {
                            isRunning = false
                        }
                    },
                    shape = RoundedCornerShape(12.dp),
                    modifier = Modifier.weight(1f).height(56.dp)
                ) {
                    Text(if (isRunning) "暂停" else "开始", fontSize = 18.sp)
                }
                OutlinedButton(
                    onClick = {
                        isRunning = false
                        elapsedMillis = 0
                        sets = 0
                        trackPoints.clear()
                    },
                    shape = RoundedCornerShape(12.dp),
                    modifier = Modifier.weight(1f).height(56.dp)
                ) {
                    Text("重置", fontSize = 18.sp)
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            Button(
                onClick = {
                    val workout = Workout(
                        type = sport.name,
                        startTime = startTime,
                        endTime = System.currentTimeMillis(),
                        totalDurationMillis = elapsedMillis,
                        note = if (sport.isStrength) "组数: $sets" else null
                    )
                    viewModel.saveWorkout(workout, trackPoints) { onBack() }
                },
                enabled = elapsedMillis > 0,
                shape = RoundedCornerShape(12.dp),
                modifier = Modifier.fillMaxWidth().height(56.dp),
                colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.secondary)
            ) {
                Text("保存记录", fontSize = 18.sp)
            }
        }
    }
}

@SuppressLint("MissingPermission")
@Composable
fun MapDisplay(
    context: Context,
    isRunning: Boolean,
    trackPoints: MutableList<TrackPoint>
) {
    Configuration.getInstance().load(context, context.getSharedPreferences("osmdroid", Context.MODE_PRIVATE))

    val gaodeSource = remember {
        object : OnlineTileSourceBase(
            "Gaode",
            1, 19, 256, "",
            arrayOf(
                "https://webrd01.is.autonavi.com/appmaptile?lang=zh_cn&size=1&scale=1&style=8&x=",
                "https://webrd02.is.autonavi.com/appmaptile?lang=zh_cn&size=1&scale=1&style=8&x=",
                "https://webrd03.is.autonavi.com/appmaptile?lang=zh_cn&size=1&scale=1&style=8&x=",
                "https://webrd04.is.autonavi.com/appmaptile?lang=zh_cn&size=1&scale=1&style=8&x="
            )
        ) {
            override fun getTileURLString(pMapTileIndex: Long): String {
                val x = MapTileIndex.getX(pMapTileIndex)
                val y = MapTileIndex.getY(pMapTileIndex)
                val z = MapTileIndex.getZoom(pMapTileIndex)
                return baseUrl + x + "&y=" + y + "&z=" + z
            }
        }
    }

    val mapView = remember {
        MapView(context).apply {
            setTileSource(gaodeSource)
            setMultiTouchControls(true)
            controller.setZoom(18.0)
        }
    }

    // 地图只在组件销毁时释放，绝不受 isRunning 影响
    DisposableEffect(Unit) {
        onDispose { mapView.onDetach() }
    }

    val polyline = remember {
        Polyline(mapView).apply {
            outlinePaint.color = android.graphics.Color.BLUE
            outlinePaint.strokeWidth = 10f
        }
    }

    // 监听 isRunning 状态，开启/关闭定位
    DisposableEffect(isRunning) {
        val locationManager = context.getSystemService(Context.LOCATION_SERVICE) as LocationManager

        val locationListener = object : android.location.LocationListener {
            override fun onLocationChanged(location: Location) {
                // 坐标转换 WGS-84 -> GCJ-02 (解决高德地图偏移问题)
                val gcjPoint = wgs84ToGcj02(location.latitude, location.longitude)
                val geoPoint = GeoPoint(gcjPoint.first, gcjPoint.second)
                
                trackPoints.add(TrackPoint(workoutId = 0, latitude = gcjPoint.first, longitude = gcjPoint.second, timestamp = System.currentTimeMillis()))
                polyline.addPoint(geoPoint)
                mapView.controller.animateTo(geoPoint)
                mapView.invalidate()
            }
            override fun onStatusChanged(provider: String?, status: Int, extras: android.os.Bundle?) {}
            override fun onProviderEnabled(provider: String) {}
            override fun onProviderDisabled(provider: String) {}
        }

        if (isRunning) {
            if (ContextCompat.checkSelfPermission(context, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED) {
                if (polyline.outlinePaint != null) {
                    mapView.overlays.add(polyline) // 确保线只加一次
                }
                locationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, 1000L, 1f, locationListener)
            }
        }

        onDispose {
            locationManager.removeUpdates(locationListener)
        }
    }

    AndroidView(factory = { mapView }, modifier = Modifier.fillMaxSize())
}

// WGS-84 转 GCJ-02 坐标转换算法 (解决国内地图偏移)
private fun wgs84ToGcj02(lat: Double, lon: Double): Pair<Double, Double> {
    val a = 6378245.0
    val ee = 0.00669342162296594323
    var dLat = transformLat(lon - 105.0, lat - 35.0)
    var dLon = transformLon(lon - 105.0, lat - 35.0)
    val radLat = lat / 180.0 * Math.PI
    var magic = Math.sin(radLat)
    magic = 1 - ee * magic * magic
    val sqrtMagic = Math.sqrt(magic)
    dLat = (dLat * 180.0) / ((a * (1 - ee)) / (magic * sqrtMagic) * Math.PI)
    dLon = (dLon * 180.0) / (a / sqrtMagic * Math.cos(radLat) * Math.PI)
    return Pair(lat + dLat, lon + dLon)
}

private fun transformLat(x: Double, y: Double): Double {
    var ret = -100.0 + 2.0 * x + 3.0 * y + 0.2 * y * y + 0.1 * x * y + 0.2 * Math.sqrt(Math.abs(x))
    ret += (20.0 * Math.sin(6.0 * x * Math.PI) + 20.0 * Math.sin(2.0 * x * Math.PI)) * 2.0 / 3.0
    ret += (20.0 * Math.sin(y * Math.PI) + 40.0 * Math.sin(y / 3.0 * Math.PI)) * 2.0 / 3.0
    ret += (160.0 * Math.sin(y / 12.0 * Math.PI) + 320 * Math.sin(y * Math.PI / 30.0)) * 2.0 / 3.0
    return ret
}

private fun transformLon(x: Double, y: Double): Double {
    var ret = 300.0 + x + 2.0 * y + 0.1 * x * x + 0.1 * x * y + 0.1 * Math.sqrt(Math.abs(x))
    ret += (20.0 * Math.sin(6.0 * x * Math.PI) + 20.0 * Math.sin(2.0 * x * Math.PI)) * 2.0 / 3.0
    ret += (20.0 * Math.sin(x * Math.PI) + 40.0 * Math.sin(x / 3.0 * Math.PI)) * 2.0 / 3.0
    ret += (150.0 * Math.sin(x / 12.0 * Math.PI) + 300.0 * Math.sin(x / 30.0 * Math.PI)) * 2.0 / 3.0
    return ret
}

private fun formatDuration(millis: Long): String {
    val totalSeconds = millis / 1000
    val hours = totalSeconds / 3600
    val minutes = (totalSeconds % 3600) / 60
    val seconds = totalSeconds % 60
    return if (hours > 0) String.format("%d:%02d:%02d", hours, minutes, seconds)
    else String.format("%02d:%02d", minutes, seconds)
}