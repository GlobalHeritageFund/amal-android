package global.amal.app

import android.content.Context
import android.util.Log

object DraftUtils {
    private const val TAG = "DraftUtils"

    fun saveDraft(context: Context, draftToSave: ReportDraft) {
        val preferences = context.getSharedPreferences(ReportsAdapter.DRAFT_REPORT_PREFERENCE, Context.MODE_PRIVATE)
        val draftJson = preferences.getString(ReportsAdapter.DRAFT_REPORT_PREFERENCE, null)
        val currentDrafts: MutableList<ReportDraft> = if (draftJson != null) {
            try {
                ReportDraft.jsonAdapter.fromJson(draftJson)?.list?.toMutableList() ?: mutableListOf()
            } catch (e: Exception) {
                Log.e(TAG, "Error parsing existing drafts, starting fresh.", e)
                mutableListOf()
            }
        } else {
            mutableListOf()
        }

        // Remove existing draft with the same ID, if any, to update it
        val existingIndex = currentDrafts.indexOfFirst { it.id == draftToSave.id }
        if (existingIndex != -1) {
            currentDrafts.removeAt(existingIndex)
            Log.d(TAG, "Updating existing draft with ID: ${draftToSave.id}")
        } else {
            Log.d(TAG, "Adding new draft with ID: ${draftToSave.id}")
        }
        currentDrafts.add(draftToSave) // Add the new/updated draft

        // Save the updated list back to preferences
        val editor = preferences.edit()
        val newJson = ReportDraft.jsonAdapter.toJson(ReportDraft.Companion.DraftWrapper(currentDrafts))
        editor.putString(ReportsAdapter.DRAFT_REPORT_PREFERENCE, newJson)
        editor.apply()
        Log.d(TAG, "Draft list saved to preferences.")
    }

    fun deleteDraft(context: Context, draftId: String) {
        val preferences = context.getSharedPreferences(ReportsAdapter.DRAFT_REPORT_PREFERENCE, Context.MODE_PRIVATE)
        val jsonString = preferences.getString(ReportsAdapter.DRAFT_REPORT_PREFERENCE, null)
        if (!jsonString.isNullOrEmpty()) {
            try {
                val wrapper = ReportDraft.jsonAdapter.fromJson(jsonString)
                if (wrapper != null) {
                    val drafts = wrapper.list.toMutableList()
                    val removed = drafts.removeAll { it.id == draftId }
                    if (removed) {
                        val editor = preferences.edit()
                        val newJson = ReportDraft.jsonAdapter.toJson(ReportDraft.Companion.DraftWrapper(drafts))
                        editor.putString(ReportsAdapter.DRAFT_REPORT_PREFERENCE, newJson)
                        editor.apply()
                        Log.d(TAG, "Successfully deleted draft $draftId from preferences.")
                    } else {
                        Log.w(TAG,"Draft $draftId not found for deletion.")
                    }
                }
            } catch (e: Exception) {
                Log.e(TAG, "Error processing drafts for deletion", e)
            }
        } else {
            Log.w(TAG, "No drafts found in preferences to delete from.")
        }
    }
}