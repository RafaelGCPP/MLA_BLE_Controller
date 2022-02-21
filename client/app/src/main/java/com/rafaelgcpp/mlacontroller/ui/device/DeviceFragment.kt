package com.rafaelgcpp.mlacontroller.ui.device

import android.os.Bundle
import android.view.*
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.lifecycle.asLiveData
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.snackbar.Snackbar
import com.juul.kable.Advertisement
import com.rafaelgcpp.mlacontroller.R
import com.rafaelgcpp.mlacontroller.util.observe
import com.rafaelgcpp.mlacontroller.util.showAlert
import com.rafaelgcpp.mlacontroller.viewmodel.MainViewModel
import com.rafaelgcpp.mlacontroller.viewmodel.ScanStatus
import timber.log.Timber

/**
 * A fragment representing a list of Items.
 */
class DeviceFragment : Fragment() {

    companion object {
        fun newInstance() = DeviceFragment()
    }

    private lateinit var scanAdapter: ScanDevicesAdapter
    private val viewModel: MainViewModel by activityViewModels()
    private var snackbar: Snackbar? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)


    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {

        val listener = { advertisement: Advertisement ->
            viewModel.stopScan()
            Timber.i(advertisement.toString())
            //val intent = SensorActivityIntent(
            //    context = this@ScanActivity,
            //    macAddress = advertisement.address
            //)
            //startActivity(intent)
        }
        scanAdapter = ScanDevicesAdapter(listener).apply {
            setHasStableIds(true)
        }
        observe(viewModel.advertisements.asLiveData()) {
            scanAdapter.update(it)
        }

        observe(viewModel.scanStatus.asLiveData()) { status ->
            Timber.d("Scan status: $status")

            when (status) {
                ScanStatus.Started -> showSnackbar("Scanning")
                ScanStatus.Stopped -> dismissSnackbar()
                is ScanStatus.Failed -> {
                    dismissSnackbar()
                    context?.showAlert("Scan failed!\n${status.message}")
                }
            }
        }

        val view = inflater.inflate(R.layout.fragment_device_list, container, false)
        setHasOptionsMenu(true)

        // Set the adapter
        if (view is RecyclerView) {
            with(view) {
                layoutManager = LinearLayoutManager(context)
                adapter = scanAdapter
            }

        }
        return view
    }


    override fun onCreateOptionsMenu(menu: Menu, inflater: MenuInflater) {
        inflater.inflate(R.menu.scan_menu, menu)
        super.onCreateOptionsMenu(menu, inflater)
    }


    override fun onOptionsItemSelected(
        item: MenuItem,
    ): Boolean {
        when (item.itemId) {
            R.id.refresh -> viewModel.startScan()
            R.id.clear -> {
                viewModel.stopScan()
                scanAdapter.update(emptyList())
            }
            else -> return super.onOptionsItemSelected(item)
        }
        return true
    }


    private data class SnackbarAction(
        val text: CharSequence,
        val action: View.OnClickListener,
    )

    private fun showSnackbar(
        text: CharSequence,
        action: SnackbarAction? = null,
    ) {
        snackbar = Snackbar
            .make(requireView().findViewById(R.id.scan_list), text, Snackbar.LENGTH_INDEFINITE)
            .apply {
                if (action != null) setAction(action.text, action.action)
                show()
            }
    }

    private fun dismissSnackbar() {
        snackbar?.dismiss()
        snackbar = null
    }


}