package com.example.applymate.ui

import android.content.Intent
import android.os.Bundle
import android.view.View
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.applymate.databinding.ActivityMainBinding
import com.example.applymate.ui.adapters.ApplicationAdapter
import com.example.applymate.viewmodels.MainViewModel
import com.google.firebase.auth.FirebaseAuth

class MainActivity : AppCompatActivity() {
    private lateinit var binding: ActivityMainBinding
    private lateinit var viewModel: MainViewModel
    private lateinit var adapter: ApplicationAdapter

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        viewModel = ViewModelProvider(this).get(MainViewModel::class.java)
        setupRecyclerView()
        observeViewModel()

        binding.fabAdd.setOnClickListener {
            // Show Add Dialog
        }

        binding.btnLogout.setOnClickListener {
            FirebaseAuth.getInstance().signOut()
            startActivity(Intent(this, LoginActivity::class.java))
            finish()
        }

        viewModel.fetchApplications()
    }

    private fun setupRecyclerView() {
        adapter = ApplicationAdapter { app ->
            viewModel.deleteApplication(app.id)
        }
        binding.recyclerView.layoutManager = LinearLayoutManager(this)
        binding.recyclerView.adapter = adapter
    }

    private fun observeViewModel() {
        viewModel.applications.observe(this) { apps ->
            adapter.submitList(apps)
            updateStats(apps)
        }

        viewModel.loading.observe(this) { isLoading ->
            binding.progressBar.visibility = if (isLoading) View.VISIBLE else View.GONE
        }
    }

    private fun updateStats(apps: List<com.example.applymate.models.Application>) {
        binding.tvTotal.text = apps.size.toString()
        binding.tvPending.text = apps.count { it.status == "Pending" }.toString()
        binding.tvAccepted.text = apps.count { it.status == "Accepted" }.toString()
        binding.tvRejected.text = apps.count { it.status == "Rejected" }.toString()
    }
}
