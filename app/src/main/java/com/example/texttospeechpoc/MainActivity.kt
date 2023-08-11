package com.example.texttospeechpoc

import android.Manifest.permission.RECORD_AUDIO
import android.annotation.SuppressLint
import android.app.Activity
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Bundle
import android.speech.RecognizerIntent
import android.speech.SpeechRecognizer
import android.speech.tts.TextToSpeech
import android.util.Log
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import com.example.texttospeechpoc.databinding.ActivityMainBinding
import java.util.*


class MainActivity : AppCompatActivity() {

    private lateinit var binding : ActivityMainBinding
    private lateinit var textToSpeech1 : TextToSpeech

    private var currentSpeechLanguage = "en_IN"

    @SuppressLint("ClickableViewAccessibility")
    override fun onCreate(savedInstanceState : Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        checkPermission(
            RECORD_AUDIO,
            MIC_PERMISSION_CODE
        )

        initTextToSpeech()
        initRadioGroup()


        binding.ivSpeaker.setOnClickListener {
            speak()
        }



        binding.ivMic.setOnClickListener {
//            checkPermission(
//                RECORD_AUDIO,
//                MIC_PERMISSION_CODE
//            )
            initSpeechToText()
        }
    }

    private val launcherGoogleTextToSpeech =
        registerForActivityResult(ActivityResultContracts.StartActivityForResult()) {
            when (it.resultCode) {
                Activity.RESULT_OK -> {
                    if (it != null && it.data != null
                    ) {
                        val matches =
                            it.data!!.getStringArrayListExtra(RecognizerIntent.EXTRA_RESULTS)
                        val sTextFromSpeech : String =
                            if (!matches.isNullOrEmpty()) {
                                matches[0].toString()
                            } else {
                                ""
                            }
                        binding.etStt.setText(sTextFromSpeech)
                        Log.i(TAG, "onResults: $sTextFromSpeech")
                    }
                }
                Activity.RESULT_CANCELED -> {

                }
            }
        }

    private fun initRadioGroup() {
        binding.rgLanguages.setOnCheckedChangeListener { group, checkedId ->
            currentSpeechLanguage = when (checkedId) {
                R.id.radioButton -> "en_IN"
                R.id.radioButton2 -> "hi"
                R.id.radioButton3 -> "mr"
                R.id.radioButton4 -> "gu"
                R.id.radioButton5 -> "kn_IN"
                R.id.radioButton6 -> "te_IN"
                else -> ""
            }
        }
        binding.rgLanguages.check(R.id.radioButton)
    }

    private fun initSpeechToText() {
        if(SpeechRecognizer.isRecognitionAvailable(this)) {
           val speechRecognizerIntent = Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH)

            speechRecognizerIntent.putExtra(RecognizerIntent.EXTRA_PARTIAL_RESULTS, true)
            speechRecognizerIntent.putExtra(
                RecognizerIntent.EXTRA_LANGUAGE_MODEL,
                RecognizerIntent.LANGUAGE_MODEL_FREE_FORM
            )
            speechRecognizerIntent.putExtra(
                RecognizerIntent.EXTRA_PROMPT,
                "Say Something"
            )
            speechRecognizerIntent.putExtra(RecognizerIntent.EXTRA_LANGUAGE, currentSpeechLanguage)
            launcherGoogleTextToSpeech.launch(speechRecognizerIntent)

        }
    }

    private fun speak() {
        val toSpeak : String = binding.etTts.text.toString().trim()
        if (toSpeak.isNotBlank()) {
            Toast.makeText(applicationContext, toSpeak, Toast.LENGTH_SHORT).show()
            textToSpeech1.setSpeechRate(0.5F)
            textToSpeech1.speak(toSpeak, TextToSpeech.QUEUE_FLUSH, null, null)
        }
    }

    private fun initTextToSpeech() {
        textToSpeech1 = TextToSpeech(
            applicationContext
        ) { status ->
            if (status != TextToSpeech.ERROR) {
                textToSpeech1.language = Locale.getDefault()
            }
        }
    }

    override fun onDestroy() {
        textToSpeech1.shutdown()
        super.onDestroy()
    }

    private fun checkPermission(permission : String, requestCode : Int) {
        if (ContextCompat.checkSelfPermission(
                this,
                permission
            ) == PackageManager.PERMISSION_DENIED
        ) {
            // Requesting the permission
            ActivityCompat.requestPermissions(this, arrayOf(permission), requestCode)
        } else {
//            Toast.makeText(this, "Permission already granted", Toast.LENGTH_SHORT).show()
        }
    }

    override fun onRequestPermissionsResult(
        requestCode : Int,
        permissions : Array<String>,
        grantResults : IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        if (requestCode == MIC_PERMISSION_CODE) {
            if (grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                Toast.makeText(this@MainActivity, "Mic Permission Granted", Toast.LENGTH_SHORT)
                    .show()
            } else {
                Toast.makeText(this@MainActivity, "Mic Permission Denied", Toast.LENGTH_SHORT)
                    .show()
            }
        }
    }

    companion object {
        const val TAG = "STT"
    }

}