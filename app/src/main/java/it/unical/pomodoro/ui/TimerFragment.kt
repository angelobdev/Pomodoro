package it.unical.pomodoro.ui

import android.app.AlertDialog
import android.content.Context
import android.content.SharedPreferences
import android.media.MediaPlayer
import android.os.Bundle
import android.os.CountDownTimer
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.ProgressBar
import android.widget.TextView
import androidx.activity.OnBackPressedCallback
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.Fragment
import androidx.navigation.findNavController
import it.unical.pomodoro.R
import it.unical.pomodoro.databinding.FragmentTimerBinding

class TimerFragment : Fragment() {

    private var _binding: FragmentTimerBinding? = null
    private val binding get() = _binding!!

    private var studio: Int = 0
    private var relax: Int = 0

    private lateinit var player: MediaPlayer

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        player = MediaPlayer.create(requireContext(), R.raw.pewpew)

        (activity as AppCompatActivity?)!!.supportActionBar!!.hide()
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {

        _binding = FragmentTimerBinding.inflate(inflater, container, false)

        val timerSP: SharedPreferences =
            requireActivity().getSharedPreferences("timer", Context.MODE_PRIVATE)
        val timer = timerSP.getString("timer", "0.0")
        val studioRelax = timer!!.split(".")

        studio = studioRelax[0].toInt()
        relax = studioRelax[1].toInt()

        // Back
        requireActivity().onBackPressedDispatcher.addCallback(
            viewLifecycleOwner,
            object : OnBackPressedCallback(true) {
                override fun handleOnBackPressed() {
                    promptStop()
                }
            }
        )

        // Inflate the layout for this fragment
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        // Stop Button
        val stopButton = requireView().findViewById<Button>(R.id.timer_stop_button)
        stopButton.setOnClickListener {
            promptStop()
        }

        startCycle()
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    override fun onDestroy() {
        super.onDestroy()
        player.release()
    }

    // FUNZIONI

    private fun startCycle() {

        val progressBar = requireView().findViewById<ProgressBar>(R.id.progress_bar)
        val phaseText = requireView().findViewById<TextView>(R.id.phase_text)
        val countdownText = requireView().findViewById<TextView>(R.id.countdown_timer)
        val infoBoxText = requireView().findViewById<TextView>(R.id.timer_info_box)

        val step = 1L

        // Timer studio
        val studioTimerMillis = studio * 60000L
        progressBar.progress = 100
        phaseText.text = resources.getString(R.string.studio_tv_text)
        infoBoxText.text = resources.getString(R.string.timer_info_box_studio)

        object : CountDownTimer(studioTimerMillis, step) {

            override fun onTick(millisUntilFinished: Long) {
                countdownText.text = millisToString(millisUntilFinished)
                progressBar.progress =
                    ((millisUntilFinished.toFloat() / studioTimerMillis.toFloat()) * progressBar.max).toInt()
            }

            override fun onFinish() {

                // Notifico utente
                player.start()

                AlertDialog.Builder(requireContext())
                    .setMessage("Stai per passare alla fase di relax")
                    .setPositiveButton("Sono pronto") { _, _ ->

                        // Avvio relax
                        val relaxTimerMillis = relax * 60000L
                        progressBar.progress = 100
                        phaseText.text = resources.getString(R.string.relax_tv_text)
                        infoBoxText.text = resources.getString(R.string.timer_info_box_relax)

                        object : CountDownTimer(relaxTimerMillis, step) {

                            override fun onTick(millisUntilFinished: Long) {

                                countdownText.text = millisToString(millisUntilFinished)
                                progressBar.progress =
                                    ((millisUntilFinished.toFloat() / relaxTimerMillis.toFloat()) * progressBar.max).toInt()
                            }

                            override fun onFinish() {

                                // Notifico utente
                                player.start()

                                AlertDialog.Builder(requireContext())
                                    .setMessage("Stai per passare alla fase di studio")
                                    .setPositiveButton("Sono pronto") { _, _ ->
                                        startCycle()
                                    }.show() // Alert (Relax -> Studio)

                            } // onFinish (Relax)

                        }.start() // Relax

                    }.show() // Alert (Studio -> Relax)

            } // onFinish (Studio)

        }.start() // Studio
    }

    private fun promptStop() {
        AlertDialog.Builder(requireContext())
            .setTitle("Attenzione")
            .setMessage("Vuoi terminare la sessione?")
            .setPositiveButton("Si") { _, _ ->
                //Torno alla home
                val navController =
                    requireActivity().findNavController(R.id.content_main)
                navController.navigate(R.id.nav_home)

                (activity as AppCompatActivity?)!!.supportActionBar!!.show()
            }
            .setNegativeButton("No") { _, _ ->

            }.show()
    }

    private fun millisToString(millis: Long): String {
        val minuti = (millis / 60000L).toInt()
        val secondi = ((millis % 60000L) / 1000).toInt()

        val minStr = if (minuti < 10) "0$minuti" else "$minuti"
        val secStr = if (secondi < 10) "0$secondi" else "$secondi"

        return "$minStr : $secStr"
    }

}