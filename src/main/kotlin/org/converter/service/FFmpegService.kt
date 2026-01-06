package org.converter.service

import org.springframework.stereotype.Service
import java.io.BufferedReader
import java.io.File
import java.io.InputStreamReader

@Service
class FFmpegService {

    fun convertToMp4(inputFile: File): File {
        val outputFile = File.createTempFile("output-", ".mp4")

        // ffmpeg command logic:
        // -i: input file
        // -c:v libx264: Use H.264 video codec (standard for MP4)
        // -crf 28: Constant Rate Factor. Higher = smaller size/lower quality.
        //          23 is default, 28 is visibly smaller but acceptable for web.
        // -c:a aac: Convert audio to AAC (standard for MP4)
        val command = listOf(
            "ffmpeg",
            "-y", // Overwrite output files without asking
            "-i", inputFile.absolutePath,
            "-c:v", "libx264",
            "-crf", "28",
            "-c:a", "aac",
            outputFile.absolutePath
        )

        val processBuilder = ProcessBuilder(command)
        processBuilder.redirectErrorStream(true) // Merge error and output streams

        try {
            val process = processBuilder.start()

            // Log output to the console for debugging
            val reader = BufferedReader(InputStreamReader(process.inputStream))
            var line: String?
            while (reader.readLine().also { line = it } != null) {
                println(line)
            }

            val exitCode = process.waitFor()
            if (exitCode != 0) {
                throw RuntimeException("FFmpeg conversion failed with exit code $exitCode")
            }

            // Clean up the input file after conversion
            inputFile.delete()

        } catch (e: Exception) {
            throw RuntimeException("Error during video conversion: ${e.message}", e)
        }

        return outputFile
    }
}

