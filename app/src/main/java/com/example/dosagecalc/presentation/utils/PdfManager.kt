package com.example.dosagecalc.presentation.utils

import android.content.Context
import android.content.Intent
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.graphics.RectF
import android.graphics.Typeface
import android.graphics.pdf.PdfDocument
import androidx.core.content.FileProvider
import com.example.dosagecalc.domain.model.DosageResult
import com.example.dosagecalc.domain.model.Drug
import com.example.dosagecalc.domain.model.Patient
import com.example.dosagecalc.presentation.ui.util.formatDose
import com.example.dosagecalc.presentation.ui.util.label
import java.io.File
import java.io.FileOutputStream
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter
import java.util.Locale

object PdfManager {
    private val CLR_PRIMARY = Color.parseColor("#6760F6")
    private val CLR_PRIMARY_CONT = Color.parseColor("#E8DEFF")
    private val CLR_ON_PRIMARY = Color.WHITE
    private val CLR_SECONDARY = Color.parseColor("#148F84")
    private val CLR_SECONDARY_CONT = Color.parseColor("#B8EAE7")
    private val CLR_ERROR = Color.parseColor("#BA1A1A")
    private val CLR_ERROR_CONT = Color.parseColor("#FFDAD6")
    private val CLR_BACKGROUND = Color.parseColor("#FAF7F2")
    private val CLR_SURFACE = Color.WHITE
    private val CLR_SURFACE_VAR = Color.parseColor("#EFE9E1")
    private val CLR_ON_SURFACE = Color.parseColor("#1C1B1F")
    private val CLR_ON_SURFACE_VAR = Color.parseColor("#49454F")
    private val CLR_OUTLINE = Color.parseColor("#B8B0A6")

    private const val PW = 595f
    private const val PH = 842f
    private const val MX = 44f
    private const val CW = PW - 2 * MX

    fun generateAndSharePdf(
        context: Context,
        drug: Drug,
        patient: Patient?,
        result: DosageResult.Success,
    ) {
        val document = PdfDocument()
        val page = document.startPage(PdfDocument.PageInfo.Builder(PW.toInt(), PH.toInt(), 1).create())
        val canvas = page.canvas

        drawBackground(canvas)
        var y = drawHeader(canvas, drug, LocalDateTime.now())
        y = drawPatientSection(canvas, patient, y)
        y = drawDoseHero(canvas, result, y)
        y = drawDrugSection(canvas, drug, y)
        y = drawFormulaSection(canvas, result, y)
        if (result.cappedToMaxDose || drug.alert.isNotBlank()) {
            y = drawAlertSection(canvas, result, drug, y)
        }
        drawFooter(canvas)

        document.finishPage(page)

        try {
            val file = File(context.cacheDir, "DosageCalc_${drug.name.replace(" ", "_")}_${System.currentTimeMillis()}.pdf")
            document.writeTo(FileOutputStream(file))
            document.close()

            val uri = FileProvider.getUriForFile(context, "${context.packageName}.fileprovider", file)
            val intent =
                Intent(Intent.ACTION_SEND).apply {
                    type = "application/pdf"
                    putExtra(Intent.EXTRA_STREAM, uri)
                    putExtra(Intent.EXTRA_SUBJECT, "Referto DosageCalc – ${drug.name}")
                    addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
                }
            context.startActivity(Intent.createChooser(intent, "Condividi il referto…"))
        } catch (e: Exception) {
            e.printStackTrace()
            document.close()
        }
    }

    private fun drawBackground(canvas: Canvas) {
        canvas.drawRect(0f, 0f, PW, PH, fill(CLR_BACKGROUND))
    }

