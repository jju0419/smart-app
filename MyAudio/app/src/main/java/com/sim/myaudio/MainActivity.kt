package com.sim.myaudio

import android.content.ContentValues
import android.content.Context
import android.media.MediaPlayer
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.os.Environment
import android.view.View
import android.widget.*
import androidx.appcompat.app.AlertDialog
import androidx.core.app.ActivityCompat
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import java.io.File

class MainActivity : AppCompatActivity() {





    lateinit var btnPlay: Button //재생 버튼
    lateinit var btnStop: Button //중지 버튼
    lateinit var tvMP3: TextView // 음악 파일명 표시
    lateinit var pbMP3: ProgressBar //재생중 표시

    lateinit var mp3List: ArrayList<String> //음악파일 저장
    lateinit var selectedMP3: String // 선택한 음악 파일명
    //경로: /sdcard/mp3
    var mp3Path = Environment.getExternalStorageDirectory().path + "/mp3/"

    lateinit var mPlayer: MediaPlayer //음악과 동영상을 재생


    private lateinit var dbHelper: DBHelper
    private lateinit var recyclerView: RecyclerView
    private lateinit var musicAdapter: MusicAdapter


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        title = "MP3 플레이어"
        //권한 요청
        ActivityCompat.requestPermissions(this,
            arrayOf(android.Manifest.permission.WRITE_EXTERNAL_STORAGE),
            Context.MODE_PRIVATE)

        // SDCard의 파일을 List에 저장
        mp3List = ArrayList()



        dbHelper = DBHelper(this)
        recyclerView = findViewById(R.id.recyclerView)
        musicAdapter = MusicAdapter(getMusicListFromDB(), object : MusicAdapter.OnItemClickListener {
            override fun onEditClick(position: Int) {
                // 음원 정보 수정을 위한 처리 (수정 다이얼로그 등을 표시)
                showEditMusicDialog(position)
            }

            override fun onDeleteClick(position: Int) {
                // 음원 정보 삭제를 위한 처리
                deleteMusic(position)
            }
        })
        recyclerView.adapter = musicAdapter
        recyclerView.layoutManager = LinearLayoutManager(this)


        var listFiles = File(mp3Path).listFiles()
        var fileName: String
        var extName: String
        // 파일의 개수만큼 반복문 실행
        for (file in listFiles!!) {
            fileName = file.name
            //파일명, 확장자명 추출
            extName = fileName.substring(fileName.length - 3)
             //확장자명이 mp3라면 리스트에 추가
            if (extName == "mp3")
                mp3List.add(fileName)
        }

        //처음 실행하면 선택된 mp3파일을 리스트뷰의 첫 번째 mp3파일로 설정
        selectedMP3 = mp3List[0]

        btnPlay = findViewById<Button>(R.id.btnPlay) //듣기 버튼
        btnStop = findViewById<Button>(R.id.btnStop) // 중지 버튼
        tvMP3 = findViewById<TextView>(R.id.tvMP3) //mp3파일명 표시
        pbMP3 = findViewById<ProgressBar>(R.id.pbMP3) // 재생중 표시

        btnPlay.setOnClickListener {
           //듣기버튼 누르면 음악 재생
            mPlayer = MediaPlayer()
            //MediaPlayer를 생성하고 선택한 파일 재생
            selectedMP3 = getSelectedMusic()?.fileName ?: ""

            if (selectedMP3.isNotEmpty()) {
                // MediaPlayer를 생성하고 선택한 파일 재생
                mPlayer.setDataSource(mp3Path + selectedMP3)
                mPlayer.prepare()
                mPlayer.start()
                btnPlay.isClickable = false
                btnStop.isClickable = true
                tvMP3.text = "실행중인 음악 :  $selectedMP3"
                pbMP3.visibility = View.VISIBLE
            } else {
                Toast.makeText(this, "선택된 음악이 없습니다.", Toast.LENGTH_SHORT).show()
            }


        }

