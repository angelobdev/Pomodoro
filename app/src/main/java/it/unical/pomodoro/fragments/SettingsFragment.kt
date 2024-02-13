package it.unical.pomodoro.fragments

import android.annotation.SuppressLint
import android.content.Context
import android.content.SharedPreferences
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Switch
import androidx.fragment.app.Fragment
import it.unical.pomodoro.R
import it.unical.pomodoro.databinding.FragmentSettingsBinding

@SuppressLint("UseSwitchCompatOrMaterialCode")
class SettingsFragment : Fragment() {

    private var _binding: FragmentSettingsBinding? = null
    private val binding get() = _binding!!

    private lateinit var settingsSP: SharedPreferences

    companion object {
        const val ALERT_MOVIMENTO_KEY = "alert_movimento"
        const val SILENZIOSO_KEY = "silenzioso"
        const val NON_DISTURBARE_KEY = "non_disturbare"
        const val BLOCCA_CONNESSIONE_KEY = "blocca_connessione"
    }

    private lateinit var alertMovimentoSwitch: Switch
    private lateinit var silenziosoSwitch: Switch
    private lateinit var nonDisturbareSwitch: Switch
    private lateinit var bloccaConnessioneSwitch: Switch

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentSettingsBinding.inflate(inflater, container, false)

        settingsSP = requireActivity().getSharedPreferences("settings", Context.MODE_PRIVATE)

        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        alertMovimentoSwitch = view.findViewById(R.id.switch_alert_movimento)
        alertMovimentoSwitch.isChecked = settingsSP.getBoolean(ALERT_MOVIMENTO_KEY, false)
        alertMovimentoSwitch.setOnCheckedChangeListener { _, isChecked ->
            settingsSP.edit().putBoolean(ALERT_MOVIMENTO_KEY, isChecked).apply()
        }

        silenziosoSwitch = view.findViewById(R.id.switch_silenzioso)
        silenziosoSwitch.isChecked = settingsSP.getBoolean(SILENZIOSO_KEY, false)
        silenziosoSwitch.setOnCheckedChangeListener { _, isChecked ->
            settingsSP.edit().putBoolean(SILENZIOSO_KEY, isChecked).apply()
        }

        nonDisturbareSwitch = view.findViewById(R.id.switch_non_distubare)
        nonDisturbareSwitch.isChecked = settingsSP.getBoolean(NON_DISTURBARE_KEY, false)
        nonDisturbareSwitch.setOnCheckedChangeListener { _, isChecked ->
            settingsSP.edit().putBoolean(NON_DISTURBARE_KEY, isChecked).apply()
        }

        bloccaConnessioneSwitch = view.findViewById(R.id.switch_connessione)
        bloccaConnessioneSwitch.isChecked = settingsSP.getBoolean(BLOCCA_CONNESSIONE_KEY, false)
        bloccaConnessioneSwitch.setOnCheckedChangeListener { _, isChecked ->
            settingsSP.edit().putBoolean(BLOCCA_CONNESSIONE_KEY, isChecked).apply()
        }


    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}