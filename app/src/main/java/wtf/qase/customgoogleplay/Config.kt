package wtf.qase.customgoogleplay

object Config {

    data class XAPKFile(val isMain: Boolean, val fileVersion: Int, val fileSize: Long)
    data class XFile(val dir: String, val length: Long)

    val xAPKS = arrayOf(XAPKFile(
        true,
        16,
        10951976L
    ))

    val xFILES = linkedMapOf(
        "data.img" to
            XFile("/", 10240000L),

        "image.jpg" to
            XFile("/", 711745L)
    )
}
