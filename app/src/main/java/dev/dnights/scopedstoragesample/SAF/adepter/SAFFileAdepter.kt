package dev.dnights.scopedstoragesample.SAF.adepter

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import dev.dnights.scopedstoragesample.R
import dev.dnights.scopedstoragesample.SAF.data.SAFFileData

class SAFFileAdepter(val clickListeners: FileClickListeners) :
    RecyclerView.Adapter<SAFFileAdepter.FileViewHolder>() {

    private val fileList: MutableList<SAFFileData> = mutableListOf()

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): FileViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_saf_file, parent, false)
        return FileViewHolder(view)
    }

    override fun getItemCount(): Int = fileList.size

    override fun onBindViewHolder(holder: FileViewHolder, position: Int) {
        holder.name.text = fileList[position].name
        holder.type.text = fileList[position].type
        holder.isDirectory.text = fileList[position].isDirectory.toString()
        holder.uri.text = fileList[position].uri.toString()

        holder.root.setOnClickListener {
            clickListeners.onClick(fileList[position])
        }

        holder.root.setOnLongClickListener {
            clickListeners.onLongClick(fileList[position])
            true
        }
    }

    fun setFileList(newList: List<SAFFileData>) {
        synchronized(fileList) {
            fileList.clear()
            fileList.addAll(newList)
            notifyDataSetChanged()
        }
    }

    class FileViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val root = view
        val name = view.findViewById<TextView>(R.id.file_name)
        val type = view.findViewById<TextView>(R.id.file_type)
        val isDirectory = view.findViewById<TextView>(R.id.file_is_directory)
        val uri = view.findViewById<TextView>(R.id.file_uri)
    }
}

interface FileClickListeners {
    fun onClick(mediaFileData: SAFFileData)
    fun onLongClick(mediaFileData: SAFFileData)
}
