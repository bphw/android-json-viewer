package app.vercel.bambangp.jsonviewer

import android.content.ClipboardManager
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.view.Menu
import android.view.MenuItem
import android.view.View
import android.view.inputmethod.InputMethodManager
import android.webkit.URLUtil
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.DividerItemDecoration
import androidx.recyclerview.widget.LinearLayoutManager
import app.vercel.bambangp.jsonviewer.databinding.ActivityMainBinding
import app.vercel.bambangp.jsonviewer.databinding.DialogUrlInputBinding
import app.vercel.bambangp.jsonviewer.utils.NetworkUtils
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

class MainActivity : AppCompatActivity() {

    private lateinit var binding: ActivityMainBinding
    private lateinit var adapter: JsonItemAdapter

    // File picker contract
    private val filePickerLauncher = registerForActivityResult(
        ActivityResultContracts.OpenDocument()
    ) { uri ->
        uri?.let { loadJsonFromUri(it) }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        setupToolbar()
        setupRecyclerView()
    }

    private fun setupToolbar() {
        setSupportActionBar(binding.toolbar)
        supportActionBar?.title = getString(R.string.app_name)
    }

    private fun setupRecyclerView() {
        adapter = JsonItemAdapter(emptyList(), this)
        binding.recyclerView.apply {
            layoutManager = LinearLayoutManager(this@MainActivity)
            addItemDecoration(DividerItemDecoration(this@MainActivity, DividerItemDecoration.VERTICAL))
            adapter = this@MainActivity.adapter
        }
    }

    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        menuInflater.inflate(R.menu.main_menu, menu)
        return true
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        return when (item.itemId) {
            R.id.menu_open_file -> {
                openFilePicker()
                true
            }
            R.id.menu_open_url -> {
                showUrlInputDialog()
                true
            }
            R.id.menu_privacy_policy -> {
                openPrivacyPolicy()
                true
            }
            R.id.menu_app_info -> {
                showAppInfoDialog()
                true
            }
            else -> super.onOptionsItemSelected(item)
        }
    }

    private fun openFilePicker() {
        filePickerLauncher.launch(arrayOf("application/json"))
    }

    private fun showUrlInputDialog() {
        val dialogBinding = DialogUrlInputBinding.inflate(layoutInflater)

        AlertDialog.Builder(this)
            .setTitle(getString(R.string.load_from_url))
            .setView(dialogBinding.root)
            .setPositiveButton(getString(R.string.load)) { _, _ ->
                val url = dialogBinding.etUrl.text.toString().trim()
                if (url.isNotEmpty() && URLUtil.isValidUrl(url)) {
                    loadFromUrl(url)
                } else {
                    showError(getString(R.string.invalid_url))
                }
            }
            .setNegativeButton(getString(R.string.cancel), null)
            .show()

        dialogBinding.etUrl.requestFocus()
        val imm = getSystemService(INPUT_METHOD_SERVICE) as InputMethodManager
        imm.showSoftInput(dialogBinding.etUrl, InputMethodManager.SHOW_IMPLICIT)
    }

    private fun loadFromUrl(url: String) {
        lifecycleScope.launch {
            try {
                binding.progressBar.visibility = View.VISIBLE
                val json = withContext(Dispatchers.IO) {
                    NetworkUtils.loadJsonFromUrl(url)
                }
                displayJson(json)
            } catch (e: Exception) {
                showError(getString(R.string.load_error_format, e.message ?: "Unknown error"))
            } finally {
                binding.progressBar.visibility = View.GONE
            }
        }
    }

    private fun loadJsonFromUri(uri: Uri) {
        lifecycleScope.launch {
            try {
                binding.progressBar.visibility = View.VISIBLE
                val json = withContext(Dispatchers.IO) {
                    contentResolver.openInputStream(uri)?.use { stream ->
                        stream.bufferedReader().use { it.readText() }
                    }
                }
                json?.let { displayJson(it) } ?: showError(getString(R.string.empty_file))
            } catch (e: Exception) {
                showError(getString(R.string.file_error_format, e.message ?: "Unknown error"))
            } finally {
                binding.progressBar.visibility = View.GONE
            }
        }
    }

    private fun displayJson(json: String) {
        try {
            val items = JsonItemAdapter.parseJson(json)
            adapter.updateItems(items)
        } catch (e: Exception) {
            showError(getString(R.string.invalid_json))
        }
    }

    private fun openPrivacyPolicy() {
        try {
            startActivity(
                Intent(
                    Intent.ACTION_VIEW,
                    Uri.parse("https://bambangp.vercel.app/privacy-policy")
                )
            )
        } catch (e: Exception) {
            showError(getString(R.string.no_browser))
        }
    }

    private fun showAppInfoDialog() {
        val versionText = "Version v20250919"
        val message = "$versionText\n\nhttps://bambangp.vercel.app"
        
        val spannableString = android.text.SpannableString(message)
        val urlStart = message.indexOf("https://bambangp.vercel.app")
        val urlEnd = urlStart + "https://bambangp.vercel.app".length
        
        val clickableSpan = object : android.text.style.ClickableSpan() {
            override fun onClick(widget: View) {
                val intent = Intent(Intent.ACTION_VIEW, Uri.parse("https://bambangp.vercel.app"))
                startActivity(intent)
            }
        }
        
        spannableString.setSpan(clickableSpan, urlStart, urlEnd, android.text.Spanned.SPAN_EXCLUSIVE_EXCLUSIVE)
        
        val dialog = AlertDialog.Builder(this)
            .setTitle(getString(R.string.app_name))
            .setMessage(spannableString)
            .setPositiveButton(getString(R.string.ok), null)
            .show()
            
        // Make links clickable
        dialog.findViewById<android.widget.TextView>(android.R.id.message)?.movementMethod = 
            android.text.method.LinkMovementMethod.getInstance()
    }

    private fun showError(message: String) {
        runOnUiThread {
            Toast.makeText(this, message, Toast.LENGTH_LONG).apply {
                setGravity(android.view.Gravity.CENTER, 0, 0)
                show()
            }
        }
    }
}