package project.moms.attractions.presentation.partWithMap

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import project.moms.attractions.R
import project.moms.attractions.databinding.FullScreenItemBinding
import project.moms.attractions.model.Element

class FragmentFullScreenItem : Fragment() {
    private var _binding : FullScreenItemBinding? = null
    private val binding : FullScreenItemBinding
        get() {return _binding!!}

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        _binding = FullScreenItemBinding.inflate(inflater)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        val marker = arguments?.getParcelable<Element>(KEY_MARKER)
        marker?.let {
            val header = it.tags["name:en"] ?: "Unknown"
            val latitude = it.lat
            val longitude = it.lon

            if (it.name == "My location") {
                binding.fieldHeader.text = it.name
            } else {
                binding.fieldHeader.text = header
            }
            binding.fieldLatitude.text = getString(R.string.latitude_string, latitude.toString())
            binding.fieldLongitude.text = getString(R.string.longitude_string, longitude.toString())
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    companion object {
        const val KEY_MARKER = "KEY_MARKER"
    }
}