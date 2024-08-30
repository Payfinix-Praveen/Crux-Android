package com.sujanix.cruxmdm.features.content_management.presentation.view

import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.navArgs
import androidx.recyclerview.widget.LinearLayoutManager
import com.sujanix.cruxmdm.R
import com.sujanix.cruxmdm.databinding.FragmentFileBinding
import com.sujanix.cruxmdm.features.content_management.data.model.response.shared_file.Children
import com.sujanix.cruxmdm.features.content_management.data.model.response.shared_file.Data
import com.sujanix.cruxmdm.features.content_management.data.repository.ContentManagementRepository
import com.sujanix.cruxmdm.features.content_management.presentation.adapter.FileAdapter
import com.sujanix.cruxmdm.features.content_management.presentation.viewmodel.ContentManagementViewmodel
import com.sujanix.cruxmdm.features.content_management.utlis.FileUtils.openFile
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.launch
import javax.inject.Inject

@AndroidEntryPoint
class FileFragment: Fragment(R.layout.fragment_file), FileAdapter.Listener {

    private val TAG = "FileFragment"
    private val args: FileFragmentArgs by navArgs()
    private var _binding: FragmentFileBinding? = null
    private val viewModel: ContentManagementViewmodel by viewModels()
    private val binding get() = _binding!!
    private lateinit var folderData: Data

    @Inject
    lateinit var repository: ContentManagementRepository

    private lateinit var fileAdapter: FileAdapter

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        _binding = FragmentFileBinding.bind(view)
        folderData = args.folderData
        Log.d(TAG, "args: ${args.folderData}")
        lifecycleScope.launch {
            viewModel.getDeviceId().collect { deviceId ->
                if (deviceId != null) {
                    val files = getFiles(folderData, deviceId)
                    fileAdapter = FileAdapter(requireContext(), this@FileFragment, repository)
//                    fileAdapter.setPath(folderData.path)
                    setUpRecyclerView()
                    Log.d(TAG, "onViewCreated: $files")
                    fileAdapter.submitList(files)
                } else {
                    Toast.makeText(requireContext(), "Device ID not found", Toast.LENGTH_SHORT)
                        .show()
                }
            }
        }

        binding.apply {
            toolBar.setNavigationOnClickListener {
                requireActivity().onBackPressed()
            }
            toolBar.title = folderData.name
        }
    }

    private fun getFiles(folderData: Data, deviceId: String): List<Children> {
        val filesNames = mutableListOf<Children>()

        if (folderData.type == "folder" && folderData.shared_with.any { it.device_id == deviceId }) {
            val children = folderData.children.map {
                it.fileKey = "${folderData.path}/${it.name}"
                it
            }
            filesNames.addAll(children)
        } else {
            folderData.children.forEach { file ->
                if (file.shared_with.any { it.device_id == deviceId }) {
                    file.fileKey = "${folderData.path}/${file.name}"
                    filesNames.add(file)
                }
            }
        }
        return filesNames
    }

    private fun setUpRecyclerView() {
        binding.apply {
            rvFiles.apply {
                adapter = fileAdapter
                layoutManager =
                    LinearLayoutManager(requireContext(), LinearLayoutManager.VERTICAL, false)
                setHasFixedSize(true)
            }
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()

        _binding = null
    }

    override fun onFileClick(data: Children) {
        lifecycleScope.launch {
            val fileInfo = repository.getFileByKey(data.fileKey)
            if(fileInfo != null){
                openFile(requireContext(), fileInfo.path)
            }
        }
    }
}