        btnStop.setOnClickListener {
            //중지 버튼 누르면 음악 중지 및 UI 활성화/비활성화화
             mPlayer.stop()
            mPlayer.reset()
            btnPlay.isClickable = true
            btnStop.isClickable = false
            tvMP3.text = "실행중인 음악 :  "
            pbMP3.visibility = View.INVISIBLE
        }
        btnStop.isClickable = false


        val btnAdd = findViewById<Button>(R.id.btnAdd)
        btnAdd.setOnClickListener {
            showAddMusicDialog()
        }

        val seekBarVolume = findViewById<SeekBar>(R.id.seekBarVolume)

        mPlayer = MediaPlayer()

        // 음악 볼륨 조절 SeekBar 초기화
        seekBarVolume.progress = 50 // 초기 볼륨 값 (필요에 따라 조절)
        seekBarVolume.setOnSeekBarChangeListener(object : SeekBar.OnSeekBarChangeListener {
            override fun onProgressChanged(seekBar: SeekBar?, progress: Int, fromUser: Boolean) {
                // SeekBar 값에 따라 볼륨 조절
                val volume = progress / 100.0f
                mPlayer.setVolume(volume, volume)
            }

            override fun onStartTrackingTouch(seekBar: SeekBar?) {}

            override fun onStopTrackingTouch(seekBar: SeekBar?) {}
        })



    }

    private fun showAddMusicDialog() {
        val dialogBuilder = AlertDialog.Builder(this)
        val inflater = this.layoutInflater
        val dialogView = inflater.inflate(R.layout.dialog_add_music, null)
        dialogBuilder.setView(dialogView)

        val etTitle = dialogView.findViewById<EditText>(R.id.etTitle)
        val etArtist = dialogView.findViewById<EditText>(R.id.etArtist)
        val etGenre = dialogView.findViewById<EditText>(R.id.etGenre)
        val etDuration = dialogView.findViewById<EditText>(R.id.etDuration)

        dialogBuilder.setTitle("Add Music")
        dialogBuilder.setPositiveButton("Add") { _, _ ->
            val title = etTitle.text.toString()
            val artist = etArtist.text.toString()
            val genre = etGenre.text.toString()
            val duration = etDuration.text.toString().toIntOrNull()

            if (title.isNotEmpty() && artist.isNotEmpty() && genre.isNotEmpty() && duration != null) {
                addMusicToDB(title, artist, genre, duration)
            } else {
                Toast.makeText(this, "Please fill in all fields", Toast.LENGTH_SHORT).show()
            }
        }
        dialogBuilder.setNegativeButton("Cancel") { dialog, _ ->
            dialog.cancel()
        }

        val dialog = dialogBuilder.create()
        dialog.show()
    }

    private fun addMusicToDB(title: String, artist: String, genre: String, duration: Int) {
        val db = dbHelper.writableDatabase
        val values = ContentValues().apply {
            put(DBHelper.COLUMN_TITLE, title)
            put(DBHelper.COLUMN_ARTIST, artist)
            put(DBHelper.COLUMN_GENRE, genre)
            put(DBHelper.COLUMN_DURATION, duration)
        }

        val newRowId = db.insert(DBHelper.TABLE_NAME, null, values)
        if (newRowId != -1L) {
            Toast.makeText(this, "Music added successfully", Toast.LENGTH_SHORT).show()
            // 여기에서 추가한 음원 정보를 활용할 수 있습니다.
        } else {
            Toast.makeText(this, "Error adding music", Toast.LENGTH_SHORT).show()
        }

        db.close()
    }

    // DBHelper에서 음원 리스트 가져오기
    private fun getMusicListFromDB(): List<Music> {
        val musicList = mutableListOf<Music>()
        val db = dbHelper.readableDatabase
        val cursor = db.query(
            DBHelper.TABLE_NAME,
            arrayOf(
                DBHelper.COLUMN_ID,
                DBHelper.COLUMN_TITLE,
                DBHelper.COLUMN_ARTIST,
                DBHelper.COLUMN_GENRE,
                DBHelper.COLUMN_DURATION
            ),
            null, null, null, null, null
        )

        while (cursor.moveToNext()) {
            val id = cursor.getLong(cursor.getColumnIndex(DBHelper.COLUMN_ID))
            val title = cursor.getString(cursor.getColumnIndex(DBHelper.COLUMN_TITLE))
            val artist = cursor.getString(cursor.getColumnIndex(DBHelper.COLUMN_ARTIST))
            val genre = cursor.getString(cursor.getColumnIndex(DBHelper.COLUMN_GENRE))
            val duration = cursor.getInt(cursor.getColumnIndex(DBHelper.COLUMN_DURATION))

            val fileName = findMatchingMusicFile(title) ?: ""

            val music = Music(id, title, artist, genre, duration,fileName)
            musicList.add(music)
        }

        cursor.close()
        db.close()

        return musicList
    }

    private fun showEditMusicDialog(position: Int) {
        val music = musicAdapter.musicList[position]

        val builder = AlertDialog.Builder(this)
        builder.setTitle("음원 정보 수정")

        val view = layoutInflater.inflate(R.layout.dialog_edit_music, null)
        builder.setView(view)

        val etTitle = view.findViewById<EditText>(R.id.etEditTitle)
        val etArtist = view.findViewById<EditText>(R.id.etEditArtist)
        val etGenre = view.findViewById<EditText>(R.id.etEditGenre)
        val etDuration = view.findViewById<EditText>(R.id.etEditDuration)

        // 현재 음원 정보로 EditText 초기화
        etTitle.setText(music.title)
        etArtist.setText(music.artist)
        etGenre.setText(music.genre)
        etDuration.setText(music.duration.toString())

        builder.setPositiveButton("확인") { _, _ ->
            // 수정된 정보로 DB 업데이트
            updateMusic(position, etTitle.text.toString(), etArtist.text.toString(), etGenre.text.toString(), etDuration.text.toString().toInt())
        }

        builder.setNegativeButton("취소") { dialog, _ ->
            dialog.dismiss()
        }

        builder.show()
    }
    private fun updateMusic(position: Int, title: String, artist: String, genre: String, duration: Int) {
        val music = musicAdapter.musicList[position]
        val db = dbHelper.writableDatabase

        val values = ContentValues().apply {
            put(DBHelper.COLUMN_TITLE, title)
            put(DBHelper.COLUMN_ARTIST, artist)
            put(DBHelper.COLUMN_GENRE, genre)
            put(DBHelper.COLUMN_DURATION, duration)
        }

        db.update(DBHelper.TABLE_NAME, values, "${DBHelper.COLUMN_ID} = ?", arrayOf(music.id.toString()))
        db.close()

        // 업데이트 후 RecyclerView 갱신
        musicAdapter.musicList = getMusicListFromDB()
        musicAdapter.notifyDataSetChanged()
    }


    private fun deleteMusic(position: Int) {
        val music = musicAdapter.musicList[position]
        val db = dbHelper.writableDatabase
        db.delete(DBHelper.TABLE_NAME, "${DBHelper.COLUMN_ID} = ?", arrayOf(music.id.toString()))
        db.close()

        // 삭제 후 RecyclerView 갱신
        musicAdapter.musicList = getMusicListFromDB()
        musicAdapter.notifyDataSetChanged()
    }

    private fun findMatchingMusicFile(title: String): String? {
        val listFiles = File(mp3Path).listFiles()

        for (file in listFiles!!) {
            val fileName = file.name
            if (fileName == "$title.mp3") {
                return fileName
            }
        }
        return null
    }

    private fun getSelectedMusic(): Music? {
        return musicAdapter.musicList.find { it.isSelected }
    }
}
