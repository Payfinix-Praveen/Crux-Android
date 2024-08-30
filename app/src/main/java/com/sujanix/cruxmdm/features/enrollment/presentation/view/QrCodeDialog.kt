package com.sujanix.cruxmdm.features.enrollment.presentation.view

import android.app.Dialog
import android.content.Context
import android.graphics.Bitmap
import android.graphics.Color
import android.os.CountDownTimer
import android.view.LayoutInflater
import com.bumptech.glide.Glide
import com.google.zxing.BarcodeFormat
import com.google.zxing.qrcode.QRCodeWriter
import com.sujanix.cruxmdm.R
import com.sujanix.cruxmdm.databinding.DialogQrCodeBinding
import java.util.concurrent.TimeUnit

class QrCodeDialog(
    context: Context,
    qrCodeUrl: String,
    duration: Long
) : Dialog(context) {

    private val binding: DialogQrCodeBinding = DialogQrCodeBinding.inflate(LayoutInflater.from(context))

    private val qrCodeWriter = QRCodeWriter()

    init {

        // Generate the QR code
        Glide.with(context)
            .load(qrCodeUrl)
            .into(binding.cvQrCode)

        // Set the countdown timer
        val timer = object : CountDownTimer(duration, 1000) {
            override fun onTick(millisUntilFinished: Long) {
                val minutes = TimeUnit.MILLISECONDS.toMinutes(millisUntilFinished)
                val seconds = TimeUnit.MILLISECONDS.toSeconds(millisUntilFinished) % 60
                binding.countdownTextView.text = String.format("%02d:%02d", minutes, seconds)
            }

            override fun onFinish() {
//                dismiss()
            }
        }
        timer.start()

        // Set the button listeners
        binding.apply {
            refreshButton.setOnClickListener {
                // Generate a new QR code and update the image
//                val newQrCodeBitmap = generateQrCode(qrCodeUrl)
                Glide.with(context)
                    .load(qrCodeUrl)
                    .into(cvQrCode)
//                qrCodeImageView.setImageBitmap(newQrCodeBitmap)
            }

            doneButton.setOnClickListener {
                dismiss()
            }
        }

        setContentView(binding.root)
    }

    private fun generateQrCode(qrCodeUrl: String): Bitmap {
        val bitMatrix = qrCodeWriter.encode(qrCodeUrl, BarcodeFormat.QR_CODE, 500, 500)
        val bitmap = Bitmap.createBitmap(bitMatrix.width, bitMatrix.height, Bitmap.Config.RGB_565)
        for (x in 0 until bitMatrix.width) {
            for (y in 0 until bitMatrix.height) {
                bitmap.setPixel(x, y, if (bitMatrix.get(x, y)) Color.BLACK else Color.WHITE)
            }
        }
        return bitmap
    }
}
