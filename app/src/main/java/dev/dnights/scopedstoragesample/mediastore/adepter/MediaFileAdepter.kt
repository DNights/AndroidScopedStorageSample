package dev.dnights.scopedstoragesample.mediastore.adepter


import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import dev.dnights.scopedstoragesample.R
import dev.dnights.scopedstoragesample.mediastore.data.MediaFileData

class MediaFileAdepter(val clickListeners: FileClickListeners) :
    RecyclerView.Adapter<MediaFileAdepter.FileViewHolder>() {

    private val fileList: MutableList<MediaFileData> = mutableListOf()

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): FileViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_media_store_file, parent, false)
        return FileViewHolder(view)
    }

    override fun getItemCount(): Int = fileList.size

    override fun onBindViewHolder(holder: FileViewHolder, position: Int) {
        holder.id.text = fileList[position].id.toString()
        holder.name.text = fileList[position].displayName
        holder.date.text = fileList[position].dateTaken.toString()
        holder.uri.text = fileList[position].uri.toString()

        holder.root.setOnClickListener {
            clickListeners.onClick(fileList[position])
        }

        holder.root.setOnLongClickListener {
            clickListeners.onLongClick(fileList[position])
            true
        }
    }

    fun setFileList(newList: List<MediaFileData>) {
        synchronized(fileList) {
            fileList.clear()
            fileList.addAll(newList)
            notifyDataSetChanged()
        }
    }

    class FileViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val root = view
        val id = view.findViewById<TextView>(R.id.file_id)
        val name = view.findViewById<TextView>(R.id.file_name)
        val date = view.findViewById<TextView>(R.id.file_date)
        val uri = view.findViewById<TextView>(R.id.file_uri)
    }
}

interface FileClickListeners {
    fun onClick(mediaFileData: MediaFileData)
    fun onLongClick(mediaFileData: MediaFileData)
}
