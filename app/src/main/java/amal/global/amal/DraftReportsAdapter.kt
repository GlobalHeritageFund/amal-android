package amal.global.amal

import android.content.Context
import android.content.SharedPreferences
import androidx.recyclerview.widget.RecyclerView
import android.view.ViewGroup

interface DraftReportsAdapterDelegate {
    fun noDraftReportsFound()
    fun draftReportsFound()
}

class DraftReportsAdapter(val context: Context, val delegate: DraftReportsAdapterDelegate) : RecyclerView.Adapter<SubtitleViewHolder>() {

    companion object {
        val TAG = "Draft Reports Adapter"
    }

    val draftReports: MutableList<ReportDraft> = mutableListOf()

    init {

        val draftReportPreferenceName = "DraftReportPreferences" //change so don't have this hard coded in two places
        val preferences: SharedPreferences by lazy {
            context.getSharedPreferences(draftReportPreferenceName, Context.MODE_PRIVATE)
        }
        val draftReportsString = preferences.getString(draftReportPreferenceName,"")
        if (draftReportsString == "") {
            delegate.noDraftReportsFound()
        } else {
            draftReports.addAll(ReportDraft.jsonAdapter.fromJson(draftReportsString)!!.list)
            delegate.draftReportsFound()
            notifyDataSetChanged()
        }

    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): SubtitleViewHolder {
        val itemView = parent.inflate(R.layout.subtitle_row, false)
        return SubtitleViewHolder(itemView)
    }

    override fun onBindViewHolder(holder: SubtitleViewHolder, position: Int) {
        val draftReport = draftReports[position]
        if (draftReport.title == "") holder.title.text = "No title assigned"
        else holder.title.text = draftReport.title

        val count = draftReport.images.count()
        holder.subtitle.text = if (count == 1) "1 item" else count.toString() + " items"

        draftReport.images.firstOrNull()?.let { it.load(context).centerCrop().into(holder.imageView) }
    }

    override fun getItemCount(): Int {
        return draftReports.size
    }
}
