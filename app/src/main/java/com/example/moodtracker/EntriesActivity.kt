package com.example.moodtracker

import android.app.AlertDialog
import android.content.Intent
import android.os.Bundle
import android.widget.Button
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.DialogFragment
import com.google.firebase.firestore.FirebaseFirestore

class EntriesActivity : AppCompatActivity() {

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

    companion object {
        const val EDIT_ENTRY_REQUEST_CODE = 123
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_entries)

        val entriesContainer: LinearLayout = findViewById(R.id.entriesContainer)

        userEntriesCollection.get()
            .addOnSuccessListener { documents ->
                for (document in documents) {
                    val entry = document.toObject(UserEntry::class.java)
                    addEntryToUI(entry, document.id, entriesContainer)
                }
            }
            .addOnFailureListener { exception ->
                showToast("Ошибка при загрузке данных: $exception")
            }
    }

    private fun addEntryToUI(entry: UserEntry, entryId: String, entriesContainer: LinearLayout) {
        val entryView = layoutInflater.inflate(R.layout.item_entry, null)

        val imageView: ImageView = entryView.findViewById(R.id.entryImage)
        val dateTextView: TextView = entryView.findViewById(R.id.entryDate)
        val notesTextView: TextView = entryView.findViewById(R.id.entryNotes)
        val editButton: Button = entryView.findViewById(R.id.btnEdit)
        val deleteButton: Button = entryView.findViewById(R.id.btnDelete)

        val resourceId = moodImageMap[entry.mood] ?: R.drawable.default_mood_image
        imageView.setImageResource(resourceId)

        dateTextView.text = entry.date
        notesTextView.text = entry.notes

        deleteButton.setOnClickListener {
            showDeleteConfirmationDialog(entryId, entriesContainer)
        }

        editButton.setOnClickListener {
            val intent = Intent(this@EntriesActivity, MoodSelectionActivity::class.java).apply {
                putExtra("entry_id", entryId)
                putExtra("mood", entry.mood)
                putExtra("notes", entry.notes)
                putExtra("date", entry.date)
                putExtra("is_edit", true)
            }
            startActivityForResult(intent, EDIT_ENTRY_REQUEST_CODE)
        }

        entriesContainer.addView(entryView)
    }

    private fun showDeleteConfirmationDialog(entryId: String, entriesContainer: LinearLayout) {
        val dialog = DeleteEntryDialogFragment(entryId, entriesContainer)
        dialog.show(supportFragmentManager, "DeleteEntryDialogFragment")
    }

    fun deleteEntry(entryId: String, entriesContainer: LinearLayout) {
        userEntriesCollection.document(entryId)
            .delete()
            .addOnSuccessListener {
                showToast("Запись удалена")
                entriesContainer.removeAllViews()
                reloadEntries(entriesContainer)
            }
            .addOnFailureListener { exception ->
                showToast("Ошибка при удалении записи: $exception")
            }
    }

    private fun showToast(message: String) {
        Toast.makeText(this, message, Toast.LENGTH_SHORT).show()
    }

    private fun reloadEntries(entriesContainer: LinearLayout) {
        userEntriesCollection.get()
            .addOnSuccessListener { documents ->
                for (document in documents) {
                    val entry = document.toObject(UserEntry::class.java)
                    addEntryToUI(entry, document.id, entriesContainer)
                }
            }
            .addOnFailureListener { exception ->
                showToast("Ошибка при загрузке данных: $exception")
            }
    }

    class DeleteEntryDialogFragment(
        private val entryId: String,
        private val entriesContainer: LinearLayout
    ) : DialogFragment() {

        override fun onCreateDialog(savedInstanceState: Bundle?): AlertDialog {
            return activity?.let {
                val builder = AlertDialog.Builder(it)
                builder.apply {
                    setTitle("Подтверждение удаления")
                    setMessage("Вы уверены, что хотите удалить эту запись?")
                    setPositiveButton("Удалить") { dialog, _ ->
                        (activity as EntriesActivity).deleteEntry(entryId, entriesContainer)
                        dialog.dismiss()
                    }
                    setNegativeButton("Отмена") { dialog, _ ->
                        dialog.cancel()
                    }
                }
                builder.create()
            } ?: throw IllegalStateException("Activity cannot be null")
        }
    }
}
