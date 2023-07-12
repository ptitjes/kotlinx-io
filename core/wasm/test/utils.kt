/*
 * Copyright 2010-2023 JetBrains s.r.o. and Kotlin Programming Language contributors.
 * Use of this source code is governed by the Apache 2.0 license that can be found in the LICENSE.txt file.
 */

package kotlinx.io

actual fun createTempFile(): String = TODO("Files support is missing for Wasm")
actual fun deleteFile(path: String): Unit = TODO("Files support is missing for Wasm")
