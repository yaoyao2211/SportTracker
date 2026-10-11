package com.example.sporttracker.ui

import android.Manifest
import android.annotation.SuppressLint
import android.content.Context
import android.content.pm.PackageManager
import android.location.Location
import android.location.LocationManager
import android.os.Environment
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
import org.osmdroid.tileprovider.tilesource.TileSourceFactory
import org.osmdroid.util.GeoPoint
import org.osmdroid.views.MapView
import org.osmdroid.views.overlay.Marker
import org.osmdroid.views.overlay.Polyline
import java.io.File
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

    // 权限请求
    val permissionLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { isGranted ->
        if (isGranted) {
            isRunning = true
        }
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
            
            // --- 地图显示区域 (仅跑步时显示) ---
            if (sport.needsGps) {
                Box(modifier = Modifier.weight(1f).fillMaxWidth().padding(vertical = 8.dp)) {
                    MapDisplay(
                        context = context,
                        isRunning = isRunning,
                        trackPoints = trackPoints
                    )
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
                    viewModel.saveWorkout(workout, trackPoints) {
                        onBack()
                    }
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
    // 初始化 osmdroid 配置
    Configuration.getInstance().load(context, androidx.preference.PreferenceManager.getDefaultSharedPreferences(context))
    
    // 设置离线地图路径（必须是手机外部存储的 Android/data/包名/files/ 目录）
    val osmdroidBasePath = context.getExternalFilesDir(null)?.absolutePath + "/osmdroid"
    val osmdroidTilePath = "$osmdroidBasePath/tiles"
    Configuration.getInstance().osmdroidBasePath = File(osmdroidBasePath)
    Configuration.getInstance().osmdroidTileCache = File(osmdroidTilePath)

    val mapView = remember {
        MapView(context).apply {
            setTileSource(TileSourceFactory.MAPNIK)
            setMultiTouchControls(true)
            controller.setZoom(18.0)
        }
    }

    // 监听 GPS 位置并绘制轨迹
    DisposableEffect(isRunning) {
        val locationManager = context.getSystemService(Context.LOCATION_SERVICE) as LocationManager
        val polyline = Polyline(mapView).apply {
            outlinePaint.color = android.graphics.Color.BLUE
            outlinePaint.strokeWidth = 10f
        }
        mapView.overlays.add(polyline)

        val locationListener = object : android.location.LocationListener {
            override fun onLocationChanged(location: Location) {
                val geoPoint = GeoPoint(location.latitude, location.longitude)
                trackPoints.add(TrackPoint(workoutId = 0, latitude = location.latitude, longitude = location.longitude, timestamp = System.currentTimeMillis()))
                polyline.addPoint(geoPoint)
                mapView.controller.animateTo(geoPoint)
                mapView.invalidate()
            }
            override fun onStatusChanged(provider: String?, status: Int, extras: android.os.Bundle?) {}
            override fun onProviderEnabled(provider: String) {}
            override fun onProviderDisabled(provider: String) {}
        }

        if (isRunning) {
            locationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, 1000L, 1f, locationListener)
        }

        onDispose {
            locationManager.removeUpdates(locationListener)
            mapView.onDetach()
        }
    }

    AndroidView(
        factory = { mapView },
        modifier = Modifier.fillMaxSize()
    )
}

private fun formatDuration(millis: Long): String {
    val totalSeconds = millis / 1000
    val hours = totalSeconds / 3600
    val minutes = (totalSeconds % 3600) / 60
    val seconds = totalSeconds % 60
    return if (hours > 0) String.format("%d:%02d:%02d", hours, minutes, seconds)
    else String.format("%02d:%02d", minutes, seconds)
}