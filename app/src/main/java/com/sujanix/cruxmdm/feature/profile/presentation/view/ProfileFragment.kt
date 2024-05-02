package com.sujanix.cruxmdm.feature.profile.presentation.view

import android.app.Dialog
import android.content.Context
import android.content.SharedPreferences
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.os.Bundle
import android.view.Gravity
import android.view.View
import android.view.ViewGroup
import android.view.Window
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatDelegate
import androidx.appcompat.widget.Toolbar
import androidx.core.view.MenuItemCompat
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.navigation.fragment.findNavController
import com.sujanix.cruxmdm.R
import com.sujanix.cruxmdm.databinding.ChangeThemeLayoutBinding
import com.sujanix.cruxmdm.databinding.FragmentProfileBinding
import com.sujanix.cruxmdm.feature.common.presentation.viewmodel.CruxViewModel
import com.sujanix.cruxmdm.feature.common.utlis.Constant.APP_THEME
import com.sujanix.cruxmdm.feature.common.utlis.Constant.DARK
import com.sujanix.cruxmdm.feature.common.utlis.Constant.LIGHT
import com.sujanix.cruxmdm.feature.common.utlis.Constant.PREFERENCE_NAME
import com.sujanix.cruxmdm.feature.common.utlis.Constant.SYSTEM_DEFAULT
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

@AndroidEntryPoint
class ProfileFragment : Fragment(R.layout.fragment_profile) {

    private var _binding: FragmentProfileBinding? = null
    private val binding get() = _binding!!
    private val viewModel: CruxViewModel by viewModels()
    private lateinit var preferences: SharedPreferences
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        _binding = FragmentProfileBinding.bind(view)
        CoroutineScope(Dispatchers.Main).launch {
            setUpNavViewHeader()
        }
        handleNavigationItemClick()
    }

    private fun handleNavigationItemClick() {
        try {
            val colorTheme = MenuItemCompat.getActionView(binding.navView.menu.findItem(R.id.colorScheme)) as TextView
            colorTheme.text = getString(R.string.system_default)
        } catch (e: Exception){
            e.printStackTrace()
        }

        binding.navView.setNavigationItemSelectedListener { menuItem ->
            when (menuItem.itemId) {
                R.id.appCatalog -> {
                    findNavController().navigate(R.id.action_profileFragment_to_appStoreFragment)
                }

                R.id.content -> {
                    findNavController().navigate(R.id.action_profileFragment_to_contentFragment)
                }

                R.id.device_details -> {
                    findNavController().navigate(R.id.action_profileFragment_to_deviceDetailFragment)
                }

                R.id.notification -> { findNavController().navigate(R.id.action_profileFragment_to_notificationActivityFragment) }
                R.id.colorScheme -> { showThemeDialog(requireContext()) }
                R.id.helpCenter -> {}
                R.id.privacyPolicy -> {}
                R.id.termOfUse -> {}
            }
            menuItem.isChecked = false
            true
        }
    }

    private fun showThemeDialog(context: Context){
        preferences = context.getSharedPreferences(PREFERENCE_NAME, Context.MODE_PRIVATE)
        val themeType = preferences.getString(APP_THEME, SYSTEM_DEFAULT)
        val themeDialog = Dialog(context)
        val dialogBinding = ChangeThemeLayoutBinding.inflate(layoutInflater)
        themeDialog.requestWindowFeature(Window.FEATURE_NO_TITLE)
        themeDialog.setCancelable(true)
        val window = themeDialog.window
        window!!.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))
        window.attributes.windowAnimations = R.style.DialogAnimation
        window.setGravity(Gravity.CENTER)
        themeDialog.setContentView(dialogBinding.root)
        window.setLayout(900, ViewGroup.LayoutParams.WRAP_CONTENT);

        dialogBinding.apply {
            tvCancel.setOnClickListener { themeDialog.dismiss() }

            when(themeType){
                SYSTEM_DEFAULT -> btnSystem.isChecked = true
                DARK -> btnDark.isChecked = true
                LIGHT -> btnLight.isChecked = true
            }

            radioGroup.setOnCheckedChangeListener { _, checkId ->
                when(checkId) {
                    R.id.btnSystem -> {
                        setThemeMode(AppCompatDelegate.MODE_NIGHT_FOLLOW_SYSTEM, SYSTEM_DEFAULT)
                        themeDialog.dismiss()
                    }
                    R.id.btnDark -> {
                        setThemeMode(AppCompatDelegate.MODE_NIGHT_YES, DARK)
                        themeDialog.dismiss()
                    }
                    R.id.btnLight -> {
                        setThemeMode(AppCompatDelegate.MODE_NIGHT_NO, LIGHT)
                        themeDialog.dismiss()
                    }
                }
            }
        }
        themeDialog.show()
    }

    private suspend fun setUpNavViewHeader() {
        try {
            binding.apply {
                viewModel.getDeviceId().collect { deviceId ->
                    val headerView = navView.getHeaderView(0)
                    val navUsername = headerView.findViewById<Toolbar>(R.id.topAppBar)
                    navUsername.subtitle = "Device ID: $deviceId"

                    viewModel.getUserData().collect { user ->
                        navUsername.title = "user?.name"
                    }
                }
            }
        } catch (e: Exception) {
            e.printStackTrace()
            Toast.makeText(requireContext(), "unable to load user details...", Toast.LENGTH_SHORT).show()
        }
    }

    private fun setThemeMode(theme: Int, themeType: String){
        val editor = preferences.edit()
        AppCompatDelegate.setDefaultNightMode(theme)
        editor.putString(APP_THEME, themeType)
        editor.apply()
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}