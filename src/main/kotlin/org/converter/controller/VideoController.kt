package org.converter.controller

import org.converter.service.FFmpegService
import org.springframework.core.io.FileSystemResource
import org.springframework.core.io.Resource
import org.springframework.http.HttpHeaders
import org.springframework.http.MediaType
import org.springframework.http.ResponseEntity
import org.springframework.stereotype.Controller
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestParam
import org.springframework.web.multipart.MultipartFile
import java.io.File

@Controller
class VideoController(private val ffmpegService: FFmpegService) {

    @GetMapping("/")
    fun home(): String {
        return "index"
    }

    @PostMapping("/convert")
    fun convertVideo(@RequestParam("file") file: MultipartFile): ResponseEntity<Any> {
        // 1. Validate file
        if (file.isEmpty) {
            return ResponseEntity.badRequest().body("Please select a file to upload.")
        }

        // 2. Save uploaded file to a temp location
        val tempInputFile = File.createTempFile("input-", ".tmp")
        file.transferTo(tempInputFile)

        return try {
            // 3. Convert
            val convertedFile = ffmpegService.convertToMp4(tempInputFile)

            // 4. Send back the file
            val resource: Resource = FileSystemResource(convertedFile)

            ResponseEntity.ok()
                .contentType(MediaType.APPLICATION_OCTET_STREAM)
                .header(
                    HttpHeaders.CONTENT_DISPOSITION,
                    "attachment; filename=\"${
                        file.originalFilename?.replace(
                            Regex("\\.3gp$"),
                            ""
                        )
                    }.mp4\""
                )
                .body(resource)

        } catch (e: Exception) {
            ResponseEntity.internalServerError().body("Error converting video: ${e.message}")
        }
    }
}


