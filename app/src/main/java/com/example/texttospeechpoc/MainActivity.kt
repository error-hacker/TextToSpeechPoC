package com.example.texttospeechpoc

import android.Manifest.permission.RECORD_AUDIO
import android.annotation.SuppressLint
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Bundle
import android.speech.RecognitionListener
import android.speech.RecognizerIntent
import android.speech.SpeechRecognizer
import android.speech.tts.TextToSpeech
import android.util.Log
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.core.graphics.drawable.DrawableCompat
import com.example.texttospeechpoc.databinding.ActivityMainBinding
import java.util.*


class MainActivity : AppCompatActivity() {

    private lateinit var binding : ActivityMainBinding
    private lateinit var textToSpeech1 : TextToSpeech
    private lateinit var speechRecognizer : SpeechRecognizer
    private lateinit var speechRecognizerIntent : Intent

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
        initSpeechToText()
        initRadioGroup()


        binding.ivSpeaker.setOnClickListener {
            speak()
        }



        binding.ivMic.setOnClickListener {
            checkPermission(
                RECORD_AUDIO,
                MIC_PERMISSION_CODE
            )
            speechRecognizerIntent.putExtra(RecognizerIntent.EXTRA_LANGUAGE, currentSpeechLanguage)
            speechRecognizer.startListening(speechRecognizerIntent)
        }


    }

    private fun initRadioGroup() {
        binding.rgLanguages.setOnCheckedChangeListener { group, checkedId ->
            currentSpeechLanguage = when (checkedId) {
                R.id.radioButton -> "en_IN"
                R.id.radioButton2 -> "hi_IN"
                R.id.radioButton3 -> "mr_IN"
                R.id.radioButton4 -> "gu_IN"
                R.id.radioButton5 -> "kn_IN"
                R.id.radioButton6 -> "te_IN"
                else -> ""
            }
        }
        binding.rgLanguages.check(R.id.radioButton)
    }

    private fun initSpeechToText() {
        speechRecognizer = SpeechRecognizer.createSpeechRecognizer(this)

        speechRecognizerIntent = Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH)

        speechRecognizerIntent.putExtra(RecognizerIntent.EXTRA_PARTIAL_RESULTS, true)
        speechRecognizerIntent.putExtra(
            RecognizerIntent.EXTRA_LANGUAGE_MODEL,
            RecognizerIntent.LANGUAGE_MODEL_FREE_FORM
        )

        speechRecognizer.setRecognitionListener(
            object : RecognitionListener {
                override fun onReadyForSpeech(params : Bundle?) {
                    DrawableCompat.setTint(
                        DrawableCompat.wrap(binding.ivMic.drawable),
                        ContextCompat.getColor(this@MainActivity, R.color.red)
                    )
                    Log.i(TAG, "onReadyForSpeech")
                }

                override fun onBeginningOfSpeech() {
                    Log.i(TAG, "onBeginningOfSpeech")
                }

                override fun onRmsChanged(rmsdB : Float) {
                }

                override fun onBufferReceived(buffer : ByteArray?) {
                }

                override fun onEndOfSpeech() {
                    Log.i(TAG, "onEndOfSpeech")
                }

                override fun onError(error : Int) {
                    DrawableCompat.setTint(
                        DrawableCompat.wrap(binding.ivMic.drawable),
                        ContextCompat.getColor(this@MainActivity, R.color.purple_700)
                    )
                    Log.i(TAG, "onError: $error")
                }

                override fun onResults(results : Bundle?) {
                    DrawableCompat.setTint(
                        DrawableCompat.wrap(binding.ivMic.drawable),
                        ContextCompat.getColor(this@MainActivity, R.color.purple_700)
                    )

                    val matches = results?.getStringArrayList(SpeechRecognizer.RESULTS_RECOGNITION)
                    val sTextFromSpeech : String =
                        if (!matches.isNullOrEmpty()) {
                            matches[0].toString()
                        } else {
                            ""
                        }
                    binding.etStt.setText(sTextFromSpeech)
                    Log.i(TAG, "onResults: $sTextFromSpeech")
                }

                override fun onPartialResults(partialResults : Bundle?) {
                    val data = partialResults?.getStringArrayList(
                        SpeechRecognizer.RESULTS_RECOGNITION
                    )

                    val sTextFromSpeech : String = if (data != null) {
                        data[0].toString()
                    } else {
                        ""
                    }
                    binding.etStt.setText(sTextFromSpeech)
                    Log.i(TAG, "onPartialResults: $sTextFromSpeech")
                }

                override fun onEvent(eventType : Int, params : Bundle?) {
                    Log.i(TAG, "onEvent: $eventType")
                }
            }
        )
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
        speechRecognizer.destroy()
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
        private const val MIC_PERMISSION_CODE = 100
    }

}