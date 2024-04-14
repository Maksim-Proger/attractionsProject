package project.moms.attractions.presentation.adapter

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import project.moms.attractions.R
import project.moms.attractions.model.Photo

class AdapterForGallery : ListAdapter<Photo, AdapterForGallery.PhotoViewHolder>(DiffCallback()) {
    override fun onCreateViewHolder(
        parent: ViewGroup,
        viewType: Int
    ): AdapterForGallery.PhotoViewHolder {
        val itemView = LayoutInflater.from(parent.context).inflate(R.layout.item_for_recycler_view, parent, false)
        return PhotoViewHolder(itemView)
    }

    override fun onBindViewHolder(holder: PhotoViewHolder, position: Int) {
        val currentItem = getItem(position)
        holder.bind(currentItem)
    }

    inner class PhotoViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        private val photoField: ImageView = itemView.findViewById(R.id.fieldImageView)
        private val dateField: TextView = itemView.findViewById(R.id.dateTextView)
        fun bind(photo: Photo) {
            photo?.let {
                Glide
                    .with(photoField.context)
                    .load(it.image)
                    .into(photoField)
            }
            dateField.text = photo.date
        }
    }
}

