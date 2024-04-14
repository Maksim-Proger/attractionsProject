package project.moms.attractions.presentation.partWithCamera

import android.Manifest
import android.annotation.SuppressLint
import android.content.ContentValues
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Bundle
import android.provider.MediaStore
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.camera.core.CameraSelector
import androidx.camera.core.ImageCapture
import androidx.camera.core.ImageCaptureException
import androidx.camera.core.Preview
import androidx.camera.lifecycle.ProcessCameraProvider
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.fragment.findNavController
import com.bumptech.glide.Glide
import project.moms.attractions.R
import project.moms.attractions.App
import project.moms.attractions.databinding.FragmentTakePhotoBinding
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Locale
import java.util.concurrent.Executor

private const val FILE_FORMAT = "yyyy-MM-dd-HH-mm-ss"

class FragmentTakePhoto : Fragment() {

    private var _binding : FragmentTakePhotoBinding? = null
    private val binding : FragmentTakePhotoBinding
        get() { return _binding!! }

    private val viewModel: MainViewModel by viewModels {
        object : ViewModelProvider.Factory {
            override fun <T : ViewModel> create(modelClass: Class<T>): T {
                val galleryDao = (requireContext().applicationContext as App).db.galleryDao()
                return MainViewModel(galleryDao) as T
            }
        }
    }

    private lateinit var executor: Executor
    private val name = SimpleDateFormat(FILE_FORMAT, Locale.US)
        .format(System.currentTimeMillis())

    private var imageCapture: ImageCapture? = null

    private val launcher = registerForActivityResult(ActivityResultContracts.RequestMultiplePermissions()) { map ->
            if (map.values.all { it }) {
                startCamera()
            } else {
                Toast.makeText(
                    requireContext(), "Разрешение CAMERA предоставлено.",
                    Toast.LENGTH_SHORT
                ).show()
            }
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        _binding = FragmentTakePhotoBinding.inflate(inflater)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        executor = ContextCompat.getMainExecutor(requireContext())

        binding.takePhotoButton.setOnClickListener { takePhoto() }

        binding.imagePreview.setOnClickListener {
            findNavController().navigate(R.id.action_fragmentTakePhoto_to_fragmentGallery)
        }

        binding.mapButton.setOnClickListener {
            findNavController().navigate(R.id.action_fragmentTakePhoto_to_fragmentMap)
        }

        checkPermissions()
    }

    private fun checkPermissions() {
        val isAllGranted = REQUEST_PERMISSIONS.all { permission ->
            ContextCompat.checkSelfPermission(requireContext(), permission) ==
                    PackageManager.PERMISSION_GRANTED
        }

        if (isAllGranted) {
            startCamera()
            if (!viewModel.permissionToastShown) {
                Toast.makeText(requireContext(), "permission is Granted", Toast.LENGTH_SHORT).show()
                viewModel.permissionToastShown = true
            }
        } else {
            launcher.launch(REQUEST_PERMISSIONS)
        }
    }

    private fun takePhoto() {
        val imageCapture = imageCapture?: return
        val contentValues = ContentValues().apply {
            put(MediaStore.MediaColumns.DISPLAY_NAME, name)
            put(MediaStore.MediaColumns.MIME_TYPE, "image/jpeg")
        }
        val outputOptions = ImageCapture.OutputFileOptions.Builder(
            requireContext().contentResolver,
            MediaStore.Images.Media.EXTERNAL_CONTENT_URI,
            contentValues
        ).build()
        imageCapture.takePicture(
            outputOptions,
            executor,
            object : ImageCapture.OnImageSavedCallback {
                override fun onImageSaved(outputFileResults: ImageCapture.OutputFileResults) {

                    val savedUri =
                        outputFileResults
                            .savedUri?.toString() ?: return // Преобразуем URI в строку и проверяем на null

                    Toast.makeText(
                        requireContext(),
                        "Photo saved on: ${outputFileResults.savedUri}",
                        Toast.LENGTH_SHORT
                    ).show()
                    Glide.with(requireContext())
                        .load(outputFileResults.savedUri)
                        .circleCrop()
                        .into(binding.imagePreview)

                    // Получаем данные фотографии из URI для добавления в базу
                    val inputStream = requireContext().contentResolver.openInputStream(
                        Uri.parse(
                            savedUri
                        )
                    )
                    val photoByteArray = inputStream?.readBytes()

                    // Вызываем onSave() во ViewModel, передавая байтовый массив фотографии
                    photoByteArray?.let { byteArray ->
                        viewModel.onSave(byteArray, datePhoto())
                    }
                }

                override fun onError(exception: ImageCaptureException) {
                    Toast.makeText(
                        requireContext(),
                        "Photo failed: ${exception.message}", Toast.LENGTH_SHORT
                    ).show()
                    exception.printStackTrace()
                }
            }
        )
    }

    @SuppressLint("SimpleDateFormat")
    private fun datePhoto() : String{
        val dateFormat = SimpleDateFormat("dd-MM-yyyy")
        val calendar = Calendar.getInstance()
        val date = calendar.time
        return dateFormat.format(date)
    }

    private fun startCamera() {
        val cameraProviderFuture = ProcessCameraProvider.getInstance(requireContext())
        cameraProviderFuture.addListener({
            val cameraProvider = cameraProviderFuture.get()
            val preview = Preview.Builder().build()
            preview.setSurfaceProvider(binding.viewFinder.surfaceProvider)

            imageCapture = ImageCapture.Builder().build()
            cameraProvider.unbindAll()
            cameraProvider.bindToLifecycle(
                viewLifecycleOwner,
                CameraSelector.DEFAULT_BACK_CAMERA,
                preview,
                imageCapture
            )
        }, executor)
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
    companion object {
        private val REQUEST_PERMISSIONS: Array<String> = buildList {
            add(Manifest.permission.CAMERA)
        }.toTypedArray()
    }
}