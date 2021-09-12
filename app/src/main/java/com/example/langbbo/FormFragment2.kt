package com.example.langbbo

import android.app.Activity
import android.content.Intent
import android.graphics.Color
import android.media.MediaPlayer
import android.media.MediaRecorder
import android.net.Uri
import android.os.Bundle
import android.os.Environment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.fragment.app.Fragment
import com.bumptech.glide.Glide
import com.google.firebase.auth.FirebaseAuth
import kotlinx.android.synthetic.main.fragment_form2.view.*
import java.io.File
import java.io.IOException

class FormFragment2 : Fragment() {

    var fView : View? = null
    var auth: FirebaseAuth? = null

    private var recorder: MediaRecorder? = null
    private var player: MediaPlayer? = null
    private var file: File? = null
    var audioUrlString: String? = null
    var playOn: Boolean = false

    var uid: String = ""

    var pickImageFromAlbum = 0
    var imageUrl : Uri? = null


    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {

        fView = inflater.inflate(R.layout.fragment_form2, container, false)

        val block = arguments?.getString("block")

        block?.let{
            fView!!.form_second_lo.visibility = View.GONE
            fView!!.form_block_lo.visibility = View.VISIBLE
        }




        //////이미지
        fView!!.form_imageUpload_lo.setOnClickListener {
            var photoPickerIntent = Intent(Intent.ACTION_PICK)
            photoPickerIntent.type = "image/*"
            startActivityForResult(photoPickerIntent, pickImageFromAlbum)
        }


        ////// 녹음

        val path = File(Environment.getExternalStorageDirectory().path)
        try {
            file = File(path, "temporary.3gp")
            file!!.createNewFile()
        } catch (e: IOException) {}

        fView!!.form_audio_btn1.setOnClickListener {

            if(!playOn) {
                if (audioUrlString != null) {
                    val builder = AlertDialog.Builder(context!!)
                    with(builder) {
                        setMessage("이미 녹음 파일이 있습니다. 녹음하게 되면 기존의 녹음 기록은 사라집니다.")
                        //setTitle()
                        setPositiveButton("시작") { dialogInterface, i ->

                            // 기존 녹음 파일 없애기
                            fView!!.form_audioUrl_tv.text = ""

                            recorder = MediaRecorder().apply {
                                setAudioSource(MediaRecorder.AudioSource.MIC)
                                //setOutputFormat(MediaRecorder.OutputFormat.MPEG_2_TS)
                                setOutputFormat(MediaRecorder.OutputFormat.THREE_GPP)

                                setOutputFile(file?.absolutePath)
                                setAudioEncoder(MediaRecorder.AudioEncoder.AMR_NB)
                                setMaxDuration(60000)


                                prepare()

                                try {
                                } catch (e: IOException) {
                                }
                                // java.lang.IllegalStateException -> prepare 먼저 선언해야하는데...

                                start()
                            }

                            fView!!.form_audio_btn1.isEnabled = false
                            fView!!.form_audio_btn1.setBackgroundColor(Color.parseColor("#81c147"))
                            fView!!.form_audio_btn2.setBackgroundColor(Color.parseColor("#808080"))
                            fView!!.form_audio_btn3.isEnabled = false

                            fView!!.form_audio_iv.setImageResource(R.drawable.circle_shape_red)
                            fView!!.form_audio_tv.text = "녹음 중"
                        }
                        setNegativeButton("취소") { dialogInterface, i -> }
                        show()
                    }
                } else {

                    recorder = MediaRecorder().apply {
                        setAudioSource(MediaRecorder.AudioSource.MIC)
                        //setOutputFormat(MediaRecorder.OutputFormat.MPEG_2_TS)
                        setOutputFormat(MediaRecorder.OutputFormat.THREE_GPP)

                        setOutputFile(file?.absolutePath)
                        setAudioEncoder(MediaRecorder.AudioEncoder.AMR_NB)
                        setMaxDuration(30000)

                        prepare()
                        try {

                        } catch (e: IOException) {
                        }

                        start()
                    }

                    fView!!.form_audio_btn1.isEnabled = false
                    fView!!.form_audio_btn1.setBackgroundColor(Color.parseColor("#81c147"))
                    fView!!.form_audio_btn2.setBackgroundColor(Color.parseColor("#808080"))
                    fView!!.form_audio_btn3.isEnabled = false

                    fView!!.form_audio_iv.setImageResource(R.drawable.circle_shape_red)
                    fView!!.form_audio_tv.text = "녹음 중"

                }
            }
            else{
                Toast.makeText(context, "정지 후 녹음이 가능합니다.", Toast.LENGTH_LONG).show()
            }

        }

        // 실행 마치고 저장하는 곳
        fView!!.form_audio_btn2.setOnClickListener {

            if (recorder != null) {

                recorder?.apply {
                    stop()
                    release()
                    audioUrlString = file?.absolutePath

                    // formActivity에서 저장하기 위함
                    fView!!.form_audioUrl_tv.text = audioUrlString

                    player = MediaPlayer().apply {
                        try {
                            setDataSource(audioUrlString)
                            prepare()
                        } catch (e: IOException) {
                            //Log.e(LOG_TAG, "prepare() failed")
                        }
                    }

                    player?.setOnCompletionListener(MediaPlayer.OnCompletionListener {
                        fView!!.form_audio_btn1.setBackgroundColor(Color.parseColor("#808080"))
                        fView!!.form_audio_btn3.setBackgroundColor(Color.parseColor("#808080"))
                        fView!!.form_audio_btn3.text = "실행"

                        playOn = false
                    })


                }
                recorder = null


                fView!!.form_audio_btn1.isEnabled = true
                fView!!.form_audio_btn1.setBackgroundColor(Color.parseColor("#808080"))
                fView!!.form_audio_btn2.setBackgroundColor(Color.parseColor("#81c147"))
                fView!!.form_audio_btn3.isEnabled = true

                fView!!.form_audio_iv.setImageResource(R.drawable.circle_shape_green)
                fView!!.form_audio_tv.text = "녹음 완료(${Utils.getDuration(player?.duration!!)})"
            } else {
                if(audioUrlString != null){
                    Toast.makeText(context, "녹음이 이미 완료되었습니다. 다시 녹음을 원하시면 녹음 시작을 눌러주세요.", Toast.LENGTH_LONG).show()
                }
                else{
                    Toast.makeText(context, "녹음이 시작되지 않았습니다.", Toast.LENGTH_LONG).show()
                }
            }
        }


        // 실행해보는 버튼
        fView!!.form_audio_btn3.setOnClickListener {
            if (audioUrlString != null) {
                if(!playOn) {

                    player?.start()

                    /*
                    player = MediaPlayer().apply {
                        try {
                            setDataSource(audioUrl)
                            prepare()
                            start()
                        } catch (e: IOException) {
                            //Log.e(LOG_TAG, "prepare() failed")
                        }
                    }
                     */

                    //sView!!.ssf_audio_tv.text = Utils.getDuration(player?.duration!!)

                    fView!!.form_audio_btn1.setBackgroundColor(Color.parseColor("#808080"))
                    fView!!.form_audio_btn3.setBackgroundColor(Color.parseColor("#81c147"))
                    fView!!.form_audio_btn3.text = "정지"

                    playOn = true
                }
                else{

                    player?.pause()
                    player?.seekTo(0)

                    //player?.release()
                    //player = null

                    fView!!.form_audio_btn1.setBackgroundColor(Color.parseColor("#808080"))
                    fView!!.form_audio_btn3.setBackgroundColor(Color.parseColor("#808080"))
                    fView!!.form_audio_btn3.text = "실행"

                    playOn = false
                }

            } else {

                Toast.makeText(context, "실행할 녹음 파일이 없습니다.", Toast.LENGTH_LONG).show()
            }

        }


        return fView
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)

        if(requestCode == pickImageFromAlbum){
            if(resultCode == Activity.RESULT_OK){

                imageUrl = data?.data
                //fView!!.form_image_iv.setImageURI(uriPhoto)
                Glide.with(context!!).load(imageUrl).into(fView!!.form_image_iv)
                fView!!.form_image_iv.visibility = View.VISIBLE
                fView!!.form_imageUrl_tv.text = imageUrl.toString()
            }
        }
    }
}