    private fun drawHeader(
        canvas: Canvas,
        drug: Drug,
        now: LocalDateTime,
    ): Float {
        val headerH = 130f
        val cornerRadius = 32f

        val path =
            android.graphics.Path().apply {
                addRoundRect(
                    RectF(0f, 0f, PW, headerH),
                    floatArrayOf(0f, 0f, 0f, 0f, cornerRadius, cornerRadius, cornerRadius, cornerRadius),
                    android.graphics.Path.Direction.CW,
                )
            }
        canvas.save()
        canvas.clipPath(path)

        val shader =
            android.graphics.LinearGradient(
                0f,
                0f,
                PW,
                headerH,
                CLR_PRIMARY,
                Color.parseColor("#9390FA"),
                android.graphics.Shader.TileMode.CLAMP,
            )
        canvas.drawRect(0f, 0f, PW, headerH, fill(CLR_PRIMARY).also { it.shader = shader })
        canvas.drawCircle(PW - 20f, -10f, 90f, fill(Color.WHITE).also { it.alpha = 18 })

        canvas.restore()

        canvas.drawText(
            "DosageCalc",
            MX,
            50f,
            txt(24f, CLR_ON_PRIMARY, bold = true, serif = true),
        )
        canvas.drawText(
            "Referto Clinico di Dosaggio",
            MX,
            72f,
            txt(12f, CLR_ON_PRIMARY).also { it.alpha = 204 },
        )

        val datePaint =
            txt(10f, CLR_ON_PRIMARY).also {
                it.alpha = 178
                it.textAlign = Paint.Align.RIGHT
            }
        val formatter = DateTimeFormatter.ofPattern("dd MMM yyyy  HH:mm", Locale.getDefault())
        canvas.drawText(now.format(formatter), PW - MX, 50f, datePaint)
        canvas.drawText(
            drug.name,
            PW - MX,
            68f,
            datePaint.also {
                it.textSize = 12f
                it.alpha = 220
            },
        )

        return headerH + 24f
    }

    private fun drawPatientSection(
        canvas: Canvas,
        patient: Patient?,
        startY: Float,
    ): Float {
        val label = "PAZIENTE"
        val rows: List<Pair<String, String>> =
            if (patient != null) {
                listOf(
                    "Nome" to "${patient.name} ${patient.surname}",
                    "Peso" to "${patient.weightKg} kg",
                ) + (if (patient.heightCm != null) listOf("Altezza" to "${patient.heightCm} cm") else emptyList()) +
                    listOf("Età" to "${patient.ageYears} anni") +
                    (if (patient.hasRenalImpairment) listOf("Nota" to "Insufficienza renale") else emptyList()) +
                    (if (patient.hasHepaticImpairment) listOf("Nota" to "Insufficienza epatica") else emptyList())
            } else {
                listOf("" to "Calcolo anonimo")
            }

        val rowH = 22f
        val cardH = 28f + rows.size * rowH + 12f
        drawCard(canvas, startY, cardH, CLR_SURFACE_VAR)
        drawSectionLabel(canvas, label, startY + 18f, CLR_ON_SURFACE_VAR)
        var y = startY + 36f
        for ((k, v) in rows) {
            if (k.isNotEmpty()) {
                canvas.drawText(k, MX + 12f, y, txt(11f, CLR_ON_SURFACE_VAR))
                canvas.drawText(v, MX + 12f + 80f, y, txt(11f, CLR_ON_SURFACE, bold = true))
            } else {
                canvas.drawText(v, MX + 12f, y, txt(11f, CLR_ON_SURFACE_VAR))
            }
            y += rowH
        }
        return startY + cardH + 14f
    }

