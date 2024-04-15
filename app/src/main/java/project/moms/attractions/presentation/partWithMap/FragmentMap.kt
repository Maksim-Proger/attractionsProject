package project.moms.attractions.presentation.partWithMap

import android.Manifest
import android.content.Context
import android.content.pm.PackageManager
import android.graphics.Bitmap
import android.graphics.Canvas
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.Observer
import androidx.navigation.fragment.findNavController
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationServices
import com.google.firebase.crashlytics.FirebaseCrashlytics
import com.google.firebase.messaging.FirebaseMessaging
import com.yandex.mapkit.Animation
import com.yandex.mapkit.MapKitFactory
import com.yandex.mapkit.geometry.Point
import com.yandex.mapkit.map.CameraPosition
import com.yandex.mapkit.map.MapObject
import com.yandex.mapkit.map.MapObjectCollection
import com.yandex.mapkit.map.PlacemarkMapObject
import com.yandex.mapkit.mapview.MapView
import com.yandex.runtime.image.ImageProvider
import project.moms.attractions.R
import project.moms.attractions.data.api.NetworkApi
import project.moms.attractions.databinding.FragmentMapBinding
import project.moms.attractions.model.Element
import project.moms.attractions.services.NotificationService

class FragmentMap : Fragment() {
    private var _binding : FragmentMapBinding? = null
    private val binding : FragmentMapBinding
        get() {return _binding!!}

    private val launcher = registerForActivityResult(ActivityResultContracts.RequestMultiplePermissions()) { map ->
        if (map.values.all { it }) {
//            showMyLocation()
        } else {
            Toast.makeText(requireContext(), "permission is not Granted", Toast.LENGTH_SHORT).show()
        }
    }

    private lateinit var mapView: MapView // Ссылка на компонент карты в UI
    private lateinit var mapObjects: MapObjectCollection // Коллекция всех объектов отображаемых на карте.
    private lateinit var fusedClient: FusedLocationProviderClient // Клиент для доступа к данным о местоположении устройства.
    private val markerDataMap = mutableMapOf<PlacemarkMapObject, Element>() // Словарь связывающий маркеры на карте с данными.
    private val placeMarks: MutableList<MapObject> = mutableListOf() // Список хранения всех созданных маркеров для управления ими.

    private val viewModel: MapViewModel by viewModels {
        MapViewModelFactory(NetworkApi.apiService)
    }

    private lateinit var notificationService: NotificationService

    private var saveLatitude = 0.0
    private var saveLongitude = 0.0
    private var saveZoom = 0.0f
    private var routeStartLocation = Point(0.0, 0.0)

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        _binding = FragmentMapBinding.inflate(inflater)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        notificationService = NotificationService(requireContext())

        mapView = view.findViewById(R.id.mapView)
        mapView.map.move(
            CameraPosition(
                Point(55.755811, 37.617617),
                15.0f,
                0.0f,
                0.0f
            )
        )
        mapObjects = mapView.map.mapObjects.addCollection()

        fusedClient = LocationServices.getFusedLocationProviderClient(requireContext())

        viewModel.landmarkData.observe(viewLifecycleOwner, Observer { landmarks ->
            landmarks?.let {
                addingIconsForElements(it)
            }
        })

        viewModel.fetchLandmarks()

        if (savedInstanceState != null) {
            saveLatitude = savedInstanceState.getDouble(SAVE_LATITUDE)
            saveLongitude = savedInstanceState.getDouble(SAVE_LONGITUDE)
            saveZoom = savedInstanceState.getFloat(SAVE_ZOOM)
            routeStartLocation = Point(saveLatitude, saveLongitude)
            cameraSavePosition()
        }

        binding.locationButton.setOnClickListener {
//            FirebaseCrashlytics.getInstance().log("Имитируем краш приложения!")
//            throw Exception("Приложение сломалось!")
            notificationService.createNotification()
            showMyLocation()
        }

