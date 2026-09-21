package com.example.campusrelay.ui.settings

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.appcompat.app.AppCompatDelegate
import androidx.core.os.LocaleListCompat
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import androidx.navigation.fragment.findNavController
import com.example.campusrelay.CampusRelayApp
import com.example.campusrelay.R
import com.example.campusrelay.common.Constants
import com.example.campusrelay.databinding.FragmentSettingsBinding
import com.example.campusrelay.ui.common.ViewModelFactory
import kotlinx.coroutines.launch

/** Settings & Preferences screen (REQ-SET-1, REQ-SET-2, REQ-LANG-1, REQ-BIO-2). */
class SettingsFragment : Fragment() {

    private var binding: FragmentSettingsBinding? = null
    private var hasPopulatedFields = false

    private val viewModel: SettingsViewModel by viewModels {
        val app = requireActivity().application as CampusRelayApp
        ViewModelFactory {
            SettingsViewModel(app.serviceLocator.settingsRepository, app.serviceLocator.authRepository)
        }
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        binding = FragmentSettingsBinding.inflate(inflater, container, false)
        return binding!!.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        val b = binding ?: return

        b.buttonSave.setOnClickListener { save(b) }
        b.buttonSignOut.setOnClickListener {
            viewModel.signOut {
                findNavController().navigate(R.id.action_settings_to_login)
            }
        }

        viewLifecycleOwner.lifecycleScope.launch {
            viewLifecycleOwner.repeatOnLifecycle(Lifecycle.State.STARTED) {
                viewModel.uiState.collect { state ->
                    if (!hasPopulatedFields) {
                        populateFields(b, state)
                        hasPopulatedFields = true
                    }
                    b.textProfileName.text = state.fullName
                    b.textProfileEmail.text = state.email
                }
            }
        }
    }

    private fun populateFields(b: FragmentSettingsBinding, state: SettingsUiState) {
        b.editBio.setText(state.bio)
        b.editLocation.setText(state.campusLocation)
        b.switchBiometricLock.isChecked = state.biometricLock
        b.switchNotifyDelivery.isChecked = state.notifyDelivery
        b.switchNotifyChat.isChecked = state.notifyChat
        b.switchDarkMode.isChecked = state.darkMode
        when (state.languageCode) {
            Constants.LANGUAGE_ZULU -> b.radioLangZu.isChecked = true
            Constants.LANGUAGE_AFRIKAANS -> b.radioLangAf.isChecked = true
            else -> b.radioLangEn.isChecked = true
        }
    }

    private fun save(b: FragmentSettingsBinding) {
        val languageCode = when (b.radioGroupLanguage.checkedRadioButtonId) {
            R.id.radio_lang_zu -> Constants.LANGUAGE_ZULU
            R.id.radio_lang_af -> Constants.LANGUAGE_AFRIKAANS
            else -> Constants.LANGUAGE_ENGLISH
        }

        viewModel.save(
            bio = b.editBio.text?.toString().orEmpty(),
            campusLocation = b.editLocation.text?.toString().orEmpty(),
            languageCode = languageCode,
            darkMode = b.switchDarkMode.isChecked,
            biometricLock = b.switchBiometricLock.isChecked,
            notifyDelivery = b.switchNotifyDelivery.isChecked,
            notifyChat = b.switchNotifyChat.isChecked
        )

        AppCompatDelegate.setApplicationLocales(LocaleListCompat.forLanguageTags(languageCode))
        Toast.makeText(requireContext(), R.string.settings_saved, Toast.LENGTH_SHORT).show()
    }

    override fun onDestroyView() {
        super.onDestroyView()
        binding = null
    }
}
