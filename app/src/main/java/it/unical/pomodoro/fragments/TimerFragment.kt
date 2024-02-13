package it.unical.pomodoro.fragments

import android.app.AlertDialog
import android.content.Context
import android.content.Intent
import android.content.SharedPreferences
import android.hardware.Sensor
import android.hardware.SensorEvent
import android.hardware.SensorEventListener
import android.hardware.SensorManager
import android.media.AudioManager
import android.media.MediaPlayer
import android.os.Bundle
import android.os.CountDownTimer
import android.provider.Settings
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.ProgressBar
import android.widget.TextView
import android.widget.Toast
import androidx.activity.OnBackPressedCallback
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.Fragment
import androidx.navigation.findNavController
import it.unical.pomodoro.R
import it.unical.pomodoro.databinding.FragmentTimerBinding
import kotlin.math.sqrt


class TimerFragment : Fragment(), SensorEventListener {

    private var _binding: FragmentTimerBinding? = null
    private val binding get() = _binding!!

    // Impostazioni
    private lateinit var settingsSP: SharedPreferences
    private var alertMovimento: Boolean = false
    private var silenzioso: Boolean = false
    private var nonDisturbare: Boolean = false
    private var bloccaConnessione: Boolean = false

    // Sensori e Servizi
    private lateinit var player: MediaPlayer
    private lateinit var audioManager: AudioManager
    private lateinit var sensorManager: SensorManager
    private lateinit var accelerometer: Sensor

    private var alertShowing = false


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // Inizializzo le impostazioni
        settingsSP = requireActivity().getSharedPreferences("settings", Context.MODE_PRIVATE)
        alertMovimento = settingsSP.getBoolean(SettingsFragment.ALERT_MOVIMENTO_KEY, false)
        silenzioso = settingsSP.getBoolean(SettingsFragment.SILENZIOSO_KEY, false)
        nonDisturbare = settingsSP.getBoolean(SettingsFragment.NON_DISTURBARE_KEY, false)
        bloccaConnessione = settingsSP.getBoolean(SettingsFragment.BLOCCA_CONNESSIONE_KEY, false)

        // Sensori e Servizi
        player = MediaPlayer.create(requireContext(), R.raw.pewpew)
        audioManager = requireContext().getSystemService(Context.AUDIO_SERVICE) as AudioManager
        sensorManager = requireContext().getSystemService(Context.SENSOR_SERVICE) as SensorManager
        accelerometer = sensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER)!!

        (activity as AppCompatActivity?)!!.supportActionBar!!.hide()
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {

        _binding = FragmentTimerBinding.inflate(inflater, container, false)

        // Intercetto l'azione di 'back'
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

        // Controllo le impostazioni e faccio partire il timer
        checkSettings()
        startCycle()
    }

    override fun onResume() {
        super.onResume()
        sensorManager.registerListener(this, accelerometer, SensorManager.SENSOR_DELAY_NORMAL)
    }

    override fun onPause() {
        super.onPause()
        sensorManager.unregisterListener(this)
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    override fun onDestroy() {
        super.onDestroy()
        player.release()
    }

    // SENSORI

    override fun onSensorChanged(event: SensorEvent?) {

        val threshold = 10f // Valore soglia per rilevare se il dispositivo è fermo

        if (event!!.sensor.type == Sensor.TYPE_ACCELEROMETER) {
            val acceleration = sqrt(
                (event.values[0] * event.values[0] + event.values[1] * event.values[1] + event.values[2] * event.values[2]).toDouble()
            ).toFloat()

            println("$acceleration")

            if (alertMovimento && acceleration > threshold) {

                if (!alertShowing) {
                    // Il dispositivo si sta muovendo
                    AlertDialog.Builder(requireContext())
                        .setMessage("Non dovresti spostare il telefono durante la fase di studio")
                        .setOnDismissListener {
                            alertShowing = false
                        }
                        .show()
                    alertShowing = true
                }

            }
        }
    }

    override fun onAccuracyChanged(sensor: Sensor?, accuracy: Int) {
        // Niente
    }

    // FUNZIONI

    private fun checkSettings() {

        if (silenzioso) {
            audioManager.ringerMode = AudioManager.RINGER_MODE_VIBRATE
        }

        if (nonDisturbare) {
            audioManager.ringerMode = AudioManager.RINGER_MODE_SILENT
        }

        if (bloccaConnessione) {
            requestAirplaneMode(requireContext(), true)
        }

    }

    private fun startCycle() {

        val timerSP: SharedPreferences =
            requireActivity().getSharedPreferences("timer", Context.MODE_PRIVATE)
        val timer = timerSP.getString("timer", "0.0")
        val studioRelax = timer!!.split(".")

        val studio = studioRelax[0].toInt()
        val relax = studioRelax[1].toInt()

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

                // Riporto il sistema allo stato iniziale
                if (silenzioso || nonDisturbare) {
                    audioManager.ringerMode = AudioManager.RINGER_MODE_NORMAL
                }

                if (bloccaConnessione) {
                    requestAirplaneMode(requireContext(), false)
                }

                //Torno alla home
                val navController =
                    requireActivity().findNavController(R.id.content_main)
                navController.navigate(R.id.nav_home)

                (activity as AppCompatActivity?)!!.supportActionBar!!.show()
            }
            .setNegativeButton("No") { _, _ ->
                // Niente
            }.show()
    }

    private fun millisToString(millis: Long): String {
        val minuti = (millis / 60000L).toInt()
        val secondi = ((millis % 60000L) / 1000).toInt()

        val minStr = if (minuti < 10) "0$minuti" else "$minuti"
        val secStr = if (secondi < 10) "0$secondi" else "$secondi"

        return "$minStr : $secStr"
    }

    private fun requestAirplaneMode(context: Context, active: Boolean) {

        Toast.makeText(
            requireContext(),
            (if (active) "Abilita" else "Disabilita") + " manualmente la modalità aereo e torna all'applicazione",
            Toast.LENGTH_LONG
        ).show()

        val intent = Intent(Settings.ACTION_AIRPLANE_MODE_SETTINGS)
        intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK
        context.startActivity(intent)
    }

}