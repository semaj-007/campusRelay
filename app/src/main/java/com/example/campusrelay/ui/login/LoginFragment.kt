package com.example.campusrelay.ui.login

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ArrayAdapter
import android.widget.Toast
import androidx.appcompat.app.AppCompatDelegate
import androidx.biometric.BiometricManager
import androidx.biometric.BiometricPrompt
import androidx.core.content.ContextCompat
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
import com.example.campusrelay.databinding.FragmentLoginBinding
import com.example.campusrelay.ui.common.ViewModelFactory
import kotlinx.coroutines.launch

/** Login screen (REQ-AUTH-1, REQ-AUTH-2, REQ-BIO-1, login screen spec from Part 1). */
class LoginFragment : Fragment() {

    private var binding: FragmentLoginBinding? = null

    private val viewModel: LoginViewModel by viewModels {
        val app = requireActivity().application as CampusRelayApp
        ViewModelFactory { LoginViewModel(app.serviceLocator.authRepository) }
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        binding = FragmentLoginBinding.inflate(inflater, container, false)
        return binding!!.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        val b = binding ?: return

        setUpLanguageDropdown(b)

        b.buttonSsoGoogle.setOnClickListener { viewModel.signIn("Google") }
        b.buttonSsoMicrosoft.setOnClickListener { viewModel.signIn("Microsoft") }
        b.buttonBiometric.setOnClickListener { launchBiometricPrompt() }

        viewLifecycleOwner.lifecycleScope.launch {
            viewLifecycleOwner.repeatOnLifecycle(Lifecycle.State.STARTED) {
                launch {
                    viewModel.isSignedIn.collect { signedIn ->
                        if (signedIn) navigateToHome()
                    }
                }
                launch {
                    viewModel.uiState.collect { state -> render(state, b) }
                }
            }
        }
    }

    private fun render(state: LoginUiState, b: FragmentLoginBinding) {
        when (state) {
            is LoginUiState.Loading -> {
                b.buttonSsoGoogle.isEnabled = false
                b.buttonSsoMicrosoft.isEnabled = false
            }
            is LoginUiState.Success -> {
                b.textSignedInAs.visibility = View.VISIBLE
                b.textSignedInAs.text = getString(R.string.login_signed_in_as, state.fullName)
                // isSignedIn (above) drives the actual navigation once the session is persisted.
            }
            is LoginUiState.Error -> {
                b.buttonSsoGoogle.isEnabled = true
                b.buttonSsoMicrosoft.isEnabled = true
                Toast.makeText(requireContext(), state.message, Toast.LENGTH_SHORT).show()
            }
            LoginUiState.Idle -> Unit
        }
    }

    private fun setUpLanguageDropdown(b: FragmentLoginBinding) {
        val languages = listOf(
            getString(R.string.language_english) to Constants.LANGUAGE_ENGLISH,
            getString(R.string.language_zulu) to Constants.LANGUAGE_ZULU,
            getString(R.string.language_afrikaans) to Constants.LANGUAGE_AFRIKAANS
        )
        val adapter = ArrayAdapter(requireContext(), android.R.layout.simple_list_item_1, languages.map { it.first })
        b.dropdownLanguage.setAdapter(adapter)
        b.dropdownLanguage.setText(languages.first().first, false)
        b.dropdownLanguage.setOnItemClickListener { _, _, position, _ ->
            val code = languages[position].second
            AppCompatDelegate.setApplicationLocales(LocaleListCompat.forLanguageTags(code))
        }
    }

    // REQ-BIO-1 / REQ-BIO-2
    private fun launchBiometricPrompt() {
        val context = requireContext()
        val biometricManager = BiometricManager.from(context)
        val canAuthenticate = biometricManager.canAuthenticate(BiometricManager.Authenticators.BIOMETRIC_WEAK)
        if (canAuthenticate != BiometricManager.BIOMETRIC_SUCCESS) {
            Toast.makeText(context, R.string.login_biometric_unavailable, Toast.LENGTH_SHORT).show()
            return
        }

        val executor = ContextCompat.getMainExecutor(context)
        val prompt = BiometricPrompt(this, executor, object : BiometricPrompt.AuthenticationCallback() {
            override fun onAuthenticationSucceeded(result: BiometricPrompt.AuthenticationResult) {
                super.onAuthenticationSucceeded(result)
                viewModel.signIn("Biometric")
            }
        })

        val promptInfo = BiometricPrompt.PromptInfo.Builder()
            .setTitle(getString(R.string.login_biometric_prompt_title))
            .setSubtitle(getString(R.string.login_biometric_prompt_subtitle))
            .setAllowedAuthenticators(BiometricManager.Authenticators.BIOMETRIC_WEAK)
            .setNegativeButtonText(getString(R.string.action_cancel))
            .build()

        prompt.authenticate(promptInfo)
    }

    private fun navigateToHome() {
        val navController = findNavController()
        if (navController.currentDestination?.id == R.id.loginFragment) {
            navController.navigate(R.id.action_login_to_home)
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        binding = null
    }
}