        binding.enlargeButton.setOnClickListener { changeZoom(5f) }
        binding.reduceButton.setOnClickListener { changeZoom(-5f) }
    }

    // region saveState
    override fun onSaveInstanceState(outState: Bundle) {
        super.onSaveInstanceState(outState)
        outState.putDouble(SAVE_LATITUDE, saveLatitude)
        outState.putDouble(SAVE_LONGITUDE, saveLongitude)
        outState.putFloat(SAVE_ZOOM, saveZoom)
    }
    private fun cameraSavePosition() {
        mapView.mapWindow.map.move(
            CameraPosition(
                Point(saveLatitude, saveLongitude),
                saveZoom,
                0f,
                0f
            )
        )
    }
    // endregion

    private fun changeZoom(delta: Float) {
        val currentZoom = mapView.map.cameraPosition.zoom
        mapView.map.move(
            CameraPosition(mapView.map.cameraPosition.target, currentZoom + delta, 0.0f, 0.0f),
            Animation(Animation.Type.SMOOTH, 1f), null
        )
    }

    private fun checkPermissions() {
        val isAllGranted = REQUEST_PERMISSIONS.all { permission ->
            ContextCompat.checkSelfPermission(requireContext(), permission) ==
                    PackageManager.PERMISSION_GRANTED
        }
        if (isAllGranted) {
            if (!viewModel.permissionToastShown) {
                Toast.makeText(requireContext(), "Разрешения для локации предоставлены",
                    Toast.LENGTH_SHORT).show()
                viewModel.permissionToastShown = true
            }
        }
        else {
            launcher.launch(REQUEST_PERMISSIONS)
        }
    }

    private fun getBitmapFromVectorDrawable(context: Context, drawableId: Int): Bitmap? {
        val drawable = ContextCompat.getDrawable(context, drawableId)
        drawable?.let {
            val bitmap = Bitmap.createBitmap(drawable.intrinsicWidth,
                drawable.intrinsicHeight, Bitmap.Config.ARGB_8888)

            val canvas = Canvas(bitmap)
            drawable.setBounds(0, 0, canvas.width, canvas.height)
            drawable.draw(canvas)
            return bitmap
        }
        return null
    }

    private fun showMyLocation() {
        val permissionsToRequest = REQUEST_PERMISSIONS.filter {
            ContextCompat.checkSelfPermission(requireContext(), it) !=
                    PackageManager.PERMISSION_GRANTED
        }.toTypedArray()

        if (permissionsToRequest.isEmpty()) {
            fusedClient.lastLocation.addOnSuccessListener { location ->
                location?.let {
                    Log.d("My location", "Получено местоположение: $location")
                    val point = Point(it.latitude, it.longitude)
                    saveLatitude = it.latitude
                    saveLongitude = it.longitude
                    saveZoom = 11.0f
                    mapView.map.move(
                        CameraPosition(
                            point,
                            17.0f,
                            0.0f,
                            0.0f
                        ), Animation(Animation.Type.SMOOTH, 3f), null
                    )
                    val currentLocation = Element(
                        name = "My location",
                        lat = it.latitude,
                        lon = it.longitude,
                        tags = mapOf()
                    )
                    val bitmap = getBitmapFromVectorDrawable(requireContext(), R.drawable.marker)
                    val icon = ImageProvider.fromBitmap(bitmap)
                    addMarks(point, currentLocation, icon)
                }
            }
        }
        else {
            launcher.launch(permissionsToRequest)
        }
    }

    private fun addingIconsForElements(landmarks: List<Element>) {
        val bitmap = getBitmapFromVectorDrawable(requireContext(), R.drawable.marker)
        val icon = ImageProvider.fromBitmap(bitmap)
        for (landmark in landmarks) {
            checkCoordinate(landmark, icon)
        }
    }

    private fun checkCoordinate(element: Element, icon: ImageProvider) {
        if (isValidCoordinate(element.lat, element.lon)) {
            val name = element.tags["name:en"] ?: "Unknown"
            if (name != "Unknown") {
                val point = Point(element.lat, element.lon)
                addMarks(point, element, icon)
            }
        }
    }

    private fun isValidCoordinate(lat: Double, lon: Double): Boolean {
        val isLatValid = lat >= -90.0 && lat <= 90.0
        val isLonValid = lon >= -180.0 && lon <= 180.0
        return isLatValid && isLonValid
    }

    private fun addMarks(point: Point, element: Element, icon: ImageProvider) {
        val placeMark = mapObjects.addPlacemark(point)
        placeMark.setIcon(icon)
        markerDataMap[placeMark] = element
        placeMarks.add(placeMark)  // Сохраняем ссылку на placeMark

        placeMark.addTapListener { mapObject, _ ->
            Log.d("MarkerTap", "Маркер нажат: ${markerDataMap[mapObject]}")
            markerDataMap[mapObject]?.let { element ->
                sendMarker(element)
            }
            true
        }
    }

    private fun sendMarker(item: Element) {
        Log.d("Send Method", item.toString())
        val bundle = Bundle().apply {
            putParcelable(FragmentFullScreenItem.KEY_MARKER, item)
        }
//        findNavController().navigate(R.id.action_fragmentMap_to_fragmentFullScreenItem, bundle)
        findNavController().navigate(R.id.fragmentFullScreenItem, bundle)
    }

    override fun onStart() {
        super.onStart()
        mapView.onStart()
        MapKitFactory.getInstance().onStart()
        checkPermissions()
    }
    override fun onStop() {
        super.onStop()
        mapView.onStop()
        MapKitFactory.getInstance().onStop()
    }
    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
    companion object {
        private val REQUEST_PERMISSIONS: Array<String> = buildList {
            add(Manifest.permission.ACCESS_FINE_LOCATION)
            add(Manifest.permission.ACCESS_COARSE_LOCATION)
            add(Manifest.permission.POST_NOTIFICATIONS)
        }.toTypedArray()
        private const val SAVE_LATITUDE = "latitude"
        private const val SAVE_LONGITUDE = "longitude"
        private const val SAVE_ZOOM = "zoom"
    }
}