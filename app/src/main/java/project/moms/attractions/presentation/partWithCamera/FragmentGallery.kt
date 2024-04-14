package project.moms.attractions.presentation.partWithCamera

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.lifecycleScope
import kotlinx.coroutines.launch
import project.moms.attractions.App
import project.moms.attractions.databinding.FragmentGalleryBinding
import project.moms.attractions.presentation.adapter.AdapterForGallery

class FragmentGallery : Fragment() {

    private var _binding : FragmentGalleryBinding? = null
    private val binding : FragmentGalleryBinding
        get() { return _binding!! }

    private lateinit var adapter: AdapterForGallery

    private val viewModel: MainViewModel by viewModels {
        object : ViewModelProvider.Factory {
            override fun <T : ViewModel> create(modelClass: Class<T>): T {
                val galleryDao = (requireContext().applicationContext as App).db.galleryDao()
                return MainViewModel(galleryDao) as T
            }
        }
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        _binding = FragmentGalleryBinding.inflate(inflater)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        adapter = AdapterForGallery()
        binding.recyclerView.adapter = adapter

        lifecycleScope.launch {
            viewModel.allPhotos.collect {photos ->
                adapter.submitList(photos)
            }
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}