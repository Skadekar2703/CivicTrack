package com.tommy.civictrack

import android.content.Intent
import android.os.Bundle
import android.view.View
import android.widget.EditText
import android.widget.TextView
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.android.gms.auth.api.signin.GoogleSignInClient
import com.google.android.gms.auth.api.signin.GoogleSignInOptions
import com.google.android.gms.common.api.ApiException
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.GoogleAuthProvider

class LoginActivity : AppCompatActivity() {

    private lateinit var firebaseAuth: FirebaseAuth
    private lateinit var googleSignInClient: GoogleSignInClient
    private lateinit var googleSignInButton: View
    private lateinit var emailButton: View
    private lateinit var emailAuthContainer: View
    private lateinit var emailInput: EditText
    private lateinit var passwordInput: EditText
    private lateinit var emailAuthSubmitButton: TextView
    private lateinit var emailAuthTitle: TextView
    private lateinit var authModePrompt: TextView
    private lateinit var authModeAction: TextView
    private lateinit var loadingView: View
    private var emailAuthMode = EmailAuthMode.SIGN_IN

    private val signInLauncher =
        registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
            val task = GoogleSignIn.getSignedInAccountFromIntent(result.data)
            try {
                val account = task.getResult(ApiException::class.java)
                val idToken = account.idToken
                if (idToken.isNullOrBlank()) {
                    showError("Google Sign-In did not return an ID token.")
                    return@registerForActivityResult
                }
                firebaseAuthWithGoogle(idToken)
            } catch (error: ApiException) {
                showError("Google Sign-In failed: ${error.localizedMessage ?: error.statusCode}")
            }
        }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        firebaseAuth = FirebaseAuth.getInstance()
        if (firebaseAuth.currentUser != null) {
            openMain()
            return
        }

        setContentView(R.layout.activity_login)

        googleSignInButton = findViewById(R.id.googleSignInBtn)
        emailButton = findViewById(R.id.emailContinueBtn)
        emailAuthContainer = findViewById(R.id.emailAuthContainer)
        emailInput = findViewById(R.id.emailInput)
        passwordInput = findViewById(R.id.passwordInput)
        emailAuthSubmitButton = findViewById(R.id.emailAuthSubmitButton)
        emailAuthTitle = findViewById(R.id.emailAuthTitle)
        authModePrompt = findViewById(R.id.authModePrompt)
        authModeAction = findViewById(R.id.authModeAction)
        loadingView = findViewById(R.id.loginProgress)

        val gso = GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
            .requestEmail()
            .requestIdToken(getString(R.string.default_web_client_id))
            .build()

        googleSignInClient = GoogleSignIn.getClient(this, gso)

        googleSignInButton.setOnClickListener {
            setLoading(true)
            signInLauncher.launch(googleSignInClient.signInIntent)
        }
        emailButton.setOnClickListener {
            toggleEmailAuthVisibility()
        }
        emailAuthSubmitButton.setOnClickListener {
            submitEmailAuth()
        }
        authModeAction.setOnClickListener {
            emailAuthMode = if (emailAuthMode == EmailAuthMode.SIGN_IN) {
                EmailAuthMode.CREATE_ACCOUNT
            } else {
                EmailAuthMode.SIGN_IN
            }
            bindEmailAuthMode()
        }
        bindEmailAuthMode()
    }

    override fun onStart() {
        super.onStart()
        if (firebaseAuth.currentUser != null) {
            openMain()
        }
    }

    private fun firebaseAuthWithGoogle(idToken: String) {
        val credential = GoogleAuthProvider.getCredential(idToken, null)
        firebaseAuth.signInWithCredential(credential)
            .addOnCompleteListener(this) { task ->
                if (task.isSuccessful) {
                    openMain()
                } else {
                    showError(task.exception?.localizedMessage ?: "Firebase authentication failed.")
                }
            }
    }

    private fun toggleEmailAuthVisibility() {
        emailAuthContainer.visibility =
            if (emailAuthContainer.visibility == View.VISIBLE) View.GONE else View.VISIBLE
    }

    private fun bindEmailAuthMode() {
        val createMode = emailAuthMode == EmailAuthMode.CREATE_ACCOUNT
        emailAuthTitle.text = if (createMode) "Create account with email" else "Sign in with email"
        emailAuthSubmitButton.text = if (createMode) "Create Account" else "Sign In"
        authModePrompt.text = if (createMode) "Already have an account?" else "Need an account?"
        authModeAction.text = if (createMode) "Sign in" else "Create one"
    }

    private fun submitEmailAuth() {
        val email = emailInput.text?.toString().orEmpty().trim()
        val password = passwordInput.text?.toString().orEmpty()

        when {
            email.isBlank() -> showError("Enter your email address.")
            password.isBlank() -> showError("Enter your password.")
            password.length < 6 -> showError("Password must be at least 6 characters.")
            else -> {
                setLoading(true)
                if (emailAuthMode == EmailAuthMode.CREATE_ACCOUNT) {
                    firebaseAuth.createUserWithEmailAndPassword(email, password)
                        .addOnCompleteListener(this) { task ->
                            if (task.isSuccessful) {
                                openMain()
                            } else {
                                showError(task.exception?.localizedMessage ?: "Account creation failed.")
                            }
                        }
                } else {
                    firebaseAuth.signInWithEmailAndPassword(email, password)
                        .addOnCompleteListener(this) { task ->
                            if (task.isSuccessful) {
                                openMain()
                            } else {
                                showError(task.exception?.localizedMessage ?: "Email sign-in failed.")
                            }
                        }
                }
            }
        }
    }

    private fun openMain() {
        startActivity(Intent(this, MainActivity::class.java))
        finish()
    }

    private fun showError(message: String) {
        setLoading(false)
        showTextToast(message)
    }

    private fun setLoading(loading: Boolean) {
        loadingView.visibility = if (loading) View.VISIBLE else View.GONE
        googleSignInButton.isEnabled = !loading
        emailButton.isEnabled = !loading
        emailAuthSubmitButton.isEnabled = !loading
        emailInput.isEnabled = !loading
        passwordInput.isEnabled = !loading
        authModeAction.isEnabled = !loading
    }

    private enum class EmailAuthMode {
        SIGN_IN,
        CREATE_ACCOUNT
    }
}
