package com.sim.myaudio

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.RadioButton
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView

class MusicAdapter(var musicList: List<Music>, private val listener: OnItemClickListener) :
    RecyclerView.Adapter<MusicAdapter.ViewHolder>() {

    interface OnItemClickListener {
        fun onEditClick(position: Int)
        fun onDeleteClick(position: Int)
    }

    inner class ViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val titleTextView: TextView = itemView.findViewById(R.id.titleTextView)
        val artistTextView: TextView = itemView.findViewById(R.id.artistTextView)
        val genreTextView: TextView = itemView.findViewById(R.id.genreTextView)
        val durationTextView: TextView = itemView.findViewById(R.id.durationTextView)
        val editButton: Button = itemView.findViewById(R.id.btnEdit)
        val deleteButton: Button = itemView.findViewById(R.id.btnDelete)
        val radioButton: RadioButton = itemView.findViewById(R.id.radioBtnSelect)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val itemView = LayoutInflater.from(parent.context).inflate(R.layout.item_music, parent, false)
        return ViewHolder(itemView)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val currentItem = musicList[position]

        holder.titleTextView.text = currentItem.title
        holder.artistTextView.text = currentItem.artist
        holder.genreTextView.text = currentItem.genre
        holder.durationTextView.text = currentItem.duration.toString()
        holder.radioButton.isChecked = currentItem.isSelected

        holder.radioButton.setOnClickListener {
            // 라디오 버튼을 클릭할 때 선택 상태 업데이트
            currentItem.isSelected = true
            notifyDataSetChanged()
        }

        holder.itemView.setOnClickListener {
            // 리스트 아이템을 클릭할 때 선택 상태 업데이트
            currentItem.isSelected = !currentItem.isSelected
            notifyDataSetChanged()
        }

        holder.editButton.setOnClickListener {
            listener.onEditClick(position)
        }

        holder.deleteButton.setOnClickListener {
            listener.onDeleteClick(position)
        }
    }

    override fun getItemCount() = musicList.size

    fun updateData(newMusicList: List<Music>) {
        musicList = newMusicList
        notifyDataSetChanged()

        // 선택 상태 초기화
        for (music in musicList) {
            music.isSelected = false
        }
    }
}
