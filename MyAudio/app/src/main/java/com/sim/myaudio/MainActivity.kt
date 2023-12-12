package com.sim.myaudio

import android.content.Context
import android.media.MediaPlayer
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.os.Environment
import android.view.View
import android.widget.*
import androidx.core.app.ActivityCompat
import java.io.File

class MainActivity : AppCompatActivity() {

    var listViewMP3: ListView? = null //리스트 뷰
    lateinit var btnPlay: Button //재생 버튼
    lateinit var btnStop: Button //중지 버튼
    lateinit var tvMP3: TextView // 음악 파일명 표시
    lateinit var pbMP3: ProgressBar //재생중 표시

    lateinit var mp3List: ArrayList<String> //음악파일 저장
    lateinit var selectedMP3: String // 선택한 음악 파일명
    //경로: /sdcard/mp3
    var mp3Path = Environment.getExternalStorageDirectory().path + "/mp3/"

    lateinit var mPlayer: MediaPlayer //음악과 동영상을 재생

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

        //리스트뷰에 mp3List 배열 내용을 출력
        var listViewMP3 = findViewById<ListView>(R.id.listViewMP3)
        var adapter = ArrayAdapter(this,
            android.R.layout.simple_list_item_single_choice, mp3List)
        listViewMP3.choiceMode = ListView.CHOICE_MODE_SINGLE
        listViewMP3.adapter = adapter
        listViewMP3.setItemChecked(0, true)
        //리스트뷰의 항목을 선택하면 해당 파일을 selectedMP3에 저장
        listViewMP3.setOnItemClickListener{ arg0, arg1, arg2, arg3 ->
            selectedMP3 = mp3List[arg2]
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
            mPlayer.setDataSource(mp3Path + selectedMP3)
            Toast.makeText(this,mp3Path + selectedMP3,
                Toast.LENGTH_LONG )
            mPlayer.prepare()
            mPlayer.start()
            btnPlay.isClickable = false
            btnStop.isClickable = true
            tvMP3.text = "실행중인 음악 :  $selectedMP3"
            pbMP3.visibility = View.VISIBLE

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


    }
}