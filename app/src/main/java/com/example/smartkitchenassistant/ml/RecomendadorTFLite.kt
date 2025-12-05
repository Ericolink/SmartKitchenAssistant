package com.example.smartkitchenassistant.ml

import android.content.Context
import org.tensorflow.lite.Interpreter
import java.nio.ByteBuffer
import java.nio.ByteOrder

class RecomendadorTFLite(context: Context) {

    private val interpreter: Interpreter

    init {
        // Asegúrate que el archivo sea el modelo MEJORADO
        val model = context.assets.open("modelo_recomendador.tflite").readBytes()

        val buffer = ByteBuffer.allocateDirect(model.size)
            .order(ByteOrder.nativeOrder())
        buffer.put(model)

        interpreter = Interpreter(buffer)
    }

    /**
     *  NUEVO MÉTODO  →  10 FEATURES
     */
    fun predecir(
        matchPct: Float,
        missingCount: Float,
        ingredientOverlapRatio: Float,
        ingredientCountScore: Float,
        categoryMatch: Float,
        areaMatch: Float,
        favoriteNameSimilarity: Float,
        searchSimilarity: Float,
        ingredientVectorScore: Float,
        pastInteractionScore: Float
    ): Float {

        // Tensor input shape: [1, 10]
        val input = arrayOf(
            floatArrayOf(
                matchPct,
                missingCount,
                ingredientOverlapRatio,
                ingredientCountScore,
                categoryMatch,
                areaMatch,
                favoriteNameSimilarity,
                searchSimilarity,
                ingredientVectorScore,
                pastInteractionScore
            )
        )

        // output [1, 1]
        val output = Array(1) { FloatArray(1) }

        interpreter.run(input, output)

        return output[0][0]
    }
}
