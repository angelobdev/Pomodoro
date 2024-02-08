package it.unical.pomodoro.ui.home

import android.app.AlertDialog
import android.content.Context
import android.content.SharedPreferences
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.AdapterView
import android.widget.ArrayAdapter
import android.widget.Button
import android.widget.EditText
import android.widget.NumberPicker
import android.widget.NumberPicker.OnScrollListener.SCROLL_STATE_IDLE
import android.widget.Spinner
import android.widget.TextView
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import it.unical.pomodoro.R
import it.unical.pomodoro.databinding.FragmentHomeBinding


class HomeFragment : Fragment() {

    private val maxMinuti = 60
    private val minMinuti = 1

    private var _binding: FragmentHomeBinding? = null

    // This property is only valid between onCreateView and
    // onDestroyView.
    private val binding get() = _binding!!

    private lateinit var homeViewModel: HomeViewModel
    private lateinit var presetsSP: SharedPreferences
    private lateinit var numberPickerStudio: NumberPicker
    private lateinit var numberPickerRelax: NumberPicker
    private lateinit var presetsSpinner: Spinner
    private lateinit var startButton: Button

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        this.homeViewModel =
            ViewModelProvider(this).get(HomeViewModel::class.java)

        _binding = FragmentHomeBinding.inflate(inflater, container, false)
        val root: View = binding.root

        this.presetsSP =
            requireContext().getSharedPreferences("Presets", Context.MODE_PRIVATE)

        return root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        // PICKERS
        numberPickerStudio = view.findViewById(R.id.studio_timer)
        numberPickerStudio.maxValue = maxMinuti
        numberPickerStudio.minValue = minMinuti
        numberPickerStudio.value = 45;

        numberPickerRelax = view.findViewById(R.id.relax_timer)
        numberPickerRelax.maxValue = maxMinuti
        numberPickerRelax.minValue = minMinuti
        numberPickerRelax.value = 15;

        // Listeners
        numberPickerStudio.setOnScrollListener { _, scrollState ->
            checkScroll(view, scrollState, numberPickerStudio, numberPickerRelax)
        } // studio

        numberPickerRelax.setOnScrollListener { _, scrollState ->
            checkScroll(view, scrollState, numberPickerStudio, numberPickerRelax)
        } // relax

        val textView = view.findViewById<TextView>(R.id.indice_equilibrio)
        textView.text = resources.getString(R.string.equilibro_text_2)

        // DROPDOWN
        presetsSpinner = view.findViewById(R.id.dropdown_salvati)
        presetsSpinner.prompt = "Seleziona un preset:"

        presetsSpinner.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
            override fun onItemSelected(
                parent: AdapterView<*>?,
                view: View?,
                position: Int,
                id: Long
            ) {
                // Codice da eseguire quando viene selezionata un'opzione
                val selezionato = parent?.getItemAtPosition(position).toString()

                val timer = presetsSP.getString(selezionato, "NULL");
                val studioRelax = timer?.split(".")!!

                numberPickerStudio.value = studioRelax[0].toInt()
                numberPickerRelax.value = studioRelax[1].toInt()

            }

            override fun onNothingSelected(parent: AdapterView<*>?) {
                // Codice da eseguire quando nessuna opzione è selezionata
            }
        }

        val adapter: ArrayAdapter<String> =
            ArrayAdapter(requireContext(), android.R.layout.simple_spinner_item)
        presetsSP.all.forEach { (key, _) ->
            run {
                adapter.add(key)
            }
        }

        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        presetsSpinner.adapter = adapter


        //Salva
        val salvaPresetButton = view.findViewById<Button>(R.id.salva_preset_button)
        salvaPresetButton.setOnClickListener {
            showInputDialog()
        }

        //Avvia
        val startButton = view.findViewById<Button>(R.id.start_button)

        //Listener avvia
        startButton.setOnClickListener {
            println("Ciao")
        }

    }

    private fun checkScroll(
        view: View,
        scrollState: Int,
        numberPickerStudio: NumberPicker,
        numberPickerRelax: NumberPicker
    ) {
        if (scrollState == SCROLL_STATE_IDLE) {
            val studio = numberPickerStudio.value
            val relax = numberPickerRelax.value

            val ratio = studio.toFloat() / relax.toFloat()
            val textView = view.findViewById<TextView>(R.id.indice_equilibrio)

            textView.text = when (ratio) {
                in 0.0f..0.99f -> resources.getString(R.string.equilibro_text_0);    // Poco efficiente
                in 1.00f..2.49f -> resources.getString(R.string.equilibro_text_1);      // Poco Equilibrato //TODO: 2.49
                in 2.5f..3.5f -> resources.getString(R.string.equilibro_text_2);    // Equilibrato
                in 3.51f..6f -> resources.getString(R.string.equilibro_text_3);   // Abbastanza Equilibrato
                else -> resources.getString(R.string.equilibro_text_4);    // Intenso
            }

            println("Ratio: $ratio, Valore: ${textView.text}")
        }

    }

    private fun showInputDialog() {
        val builder = AlertDialog.Builder(context)
        builder.setTitle("Inserisci il nome del preset da salvare")

        // Aggiungi un EditText per l'inserimento del valore
        val input = EditText(context)
        builder.setView(input)

        // Aggiungi il pulsante "Salva"
        builder.setPositiveButton(
            "Salva"
        ) { dialog, which ->
            val nome = input.text.toString()

            // Salvo il valore nelle SharedPreferences
            val editor: SharedPreferences.Editor = presetsSP.edit()

            val studio = numberPickerStudio.value
            val relax = numberPickerRelax.value

            editor.putString(nome, "$studio.$relax")
            editor.apply()

            val adapter = presetsSpinner.adapter as ArrayAdapter<String>
            adapter.add(nome)
            adapter.notifyDataSetChanged()

            // Notifico l'utente
            Toast.makeText(
                this@HomeFragment.context,
                "Valore salvato con successo",
                Toast.LENGTH_SHORT
            )
                .show()
        }

        // Mostra il dialog
        builder.show()
    }

    //start button press


    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}