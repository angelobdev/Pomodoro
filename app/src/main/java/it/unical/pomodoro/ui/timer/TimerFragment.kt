package it.unical.pomodoro.ui.timer

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.lifecycle.ViewModelProvider
import it.unical.pomodoro.R
import it.unical.pomodoro.databinding.FragmentSettingsBinding
import it.unical.pomodoro.databinding.FragmentTimerBinding

class TimerFragment : Fragment() {

    private var _binding: FragmentTimerBinding? = null

    // This property is only valid between onCreateView and
    // onDestroyView.
    private val binding get() = _binding!!

    override fun onCreateView(
        inflater: LayoutInflater,
        contaier: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        val timerViewModel =
            ViewModelProvider(this).get(TimerViewModel::class.java)

        _binding = FragmentTimerBinding.inflate(inflater, contaier, false)
        val root: View = binding.root

        return root
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }



}