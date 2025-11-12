package com.athleticai.app.ui.theme

import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.net.Uri
import android.os.Build
import androidx.annotation.RequiresApi
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.toArgb
import androidx.palette.graphics.Palette
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.InputStream

/**
 * Extracts dominant colors from images to create custom theme palettes
 */
object ColorExtractor {
    
    /**
     * Extract theme colors from an image URI
     */
    suspend fun extractColorsFromImage(
        context: Context,
        imageUri: Uri
    ): Result<CustomThemeColors> = withContext(Dispatchers.IO) {
        try {
            val bitmap = loadBitmapFromUri(context, imageUri)
            val palette = Palette.from(bitmap).generate()
            
            // Extract various swatches for comprehensive theming
            val vibrantSwatch = palette.vibrantSwatch
            val dominantSwatch = palette.dominantSwatch
            val mutedSwatch = palette.mutedSwatch
            val darkVibrantSwatch = palette.darkVibrantSwatch
            val lightVibrantSwatch = palette.lightVibrantSwatch
            
            // Determine seed color (prioritize vibrant colors)
            val seedColor = vibrantSwatch?.rgb 
                ?: dominantSwatch?.rgb 
                ?: palette.swatches.firstOrNull()?.rgb
                ?: Color(0xFF6200EA).toArgb()
            
            // Extract primary color (most vibrant or dominant)
            val primaryColor = vibrantSwatch?.rgb 
                ?: dominantSwatch?.rgb 
                ?: seedColor
            
            // Extract secondary color (complementary to primary)
            val secondaryColor = when {
                mutedSwatch != null -> mutedSwatch.rgb
                darkVibrantSwatch != null -> darkVibrantSwatch.rgb
                palette.swatches.size > 1 -> palette.swatches[1].rgb
                else -> adjustColorBrightness(primaryColor, -0.2f)
            }
            
            // Extract tertiary color (accent)
            val tertiaryColor = when {
                lightVibrantSwatch != null -> lightVibrantSwatch.rgb
                palette.swatches.size > 2 -> palette.swatches[2].rgb
                else -> adjustColorBrightness(primaryColor, 0.3f)
            }
            
            // Extract neutral color for backgrounds
            val neutralColor = mutedSwatch?.rgb 
                ?: palette.swatches.lastOrNull()?.rgb
                ?: Color(0xFF121212).toArgb()
            
            Result.success(
                CustomThemeColors(
                    seedColor = seedColor.toLong(),
                    primary = primaryColor.toLong(),
                    secondary = secondaryColor.toLong(),
                    tertiary = tertiaryColor.toLong(),
                    neutral = neutralColor.toLong()
                )
            )
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
    
    /**
     * Load and resize bitmap from URI for efficient processing
     */
    private fun loadBitmapFromUri(context: Context, uri: Uri): Bitmap {
        val inputStream: InputStream = context.contentResolver.openInputStream(uri)
            ?: throw IllegalArgumentException("Cannot open input stream from URI")
        
        // First decode to get dimensions
        val options = BitmapFactory.Options().apply {
            inJustDecodeBounds = true
        }
        BitmapFactory.decodeStream(inputStream, null, options)
        inputStream.close()
        
        // Calculate sample size for efficient loading (max 500x500)
        val targetSize = 500
        val sampleSize = calculateSampleSize(
            options.outWidth, 
            options.outHeight, 
            targetSize, 
            targetSize
        )
        
        // Load bitmap with calculated sample size
        val newInputStream = context.contentResolver.openInputStream(uri)
            ?: throw IllegalArgumentException("Cannot reopen input stream")
        
        val bitmap = BitmapFactory.Options().apply {
            inSampleSize = sampleSize
            inJustDecodeBounds = false
        }.let { opts ->
            BitmapFactory.decodeStream(newInputStream, null, opts)
        }
        
        newInputStream.close()
        
        return bitmap ?: throw IllegalArgumentException("Failed to decode bitmap")
    }
    
    /**
     * Calculate optimal sample size for bitmap loading
     */
    private fun calculateSampleSize(
        width: Int,
        height: Int,
        reqWidth: Int,
        reqHeight: Int
    ): Int {
        var inSampleSize = 1
        
        if (height > reqHeight || width > reqWidth) {
            val halfHeight = height / 2
            val halfWidth = width / 2
            
            while ((halfHeight / inSampleSize) >= reqHeight &&
                   (halfWidth / inSampleSize) >= reqWidth) {
                inSampleSize *= 2
            }
        }
        
        return inSampleSize
    }
    
    /**
     * Adjust color brightness for creating variations
     */
    private fun adjustColorBrightness(color: Int, factor: Float): Int {
        val a = (color shr 24) and 0xFF
        val r = ((color shr 16) and 0xFF).toFloat()
        val g = ((color shr 8) and 0xFF).toFloat()
        val b = (color and 0xFF).toFloat()
        
        val adjustedR = (r * (1 + factor)).coerceIn(0f, 255f).toInt()
        val adjustedG = (g * (1 + factor)).coerceIn(0f, 255f).toInt()
        val adjustedB = (b * (1 + factor)).coerceIn(0f, 255f).toInt()
        
        return (a shl 24) or (adjustedR shl 16) or (adjustedG shl 8) or adjustedB
    }
    
    /**
     * Generate Material3 tonal palette from seed color
     * This creates a comprehensive set of tones for Material You theming
     */
    @RequiresApi(Build.VERSION_CODES.S)
    fun generateTonalPalette(seedColor: Int): Map<Int, Color> {
        // Generate tones from 0 (black) to 100 (white)
        val tones = listOf(0, 10, 20, 30, 40, 50, 60, 70, 80, 90, 95, 99, 100)
        val palette = mutableMapOf<Int, Color>()
        
        tones.forEach { tone ->
            val adjustedColor = when (tone) {
                0 -> Color.Black
                100 -> Color.White
                else -> {
                    // Blend seed color with white/black based on tone
                    val factor = tone / 100f
                    blendColors(Color.Black, Color(seedColor), factor)
                }
            }
            palette[tone] = adjustedColor
        }
        
        return palette
    }
    
    /**
     * Blend two colors together
     */
    private fun blendColors(color1: Color, color2: Color, ratio: Float): Color {
        val inverseRatio = 1f - ratio
        return Color(
            red = color1.red * inverseRatio + color2.red * ratio,
            green = color1.green * inverseRatio + color2.green * ratio,
            blue = color1.blue * inverseRatio + color2.blue * ratio,
            alpha = color1.alpha * inverseRatio + color2.alpha * ratio
        )
    }
    
    /**
     * Validate if extracted colors provide sufficient contrast
     */
    fun validateColorContrast(colors: CustomThemeColors): Boolean {
        val primary = Color(colors.primary)
        val secondary = Color(colors.secondary)
        
        // Calculate relative luminance
        fun Color.luminance(): Float {
            val r = red
            val g = green
            val b = blue
            return 0.299f * r + 0.587f * g + 0.114f * b
        }
        
        // Ensure sufficient contrast between primary and secondary
        val primaryLuminance = primary.luminance()
        val secondaryLuminance = secondary.luminance()
        val contrastRatio = kotlin.math.abs(primaryLuminance - secondaryLuminance)
        
        return contrastRatio > 0.3f // Minimum contrast threshold
    }
}