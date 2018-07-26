package com.github.quadflask.cnj.messaging

import org.springframework.util.Assert
import java.io.File
import java.nio.file.Files
import java.nio.file.StandardCopyOption

fun mv(`in`: File, out: File) {
    Assert.isTrue(out.exists() || out.mkdirs())
    val target = File(out, `in`.name)
    Files.copy(`in`.toPath(), target.toPath(), StandardCopyOption.REPLACE_EXISTING)
}