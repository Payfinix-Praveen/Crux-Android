package com.sujanix.cruxmdm.features.content_management.presentation.view

import android.app.Dialog
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import com.amazonaws.auth.BasicAWSCredentials
import com.amazonaws.internal.StaticCredentialsProvider
import com.amazonaws.regions.Region
import com.amazonaws.regions.Regions
import com.amazonaws.services.s3.AmazonS3Client
import com.sujanix.cruxmdm.R
import com.sujanix.cruxmdm.databinding.FragmentContentBinding
import com.sujanix.cruxmdm.features.content_management.data.data_source.local.FileData
import com.sujanix.cruxmdm.features.core.data.model.OrganizationData
import com.sujanix.cruxmdm.features.core.utlis.Constant
import com.sujanix.cruxmdm.features.core.utlis.Resource
import com.sujanix.cruxmdm.features.content_management.data.model.response.shared_file.Children
import com.sujanix.cruxmdm.features.content_management.data.model.response.shared_file.Data
import com.sujanix.cruxmdm.features.content_management.data.repository.ContentManagementRepository
import com.sujanix.cruxmdm.features.content_management.presentation.adapter.FileAdapter
import com.sujanix.cruxmdm.features.content_management.presentation.adapter.ContentFolderAdapter
import com.sujanix.cruxmdm.features.content_management.presentation.viewmodel.ContentManagementViewmodel
import com.sujanix.cruxmdm.features.content_management.utlis.FileUtils.openFile
import com.sujanix.cruxmdm.features.core.utlis.showLoadingDialog
import com.sujanix.cruxmdm.features.device_detail.utlis.getTotalExternalMemorySize
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.io.File
import javax.inject.Inject

@AndroidEntryPoint
class ContentFragment: Fragment(R.layout.fragment_content), ContentFolderAdapter.Listener,
    FileAdapter.Listener {

    private lateinit var downloadedFiles: List<FileData>
    private lateinit var dialog: Dialog
    private var deviceId: String = ""
    private val TAG: String = "ContentFragment"
    private var _binding: FragmentContentBinding? = null
    private val binding get() = _binding!!

    private val viewModel: ContentManagementViewmodel by viewModels()
    @Inject
    lateinit var repository: ContentManagementRepository

//    private val contentFolderAdapter by lazy { ContentFolderAdapter(requireContext(), this@ContentFragment) }
    private lateinit var contentFolderAdapter : ContentFolderAdapter

    private var path: String = ""
//    private val fileAdapter by lazy { FileAdapter(requireContext(), this, repository) }
    private lateinit var fileAdapter : FileAdapter

    private val deviceDetails: Pair<Long, Long>? = getTotalExternalMemorySize()

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {

        _binding = FragmentContentBinding.inflate(inflater, container, false)

        contentFolderAdapter = ContentFolderAdapter(requireContext(), this)
        fileAdapter = FileAdapter(requireContext(), this, repository)

        Log.d(TAG, "onCreateView: called")
        binding.apply {
            lifecycleScope.launch {
                viewModel.getDeviceId().collect { device_Id ->
                    if (device_Id != null) {
                        deviceId = device_Id
                        contentFolderAdapter.setDeviceId(device_Id)
                        rvContent.apply {
                            adapter = contentFolderAdapter
                            layoutManager = LinearLayoutManager(
                                requireContext(),
                                LinearLayoutManager.HORIZONTAL,
                                false
                            )
                            setHasFixedSize(true)
                        }
                    } else {
                        Toast.makeText(requireContext(), "Device ID not found", Toast.LENGTH_SHORT)
                            .show()
                    }
                }
            }
            rvRecentFiles.apply {
                adapter = fileAdapter
                layoutManager = object : LinearLayoutManager(requireContext()) { override fun canScrollVertically() = false }
                setHasFixedSize(true)
            }

            if(deviceDetails != null) {
                pbSpaceAvailable.max = deviceDetails.first.toInt()
                val usedSpace = deviceDetails.first - deviceDetails.second
                pbSpaceAvailable.progress = usedSpace.toInt()

                tvSpaceAvailable.text =
                    getString(R.string.used_storage, usedSpace.toString(), deviceDetails.first.toString())
            }
        }
        lifecycleScope.launch {
            downloadedFiles = viewModel.getAllDownloadedFiles()
        }

        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

//        _binding = FragmentContentBinding.bind(view)
        Log.d(TAG, "onViewCreated: called")
        lifecycleScope.launch{
            viewModel.getUserData().collect { userData ->
                if(userData != null) {
                    viewModel.getSharedJsonFile(
                        OrganizationData(
                            enterprise_id = userData.enterprise_id,
                            organisation = userData.org_name
                        )
                    )
                }
            }
        }
        dialog = showLoadingDialog(requireContext())
        observeSharedFile()
    }

    private fun observeSharedFile() {
        CoroutineScope(Dispatchers.IO).launch {
            viewModel.sharedJsonFile.collect { resource ->
                Log.d(TAG, "observeSharedFile: called")
                when (resource) {
                    is Resource.Success -> {
                        path = resource.value.path
                        val folders = findSharedFoldersAndFiles(resource.value.data, deviceId)
//                        fileAdapter.setPath(path)
                        contentFolderAdapter.submitList(folders.first.take(5))
                        fileAdapter.submitList(folders.second.take(5))
                        dialog.dismiss()
                    }

                    is Resource.Failure -> {
                        Log.d(TAG, "observeSharedFileErr: ${resource.errorBody}")
                        dialog.dismiss()
                    }

                    Resource.Loading -> {
                        Log.d(TAG, "observeSharedFileLoading: calledCo")
                    }
                }
            }
        }
    }

    private fun findSharedFoldersAndFiles(response: List<Data>, deviceId: String): Pair<List<Data>, List<Children>> {
        val folders = mutableListOf<Data>()
        val files = mutableListOf<Children>()

        response.forEach { folder ->
            if (folder.type == "folder" && folder.shared_with.any { it.device_id == deviceId }) {
                folders.add(folder)
                val children = folder.children.map {
                    Log.d(TAG, "findSharedFoldersAndFiles: $path${folder.name}/${it.name}")

                    it.fileKey = "$path${folder.name}/${it.name}"
                    it
                }
                files.addAll(children)
                return@forEach
            }
            folder.children.forEach { file ->
                if (file.shared_with.any { it.device_id == deviceId }) {
                    file.fileKey = "$path${folder.name}/${file.name}"
                    files.add(file)
                }
            }
        }
        val filesWithDownloadStatus = checkFileDownload(files)
        return Pair(folders, filesWithDownloadStatus)
    }

    private fun checkFileDownload(filesNames: MutableList<Children>): List<Children> {
        return filesNames.map { file ->
            val downloadedFile = downloadedFiles.find { it.key == file.fileKey }
            if (downloadedFile != null) {
                file.isDownloaded = true
            }
            file
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()

        _binding = null
    }

    override fun onFolderClick(data: Data) {

        val folderData = data.copy(
            path = if(path.endsWith("/")) "$path${data.name}" else "$path/${data.name}"
        )
        findNavController().navigate(ContentFragmentDirections.actionContentFragmentToFileFragment(folderData))
    }

    override fun onFileClick(data: Children) {
        lifecycleScope.launch {
            val fileInfo = repository.getFileByKey(data.fileKey)
            if(fileInfo != null){
                openFile(requireContext(), fileInfo.path)
            }
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        Log.d(TAG, "onDestroy: called")
    }
}