    private fun drawDoseHero(
        canvas: Canvas,
        result: DosageResult.Success,
        startY: Float,
    ): Float {
        val cardH = 110f
        drawCard(canvas, startY, cardH, CLR_PRIMARY_CONT)

        canvas.drawText(
            "Dose Calcolata",
            PW / 2f,
            startY + 22f,
            txt(10f, CLR_PRIMARY).also {
                it.textAlign = Paint.Align.CENTER
                it.alpha = 200
            },
        )

        val doseStr = formatDoseRange(result)
        canvas.drawText(
            doseStr,
            PW / 2f,
            startY + 66f,
            txt(38f, CLR_PRIMARY, bold = true, serif = true).also { it.textAlign = Paint.Align.CENTER },
        )

        canvas.drawText(
            result.unit,
            PW / 2f,
            startY + 88f,
            txt(14f, CLR_PRIMARY, serif = true).also {
                it.textAlign = Paint.Align.CENTER
                it.alpha = 200
            },
        )

        if (result.cappedToMaxDose) {
            val badge = "⚠  ridotta al massimo consentito"
            val bPaint = txt(9f, CLR_ERROR)
            val bW = bPaint.measureText(badge) + 20f
            val bx = (PW - bW) / 2f
            canvas.drawRoundRect(RectF(bx, startY + 94f, bx + bW, startY + 106f), 6f, 6f, fill(CLR_ERROR_CONT))
            canvas.drawText(badge, PW / 2f - bW / 2f + 10f, startY + 104f, bPaint)
        }

        return startY + cardH + 14f
    }

    private fun drawDrugSection(
        canvas: Canvas,
        drug: Drug,
        startY: Float,
    ): Float {
        val rows =
            listOf(
                "Farmaco" to drug.name,
                "Indicazione" to drug.indication,
                "Formula" to drug.formulaType.label(),
            )
        val cardH = 28f + rows.size * 22f + 12f
        drawCard(canvas, startY, cardH, CLR_SURFACE)
        drawSectionLabel(canvas, "DETTAGLI FARMACO", startY + 18f, CLR_ON_SURFACE_VAR)
        var y = startY + 36f
        for ((k, v) in rows) {
            canvas.drawText(k, MX + 12f, y, txt(11f, CLR_ON_SURFACE_VAR))
            canvas.drawText(v, MX + 12f + 90f, y, txt(11f, CLR_ON_SURFACE, bold = true))
            y += 22f
        }

        val badgeText = "  ${drug.formulaType.label()}  "
        val badgePaint = txt(9f, CLR_PRIMARY)
        val badgeW = badgePaint.measureText(badgeText)
        canvas.drawRoundRect(
            RectF(
                MX + 12f + 90f + badgePaint.measureText(rows.last().second) + 8f,
                startY + cardH - 26f,
                MX + 12f + 90f + badgePaint.measureText(rows.last().second) + 8f + badgeW,
                startY + cardH - 14f,
            ),
            4f,
            4f,
            fill(CLR_PRIMARY_CONT),
        )
        return startY + cardH + 14f
    }

    private fun drawFormulaSection(
        canvas: Canvas,
        result: DosageResult.Success,
        startY: Float,
    ): Float {
        val formulaLines = wrapText(result.formula, 68)
        val cardH = 32f + formulaLines.size * 17f + 12f
        drawCard(canvas, startY, cardH, CLR_SURFACE)
        drawSectionLabel(canvas, "FORMULA APPLICATA", startY + 18f, CLR_ON_SURFACE_VAR)

        canvas.drawRoundRect(
            RectF(MX + 8f, startY + 24f, MX + CW - 8f, startY + cardH - 8f),
            16f,
            16f,
            fill(CLR_PRIMARY_CONT).also { it.alpha = 120 },
        )
        var y = startY + 40f
        for (line in formulaLines) {
            canvas.drawText(line, MX + 18f, y, txt(10f, CLR_PRIMARY).also { it.typeface = Typeface.MONOSPACE })
            y += 17f
        }
        if (result.source.isNotBlank()) {
            y += 4f
            canvas.drawText("Fonte: ${result.source}", MX + 18f, y, txt(9f, CLR_ON_SURFACE_VAR))
        }
        return startY + cardH + 14f
    }

