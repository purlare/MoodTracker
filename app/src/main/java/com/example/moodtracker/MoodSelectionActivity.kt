package com.example.moodtracker

import android.content.Intent
import android.os.Bundle
import android.widget.Button
import android.widget.CalendarView
import android.widget.EditText
import android.widget.ImageButton
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.google.firebase.firestore.FirebaseFirestore
import java.text.SimpleDateFormat
import java.util.*

class MoodSelectionActivity : AppCompatActivity() {

    private lateinit var btnHappy: ImageButton
    private lateinit var btnNeutral: ImageButton
    private lateinit var btnAnxiety: ImageButton
    private lateinit var btnLove: ImageButton
    private lateinit var btnSad: ImageButton
    private lateinit var btnAngry: ImageButton
    private lateinit var etNotes: EditText
    private lateinit var calendarView: CalendarView
    private lateinit var btnAddMood: Button
    private lateinit var btnViewEntries: Button

    private val db = FirebaseFirestore.getInstance()
    private val userEntriesCollection = db.collection("user_entries")

    private val moodImageMap = mapOf(
        "Счастливое настроение" to R.drawable.happy,
        "Нейтральное настроение" to R.drawable.neutral,
        "Тревожное настроение" to R.drawable.anxiety,
        "Романтичное настроение" to R.drawable.love,
        "Печальное настроение" to R.drawable.sad,
        "Злобное настроение" to R.drawable.angry
    )

    private lateinit var selectedDate: String

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_mood_selection)

        btnHappy = findViewById(R.id.btnHappy)
        btnNeutral = findViewById(R.id.btnNeutral)
        btnAnxiety = findViewById(R.id.btnAnxiety)
        btnLove = findViewById(R.id.btnLove)
        btnSad = findViewById(R.id.btnSad)
        btnAngry = findViewById(R.id.btnAngry)
        etNotes = findViewById(R.id.etNotes)
        calendarView = findViewById(R.id.calendarView)
        btnAddMood = findViewById(R.id.btnAddMood)
        btnViewEntries = findViewById(R.id.btnViewEntries)

        calendarView.setOnDateChangeListener { view, year, month, dayOfMonth ->
            val calendar = Calendar.getInstance()
            calendar.set(year, month, dayOfMonth)
            val sdf = SimpleDateFormat("dd-MM-yyyy", Locale.getDefault())
            selectedDate = sdf.format(calendar.time)
        }

        btnHappy.setOnClickListener {
            onMoodSelected("Счастливое настроение")
        }
        btnNeutral.setOnClickListener {
            onMoodSelected("Нейтральное настроение")
        }
        btnAnxiety.setOnClickListener {
            onMoodSelected("Тревожное настроение")
        }
        btnLove.setOnClickListener {
            onMoodSelected("Романтичное настроение")
        }
        btnSad.setOnClickListener {
            onMoodSelected("Печальное настроение")
        }
        btnAngry.setOnClickListener {
            onMoodSelected("Злобное настроение")
        }

        btnAddMood.setOnClickListener {
            val selectedMood = etNotes.text.toString()
            val notes = etNotes.text.toString()

            if (!::selectedDate.isInitialized) {
                showToast("Выберите дату")
                return@setOnClickListener
            }

            val entry = hashMapOf(
                "mood" to selectedMood,
                "notes" to notes,
                "date" to selectedDate
            )

            val entryId = intent.getStringExtra("entry_id")
            if (entryId != null) {
                userEntriesCollection.document(entryId)
                    .set(entry)
                    .addOnSuccessListener {
                        showToast("Запись обновлена")
                    }
                    .addOnFailureListener { e ->
                        showToast("Ошибка при обновлении записи: $e")
                    }
            } else {
                userEntriesCollection.add(entry)
                    .addOnSuccessListener { documentReference ->
                        showToast("Запись добавлена")
                    }
                    .addOnFailureListener { e ->
                        showToast("Ошибка при добавлении записи: $e")
                    }
            }
        }

        btnViewEntries.setOnClickListener {
            startActivity(Intent(this@MoodSelectionActivity, EntriesActivity::class.java))
        }
    }

    private fun onMoodSelected(mood: String) {
        etNotes.setText(mood)
    }

    private fun showToast(message: String) {
        Toast.makeText(this, message, Toast.LENGTH_SHORT).show()
    }
}
