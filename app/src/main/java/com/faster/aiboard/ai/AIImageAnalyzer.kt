package com.faster.aiboard.ai

import android.graphics.Bitmap
import android.util.Base64
import java.io.ByteArrayOutputStream

class AIImageAnalyzer(private val aiService: AIService) {

    suspend fun analyzeArea(
        areaBitmap: Bitmap,
        question: String
    ): String {
        val base64 = bitmapToBase64(areaBitmap)
        return aiService.analyzeImage(base64, question)
    }

    private fun bitmapToBase64(bitmap: Bitmap): String {
        val stream = ByteArrayOutputStream()
        bitmap.compress(Bitmap.CompressFormat.PNG, 90, stream)
        val bytes = stream.toByteArray()
        return Base64.encodeToString(bytes, Base64.NO_WRAP)
    }
}
