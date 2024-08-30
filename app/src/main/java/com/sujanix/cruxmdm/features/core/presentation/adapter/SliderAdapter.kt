package com.sujanix.cruxmdm.features.core.presentation.adapter

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.viewpager.widget.PagerAdapter
import com.sujanix.cruxmdm.features.core.data.model.SliderData
import com.sujanix.cruxmdm.databinding.SliderItemBinding
import java.util.Objects

class SliderAdapter(
    private val list: List<SliderData>
): PagerAdapter() {

    override fun getCount(): Int {
        return list.size
    }

    override fun isViewFromObject(view: View, `object`: Any): Boolean {
        return view === `object`
    }

    override fun instantiateItem(container: ViewGroup, position: Int): Any {
//        return super.instantiateItem(container, position)

        val  binding = SliderItemBinding.inflate(LayoutInflater.from(container.context), container, false)
        binding.apply {
            val currentItem = list[position]
            ivDescImg.setImageResource(currentItem.imageView)
            tvDescription.text = currentItem.text

        }

        Objects.requireNonNull(container).addView(binding.root)

        return binding.root
    }

    override fun destroyItem(container: ViewGroup, position: Int, `object`: Any) {
        container.removeView(container)
    }
}