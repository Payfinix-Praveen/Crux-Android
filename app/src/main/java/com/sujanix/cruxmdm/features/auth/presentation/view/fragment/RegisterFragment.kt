package com.sujanix.cruxmdm.features.auth.presentation.view.fragment

import android.content.Context
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ArrayAdapter
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.navigation.fragment.findNavController
import com.bumptech.glide.Glide
import com.google.android.gms.auth.api.signin.GoogleSignInAccount
import com.sujanix.cruxmdm.R
import com.sujanix.cruxmdm.databinding.FragmentRegisterBinding
import com.sujanix.cruxmdm.features.auth.data.model.request.RegisterData
import com.sujanix.cruxmdm.features.auth.presentation.viewmodel.AuthViewModel
import com.sujanix.cruxmdm.features.core.utlis.Constant
import com.sujanix.cruxmdm.features.core.utlis.Resource
import com.sujanix.cruxmdm.features.core.utlis.hideKeyboard
import com.sujanix.cruxmdm.features.core.utlis.setEnable
import com.sujanix.cruxmdm.features.core.utlis.setVerifiedIcon
import com.sujanix.cruxmdm.features.core.utlis.visible
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class RegisterFragment: Fragment(R.layout.fragment_register) {

    private var _binding: FragmentRegisterBinding? = null
    private val binding get() = _binding!!
    private val viewModel: AuthViewModel by activityViewModels()
    private val TAG = "RegisterFragment"

    private lateinit var enterpriseData: Triple<String, String, String>


    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        _binding = FragmentRegisterBinding.inflate(layoutInflater, container, false)

        observeGoogleAccountData()
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)


//        getLocationData()
//        getDeviceDetails()
        viewModel.getEnterpriseList()

        binding.apply {

            tvAlreadyRgstd.setOnClickListener {
                activity?.onBackPressed()
            }
            btnSendOtp.setOnClickListener {
                registerUser()
            }
        }

        observeEnterpriseList()
        observeRegisterUser()
    }

    private fun observeGoogleAccountData() {
        viewModel.googleAccountDetails.observe(viewLifecycleOwner) { data ->
            Log.d(TAG, "observeGoogleAccountData: ${data?.email}, ${data?.account}, ${data?.displayName}, ${data?.photoUrl}")

            if(data != null) {
                updateUi(data)
            }
        }
    }

    private fun updateUi(data: GoogleSignInAccount) {
        binding.apply {
            etEmailId.setText(data.email)
            etName.setText(data.displayName)
            if (data.photoUrl != null) {
                Glide.with(requireContext())
                    .load(data.photoUrl)
                    .into(ivMrcImage)
            }

            setVerifiedIcon(requireContext(), etEmailId)
        }
    }

    private fun observeRegisterUser() {

        viewModel.registerResponse.observe(viewLifecycleOwner) { resource ->

            when(resource) {
                is Resource.Loading -> {
                    Log.d(TAG, "ObserveRegisterUser: Loading")
                }
                is Resource.Success -> {
                    Log.d(TAG, "ObserveRegisterUser: Success")
                    binding.apply {
                        progressBar3.visible(false)
                    }
                    if(resource.value.Status) {

                        val sharedPreferences = activity?.getSharedPreferences(
                            Constant.PREFERENCE_NAME,
                            Context.MODE_PRIVATE
                        )
                        val editor = sharedPreferences?.edit()
                        editor?.putString(Constant.DEVICE_USER_ID, resource.value.device_user_id)
                        editor?.apply()

                        Toast.makeText(
                            requireContext(),
                            "Registered Successfully",
                            Toast.LENGTH_SHORT
                        ).show()
                        findNavController().popBackStack()
                    } else {
                        Toast.makeText(
                            requireContext(),
                            resource.value.Message,
                            Toast.LENGTH_SHORT
                        ).show()
                        binding.apply {
                            progressBar3.visible(false)
                            linearLayout.setEnable(true)
                            btnSendOtp.text = getString(R.string.register)
                        }
                    }
                }
                is Resource.Failure -> {
                    Toast.makeText(
                        requireContext(),
                        "Registration Failed",
                        Toast.LENGTH_SHORT
                    ).show()
                    binding?.apply {
                        progressBar3.visible(false)
                        linearLayout.setEnable(true)
                        btnSendOtp.text = getString(R.string.register)
                    }
                }
            }
        }
    }

    private fun observeEnterpriseList() {
        viewModel.enterpriseListResponse.observe(viewLifecycleOwner) { resource ->
            when(resource){
                is Resource.Loading -> {

                }
                is Resource.Success -> {
                    Log.d(TAG, "observeEnterpriseList: ${resource.value}")
                    if(resource.value.status) {
                        resource.value.data.let { list ->
                            val enterpriseList = list.map { Triple(it.enterprise_id, it.name, it.org_id) }
                            binding?.enterpriseDropDown?.apply {
                                val adapter = ArrayAdapter(requireContext(), R.layout.list_item, enterpriseList.map { it.second })

                                setAdapter(adapter)
                                setOnItemClickListener { _, _, position, _ ->
                                    enterpriseData = Triple(enterpriseList[position].first!!, enterpriseList[position].second!!, enterpriseList[position].third!!)
                                    Log.d(TAG, "Selected Enterprise: ${enterpriseData.first}")
                                }
                            }
                        }
                    }
                }
                is Resource.Failure -> {

                }
            }
        }
    }

    private fun registerUser() {
        binding.apply {
            val name = etName.text.toString().trim()
            val email = etEmailId.text.toString().trim()
            val phone = etPhone.text.toString().trim()
            val enterpriseId = enterpriseData.first
            val orgId = enterpriseData.third
            val aadhar = etAadhar.text.toString().trim()

            when {
                name.isEmpty() -> etName.error = "Enter name"
                email.isEmpty() -> etEmailId.error = "Enter Email"
                phone.isEmpty() -> etPhone.error = "Enter Phone No."
                aadhar.isEmpty() -> etAadhar.error = "Enter Aadhar No."

                else -> {
                    linearLayout.setEnable(false)
                    btnSendOtp.text = ""
                    progressBar3.visible(true)
                    view?.hideKeyboard()
                }
            }
            viewModel.registerUser(
                RegisterData(
                    name = name,
                    email = email,
                    mobile_number = phone,
                    org_id = orgId,
                    enterprise_id = enterpriseId,
                    aadhar_number = aadhar
                )
            )
        }
    }



    override fun onDestroyView() {
        super.onDestroyView()

        _binding = null
    }
}