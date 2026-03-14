package my.gov.met.nwsmalaysia.data.model

import com.google.gson.annotations.SerializedName

data class WarningResponse(
    @SerializedName("valid_from")   val validFrom:   String? = null,
    @SerializedName("valid_to")     val validTo:     String? = null,
    @SerializedName("heading_bm")   val headingBm:   String? = null,
    @SerializedName("heading_en")   val headingEn:   String? = null,
    @SerializedName("text_bm")      val textBm:      String? = null,
    @SerializedName("text_en")      val textEn:      String? = null,
    @SerializedName("instruction_en") val instructionEn: String? = null,
    @SerializedName("instruction_bm") val instructionBm: String? = null,
    @SerializedName("warning_issue") val warningIssue: WarningIssue? = null,
    val state:    String? = null,
    val district: String? = null
)

data class WarningIssue(
    @SerializedName("title_bm") val titleBm: String? = null,
    @SerializedName("title_en") val titleEn: String? = null,
    val issued: String? = null
)