    private fun drawAlertSection(
        canvas: Canvas,
        result: DosageResult.Success,
        drug: Drug,
        startY: Float,
    ): Float {
        val lines = mutableListOf<String>()
        if (result.cappedToMaxDose) lines += "La dose è stata ridotta al massimo consentito."
        if (drug.alert.isNotBlank()) lines += wrapText(drug.alert, 68)
        if (lines.isEmpty()) return startY

        val cardH = 28f + lines.size * 18f + 12f
        drawCard(canvas, startY, cardH, CLR_ERROR_CONT)
        drawSectionLabel(canvas, "⚠  AVVISO CLINICO", startY + 18f, CLR_ERROR)
        var y = startY + 36f
        for (line in lines) {
            canvas.drawText(line, MX + 12f, y, txt(11f, CLR_ERROR).also { it.alpha = 210 })
            y += 18f
        }
        return startY + cardH + 14f
    }

    private fun drawFooter(canvas: Canvas) {
        val lineY = PH - 54f
        canvas.drawRect(MX, lineY, MX + CW, lineY + 0.8f, fill(CLR_OUTLINE))
        val disc =
            "DISCLAIMER: Strumento a finalità esclusivamente didattiche. " +
                "Non sostituisce la valutazione clinica del medico. Verificare sempre il dosaggio sulla scheda tecnica ufficiale (RCP/AIFA)."
        val discLines = wrapText(disc, 88)
        var y = lineY + 14f
        for (line in discLines) {
            canvas.drawText(line, MX, y, txt(8f, CLR_ON_SURFACE_VAR).also { it.alpha = 150 })
            y += 12f
        }
        canvas.drawText(
            "DosageCalc  •  1 / 1",
            PW - MX,
            PH - 12f,
            txt(8f, CLR_ON_SURFACE_VAR).also {
                it.textAlign = Paint.Align.RIGHT
                it.alpha = 120
            },
        )
    }

    private fun drawCard(
        canvas: Canvas,
        y: Float,
        h: Float,
        color: Int,
    ) {
        val rect = RectF(MX, y, MX + CW, y + h)
        canvas.drawRoundRect(rect, 24f, 24f, fill(color))
    }

    private fun drawSectionLabel(
        canvas: Canvas,
        text: String,
        y: Float,
        color: Int,
    ) {
        canvas.drawText(text, MX + 12f, y, txt(9f, color).also { it.letterSpacing = 0.08f })
    }

    private fun fill(color: Int) =
        Paint(Paint.ANTI_ALIAS_FLAG).apply {
            this.color = color
            style = Paint.Style.FILL
        }

    private fun txt(
        size: Float,
        color: Int,
        bold: Boolean = false,
        serif: Boolean = false,
    ) = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        this.color = color
        textSize = size
        isAntiAlias = true
        typeface =
            when {
                serif && bold -> Typeface.create(Typeface.SERIF, Typeface.BOLD)
                serif -> Typeface.create(Typeface.SERIF, Typeface.NORMAL)
                bold -> Typeface.create(Typeface.SANS_SERIF, Typeface.BOLD)
                else -> Typeface.SANS_SERIF
            }
    }

    private fun formatDoseRange(result: DosageResult.Success): String =
        if (result.totalDoseMax != null) {
            "${result.totalDose.formatDose()} – ${result.totalDoseMax.formatDose()}"
        } else {
            result.totalDose.formatDose()
        }

    private fun wrapText(
        text: String,
        maxChars: Int,
    ): List<String> {
        val words = text.split(" ")
        val lines = mutableListOf<String>()
        var current = StringBuilder()
        for (word in words) {
            if (current.length + word.length + 1 > maxChars) {
                if (current.isNotEmpty()) lines += current.toString().trim()
                current = StringBuilder(word)
            } else {
                if (current.isNotEmpty()) current.append(' ')
                current.append(word)
            }
        }
        if (current.isNotEmpty()) lines += current.toString().trim()
        return lines.ifEmpty { listOf(text) }
    }
}
