package com.sujanix.cruxmdm.features.app_catalog.presentation.view.fragment

import android.app.Dialog
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.os.Bundle
import android.util.Log
import android.view.Gravity
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.WindowManager
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import com.sujanix.cruxmdm.R
import com.sujanix.cruxmdm.databinding.DialogRequestAppBinding
import com.sujanix.cruxmdm.databinding.FragmentRequestedAppBinding
import com.sujanix.cruxmdm.features.app_catalog.presentation.adapter.AppCatalogAdapter
import com.sujanix.cruxmdm.features.app_catalog.presentation.adapter.RequestedAppAdapter
import com.sujanix.cruxmdm.features.app_catalog.presentation.viewmodel.AppCatalogViewmodel
import com.sujanix.cruxmdm.features.app_catalog.utlis.RequestAppDialog
import com.sujanix.cruxmdm.features.app_catalog.utlis.Utils
import com.sujanix.cruxmdm.features.core.utlis.Constant.APP_CATEGORY_ALL_APPS
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.launch

@AndroidEntryPoint
class RequestedAppFragment: Fragment(R.layout.fragment_requested_app) {

    private val TAG = "RequestedAppFragment"
    private var _binding: FragmentRequestedAppBinding? = null
    private val binding get() = _binding!!
    private val viewModel: AppCatalogViewmodel by viewModels()

    private val appAdapter: RequestedAppAdapter by lazy {
        RequestedAppAdapter(
            requireContext()
        )
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        _binding = FragmentRequestedAppBinding.bind(view)

        binding.apply {
            rvAppCatalog.apply {
                adapter = appAdapter
                layoutManager = LinearLayoutManager(requireContext())
                setHasFixedSize(true)
            }

            btnRequestApp.setOnClickListener {
                showRequestAppDialog()
            }
        }

        viewLifecycleOwner.lifecycleScope.launch {
            viewModel.requestedAppList.collect {
                appAdapter.submitList(it)
            }
        }
    }

    private fun showRequestAppDialog() {
        // Show the dialog
        val dialog = Dialog(requireContext())
        val requestAppDialogBinding = DialogRequestAppBinding.inflate(layoutInflater)
        dialog.window?.apply {
            setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))
            attributes.windowAnimations = R.style.DialogAnimation
            setGravity(Gravity.CENTER)
        }

        dialog.setContentView(requestAppDialogBinding.root)
        dialog.setCancelable(false)

        requestAppDialogBinding.apply {
            btnRequest.setOnClickListener {
                val url = etPlayStoreUrl.text.toString()
                // Process the URL (e.g., extract package name, initiate download)
                val packageName = Utils.getPackageNameFromPlayStoreUrl(url)
                //TODO: Hit Request App API with package name and save response in DB, if the app exists else show error message
                Log.d(TAG, "showRequestAppDialog: $packageName")
                dialog.dismiss()
            }
        }

        dialog.show()
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

}