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

    private val googleSignIn = registerForActivityResult(androidx.activity.result.contract.ActivityResultContracts.StartActivityForResult()) { result ->
        try {
            val account = com.google.android.gms.auth.api.signin.GoogleSignIn.getSignedInAccountFromIntent(result.data)
                .getResult(com.google.android.gms.common.api.ApiException::class.java)
            val token = account.idToken ?: error("Missing Google ID token")
            viewModel.signInWithGoogleToken(token)
        } catch (_: Exception) {
            Toast.makeText(requireContext(), R.string.login_failed, Toast.LENGTH_LONG).show()
        }
    }
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

        b.buttonSsoGoogle.setOnClickListener {
            val resourceId = resources.getIdentifier("default_web_client_id", "string", requireContext().packageName)
            val clientId = if (resourceId != 0) getString(resourceId) else com.example.campusrelay.BuildConfig.GOOGLE_WEB_CLIENT_ID
            if (clientId.isBlank()) {
                Toast.makeText(requireContext(), R.string.login_configuration_required, Toast.LENGTH_LONG).show()
                return@setOnClickListener
            }
            val options = com.google.android.gms.auth.api.signin.GoogleSignInOptions.Builder(com.google.android.gms.auth.api.signin.GoogleSignInOptions.DEFAULT_SIGN_IN)
                .requestIdToken(clientId).requestEmail().build()
            googleSignIn.launch(com.google.android.gms.auth.api.signin.GoogleSignIn.getClient(requireActivity(), options).signInIntent)
        }
        b.buttonSsoMicrosoft.setOnClickListener { viewModel.signIn("Microsoft", requireActivity()) }
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
        val selected = AppCompatDelegate.getApplicationLocales().toLanguageTags().substringBefore(',').ifBlank { "en" }
        b.dropdownLanguage.setText(languages.firstOrNull { it.second == selected }?.first ?: languages.first().first, false)
        b.dropdownLanguage.setOnItemClickListener { _, _, position, _ ->
            val code = languages[position].second
            AppCompatDelegate.setApplicationLocales(LocaleListCompat.forLanguageTags(code))
        }
    }

    private fun launchBiometricPrompt() {
        (requireActivity() as com.example.campusrelay.MainActivity).authenticateSession(false) { navigateToHome() }
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
