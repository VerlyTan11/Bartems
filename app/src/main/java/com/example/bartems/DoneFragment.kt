package com.example.bartems

import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import androidx.fragment.app.Fragment

class DoneFragment : Fragment() {

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.fragment_done, container, false)

        // Handle button click to navigate to HomeActivity
        val backToHomeButton = view.findViewById<Button>(R.id.button)
        backToHomeButton.setOnClickListener {
            val intent = Intent(activity, HomeActivity::class.java)
            startActivity(intent)
            activity?.finish() // Optionally close the current activity
        }

        return view
    }
}
