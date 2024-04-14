package project.moms.attractions.presentation.adapter

import androidx.recyclerview.widget.DiffUtil
import project.moms.attractions.model.Photo

class DiffCallback : DiffUtil.ItemCallback<Photo>() {
    override fun areItemsTheSame(oldItem: Photo, newItem: Photo): Boolean =
        oldItem.id == newItem.id

    override fun areContentsTheSame(oldItem: Photo, newItem: Photo): Boolean = oldItem == newItem

}