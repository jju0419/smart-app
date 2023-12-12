package com.sim.myaudio

data class Music(
    val id: Long,
    val title: String,
    val artist: String,
    val genre: String,
    val duration: Int,
    var fileName: String = "", // 음원 파일 이름 속성 추가

    var isSelected: Boolean = false
)
