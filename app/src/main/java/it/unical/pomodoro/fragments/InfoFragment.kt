package it.unical.pomodoro.fragments

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.fragment.app.Fragment
import it.unical.pomodoro.R
import it.unical.pomodoro.databinding.FragmentInfoBinding
import java.io.BufferedReader
import java.io.InputStreamReader

class InfoFragment : Fragment() {

    private var _binding: FragmentInfoBinding? = null
    private val binding get() = _binding!!

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentInfoBinding.inflate(inflater, container, false)

        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val inputStream = resources.openRawResource(R.raw.info)
        val content = inputStream.bufferedReader().use { it.readText() }

        val textView = view.findViewById<TextView>(R.id.info_text_view)
        textView.text = content;
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}