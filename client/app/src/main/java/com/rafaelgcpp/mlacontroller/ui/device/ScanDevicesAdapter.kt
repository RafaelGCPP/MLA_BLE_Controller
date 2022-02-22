package com.rafaelgcpp.mlacontroller.ui.device

import android.annotation.SuppressLint
import android.content.Context
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.RecyclerView
import com.juul.kable.Advertisement
import com.rafaelgcpp.mlacontroller.R
import com.rafaelgcpp.mlacontroller.databinding.FragmentDeviceBinding
import java.lang.Long.parseLong

class ScanDevicesAdapter(
    private val listener: (Advertisement) -> Unit
) : RecyclerView.Adapter<ScanItemViewBinder>() {

    private val advertisements = mutableListOf<Advertisement>()

    @SuppressLint("NotifyDataSetChanged") // when the list is clear, there is nothing specific to notify.
    fun update(newList: List<Advertisement>) {
        if (newList.isEmpty()) {
            advertisements.clear()
            notifyDataSetChanged()
        } else {
            val result = DiffUtil.calculateDiff(DiffCallback(advertisements, newList), false)
            advertisements.clear()
            advertisements.addAll(newList)
            result.dispatchUpdatesTo(this)
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ScanItemViewBinder {

        val binding =
            FragmentDeviceBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return ScanItemViewBinder(binding)
    }

    override fun onBindViewHolder(binder: ScanItemViewBinder, position: Int) =
        binder.bind(advertisements[position], listener)

    override fun getItemCount(): Int = advertisements.size

    override fun getItemId(position: Int): Long = advertisements[position].id
}

class ScanItemViewBinder(
    private val binding: FragmentDeviceBinding
) : RecyclerView.ViewHolder(binding.root) {

    private val context: Context = binding.root.context

    fun bind(
        advertisement: Advertisement,
        listener: (Advertisement) -> Unit
    ) = with(binding) {
        deviceName.text =
            advertisement.name ?: context.getString(android.R.string.unknownName)
        macAddress.text = advertisement.address
        rssi.text = context
            .getString(R.string.rssi, advertisement.rssi.toString())

        root.setOnClickListener { listener.invoke(advertisement) }
    }
}

private val Advertisement.id: Long
    get() {
        require(address.isNotBlank())
        return parseLong(address.replace(":", ""), 16)
    }