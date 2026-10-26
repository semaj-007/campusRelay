package com.example.campusrelay

import android.os.Bundle
import android.view.View
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.app.AppCompatDelegate
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.NavHostFragment
import androidx.navigation.ui.setupWithNavController
import com.example.campusrelay.databinding.ActivityMainBinding
import kotlinx.coroutines.launch

/**
 * Hosts the [androidx.navigation.fragment.NavHostFragment] declared in
 * res/navigation/nav_graph.xml and wires it up to the bottom navigation bar. The bottom
 * bar is hidden on the login screen, since a user who isn't signed in yet has nothing to
 * navigate between (see the User Navigation Flow in the Part 1 design doc).
 */
class MainActivity : AppCompatActivity() {

    private lateinit var binding: ActivityMainBinding

    private val app: CampusRelayApp get() = application as CampusRelayApp

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        kotlinx.coroutines.runBlocking { app.serviceLocator.preferencesManager.migrateSession() }
        window.addFlags(android.view.WindowManager.LayoutParams.FLAG_SECURE)
        enableEdgeToEdge()
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        ViewCompat.setOnApplyWindowInsetsListener(binding.main) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        val navHostFragment = supportFragmentManager
            .findFragmentById(R.id.nav_host_fragment) as NavHostFragment
        val navController = navHostFragment.navController

        binding.bottomNav.setupWithNavController(navController)

        navController.addOnDestinationChangedListener { _, destination, _ ->
            binding.bottomNav.visibility = if (destination.id == R.id.loginFragment) View.GONE else View.VISIBLE
        }

        observeAppearancePreference()
        lifecycleScope.launch {
            app.serviceLocator.preferencesManager.isSignedIn.collect { signedIn ->
                if (signedIn) {
                    com.example.campusrelay.notifications.DeviceRegistrationWorker.schedule(this@MainActivity)
                    com.example.campusrelay.sync.SyncScheduler.scheduleSyncWhenOnline(this@MainActivity)
                    if (androidx.core.content.ContextCompat.checkSelfPermission(this@MainActivity, android.Manifest.permission.POST_NOTIFICATIONS) != android.content.pm.PackageManager.PERMISSION_GRANTED) {
                        notificationPermission.launch(android.Manifest.permission.POST_NOTIFICATIONS)
                    }
                }
            }
        }
    }

    private val notificationPermission = registerForActivityResult(androidx.activity.result.contract.ActivityResultContracts.RequestPermission()) { _ -> }
    private var promptActive = false
    private val vault get() = app.serviceLocator.preferencesManager.sessionVault

    override fun onStart() {
        super.onStart()
        if (vault.protected && vault.token() == null) {
            binding.root.visibility = View.INVISIBLE
            authenticateSession(false) { binding.root.visibility = View.VISIBLE }
        } else binding.root.visibility = View.VISIBLE
    }

    override fun onStop() {
        binding.root.visibility = View.INVISIBLE
        vault.lock()
        super.onStop()
    }

    fun authenticateSession(enroll: Boolean, onSuccess: () -> Unit) {
        if (promptActive) return
        if (!enroll && !vault.protected) {
            android.widget.Toast.makeText(this, R.string.biometric_sign_in_first, android.widget.Toast.LENGTH_LONG).show()
            return
        }
        try {
            val cipher = if (enroll) vault.enrollmentCipher() else vault.unlockCipher()
            promptActive = true
            val prompt = androidx.biometric.BiometricPrompt(this, androidx.core.content.ContextCompat.getMainExecutor(this),
                object : androidx.biometric.BiometricPrompt.AuthenticationCallback() {
                    override fun onAuthenticationSucceeded(result: androidx.biometric.BiometricPrompt.AuthenticationResult) {
                        promptActive = false
                        try {
                            val authenticated = requireNotNull(result.cryptoObject?.cipher)
                            if (enroll) vault.enable(authenticated) else vault.unlock(authenticated)
                            binding.root.visibility = View.VISIBLE
                            com.example.campusrelay.sync.SyncScheduler.scheduleSyncWhenOnline(this@MainActivity)
                            com.example.campusrelay.notifications.DeviceRegistrationWorker.schedule(this@MainActivity)
                            onSuccess()
                        } catch (_: Exception) { showLocked(enroll) }
                    }
                    override fun onAuthenticationError(code: Int, message: CharSequence) {
                        promptActive = false
                        if (!enroll) showLocked(false)
                        else android.widget.Toast.makeText(this@MainActivity, message, android.widget.Toast.LENGTH_LONG).show()
                    }
                })
            val info = androidx.biometric.BiometricPrompt.PromptInfo.Builder()
                .setTitle(getString(R.string.login_biometric_prompt_title))
                .setSubtitle(getString(R.string.login_biometric_prompt_subtitle))
                .setAllowedAuthenticators(androidx.biometric.BiometricManager.Authenticators.BIOMETRIC_STRONG)
                .setNegativeButtonText(getString(R.string.action_cancel)).build()
            prompt.authenticate(info, androidx.biometric.BiometricPrompt.CryptoObject(cipher))
        } catch (_: Exception) {
            promptActive = false
            showLocked(enroll)
        }
    }

    private fun showLocked(enroll: Boolean) {
        if (isFinishing || isDestroyed) return
        if (enroll) {
            android.widget.Toast.makeText(this, R.string.login_biometric_unavailable, android.widget.Toast.LENGTH_LONG).show()
            return
        }
        androidx.appcompat.app.AlertDialog.Builder(this)
            .setMessage(R.string.biometric_locked)
            .setCancelable(false)
            .setPositiveButton(R.string.action_retry) { _, _ -> authenticateSession(false) {} }
            .setNegativeButton(R.string.settings_sign_out) { _, _ ->
                lifecycleScope.launch {
                    app.serviceLocator.authRepository.signOut()
                    val host = supportFragmentManager.findFragmentById(R.id.nav_host_fragment) as NavHostFragment
                    host.navController.setGraph(R.navigation.nav_graph)
                    binding.root.visibility = View.VISIBLE
                }
            }.show()
    }

    private fun observeAppearancePreference() {
        lifecycleScope.launch {
            app.serviceLocator.settingsRepository.darkMode.collect { isDark ->
                val mode = if (isDark) AppCompatDelegate.MODE_NIGHT_YES else AppCompatDelegate.MODE_NIGHT_NO
                if (AppCompatDelegate.getDefaultNightMode() != mode) {
                    AppCompatDelegate.setDefaultNightMode(mode)
                }
            }
        }
    }
}
