package wtf.qase.customgoogleplay

object Config {

    data class XAPKFile(val isMain: Boolean, val fileVersion: Int, val fileSize: Long)
    data class XFile(val dir: String, val length: Long)

    val xAPKS = arrayOf(XAPKFile(
        true,
        4,
        666154207L
    ))

    val xFILES = linkedMapOf(
        "mtvu.mbtiles" to
            XFile("/osmdroid/maps", 619723776L),

        "addresses.sqlite" to
            XFile("/osmdroid", 19939328L),

        "spatialite.sqlite" to
            XFile("/osmdroid", 266915840L)
    )
}
